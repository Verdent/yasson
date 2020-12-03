package org.eclipse.yasson.internal.processor.serializer.types;

import java.time.Instant;
import java.time.ZoneId;
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
            return (value, generator, context) -> formatWithFormatter(value, generator, dateTimeFormatter);
        } else {
            DateTimeFormatter configDateTimeFormatter = properties.getConfigDateFormatter().getDateTimeFormatter();
            if (configDateTimeFormatter != null) {
                return (value, generator, context) -> formatWithFormatter(value, generator, configDateTimeFormatter);
            }
        }
        if (properties.isStrictIJson()) {
            return (value, generator, context) -> formatWithFormatter(value, generator, JsonbDateFormatter.IJSON_DATE_FORMATTER);
        }
        Locale locale = properties.getLocale(formatter.getLocale());
        return (value, generator, context) -> formatWithFormatter(value, generator, defaultFormatter((T) value, locale));
    }

    @SuppressWarnings("unchecked")
    private void formatWithFormatter(Object value,
                                     JsonGenerator generator,
                                     DateTimeFormatter dateTimeFormatter) {
        DateTimeFormatter formatter = updateFormatter(dateTimeFormatter);
        generator.write(formatter.format(toTemporalAccessor((T) value)));
    }

    /**
     * Convert java.time object to epoch milliseconds instant. Discards zone offset and zone id information.
     *
     * @param value date object to convert
     * @return instant
     */
    protected abstract Instant toInstant(T value);

    /**
     * Convert date object to {@link TemporalAccessor}
     *
     * Only for legacy dates.
     *
     * @param value date object
     * @return converted {@link TemporalAccessor}
     */
    protected abstract TemporalAccessor toTemporalAccessor(T value);

    /**
     * Return default formatter for the specific type.
     *
     * @param value date object
     * @param locale locale for formatter
     * @return default formatter
     */
    protected abstract DateTimeFormatter defaultFormatter(T value, Locale locale);

    /**
     * Add additional formatter configuration if needed.
     *
     * @param formatter formatter to be changed
     * @return updated formatter
     */
    protected DateTimeFormatter updateFormatter(DateTimeFormatter formatter) {
        return formatter;
    }

    private JsonbDateFormatter getJsonbDateFormatter(JsonbConfigProperties properties, Customization customization) {
        return Optional.ofNullable(customization.getSerializeDateFormatter())
                .orElse(properties.getConfigDateFormatter());
    }

    @Override
    void serializeValue(T value, JsonGenerator generator, SerializationContextImpl context) {
        actualSerializer.serialize(value, generator, context);
    }
}
