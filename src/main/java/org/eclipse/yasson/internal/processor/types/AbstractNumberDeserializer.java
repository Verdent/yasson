package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;
import org.eclipse.yasson.internal.serializer.JsonbNumberFormatter;

/**
 * TODO javadoc
 */
abstract class AbstractNumberDeserializer<T extends Number> extends TypeDeserializer {

    private final ModelDeserializer<String> actualDeserializer;
    private final boolean integerOnly;

    AbstractNumberDeserializer(TypeDeserializerBuilder builder, boolean integerOnly) {
        super(builder);
        this.actualDeserializer = actualDeserializer(builder);
        this.integerOnly = integerOnly;
    }

    private ModelDeserializer<String> actualDeserializer(TypeDeserializerBuilder builder) {
        Customization customization = builder.getCustomization();
        if (customization.getDeserializeNumberFormatter() == null) {
            return (value, context) -> {
                try {
                    return parseNumberValue(value);
                } catch (NumberFormatException e) {
                    throw new JsonbException(Messages.getMessage(MessageKeys.DESERIALIZE_VALUE_ERROR, getType()));
                }
            };
        }

        final JsonbNumberFormatter numberFormat = customization.getDeserializeNumberFormatter();
        //consider synchronizing on format instance or per thread cache.
        final NumberFormat format = NumberFormat.getInstance(builder.getConfigProperties().getLocale(numberFormat.getLocale()));
        ((DecimalFormat) format).applyPattern(numberFormat.getFormat());
        format.setParseIntegerOnly(integerOnly);
        return (value, context) -> {
            try {
                return parseNumberValue(String.valueOf(format.parse(value)));
            } catch (ParseException e) {
                throw new JsonbException(Messages.getMessage(MessageKeys.PARSING_NUMBER, value, numberFormat.getFormat()));
            }
        };
    }

    abstract T parseNumberValue(String value);

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return actualDeserializer.deserialize(value, context);
    }

}
