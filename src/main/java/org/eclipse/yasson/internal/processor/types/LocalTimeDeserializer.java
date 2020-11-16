package org.eclipse.yasson.internal.processor.types;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class LocalTimeDeserializer extends AbstractDateDeserializer<LocalTime> {

    LocalTimeDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    protected LocalTime fromInstant(Instant instant) {
        throw new JsonbException(Messages.getMessage(MessageKeys.TIME_TO_EPOCH_MILLIS_ERROR, LocalTime.class.getSimpleName()));
    }

    @Override
    protected LocalTime parseDefault(String jsonValue, Locale locale) {
        return LocalTime.parse(jsonValue, DateTimeFormatter.ISO_LOCAL_TIME.withLocale(locale));
    }

    @Override
    protected LocalTime parseWithFormatter(String jsonValue, DateTimeFormatter formatter) {
        return LocalTime.parse(jsonValue, formatter);
    }
}
