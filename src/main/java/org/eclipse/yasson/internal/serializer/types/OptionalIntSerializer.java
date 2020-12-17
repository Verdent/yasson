package org.eclipse.yasson.internal.serializer.types;

import java.util.OptionalInt;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.serializer.ModelSerializer;

/**
 * TODO javadoc
 */
class OptionalIntSerializer implements ModelSerializer {

    private final ModelSerializer typeSerializer;

    OptionalIntSerializer(ModelSerializer typeSerializer) {
        this.typeSerializer = typeSerializer;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        OptionalInt optionalInt = (OptionalInt) value;
        if (optionalInt.isPresent()) {
            typeSerializer.serialize(optionalInt.getAsInt(), generator, context);
        } else {
            typeSerializer.serialize(null, generator, context);
        }
    }
}
