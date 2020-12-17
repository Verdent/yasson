package org.eclipse.yasson.internal.serializer.types;

import java.math.BigInteger;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class BigIntegerSerializer extends AbstractNumberSerializer<BigInteger> {

    BigIntegerSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(BigInteger value, JsonGenerator generator) {
        generator.write(value);
    }
}
