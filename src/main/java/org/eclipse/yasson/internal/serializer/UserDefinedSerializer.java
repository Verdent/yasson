package org.eclipse.yasson.internal.serializer;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.YassonGenerator;

/**
 * TODO javadoc
 */
class UserDefinedSerializer<T> implements ModelSerializer {

    private final JsonbSerializer<T> userDefinedSerializer;

    UserDefinedSerializer(JsonbSerializer<T> userDefinedSerializer) {
        this.userDefinedSerializer = userDefinedSerializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        YassonGenerator yassonGenerator = new YassonGenerator(generator);
        userDefinedSerializer.serialize((T) value, yassonGenerator, context);
    }

}