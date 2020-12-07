package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

import org.eclipse.yasson.internal.serializer.JsonbDateFormatter;

/**
 * TODO javadoc
 */
class DateSerializer<T extends Date> extends AbstractDateSerializer<T> {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME.withZone(UTC);

    DateSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    protected Instant toInstant(Date value) {
        return value.toInstant();
    }

    @Override
    protected String formatDefault(Date value, Locale locale) {
        return DEFAULT_DATE_FORMATTER.withLocale(locale).format(toInstant(value));
    }

    @Override
    protected String formatWithFormatter(Date value, DateTimeFormatter formatter) {
        return getZonedFormatter(formatter).format(toTemporalAccessor(value));
    }

    @Override
    protected String formatStrictIJson(Date value) {
        return JsonbDateFormatter.IJSON_DATE_FORMATTER.withZone(UTC).format(toTemporalAccessor(value));
    }

    @Override
    protected TemporalAccessor toTemporalAccessor(Date object) {
        return toInstant(object);
    }

}
