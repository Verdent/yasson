package org.eclipse.yasson.internal.processor.deserializer;

import java.util.Map;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class ObjectDeserializer implements ModelDeserializer<JsonParser> {

    private final Map<String, ModelDeserializer<JsonParser>> propertyDeserializerChains;

    public ObjectDeserializer(Map<String, ModelDeserializer<JsonParser>> propertyDeserializerChains) {
        this.propertyDeserializerChains = propertyDeserializerChains;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
        String key = null;
        while (parser.hasNext()) {
            final JsonParser.Event next = parser.next();
            context.setLastValueEvent(next);
            switch (next) {
            case KEY_NAME:
                key = parser.getString();
                break;
            case VALUE_NULL:
            case START_OBJECT:
            case START_ARRAY:
            case VALUE_STRING:
            case VALUE_NUMBER:
            case VALUE_FALSE:
            case VALUE_TRUE:
                if (propertyDeserializerChains.containsKey(key)) {
                    propertyDeserializerChains.get(key).deserialize(parser, context);
                }
                break;
            case END_ARRAY:
                continue;
            case END_OBJECT:
                return context.getInstance();
            default:
                throw new JsonbException("Unexpected state: " + next);
            }
        }
        return context.getInstance();
    }
}
