/**
 * Copyright (C) 2020 - present by Marc Henrard.
 */
package marc.henrard.murisq.product.cms;

import java.io.Serializable;

import org.joda.beans.ImmutableBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;

import com.opengamma.strata.product.ResolvedProduct;
import com.opengamma.strata.product.cms.CmsPeriod;
import com.opengamma.strata.product.cms.CmsPeriodType;

import java.util.Map;
import java.util.NoSuchElementException;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * CMS period wrapped in a {@link ResolvedProduct} to facilitate some pricer code.
 * 
 * @author Marc Henrard
 */
@BeanDefinition(factoryName = "of")
public final class CmsPeriodResolved
    implements ResolvedProduct, ImmutableBean, Serializable {
  
  /**
   * The CMS period wrapped.
   */
  @PropertyDefinition(validate = "notNull")
  private final CmsPeriod period;
  
  /**
   * Computes the CMS payoff rate from the swap rates. Single rate version.
   * 
   * @param swapRate  the swap rate
   * @param cms  the CMS period description
   * @return the payoff
   */
  public double payoff(double swapRate) {
    double factor = period.getNotional() * period.getYearFraction();
    if (period.getCmsPeriodType().equals(CmsPeriodType.CAPLET)) {
      return factor * Math.max(0, swapRate - period.getStrike());
    }
    if (period.getCmsPeriodType().equals(CmsPeriodType.FLOORLET)) {
      return factor * Math.max(0,  period.getStrike() - swapRate);
    }
    return factor * swapRate;
  }
  
  /**
   * Computes the CMS payoff rate from the swap rates. Array version.
   * 
   * @param swapRate  the swap rates
   * @param cms  the CMS period description
   * @return the payoffs
   */
  public double[] payoff(double[] swapRate) {
    double factor = period.getNotional() * period.getYearFraction();
    if (period.getCmsPeriodType().equals(CmsPeriodType.CAPLET)) {
      double[] payoff = new double[swapRate.length];
      for (int i = 0; i < swapRate.length; i++) {
        payoff[i] = factor * Math.max(0, swapRate[i] - period.getStrike());
      }
      return payoff;
    }
    if (period.getCmsPeriodType().equals(CmsPeriodType.FLOORLET)) {
      double[] payoff = new double[swapRate.length];
      for (int i = 0; i < swapRate.length; i++) {
        payoff[i] = factor * Math.max(0, period.getStrike() - swapRate[i]);
      }
      return payoff;
    }
    double[] payoff = new double[swapRate.length];
    for (int i = 0; i < swapRate.length; i++) {
      payoff[i] = factor * swapRate[i];
    }
    return payoff;
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code CmsPeriodResolved}.
   * @return the meta-bean, not null
   */
  public static CmsPeriodResolved.Meta meta() {
    return CmsPeriodResolved.Meta.INSTANCE;
  }

  static {
    MetaBean.register(CmsPeriodResolved.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Obtains an instance.
   * @param period  the value of the property, not null
   * @return the instance
   */
  public static CmsPeriodResolved of(
      CmsPeriod period) {
    return new CmsPeriodResolved(
      period);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CmsPeriodResolved.Builder builder() {
    return new CmsPeriodResolved.Builder();
  }

  private CmsPeriodResolved(
      CmsPeriod period) {
    JodaBeanUtils.notNull(period, "period");
    this.period = period;
  }

  @Override
  public CmsPeriodResolved.Meta metaBean() {
    return CmsPeriodResolved.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the CMS period wrapped.
   * @return the value of the property, not null
   */
  public CmsPeriod getPeriod() {
    return period;
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
      CmsPeriodResolved other = (CmsPeriodResolved) obj;
      return JodaBeanUtils.equal(period, other.period);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(period);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("CmsPeriodResolved{");
    buf.append("period").append('=').append(JodaBeanUtils.toString(period));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CmsPeriodResolved}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code period} property.
     */
    private final MetaProperty<CmsPeriod> period = DirectMetaProperty.ofImmutable(
        this, "period", CmsPeriodResolved.class, CmsPeriod.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "period");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -991726143:  // period
          return period;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CmsPeriodResolved.Builder builder() {
      return new CmsPeriodResolved.Builder();
    }

    @Override
    public Class<? extends CmsPeriodResolved> beanType() {
      return CmsPeriodResolved.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code period} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CmsPeriod> period() {
      return period;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -991726143:  // period
          return ((CmsPeriodResolved) bean).getPeriod();
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
   * The bean-builder for {@code CmsPeriodResolved}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CmsPeriodResolved> {

    private CmsPeriod period;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CmsPeriodResolved beanToCopy) {
      this.period = beanToCopy.getPeriod();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -991726143:  // period
          return period;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -991726143:  // period
          this.period = (CmsPeriod) newValue;
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
    public CmsPeriodResolved build() {
      return new CmsPeriodResolved(
          period);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the CMS period wrapped.
     * @param period  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder period(CmsPeriod period) {
      JodaBeanUtils.notNull(period, "period");
      this.period = period;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("CmsPeriodResolved.Builder{");
      buf.append("period").append('=').append(JodaBeanUtils.toString(period));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
