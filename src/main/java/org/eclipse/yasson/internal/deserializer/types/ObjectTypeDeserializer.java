package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
class ObjectTypeDeserializer implements ModelDeserializer<JsonParser> {

    private static final Type LIST = List.class;

    private final ModelDeserializer<Object> delegate;
    private final Class<?> mapClass;

    public ObjectTypeDeserializer(TypeDeserializerBuilder builder) {
        this.delegate = builder.getDelegate();
        this.mapClass = builder.getConfigProperties().getDefaultMapImplType();
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        Object toSet;
        switch (context.getLastValueEvent()) {
        case VALUE_TRUE:
            toSet = Boolean.TRUE;
            break;
        case VALUE_FALSE:
            toSet = Boolean.FALSE;
            break;
        case VALUE_NUMBER:
            toSet = new BigDecimal(value.getString());
            break;
        case KEY_NAME:
        case VALUE_STRING:
            toSet = value.getString();
            break;
        case START_OBJECT:
            DeserializationContextImpl newContext = new DeserializationContextImpl(context);
            toSet = newContext.deserialize(mapClass, value);
            break;
        case START_ARRAY:
            DeserializationContextImpl newContext1 = new DeserializationContextImpl(context);
            toSet = newContext1.deserialize(LIST, value);
            break;
        default:
            throw new JsonbException("Unexpected event: " + context.getLastValueEvent());
        }
        return delegate.deserialize(toSet, context);
    }

}
