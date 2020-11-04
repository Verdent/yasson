package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class NullCheckDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> nonNullDeserializer;
    private final ModelDeserializer<Object> nullDeserializer;
    private final Class<?> propertyClass;

    public NullCheckDeserializer(ModelDeserializer<JsonParser> nonNullDeserializer,
                                 ModelDeserializer<Object> nullDeserializer,
                                 Class<?> propertyClass) {
        this.nonNullDeserializer = nonNullDeserializer;
        this.nullDeserializer = nullDeserializer;
        this.propertyClass = propertyClass;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        if (context.getLastValueEvent() != JsonParser.Event.VALUE_NULL) {
            return nonNullDeserializer.deserialize(value, context, rType);
        } else if (propertyClass.isPrimitive()) {
            return null;
        }
        return nullDeserializer.deserialize(null, context, rType);
    }
}
