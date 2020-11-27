package org.eclipse.yasson.internal.processor.deserializer.types;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.Function;

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
        Locale locale = builder.getConfigProperties().getLocale(numberFormat.getLocale());
        final NumberFormat format = NumberFormat.getInstance(locale);
        ((DecimalFormat) format).applyPattern(numberFormat.getFormat());
        format.setParseIntegerOnly(integerOnly);
        Function<String, String> valueChanger = createCompatibilityValueChanger(locale);
        return (value, context) -> {
            try {
                String updated = valueChanger.apply(value);
                return parseNumberValue(String.valueOf(format.parse(updated)));
            } catch (ParseException e) {
                throw new JsonbException(Messages.getMessage(MessageKeys.PARSING_NUMBER, value, numberFormat.getFormat()));
            }
        };
    }

    private Function<String, String> createCompatibilityValueChanger(Locale locale) {
        char beforeJdk13GroupSeparator = '\u00A0';
        char frenchGroupingSeparator = DecimalFormatSymbols.getInstance(Locale.FRENCH).getGroupingSeparator();
        if (locale.getLanguage().equals(Locale.FRENCH.getLanguage()) && beforeJdk13GroupSeparator != frenchGroupingSeparator) {
            //JDK-8225245
            return value -> value.replace(beforeJdk13GroupSeparator, frenchGroupingSeparator);
        }
        return value -> value;
    }

    abstract T parseNumberValue(String value);

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return actualDeserializer.deserialize(value, context);
    }

}
