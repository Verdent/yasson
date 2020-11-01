package org.eclipse.yasson.internal.processor.convertor;

import java.math.BigInteger;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class BigIntegerConvertor implements TypeConvertor<BigInteger> {
    @Override
    public String serialize(BigInteger object, SerializationContextImpl context) {
        return object.toString();
    }

    @Override
    public BigInteger deserialize(String object, DeserializationContextImpl context) {
        return new BigInteger(object);
    }
}
