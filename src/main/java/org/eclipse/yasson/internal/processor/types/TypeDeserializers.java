package org.eclipse.yasson.internal.processor.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
public class TypeDeserializers {

    private static final Map<Class<?>, Function<TypeDeserializerBuilder, ModelDeserializer<String>>> DESERIALIZERS = new HashMap<>();

    static {
        DESERIALIZERS.put(Date.class, DateDeserializer::new);
        DESERIALIZERS.put(String.class, StringDeserializer::new);
        DESERIALIZERS.put(Integer.class, IntegerDeserializer::new);
        DESERIALIZERS.put(BigInteger.class, BigIntegerDeserializer::new);
        DESERIALIZERS.put(BigDecimal.class, BigDecimalDeserializer::new);
    }

    public static ModelDeserializer<String> getTypeDeserializer(Class<?> clazz,
                                                                Customization customization,
                                                                JsonbConfigProperties properties,
                                                                ModelDeserializer<Object> delegate) {
        TypeDeserializerBuilder builder = new TypeDeserializerBuilder(customization, properties, delegate);
        return Optional.ofNullable(DESERIALIZERS.get(clazz)).map(it -> it.apply(builder)).orElse(null);
    }

}
