package org.eclipse.yasson.internal.processor.convertor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO javadoc
 */
public class TypeConvertors {

    private static final Map<Class<?>, TypeConvertor<?>> TYPE_CONVERTORS = new HashMap<>();

    static {
        TYPE_CONVERTORS.put(String.class, new StringConvertor());
        TYPE_CONVERTORS.put(Integer.class, new IntegerConvertor());
        TYPE_CONVERTORS.put(BigInteger.class, new BigIntegerConvertor());
        TYPE_CONVERTORS.put(BigDecimal.class, new BigDecimalConvertor());
    }

    private TypeConvertors() {
        throw new IllegalStateException("This class cannot be instantiated");
    }

    public static TypeConvertor<?> getConvertor(Class<?> type) {
        return TYPE_CONVERTORS.get(type);
    }

}
