package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class ObjectInstanceDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> delegate;
    private final Class<?> clazz;

    public ObjectInstanceDeserializer(ModelDeserializer<JsonParser> delegate,
                                      Class<?> clazz) {
        this.delegate = delegate;
        this.clazz = clazz;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            context.setInstance(instance);
            return delegate.deserialize(value, context, rType);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonbException("Cannot create new instance of " + clazz, e);
        }
    }
}
