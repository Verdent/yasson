package org.eclipse.yasson.internal.processor.deserializer.types;

import java.lang.reflect.Type;
import java.util.UUID;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class UuidDeserializer extends TypeDeserializer {

    UuidDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return UUID.fromString(value);
    }
}
