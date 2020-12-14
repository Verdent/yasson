package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.processor.serializer.ModelSerializer;

/**
 * TODO javadoc
 */
abstract class TypeSerializer<T> implements ModelSerializer {

    private final Class<?> clazz;
    private final ModelSerializer serializer;

    TypeSerializer(TypeSerializerBuilder serializerBuilder) {
        this.clazz = serializerBuilder.getClazz();
        if (serializerBuilder.isKey()) {
            serializer = new KeySerializer();
        } else {
            serializer = new ValueSerializer();
        }
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        serializer.serialize(value, generator, context);
    }

    abstract void serializeValue(T value, JsonGenerator generator, SerializationContextImpl context);

    void serializeKey(T key, JsonGenerator generator, SerializationContextImpl context) {
        generator.writeKey(String.valueOf(key));
    }

    protected Class<?> getClazz() {
        return clazz;
    }

    private final class ValueSerializer implements ModelSerializer {

        @SuppressWarnings("unchecked")
        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
            serializeValue((T) value, generator, context);
        }

    }

    private final class KeySerializer implements ModelSerializer {

        @SuppressWarnings("unchecked")
        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
            serializeKey((T) value, generator, context);
        }

    }
}
