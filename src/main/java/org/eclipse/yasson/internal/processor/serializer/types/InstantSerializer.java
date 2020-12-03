package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * TODO javadoc
 */
class InstantSerializer extends AbstractDateSerializer<Instant> {

    InstantSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    protected Instant toInstant(Instant value) {
        return value;
    }

    @Override
    protected TemporalAccessor toTemporalAccessor(Instant value) {
        return value;
    }

    @Override
    protected DateTimeFormatter defaultFormatter(Instant value, Locale locale) {
        return DateTimeFormatter.ISO_INSTANT.withLocale(locale);
    }

    @Override
    protected DateTimeFormatter updateFormatter(DateTimeFormatter formatter) {
        return formatter.withZone(UTC);
    }
}
