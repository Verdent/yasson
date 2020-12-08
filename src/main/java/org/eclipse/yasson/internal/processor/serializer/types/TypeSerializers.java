package org.eclipse.yasson.internal.processor.serializer.types;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;

import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.json.JsonValue;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.serializer.KeyWriter;
import org.eclipse.yasson.internal.processor.serializer.ModelSerializer;
import org.eclipse.yasson.internal.processor.serializer.NullSerializer;
import org.eclipse.yasson.internal.processor.serializer.SerializationModelCreator;
import org.eclipse.yasson.internal.serializer.DateTypeDeserializer;
import org.eclipse.yasson.internal.serializer.SerializerProviderWrapper;
import org.eclipse.yasson.internal.serializer.SqlDateTypeDeserializer;
import org.eclipse.yasson.internal.serializer.SqlDateTypeSerializer;
import org.eclipse.yasson.internal.serializer.SqlTimestampTypeDeserializer;
import org.eclipse.yasson.internal.serializer.SqlTimestampTypeSerializer;

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
        cache.put(Duration.class, DurationSerializer::new);
        cache.put(Float.class, FloatSerializer::new);
        cache.put(Float.TYPE, FloatSerializer::new);
        cache.put(Integer.class, IntegerSerializer::new);
        cache.put(Integer.TYPE, IntegerSerializer::new);
        cache.put(Instant.class, InstantSerializer::new);
        cache.put(LocalDateTime.class, LocalDateTimeSerializer::new);
        cache.put(LocalDate.class, LocalDateSerializer::new);
        cache.put(LocalTime.class, LocalTimeSerializer::new);
        cache.put(Long.class, LongSerializer::new);
        cache.put(Long.TYPE, LongSerializer::new);
        cache.put(Number.class, NumberSerializer::new);
        cache.put(Object.class, ObjectTypeSerializer::new);
        cache.put(OffsetDateTime.class, OffsetDateTimeSerializer::new);
        cache.put(OffsetTime.class, OffsetTimeSerializer::new);
        cache.put(Path.class, PathSerializer::new);
        cache.put(Period.class, PeriodSerializer::new);
        cache.put(Short.class, ShortSerializer::new);
        cache.put(Short.TYPE, ShortSerializer::new);
        cache.put(String.class, StringSerializer::new);
        cache.put(TimeZone.class, TimeZoneSerializer::new);
        cache.put(URI.class, UriSerializer::new);
        cache.put(URL.class, UrlSerializer::new);
        cache.put(UUID.class, UuidSerializer::new);
        cache.put(XMLGregorianCalendar.class, XmlGregorianCalendarSerializer::new);
        cache.put(ZonedDateTime.class, ZonedDateTimeSerializer::new);
        cache.put(ZoneId.class, ZoneIdSerializer::new);
        cache.put(ZoneOffset.class, ZoneOffsetSerializer::new);
        if (isClassAvailable("java.sql.Date")) {
            cache.put(Date.class, SqlDateSerializer::new);
            cache.put(java.sql.Date.class, SqlDateSerializer::new);
            cache.put(java.sql.Timestamp.class, SqlTimestampSerializer::new);
        }
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

        if (Enum.class.isAssignableFrom(clazz)) {
            return SerializationModelCreator.wrapInCommonSet(new EnumSerializer(builder), customization);
        } else if (JsonValue.class.isAssignableFrom(clazz)) {
            return SerializationModelCreator.wrapInCommonSet(new JsonValueSerializer(builder), customization);
        }

        do {
            if (SERIALIZERS.containsKey(current)) {
                return SerializationModelCreator.wrapInCommonSet(SERIALIZERS.get(current).apply(builder), customization);
            }
            current = current.getSuperclass();
        } while (!Object.class.equals(current) && current != null);

        return null;
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

}
