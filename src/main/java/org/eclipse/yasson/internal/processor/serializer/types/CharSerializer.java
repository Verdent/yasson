package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class CharSerializer extends TypeSerializer<Character> {

    CharSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(Character value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(String.valueOf(value));
    }

}
