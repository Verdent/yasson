package org.eclipse.yasson.internal.deserializer;

import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class DelayedDeserializer implements ModelDeserializer<Object> {

    private final ModelDeserializer<Object> delegate;

    public DelayedDeserializer(ModelDeserializer<Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(Object value, DeserializationContextImpl context) {
        context.getDelayedSetters().add(() -> delegate.deserialize(value, context));
        return value;
    }

}
