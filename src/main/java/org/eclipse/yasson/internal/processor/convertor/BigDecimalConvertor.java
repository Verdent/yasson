package org.eclipse.yasson.internal.processor.convertor;

import java.math.BigDecimal;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
public class BigDecimalConvertor implements TypeConvertor<BigDecimal> {
    @Override
    public String serialize(BigDecimal object, SerializationContextImpl context) {
        return object.toString();
    }

    @Override
    public BigDecimal deserialize(String object, DeserializationContextImpl context) {
        return new BigDecimal(object);
    }
}
