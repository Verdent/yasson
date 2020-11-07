package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.serializer.ResolvedParameterizedType;

/**
 * TODO javadoc
 */
public class DynamicAdapterResolver implements ModelDeserializer<JsonParser> {

    private final ParameterizedType type;
    private final ModelDeserializer<Object> adaptedValueWriter;

    public DynamicAdapterResolver(ParameterizedType type,
                                  ModelDeserializer<Object> adaptedValueWriter) {
        this.type = type;
        this.adaptedValueWriter = adaptedValueWriter;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
//        Type resolved = rType instanceof ResolvedParameterizedType ? rType : ReflectionUtils.resolveTypeArguments(type, rType);

        Type resolved = type.getRawType().equals(ReflectionUtils.getRawType(rType))
                ? rType
                : ReflectionUtils.resolveTypeArguments(type, rType) ;
        DeserializationContextImpl newContext = new DeserializationContextImpl(context);
        ClassModel targetModel = context.getJsonbContext().getMappingContext()
                .getOrCreateClassModel(ReflectionUtils.getRawType(resolved));
        ModelDeserializer<JsonParser> deserializer = context.getJsonbContext().getChainModelCreator()
                .deserializerChain(resolved, targetModel);
        Object object = deserializer.deserialize(value, newContext, resolved);
        return adaptedValueWriter.deserialize(object, context, resolved);
    }

}
