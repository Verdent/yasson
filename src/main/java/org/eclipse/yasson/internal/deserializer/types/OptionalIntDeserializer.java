package org.eclipse.yasson.internal.deserializer.types;

import java.util.OptionalInt;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
class OptionalIntDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> extractor;
    private final ModelDeserializer<Object> delegate;

    OptionalIntDeserializer(ModelDeserializer<JsonParser> extractor, ModelDeserializer<Object> delegate) {
        this.extractor = extractor;
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        if (context.getLastValueEvent() == JsonParser.Event.VALUE_NULL) {
            return delegate.deserialize(OptionalInt.empty(), context);
        }
        OptionalInt optional = OptionalInt.of((Integer) extractor.deserialize(value, context));
        return delegate.deserialize(optional, context);
    }
}
