package org.eclipse.yasson.internal.processor.convertor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class StringConvertor implements TypeConvertor<String> {

    StringConvertor() {
    }

    @Override
    public String serialize(String object, SerializationContextImpl context) {
        return processString(object, context.getJsonbContext().getConfig());
    }

    @Override
    public String deserialize(String object, DeserializationContextImpl context) {
        return processString(object, context.getJsonbContext().getConfig());
    }

    private String processString(String object, JsonbConfig config) {
        if ((boolean) config.getProperty(JsonbConfig.STRICT_IJSON).orElse(false)) {
            String newString = new String(object.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            if (!newString.equals(object)) {
                throw new JsonbException(Messages.getMessage(MessageKeys.UNPAIRED_SURROGATE));
            }
        }
        return object;
    }
}
