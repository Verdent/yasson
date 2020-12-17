package org.eclipse.yasson.internal.serializer.types;

import java.time.ZoneId;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * TODO javadoc
 */
class ZoneIdSerializer extends TypeSerializer<ZoneId> {

    ZoneIdSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(ZoneId value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.getId());
    }

}
