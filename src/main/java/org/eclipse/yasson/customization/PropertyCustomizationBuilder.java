/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.eclipse.yasson.customization;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.json.bind.annotation.JsonbDateFormat;

/**
 * Builder of the specific class property customization.
 * <br>
 * Customization of the property accessor methods is done over the methods with the {@link Scope}
 * parameter. Methods without any specific {@link Scope} parameter are setting required customization
 * to the deserialization and serialization.
 * <p>
 * For example having getter method with custom name like this
 * <pre>{@code
 * @JsonbProperty("customName")
 * public String getExample() { .... }
 * }</pre>
 * is effectively the same as having it set like this using the builder
 * <pre>{@code
 * builder.name("customName", Scope.DESERIALIZATION);
 * }</pre>
 * </p>
 */
public final class PropertyCustomizationBuilder
        extends SerializationCustomizationBuilder<PropertyCustomizationBuilder, PropertyCustomization> {

    private final String propertyName;

    private String deserializationName;
    private String serializationName;
    private NumberFormatCustomization serializationNumberFormat;
    private NumberFormatCustomization deserializationNumberFormat;
    private DateFormatCustomization serializationDateFormat;
    private DateFormatCustomization deserializationDateFormat;
    private Boolean deserializationTransient;
    private Boolean serializationTransient;
    private boolean ignoreDeserializationName;
    private boolean ignoreSerializationName;
    private boolean ignoreDeserializationNillable;
    private boolean ignoreSerializationNillable;
    private boolean ignoreDeserializationNumberFormat;
    private boolean ignoreSerializationNumberFormat;
    private boolean ignoreDeserializationDateFormat;
    private boolean ignoreSerializationDateFormat;
    private boolean ignoreDeserializationTransient;
    private boolean ignoreSerializationTransient;

    PropertyCustomizationBuilder(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Set custom property name for serialization and deserialization.
     *
     * @param name custom property name
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder name(String name) {
        return name(name, Scope.SERIALIZATION).name(name, Scope.DESERIALIZATION);
    }

    /**
     * Set custom property name bound to the selected {@link Scope}.
     *
     * @param name  custom property name
     * @param scope scope to which this name is bound
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder name(String name, Scope scope) {
        return setValue(scope, () -> serializationName = name, () -> deserializationName = name);
    }

    /**
     * Ignore custom property name.
     *
     * Custom property name will be ignored for serialization and deserialization.
     *
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder ignoreName() {
        return ignoreName(Scope.SERIALIZATION)
                .ignoreName(Scope.DESERIALIZATION);
    }

    /**
     * Ignore custom property name in the given {@link Scope}.
     *
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder ignoreName(Scope scope) {
        return setValue(scope, () -> ignoreSerializationName = true, () -> ignoreDeserializationName = true);
    }

    /**
     * Ignore nillable customization of this component.
     *
     * Nillable customization will be ignored for both serialization and deserialization.
     *
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder ignoreNillable() {
        return ignoreNillable(Scope.SERIALIZATION).ignoreNillable(Scope.DESERIALIZATION);
    }

    /**
     * Ignore nillable customization of this component in the given {@link Scope}.
     *
     * @param scope ignored scope
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder ignoreNillable(Scope scope) {
        return setValue(scope, () -> ignoreSerializationNillable = true, () -> ignoreDeserializationNillable = true);
    }

    /**
     * Set number format which should be used.
     *
     * This number format is used for serialization and deserialization of the property.
     *
     * @param numberFormat number format
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder numberFormat(String numberFormat) {
        return numberFormat(numberFormat, Scope.SERIALIZATION).numberFormat(numberFormat, Scope.DESERIALIZATION);
    }

    /**
     * Set number format locale which should be used.
     *
     * This locale is used for serialization and deserialization of the property.
     *
     * @param locale locale
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder numberFormat(Locale locale) {
        return numberFormat(locale, Scope.SERIALIZATION).numberFormat(locale, Scope.DESERIALIZATION);
    }

    /**
     * Set number format and locale which should be used.
     *
     * Both number format and locale are used for serialization and deserialization of the property.
     *
     * @param numberFormat number format
     * @param locale       locale
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder numberFormat(String numberFormat, Locale locale) {
        return numberFormat(numberFormat, locale, Scope.SERIALIZATION).numberFormat(numberFormat, locale, Scope.DESERIALIZATION);
    }

    /**
     * Set {@link NumberFormat} instance which should be used.
     *
     * This {@link NumberFormat} instance is used for serialization and deserialization of the property.
     *
     * @param numberFormat pre created NumberFormat instance
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder numberFormat(NumberFormat numberFormat) {
        return numberFormat(numberFormat, Scope.SERIALIZATION)
                .numberFormat(numberFormat, Scope.DESERIALIZATION);
    }

    /**
     * Set number format and locale which should be used in the given {@link Scope}.
     *
     * @param numberFormat number format
     * @param locale       number format locale
     * @param scope        scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder numberFormat(String numberFormat, Locale locale, Scope scope) {
        NumberFormatCustomization customization = new NumberFormatCustomization(numberFormat, locale, null);
        return setValue(scope, () -> serializationNumberFormat = customization, () -> deserializationNumberFormat = customization);
    }

    /**
     * Set {@link NumberFormat} instance which should be used in the given {@link Scope}.
     *
     * @param numberFormat pre created NumberFormat instance
     * @param scope        scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder numberFormat(NumberFormat numberFormat, Scope scope) {
        NumberFormatCustomization customization = new NumberFormatCustomization(null, null, numberFormat);
        return setValue(scope, () -> serializationNumberFormat = customization, () -> deserializationNumberFormat = customization);
    }

    /**
     * Set number format which should be used in the given {@link Scope}.
     *
     * @param numberFormat number format
     * @param scope        scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder numberFormat(String numberFormat, Scope scope) {
        return numberFormat(numberFormat, null, scope);
    }

    /**
     * Set number format locale which should be used in the given {@link Scope}.
     *
     * @param locale number format locale
     * @param scope  scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder numberFormat(Locale locale, Scope scope) {
        return numberFormat(null, locale, scope);
    }

    /**
     * Ignore number format and locale customization of this property.
     *
     * Both number format and locale will be ignored for serialization and deserialization of the property.
     *
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder ignoreNumberFormat() {
        return ignoreNumberFormat(Scope.SERIALIZATION).ignoreTransient(Scope.DESERIALIZATION);
    }

    /**
     * Ignore number format customization of this property in the given {@link Scope}.
     *
     * @param scope ignored scope
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder ignoreNumberFormat(Scope scope) {
        return setValue(scope, () -> ignoreSerializationNumberFormat = true, () -> ignoreDeserializationNumberFormat = true);
    }

    /**
     * Set date format and locale which should be used.
     *
     * Both date format and locale are used for serialization and deserialization of the property.
     *
     * @param dateFormat date format
     * @param locale     locale
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder dateFormat(String dateFormat, Locale locale) {
        return dateFormat(dateFormat, locale, Scope.SERIALIZATION).dateFormat(dateFormat, locale, Scope.DESERIALIZATION);
    }

    /**
     * Set date format which should be used.
     *
     * This date format is used for serialization and deserialization of the property.
     *
     * @param dateFormat date format
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder dateFormat(String dateFormat) {
        return dateFormat(dateFormat, Scope.SERIALIZATION).dateFormat(dateFormat, Scope.DESERIALIZATION);
    }

    /**
     * Set date format locale which should be used.
     *
     * This date format locale is used for serialization and deserialization of the property.
     *
     * @param locale locale
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder dateFormat(Locale locale) {
        return dateFormat(locale, Scope.SERIALIZATION).dateFormat(locale, Scope.DESERIALIZATION);
    }

    /**
     * Set {@link DateFormat} instance which should be used.
     *
     * This {@link DateFormat} instance is used for serialization and deserialization of the property.
     *
     * @param dateFormat pre created DateFormat instance
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder dateFormat(DateTimeFormatter dateFormat) {
        return dateFormat(dateFormat, Scope.SERIALIZATION).dateFormat(dateFormat, Scope.DESERIALIZATION);
    }

    /**
     * Set date format and locale which should be used in the given {@link Scope}.
     *
     * @param dateFormat date format
     * @param locale     date format locale
     * @param scope      scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder dateFormat(String dateFormat, Locale locale, Scope scope) {
        DateFormatCustomization customization = new DateFormatCustomization(dateFormat, locale, null);
        return setValue(scope, () -> serializationDateFormat = customization, () -> deserializationDateFormat = customization);
    }

    /**
     * Set {@link DateFormat} instance which should be used in the given {@link Scope}.
     *
     * @param dateFormat pre created DateFormat instance
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder dateFormat(DateTimeFormatter dateFormat, Scope scope) {
        DateFormatCustomization customization = new DateFormatCustomization(null, null, dateFormat);
        return setValue(scope, () -> serializationDateFormat = customization, () -> deserializationDateFormat = customization);
    }

    /**
     * Set date format which should be used in the given {@link Scope}.
     *
     * @param dateFormat date format
     * @param scope      scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder dateFormat(String dateFormat, Scope scope) {
        return dateFormat(dateFormat, null, scope);
    }

    /**
     * Set date format locale which should be used in the given {@link Scope}.
     *
     * @param locale date format locale
     * @param scope  scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder dateFormat(Locale locale, Scope scope) {
        return dateFormat(null, locale, scope);
    }

    /**
     * Ignore date format customization of this property.
     *
     * The date format is ignored for serialization and deserialization of the property.
     *
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder ignoreDateFormat() {
        return ignoreDateFormat(Scope.SERIALIZATION).ignoreDateFormat(Scope.DESERIALIZATION);
    }

    /**
     * Ignore date format customization of this property in the given {@link Scope}.
     *
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder ignoreDateFormat(Scope scope) {
        return setValue(scope, () -> ignoreSerializationDateFormat = true, () -> ignoreDeserializationDateFormat = true);
    }

    /**
     * Whether the property is transient.
     *
     * This value of the property will be set for serialization and deserialization.
     *
     * @param isTransient transient property
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder transientProperty(boolean isTransient) {
        return transientProperty(isTransient, Scope.SERIALIZATION).transientProperty(isTransient, Scope.DESERIALIZATION);
    }

    /**
     * Whether the property is transient in the given {@link Scope}.
     *
     * @param isTransient transient property
     * @param scope       transient scope
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder transientProperty(boolean isTransient, Scope scope) {
        return setValue(scope, () -> serializationTransient = isTransient,() -> deserializationTransient = isTransient);
    }

    /**
     * Ignore transient status of this property.
     *
     * Transient will be ignored for serialization and deserialization.
     *
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder ignoreTransient() {
        return ignoreTransient(Scope.SERIALIZATION).ignoreTransient(Scope.DESERIALIZATION);
    }

    /**
     * Ignore transient status of this property in the given {@link Scope}.
     *
     * @param scope transient ignore scope
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder ignoreTransient(Scope scope) {
        return setValue(scope, () -> ignoreSerializationTransient = true, () -> ignoreDeserializationTransient = true);
    }

    @Override
    public PropertyCustomization build() {
        return new PropertyCustomizationImpl(this);
    }

    private PropertyCustomizationBuilder setValue(Scope scope, Runnable serializationScope, Runnable deserializationScope) {
        if (scope == Scope.SERIALIZATION) {
            serializationScope.run();
        } else {
            deserializationScope.run();
        }
        return this;
    }

    String getPropertyName() {
        return propertyName;
    }

    String getDeserializationName() {
        return deserializationName;
    }

    String getSerializationName() {
        return serializationName;
    }

    NumberFormatCustomization getSerializationNumberFormat() {
        return serializationNumberFormat;
    }

    NumberFormatCustomization getDeserializationNumberFormat() {
        return deserializationNumberFormat;
    }

    DateFormatCustomization getSerializationDateFormat() {
        return serializationDateFormat;
    }

    DateFormatCustomization getDeserializationDateFormat() {
        return deserializationDateFormat;
    }

    Boolean isDeserializationTransient() {
        return deserializationTransient;
    }

    Boolean isSerializationTransient() {
        return serializationTransient;
    }

    boolean isIgnoreDeserializationName() {
        return ignoreDeserializationName;
    }

    boolean isIgnoreSerializationName() {
        return ignoreSerializationName;
    }

    boolean isIgnoreDeserializationNillable() {
        return ignoreDeserializationNillable;
    }

    boolean isIgnoreSerializationNillable() {
        return ignoreSerializationNillable;
    }

    boolean isIgnoreDeserializationNumberFormat() {
        return ignoreDeserializationNumberFormat;
    }

    boolean isIgnoreSerializationNumberFormat() {
        return ignoreSerializationNumberFormat;
    }

    boolean isIgnoreDeserializationDateFormat() {
        return ignoreDeserializationDateFormat;
    }

    boolean isIgnoreSerializationDateFormat() {
        return ignoreSerializationDateFormat;
    }

    boolean isIgnoreDeserializationTransient() {
        return ignoreDeserializationTransient;
    }

    boolean isIgnoreSerializationTransient() {
        return ignoreSerializationTransient;
    }

}
