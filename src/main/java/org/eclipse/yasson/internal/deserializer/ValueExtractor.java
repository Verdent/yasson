package org.eclipse.yasson.internal.deserializer;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class ValueExtractor implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<String> delegate;

    public ValueExtractor(ModelDeserializer<String> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        String valueToPropagate;
        JsonParser.Event last = context.getLastValueEvent();
        switch (last) {
        case VALUE_TRUE:
            valueToPropagate = "true";
            break;
        case VALUE_FALSE:
            valueToPropagate = "false";
            break;
        default:
            valueToPropagate = value.getString();
        }
        return delegate.deserialize(valueToPropagate, context);
    }
}
