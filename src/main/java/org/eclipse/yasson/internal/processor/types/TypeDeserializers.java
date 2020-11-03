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
        DESERIALIZERS.put(BigInteger.class, BigIntegerDeserializer::new);
        DESERIALIZERS.put(BigDecimal.class, BigDecimalDeserializer::new);
        DESERIALIZERS.put(Boolean.class, BooleanDeserializer::new);
        DESERIALIZERS.put(Boolean.TYPE, BooleanDeserializer::new);
        DESERIALIZERS.put(Byte.class, ByteDeserializer::new);
        DESERIALIZERS.put(Byte.TYPE, ByteDeserializer::new);
        DESERIALIZERS.put(Date.class, DateDeserializer::new);
        DESERIALIZERS.put(Integer.class, IntegerDeserializer::new);
        DESERIALIZERS.put(Integer.TYPE, IntegerDeserializer::new);
        DESERIALIZERS.put(Long.class, LongDeserializer::new);
        DESERIALIZERS.put(Long.TYPE, LongDeserializer::new);
        DESERIALIZERS.put(Short.class, ShortDeserializer::new);
        DESERIALIZERS.put(Short.TYPE, ShortDeserializer::new);
        DESERIALIZERS.put(String.class, StringDeserializer::new);
    }

    public static ModelDeserializer<String> getTypeDeserializer(Class<?> clazz,
                                                                Customization customization,
                                                                JsonbConfigProperties properties,
                                                                ModelDeserializer<Object> delegate) {
        TypeDeserializerBuilder builder = new TypeDeserializerBuilder(customization, properties, delegate);
        return Optional.ofNullable(DESERIALIZERS.get(clazz)).map(it -> it.apply(builder)).orElse(null);
    }

}
