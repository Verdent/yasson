package org.eclipse.yasson.internal.processor.deserializer.types;

import java.lang.reflect.Type;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class BooleanDeserializer extends TypeDeserializer {

    BooleanDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    public Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        switch (context.getLastValueEvent()) {
        case VALUE_FALSE:
            return Boolean.FALSE;
        case VALUE_TRUE:
            return Boolean.TRUE;
        default:
            return Boolean.parseBoolean(value);
        }
    }

}
