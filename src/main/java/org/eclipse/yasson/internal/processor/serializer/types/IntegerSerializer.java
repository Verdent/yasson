package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class IntegerSerializer extends AbstractNumberSerializer<Integer> {

    IntegerSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(Integer value, JsonGenerator generator) {
        generator.write(value);
    }

}
