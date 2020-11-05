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
        while (parser.hasNext()) {
            final JsonParser.Event next = parser.next();
            switch (next) {
            case START_OBJECT:
            case START_ARRAY:
            case VALUE_STRING:
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NUMBER:
                DeserializationContextImpl newContext = new DeserializationContextImpl(context);
                collection.add(delegate.deserialize(parser, newContext, ((ParameterizedType)rType).getActualTypeArguments()[0]));
                break;
            case END_ARRAY:
                return context.getInstance();
            default:
                throw new JsonbException("Unexpected state: " + next);
            }
        }
        return context.getInstance();
    }

}
