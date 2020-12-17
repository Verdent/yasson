package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;
import java.time.Duration;

import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class DurationDeserializer extends TypeDeserializer {

    DurationDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    public Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return Duration.parse(value);
    }

}
