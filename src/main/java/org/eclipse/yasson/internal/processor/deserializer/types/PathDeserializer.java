package org.eclipse.yasson.internal.processor.deserializer.types;

import java.lang.reflect.Type;
import java.nio.file.Paths;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class PathDeserializer extends TypeDeserializer {

    PathDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return Paths.get(value);
    }
}
