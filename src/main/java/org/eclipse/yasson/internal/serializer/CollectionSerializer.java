package org.eclipse.yasson.internal.serializer;

import java.util.Collection;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * TODO javadoc
 */
class CollectionSerializer implements ModelSerializer {

    private final ModelSerializer delegate;

    CollectionSerializer(ModelSerializer delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        Collection<Object> collection = (Collection<Object>) value;
        generator.writeStartArray();
        collection.forEach(object -> delegate.serialize(object, generator, context));
        generator.writeEnd();
    }

}
