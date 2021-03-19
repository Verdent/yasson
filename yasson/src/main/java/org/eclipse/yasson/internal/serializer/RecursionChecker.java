package org.eclipse.yasson.internal.serializer;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class RecursionChecker implements ModelSerializer {

    private final ModelSerializer delegate;

    public RecursionChecker(ModelSerializer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        if (!context.addProcessedObject(value)) {
            throw new JsonbException(Messages.getMessage(MessageKeys.RECURSIVE_REFERENCE, value.getClass()));
        }
        delegate.serialize(value, generator, context);
        context.removeProcessedObject(value);
    }

}
