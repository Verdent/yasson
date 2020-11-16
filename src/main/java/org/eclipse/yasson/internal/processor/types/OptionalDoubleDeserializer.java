package org.eclipse.yasson.internal.processor.types;

import java.util.OptionalDouble;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
class OptionalDoubleDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> extractor;
    private final ModelDeserializer<Object> nullValueDelegate;

    OptionalDoubleDeserializer(ModelDeserializer<JsonParser> extractor, ModelDeserializer<Object> nullValueDelegate) {
        this.extractor = extractor;
        this.nullValueDelegate = nullValueDelegate;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        if (context.getLastValueEvent() == JsonParser.Event.VALUE_NULL) {
            return nullValueDelegate.deserialize(OptionalDouble.empty(), context);
        }
        return OptionalDouble.of((Double) extractor.deserialize(value, context));
    }
}
