package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.MappingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.VariableTypeInheritanceSearch;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.ModelCreator;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertor;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertors;

/**
 * TODO javadoc
 */
public class DynamicTypeDeserializer implements ModelDeserializer<JsonParser> {

    private static final Map<Type, ResolvedTypeHolder> resolvedTypeCache = new ConcurrentHashMap<>();

    private final ModelDeserializer<Object> delegate;
    private final Type unresolvedType;
    private final boolean isTypeVariable;

    public DynamicTypeDeserializer(ModelDeserializer<Object> delegate,
                                   Type unresolvedType) {
        this.delegate = delegate;
        this.unresolvedType = unresolvedType;
        this.isTypeVariable = unresolvedType instanceof TypeVariable;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        ModelDeserializer<JsonParser> modelDeserializer = resolvedTypeCache
                .computeIfAbsent(rType, type -> new ResolvedTypeHolder())
                .getDeserializersForType()
                .computeIfAbsent(unresolvedType, type -> createDeserializer(rType, context));

        DeserializationContextImpl newContext = new DeserializationContextImpl(context);
        return delegate.deserialize(modelDeserializer.deserialize(value, newContext, rType), context, rType);
    }


    private ModelDeserializer<JsonParser> createDeserializer(Type rType,
                                                             DeserializationContextImpl context) {

        Type clazz = resolveType(rType, context);
        TypeConvertor<?> convertor = TypeConvertors.getConvertor(ReflectionUtils.getRawType(clazz));
        if (convertor != null) {
            return new ValueExtractor(new TypeDeserializer(JustReturn.create(), convertor));
        }
        MappingContext mappingContext = context.getMappingContext();
        return ModelCreator.getOrCreateProcessorChain(mappingContext.getClassModel(ReflectionUtils.getRawType(clazz)),
                                                      mappingContext);
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
        return new VariableTypeInheritanceSearch().searchParametrizedType(rType, unresolvedType);
//        for (Type type : context.getRtypeChain()) {
//            if (ReflectionUtils.getRawType(type) != unresolvedType.getGenericDeclaration()) {
//                continue;
//            }
//            return new VariableTypeInheritanceSearch().searchParametrizedType(type, unresolvedType);
//        }
//        return null;
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
        Type resolvedBoundType = bound instanceof TypeVariable ? resolveTypeVariable(rType, (TypeVariable<?>) bound, context) : bound;
        Class<?> boundRawType = ReflectionUtils.getRawType(resolvedBoundType);
        //resolved class is a subclass of a result candidate
        if (result.isAssignableFrom(boundRawType)) {
            result = boundRawType;
        }
        return result;
    }

    private static final class ResolvedTypeHolder {

        private final Map<Type, ModelDeserializer<JsonParser>> deserializersForType = new ConcurrentHashMap<>();

        public Map<Type, ModelDeserializer<JsonParser>> getDeserializersForType() {
            return deserializersForType;
        }
    }
}
