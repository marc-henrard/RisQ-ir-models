package marc.henrard.murisq.loader.csv;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;

import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.light.LightMetaBean;

import com.opengamma.strata.market.curve.CurveName;

/**
 * This is a copy of OpenGamma's Strata version to solve method visibility.
 * 
 * Identifies an instance of a named curve on a specific date.
 */
@BeanDefinition(style = "light")
final class LoadedCurveKey2
    implements ImmutableBean {

  /**
   * The curve date.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate curveDate;
  /**
   * The curve name.
   */
  @PropertyDefinition(validate = "notNull")
  private final CurveName curveName;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance from typed strings where applicable.
   * 
   * @param curveDate  the curve date
   * @param curveName  the curve name
   * @return the curve key
   */
  static LoadedCurveKey2 of(LocalDate curveDate, CurveName curveName) {
    return new LoadedCurveKey2(curveDate, curveName);
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code LoadedCurveKey2}.
   */
  private static final TypedMetaBean<LoadedCurveKey2> META_BEAN =
      LightMetaBean.of(
          LoadedCurveKey2.class,
          MethodHandles.lookup(),
          new String[] {
              "curveDate",
              "curveName"},
          new Object[0]);

  /**
   * The meta-bean for {@code LoadedCurveKey2}.
   * @return the meta-bean, not null
   */
  public static TypedMetaBean<LoadedCurveKey2> meta() {
    return META_BEAN;
  }

  static {
    MetaBean.register(META_BEAN);
  }

  private LoadedCurveKey2(
      LocalDate curveDate,
      CurveName curveName) {
    JodaBeanUtils.notNull(curveDate, "curveDate");
    JodaBeanUtils.notNull(curveName, "curveName");
    this.curveDate = curveDate;
    this.curveName = curveName;
  }

  @Override
  public TypedMetaBean<LoadedCurveKey2> metaBean() {
    return META_BEAN;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the curve date.
   * @return the value of the property, not null
   */
  public LocalDate getCurveDate() {
    return curveDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the curve name.
   * @return the value of the property, not null
   */
  public CurveName getCurveName() {
    return curveName;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      LoadedCurveKey2 other = (LoadedCurveKey2) obj;
      return JodaBeanUtils.equal(curveDate, other.curveDate) &&
          JodaBeanUtils.equal(curveName, other.curveName);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(curveDate);
    hash = hash * 31 + JodaBeanUtils.hashCode(curveName);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("LoadedCurveKey2{");
    buf.append("curveDate").append('=').append(JodaBeanUtils.toString(curveDate)).append(',').append(' ');
    buf.append("curveName").append('=').append(JodaBeanUtils.toString(curveName));
    buf.append('}');
    return buf.toString();
  }

  //-------------------------- AUTOGENERATED END --------------------------
}
