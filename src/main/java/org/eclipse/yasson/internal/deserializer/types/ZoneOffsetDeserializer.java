package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;
import java.time.ZoneOffset;

import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class ZoneOffsetDeserializer extends TypeDeserializer {

    ZoneOffsetDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return ZoneOffset.of(value);
    }
}
