package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class LongSerializer extends AbstractNumberSerializer<Long> {

    LongSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(Long value, JsonGenerator generator) {
        generator.write(value);
    }
}
