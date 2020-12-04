package org.eclipse.yasson.internal.processor.serializer.types;

import java.util.OptionalLong;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.processor.serializer.ModelSerializer;

/**
 * TODO javadoc
 */
class OptionalLongSerializer implements ModelSerializer {

    private final ModelSerializer typeSerializer;

    OptionalLongSerializer(ModelSerializer typeSerializer) {
        this.typeSerializer = typeSerializer;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        OptionalLong optionalLong = (OptionalLong) value;
        if (optionalLong.isPresent()) {
            typeSerializer.serialize(optionalLong.getAsLong(), generator, context);
        } else {
            typeSerializer.serialize(null, generator, context);
        }
    }
}
