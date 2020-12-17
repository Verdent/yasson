package org.eclipse.yasson.internal.serializer;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * TODO javadoc
 */
class CyclicReferenceSerializer implements ModelSerializer {

    private final Type type;
    private ModelSerializer delegate;

    CyclicReferenceSerializer(Type type) {
        this.type = type;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        if (delegate == null) {
            delegate = context.getJsonbContext().getSerializationModelCreator().serializerChain(type, true);
        }
        delegate.serialize(value, generator, context);
    }
}
