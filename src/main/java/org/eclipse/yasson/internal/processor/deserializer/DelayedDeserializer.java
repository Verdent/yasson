package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class DelayedDeserializer implements ModelDeserializer<Object> {

    private final ModelDeserializer<Object> delegate;

    public DelayedDeserializer(ModelDeserializer<Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(Object value, DeserializationContextImpl context, Type rType) {
        context.getDelayedSetters().add(() -> delegate.deserialize(value, context, rType));
        return value;
    }

}
