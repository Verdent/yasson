package org.eclipse.yasson.internal.serializer;

import java.util.Optional;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * TODO javadoc
 */
class OptionalSerializer implements ModelSerializer {

    private final ModelSerializer delegate;

    OptionalSerializer(ModelSerializer delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        Optional<Object> optional = (Optional<Object>) value;
        delegate.serialize(optional.orElse(null), generator, context);
    }

}
