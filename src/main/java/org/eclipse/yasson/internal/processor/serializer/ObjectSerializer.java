package org.eclipse.yasson.internal.processor.serializer;

import java.util.LinkedHashMap;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class ObjectSerializer implements ModelSerializer {

    private final LinkedHashMap<String, ModelSerializer> propertySerializers;


    ObjectSerializer(LinkedHashMap<String, ModelSerializer> propertySerializers) {
        this.propertySerializers = propertySerializers;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        generator.writeStartObject();
        propertySerializers.forEach((key, serializer) -> {
            try {
                context.setKey(key);
                serializer.serialize(value, generator, context);
            } catch (Exception e) {
                throw new JsonbException(Messages.getMessage(MessageKeys.SERIALIZE_PROPERTY_ERROR, key,
                                                             value.getClass().getCanonicalName()), e);
            }
        });
        generator.writeEnd();
    }
}
