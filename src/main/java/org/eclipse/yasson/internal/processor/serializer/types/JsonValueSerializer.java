package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class JsonValueSerializer extends TypeSerializer<JsonValue> {

    JsonValueSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(JsonValue value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value);
    }

}
