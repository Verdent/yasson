package org.eclipse.yasson.internal.serializer.types;

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
    protected String formatDefault(Calendar value, Locale locale) {
        DateTimeFormatter formatter = value.isSet(Calendar.HOUR) || value.isSet(Calendar.HOUR_OF_DAY)
                ? DateTimeFormatter.ISO_DATE_TIME
                : DateTimeFormatter.ISO_DATE;
        return formatter.withZone(value.getTimeZone().toZoneId())
                .withLocale(locale).format(toTemporalAccessor(value));
    }

    @Override
    protected TemporalAccessor toTemporalAccessor(Calendar object) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(object.getTimeInMillis()),
                                       object.getTimeZone().toZoneId());
    }

}
