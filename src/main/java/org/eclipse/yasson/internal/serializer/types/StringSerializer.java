package org.eclipse.yasson.internal.serializer.types;

import java.nio.charset.StandardCharsets;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class StringSerializer extends TypeSerializer<String> {

    StringSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(String value, JsonGenerator generator, SerializationContextImpl context) {
        JsonbConfigProperties configProperties = context.getJsonbContext().getConfigProperties();
        if (configProperties.isStrictIJson()) {
            String newString = new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            if (!newString.equals(value)) {
                throw new JsonbException(Messages.getMessage(MessageKeys.UNPAIRED_SURROGATE));
            }
        }
        generator.write(value);
    }

}
