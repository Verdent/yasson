package org.eclipse.yasson.internal.serializer.types;

import java.util.OptionalDouble;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.serializer.ModelSerializer;

/**
 * TODO javadoc
 */
class OptionalDoubleSerializer implements ModelSerializer {

    private final ModelSerializer typeSerializer;

    OptionalDoubleSerializer(ModelSerializer typeSerializer) {
        this.typeSerializer = typeSerializer;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        OptionalDouble optionalDouble = (OptionalDouble) value;
        if (optionalDouble.isPresent()) {
            typeSerializer.serialize(optionalDouble.getAsDouble(), generator, context);
        } else {
            typeSerializer.serialize(null, generator, context);
        }
    }
}
