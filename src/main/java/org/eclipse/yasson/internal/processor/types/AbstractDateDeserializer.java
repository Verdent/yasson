package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;
import org.eclipse.yasson.internal.serializer.JsonbDateFormatter;

/**
 * TODO javadoc
 */
abstract class AbstractDateDeserializer<T> extends TypeDeserializer {

    static final ZoneId UTC = ZoneId.of("UTC");

    private final ModelDeserializer<String> actualDeserializer;

    AbstractDateDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
        this.actualDeserializer = actualDeserializer(builder);
    }

    private ModelDeserializer<String> actualDeserializer(TypeDeserializerBuilder builder) {
        JsonbConfigProperties configProperties = builder.getConfigProperties();
        final JsonbDateFormatter formatter = getJsonbDateFormatter(builder);
        if (JsonbDateFormat.TIME_IN_MILLIS.equals(formatter.getFormat())) {
            return (value, context) -> fromInstant(Instant.ofEpochMilli(Long.parseLong(value)));
        } else if (formatter.getDateTimeFormatter() != null) {
            return (value, context) -> parseWithFormatterInternal(value, formatter.getDateTimeFormatter());
        } else {
            DateTimeFormatter configDateTimeFormatter = configProperties.getConfigDateFormatter().getDateTimeFormatter();
            if (configDateTimeFormatter != null) {
                return (value, context) -> parseWithFormatterInternal(value, configDateTimeFormatter);
            }
        }
        if (configProperties.isStrictIJson()) {
            return (value, context) -> parseWithFormatterInternal(value, JsonbDateFormatter.IJSON_DATE_FORMATTER);
        }
        Locale locale = configProperties.getLocale(formatter.getLocale());
        return (value, context) -> {
            try {
                return parseDefault(value, locale);
            } catch (DateTimeException e) {
                throw new JsonbException(Messages.getMessage(MessageKeys.DATE_PARSE_ERROR, value, getType()), e);
            }
        };
    }

    private JsonbDateFormatter getJsonbDateFormatter(TypeDeserializerBuilder builder) {
        return Optional.ofNullable(builder.getCustomization().getDeserializeDateFormatter())
                .orElse(builder.getConfigProperties().getConfigDateFormatter());
    }

    @Override
    public Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return actualDeserializer.deserialize(value, context);
    }

    /**
     * Construct date object from an instant containing epoch millisecond.
     * If date object supports zone offset / zone id, system default is used and warning is logged.
     *
     * @param instant instant to construct from
     * @return date object
     */
    abstract T fromInstant(Instant instant);

    /**
     * Parse java.time date object with default formatter.
     * Different default formatter for each date object type is used.
     *
     * @param jsonValue string value to parse from
     * @param locale    annotated locale or default
     * @return parsed date object
     */
    abstract T parseDefault(String jsonValue, Locale locale);

    /**
     * Parse java.time date object with provided formatter.
     *
     * @param jsonValue string value to parse from
     * @param formatter a formatter to use
     * @return parsed date object
     */
    abstract T parseWithFormatter(String jsonValue, DateTimeFormatter formatter);

    private T parseWithFormatterInternal(String jsonValue, DateTimeFormatter formatter) {
        try {
            return parseWithFormatter(jsonValue, formatter);
        } catch (DateTimeException e) {
            throw new JsonbException(Messages.getMessage(MessageKeys.DATE_PARSE_ERROR, jsonValue, getType()), e);
        }
    }

    protected DateTimeFormatter getZonedFormatter(DateTimeFormatter formatter) {
        return formatter.getZone() != null
                ? formatter
                : formatter.withZone(UTC);
    }

}
