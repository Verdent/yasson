package org.eclipse.yasson.internal.processor;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.json.JsonValue;

/**
 * TODO javadoc
 */
public class BuiltInTypes {
    
    private static final Set<Class<?>> BUILD_IN_SUPPORT;
    
    static {
        Set<Class<?>> buildInTypes = new HashSet<>();
        buildInTypes.add(Byte.class);
        buildInTypes.add(Byte.TYPE);
        buildInTypes.add(BigDecimal.class);
        buildInTypes.add(BigInteger.class);
        buildInTypes.add(Boolean.class);
        buildInTypes.add(Boolean.TYPE);
        buildInTypes.add(Calendar.class);
        buildInTypes.add(Character.class);
        buildInTypes.add(Character.TYPE);
        buildInTypes.add(Date.class);
        buildInTypes.add(Double.class);
        buildInTypes.add(Double.TYPE);
        buildInTypes.add(Duration.class);
        buildInTypes.add(Float.class);
        buildInTypes.add(Float.TYPE);
        buildInTypes.add(Integer.class);
        buildInTypes.add(Integer.TYPE);
        buildInTypes.add(Instant.class);
        buildInTypes.add(LocalDateTime.class);
        buildInTypes.add(LocalDate.class);
        buildInTypes.add(LocalTime.class);
        buildInTypes.add(Long.class);
        buildInTypes.add(Long.TYPE);
        buildInTypes.add(Number.class);
        buildInTypes.add(OffsetDateTime.class);
        buildInTypes.add(OffsetTime.class);
        buildInTypes.add(OptionalDouble.class);
        buildInTypes.add(OptionalInt.class);
        buildInTypes.add(OptionalLong.class);
        buildInTypes.add(Path.class);
        buildInTypes.add(Period.class);
        buildInTypes.add(Short.class);
        buildInTypes.add(Short.TYPE);
        buildInTypes.add(String.class);
        buildInTypes.add(TimeZone.class);
        buildInTypes.add(URI.class);
        buildInTypes.add(URL.class);
        buildInTypes.add(UUID.class);
        buildInTypes.add(XMLGregorianCalendar.class);
        buildInTypes.add(ZonedDateTime.class);
        buildInTypes.add(ZoneId.class);
        buildInTypes.add(ZoneOffset.class);
        if (isClassAvailable("java.sql.Date")) {
            buildInTypes.add(Date.class);
            buildInTypes.add(java.sql.Date.class);
            buildInTypes.add(java.sql.Timestamp.class);
        }
        BUILD_IN_SUPPORT = Collections.unmodifiableSet(buildInTypes);
    }


    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    public static boolean isKnowType(Class<?> clazz) {
        boolean knownContainerValueType = Collection.class.isAssignableFrom(clazz)
                || Map.class.isAssignableFrom(clazz)
                || JsonValue.class.isAssignableFrom(clazz)
                || Optional.class.isAssignableFrom(clazz)
                || clazz.isArray();

        return knownContainerValueType || findIfClassIsSupported(clazz);
    }

    private static boolean findIfClassIsSupported(Class<?> clazz) {
        Class<?> current = clazz;
        do {
            if (BUILD_IN_SUPPORT.contains(current)) {
                return true;
            }
            current = current.getSuperclass();
        } while (current != null);
        return false;
    }
}
