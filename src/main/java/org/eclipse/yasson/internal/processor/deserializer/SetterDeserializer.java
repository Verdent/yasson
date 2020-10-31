package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class SetterDeserializer implements ModelDeserializer<Object>{

    private final Method method;

    public SetterDeserializer(Method method) {
        this.method = method;
    }

    @Override
    public Object deserialize(Object value, DeserializationContextImpl context, Type rType) {
        try {
            method.invoke(context.getInstance(), value);
            return value;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JsonbException("There has been an error while setting the setter method.", e);
        }
    }

}
