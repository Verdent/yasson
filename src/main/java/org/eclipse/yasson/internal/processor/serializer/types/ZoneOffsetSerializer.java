package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.ZoneOffset;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class ZoneOffsetSerializer extends TypeSerializer<ZoneOffset> {

    ZoneOffsetSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(ZoneOffset value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.getId());
    }
}
