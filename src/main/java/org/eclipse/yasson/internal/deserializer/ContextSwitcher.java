package org.eclipse.yasson.internal.deserializer;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class ContextSwitcher implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<Object> delegate;
    private final ModelDeserializer<JsonParser> modelDeserializer;

    public ContextSwitcher(ModelDeserializer<Object> delegate,
                           ModelDeserializer<JsonParser> modelDeserializer) {
        this.delegate = delegate;
        this.modelDeserializer = modelDeserializer;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        DeserializationContextImpl ctx = new DeserializationContextImpl(context);
        return delegate.deserialize(modelDeserializer.deserialize(value, ctx), context);
    }
}
