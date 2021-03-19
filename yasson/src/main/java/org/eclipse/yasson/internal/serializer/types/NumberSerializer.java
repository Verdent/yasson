package org.eclipse.yasson.internal.serializer.types;

import java.math.BigDecimal;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class NumberSerializer extends AbstractNumberSerializer<Number> {

    NumberSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(Number value, JsonGenerator generator) {
        generator.write(new BigDecimal(String.valueOf(value)));
    }
}
