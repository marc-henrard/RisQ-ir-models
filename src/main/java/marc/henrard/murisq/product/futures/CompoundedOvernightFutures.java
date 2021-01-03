/**
 * Copyright (C) 2018 - present by Marc Henrard.
 */
package marc.henrard.murisq.product.futures;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.ImmutablePreBuild;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.Resolvable;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.index.OvernightIndex;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.product.SecuritizedProduct;
import com.opengamma.strata.product.SecurityId;

/**
 * Description of a futures contract based on an overnight index with composition similar to OIS.
 * <p>
 * The settlement price is based on the simple rate generating a investment equivalent, on the accrual 
 * period, to the composition of overnight investments at the overnight benchmark rates, with all the rates 
 * using the benchmark conventions. The Exchange Delivery Settlement Price is given by 100% - above rate.
 * 
 * @author Marc Henrard
 */
@BeanDefinition
public final class CompoundedOvernightFutures
    implements SecuritizedProduct, Resolvable<CompoundedOvernightFuturesResolved>, ImmutableBean, Serializable {

  /**
   * The security identifier.
   * <p>
   * This identifier uniquely identifies the security within the system.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final SecurityId securityId;
  /**
   * The currency that the future is traded in, defaulted from the index if not set.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final Currency currency;
  /**
   * The notional amount.
   * <p>
   * This is the full notional of the futures, such as 500,000 GBP.
   */
  @PropertyDefinition(validate = "ArgChecker.notNegativeOrZero")
  private final double notional;
  /**
   * The accrual factor, defaulted from the index if not set.
   * <p>
   * This is the year fraction of the contract, typically around 0.25 for a futures with a 3 month period.
   */
  @PropertyDefinition(validate = "ArgChecker.notNegativeOrZero")
  private final double accrualFactor;
  /**
   * The start accrual date
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate startAccrualDate;
  /**
   * The start accrual date
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate endAccrualDate;
  /**
   * The underlying overnight index.
   */
  @PropertyDefinition(validate = "notNull")
  private final OvernightIndex index;

  @ImmutablePreBuild
  private static void preBuild(Builder builder) {
    if (builder.index != null) {
      if (builder.accrualFactor == 0d) {
        builder.accrualFactor(builder.index.getDayCount()
            .relativeYearFraction(builder.startAccrualDate, builder.endAccrualDate));
      }
      if (builder.currency == null) {
        builder.currency = builder.index.getCurrency();
      }
    }
  }

  @Override
  public CompoundedOvernightFuturesResolved resolve(ReferenceData refData) {
    HolidayCalendar calendar = refData.getValue(index.getFixingCalendar());
    List<LocalDate> onDates = new ArrayList<>();
    LocalDate currentDate = startAccrualDate;
    while (!currentDate.isAfter(endAccrualDate)) {
      onDates.add(currentDate);
      currentDate = calendar.next(currentDate);
    }
    List<Double> deltai = new ArrayList<>();
    for (int i = 0; i < onDates.size() - 1; i++) {
      deltai.add(index.getDayCount().relativeYearFraction(onDates.get(i), onDates.get(i + 1)));
    }
    return CompoundedOvernightFuturesResolved.builder()
        .securityId(securityId)
        .currency(currency)
        .notional(notional)
        .accrualFactor(accrualFactor)
        .startAccrualDate(startAccrualDate)
        .endAccrualDate(endAccrualDate)
        .index(index)
        .onDates(onDates)
        .onAccruals(deltai).build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code CompoundedOvernightFutures}.
   * @return the meta-bean, not null
   */
  public static CompoundedOvernightFutures.Meta meta() {
    return CompoundedOvernightFutures.Meta.INSTANCE;
  }

  static {
    MetaBean.register(CompoundedOvernightFutures.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CompoundedOvernightFutures.Builder builder() {
    return new CompoundedOvernightFutures.Builder();
  }

  private CompoundedOvernightFutures(
      SecurityId securityId,
      Currency currency,
      double notional,
      double accrualFactor,
      LocalDate startAccrualDate,
      LocalDate endAccrualDate,
      OvernightIndex index) {
    JodaBeanUtils.notNull(securityId, "securityId");
    JodaBeanUtils.notNull(currency, "currency");
    ArgChecker.notNegativeOrZero(notional, "notional");
    ArgChecker.notNegativeOrZero(accrualFactor, "accrualFactor");
    JodaBeanUtils.notNull(startAccrualDate, "startAccrualDate");
    JodaBeanUtils.notNull(endAccrualDate, "endAccrualDate");
    JodaBeanUtils.notNull(index, "index");
    this.securityId = securityId;
    this.currency = currency;
    this.notional = notional;
    this.accrualFactor = accrualFactor;
    this.startAccrualDate = startAccrualDate;
    this.endAccrualDate = endAccrualDate;
    this.index = index;
  }

  @Override
  public CompoundedOvernightFutures.Meta metaBean() {
    return CompoundedOvernightFutures.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security identifier.
   * <p>
   * This identifier uniquely identifies the security within the system.
   * @return the value of the property, not null
   */
  @Override
  public SecurityId getSecurityId() {
    return securityId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency that the future is traded in, defaulted from the index if not set.
   * @return the value of the property, not null
   */
  @Override
  public Currency getCurrency() {
    return currency;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the notional amount.
   * <p>
   * This is the full notional of the futures, such as 500,000 GBP.
   * @return the value of the property
   */
  public double getNotional() {
    return notional;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the accrual factor, defaulted from the index if not set.
   * <p>
   * This is the year fraction of the contract, typically around 0.25 for a futures with a 3 month period.
   * @return the value of the property
   */
  public double getAccrualFactor() {
    return accrualFactor;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start accrual date
   * @return the value of the property, not null
   */
  public LocalDate getStartAccrualDate() {
    return startAccrualDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start accrual date
   * @return the value of the property, not null
   */
  public LocalDate getEndAccrualDate() {
    return endAccrualDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying overnight index.
   * @return the value of the property, not null
   */
  public OvernightIndex getIndex() {
    return index;
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
      CompoundedOvernightFutures other = (CompoundedOvernightFutures) obj;
      return JodaBeanUtils.equal(securityId, other.securityId) &&
          JodaBeanUtils.equal(currency, other.currency) &&
          JodaBeanUtils.equal(notional, other.notional) &&
          JodaBeanUtils.equal(accrualFactor, other.accrualFactor) &&
          JodaBeanUtils.equal(startAccrualDate, other.startAccrualDate) &&
          JodaBeanUtils.equal(endAccrualDate, other.endAccrualDate) &&
          JodaBeanUtils.equal(index, other.index);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(securityId);
    hash = hash * 31 + JodaBeanUtils.hashCode(currency);
    hash = hash * 31 + JodaBeanUtils.hashCode(notional);
    hash = hash * 31 + JodaBeanUtils.hashCode(accrualFactor);
    hash = hash * 31 + JodaBeanUtils.hashCode(startAccrualDate);
    hash = hash * 31 + JodaBeanUtils.hashCode(endAccrualDate);
    hash = hash * 31 + JodaBeanUtils.hashCode(index);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("CompoundedOvernightFutures{");
    buf.append("securityId").append('=').append(JodaBeanUtils.toString(securityId)).append(',').append(' ');
    buf.append("currency").append('=').append(JodaBeanUtils.toString(currency)).append(',').append(' ');
    buf.append("notional").append('=').append(JodaBeanUtils.toString(notional)).append(',').append(' ');
    buf.append("accrualFactor").append('=').append(JodaBeanUtils.toString(accrualFactor)).append(',').append(' ');
    buf.append("startAccrualDate").append('=').append(JodaBeanUtils.toString(startAccrualDate)).append(',').append(' ');
    buf.append("endAccrualDate").append('=').append(JodaBeanUtils.toString(endAccrualDate)).append(',').append(' ');
    buf.append("index").append('=').append(JodaBeanUtils.toString(index));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CompoundedOvernightFutures}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code securityId} property.
     */
    private final MetaProperty<SecurityId> securityId = DirectMetaProperty.ofImmutable(
        this, "securityId", CompoundedOvernightFutures.class, SecurityId.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> currency = DirectMetaProperty.ofImmutable(
        this, "currency", CompoundedOvernightFutures.class, Currency.class);
    /**
     * The meta-property for the {@code notional} property.
     */
    private final MetaProperty<Double> notional = DirectMetaProperty.ofImmutable(
        this, "notional", CompoundedOvernightFutures.class, Double.TYPE);
    /**
     * The meta-property for the {@code accrualFactor} property.
     */
    private final MetaProperty<Double> accrualFactor = DirectMetaProperty.ofImmutable(
        this, "accrualFactor", CompoundedOvernightFutures.class, Double.TYPE);
    /**
     * The meta-property for the {@code startAccrualDate} property.
     */
    private final MetaProperty<LocalDate> startAccrualDate = DirectMetaProperty.ofImmutable(
        this, "startAccrualDate", CompoundedOvernightFutures.class, LocalDate.class);
    /**
     * The meta-property for the {@code endAccrualDate} property.
     */
    private final MetaProperty<LocalDate> endAccrualDate = DirectMetaProperty.ofImmutable(
        this, "endAccrualDate", CompoundedOvernightFutures.class, LocalDate.class);
    /**
     * The meta-property for the {@code index} property.
     */
    private final MetaProperty<OvernightIndex> index = DirectMetaProperty.ofImmutable(
        this, "index", CompoundedOvernightFutures.class, OvernightIndex.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "securityId",
        "currency",
        "notional",
        "accrualFactor",
        "startAccrualDate",
        "endAccrualDate",
        "index");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1574023291:  // securityId
          return securityId;
        case 575402001:  // currency
          return currency;
        case 1585636160:  // notional
          return notional;
        case -1540322338:  // accrualFactor
          return accrualFactor;
        case 198872123:  // startAccrualDate
          return startAccrualDate;
        case 1379430946:  // endAccrualDate
          return endAccrualDate;
        case 100346066:  // index
          return index;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CompoundedOvernightFutures.Builder builder() {
      return new CompoundedOvernightFutures.Builder();
    }

    @Override
    public Class<? extends CompoundedOvernightFutures> beanType() {
      return CompoundedOvernightFutures.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code securityId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<SecurityId> securityId() {
      return securityId;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Currency> currency() {
      return currency;
    }

    /**
     * The meta-property for the {@code notional} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> notional() {
      return notional;
    }

    /**
     * The meta-property for the {@code accrualFactor} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> accrualFactor() {
      return accrualFactor;
    }

    /**
     * The meta-property for the {@code startAccrualDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> startAccrualDate() {
      return startAccrualDate;
    }

    /**
     * The meta-property for the {@code endAccrualDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> endAccrualDate() {
      return endAccrualDate;
    }

    /**
     * The meta-property for the {@code index} property.
     * @return the meta-property, not null
     */
    public MetaProperty<OvernightIndex> index() {
      return index;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1574023291:  // securityId
          return ((CompoundedOvernightFutures) bean).getSecurityId();
        case 575402001:  // currency
          return ((CompoundedOvernightFutures) bean).getCurrency();
        case 1585636160:  // notional
          return ((CompoundedOvernightFutures) bean).getNotional();
        case -1540322338:  // accrualFactor
          return ((CompoundedOvernightFutures) bean).getAccrualFactor();
        case 198872123:  // startAccrualDate
          return ((CompoundedOvernightFutures) bean).getStartAccrualDate();
        case 1379430946:  // endAccrualDate
          return ((CompoundedOvernightFutures) bean).getEndAccrualDate();
        case 100346066:  // index
          return ((CompoundedOvernightFutures) bean).getIndex();
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
   * The bean-builder for {@code CompoundedOvernightFutures}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CompoundedOvernightFutures> {

    private SecurityId securityId;
    private Currency currency;
    private double notional;
    private double accrualFactor;
    private LocalDate startAccrualDate;
    private LocalDate endAccrualDate;
    private OvernightIndex index;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CompoundedOvernightFutures beanToCopy) {
      this.securityId = beanToCopy.getSecurityId();
      this.currency = beanToCopy.getCurrency();
      this.notional = beanToCopy.getNotional();
      this.accrualFactor = beanToCopy.getAccrualFactor();
      this.startAccrualDate = beanToCopy.getStartAccrualDate();
      this.endAccrualDate = beanToCopy.getEndAccrualDate();
      this.index = beanToCopy.getIndex();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1574023291:  // securityId
          return securityId;
        case 575402001:  // currency
          return currency;
        case 1585636160:  // notional
          return notional;
        case -1540322338:  // accrualFactor
          return accrualFactor;
        case 198872123:  // startAccrualDate
          return startAccrualDate;
        case 1379430946:  // endAccrualDate
          return endAccrualDate;
        case 100346066:  // index
          return index;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1574023291:  // securityId
          this.securityId = (SecurityId) newValue;
          break;
        case 575402001:  // currency
          this.currency = (Currency) newValue;
          break;
        case 1585636160:  // notional
          this.notional = (Double) newValue;
          break;
        case -1540322338:  // accrualFactor
          this.accrualFactor = (Double) newValue;
          break;
        case 198872123:  // startAccrualDate
          this.startAccrualDate = (LocalDate) newValue;
          break;
        case 1379430946:  // endAccrualDate
          this.endAccrualDate = (LocalDate) newValue;
          break;
        case 100346066:  // index
          this.index = (OvernightIndex) newValue;
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
    public CompoundedOvernightFutures build() {
      preBuild(this);
      return new CompoundedOvernightFutures(
          securityId,
          currency,
          notional,
          accrualFactor,
          startAccrualDate,
          endAccrualDate,
          index);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the security identifier.
     * <p>
     * This identifier uniquely identifies the security within the system.
     * @param securityId  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder securityId(SecurityId securityId) {
      JodaBeanUtils.notNull(securityId, "securityId");
      this.securityId = securityId;
      return this;
    }

    /**
     * Sets the currency that the future is traded in, defaulted from the index if not set.
     * @param currency  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder currency(Currency currency) {
      JodaBeanUtils.notNull(currency, "currency");
      this.currency = currency;
      return this;
    }

    /**
     * Sets the notional amount.
     * <p>
     * This is the full notional of the futures, such as 500,000 GBP.
     * @param notional  the new value
     * @return this, for chaining, not null
     */
    public Builder notional(double notional) {
      ArgChecker.notNegativeOrZero(notional, "notional");
      this.notional = notional;
      return this;
    }

    /**
     * Sets the accrual factor, defaulted from the index if not set.
     * <p>
     * This is the year fraction of the contract, typically around 0.25 for a futures with a 3 month period.
     * @param accrualFactor  the new value
     * @return this, for chaining, not null
     */
    public Builder accrualFactor(double accrualFactor) {
      ArgChecker.notNegativeOrZero(accrualFactor, "accrualFactor");
      this.accrualFactor = accrualFactor;
      return this;
    }

    /**
     * Sets the start accrual date
     * @param startAccrualDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder startAccrualDate(LocalDate startAccrualDate) {
      JodaBeanUtils.notNull(startAccrualDate, "startAccrualDate");
      this.startAccrualDate = startAccrualDate;
      return this;
    }

    /**
     * Sets the start accrual date
     * @param endAccrualDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder endAccrualDate(LocalDate endAccrualDate) {
      JodaBeanUtils.notNull(endAccrualDate, "endAccrualDate");
      this.endAccrualDate = endAccrualDate;
      return this;
    }

    /**
     * Sets the underlying overnight index.
     * @param index  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder index(OvernightIndex index) {
      JodaBeanUtils.notNull(index, "index");
      this.index = index;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(256);
      buf.append("CompoundedOvernightFutures.Builder{");
      buf.append("securityId").append('=').append(JodaBeanUtils.toString(securityId)).append(',').append(' ');
      buf.append("currency").append('=').append(JodaBeanUtils.toString(currency)).append(',').append(' ');
      buf.append("notional").append('=').append(JodaBeanUtils.toString(notional)).append(',').append(' ');
      buf.append("accrualFactor").append('=').append(JodaBeanUtils.toString(accrualFactor)).append(',').append(' ');
      buf.append("startAccrualDate").append('=').append(JodaBeanUtils.toString(startAccrualDate)).append(',').append(' ');
      buf.append("endAccrualDate").append('=').append(JodaBeanUtils.toString(endAccrualDate)).append(',').append(' ');
      buf.append("index").append('=').append(JodaBeanUtils.toString(index));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
