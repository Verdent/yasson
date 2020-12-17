package org.eclipse.yasson.internal.deserializer.types;

import java.math.BigInteger;

/**
 * TODO javadoc
 */
class BigIntegerDeserializer extends AbstractNumberDeserializer<BigInteger> {

    BigIntegerDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true);
    }

    @Override
    BigInteger parseNumberValue(String value) {
        return new BigInteger(value);
    }
}
