package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class EnumDeserializer extends TypeDeserializer {

    EnumDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    Object deserializeStringValue(String value, DeserializationContextImpl context, Type rType) {
        return Enum.valueOf((Class<Enum>) rType, value);
    }
}
