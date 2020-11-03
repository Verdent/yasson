package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.Unmarshaller;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;
import org.eclipse.yasson.internal.serializer.DeserializerBuilder;
import org.eclipse.yasson.internal.serializer.JsonbDateFormatter;

/**
 * TODO javadoc
 */
abstract class AbstractDateDeserializer<T> extends TypeDeserializer<T> {

    private final ModelDeserializer<String> actualDeserializer;
    private final Class<T> clazz;

    AbstractDateDeserializer(TypeDeserializerBuilder builder,
                             Class<T> clazz) {
        super(builder);
        this.clazz = clazz;
        this.actualDeserializer = actualDeserializer(builder);
    }

    private ModelDeserializer<String> actualDeserializer(TypeDeserializerBuilder builder) {
        final JsonbDateFormatter formatter = getJsonbDateFormatter(builder);
        if (JsonbDateFormat.TIME_IN_MILLIS.equals(formatter.getFormat())) {
            return (value, context, type) -> fromInstant(Instant.ofEpochMilli(Long.parseLong(value)));
        } else if (formatter.getDateTimeFormatter() != null) {
            return (value, context, type) -> parseWithFormatterInternal(value, formatter.getDateTimeFormatter());
        } else {
            DateTimeFormatter configDateTimeFormatter = builder.getConfigProperties()
                    .getConfigDateFormatter().getDateTimeFormatter();
            if (configDateTimeFormatter != null) {
                return (value, context, type) -> parseWithFormatterInternal(value, configDateTimeFormatter);
            }
        }
        final boolean strictIJson = builder.getConfigProperties().isStrictIJson();
        if (strictIJson) {
            return (value, context, type) -> parseWithFormatterInternal(value, JsonbDateFormatter.IJSON_DATE_FORMATTER);
        }
        Locale locale = builder.getConfigProperties().getLocale(formatter.getLocale());
        return (value, context, type) -> {
            try {
                return parseDefault(value, locale);
            } catch (DateTimeException e) {
                throw new JsonbException(Messages.getMessage(MessageKeys.DATE_PARSE_ERROR, value, clazz), e);
            }
        };
    }

    private JsonbDateFormatter getJsonbDateFormatter(TypeDeserializerBuilder builder) {
        return Optional.ofNullable(builder.getCustomization().getDeserializeDateFormatter())
                .orElse(builder.getConfigProperties().getConfigDateFormatter());
    }

    @Override
    public Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return actualDeserializer.deserialize(value, context, rType);
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
            throw new JsonbException(Messages.getMessage(MessageKeys.DATE_PARSE_ERROR, jsonValue, clazz), e);
        }
    }

}
