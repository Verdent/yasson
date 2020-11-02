package org.eclipse.yasson.internal.processor.types;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.serializer.DeserializerBuilder;

import static java.time.ZoneOffset.UTC;

/**
 * TODO javadoc
 */
class DateDeserializer extends AbstractDateDeserializer<Date> {

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    DateDeserializer(TypeDeserializerBuilder builder) {
        super(builder, Date.class);
    }

    @Override
    Date fromInstant(Instant instant) {
        return new Date(instant.toEpochMilli());
    }

    @Override
    Date parseDefault(String jsonValue, Locale locale) {
        return parseWithOrWithoutZone(jsonValue, DEFAULT_DATE_TIME_FORMATTER.withLocale(locale));
    }

    @Override
    Date parseWithFormatter(String jsonValue, DateTimeFormatter formatter) {
        return parseWithOrWithoutZone(jsonValue, formatter);
    }

    private static Date parseWithOrWithoutZone(String jsonValue, DateTimeFormatter formatter) {
        TemporalAccessor parsed;
        try {
            // Try parsing with a Zone
            parsed = ZonedDateTime.parse(jsonValue, formatter);
        } catch (DateTimeParseException e) {
            // Possibly exception occures because no Offset/ZoneId was found
            // Therefore parse with defaultZone again
            parsed = ZonedDateTime.parse(jsonValue, formatter.withZone(UTC));
        }
        return new Date(Instant.from(parsed).toEpochMilli());
    }

}
