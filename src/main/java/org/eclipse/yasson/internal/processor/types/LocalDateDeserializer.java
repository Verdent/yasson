package org.eclipse.yasson.internal.processor.types;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * TODO javadoc
 */
class LocalDateDeserializer extends AbstractDateDeserializer<LocalDate> {

    LocalDateDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    protected LocalDate fromInstant(Instant instant) {
        return instant.atZone(UTC).toLocalDate();
    }

    @Override
    protected LocalDate parseDefault(String jsonValue, Locale locale) {
        return LocalDate.parse(jsonValue, DateTimeFormatter.ISO_LOCAL_DATE.withLocale(locale));
    }

    @Override
    protected LocalDate parseWithFormatter(String jsonValue, DateTimeFormatter formatter) {
        return LocalDate.parse(jsonValue, formatter);
    }
}
