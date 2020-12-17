package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;
import java.time.Period;

import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class PeriodDeserializer extends TypeDeserializer {

    PeriodDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return Period.parse(value);
    }
}
