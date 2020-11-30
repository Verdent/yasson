package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class BooleanSerializer extends TypeSerializer<Boolean> {

    BooleanSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(Boolean value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value);
    }
}
