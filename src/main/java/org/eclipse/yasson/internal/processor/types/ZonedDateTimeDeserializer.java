package org.eclipse.yasson.internal.processor.types;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;
import org.eclipse.yasson.internal.serializer.ZonedDateTimeTypeDeserializer;

/**
 * TODO javadoc
 */
class ZonedDateTimeDeserializer extends AbstractDateDeserializer<ZonedDateTime> {

    private static final Logger LOGGER = Logger.getLogger(ZonedDateTimeTypeDeserializer.class.getName());

    ZonedDateTimeDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    /**
     * fromInstant is called only in case {@link jakarta.json.bind.annotation.JsonbDateFormat} is TIME_IN_MILLIS,
     * which doesn't make much sense for usage with ZonedDateTime.
     */
    @Override
    protected ZonedDateTime fromInstant(Instant instant) {
        LOGGER.warning(Messages.getMessage(MessageKeys.OFFSET_DATE_TIME_FROM_MILLIS, ZonedDateTime.class.getSimpleName(), UTC));
        return ZonedDateTime.ofInstant(instant, UTC);
    }

    @Override
    protected ZonedDateTime parseDefault(String jsonValue, Locale locale) {
        return ZonedDateTime.parse(jsonValue, DateTimeFormatter.ISO_ZONED_DATE_TIME.withLocale(locale));
    }

    @Override
    protected ZonedDateTime parseWithFormatter(String jsonValue, DateTimeFormatter formatter) {
        return ZonedDateTime.parse(jsonValue, getZonedFormatter(formatter));
    }
}
