package org.eclipse.yasson.internal.deserializer.types;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
public abstract class TypeDeserializer implements ModelDeserializer<String> {

    private final ModelDeserializer<Object> delegate;
    private final Class<?> clazz;

    TypeDeserializer(TypeDeserializerBuilder builder) {
        this.delegate = builder.getDelegate();
        this.clazz = builder.getClazz();
    }

    @Override
    public final Object deserialize(String value, DeserializationContextImpl context) {
        return delegate.deserialize(this.deserializeStringValue(value, context, clazz), context);
    }

    public final Object deserialize(boolean value, DeserializationContextImpl context) {
        return delegate.deserialize(this.deserializeBooleanValue(value, context, clazz), context);
    }

    public final Object deserialize(JsonParser value, DeserializationContextImpl context) {
        return delegate.deserialize(this.deserializeNumberValue(value, context, clazz), context);
    }

    abstract Object deserializeStringValue(String value, DeserializationContextImpl context, Type rType);

    Object deserializeBooleanValue(boolean value, DeserializationContextImpl context, Type rType) {
        return deserializeStringValue(String.valueOf(value), context, rType);
    }

    Object deserializeNumberValue(JsonParser value, DeserializationContextImpl context, Type rType) {
        return deserializeStringValue(value.getString(), context, rType);
    }

    ModelDeserializer<Object> getDelegate() {
        return delegate;
    }

    Class<?> getType() {
        return clazz;
    }

}
