package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class DoubleSerializer extends AbstractNumberSerializer<Double> {

    DoubleSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(Double value, JsonGenerator generator) {
        generator.write(value);
    }
}
