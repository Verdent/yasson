package org.eclipse.yasson.internal.processor.serializer.types;

import java.util.HashMap;
import java.util.Map;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.processor.serializer.ModelSerializer;
import org.eclipse.yasson.internal.processor.serializer.SerializationModelCreator;

/**
 * TODO javadoc
 */
class ObjectTypeSerializer extends TypeSerializer<Object> {

    private final Customization customization;

    private final Map<Class<?>, ModelSerializer> cache;

    ObjectTypeSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
        this.customization = serializerBuilder.getCustomization();
        this.cache = new HashMap<>();
    }

    @Override
    void serializeValue(Object value, JsonGenerator generator, SerializationContextImpl context) {
        //Dynamically resolved type during runtime. Cached in SerializationModelCreator.
        Class<?> clazz = value.getClass();
        cache.computeIfAbsent(clazz, aClass -> {
            SerializationModelCreator serializationModelCreator = context.getJsonbContext().getSerializationModelCreator();
            return serializationModelCreator.serializerChain(clazz, customization, false);
        }).serialize(value, generator, context);
    }

}
