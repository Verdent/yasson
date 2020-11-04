package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class ValueSetterDeserializer implements ModelDeserializer<Object>{

    private final MethodHandle valueSetter;

    public ValueSetterDeserializer(MethodHandle valueSetter) {
        this.valueSetter = valueSetter;
    }

    @Override
    public Object deserialize(Object value, DeserializationContextImpl context, Type rType) {
        Object object = context.getInstance();
        try {
            valueSetter.invoke(object, value);
            return value;
        } catch (Throwable e) {
            throw new JsonbException("Error setting value on: " + object, e);
        }
    }

}
