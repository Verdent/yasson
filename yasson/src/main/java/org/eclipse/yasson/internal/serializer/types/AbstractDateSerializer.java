package org.eclipse.yasson.internal.serializer.types;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.JsonbDateFormatter;

/**
 * TODO javadoc
 */
abstract class AbstractDateSerializer<T> extends TypeSerializer<T> {

    static final ZoneId UTC = ZoneId.of("UTC");

    private final Function<T, String> valueSerializer;

    AbstractDateSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
        valueSerializer = valueSerializer(serializerBuilder);
    }

    private Function<T, String> valueSerializer(TypeSerializerBuilder serializerBuilder) {
        Customization customization = serializerBuilder.getCustomization();
        JsonbConfigProperties properties = serializerBuilder.getJsonbContext().getConfigProperties();
        final JsonbDateFormatter formatter = getJsonbDateFormatter(properties, customization);
        if (JsonbDateFormat.TIME_IN_MILLIS.equals(formatter.getFormat())) {
            return value -> String.valueOf(toInstant(value).toEpochMilli());
        } else if (formatter.getDateTimeFormatter() != null) {
            DateTimeFormatter dateTimeFormatter = formatter.getDateTimeFormatter();
            return value -> formatWithFormatter(value, dateTimeFormatter);
        } else {
            DateTimeFormatter configDateTimeFormatter = properties.getConfigDateFormatter().getDateTimeFormatter();
            if (configDateTimeFormatter != null) {
                return value -> formatWithFormatter(value, configDateTimeFormatter);
            }
        }
        if (properties.isStrictIJson()) {
            return this::formatStrictIJson;
        }
        Locale locale = properties.getLocale(formatter.getLocale());
        return value -> formatDefault(value, locale);
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
    protected TemporalAccessor toTemporalAccessor(T value) {
        return (TemporalAccessor) value;
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
        generator.write(valueSerializer.apply(value));
    }

    @Override
    void serializeKey(T key, JsonGenerator generator, SerializationContextImpl context) {
        generator.writeKey(valueSerializer.apply(key));
    }
}
