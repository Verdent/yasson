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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.serializer.JsonbDeserializer;

/**
 * Common interface for all customization builders.
 */
@SuppressWarnings("unchecked")
abstract class YassonCustomizationBuilder<T extends YassonCustomizationBuilder<T, B>, B extends YassonCustomization> {

    private Locale numberLocale;
    private boolean ignoreNillable = false;
    private boolean ignoreDeserializer = false;
    private boolean ignoreAdapter = false;
    private boolean ignoreNumberFormat = false;
    private boolean ignoreDateFormat = false;
    private boolean ignoreAllCustomization = false;
    private Boolean nillable;
    private JsonbDeserializer<?> deserializer;
    private JsonbAdapter<?, ?> adapter;
    private NumberFormatCustomization numberFormat;
    private DateFormatCustomization dateFormat;

    protected static NumberFormat createNumberFormat(String format, Locale locale) {
        //TODO NumberFormat.getCompactNumberInstance(locale, NumberFormat.Style.valueOf(numberFormat)) test
        final NumberFormat numberFormat = NumberFormat.getInstance(locale);
        ((DecimalFormat) numberFormat).applyPattern(format);
        return numberFormat;
    }

    /**
     * Whether this component can be nillable.
     *
     * @param nillable nillable component
     * @return updated builder instance
     */
    public T nillable(boolean nillable) {
        this.nillable = nillable;
        return (T) this;
    }

    /**
     * Ignore nillable annotation on this component.
     *
     * @return updated builder instance
     */
    public T ignoreNillable() {
        ignoreNillable = true;
        return (T) this;
    }

    /**
     * Set {@link JsonbDeserializer} which should be used.
     *
     * @param deserializer component deserializer
     * @return updated builder instance
     */
    public T deserializer(JsonbDeserializer<?> deserializer) {
        this.deserializer = deserializer;
        return (T) this;
    }

    /**
     * Ignore deserializer of this component.
     *
     * @return updated builder instance
     */
    public T ignoreDeserializer() {
        ignoreDeserializer = true;
        return (T) this;
    }

    /**
     * Set {@link JsonbAdapter} which should be used.
     *
     * @param adapter component adapter
     * @return updated builder instance
     */
    public T adapter(JsonbAdapter<?, ?> adapter) {
        this.adapter = adapter;
        return (T) this;
    }

    /**
     * Ignore adapter of this component.
     *
     * @return updated builder instance
     */
    public T ignoreAdapter() {
        ignoreAdapter = true;
        return (T) this;
    }

    /**
     * Set number format and locale which should be used.
     *
     * @param numberFormat number format
     * @param locale locale
     * @return updated builder instance
     */
    public T numberFormat(String numberFormat, Locale locale) {
        return numberFormat(numberFormat, locale, null);
    }

    /**
     * Set number format which should be used.
     *
     * @param numberFormat number format
     * @return updated builder instance
     */
    public T numberFormat(String numberFormat) {
        return numberFormat(numberFormat, null);
    }

    /**
     * Set locale which should be used.
     *
     * @param locale locale
     * @return updated builder instance
     */
    public T numberFormat(Locale locale) {
        return numberFormat(null, locale);
    }

    /**
     * Set {@link NumberFormat} instance which should be used.
     *
     * @param numberFormat pre created NumberFormat instance
     * @return updated builder instance
     */
    public T numberFormat(NumberFormat numberFormat) {
        return numberFormat(null, null, numberFormat);
    }

    private T numberFormat(String format, Locale locale, NumberFormat numberFormat) {
        this.numberFormat = new NumberFormatCustomization(format, locale, numberFormat);
        return (T) this;
    }

    /**
     * Ignore number format customization of this component.
     *
     * @return updated builder instance
     */
    public T ignoreNumberFormat() {
        this.ignoreNumberFormat = true;
        return (T) this;
    }

    /**
     * Set date format and locale which should be used.
     *
     * @param dateFormat date format
     * @param locale locale
     * @return updated builder instance
     */
    public T dateFormat(String dateFormat, Locale locale) {
        return dateFormat(dateFormat, locale, null);
    }

    /**
     * Set date format which should be used.
     *
     * @param dateFormat date format
     * @return updated builder instance
     */
    public T dateFormat(String dateFormat) {
        return dateFormat(dateFormat, null);
    }

    /**
     * Set locale which should be used.
     *
     * @param locale locale
     * @return updated builder instance
     */
    public T dateFormat(Locale locale) {
        return dateFormat(null, locale);
    }

    /**
     * Set {@link DateTimeFormatter} instance which should be used.
     *
     * @param dateFormat pre created DateFormat instance
     * @return updated builder instance
     */
    public T dateFormat(DateTimeFormatter dateFormat) {
        return dateFormat(null, null, dateFormat);
    }

    private T dateFormat(String format, Locale locale, DateTimeFormatter dateTimeFormatter) {
        this.dateFormat = new DateFormatCustomization(format, locale, dateTimeFormatter);
        return (T) this;
    }

    /**
     * Ignore date format customization of this component.
     *
     * @return updated builder instance
     */
    public T ignoreDateFormat() {
        ignoreDateFormat = true;
        return (T) this;
    }

    /**
     * Ignore all the customizations.
     *
     * @return updated builder instance
     */
    public T ignoreAllCustomizations() {
        ignoreAllCustomization = true;
        return (T) this;
    }

    /**
     * Build the new instance from this builder.
     *
     * @return new instance
     */
    public abstract B build();

    boolean isIgnoreAllCustomization() {
        return ignoreAllCustomization;
    }

    boolean isIgnoreNillable() {
        return ignoreNillable;
    }

    boolean isIgnoreDeserializer() {
        return ignoreDeserializer;
    }

    boolean isIgnoreAdapter() {
        return ignoreAdapter;
    }

    boolean isIgnoreNumberFormat() {
        return ignoreNumberFormat;
    }

    boolean isIgnoreDateFormat() {
        return ignoreDateFormat;
    }

    Boolean isNillable() {
        return nillable;
    }

    JsonbDeserializer<?> getDeserializer() {
        return deserializer;
    }

    JsonbAdapter<?, ?> getAdapter() {
        return adapter;
    }

    NumberFormatCustomization getNumberFormat() {
        return numberFormat;
    }

    DateFormatCustomization getDateFormat() {
        return dateFormat;
    }
}
