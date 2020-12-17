package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;

import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
abstract class TypeDeserializer implements ModelDeserializer<String> {

    private final ModelDeserializer<Object> delegate;
    private final Class<?> clazz;

    TypeDeserializer(TypeDeserializerBuilder builder) {
        this.delegate = builder.getDelegate();
        this.clazz = builder.getClazz();
    }

    @Override
    public final Object deserialize(String value, DeserializationContextImpl context) {
        return delegate.deserialize(deserializeValue(value, context, clazz), context);
    }

    abstract Object deserializeValue(String value, DeserializationContextImpl context, Type rType);

    ModelDeserializer<Object> getDelegate() {
        return delegate;
    }

    Class<?> getType() {
        return clazz;
    }

}
