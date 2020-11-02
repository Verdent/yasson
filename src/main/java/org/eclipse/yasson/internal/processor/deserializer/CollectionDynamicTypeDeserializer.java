package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.MappingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.ChainModelCreator;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertor;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertors;

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
        TypeConvertor<?> convertor = TypeConvertors.getConvertor(ReflectionUtils.getRawType(clazz));
        if (convertor != null) {
            return new ValueExtractor(new TypeDeserializer(JustReturn.create(), convertor));
        }
        MappingContext mappingContext = context.getMappingContext();
        return context.getJsonbContext().getChainModelCreator()
                .deserializerChain(mappingContext.getOrCreateClassModel(ReflectionUtils.getRawType(clazz)));
    }

}
