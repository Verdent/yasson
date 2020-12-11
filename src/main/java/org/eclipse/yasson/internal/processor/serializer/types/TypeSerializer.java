package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.processor.serializer.ModelSerializer;

/**
 * TODO javadoc
 */
abstract class TypeSerializer<T> implements ModelSerializer {

    private final Class<?> clazz;

    TypeSerializer(TypeSerializerBuilder serializerBuilder) {
        this.clazz = serializerBuilder.getClazz();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        serializeValue((T) value, generator, context);
    }

    abstract void serializeValue(T value, JsonGenerator generator, SerializationContextImpl context);

    abstract void serializeKey(T key, JsonGenerator generator, SerializationContextImpl context);

    protected Class<?> getClazz() {
        return clazz;
    }
}
