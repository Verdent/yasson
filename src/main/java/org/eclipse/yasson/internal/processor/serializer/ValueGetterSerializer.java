package org.eclipse.yasson.internal.processor.serializer;

import java.lang.invoke.MethodHandle;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

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
        Object object;
        try {
            object = valueGetter.invoke(value);
            context.setKey(propertyName);
        } catch (Throwable e) {
            throw new JsonbException("Error getting value on: " + value, e);
        }
        if (!context.addProcessedObject(object)) {
            throw new JsonbException(Messages.getMessage(MessageKeys.RECURSIVE_REFERENCE, object.getClass()));
        }
        delegate.serialize(object, generator, context);
        context.removeProcessedObject(object);
    }
}
