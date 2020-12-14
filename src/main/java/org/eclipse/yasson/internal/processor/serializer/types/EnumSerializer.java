package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class EnumSerializer extends TypeSerializer<Enum<?>> {

    EnumSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(Enum<?> value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.name());
    }

    @Override
    void serializeKey(Enum<?> key, JsonGenerator generator, SerializationContextImpl context) {
        generator.writeKey(key.name());
    }
}
