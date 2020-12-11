package org.eclipse.yasson.internal.processor.deserializer.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Timestamp;
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;

import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.json.JsonValue;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.JustReturn;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.NullCheckDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.PositionChecker;
import org.eclipse.yasson.internal.processor.deserializer.ValueExtractor;

/**
 * TODO javadoc
 */
public class TypeDeserializers {

    private static final Map<Class<?>, Function<TypeDeserializerBuilder, ModelDeserializer<String>>> DESERIALIZERS =
            new HashMap<>();
    private static final Map<Class<?>, Function<TypeDeserializerBuilder, ModelDeserializer<JsonParser>>> ASSIGNABLE =
            new HashMap<>();
    private static final Map<Class<?>, Class<?>> OPTIONAL_TYPES = new HashMap<>();

    static {
        DESERIALIZERS.put(BigInteger.class, BigIntegerDeserializer::new);
        DESERIALIZERS.put(BigDecimal.class, BigDecimalDeserializer::new);
        DESERIALIZERS.put(Boolean.class, BooleanDeserializer::new);
        DESERIALIZERS.put(Boolean.TYPE, BooleanDeserializer::new);
        DESERIALIZERS.put(Byte.class, ByteDeserializer::new);
        DESERIALIZERS.put(Byte.TYPE, ByteDeserializer::new);
        DESERIALIZERS.put(Calendar.class, CalendarDeserializer::new);
        DESERIALIZERS.put(Character.TYPE, CharDeserializer::new);
        DESERIALIZERS.put(Character.class, CharDeserializer::new);
        DESERIALIZERS.put(Date.class, DateDeserializer::new);
        DESERIALIZERS.put(Double.class, DoubleDeserializer::new);
        DESERIALIZERS.put(Double.TYPE, DoubleDeserializer::new);
        DESERIALIZERS.put(Duration.class, DurationDeserializer::new);
        DESERIALIZERS.put(Float.class, FloatDeserializer::new);
        DESERIALIZERS.put(Float.TYPE, FloatDeserializer::new);
        DESERIALIZERS.put(GregorianCalendar.class, CalendarDeserializer::new);
        DESERIALIZERS.put(Instant.class, InstantDeserializer::new);
        DESERIALIZERS.put(Integer.class, IntegerDeserializer::new);
        DESERIALIZERS.put(Integer.TYPE, IntegerDeserializer::new);
        DESERIALIZERS.put(LocalDate.class, LocalDateDeserializer::new);
        DESERIALIZERS.put(LocalDateTime.class, LocalDateTimeDeserializer::new);
        DESERIALIZERS.put(LocalTime.class, LocalTimeDeserializer::new);
        DESERIALIZERS.put(Long.class, LongDeserializer::new);
        DESERIALIZERS.put(Long.TYPE, LongDeserializer::new);
        DESERIALIZERS.put(Number.class, NumberDeserializer::new);
        DESERIALIZERS.put(OffsetDateTime.class, OffsetDateTimeDeserializer::new);
        DESERIALIZERS.put(OffsetTime.class, OffsetTimeDeserializer::new);
        DESERIALIZERS.put(Path.class, PathDeserializer::new);
        DESERIALIZERS.put(Period.class, PeriodDeserializer::new);
        DESERIALIZERS.put(Short.class, ShortDeserializer::new);
        DESERIALIZERS.put(Short.TYPE, ShortDeserializer::new);
        DESERIALIZERS.put(String.class, StringDeserializer::new);
        DESERIALIZERS.put(SimpleTimeZone.class, TimeZoneDeserializer::new);
        DESERIALIZERS.put(TimeZone.class, TimeZoneDeserializer::new);
        DESERIALIZERS.put(URI.class, UriDeserializer::new);
        DESERIALIZERS.put(URL.class, UrlDeserializer::new);
        DESERIALIZERS.put(UUID.class, UuidDeserializer::new);
        DESERIALIZERS.put(XMLGregorianCalendar.class, XmlGregorianCalendar::new);
        DESERIALIZERS.put(ZonedDateTime.class, ZonedDateTimeDeserializer::new);
        DESERIALIZERS.put(ZoneId.class, ZoneIdDeserializer::new);
        DESERIALIZERS.put(ZoneOffset.class, ZoneOffsetDeserializer::new);
        if (isClassAvailable("java.sql.Date")) {
            DESERIALIZERS.put(java.sql.Date.class, SqlDateDeserializer::new);
            DESERIALIZERS.put(Timestamp.class, SqlTimestampDeserializer::new);
        }

        ASSIGNABLE.put(JsonValue.class, JsonValueDeserializer::new);

        OPTIONAL_TYPES.put(OptionalLong.class, Long.class);
        OPTIONAL_TYPES.put(OptionalInt.class, Integer.class);
        OPTIONAL_TYPES.put(OptionalDouble.class, Double.class);
    }

    public static ModelDeserializer<JsonParser> getTypeDeserializer(Class<?> clazz,
                                                                    Customization customization,
                                                                    JsonbConfigProperties properties,
                                                                    ModelDeserializer<Object> delegate,
                                                                    PositionChecker.Checker checker) {
        return getTypeDeserializer(clazz, customization, properties, delegate, checker.getEvents());
    }

    public static ModelDeserializer<JsonParser> getTypeDeserializer(Class<?> clazz,
                                                                    Customization customization,
                                                                    JsonbConfigProperties properties,
                                                                    ModelDeserializer<Object> delegate,
                                                                    Set<JsonParser.Event> events) {
        JsonParser.Event[] eventArray = events.toArray(new JsonParser.Event[0]);
        if (OPTIONAL_TYPES.containsKey(clazz)) {
            Class<?> optionalType = OPTIONAL_TYPES.get(clazz);
            TypeDeserializerBuilder builder = new TypeDeserializerBuilder(optionalType,
                                                                          customization,
                                                                          properties,
                                                                          JustReturn.create());
            ValueExtractor valueExtractor = new ValueExtractor(DESERIALIZERS.get(optionalType).apply(builder));
            PositionChecker positionChecker = new PositionChecker(valueExtractor, clazz, eventArray);
            if (OptionalLong.class.equals(clazz)) {
                return new OptionalLongDeserializer(positionChecker, delegate);
            } else if (OptionalInt.class.equals(clazz)) {
                return new OptionalIntDeserializer(positionChecker, delegate);
            } else if (OptionalDouble.class.equals(clazz)) {
                return new OptionalDoubleDeserializer(positionChecker, delegate);
            } else {
                throw new JsonbException("Unsupported Optional type for deserialization: " + clazz);
            }
        }

        TypeDeserializerBuilder builder = new TypeDeserializerBuilder(clazz, customization, properties, delegate);
        if (DESERIALIZERS.containsKey(clazz)) {
            ValueExtractor valueExtractor = new ValueExtractor(DESERIALIZERS.get(clazz).apply(builder));
            return new NullCheckDeserializer(new PositionChecker(valueExtractor, clazz, eventArray),
                                             delegate,
                                             clazz);
        }

        ModelDeserializer<JsonParser> deserializer = assignableCases(builder, eventArray);
        if (deserializer != null) {
            return new NullCheckDeserializer(deserializer, delegate, clazz);
        }
        return null;

        //        return Optional.ofNullable(Optional.ofNullable(DESERIALIZERS.get(clazz))
        //                .map(it -> it.apply(builder))
        //                .map(ValueExtractor::new)
        //                .map(extractor -> (ModelDeserializer<JsonParser>) extractor)
        //                .orElseGet(() -> assignableCases(builder)))
        //                .map(deserializer -> new NullCheckDeserializer(deserializer, delegate, clazz))
        //                .orElse(null);
    }

    private static ModelDeserializer<JsonParser> assignableCases(TypeDeserializerBuilder builder,
                                                                 JsonParser.Event[] checker) {
        if (Enum.class.isAssignableFrom(builder.getClazz())) {
            return new PositionChecker(new ValueExtractor(new EnumDeserializer(builder)),
                                       builder.getClazz(),
                                       checker);
        } else if (Object.class.equals(builder.getClazz())) {
            return new ObjectTypeDeserializer(builder);
        }
        //TODO replace to else if?
        for (Class<?> clazz : ASSIGNABLE.keySet()) {
            if (clazz.isAssignableFrom(builder.getClazz())) {
                return ASSIGNABLE.get(clazz).apply(builder);
            }
        }
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
