package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertor;

/**
 * TODO javadoc
 */
public class TypeDeserializer implements ModelDeserializer<String> {

    private final ModelDeserializer<Object> delegate;
    private final TypeConvertor<?> typeConvertor;

    public TypeDeserializer(ModelDeserializer<Object> delegate,
                            TypeConvertor<?> typeConvertor) {
        this.delegate = delegate;
        this.typeConvertor = typeConvertor;
    }

    @Override
    public Object deserialize(String value, DeserializationContextImpl context, Type rType) {
        return delegate.deserialize(typeConvertor.deserialize(value, context), context, rType);
    }
}
