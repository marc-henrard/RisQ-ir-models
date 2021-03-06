/**
 * Copyright (C) 2016 - present by Marc Henrard.
 */
package marc.henrard.murisq.model.generic;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.param.ParameterMetadata;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;

import marc.henrard.murisq.basics.time.TimeMeasurement;

/**
 * Description of a generic parameter curve.
 * <p> 
 * Sensitivities are not available for this generic implementation. 
 * 
 * @author Marc Henrard
 */
@BeanDefinition
public final class GenericParameterDateCurve
    implements ParameterDateCurve, ImmutableBean, Serializable {

  /** The day count used to measure the time. */
  @PropertyDefinition(validate = "notNull")
  private final TimeMeasurement timeMeasure;
  /** The curve for the parameters. */
  @PropertyDefinition(validate = "notNull")
  private final Curve underlying;
  /** The valuation date. */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate valuationDate;

  /**
   * Returns an instance of the curve.
   * 
   * @param dayCount  the day count
   * @param underlying  the underlying curve
   * @param valuationDate  the valuation date
   * @return
   */
  public static GenericParameterDateCurve of(TimeMeasurement timeMeasure, Curve underlying, LocalDate valuationDate) {
    return new GenericParameterDateCurve(timeMeasure, underlying, valuationDate);
  }

  @Override
  public double parameterValue(LocalDate date) {
    return underlying.yValue(timeMeasure.relativeTime(valuationDate, date));
  }

  @Override
  public PointSensitivityBuilder parameterValueCurveSensitivity(LocalDate date) {
    throw new IllegalArgumentException("Sensitivity not implemented for generic parameter curve");
  }

  @Override
  public int getParameterCount() {
    return underlying.getParameterCount();
  }

  @Override
  public double getParameter(int parameterIndex) {
    return underlying.getParameter(parameterIndex);
  }

  @Override
  public ParameterMetadata getParameterMetadata(int parameterIndex) {
    return underlying.getParameterMetadata(parameterIndex);
  }

  @Override
  public GenericParameterDateCurve withParameter(int parameterIndex, double newValue) {
    return new GenericParameterDateCurve(timeMeasure, underlying.withParameter(parameterIndex, newValue), valuationDate);
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code GenericParameterDateCurve}.
   * @return the meta-bean, not null
   */
  public static GenericParameterDateCurve.Meta meta() {
    return GenericParameterDateCurve.Meta.INSTANCE;
  }

  static {
    MetaBean.register(GenericParameterDateCurve.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static GenericParameterDateCurve.Builder builder() {
    return new GenericParameterDateCurve.Builder();
  }

  private GenericParameterDateCurve(
      TimeMeasurement timeMeasure,
      Curve underlying,
      LocalDate valuationDate) {
    JodaBeanUtils.notNull(timeMeasure, "timeMeasure");
    JodaBeanUtils.notNull(underlying, "underlying");
    JodaBeanUtils.notNull(valuationDate, "valuationDate");
    this.timeMeasure = timeMeasure;
    this.underlying = underlying;
    this.valuationDate = valuationDate;
  }

  @Override
  public GenericParameterDateCurve.Meta metaBean() {
    return GenericParameterDateCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count used to measure the time.
   * @return the value of the property, not null
   */
  public TimeMeasurement getTimeMeasure() {
    return timeMeasure;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the curve for the parameters.
   * @return the value of the property, not null
   */
  public Curve getUnderlying() {
    return underlying;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valuation date.
   * @return the value of the property, not null
   */
  public LocalDate getValuationDate() {
    return valuationDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      GenericParameterDateCurve other = (GenericParameterDateCurve) obj;
      return JodaBeanUtils.equal(timeMeasure, other.timeMeasure) &&
          JodaBeanUtils.equal(underlying, other.underlying) &&
          JodaBeanUtils.equal(valuationDate, other.valuationDate);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(timeMeasure);
    hash = hash * 31 + JodaBeanUtils.hashCode(underlying);
    hash = hash * 31 + JodaBeanUtils.hashCode(valuationDate);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("GenericParameterDateCurve{");
    buf.append("timeMeasure").append('=').append(JodaBeanUtils.toString(timeMeasure)).append(',').append(' ');
    buf.append("underlying").append('=').append(JodaBeanUtils.toString(underlying)).append(',').append(' ');
    buf.append("valuationDate").append('=').append(JodaBeanUtils.toString(valuationDate));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code GenericParameterDateCurve}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code timeMeasure} property.
     */
    private final MetaProperty<TimeMeasurement> timeMeasure = DirectMetaProperty.ofImmutable(
        this, "timeMeasure", GenericParameterDateCurve.class, TimeMeasurement.class);
    /**
     * The meta-property for the {@code underlying} property.
     */
    private final MetaProperty<Curve> underlying = DirectMetaProperty.ofImmutable(
        this, "underlying", GenericParameterDateCurve.class, Curve.class);
    /**
     * The meta-property for the {@code valuationDate} property.
     */
    private final MetaProperty<LocalDate> valuationDate = DirectMetaProperty.ofImmutable(
        this, "valuationDate", GenericParameterDateCurve.class, LocalDate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "timeMeasure",
        "underlying",
        "valuationDate");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1642109393:  // timeMeasure
          return timeMeasure;
        case -1770633379:  // underlying
          return underlying;
        case 113107279:  // valuationDate
          return valuationDate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public GenericParameterDateCurve.Builder builder() {
      return new GenericParameterDateCurve.Builder();
    }

    @Override
    public Class<? extends GenericParameterDateCurve> beanType() {
      return GenericParameterDateCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code timeMeasure} property.
     * @return the meta-property, not null
     */
    public MetaProperty<TimeMeasurement> timeMeasure() {
      return timeMeasure;
    }

    /**
     * The meta-property for the {@code underlying} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Curve> underlying() {
      return underlying;
    }

    /**
     * The meta-property for the {@code valuationDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> valuationDate() {
      return valuationDate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1642109393:  // timeMeasure
          return ((GenericParameterDateCurve) bean).getTimeMeasure();
        case -1770633379:  // underlying
          return ((GenericParameterDateCurve) bean).getUnderlying();
        case 113107279:  // valuationDate
          return ((GenericParameterDateCurve) bean).getValuationDate();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code GenericParameterDateCurve}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<GenericParameterDateCurve> {

    private TimeMeasurement timeMeasure;
    private Curve underlying;
    private LocalDate valuationDate;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(GenericParameterDateCurve beanToCopy) {
      this.timeMeasure = beanToCopy.getTimeMeasure();
      this.underlying = beanToCopy.getUnderlying();
      this.valuationDate = beanToCopy.getValuationDate();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1642109393:  // timeMeasure
          return timeMeasure;
        case -1770633379:  // underlying
          return underlying;
        case 113107279:  // valuationDate
          return valuationDate;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1642109393:  // timeMeasure
          this.timeMeasure = (TimeMeasurement) newValue;
          break;
        case -1770633379:  // underlying
          this.underlying = (Curve) newValue;
          break;
        case 113107279:  // valuationDate
          this.valuationDate = (LocalDate) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public GenericParameterDateCurve build() {
      return new GenericParameterDateCurve(
          timeMeasure,
          underlying,
          valuationDate);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the day count used to measure the time.
     * @param timeMeasure  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder timeMeasure(TimeMeasurement timeMeasure) {
      JodaBeanUtils.notNull(timeMeasure, "timeMeasure");
      this.timeMeasure = timeMeasure;
      return this;
    }

    /**
     * Sets the curve for the parameters.
     * @param underlying  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder underlying(Curve underlying) {
      JodaBeanUtils.notNull(underlying, "underlying");
      this.underlying = underlying;
      return this;
    }

    /**
     * Sets the valuation date.
     * @param valuationDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder valuationDate(LocalDate valuationDate) {
      JodaBeanUtils.notNull(valuationDate, "valuationDate");
      this.valuationDate = valuationDate;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("GenericParameterDateCurve.Builder{");
      buf.append("timeMeasure").append('=').append(JodaBeanUtils.toString(timeMeasure)).append(',').append(' ');
      buf.append("underlying").append('=').append(JodaBeanUtils.toString(underlying)).append(',').append(' ');
      buf.append("valuationDate").append('=').append(JodaBeanUtils.toString(valuationDate));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
