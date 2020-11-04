package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.UserParser;

/**
 * TODO javadoc
 */
public class UserDefinedDeserializer implements ModelDeserializer<JsonParser> {

    private final JsonbDeserializer<?> userDefinedDeserializer;

    public UserDefinedDeserializer(JsonbDeserializer<?> userDefinedDeserializer) {
        this.userDefinedDeserializer = userDefinedDeserializer;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        UserParser userParser = new UserParser(value);
        Object object = userDefinedDeserializer.deserialize(userParser, context, rType);
        userParser.skipRemaining();
        return object;
    }

}
