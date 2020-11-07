package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class MapDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> keyDelegate;
    private final ModelDeserializer<JsonParser> valueDelegate;

    public MapDeserializer(ModelDeserializer<JsonParser> keyDelegate,
                           ModelDeserializer<JsonParser> valueDelegate) {
        this.keyDelegate = keyDelegate;
        this.valueDelegate = valueDelegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser parser, DeserializationContextImpl context, Type rType) {
        Map<Object, Object> map = (Map<Object, Object>) context.getInstance();
        Type resolved = context.getRtypeChain().size() > 0 ? ReflectionUtils.resolveType(context.getRtypeChain(), rType) : rType;
        context.getRtypeChain().add(resolved);
        Object key = null;
        String keyName = null;
        Mode mode = Mode.NONE;
        State state = State.NEXT;
        while (parser.hasNext()) {
            final JsonParser.Event next = parser.next();
            context.setLastValueEvent(next);
            switch (next) {
            case KEY_NAME:
                mode = mode == Mode.NONE ? Mode.NORMAL : mode;
                keyName = parser.getString();
                break;
            case START_OBJECT:
                mode = mode == Mode.NONE ? Mode.OBJECT : mode;
            case START_ARRAY:
            case VALUE_STRING:
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NUMBER:
                if (mode == Mode.OBJECT) {
                    if (state == State.NEXT) {
                        state = State.KEY;
                    } else if (state == State.KEY) {
                        validateKeyName(keyName, state);
                        key = deserializeValue(parser, context, resolved, 0, keyDelegate);
                        state = State.VALUE;
                    } else if (state == State.VALUE) {
                        validateKeyName(keyName, state);
                        Object value = deserializeValue(parser, context, resolved, 1, valueDelegate);
                        map.put(key, value);
                        state = State.DONE;
                    } else {
                        throw new JsonbException("Only attributes 'key' and 'value' allowed!");
                    }
                } else {
                    Object value = deserializeValue(parser, context, resolved, 1, valueDelegate);
                    map.put(keyName, value);
                }
                break;
            case END_OBJECT:
                state = State.NEXT;
                if (mode == Mode.OBJECT) {
                    break;
                }
            case END_ARRAY:
                context.getRtypeChain().removeLast();
                return map;
            default:
                throw new JsonbException("Unexpected state: " + next);
            }
        }
        context.getRtypeChain().removeLast();
        return map;
    }

    private void validateKeyName(String keyName, State state) {
        if (state == State.KEY && !keyName.equals("key")) {
            throw new JsonbException("Attribute name has to be 'key' when representing map entry key. Got: " + keyName);
        } else if (state == State.VALUE && !keyName.equals("value")) {
            throw new JsonbException("Attribute name has to be 'value' when representing map entry value. Got: " + keyName);
        }
    }

    private Object deserializeValue(JsonParser parser,
                                    DeserializationContextImpl context,
                                    Type rType,
                                    int index,
                                    ModelDeserializer<JsonParser> deserializer) {
        DeserializationContextImpl keyContext = new DeserializationContextImpl(context);
        Type keyType = getRtype(rType, index);
        return deserializer.deserialize(parser, keyContext, keyType);
    }

    private Type getRtype(Type rType, int index) {
        return rType instanceof ParameterizedType
                ? ((ParameterizedType) rType).getActualTypeArguments()[index]
                : Object.class;
    }

    private enum Mode {

        NONE,
        NORMAL,
        OBJECT

    }

    private enum State {

        NEXT,
        VALUE,
        KEY,
        DONE

    }

}