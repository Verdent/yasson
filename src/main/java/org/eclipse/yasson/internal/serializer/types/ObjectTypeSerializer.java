package org.eclipse.yasson.internal.serializer.types;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.serializer.ModelSerializer;
import org.eclipse.yasson.internal.serializer.SerializationModelCreator;

/**
 * TODO javadoc
 */
public class ObjectTypeSerializer extends TypeSerializer<Object> {

    private final Customization customization;

    private final Map<Class<?>, ModelSerializer> cache;
    private final List<Type> chain;
    private final boolean isKey;

    ObjectTypeSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
        this.customization = serializerBuilder.getCustomization();
        this.cache = new ConcurrentHashMap<>();
        this.chain = new LinkedList<>(serializerBuilder.getChain());
        this.isKey = serializerBuilder.isKey();
    }

    @Override
    void serializeValue(Object value, JsonGenerator generator, SerializationContextImpl context) {
        //Dynamically resolved type during runtime. Cached in SerializationModelCreator.
        findSerializer(value, generator, context);
    }

    @Override
    void serializeKey(Object key, JsonGenerator generator, SerializationContextImpl context) {
        if (key == null) {
            super.serializeKey(null, generator, context);
            return;
        }
        //Dynamically resolved type during runtime. Cached in SerializationModelCreator.
        findSerializer(key, generator, context);
    }

    private void findSerializer(Object key, JsonGenerator generator, SerializationContextImpl context) {
        Class<?> clazz = key.getClass();
        cache.computeIfAbsent(clazz, aClass -> {
            SerializationModelCreator serializationModelCreator = context.getJsonbContext().getSerializationModelCreator();
            return serializationModelCreator.serializerChainRuntime(new LinkedList<>(chain), clazz, customization, false, isKey);
        }).serialize(key, generator, context);
    }

    public void addSpecificSerializer(Class<?> clazz, ModelSerializer modelSerializer) {
        cache.put(clazz, modelSerializer);
    }
}
