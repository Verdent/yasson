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
class BooleanDeserializer extends TypeDeserializer<Boolean> {

    BooleanDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    public Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        return Boolean.parseBoolean(value);
    }

}
