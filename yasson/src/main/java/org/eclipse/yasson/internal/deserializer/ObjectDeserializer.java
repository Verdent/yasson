package org.eclipse.yasson.internal.deserializer;

import java.util.Map;
import java.util.function.Function;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
public class ObjectDeserializer implements ModelDeserializer<JsonParser> {

    private final Map<String, ModelDeserializer<JsonParser>> propertyDeserializerChains;
    private final Function<String, String> renamer;
    private final Class<?> rawClass;

    public ObjectDeserializer(Map<String, ModelDeserializer<JsonParser>> propertyDeserializerChains,
                              Function<String, String> renamer, Class<?> rawClass) {
        this.propertyDeserializerChains = propertyDeserializerChains;
        this.renamer = renamer;
        this.rawClass = rawClass;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
        String key = null;
        while (parser.hasNext()) {
            final JsonParser.Event next = parser.next();
            context.setLastValueEvent(next);
            switch (next) {
            case KEY_NAME:
                key = renamer.apply(parser.getString());
                break;
            case VALUE_NULL:
            case START_OBJECT:
            case START_ARRAY:
            case VALUE_STRING:
            case VALUE_NUMBER:
            case VALUE_FALSE:
            case VALUE_TRUE:
                if (propertyDeserializerChains.containsKey(key)) {
                    try {
                        propertyDeserializerChains.get(key).deserialize(parser, context);
                    } catch (JsonbException e) {
                        throw new JsonbException("Unable to deserialize property '" + key
                                                         + "' because of: " + e.getMessage(), e);
                    }
                } else if (context.getJsonbContext().getConfigProperties().getConfigFailOnUnknownProperties()) {
                    throw new JsonbException(Messages.getMessage(MessageKeys.UNKNOWN_JSON_PROPERTY,
                                                                 key,
                                                                 rawClass));
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
