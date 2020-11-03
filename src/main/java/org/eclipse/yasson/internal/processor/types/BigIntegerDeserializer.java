package org.eclipse.yasson.internal.processor.types;

import java.math.BigInteger;

/**
 * TODO javadoc
 */
class BigIntegerDeserializer extends AbstractNumberDeserializer<BigInteger> {

    BigIntegerDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true, BigInteger.class);
    }

    @Override
    BigInteger parseNumberValue(String value) {
        return new BigInteger(value);
    }
}
