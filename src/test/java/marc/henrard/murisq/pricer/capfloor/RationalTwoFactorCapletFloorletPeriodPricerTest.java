/**
 * Copyright (C) 2017 - present by Marc Henrard.
 */
package marc.henrard.murisq.pricer.capfloor;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.EUTA;
import static com.opengamma.strata.basics.index.IborIndices.EUR_EURIBOR_6M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.date.DayCounts;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.market.ValueType;
import com.opengamma.strata.market.surface.ConstantSurface;
import com.opengamma.strata.market.surface.DefaultSurfaceMetadata;
import com.opengamma.strata.pricer.capfloor.BlackIborCapletFloorletExpiryStrikeVolatilities;
import com.opengamma.strata.pricer.capfloor.BlackIborCapletFloorletPeriodPricer;
import com.opengamma.strata.pricer.capfloor.NormalIborCapletFloorletExpiryStrikeVolatilities;
import com.opengamma.strata.pricer.capfloor.NormalIborCapletFloorletPeriodPricer;
import com.opengamma.strata.pricer.rate.ImmutableRatesProvider;
import com.opengamma.strata.product.capfloor.IborCapletFloorletPeriod;
import com.opengamma.strata.product.capfloor.IborCapletFloorletPeriod.Builder;
import com.opengamma.strata.product.rate.IborRateComputation;

import marc.henrard.murisq.dataset.MulticurveEur20151120DataSet;
import marc.henrard.murisq.dataset.RationalTwoFactorParameters20151120DataSet;
import marc.henrard.murisq.model.rationalmulticurve.RationalTwoFactorGenericParameters;
import marc.henrard.murisq.pricer.capfloor.RationalTwoFactorCapletFloorletPeriodSemiExplicitPricer;

/**
 * Tests {@link RationalTwoFactorCapletFloorletPeriodPricer}.
 * 
 * @author Marc Henrard
 */
public class RationalTwoFactorCapletFloorletPeriodPricerTest {

  private static final ReferenceData REF_DATA = ReferenceData.standard();
  
  private static final LocalDate VALUATION_DATE = LocalDate.of(2015, 11, 20);
  private static final ZoneId VALUATION_ZONE = ZoneId.of("Europe/Brussels");
  private static final LocalTime VALUATION_TIME = LocalTime.of(11, 0);
  private static final ZonedDateTime VALUATION_DATE_TIME = 
      VALUATION_DATE.atTime(VALUATION_TIME).atZone(VALUATION_ZONE);
  private static final DayCount DAYCOUNT_DEFAULT = DayCounts.ACT_365F;
  private static final HolidayCalendar EUTA_IMPL = REF_DATA.findValue(EUTA).get();

  /* Load and calibrate curves */
  private static final ImmutableRatesProvider MULTICURVE_EUR = 
      MulticurveEur20151120DataSet.MULTICURVE_EUR_EONIA_20151120;
  private static final ImmutableRatesProvider MULTICURVE_EUR_POS = 
      MulticurveEur20151120DataSet.MULTICURVE_EUR_EONIA_POS_20151120;

  /* Rational model data */
  private static final RationalTwoFactorGenericParameters RATIONAL_2F = 
      RationalTwoFactorParameters20151120DataSet.RATIONAL_2F;

  /* Descriptions of swaptions */
  private static final Period[] EXPIRIES_PER = new Period[] {
    Period.ofMonths(3), Period.ofYears(2), Period.ofYears(10)};
  private static final int NB_EXPIRIES = EXPIRIES_PER.length;
  private static final double[] STRIKES_BACHELIER = new double[] {-0.0025, 0.0100, 0.0200};
  private static final int NB_STRIKES_BACHELIER = STRIKES_BACHELIER.length;
  private static final double[] STRIKES_BLACK = new double[] {0.0250, 0.0300, 0.0400};
  private static final int NB_STRIKES_BLACK = STRIKES_BLACK.length;
  private static final double NOTIONAL = 100_000_000.0d;
  
  /* Pricer */
  private static final RationalTwoFactorCapletFloorletPeriodSemiExplicitPricer PRICER_CAP_S_EX =
      RationalTwoFactorCapletFloorletPeriodSemiExplicitPricer.DEFAULT; 
  private static final BlackIborCapletFloorletPeriodPricer PRICER_CAP_BLACK = 
      BlackIborCapletFloorletPeriodPricer.DEFAULT;
  private static final NormalIborCapletFloorletPeriodPricer PRICER_CAP_BACHELIER = 
      NormalIborCapletFloorletPeriodPricer.DEFAULT;

  private static final Offset<Double> TOLERANCE_PV = offset(1.0E-2);
  private static final Offset<Double> TOLERANCE_IV = offset(1.0E-6);
  private static final boolean PRINT_DETAILS = false;
  
  /* Test the implied volatility for the Black/log-normal model. 
   * Data is artificially high to have positive strikes and forwards. */
  @Test
  public void black() {
    for (int i = 0; i < NB_EXPIRIES; i++) {
      for (int k = 0; k < NB_STRIKES_BLACK; k++) {  // Removed first moneyness as it has negative strike
        if(PRINT_DETAILS) {
          System.out.println(i + " - " + k);
        }
        LocalDate fixingDate = EUTA_IMPL.nextOrSame(VALUATION_DATE.plus(EXPIRIES_PER[i]));
        IborRateComputation comp = IborRateComputation.of(EUR_EURIBOR_6M, fixingDate, REF_DATA);
        IborCapletFloorletPeriod capletLong = capletFloorlet(NOTIONAL, comp, STRIKES_BLACK[k], true);
        double pvRational = PRICER_CAP_S_EX
            .presentValue(capletLong, MULTICURVE_EUR_POS, RATIONAL_2F).getAmount();
        double iv = PRICER_CAP_S_EX
            .impliedVolatilityBlack(capletLong, MULTICURVE_EUR_POS, RATIONAL_2F);
        BlackIborCapletFloorletExpiryStrikeVolatilities volatilities =
            BlackIborCapletFloorletExpiryStrikeVolatilities.of(EUR_EURIBOR_6M, VALUATION_DATE_TIME,
                ConstantSurface.of(DefaultSurfaceMetadata.builder()
                    .surfaceName("Black-vol")
                    .xValueType(ValueType.YEAR_FRACTION)
                    .yValueType(ValueType.STRIKE)
                    .zValueType(ValueType.BLACK_VOLATILITY)
                    .dayCount(DAYCOUNT_DEFAULT).build(),
                    iv));
        double pvBachelier =
            PRICER_CAP_BLACK.presentValue(capletLong, MULTICURVE_EUR_POS, volatilities).getAmount();
        assertThat(pvRational).isEqualTo(pvBachelier, TOLERANCE_PV);
        /* Short caplet. */
        IborCapletFloorletPeriod capletShort = capletFloorlet(-NOTIONAL, comp, STRIKES_BLACK[k], true);
        double iv2 = PRICER_CAP_S_EX
            .impliedVolatilityBlack(capletShort, MULTICURVE_EUR_POS, RATIONAL_2F);
        assertThat(iv).isEqualTo(iv2, TOLERANCE_IV);
        /* Long floorlet. */
        IborCapletFloorletPeriod floorletLong = capletFloorlet(NOTIONAL, comp, STRIKES_BLACK[k], false);
        double iv3 = PRICER_CAP_S_EX
            .impliedVolatilityBlack(floorletLong, MULTICURVE_EUR_POS, RATIONAL_2F);
        assertThat(iv).isEqualTo(iv3, TOLERANCE_IV);
        /* Short floorlet. */
        IborCapletFloorletPeriod floorletShort = capletFloorlet(-NOTIONAL, comp, STRIKES_BLACK[k], false);
        double iv4 = PRICER_CAP_S_EX
            .impliedVolatilityBlack(floorletShort, MULTICURVE_EUR_POS, RATIONAL_2F);
        assertThat(iv).isEqualTo(iv4, TOLERANCE_IV);
      }
    }
  }
  
  /* Test the implied volatility for the Bachelier/normal model. 
   * Data is artificially high to have positive strikes and forwards. */
  @Test
  public void bachelier() {
    for (int i = 0; i < NB_EXPIRIES; i++) {
      int kStart = 0;
      if(i== 2) { // 10Y; too far below ATM
        kStart = 1;
      }
      for (int k = kStart; k < NB_STRIKES_BACHELIER; k++) {
        LocalDate fixingDate = EUTA_IMPL.nextOrSame(VALUATION_DATE.plus(EXPIRIES_PER[i]));
        IborRateComputation comp = IborRateComputation.of(EUR_EURIBOR_6M, fixingDate, REF_DATA);
        IborCapletFloorletPeriod capletLong = capletFloorlet(NOTIONAL, comp, STRIKES_BACHELIER[k], true);
        double pvRational = PRICER_CAP_S_EX
            .presentValue(capletLong, MULTICURVE_EUR, RATIONAL_2F).getAmount();
        double iv = PRICER_CAP_S_EX
            .impliedVolatilityBachelier(capletLong, MULTICURVE_EUR, RATIONAL_2F);
        NormalIborCapletFloorletExpiryStrikeVolatilities volatilities =
            NormalIborCapletFloorletExpiryStrikeVolatilities.of(EUR_EURIBOR_6M, VALUATION_DATE_TIME,
                ConstantSurface.of(DefaultSurfaceMetadata.builder()
                    .surfaceName("Bachelier-vol")
                    .xValueType(ValueType.YEAR_FRACTION)
                    .yValueType(ValueType.STRIKE)
                    .zValueType(ValueType.NORMAL_VOLATILITY)
                    .dayCount(DAYCOUNT_DEFAULT).build(),
                    iv));
        double pvBachelier =
            PRICER_CAP_BACHELIER.presentValue(capletLong, MULTICURVE_EUR, volatilities).getAmount();
        assertThat(pvRational).isCloseTo(pvBachelier, TOLERANCE_PV);
        /* Short caplet. */
        IborCapletFloorletPeriod capletShort = capletFloorlet(-NOTIONAL, comp, STRIKES_BACHELIER[k], true);
        double iv2 = PRICER_CAP_S_EX
            .impliedVolatilityBachelier(capletShort, MULTICURVE_EUR, RATIONAL_2F);
        assertThat(iv).isCloseTo(iv2, TOLERANCE_IV);
        /* Long floorlet. */
        IborCapletFloorletPeriod floorletLong = capletFloorlet(NOTIONAL, comp, STRIKES_BACHELIER[k], false);
        double iv3 = PRICER_CAP_S_EX
            .impliedVolatilityBachelier(floorletLong, MULTICURVE_EUR, RATIONAL_2F);
        assertThat(iv).isCloseTo(iv3, TOLERANCE_IV);
        /* Short floorlet. */
        IborCapletFloorletPeriod floorletShort = capletFloorlet(-NOTIONAL, comp, STRIKES_BACHELIER[k], false);
        double iv4 = PRICER_CAP_S_EX
            .impliedVolatilityBachelier(floorletShort, MULTICURVE_EUR, RATIONAL_2F);
        assertThat(iv).isCloseTo(iv4, TOLERANCE_IV);
      }
    }
  }
  
  private IborCapletFloorletPeriod capletFloorlet(
      double notional, IborRateComputation comp, double strike, boolean isCap) {
    Builder builder = IborCapletFloorletPeriod.builder()
        .currency(EUR)
        .notional(notional)
        .startDate(comp.getEffectiveDate())
        .endDate(comp.getMaturityDate())
        .paymentDate(comp.getMaturityDate())
        .yearFraction(comp.getYearFraction())
        .iborRate(comp);
    if(isCap) {
      builder.caplet(strike);
    } else {
      builder.floorlet(strike);
    }
    return builder.build();
  }
  
}
