package org.eclipse.yasson.internal.processor.deserializer.types;

import java.math.BigDecimal;

/**
 * TODO javadoc
 */
class BigDecimalDeserializer extends AbstractNumberDeserializer<BigDecimal> {

    BigDecimalDeserializer(TypeDeserializerBuilder builder) {
        super(builder, false);
    }

    @Override
    BigDecimal parseNumberValue(String value) {
        return new BigDecimal(value);
    }
}
