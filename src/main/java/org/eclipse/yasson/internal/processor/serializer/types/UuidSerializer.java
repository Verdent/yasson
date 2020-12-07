package org.eclipse.yasson.internal.processor.serializer.types;

import java.util.UUID;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class UuidSerializer extends TypeSerializer<UUID> {

    UuidSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(UUID value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.toString());
    }

}
