package org.eclipse.yasson.internal.serializer.types;

import java.math.BigDecimal;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class BigDecimalSerializer extends AbstractNumberSerializer<BigDecimal> {

    BigDecimalSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(BigDecimal value, JsonGenerator generator) {
        generator.write(value);
    }
}
