package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.sql.Date;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.JustReturn;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;
import org.eclipse.yasson.internal.serializer.JsonbDateFormatter;

/**
 * TODO javadoc
 */
abstract class AbstractDateDeserializer<T> extends TypeDeserializer {

    static final ZoneId UTC = ZoneId.of("UTC");

    private ModelDeserializer<String> actualDeserializer;

    AbstractDateDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
        this.actualDeserializer = actualDeserializer(builder.getConfigProperties(), builder.getCustomization());
    }

    protected AbstractDateDeserializer(Class<Date> clazz) {
        super(new TypeDeserializerBuilder(clazz,
                                    null,
                                    null,
                                    JustReturn.create()));
        this.actualDeserializer = null;
    }

    private ModelDeserializer<String> actualDeserializer(JsonbConfigProperties properties, Customization customization) {
        final JsonbDateFormatter formatter = getJsonbDateFormatter(properties, customization);
        if (JsonbDateFormat.TIME_IN_MILLIS.equals(formatter.getFormat())) {
            return (value, context) -> fromInstant(Instant.ofEpochMilli(Long.parseLong(value)));
        } else if (formatter.getDateTimeFormatter() != null) {
            return (value, context) -> parseWithFormatterInternal(value, formatter.getDateTimeFormatter());
        } else {
            DateTimeFormatter configDateTimeFormatter = properties.getConfigDateFormatter().getDateTimeFormatter();
            if (configDateTimeFormatter != null) {
                return (value, context) -> parseWithFormatterInternal(value, configDateTimeFormatter);
            }
        }
        if (properties.isStrictIJson()) {
            return (value, context) -> parseWithFormatterInternal(value, JsonbDateFormatter.IJSON_DATE_FORMATTER);
        }
        Locale locale = properties.getLocale(formatter.getLocale());
        return (value, context) -> {
            try {
                return parseDefault(value, locale);
            } catch (DateTimeException e) {
                throw new JsonbException(Messages.getMessage(MessageKeys.DATE_PARSE_ERROR, value, getType()), e);
            }
        };
    }

    private JsonbDateFormatter getJsonbDateFormatter(JsonbConfigProperties properties, Customization customization) {
        return Optional.ofNullable(customization.getDeserializeDateFormatter())
                .orElse(properties.getConfigDateFormatter());
    }

    @Override
    public Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        if (actualDeserializer == null) {
            actualDeserializer = actualDeserializer(context.getJsonbContext().getConfigProperties(), context.getCustomization());
        }
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
