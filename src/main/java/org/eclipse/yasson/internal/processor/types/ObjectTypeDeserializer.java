package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
class ObjectTypeDeserializer implements ModelDeserializer<JsonParser> {

    private static final Type LIST = List.class;
    private static final Type MAP = new HashMap<String, Object>(){}.getClass().getGenericSuperclass();

    private final ModelDeserializer<Object> delegate;

    public ObjectTypeDeserializer(TypeDeserializerBuilder builder) {
        this.delegate = builder.getDelegate();
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        Object toSet;
        switch (context.getLastValueEvent()) {
        case VALUE_TRUE:
            toSet = Boolean.TRUE;
            break;
        case VALUE_FALSE:
            toSet = Boolean.FALSE;
            break;
        case VALUE_STRING:
            toSet = value.getString();
            break;
        case START_OBJECT:
            toSet = context.deserialize(MAP, value);
            break;
        case START_ARRAY:
            toSet = context.deserialize(LIST, value);
            break;
        default:
            throw new JsonbException("Unexpected event: " + context.getLastValueEvent());
        }
        return delegate.deserialize(toSet, context, rType);
    }

}
