package org.eclipse.yasson.internal.processor.serializer;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ReflectionUtils;

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
