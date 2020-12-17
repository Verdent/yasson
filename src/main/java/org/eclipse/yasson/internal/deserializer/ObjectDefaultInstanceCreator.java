package org.eclipse.yasson.internal.deserializer;

import java.lang.reflect.Constructor;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
public class ObjectDefaultInstanceCreator implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<JsonParser> delegate;
    private final Constructor<?> defaultConstructor;
    private final JsonbException exception;

    public ObjectDefaultInstanceCreator(ModelDeserializer<JsonParser> delegate,
                                        Class<?> clazz,
                                        Constructor<?> defaultConstructor) {
        this.delegate = delegate;
        this.defaultConstructor = defaultConstructor;
        if (clazz.isInterface()) {
            this.exception = new JsonbException(Messages.getMessage(MessageKeys.INFER_TYPE_FOR_UNMARSHALL, clazz.getName()));
        } else if (defaultConstructor == null) {
            this.exception = new JsonbException(Messages.getMessage(MessageKeys.NO_DEFAULT_CONSTRUCTOR, clazz));
        } else {
            this.exception = null;
        }
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        if (exception != null) {
            throw exception;
        }
        Object instance = ReflectionUtils.createNoArgConstructorInstance(defaultConstructor);
        context.setInstance(instance);
        return delegate.deserialize(value, context);
    }
}
