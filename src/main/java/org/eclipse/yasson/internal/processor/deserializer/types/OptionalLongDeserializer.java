package org.eclipse.yasson.internal.processor.deserializer.types;

import java.util.OptionalLong;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
class OptionalLongDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> extractor;
    private final ModelDeserializer<Object> nullValueDelegate;

    OptionalLongDeserializer(ModelDeserializer<JsonParser> extractor, ModelDeserializer<Object> nullValueDelegate) {
        this.extractor = extractor;
        this.nullValueDelegate = nullValueDelegate;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        if (context.getLastValueEvent() == JsonParser.Event.VALUE_NULL) {
            return nullValueDelegate.deserialize(OptionalLong.empty(), context);
        }
        OptionalLong optional = OptionalLong.of((Long) extractor.deserialize(value, context));
        return nullValueDelegate.deserialize(optional, context);
    }
}
