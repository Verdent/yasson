package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class ContextSwitcher implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<Object> delegate;
    private final ModelDeserializer<JsonParser> modelDeserializer;
    private final Type newType;

    public ContextSwitcher(ModelDeserializer<Object> delegate,
                           ModelDeserializer<JsonParser> modelDeserializer,
                           Type newType) {
        this.delegate = delegate;
        this.modelDeserializer = modelDeserializer;
        this.newType = newType;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        DeserializationContextImpl ctx = new DeserializationContextImpl(context);
        return delegate.deserialize(modelDeserializer.deserialize(value, ctx, newType), context, rType);
    }
}
