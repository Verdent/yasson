package org.eclipse.yasson.internal.processor.deserializer.types;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * TODO javadoc
 */
class CalendarDeserializer extends AbstractDateDeserializer<Calendar> {

    private static final LocalTime ZERO_LOCAL_TIME = LocalTime.parse("00:00:00");

    private final Calendar calendarTemplate;

    CalendarDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
        this.calendarTemplate = new GregorianCalendar();
        this.calendarTemplate.clear();
        this.calendarTemplate.setTimeZone(TimeZone.getTimeZone(UTC));
    }

    @Override
    Calendar fromInstant(Instant instant) {
        final Calendar calendar = (Calendar) calendarTemplate.clone();
        calendar.setTimeInMillis(instant.toEpochMilli());
        return calendar;
    }

    @Override
    Calendar parseDefault(String jsonValue, Locale locale) {
        DateTimeFormatter formatter = jsonValue.contains("T")
                ? DateTimeFormatter.ISO_DATE_TIME
                : DateTimeFormatter.ISO_DATE;
        return parseWithFormatter(jsonValue, formatter.withLocale(locale));
    }

    @Override
    Calendar parseWithFormatter(String jsonValue, DateTimeFormatter formatter) {
        final TemporalAccessor parsed = formatter.parse(jsonValue);
        LocalTime time = parsed.query(TemporalQueries.localTime());
        ZoneId zone = parsed.query(TemporalQueries.zone());
        if (zone == null) {
            zone = UTC;
        }
        if (time == null) {
            time = ZERO_LOCAL_TIME;
        }
        ZonedDateTime result = LocalDate.from(parsed).atTime(time).atZone(zone);
        return GregorianCalendar.from(result);
    }
}
