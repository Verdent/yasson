package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class ByteSerializer extends AbstractNumberSerializer<Byte> {

    ByteSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(Byte value, JsonGenerator generator) {
        generator.write(value);
    }

}
