package org.eclipse.yasson.internal.processor.serializer.types;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;

import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.serializer.KeyWriter;
import org.eclipse.yasson.internal.processor.serializer.ModelSerializer;
import org.eclipse.yasson.internal.processor.serializer.NullSerializer;

/**
 * TODO javadoc
 */
public class TypeSerializers {

    private static final Map<Class<?>, Function<TypeSerializerBuilder, ModelSerializer>> SERIALIZERS;

    private static final Map<Class<?>, Class<?>> OPTIONALS;

    static {
        Map<Class<?>, Function<TypeSerializerBuilder, ModelSerializer>> cache = new HashMap<>();
        cache.put(Byte.class, ByteSerializer::new);
        cache.put(Byte.TYPE, ByteSerializer::new);
        cache.put(BigDecimal.class, BigDecimalSerializer::new);
        cache.put(BigInteger.class, BigIntegerSerializer::new);
        cache.put(Boolean.class, BooleanSerializer::new);
        cache.put(Boolean.TYPE, BooleanSerializer::new);
        cache.put(Calendar.class, CalendarSerializer::new);
        cache.put(Character.class, CharSerializer::new);
        cache.put(Character.TYPE, CharSerializer::new);
        cache.put(Date.class, DateSerializer::new);
        cache.put(Double.class, DoubleSerializer::new);
        cache.put(Double.TYPE, DoubleSerializer::new);
        cache.put(Float.class, FloatSerializer::new);
        cache.put(Float.TYPE, FloatSerializer::new);
        cache.put(Integer.class, IntegerSerializer::new);
        cache.put(Integer.TYPE, IntegerSerializer::new);
        cache.put(Instant.class, InstantSerializer::new);
        cache.put(Long.class, LongSerializer::new);
        cache.put(Long.TYPE, LongSerializer::new);
        cache.put(Number.class, NumberSerializer::new);
        cache.put(Object.class, ObjectTypeSerializer::new);
        cache.put(Short.class, ShortSerializer::new);
        cache.put(Short.TYPE, ShortSerializer::new);
        cache.put(String.class, StringSerializer::new);
        SERIALIZERS = Collections.unmodifiableMap(cache);

        Map<Class<?>, Class<?>> optionals = new HashMap<>();
        optionals.put(OptionalDouble.class, Double.class);
        optionals.put(OptionalInt.class, Integer.class);
        optionals.put(OptionalLong.class, Long.class);
        OPTIONALS = Collections.unmodifiableMap(optionals);
    }

    private TypeSerializers() {
        throw new IllegalStateException("Util class cannot be instantiated");
    }

    public static ModelSerializer getTypeSerializer(Class<?> clazz, Customization customization, JsonbContext jsonbContext) {
        return getTypeSerializer(Collections.emptyList(), clazz, customization, jsonbContext);
    }

    public static ModelSerializer getTypeSerializer(List<Type> chain,
                                                    Class<?> clazz,
                                                    Customization customization,
                                                    JsonbContext jsonbContext) {
        List<Type> chainClone = new LinkedList<>(chain);
        Class<?> current = clazz;
        TypeSerializerBuilder builder = new TypeSerializerBuilder(chainClone, current, customization, jsonbContext);
        if (Object.class.equals(current)) {
            return new NullSerializer(SERIALIZERS.get(current).apply(builder), customization);
        }
        if (OPTIONALS.containsKey(current)) {
            Class<?> optionalInner = OPTIONALS.get(current);
            ModelSerializer serializer = getTypeSerializer(optionalInner, customization, jsonbContext);
            if (OptionalInt.class.equals(current)) {
                return new OptionalIntSerializer(serializer);
            } else if (OptionalLong.class.equals(current)) {
                return new OptionalLongSerializer(serializer);
            } else if (OptionalDouble.class.equals(current)) {
                return new OptionalDoubleSerializer(serializer);
            }
        }
        do {
            if (SERIALIZERS.containsKey(current)) {
                return new NullSerializer(new KeyWriter(SERIALIZERS.get(current).apply(builder)), customization);
            }
            current = current.getSuperclass();
        } while (!Object.class.equals(current) && current != null);

        if (Enum.class.isAssignableFrom(clazz)) {
            return new NullSerializer(new KeyWriter(new EnumSerializer(builder)), customization);
        }

        return null;
    }

}
