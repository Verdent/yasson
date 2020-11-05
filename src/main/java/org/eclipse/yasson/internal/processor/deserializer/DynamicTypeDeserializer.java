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

    private static final Map<Type, Map<Type, ResolvedTypeHolder>> resolvedTypeCache = new ConcurrentHashMap<>();

    private final ModelDeserializer<Object> delegate;
    private final Type unresolvedType;
    private final boolean isTypeVariable;
    private final Customization customization;

    public DynamicTypeDeserializer(ModelDeserializer<Object> delegate,
                                   Type unresolvedType,
                                   Customization customization) {
        this.delegate = delegate;
        this.unresolvedType = unresolvedType;
        this.isTypeVariable = unresolvedType instanceof TypeVariable;
        this.customization = customization;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        ResolvedTypeHolder holder = resolvedTypeCache
                .computeIfAbsent(rType, type -> new ConcurrentHashMap<>())
                .computeIfAbsent(unresolvedType, type -> createHolderObject(rType, context));

        DeserializationContextImpl newContext = new DeserializationContextImpl(context);
        return delegate
                .deserialize(holder.getDeserializer().deserialize(value, newContext, holder.getResolvedType()), context, rType);
    }

    private ResolvedTypeHolder createHolderObject(Type rType, DeserializationContextImpl context) {
        Type resolvedType = resolveType(rType, context);
        ModelDeserializer<JsonParser> deserializer = createDeserializer(resolvedType, context);
        return new ResolvedTypeHolder(deserializer, resolvedType);
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

    private Type resolveType(Type rType, DeserializationContextImpl context) {
        if (isTypeVariable) {
            return resolveTypeVariable(rType, (TypeVariable<?>) unresolvedType, context);
        }
        return resolveMostSpecificBound((WildcardType) unresolvedType, rType, context);
    }

    private Type resolveTypeVariable(Type rType,
                                     TypeVariable<?> unresolvedType,
                                     DeserializationContextImpl context) {
        //        return new VariableTypeInheritanceSearch().searchParametrizedType(rType, unresolvedType);
        List<Type> chain = context.getRtypeChain();
        Type returnType = unresolvedType;
        for (int i = chain.size() - 1; i >= 0; i--) {
            Type type = chain.get(i);
            returnType = new VariableTypeInheritanceSearch().searchParametrizedType(type, (TypeVariable<?>) returnType);
            if (!(returnType instanceof TypeVariable)) {
                break;
            }
        }
        if (returnType instanceof TypeVariable) {
            throw new JsonbException("Could not resolve: " + unresolvedType);
        }
        //        for (Type type : chain) {
        //            if (ReflectionUtils.getRawType(type) != unresolvedType.getGenericDeclaration()) {
        //                continue;
        //            }
        //            return new VariableTypeInheritanceSearch().searchParametrizedType(type, unresolvedType);
        //        }
        return returnType;
    }

    private Type resolveMostSpecificBound(WildcardType wildcardType, Type rType, DeserializationContextImpl context) {
        Class<?> result = Object.class;
        for (Type upperBound : wildcardType.getUpperBounds()) {
            result = getMostSpecificBound(result, upperBound, rType, context);
        }
        for (Type lowerBound : wildcardType.getLowerBounds()) {
            result = getMostSpecificBound(result, lowerBound, rType, context);
        }
        return result;
    }

    private Class<?> getMostSpecificBound(Class<?> result,
                                          Type bound,
                                          Type rType,
                                          DeserializationContextImpl context) {
        if (bound == Object.class) {
            return result;
        }
        //if bound is type variable search recursively for wrapper generic expansion
        Type resolvedBoundType = bound instanceof TypeVariable
                ? resolveTypeVariable(rType, (TypeVariable<?>) bound, context)
                : bound;
        Class<?> boundRawType = ReflectionUtils.getRawType(resolvedBoundType);
        //resolved class is a subclass of a result candidate
        if (result.isAssignableFrom(boundRawType)) {
            result = boundRawType;
        }
        return result;
    }

    private static final class ResolvedTypeHolder {

        private final ModelDeserializer<JsonParser> deserializer;
        private final Type resolvedType;

        private ResolvedTypeHolder(ModelDeserializer<JsonParser> deserializer, Type resolvedType) {
            this.deserializer = Objects.requireNonNull(deserializer);
            this.resolvedType = Objects.requireNonNull(resolvedType);
        }

        public Type getResolvedType() {
            return resolvedType;
        }

        public ModelDeserializer<JsonParser> getDeserializer() {
            return deserializer;
        }
    }
}
