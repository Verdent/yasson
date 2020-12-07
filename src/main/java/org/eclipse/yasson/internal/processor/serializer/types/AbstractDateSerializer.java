package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Optional;

import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.processor.serializer.ModelSerializer;
import org.eclipse.yasson.internal.serializer.JsonbDateFormatter;

/**
 * TODO javadoc
 */
abstract class AbstractDateSerializer<T> extends TypeSerializer<T> {

    static final ZoneId UTC = ZoneId.of("UTC");

    private final ModelSerializer actualSerializer;

    AbstractDateSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
        actualSerializer = actualSerializer(serializerBuilder);
    }

    @SuppressWarnings("unchecked")
    private ModelSerializer actualSerializer(TypeSerializerBuilder serializerBuilder) {
        Customization customization = serializerBuilder.getCustomization();
        JsonbConfigProperties properties = serializerBuilder.getJsonbContext().getConfigProperties();
        final JsonbDateFormatter formatter = getJsonbDateFormatter(properties, customization);
        if (JsonbDateFormat.TIME_IN_MILLIS.equals(formatter.getFormat())) {
            return (value, generator, context) -> generator.write(String.valueOf(toInstant((T) value).toEpochMilli()));
        } else if (formatter.getDateTimeFormatter() != null) {
            DateTimeFormatter dateTimeFormatter = formatter.getDateTimeFormatter();
            return (value, generator, context) -> generator.write(formatWithFormatter((T) value, dateTimeFormatter));
        } else {
            DateTimeFormatter configDateTimeFormatter = properties.getConfigDateFormatter().getDateTimeFormatter();
            if (configDateTimeFormatter != null) {
                return (value, generator, context) -> generator.write(formatWithFormatter((T) value, configDateTimeFormatter));
            }
        }
        if (properties.isStrictIJson()) {
            return (value, generator, context) -> generator.write(formatStrictIJson((T) value));
        }
        Locale locale = properties.getLocale(formatter.getLocale());
        return (value, generator, context) -> generator.write(formatDefault((T) value, locale));
    }

    private JsonbDateFormatter getJsonbDateFormatter(JsonbConfigProperties properties, Customization customization) {
        return Optional.ofNullable(customization.getSerializeDateFormatter())
                .orElse(properties.getConfigDateFormatter());
    }

    /**
     * Convert date object to {@link TemporalAccessor}
     *
     * Only for legacy dates.
     *
     * @param value date object
     * @return converted {@link TemporalAccessor}
     */
    protected TemporalAccessor toTemporalAccessor(T object) {
        return (TemporalAccessor) object;
    }

    /**
     * Convert java.time object to epoch milliseconds instant. Discards zone offset and zone id information.
     *
     * @param value date object to convert
     * @return instant
     */
    protected abstract Instant toInstant(T value);

    /**
     * Format with default formatter for a given java.time date object.
     * Different default formatter for each date object type is used.
     *
     * @param value  date object
     * @param locale locale from annotation / default not null
     * @return formatted date obj as string
     */
    protected abstract String formatDefault(T value, Locale locale);

    /**
     * Format date object with given formatter.
     *
     * @param value     date object to format
     * @param formatter formatter to format with
     * @return formatted result
     */
    protected String formatWithFormatter(T value, DateTimeFormatter formatter) {
        return formatter.format(toTemporalAccessor(value));
    }

    /**
     * Format date object as strict IJson date format.
     *
     * @param value value to format
     * @return formatted result
     */
    protected String formatStrictIJson(T value) {
        return JsonbDateFormatter.IJSON_DATE_FORMATTER.format(toTemporalAccessor(value));
    }


    /**
     * Append UTC zone in case zone is not set on formatter.
     *
     * @param formatter formatter
     * @return zoned formatter
     */
    protected DateTimeFormatter getZonedFormatter(DateTimeFormatter formatter) {
        return formatter.getZone() != null
                ? formatter
                : formatter.withZone(UTC);
    }

    @Override
    void serializeValue(T value, JsonGenerator generator, SerializationContextImpl context) {
        actualSerializer.serialize(value, generator, context);
    }
}
