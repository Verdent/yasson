package org.eclipse.yasson.customization;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * TODO javadoc
 */
public class DateFormatCustomization {

    private final String format;
    private final Locale locale;
    private final DateTimeFormatter dateFormatter;

    DateFormatCustomization(String format, Locale locale, DateTimeFormatter dateFormatter) {
        this.format = format;
        this.locale = locale;
        this.dateFormatter = dateFormatter;
    }

    public Optional<String> getFormat() {
        return Optional.ofNullable(format);
    }

    public Optional<Locale> getLocale() {
        return Optional.ofNullable(locale);
    }

    public Optional<DateTimeFormatter> getDateFormatter() {
        return Optional.ofNullable(dateFormatter);
    }

}
