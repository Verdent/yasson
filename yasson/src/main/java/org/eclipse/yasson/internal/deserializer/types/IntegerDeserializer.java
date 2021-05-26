package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class IntegerDeserializer extends AbstractNumberDeserializer<Integer> {

    IntegerDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true);
    }

    @Override
    Integer parseNumberValue(String value) {
        return Integer.parseInt(value);
    }

    @Override
    Object deserializeNumberValue(JsonParser value, DeserializationContextImpl context, Type rType) {
        return value.getInt();
    }
}
