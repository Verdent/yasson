package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Locale;

/**
 * Serializer for {@link Calendar} type.
 */
class CalendarSerializer extends AbstractDateSerializer<Calendar> {

    CalendarSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    protected Instant toInstant(Calendar value) {
        return value.toInstant();
    }

    @Override
    protected TemporalAccessor toTemporalAccessor(Calendar value) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(value.getTimeInMillis()),
                                       value.getTimeZone().toZoneId());
    }

    @Override
    protected DateTimeFormatter defaultFormatter(Calendar value, Locale locale) {
        DateTimeFormatter formatter = value.isSet(Calendar.HOUR) || value.isSet(Calendar.HOUR_OF_DAY)
                ? DateTimeFormatter.ISO_DATE_TIME
                : DateTimeFormatter.ISO_DATE;
        return formatter.withZone(value.getTimeZone().toZoneId())
                .withLocale(locale);
    }

}
