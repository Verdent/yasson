package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class LongDeserializer extends AbstractNumberDeserializer<Long> {

    LongDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true);
    }

    @Override
    Long parseNumberValue(String value) {
        return Long.parseLong(value);
    }

    @Override
    Object deserializeNumberValue(JsonParser value, DeserializationContextImpl context, Type rType) {
        return value.getLong();
    }
}
