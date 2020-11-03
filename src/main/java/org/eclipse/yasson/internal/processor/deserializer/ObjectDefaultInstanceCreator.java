package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Objects;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
public class ObjectDefaultInstanceCreator implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> delegate;
    private final Class<?> clazz;
    private final Constructor<?> defaultConstructor;

    public ObjectDefaultInstanceCreator(ModelDeserializer<JsonParser> delegate,
                                        Class<?> clazz,
                                        Constructor<?> defaultConstructor) {
        this.delegate = delegate;
        this.clazz = clazz;
        this.defaultConstructor = Objects.requireNonNull(defaultConstructor,
                                                         () -> Messages.getMessage(MessageKeys.NO_DEFAULT_CONSTRUCTOR, clazz));
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        Object instance = ReflectionUtils.createNoArgConstructorInstance(defaultConstructor);
        context.setInstance(instance);
        return delegate.deserialize(value, context, rType);
    }
}
