package org.eclipse.yasson.internal.serializer;

import java.util.Collection;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * TODO javadoc
 */
class PolymorphicArrayWrapperSerializer implements ModelSerializer {

    private final String typeValue;
    private final ModelSerializer delegate;

    PolymorphicArrayWrapperSerializer(String typeValue, ModelSerializer delegate) {
        this.typeValue = typeValue;
        this.delegate = delegate;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        generator.writeStartArray();
        generator.write(typeValue);
        delegate.serialize(value, generator, context);
        generator.writeEnd();
    }

}
