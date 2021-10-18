package org.eclipse.yasson.internal.deserializer;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class ValueSetterDeserializer implements ModelDeserializer<Object>{

    private final MethodHandle valueSetter;

    public ValueSetterDeserializer(MethodHandle valueSetter) {
        this.valueSetter = Objects.requireNonNull(valueSetter);
    }

    @Override
    public Object deserialize(Object value, DeserializationContextImpl context) {
        Object object = context.getInstance();
        try {
            valueSetter.invoke(object, value);
            return value;
        } catch (Throwable e) {
            throw new JsonbException("Error setting value on: " + object, e);
        }
    }

}
