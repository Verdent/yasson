package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;
import java.net.URI;

import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class UriDeserializer extends TypeDeserializer {

    UriDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return URI.create(value);
    }
}
