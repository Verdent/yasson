package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.util.Objects;

import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.serializer.DeserializerBuilder;

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
    public final Object deserialize(String value, DeserializationContextImpl context, Type rType) {
        return delegate.deserialize(deserializeValue(value, context, clazz), context, rType);
    }

    abstract Object deserializeValue(String value, DeserializationContextImpl context, Type rType);

    ModelDeserializer<Object> getDelegate() {
        return delegate;
    }

    Class<?> getType() {
        return clazz;
    }

}
