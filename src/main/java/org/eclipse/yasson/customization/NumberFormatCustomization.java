package org.eclipse.yasson.customization;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * TODO javadoc
 */
public class NumberFormatCustomization {

    private final String format;
    private final Locale locale;
    private final NumberFormat numberFormat;

    NumberFormatCustomization(String format, Locale locale, NumberFormat numberFormat) {
        this.format = format;
        this.locale = locale;
        this.numberFormat = numberFormat;
    }

    public Optional<String> getFormat() {
        return Optional.ofNullable(format);
    }

    public Optional<Locale> getLocale() {
        return Optional.ofNullable(locale);
    }

    public Optional<NumberFormat> getNumberFormat() {
        return Optional.ofNullable(numberFormat);
    }

}
