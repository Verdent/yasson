package org.eclipse.yasson.internal.serializer.types;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.serializer.ModelSerializer;
import org.eclipse.yasson.internal.JsonbNumberFormatter;

/**
 * TODO javadoc
 */
abstract class AbstractNumberSerializer<T> extends TypeSerializer<T> {

    private final ModelSerializer actualSerializer;

    AbstractNumberSerializer(TypeSerializerBuilder builder) {
        super(builder);
        actualSerializer = actualSerializer(builder.getCustomization(), builder.getJsonbContext());
    }

    @SuppressWarnings("unchecked")
    private ModelSerializer actualSerializer(Customization customization, JsonbContext jsonbContext) {
        JsonbNumberFormatter formatter = customization.getSerializeNumberFormatter();
        if (formatter == null) {
            return (value, generator, context) -> writeValue((T) value, generator);
        }
        final NumberFormat format = NumberFormat
                .getInstance(jsonbContext.getConfigProperties().getLocale(formatter.getLocale()));
        ((DecimalFormat) format).applyPattern(formatter.getFormat());
        return (value, generator, context) -> generator.write(format.format(value));
    }

    @Override
    void serializeValue(T value, JsonGenerator generator, SerializationContextImpl context) {
        actualSerializer.serialize(value, generator, context);
    }

    abstract void writeValue(T value, JsonGenerator generator);

}
