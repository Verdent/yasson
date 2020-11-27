package org.eclipse.yasson.internal.processor.deserializer.types;

import java.lang.reflect.Type;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class CharDeserializer extends TypeDeserializer {

    CharDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return value.charAt(0);
    }
}
