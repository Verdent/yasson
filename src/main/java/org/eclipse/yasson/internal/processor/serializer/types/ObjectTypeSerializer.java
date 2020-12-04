package org.eclipse.yasson.internal.processor.serializer.types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.json.stream.JsonGenerator;
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
    private final List<Type> chain;

    ObjectTypeSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
        this.customization = serializerBuilder.getCustomization();
        this.cache = new HashMap<>();
        this.chain = new LinkedList<>(serializerBuilder.getChain());
    }

    @Override
    void serializeValue(Object value, JsonGenerator generator, SerializationContextImpl context) {
        //Dynamically resolved type during runtime. Cached in SerializationModelCreator.
        Class<?> clazz = value.getClass();
        cache.computeIfAbsent(clazz, aClass -> {
            SerializationModelCreator serializationModelCreator = context.getJsonbContext().getSerializationModelCreator();
            return serializationModelCreator.serializerChainRuntime(new LinkedList<>(chain), clazz, customization, false);
        }).serialize(value, generator, context);
    }

}
