package org.eclipse.yasson.internal.processor.serializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
public final class MethodSerializer implements ModelSerializer<Object> {

    private final Method method;

    public MethodSerializer(Method method) {
        this.method = method;
    }

    @Override
    public Object serialize(Object value, SerializationContextImpl context) {
        try {
            return method.invoke(context.getInstance());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JsonbException("There has been an error while getting the value with method.", e);
        }
    }
}