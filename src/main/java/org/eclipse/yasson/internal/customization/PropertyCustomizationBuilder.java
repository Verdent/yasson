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

package org.eclipse.yasson.internal.customization;

import java.text.DateFormat;
import java.text.NumberFormat;
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

    private String serializationName;
    private boolean serializationNillable;
    private boolean ignoreSerializationName;
    private boolean ignoreSerializationNillable;

    private String derializationName;
    private boolean deserializationNillable;
    private boolean ignoreDerializationName;
    private boolean ignoreDeserializationNillable;

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
        if (scope == Scope.DESERIALIZATION) {
            derializationName = name;
        } else {
            serializationName = name;
        }
        return this;
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
        if (scope == Scope.DESERIALIZATION) {
            ignoreDerializationName = true;
        } else {
            ignoreSerializationName = true;
        }
        return this;
    }

    /**
     * Whether this component can be nillable.
     *
     * This number format is used for serialization and deserialization of the property.
     *
     * @param nillable nillable component
     * @return updated builder instance
     */
    @Override
    public PropertyCustomizationBuilder nillable(boolean nillable) {
        return nillable(nillable, Scope.SERIALIZATION).nillable(nillable, Scope.DESERIALIZATION);
    }

    /**
     * Whether this component should be nillable in the given {@link Scope}.
     *
     * @param nillable nillable component
     * @param scope    scope of the nillable
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder nillable(boolean nillable, Scope scope) {
        if (scope == Scope.DESERIALIZATION) {
            deserializationNillable = nillable;
        } else {
            serializationNillable = nillable;
        }
        return this;
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
        if (scope == Scope.DESERIALIZATION) {
            ignoreDeserializationNillable = true;
        } else {
            ignoreSerializationNillable = true;
        }
        return this;
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
        return numberFormat(numberFormat, Scope.SERIALIZATION).numberFormat(numberFormat, Scope.DESERIALIZATION);
    }

    /**
     * Set number format and locale which should be used in the given {@link Scope}.
     *
     * @param numberFormat number format
     * @param locale       number format locale
     * @param scope        scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder numberFormat(String numberFormat, Locale locale, Scope scope);

    /**
     * Set {@link NumberFormat} instance which should be used in the given {@link Scope}.
     *
     * @param numberFormat pre created NumberFormat instance
     * @param scope        scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder numberFormat(NumberFormat numberFormat, Scope scope);

    /**
     * Set number format which should be used in the given {@link Scope}.
     *
     * @param numberFormat number format
     * @param scope        scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder numberFormat(String numberFormat, Scope scope) {
        return numberFormat(numberFormat, Locale.getDefault(), scope);
    }

    /**
     * Set number format locale which should be used in the given {@link Scope}.
     *
     * @param locale number format locale
     * @param scope  scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder numberFormat(Locale locale, Scope scope) {
        return numberFormat("", locale, scope);
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
    public PropertyCustomizationBuilder ignoreNumberFormat(Scope scope);

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
        return dateFormat(dateFormat, Locale.getDefault(), Scope.SERIALIZATION)
                .dateFormat(dateFormat, Locale.getDefault(), Scope.DESERIALIZATION);
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
        return dateFormat(JsonbDateFormat.DEFAULT_FORMAT, locale, Scope.SERIALIZATION)
                .dateFormat(JsonbDateFormat.DEFAULT_FORMAT, locale, Scope.DESERIALIZATION);
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
    public PropertyCustomizationBuilder dateFormat(DateFormat dateFormat) {
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
    public PropertyCustomizationBuilder dateFormat(String dateFormat, Locale locale, Scope scope);

    /**
     * Set {@link DateFormat} instance which should be used in the given {@link Scope}.
     *
     * @param dateFormat pre created DateFormat instance
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder dateFormat(DateFormat dateFormat, Scope scope);

    /**
     * Set date format which should be used in the given {@link Scope}.
     *
     * @param dateFormat date format
     * @param scope      scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder dateFormat(String dateFormat, Scope scope) {
        return dateFormat(dateFormat, Locale.getDefault(), scope);
    }

    /**
     * Set date format locale which should be used in the given {@link Scope}.
     *
     * @param locale date format locale
     * @param scope  scope of the format
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder dateFormat(Locale locale, Scope scope) {
        return dateFormat(JsonbDateFormat.DEFAULT_FORMAT, locale, scope);
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

    @Override
    public PropertyCustomization build() {
        return new PropertyCustomizationImpl(this);
    }

    /**
     * Ignore date format customization of this property in the given {@link Scope}.
     *
     * @return updated builder instance
     */
    public PropertyCustomizationBuilder ignoreDateFormat(Scope scope);

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
    public PropertyCustomizationBuilder transientProperty(boolean isTransient, Scope scope);

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
    public PropertyCustomizationBuilder ignoreTransient(Scope scope);

}
