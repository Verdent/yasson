package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class NumberDeserializer extends TypeDeserializer {

    NumberDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return new BigDecimal(value);
    }

}
