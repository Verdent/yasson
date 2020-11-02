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
abstract class TypeDeserializer<T> implements ModelDeserializer<String> {

    private final ModelDeserializer<Object> delegate;

    TypeDeserializer(TypeDeserializerBuilder builder) {
        this.delegate = builder.getDelegate();
    }

    @Override
    public final Object deserialize(String value, DeserializationContextImpl context, Type rType) {
        return delegate.deserialize(deserializeValue(value, context, rType), context, rType);
    }

    abstract Object deserializeValue(String value, DeserializationContextImpl context, Type rType);

    public ModelDeserializer<Object> getDelegate() {
        return delegate;
    }

}
