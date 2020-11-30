package org.eclipse.yasson.internal.processor.serializer;

import java.lang.invoke.MethodHandle;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class ValueGetterSerializer  implements ModelSerializer {

    private final String propertyName;
    private final MethodHandle valueGetter;
    private final ModelSerializer delegate;

    ValueGetterSerializer(String propertyName, MethodHandle valueGetter, ModelSerializer delegate) {
        this.propertyName = propertyName;
        this.valueGetter = valueGetter;
        this.delegate = delegate;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        try {
            Object object = valueGetter.invoke(value);
            if (object != null) {
                generator.writeKey(propertyName);
            }
            context.setKey(propertyName);
            delegate.serialize(object, generator, context);
        } catch (Throwable e) {
            throw new JsonbException("Error getting value on: " + value, e);
        }
    }
}
