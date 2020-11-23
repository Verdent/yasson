package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.JustReturn;

/**
 * TODO javadoc
 */
public class SqlDateDeserializer extends AbstractDateDeserializer<Date> implements JsonbDeserializer<Date> {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_DATE.withZone(UTC);

    SqlDateDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    public SqlDateDeserializer() {
        super(Date.class);
    }

    @Override
    protected Date fromInstant(Instant instant) {
        return new Date(instant.toEpochMilli());
    }

    @Override
    protected Date parseDefault(String jsonValue, Locale locale) {
        return Date.valueOf(LocalDate.parse(jsonValue, DEFAULT_FORMATTER.withLocale(locale)));
    }

    @Override
    protected Date parseWithFormatter(String jsonValue, DateTimeFormatter formatter) {
        return Date.valueOf(LocalDate.parse(jsonValue, formatter));
    }

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        DeserializationContextImpl context = (DeserializationContextImpl) ctx;
        return (Date) deserialize(parser.getString(), context);
    }
}
