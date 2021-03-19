package org.eclipse.yasson.internal.serializer;

import java.lang.invoke.MethodHandle;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * TODO javadoc
 */
class ValueGetterSerializer  implements ModelSerializer {

    private final MethodHandle valueGetter;
    private final ModelSerializer delegate;

    ValueGetterSerializer(MethodHandle valueGetter, ModelSerializer delegate) {
        this.valueGetter = valueGetter;
        this.delegate = delegate;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        Object object;
        try {
            object = valueGetter.invoke(value);
        } catch (Throwable e) {
            throw new JsonbException("Error getting value on: " + value, e);
        }
        delegate.serialize(object, generator, context);
    }
}
