package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class ValueExtractor implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<String> delegate;

    public ValueExtractor(ModelDeserializer<String> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        return delegate.deserialize(value.getString(), context, rType);
    }
}
