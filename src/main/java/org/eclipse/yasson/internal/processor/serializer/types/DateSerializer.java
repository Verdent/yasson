package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

/**
 * TODO javadoc
 */
class DateSerializer extends AbstractDateSerializer<Date> {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME.withZone(UTC);

    DateSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    protected Instant toInstant(Date value) {
        return value.toInstant();
    }

    @Override
    protected TemporalAccessor toTemporalAccessor(Date value) {
        return toInstant(value);
    }

    @Override
    protected DateTimeFormatter defaultFormatter(Date value, Locale locale) {
        return DEFAULT_DATE_FORMATTER.withLocale(locale);
    }

    @Override
    protected DateTimeFormatter updateFormatter(DateTimeFormatter formatter) {
        return formatter.getZone() != null
                ? formatter
                : formatter.withZone(UTC);
    }

}
