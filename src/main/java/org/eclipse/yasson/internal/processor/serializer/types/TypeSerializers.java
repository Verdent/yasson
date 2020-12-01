package org.eclipse.yasson.internal.processor.serializer.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.serializer.ModelSerializer;
import org.eclipse.yasson.internal.processor.serializer.NullSerializer;

/**
 * TODO javadoc
 */
public class TypeSerializers {

    private static final Map<Class<?>, Function<TypeSerializerBuilder, ModelSerializer>> SERIALIZERS = new HashMap<>();

    static {
        SERIALIZERS.put(Byte.class, ByteSerializer::new);
        SERIALIZERS.put(Byte.TYPE, ByteSerializer::new);
        SERIALIZERS.put(BigDecimal.class, BigDecimalSerializer::new);
        SERIALIZERS.put(BigInteger.class, BigIntegerSerializer::new);
        SERIALIZERS.put(Boolean.class, BooleanSerializer::new);
        SERIALIZERS.put(Boolean.TYPE, BooleanSerializer::new);
        SERIALIZERS.put(Character.class, CharSerializer::new);
        SERIALIZERS.put(Character.TYPE, CharSerializer::new);
        SERIALIZERS.put(Double.class, DoubleSerializer::new);
        SERIALIZERS.put(Double.TYPE, DoubleSerializer::new);
        SERIALIZERS.put(Float.class, FloatSerializer::new);
        SERIALIZERS.put(Float.TYPE, FloatSerializer::new);
        SERIALIZERS.put(Integer.class, IntegerSerializer::new);
        SERIALIZERS.put(Integer.TYPE, IntegerSerializer::new);
        SERIALIZERS.put(Long.class, LongSerializer::new);
        SERIALIZERS.put(Long.TYPE, LongSerializer::new);
        SERIALIZERS.put(Number.class, NumberSerializer::new);
        SERIALIZERS.put(Object.class, ObjectTypeSerializer::new);
        SERIALIZERS.put(Short.class, ShortSerializer::new);
        SERIALIZERS.put(Short.TYPE, ShortSerializer::new);
        SERIALIZERS.put(String.class, StringSerializer::new);
    }

    private TypeSerializers() {
        throw new IllegalStateException("Util class cannot be instantiated");
    }

    public static ModelSerializer getTypeSerializer(Class<?> clazz, Customization customization, JsonbContext jsonbContext) {
        Class<?> current = clazz;
        TypeSerializerBuilder builder = new TypeSerializerBuilder(current, customization, jsonbContext);
        if (Object.class.equals(current)) {
            return new NullSerializer(SERIALIZERS.get(current).apply(builder), jsonbContext);
        }
        do {
            if (SERIALIZERS.containsKey(current)) {
                return new NullSerializer(SERIALIZERS.get(current).apply(builder), jsonbContext);
            }
            current = current.getSuperclass();
        } while (!Object.class.equals(current) && current != null);

        if (Enum.class.isAssignableFrom(clazz)) {
            return new EnumSerializer(builder);
        }

        return null;
    }

}
