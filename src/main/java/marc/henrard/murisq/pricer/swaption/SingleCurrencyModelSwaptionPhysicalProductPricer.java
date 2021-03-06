/**
 * Copyright (C) 2015 - present by Marc Henrard.
 */
package marc.henrard.murisq.pricer.swaption;

import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.pricer.impl.option.BlackFormulaRepository;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.pricer.swap.DiscountingSwapProductPricer;
import com.opengamma.strata.product.common.PayReceive;
import com.opengamma.strata.product.common.PutCall;
import com.opengamma.strata.product.swap.ResolvedSwapLeg;
import com.opengamma.strata.product.swap.SwapLegType;
import com.opengamma.strata.product.swaption.PhysicalSwaptionSettlement;
import com.opengamma.strata.product.swaption.ResolvedSwaption;

import marc.henrard.murisq.model.bachelier.BachelierFormula;
import marc.henrard.murisq.model.generic.SingleCurrencyModelParameters;

/**
 * Price of physical delivery European swaptions in single currency models.
 * 
 * @author Marc Henrard
 */
public abstract class SingleCurrencyModelSwaptionPhysicalProductPricer {

  /** The pricer used for swap measures. */
  private static final DiscountingSwapProductPricer PRICER_SWAP =
      DiscountingSwapProductPricer.DEFAULT;

  /**
   * Computes the present value of a swaption in a given model.
   * <p>
   * The result is expressed using the currency of the swaption.
   * 
   * @param swaption  the product to price
   * @param multicurve  the rates provider
   * @param model  the rational model parameters
   * @return the present value of the swaption product
   */
  public abstract CurrencyAmount presentValue(
      ResolvedSwaption swaption,
      RatesProvider multicurve,
      SingleCurrencyModelParameters model);

  /**
   * Computes the implied volatility in the Black model.
   * <p>
   * The swaption price is computed in the rational model and the implied volatility for that price is computed. 
   * The implied volatility may failed if the model price is outside the Black possible prices.
   * 
   * @param swaption  the product to price
   * @param multicurve  the rates provider
   * @param model  the rational model parameters
   * @return the implied volatility in the Black model
   */
  public double impliedVolatilityBlack(
      ResolvedSwaption swaption,
      RatesProvider multicurve,
      SingleCurrencyModelParameters model) {

    double price = presentValue(swaption, multicurve, model).getAmount();
    double parRate = PRICER_SWAP.parRate(swaption.getUnderlying(), multicurve);
    double timeToEpiry = model.relativeTime(swaption.getExpiry());
    ResolvedSwapLeg legFixed = swaption.getUnderlying().getLegs(SwapLegType.FIXED).get(0);
    double pvbp = PRICER_SWAP.getLegPricer().pvbp(legFixed, multicurve);
    double strike = PRICER_SWAP.getLegPricer().couponEquivalent(legFixed, multicurve, pvbp);
    return BlackFormulaRepository
        .impliedVolatility(Math.abs(price / pvbp), parRate, strike, timeToEpiry, !isReceiver(swaption));
  }

  /**
   * Computes the implied volatility in the Bachelier model.
   * <p>
   * The swaption price is computed in the rational model and the implied volatility for that price is computed.
   * The Bachelier formula inversion is done using {@link BachelierFormula#impliedVolatilityApproxLfk4}.
   * 
   * @param swaption  the product to price
   * @param multicurve  the rates provider
   * @param model  the rational model parameters
   * @return the implied volatility in the Bachelier model
   */
  public double impliedVolatilityBachelier(
      ResolvedSwaption swaption,
      RatesProvider multicurve,
      SingleCurrencyModelParameters model) {

    double price = presentValue(swaption, multicurve, model).getAmount();
    double timeToEpiry = model.relativeTime(swaption.getExpiry());
    return impliedVolatilityBachelier(swaption, multicurve, price, timeToEpiry);
  }

  /**
   * Computes the implied volatility in the Bachelier model.
   * <p>
   * The implied volatility for the given price is computed.
   * The Bachelier formula inversion is done using {@link BachelierFormula#impliedVolatilityApproxLfk4}.
   * 
   * @param swaption  the product to price
   * @param multicurve  the rates provider
   * @param price  the swaption price
   * @param timeToEpiry  the time to expiry as computed by the model
   * @return the implied volatility in the Bachelier model
   */
  public double impliedVolatilityBachelier(
      ResolvedSwaption swaption,
      RatesProvider multicurve,
      double price,
      double timeToEpiry) {

    double parRate = PRICER_SWAP.parRate(swaption.getUnderlying(), multicurve);
    ResolvedSwapLeg legFixed = swaption.getUnderlying().getLegs(SwapLegType.FIXED).get(0);
    double pvbp = PRICER_SWAP.getLegPricer().pvbp(legFixed, multicurve);
    double strike = PRICER_SWAP.getLegPricer().couponEquivalent(legFixed, multicurve, pvbp);
    return BachelierFormula.impliedVolatilityApproxLfk4(
        Math.abs(price), parRate, strike, timeToEpiry, Math.abs(pvbp),
        isReceiver(swaption) ? PutCall.PUT : PutCall.CALL);
  }

  /**
   * Validates that the rates and volatilities providers are coherent and that the swaption is acceptable.
   * 
   * @param rates  the rate provider
   * @param swaption  the swaption
   * @param model  the rational one-factor model
   */
  protected void validate(
      RatesProvider rates,
      ResolvedSwaption swaption,
      SingleCurrencyModelParameters model) {

    ArgChecker.isTrue(model.getValuationDate().equals(rates.getValuationDate()),
        "volatility and rate data should be for the same date");
    ArgChecker.isFalse(swaption.getUnderlying().isCrossCurrency(),
        "underlying swap should be single currency");
    ArgChecker.isTrue(swaption.getSwaptionSettlement() == PhysicalSwaptionSettlement.DEFAULT,
        "swaption should be physical settlement");
  }

  /**
   * Returns true is the underlying swap is a receiver and false otherwise.
   * 
   * @param swaption  the swaption
   * @return the receiver flag
   */
  private boolean isReceiver(ResolvedSwaption swaption) {

    return swaption.getUnderlying().getLegs(SwapLegType.FIXED).get(0).getPayReceive()
        .equals(PayReceive.RECEIVE);
  }

}
