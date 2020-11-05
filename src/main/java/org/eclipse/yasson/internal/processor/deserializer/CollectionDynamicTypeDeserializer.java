package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.MappingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.types.TypeDeserializers;

/**
 * TODO javadoc
 */
public class CollectionDynamicTypeDeserializer implements ModelDeserializer<JsonParser> {

    private static final Map<Type, ModelDeserializer<JsonParser>> resolvedTypeCache = new ConcurrentHashMap<>();

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {

        ModelDeserializer<JsonParser> modelDeserializer = resolvedTypeCache
                .computeIfAbsent(rType, type -> createDeserializer(rType, context));

        DeserializationContextImpl newContext = new DeserializationContextImpl(context);
        return modelDeserializer.deserialize(value, newContext, rType);
    }


    private ModelDeserializer<JsonParser> createDeserializer(Type clazz, DeserializationContextImpl context) {
        ModelDeserializer<JsonParser> typeDeserializer = TypeDeserializers.getTypeDeserializer(ReflectionUtils.getRawType(clazz),
                                                                                           Customization.empty(),
                                                                                           context.getJsonbContext()
                                                                                                   .getConfigProperties(),
                                                                                           JustReturn.create());
        if (typeDeserializer != null) {
            return typeDeserializer;
        }
        MappingContext mappingContext = context.getMappingContext();
        return context.getJsonbContext().getChainModelCreator()
                .deserializerChain(clazz, mappingContext.getOrCreateClassModel(ReflectionUtils.getRawType(clazz)));
    }

}
