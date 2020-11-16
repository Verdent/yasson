package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.time.ZoneId;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class ZoneIdDeserializer extends TypeDeserializer {

    ZoneIdDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return ZoneId.of(value);
    }
}
