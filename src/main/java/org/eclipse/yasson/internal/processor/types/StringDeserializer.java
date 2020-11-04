package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class StringDeserializer extends TypeDeserializer {

    StringDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    public Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        JsonbConfigProperties config = context.getJsonbContext().getConfigProperties();
        return getDelegate().deserialize(checkIJson(value, config), context, rType);
    }

    private String checkIJson(String value, JsonbConfigProperties config) {
        if (config.isStrictIJson()) {
            String newString = new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            if (!newString.equals(value)) {
                throw new JsonbException(Messages.getMessage(MessageKeys.UNPAIRED_SURROGATE));
            }
        }
        return value;
    }
}
