/**
 * Copyright (C) 2016 - present by Marc Henrard.
 */
package marc.henrard.murisq.dataset;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.index.IborIndices.EUR_EURIBOR_6M;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.market.curve.ConstantCurve;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.curve.DefaultCurveMetadata;
import com.opengamma.strata.market.curve.InterpolatedNodalCurve;
import com.opengamma.strata.market.curve.interpolator.CurveInterpolator;
import com.opengamma.strata.market.curve.interpolator.CurveInterpolators;
import com.opengamma.strata.pricer.DiscountFactors;

import marc.henrard.murisq.basics.time.ScaledSecondTime;
import marc.henrard.murisq.basics.time.TimeMeasurement;
import marc.henrard.murisq.model.generic.GenericParameterDateCurve;
import marc.henrard.murisq.model.generic.ParameterDateCurve;
import marc.henrard.murisq.model.rationalmulticurve.RationalTwoFactorGenericParameters;
import marc.henrard.murisq.model.rationalmulticurve.RationalTwoFactorHWShapePlusCstParameters;

/**
 * Examples of data sets for the Rational Two-Factor model.
 * Used for tests.
 * 
 * @author Marc Henrard
 */
public class RationalTwoFactorParameters20151120DataSet {

  public static final LocalDate VALUATION_DATE = LocalDate.of(2015, 11, 20);
  
  private static final CurveInterpolator INTERPOLATOR_LINEAR = CurveInterpolators.LINEAR;
  private static final TimeMeasurement TIME_MEAS = ScaledSecondTime.DEFAULT;
  private static final double A1 = 0.50;
  private static final double A2 = 0.40;
  private static final double RHO = 0.30;
  private static final double ALPHA = 0.10;
  private static final double BETA = 0.01;
  private static final double[] TIMES = new double[]{0.0d, 100.0d};
  private static final Curve B0_CURVE = InterpolatedNodalCurve.of(DefaultCurveMetadata.of("B1"), 
      DoubleArray.copyOf(TIMES), 
      DoubleArray.copyOf(new double[]{ALPHA, ALPHA + BETA * TIMES[1]}), INTERPOLATOR_LINEAR);
  private static final ParameterDateCurve B0 = GenericParameterDateCurve.of(
      ScaledSecondTime.DEFAULT, B0_CURVE, VALUATION_DATE);
  private static final List<IborIndex> BX_INDICES = ImmutableList.of(EUR_EURIBOR_6M);
  private static final List<ParameterDateCurve> B1 = ImmutableList.of(
        GenericParameterDateCurve.of(
            ScaledSecondTime.DEFAULT, 
            ConstantCurve.of(DefaultCurveMetadata.of("B1"), 0.010), 
            VALUATION_DATE));
  private static final List<ParameterDateCurve> B2 = ImmutableList.of(
        GenericParameterDateCurve.of(
            ScaledSecondTime.DEFAULT, 
            ConstantCurve.of(DefaultCurveMetadata.of("B2"), 0.001), 
            VALUATION_DATE));
  private static final List<ParameterDateCurve> ZERO = ImmutableList.of(
        GenericParameterDateCurve.of(
            ScaledSecondTime.DEFAULT, 
            ConstantCurve.of(DefaultCurveMetadata.of("ZERO"), 0.0d), 
            VALUATION_DATE));
  public static final LocalTime LOCAL_TIME = LocalTime.of(11, 0);
  public static final ZoneId ZONE_ID = ZoneId.of("Europe/Brussels");
  public static final RationalTwoFactorGenericParameters RATIONAL_2F = 
      RationalTwoFactorGenericParameters.of(EUR, A1, A2, RHO, B0, BX_INDICES, B1, B2, TIME_MEAS, VALUATION_DATE, LOCAL_TIME, ZONE_ID);
  public static final RationalTwoFactorGenericParameters RATIONAL_2F_REDUCED_1 = 
      RationalTwoFactorGenericParameters.of(EUR, A1, A2, 0.9, B0, BX_INDICES, B1, ZERO, TIME_MEAS, VALUATION_DATE, LOCAL_TIME, ZONE_ID);
  public static final RationalTwoFactorGenericParameters RATIONAL_2F_REDUCED_0 = 
      RationalTwoFactorGenericParameters.of(EUR, A1, A2, 0.0, B0, BX_INDICES, B1, ZERO, TIME_MEAS, VALUATION_DATE, LOCAL_TIME, ZONE_ID);
  
  public static final RationalTwoFactorGenericParameters rational2Factor(LocalTime localTime, ZoneId zone) {
    return RationalTwoFactorGenericParameters
        .of(EUR, A1, A2, RHO, B0, BX_INDICES, B1, B2, TIME_MEAS, VALUATION_DATE, localTime, zone);
  }

  private static final double B_0_0 = 0.50;
  private static final double ETA = 0.01;
  private static final double KAPPA = 0.03;
  private static final double C1 = 0.0010;
  private static final double C2 = 0.0020;
  
  public static final RationalTwoFactorHWShapePlusCstParameters rational2FactorHwCst(
      DiscountFactors discountFactors, 
      LocalTime localTime, 
      ZoneId zone) {
    return RationalTwoFactorHWShapePlusCstParameters
        .of(DoubleArray.of(A1, A2, RHO, B_0_0, ETA, KAPPA, C1, C2), 
            TIME_MEAS, discountFactors, localTime, zone);
  }
      
  
}
