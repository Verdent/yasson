package org.eclipse.yasson.internal.deserializer;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.deserializer.types.TypeDeserializer;

/**
 * TODO javadoc
 */
public class ValueExtractor implements ModelDeserializer<JsonParser> {

    private final TypeDeserializer delegate;

    public ValueExtractor(TypeDeserializer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        JsonParser.Event last = context.getLastValueEvent();
        switch (last) {
        case VALUE_TRUE:
            return delegate.deserialize(Boolean.TRUE, context);
        case VALUE_FALSE:
            return delegate.deserialize(Boolean.FALSE, context);
        case KEY_NAME:
        case VALUE_STRING:
            return delegate.deserialize(value.getString(), context);
        case VALUE_NUMBER:
            //We dont know for sure how to handle the number value, it can be int, long etc.
            //Value extracting has to be delegated to the TypeDeserializer
            return delegate.deserialize(value, context);
        default:
            throw new JsonbException("Could not extract data. Received event: " + last);
        }
    }
}
