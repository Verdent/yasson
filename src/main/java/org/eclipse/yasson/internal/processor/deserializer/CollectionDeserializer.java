package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class CollectionDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> delegate;

    public CollectionDeserializer(ModelDeserializer<JsonParser> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContextImpl context, Type rType) {
        Collection<Object> collection = (Collection<Object>) context.getInstance();
        context.getRtypeChain().add(rType);
        while (parser.hasNext()) {
            final JsonParser.Event next = parser.next();
            context.setLastValueEvent(next);
            switch (next) {
            case START_OBJECT:
            case START_ARRAY:
            case VALUE_STRING:
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NUMBER:
                DeserializationContextImpl newContext = new DeserializationContextImpl(context);
                Type colType = rType instanceof ParameterizedType
                        ? ((ParameterizedType) rType).getActualTypeArguments()[0]
                        : Object.class;
                collection.add(delegate.deserialize(parser, newContext, colType));
                break;
            case END_ARRAY:
                return collection;
            default:
                throw new JsonbException("Unexpected state: " + next);
            }
        }
        return collection;
    }

}
