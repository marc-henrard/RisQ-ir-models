/**
 * Copyright (C) 2017 - present by Marc Henrard.
 */
package marc.henrard.murisq.pricer.swap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.Payment;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.basics.index.IborIndexObservation;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.pricer.DiscountFactors;
import com.opengamma.strata.pricer.ZeroRateSensitivity;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.product.common.PayReceive;
import com.opengamma.strata.product.rate.FixedRateComputation;
import com.opengamma.strata.product.rate.IborRateComputation;
import com.opengamma.strata.product.rate.OvernightCompoundedRateComputation;
import com.opengamma.strata.product.rate.RateComputation;
import com.opengamma.strata.product.swap.NotionalExchange;
import com.opengamma.strata.product.swap.RateAccrualPeriod;
import com.opengamma.strata.product.swap.RatePaymentPeriod;
import com.opengamma.strata.product.swap.ResolvedSwap;
import com.opengamma.strata.product.swap.ResolvedSwapLeg;
import com.opengamma.strata.product.swap.SwapLegType;
import com.opengamma.strata.product.swap.SwapPaymentEvent;
import com.opengamma.strata.product.swap.SwapPaymentPeriod;

/**
 * Computes cash flow equivalent of products.
 * <p>
 * Reference: Henrard, M. The Irony in the derivatives discounting Part II: the crisis. Wilmott Journal, 2010, 2, 301-316.
 */
public class CashFlowEquivalentCalculator {
  
  /**
   * Computes cash flow equivalent of swap.
   * <p>
   * The return type is {@code ResolvedSwapLeg} in which individual payments are
   * represented in terms of {@code NotionalExchange}.
   * 
   * @param swap  the swap product
   * @param multicurve  the multi-curve rates provider
   * @return the cash flow equivalent
   */
  public static ResolvedSwapLeg cashFlowEquivalentSwap(ResolvedSwap swap, RatesProvider multicurve) {
    
    List<SwapPaymentEvent> cfEquivalent = new ArrayList<>();
    for(ResolvedSwapLeg leg: swap.getLegs()) {
      if(leg.getType().equals(SwapLegType.FIXED)) {
        cfEquivalent.addAll(cashFlowEquivalentFixedLeg(leg, multicurve).getPaymentEvents());
      } else if(leg.getType().equals(SwapLegType.IBOR)) {
        cfEquivalent.addAll(cashFlowEquivalentIborLeg(leg, multicurve).getPaymentEvents());
      } else if(leg.getType().equals(SwapLegType.OVERNIGHT)) {
        cfEquivalent.addAll(cashFlowEquivalentOnLeg(leg, multicurve).getPaymentEvents());
      } else {
        throw new IllegalArgumentException("leg type must be FIXED, IBOR or OVERNIGHT");
      }
    }
    ResolvedSwapLeg leg = ResolvedSwapLeg.builder()
        .paymentEvents(cfEquivalent)
        .payReceive(PayReceive.RECEIVE)
        .type(SwapLegType.OTHER)
        .build();
    return leg;
  }
  
  /**
   * Computes cash flow equivalent of fixed leg.
   * <p>
   * The return type is {@code ResolvedSwapLeg} in which individual payments are
   * represented in terms of {@code NotionalExchange}.
   * 
   * @param fixedLeg  the fixed leg
   * @param multicurve  the multi-curve rates provider
   * @return the cash flow equivalent
   */
  public static ResolvedSwapLeg cashFlowEquivalentFixedLeg(ResolvedSwapLeg fixedLeg, RatesProvider multicurve) {

    ArgChecker.isTrue(fixedLeg.getType().equals(SwapLegType.FIXED), "Leg type should be FIXED");
    ArgChecker.isTrue(fixedLeg.getPaymentEvents().isEmpty(), "PaymentEvent should be empty");
    List<NotionalExchange> paymentEvents = new ArrayList<NotionalExchange>();
    for (SwapPaymentPeriod paymentPeriod : fixedLeg.getPaymentPeriods()) {
      ArgChecker.isTrue(paymentPeriod instanceof RatePaymentPeriod, "rate payment should be RatePaymentPeriod");
      RatePaymentPeriod ratePaymentPeriod = (RatePaymentPeriod) paymentPeriod;
      ArgChecker.isTrue(ratePaymentPeriod.getAccrualPeriods().size() == 1, "rate payment should not be compounding");
      RateAccrualPeriod rateAccrualPeriod = ratePaymentPeriod.getAccrualPeriods().get(0);
      double factor = rateAccrualPeriod.getYearFraction() *
          ((FixedRateComputation) rateAccrualPeriod.getRateComputation()).getRate();
      CurrencyAmount notional = ratePaymentPeriod.getNotionalAmount().multipliedBy(factor);
      LocalDate paymentDate = ratePaymentPeriod.getPaymentDate();
      NotionalExchange pay = NotionalExchange.of(notional, paymentDate);
      paymentEvents.add(pay);
    }
    ResolvedSwapLeg leg = ResolvedSwapLeg.builder()
        .paymentEvents(paymentEvents)
        .payReceive(PayReceive.RECEIVE)
        .type(SwapLegType.OTHER)
        .build();
    return leg;
  }
  
  /**
   * Computes cash flow equivalent of overnight leg.
   * <p>
   * Each payment period should contain one accrual period of type {@link OvernightCompoundedRateComputation}.
   * When the payment date is not equal to the end composition date, the start and end cashflow equivalent are
   * adjusted by the ratio of discount factors between the payment date and the end date.
   * <p>
   * The return type is {@code ResolvedSwapLeg} in which individual payments are
   * represented in terms of {@code NotionalExchange}.
   * 
   * @param onLeg  the overnight leg
   * @param multicurve  the multi-curve rates provider
   * @return the cash flow equivalent
   */
  public static ResolvedSwapLeg cashFlowEquivalentOnLeg(
      ResolvedSwapLeg onLeg, 
      RatesProvider multicurve) {
    
    Currency ccy = onLeg.getCurrency();
    ArgChecker.isTrue(onLeg.getType().equals(SwapLegType.OVERNIGHT), "Leg type should be OVERNIGHT");
    ArgChecker.isTrue(onLeg.getPaymentEvents().isEmpty(), "PaymentEvent should be empty");
    List<NotionalExchange> paymentEvents = new ArrayList<NotionalExchange>();
    for (SwapPaymentPeriod paymentPeriod : onLeg.getPaymentPeriods()) {
      ArgChecker.isTrue(paymentPeriod instanceof RatePaymentPeriod, 
          "rate payment should be RatePaymentPeriod");
      RatePaymentPeriod ratePaymentPeriod = (RatePaymentPeriod) paymentPeriod;
      ArgChecker.isTrue(ratePaymentPeriod.getAccrualPeriods().size() == 1, 
          "rate payment should not be compounding");
      RateAccrualPeriod rateAccrualPeriod = ratePaymentPeriod.getAccrualPeriods().get(0);
      CurrencyAmount notional = ratePaymentPeriod.getNotionalAmount();
      RateComputation rateComputation = rateAccrualPeriod.getRateComputation();
      ArgChecker.isTrue(rateComputation instanceof OvernightCompoundedRateComputation, 
          "RateComputation should be of type OvernightCompoundedRateComputation");
      OvernightCompoundedRateComputation onComputation = (OvernightCompoundedRateComputation) rateComputation;
      LocalDate startDate = rateAccrualPeriod.getStartDate();
      LocalDate endDate = rateAccrualPeriod.getEndDate();
      double computationAccrual = onComputation.getIndex().getDayCount().yearFraction(startDate, endDate);
      LocalDate paymentDate = ratePaymentPeriod.getPaymentDate();
      double paymentAccural = rateAccrualPeriod.getYearFraction();
      double payDateRatio =
          multicurve.discountFactor(ccy, paymentDate) / multicurve.discountFactor(ccy, endDate);
      NotionalExchange payStart = NotionalExchange.of(
          notional.multipliedBy(payDateRatio * paymentAccural / computationAccrual),
          startDate);
      double spread = rateAccrualPeriod.getSpread();
      NotionalExchange payEnd = NotionalExchange.of(
          notional.multipliedBy(-paymentAccural / computationAccrual + spread * paymentAccural),
          paymentDate);
      paymentEvents.add(payStart);
      paymentEvents.add(payEnd);
    }
    ResolvedSwapLeg leg = ResolvedSwapLeg.builder()
        .paymentEvents(paymentEvents)
        .payReceive(PayReceive.RECEIVE)
        .type(SwapLegType.OTHER)
        .build();
    return leg;
  }
  
  /**
   * Computes cash flow equivalent of Ibor leg.
   * <p>
   * The return type is {@code ResolvedSwapLeg} in which individual payments are
   * represented in terms of {@code NotionalExchange}.
   * 
   * @param iborLeg  the Ibor leg
   * @param ratesProvider  the rates provider
   * @return the cash flow equivalent
   */
  public static ResolvedSwapLeg cashFlowEquivalentIborLeg(ResolvedSwapLeg iborLeg, RatesProvider ratesProvider) {
    ArgChecker.isTrue(iborLeg.getType().equals(SwapLegType.IBOR), "Leg type should be IBOR");
    ArgChecker.isTrue(iborLeg.getPaymentEvents().isEmpty(), "PaymentEvent should be empty");
    List<NotionalExchange> paymentEvents = new ArrayList<NotionalExchange>();
    for (SwapPaymentPeriod paymentPeriod : iborLeg.getPaymentPeriods()) {
      ArgChecker.isTrue(paymentPeriod instanceof RatePaymentPeriod, "rate payment should be RatePaymentPeriod");
      RatePaymentPeriod ratePaymentPeriod = (RatePaymentPeriod) paymentPeriod;
      ArgChecker.isTrue(ratePaymentPeriod.getAccrualPeriods().size() == 1, "rate payment should not be compounding");
      RateAccrualPeriod rateAccrualPeriod = ratePaymentPeriod.getAccrualPeriods().get(0);
      CurrencyAmount notional = ratePaymentPeriod.getNotionalAmount();
      LocalDate paymentDate = ratePaymentPeriod.getPaymentDate();
      RateComputation rateComputation = rateAccrualPeriod.getRateComputation();
      ArgChecker.isTrue(rateComputation instanceof IborRateComputation);
      IborIndexObservation obs = ((IborRateComputation) rateComputation).getObservation();
      IborIndex index = obs.getIndex();
      LocalDate fixingStartDate = obs.getEffectiveDate();
      double fixingYearFraction = obs.getYearFraction();
      double beta = (1d + fixingYearFraction * ratesProvider.iborIndexRates(index).rate(obs)) *
          ratesProvider.discountFactor(paymentPeriod.getCurrency(), paymentPeriod.getPaymentDate()) /
          ratesProvider.discountFactor(paymentPeriod.getCurrency(), fixingStartDate);
      double ycRatio = rateAccrualPeriod.getYearFraction() / fixingYearFraction;
      NotionalExchange payStart = NotionalExchange.of(notional.multipliedBy(beta * ycRatio), fixingStartDate);
      NotionalExchange payEnd = NotionalExchange.of(notional.multipliedBy(-ycRatio), paymentDate);
      paymentEvents.add(payStart);
      paymentEvents.add(payEnd);
    }
    ResolvedSwapLeg leg = ResolvedSwapLeg.builder()
        .paymentEvents(paymentEvents)
        .payReceive(PayReceive.RECEIVE)
        .type(SwapLegType.OTHER)
        .build();
    return leg;
  }

  //-------------------------------------------------------------------------
  /**
   * Computes cash flow equivalent and sensitivity of swap.
   * <p>
   * The swap should be a fix-for-Ibor swap without compounding, and its swap legs should not involve {@code PaymentEvent}.
   * <p>
   * The return type is a map of {@code NotionalExchange} and {@code PointSensitivityBuilder}.
   * 
   * @param swap  the swap product
   * @param ratesProvider  the rates provider
   * @return the cash flow equivalent and sensitivity
   */
  public static ImmutableMap<Payment, PointSensitivityBuilder> cashFlowEquivalentAndSensitivitySwap(
      ResolvedSwap swap,
      RatesProvider multicurve) {
    
    Builder<Payment, PointSensitivityBuilder> cfEquivalentSensitivity  = 
        ImmutableMap.<Payment, PointSensitivityBuilder>builder();
    for(ResolvedSwapLeg leg: swap.getLegs()) {
      if(leg.getType().equals(SwapLegType.FIXED)) {
        cfEquivalentSensitivity.putAll(cashFlowEquivalentAndSensitivityFixedLeg(leg, multicurve));
      } else if(leg.getType().equals(SwapLegType.IBOR)) {
        cfEquivalentSensitivity.putAll(cashFlowEquivalentAndSensitivityIborLeg(leg, multicurve));
      } else if(leg.getType().equals(SwapLegType.OVERNIGHT)) {
        cfEquivalentSensitivity.putAll(cashFlowEquivalentAndSensitivityOnLeg(leg, multicurve));
      } else {
        throw new IllegalArgumentException("leg type must be FIXED, IBOR or OVERNIGHT");
      }
    }
    return cfEquivalentSensitivity.build();
  }

  /**
   * Computes cash flow equivalent and sensitivity of fixed leg.
   * <p>
   * The return type is a map of {@code NotionalExchange} and {@code PointSensitivityBuilder}.
   * 
   * @param fixedLeg  the fixed leg
   * @param ratesProvider  the rates provider
   * @return the cash flow equivalent and sensitivity
   */
  public static ImmutableMap<Payment, PointSensitivityBuilder> cashFlowEquivalentAndSensitivityFixedLeg(
      ResolvedSwapLeg fixedLeg,
      RatesProvider ratesProvider) {

    ArgChecker.isTrue(fixedLeg.getType().equals(SwapLegType.FIXED), "Leg type should be FIXED");
    ArgChecker.isTrue(fixedLeg.getPaymentEvents().isEmpty(), "PaymentEvent should be empty");
    Map<Payment, PointSensitivityBuilder> res = new HashMap<Payment, PointSensitivityBuilder>();
    for (SwapPaymentPeriod paymentPeriod : fixedLeg.getPaymentPeriods()) {
      ArgChecker.isTrue(paymentPeriod instanceof RatePaymentPeriod, "rate payment should be RatePaymentPeriod");
      RatePaymentPeriod ratePaymentPeriod = (RatePaymentPeriod) paymentPeriod;
      ArgChecker.isTrue(ratePaymentPeriod.getAccrualPeriods().size() == 1, "rate payment should not be compounding");
      RateAccrualPeriod rateAccrualPeriod = ratePaymentPeriod.getAccrualPeriods().get(0);
      double factor = rateAccrualPeriod.getYearFraction() *
          ((FixedRateComputation) rateAccrualPeriod.getRateComputation()).getRate();
      CurrencyAmount notional = ratePaymentPeriod.getNotionalAmount().multipliedBy(factor);
      LocalDate paymentDate = ratePaymentPeriod.getPaymentDate();
      Payment pay = Payment.of(notional, paymentDate);
      res.put(pay, PointSensitivityBuilder.none());
    }
    return ImmutableMap.copyOf(res);
  }

  /**
   * Computes cash flow equivalent of and sensitivity overnight leg.
   * <p>
   * Each payment period should contain one accrual period of type {@link OvernightCompoundedRateComputation}.
   * When the payment date is not equal to the end composition date, the start and end cashflow equivalent are
   * adjusted by the ratio of discount factors between the payment date and the end date.
   * <p>
   * The return type is a map of {@code NotionalExchange} and {@code PointSensitivityBuilder}.
   * 
   * @param onLeg  the overnight leg
   * @param multicurve  the multi-curve rates provider
   * @return the cash flow equivalent
   */
  public static ImmutableMap<Payment, PointSensitivityBuilder> cashFlowEquivalentAndSensitivityOnLeg(
      ResolvedSwapLeg onLeg, 
      RatesProvider multicurve) {

    Currency ccy = onLeg.getCurrency();
    DiscountFactors df = multicurve.discountFactors(ccy);
    ArgChecker.isTrue(onLeg.getType().equals(SwapLegType.OVERNIGHT), "Leg type should be OVERNIGHT");
    ArgChecker.isTrue(onLeg.getPaymentEvents().isEmpty(), "PaymentEvent should be empty");
    Map<Payment, PointSensitivityBuilder> res = new HashMap<Payment, PointSensitivityBuilder>();
    for (SwapPaymentPeriod paymentPeriod : onLeg.getPaymentPeriods()) {
      ArgChecker.isTrue(paymentPeriod instanceof RatePaymentPeriod,
          "rate payment should be RatePaymentPeriod");
      RatePaymentPeriod ratePaymentPeriod = (RatePaymentPeriod) paymentPeriod;
      ArgChecker.isTrue(ratePaymentPeriod.getAccrualPeriods().size() == 1,
          "rate payment should not be compounding");
      RateAccrualPeriod rateAccrualPeriod = ratePaymentPeriod.getAccrualPeriods().get(0);
      CurrencyAmount notional = ratePaymentPeriod.getNotionalAmount();
      RateComputation rateComputation = rateAccrualPeriod.getRateComputation();
      ArgChecker.isTrue(rateComputation instanceof OvernightCompoundedRateComputation,
          "RateComputation should be of type OvernightCompoundedRateComputation");
      OvernightCompoundedRateComputation onComputation = (OvernightCompoundedRateComputation) rateComputation;
      LocalDate startDate = rateAccrualPeriod.getStartDate();
      LocalDate endDate = rateAccrualPeriod.getEndDate();
      double computationAccrual = onComputation.getIndex().getDayCount().yearFraction(startDate, endDate);
      LocalDate paymentDate = ratePaymentPeriod.getPaymentDate();
      double paymentAccural = rateAccrualPeriod.getYearFraction();
      double dfPay = df.discountFactor(paymentDate);
      double dfEnd = df.discountFactor(endDate);
      ZeroRateSensitivity dfPayDr = df.zeroRatePointSensitivity(paymentDate);
      ZeroRateSensitivity dfEndDr = df.zeroRatePointSensitivity(endDate);
      double payDateRatio = dfPay / dfEnd;
      PointSensitivityBuilder payDateRatioDr = dfPayDr.multipliedBy(1.0d / dfEnd)
          .combinedWith(dfEndDr.multipliedBy(-dfPay / (dfEnd * dfEnd)));
      Payment payStart = Payment.of(
          notional.multipliedBy(payDateRatio * paymentAccural / computationAccrual),
          startDate);
      double spread = rateAccrualPeriod.getSpread();
      Payment payEnd = Payment.of(
          notional.multipliedBy(-paymentAccural / computationAccrual + spread * paymentAccural),
          paymentDate);
      res.put(payStart, payDateRatioDr
          .multipliedBy(notional.getAmount() * paymentAccural / computationAccrual));
      res.put(payEnd, PointSensitivityBuilder.none());
    }
    return ImmutableMap.copyOf(res);
  }

  /**
   * Computes cash flow equivalent and sensitivity of Ibor leg.
   * <p>
   * The return type is a map of {@code NotionalExchange} and {@code PointSensitivityBuilder}.
   * 
   * @param iborLeg  the Ibor leg
   * @param ratesProvider  the rates provider
   * @return the cash flow equivalent and sensitivity
   */
  public static ImmutableMap<Payment, PointSensitivityBuilder> cashFlowEquivalentAndSensitivityIborLeg(
      ResolvedSwapLeg iborLeg,
      RatesProvider ratesProvider) {

    ArgChecker.isTrue(iborLeg.getType().equals(SwapLegType.IBOR), "Leg type should be IBOR");
    ArgChecker.isTrue(iborLeg.getPaymentEvents().isEmpty(), "PaymentEvent should be empty");
    Map<Payment, PointSensitivityBuilder> res = new HashMap<Payment, PointSensitivityBuilder>();
    for (SwapPaymentPeriod paymentPeriod : iborLeg.getPaymentPeriods()) {
      ArgChecker.isTrue(paymentPeriod instanceof RatePaymentPeriod, "rate payment should be RatePaymentPeriod");
      RatePaymentPeriod ratePaymentPeriod = (RatePaymentPeriod) paymentPeriod;
      ArgChecker.isTrue(ratePaymentPeriod.getAccrualPeriods().size() == 1, "rate payment should not be compounding");
      RateAccrualPeriod rateAccrualPeriod = ratePaymentPeriod.getAccrualPeriods().get(0);
      CurrencyAmount notional = ratePaymentPeriod.getNotionalAmount();
      LocalDate paymentDate = ratePaymentPeriod.getPaymentDate();
      IborIndexObservation obs = ((IborRateComputation) rateAccrualPeriod.getRateComputation()).getObservation();
      IborIndex index = obs.getIndex();
      LocalDate fixingStartDate = obs.getEffectiveDate();
      double fixingYearFraction = obs.getYearFraction();
      double factorIndex = (1d + fixingYearFraction * ratesProvider.iborIndexRates(index).rate(obs));
      double dfPayment = ratesProvider.discountFactor(paymentPeriod.getCurrency(), paymentPeriod.getPaymentDate());
      double dfStart = ratesProvider.discountFactor(paymentPeriod.getCurrency(), fixingStartDate);
      double beta = factorIndex * dfPayment / dfStart;
      double ycRatio = rateAccrualPeriod.getYearFraction() / fixingYearFraction;
      Payment payStart = Payment.of(notional.multipliedBy(beta * ycRatio), fixingStartDate);
      Payment payEnd = Payment.of(notional.multipliedBy(-ycRatio), paymentDate);
      double factor = ycRatio * notional.getAmount() / dfStart;
      PointSensitivityBuilder factorIndexSensi = ratesProvider.iborIndexRates(index)
          .ratePointSensitivity(obs).multipliedBy(fixingYearFraction * dfPayment * factor);
      PointSensitivityBuilder dfPaymentSensitivity = ratesProvider.discountFactors(paymentPeriod.getCurrency())
          .zeroRatePointSensitivity(paymentPeriod.getPaymentDate()).multipliedBy(factorIndex * factor);
      PointSensitivityBuilder dfStartSensitivity = ratesProvider.discountFactors(paymentPeriod.getCurrency())
          .zeroRatePointSensitivity(fixingStartDate).multipliedBy(-factorIndex * dfPayment * factor / dfStart);
      res.put(payStart, factorIndexSensi.combinedWith(dfPaymentSensitivity).combinedWith(dfStartSensitivity));
      res.put(payEnd, PointSensitivityBuilder.none());
    }
    return ImmutableMap.copyOf(res);
  }

  /**
   * Generate a new list with the dates sorted and the amounts of elements with same payment date compressed.
   * <p>
   * The original list is unchanged.
   * 
   * @param input  the starting list
   * @return the normalized list
   */
  public static List<NotionalExchange> normalize(List<NotionalExchange> input) {
    List<NotionalExchange> sorted = new ArrayList<>(input); // copy for sorting
    Collections.sort(sorted, (a, b) -> (int) (a.getPaymentDate().toEpochDay() - b.getPaymentDate().toEpochDay()));

    NotionalExchange previous = sorted.get(0);
    for (int i = 1; i < sorted.size(); i++) {
      NotionalExchange current = sorted.get(i);
      if (current.getPaymentDate().equals(previous.getPaymentDate()) &&
          current.getCurrency().equals(previous.getCurrency())) {
        current = NotionalExchange.of(
            CurrencyAmount.of(current.getCurrency(),
                current.getPaymentAmount().getAmount() + previous.getPaymentAmount().getAmount()),
            current.getPaymentDate());
        sorted.set(i - 1, current);
        sorted.remove(i);
        i--;
      }
      previous = current;
    }
    return sorted;
  }

  //-------------------------------------------------------------------------
  // restricted constructor
  private CashFlowEquivalentCalculator() {
  }
 
}
