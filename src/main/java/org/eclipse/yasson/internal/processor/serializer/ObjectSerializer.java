package org.eclipse.yasson.internal.processor.serializer;

import java.util.LinkedList;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class ObjectSerializer implements ModelSerializer {

    private final LinkedList<ModelSerializer> propertySerializers;

    ObjectSerializer(LinkedList<ModelSerializer> propertySerializers) {
        this.propertySerializers = propertySerializers;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        boolean previous = context.isContainerWithNulls();
        context.setContainerWithNulls(false);
        generator.writeStartObject();
        propertySerializers.forEach(modelSerializer -> modelSerializer.serialize(value, generator, context));
        generator.writeEnd();
        context.setContainerWithNulls(previous);
    }
}
