package org.eclipse.yasson.internal.deserializer.types;

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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class XmlGregorianCalendar extends AbstractDateDeserializer<XMLGregorianCalendar> {

    private static final LocalTime ZERO_LOCAL_TIME = LocalTime.parse("00:00:00");

    private final Calendar calendarTemplate;
    private final DatatypeFactory datatypeFactory;

    XmlGregorianCalendar(TypeDeserializerBuilder builder) {
        super(builder);
        this.calendarTemplate = new GregorianCalendar();
        this.calendarTemplate.clear();
        this.calendarTemplate.setTimeZone(TimeZone.getTimeZone(UTC));
        try {
            this.datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new JsonbException(Messages.getMessage(MessageKeys.DATATYPE_FACTORY_CREATION_FAILED), e);
        }
    }

    @Override
    protected XMLGregorianCalendar fromInstant(Instant instant) {
        final GregorianCalendar calendar = (GregorianCalendar) calendarTemplate.clone();
        calendar.setTimeInMillis(instant.toEpochMilli());
        return datatypeFactory.newXMLGregorianCalendar(calendar);
    }

    @Override
    protected XMLGregorianCalendar parseDefault(String jsonValue, Locale locale) {
        DateTimeFormatter formatter = jsonValue.contains("T")
                ? DateTimeFormatter.ISO_DATE_TIME
                : DateTimeFormatter.ISO_DATE;
        return parseWithFormatter(jsonValue, formatter.withLocale(locale));
    }

    @Override
    protected XMLGregorianCalendar parseWithFormatter(String jsonValue, DateTimeFormatter formatter) {
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
        return datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(result));
    }
}
