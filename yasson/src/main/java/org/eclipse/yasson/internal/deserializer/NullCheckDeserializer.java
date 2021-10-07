package org.eclipse.yasson.internal.deserializer;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class NullCheckDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> nonNullDeserializer;
    private final ModelDeserializer<Object> nullDeserializer;

    public NullCheckDeserializer(ModelDeserializer<JsonParser> nonNullDeserializer,
                                 ModelDeserializer<Object> nullDeserializer) {
        this.nonNullDeserializer = nonNullDeserializer;
        this.nullDeserializer = nullDeserializer;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        if (context.getLastValueEvent() != JsonParser.Event.VALUE_NULL) {
            return nonNullDeserializer.deserialize(value, context);
        }
        return nullDeserializer.deserialize(null, context);
    }
}
