/**
 * Copyright (C) 2015 - present by Marc Henrard.
 */
package marc.henrard.murisq.pricer.decomposition;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.joda.beans.ImmutableBean;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.beans.MetaBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;

/**
 * Class describing the dates and amount required to price interest rate derivatives. 
 * The data type is used in particular for Monte Carlo pricing.
 * <p>
 * One schedule is generated for instrument or package of instruments, they are not path dependent but a 
 * simplified description of the decisions or options to take and rate required to take those decisions.
 */
@BeanDefinition(factoryName = "of")
public final class MulticurveEquivalentSchedule
    implements ImmutableBean, Serializable {
  
  /** The list of multi-curve equivalent required to price an instrument. */
  @PropertyDefinition(validate = "notNull")
  private final List<MulticurveEquivalent> schedules;
  
  /**
   * Returns the list of decision times.
   * @return the times
   */
  public List<ZonedDateTime> getDecisionTimes(){
    List<ZonedDateTime> times = new ArrayList<>();
    for(MulticurveEquivalent mce: schedules) {
      times.add(mce.getDecisionTime());
    }
    return times;
  }
  
  /**
   * Returns the number of expiries.
   * @return the number of expiries
   */
  public int getExpiriesCount() {
    return schedules.size();
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code MulticurveEquivalentSchedule}.
   * @return the meta-bean, not null
   */
  public static MulticurveEquivalentSchedule.Meta meta() {
    return MulticurveEquivalentSchedule.Meta.INSTANCE;
  }

  static {
    MetaBean.register(MulticurveEquivalentSchedule.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Obtains an instance.
   * @param schedules  the value of the property, not null
   * @return the instance
   */
  public static MulticurveEquivalentSchedule of(
      List<MulticurveEquivalent> schedules) {
    return new MulticurveEquivalentSchedule(
      schedules);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static MulticurveEquivalentSchedule.Builder builder() {
    return new MulticurveEquivalentSchedule.Builder();
  }

  private MulticurveEquivalentSchedule(
      List<MulticurveEquivalent> schedules) {
    JodaBeanUtils.notNull(schedules, "schedules");
    this.schedules = ImmutableList.copyOf(schedules);
  }

  @Override
  public MulticurveEquivalentSchedule.Meta metaBean() {
    return MulticurveEquivalentSchedule.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the list of multi-curve equivalent required to price an instrument.
   * @return the value of the property, not null
   */
  public List<MulticurveEquivalent> getSchedules() {
    return schedules;
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
      MulticurveEquivalentSchedule other = (MulticurveEquivalentSchedule) obj;
      return JodaBeanUtils.equal(schedules, other.schedules);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(schedules);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("MulticurveEquivalentSchedule{");
    buf.append("schedules").append('=').append(JodaBeanUtils.toString(schedules));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MulticurveEquivalentSchedule}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code schedules} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<MulticurveEquivalent>> schedules = DirectMetaProperty.ofImmutable(
        this, "schedules", MulticurveEquivalentSchedule.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "schedules");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -160710468:  // schedules
          return schedules;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public MulticurveEquivalentSchedule.Builder builder() {
      return new MulticurveEquivalentSchedule.Builder();
    }

    @Override
    public Class<? extends MulticurveEquivalentSchedule> beanType() {
      return MulticurveEquivalentSchedule.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code schedules} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<MulticurveEquivalent>> schedules() {
      return schedules;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -160710468:  // schedules
          return ((MulticurveEquivalentSchedule) bean).getSchedules();
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
   * The bean-builder for {@code MulticurveEquivalentSchedule}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<MulticurveEquivalentSchedule> {

    private List<MulticurveEquivalent> schedules = ImmutableList.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(MulticurveEquivalentSchedule beanToCopy) {
      this.schedules = ImmutableList.copyOf(beanToCopy.getSchedules());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -160710468:  // schedules
          return schedules;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -160710468:  // schedules
          this.schedules = (List<MulticurveEquivalent>) newValue;
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
    public MulticurveEquivalentSchedule build() {
      return new MulticurveEquivalentSchedule(
          schedules);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the list of multi-curve equivalent required to price an instrument.
     * @param schedules  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder schedules(List<MulticurveEquivalent> schedules) {
      JodaBeanUtils.notNull(schedules, "schedules");
      this.schedules = schedules;
      return this;
    }

    /**
     * Sets the {@code schedules} property in the builder
     * from an array of objects.
     * @param schedules  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder schedules(MulticurveEquivalent... schedules) {
      return schedules(ImmutableList.copyOf(schedules));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("MulticurveEquivalentSchedule.Builder{");
      buf.append("schedules").append('=').append(JodaBeanUtils.toString(schedules));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
