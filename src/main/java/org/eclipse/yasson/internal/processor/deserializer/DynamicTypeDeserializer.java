package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.MappingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.VariableTypeInheritanceSearch;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.types.TypeDeserializers;

/**
 * TODO javadoc
 */
public class DynamicTypeDeserializer implements ModelDeserializer<JsonParser> {

    private static final Map<Type, Map<Type, ModelDeserializer<JsonParser>>> CACHE = new ConcurrentHashMap<>();

    private final ModelDeserializer<Object> delegate;
    private final Type unresolvedType;
    private final Customization customization;

    public DynamicTypeDeserializer(ModelDeserializer<Object> delegate,
                                   Type unresolvedType,
                                   Customization customization) {
        this.delegate = delegate;
        this.unresolvedType = unresolvedType;
        this.customization = customization;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        Type resolvedType = ReflectionUtil.resolveType(unresolvedType, context);
        ModelDeserializer<JsonParser> deserializer = CACHE
                .computeIfAbsent(unresolvedType, type -> new ConcurrentHashMap<>())
                .computeIfAbsent(resolvedType, type -> createDeserializer(type, context));

        DeserializationContextImpl newContext = new DeserializationContextImpl(context);
        return delegate.deserialize(deserializer.deserialize(value, newContext, resolvedType), context, rType);
    }

    private ModelDeserializer<JsonParser> createDeserializer(Type resolvedType,
                                                             DeserializationContextImpl context) {
        Class<?> rawType = ReflectionUtils.getRawType(resolvedType);
        ModelDeserializer<JsonParser> typeDeserializer = TypeDeserializers.getTypeDeserializer(rawType,
                                                                                           customization,
                                                                                           context.getJsonbContext()
                                                                                                   .getConfigProperties(),
                                                                                           JustReturn.create());
        if (typeDeserializer != null) {
            return typeDeserializer;
        }
        MappingContext mappingContext = context.getMappingContext();
        return context.getJsonbContext().getChainModelCreator()
                .deserializerChain(resolvedType, mappingContext.getOrCreateClassModel(rawType));
    }

}
