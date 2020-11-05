package org.eclipse.yasson.internal.processor.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.NullCheckDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ValueExtractor;

/**
 * TODO javadoc
 */
public class TypeDeserializers {

    private static final Map<Class<?>, Function<TypeDeserializerBuilder, ModelDeserializer<String>>> DESERIALIZERS =
            new HashMap<>();
    private static final Map<Class<?>, Function<TypeDeserializerBuilder, ModelDeserializer<JsonParser>>> ASSIGNABLE =
            new HashMap<>();

    static {
        DESERIALIZERS.put(BigInteger.class, BigIntegerDeserializer::new);
        DESERIALIZERS.put(BigDecimal.class, BigDecimalDeserializer::new);
        DESERIALIZERS.put(Boolean.class, BooleanDeserializer::new);
        DESERIALIZERS.put(Boolean.TYPE, BooleanDeserializer::new);
        DESERIALIZERS.put(Byte.class, ByteDeserializer::new);
        DESERIALIZERS.put(Byte.TYPE, ByteDeserializer::new);
        DESERIALIZERS.put(Date.class, DateDeserializer::new);
        DESERIALIZERS.put(Double.class, DoubleDeserializer::new);
        DESERIALIZERS.put(Double.TYPE, DoubleDeserializer::new);
        DESERIALIZERS.put(Instant.class, InstantDeserializer::new);
        DESERIALIZERS.put(Integer.class, IntegerDeserializer::new);
        DESERIALIZERS.put(Integer.TYPE, IntegerDeserializer::new);
        DESERIALIZERS.put(Long.class, LongDeserializer::new);
        DESERIALIZERS.put(Long.TYPE, LongDeserializer::new);
        DESERIALIZERS.put(Short.class, ShortDeserializer::new);
        DESERIALIZERS.put(Short.TYPE, ShortDeserializer::new);
        DESERIALIZERS.put(String.class, StringDeserializer::new);

        ASSIGNABLE.put(JsonValue.class, JsonValueDeserializer::new);
    }

    public static ModelDeserializer<JsonParser> getTypeDeserializer(Class<?> clazz,
                                                                    Customization customization,
                                                                    JsonbConfigProperties properties,
                                                                    ModelDeserializer<Object> delegate) {
        TypeDeserializerBuilder builder = new TypeDeserializerBuilder(clazz, customization, properties, delegate);
        return Optional.ofNullable(DESERIALIZERS.get(clazz))
                .map(it -> it.apply(builder))
                .map(ValueExtractor::new)
                .map(extractor -> (ModelDeserializer<JsonParser>) extractor)
                .or(() -> assignableCases(builder))
                .map(deserializer -> new NullCheckDeserializer(deserializer, delegate, clazz))
                .orElse(null);
    }

    private static Optional<ModelDeserializer<JsonParser>> assignableCases(TypeDeserializerBuilder builder) {
        if (Enum.class.isAssignableFrom(builder.getClazz())) {
            return Optional.of(new ValueExtractor(new EnumDeserializer(builder)));
        }
        for (Class<?> clazz : ASSIGNABLE.keySet()) {
            if (clazz.isAssignableFrom(builder.getClazz())) {
                return Optional.of(ASSIGNABLE.get(clazz).apply(builder));
            }
        }
        return Optional.empty();
    }

}
