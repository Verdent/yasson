package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.eclipse.yasson.internal.serializer.JsonbDateFormatter;

/**
 * TODO javadoc
 */
class LocalDateTimeSerializer extends AbstractDateSerializer<LocalDateTime> {

    LocalDateTimeSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    protected Instant toInstant(LocalDateTime value) {
        return value.atZone(UTC).toInstant();
    }

    @Override
    protected String formatDefault(LocalDateTime value, Locale locale) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(locale).format(value);
    }

    @Override
    protected String formatWithFormatter(LocalDateTime value, DateTimeFormatter formatter) {
        return getZonedFormatter(formatter).format(value);
    }

    @Override
    protected String formatStrictIJson(LocalDateTime value) {
        final ZonedDateTime zonedDateTime = value.atZone(UTC);
        return JsonbDateFormatter.IJSON_DATE_FORMATTER.format(zonedDateTime);
    }

}
