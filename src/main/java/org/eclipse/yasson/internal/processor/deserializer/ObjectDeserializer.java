package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
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
    public Object deserialize(JsonParser parser, DeserializationContextImpl context, Type rType) {
        String key = null;
        context.getRtypeChain().add(rType);
        while (parser.hasNext()) {
            final JsonParser.Event next = parser.next();
            switch (next) {
            case KEY_NAME:
                key = parser.getString();
                break;
            case START_OBJECT:
            case START_ARRAY:
            case VALUE_STRING:
            case VALUE_NUMBER:
            case VALUE_FALSE:
            case VALUE_TRUE:
                propertyDeserializerChains.get(key).deserialize(parser, context, rType);
                break;
            case END_OBJECT:
                context.getRtypeChain().remove(rType);
                return context.getInstance();
            default:
                throw new JsonbException("Unexpected state: " + next);
            }
        }
        context.getRtypeChain().remove(rType);
        return context.getInstance();
    }
}
