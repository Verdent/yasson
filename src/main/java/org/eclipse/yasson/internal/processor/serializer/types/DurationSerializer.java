package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.Duration;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class DurationSerializer extends TypeSerializer<Duration> {

    DurationSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(Duration value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.toString());
    }

}
