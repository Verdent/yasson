package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;

import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class BooleanDeserializer extends TypeDeserializer {

    BooleanDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    public Object deserializeStringValue(String value, DeserializationContextImpl context, Type rType) {
        return Boolean.parseBoolean(value);
    }

    @Override
    Object deserializeBooleanValue(boolean value, DeserializationContextImpl context, Type rType) {
        return value;
    }
}
