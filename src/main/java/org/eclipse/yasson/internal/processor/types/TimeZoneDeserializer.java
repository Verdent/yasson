package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;
import java.util.SimpleTimeZone;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class TimeZoneDeserializer extends TypeDeserializer {

    TimeZoneDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        try {
            final ZoneId zoneId = ZoneId.of(value);
            final ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(zoneId);
            return new SimpleTimeZone(zonedDateTime.getOffset().getTotalSeconds() * 1000, zoneId.getId());
        } catch (ZoneRulesException e) {
            throw new JsonbException(Messages.getMessage(MessageKeys.ZONE_PARSE_ERROR, value), e);
        }
    }
}
