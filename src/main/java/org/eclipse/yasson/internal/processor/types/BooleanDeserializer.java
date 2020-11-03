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
        switch (context.getLastValueEvent()) {
        case VALUE_FALSE:
            return Boolean.FALSE;
        case VALUE_TRUE:
            return Boolean.TRUE;
        default:
            return Boolean.parseBoolean(value);
        }
    }

}
