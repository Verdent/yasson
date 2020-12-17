package org.eclipse.yasson.internal.deserializer;

import java.util.Optional;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class OptionalDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> typeDeserializer;
    private final ModelDeserializer<Object> delegate;

    public OptionalDeserializer(ModelDeserializer<JsonParser> typeDeserializer,
                                ModelDeserializer<Object> delegate) {
        this.typeDeserializer = typeDeserializer;
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        Optional<Object> val = Optional.ofNullable(typeDeserializer.deserialize(value, context));
        return delegate.deserialize(val, context);
    }
}
