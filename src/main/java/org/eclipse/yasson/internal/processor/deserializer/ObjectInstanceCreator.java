package org.eclipse.yasson.internal.processor.deserializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.model.CreatorModel;
import org.eclipse.yasson.internal.model.JsonbCreator;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
public class ObjectInstanceCreator implements ModelDeserializer<JsonParser> {

    private final Map<String, ModelDeserializer<JsonParser>> propertyDeserializerChains;
    private final List<String> creatorParams;
    private final JsonbCreator creator;
    private final Class<?> clazz;

    public ObjectInstanceCreator(Map<String, ModelDeserializer<JsonParser>> propertyDeserializerChains,
                                 JsonbCreator creator,
                                 Class<?> clazz) {
        this.propertyDeserializerChains = propertyDeserializerChains;
        this.creatorParams = Arrays.stream(creator.getParams()).map(CreatorModel::getName).collect(Collectors.toList());
        this.creator = creator;
        this.clazz = clazz;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
        String key = null;
        Map<String, Object> paramValues = new HashMap<>();
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
                    Object o = propertyDeserializerChains.get(key).deserialize(parser, context);
                    if (creatorParams.contains(key)) {
                        paramValues.put(key, o);
                    }
                }
                break;
            case END_OBJECT:
                Object[] params = new Object[creatorParams.size()];
                for (int i = 0; i < creatorParams.size(); i++) {
                    String param = creatorParams.get(i);
                    if (paramValues.containsKey(param)) {
                        params[i] = paramValues.get(param);
                    } else {
                        throw new JsonbException(Messages.getMessage(MessageKeys.JSONB_CREATOR_MISSING_PROPERTY, param));
                    }
                }
                context.setInstance(creator.call(params, clazz));
                context.getDelayedSetters().forEach(Runnable::run);
                context.getDelayedSetters().clear();
                return context.getInstance();
            default:
                throw new JsonbException("Unexpected state: " + next);
            }
        }
        return context.getInstance();
    }

    @Override
    public String toString() {
        return "ObjectInstanceCreator{" +
                "parameters=" + creatorParams +
                ", clazz=" + clazz +
                '}';
    }
}
