package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.VariableTypeInheritanceSearch;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class ReflectionUtil {

    static Type resolveType(Type unresolvedType, DeserializationContextImpl context) {
        if (unresolvedType instanceof TypeVariable) {
            return resolveTypeVariable((TypeVariable<?>) unresolvedType, context);
        } else if (unresolvedType instanceof WildcardType) {
            return resolveMostSpecificBound((WildcardType) unresolvedType, context);
        }
        return unresolvedType;
    }

    static Type resolveType(TypeVariable<?> unresolvedType, DeserializationContextImpl context) {
        return resolveTypeVariable(unresolvedType, context);
    }

    static Type resolveType(WildcardType unresolvedType, DeserializationContextImpl context) {
        return resolveMostSpecificBound(unresolvedType, context);
    }

    private static Type resolveTypeVariable(TypeVariable<?> unresolvedType,
                                            DeserializationContextImpl context) {
        List<Type> chain = context.getRtypeChain();
        Type returnType = unresolvedType;
        for (int i = chain.size() - 1; i >= 0; i--) {
            Type type = chain.get(i);
            Type tmp = new VariableTypeInheritanceSearch().searchParametrizedType(type, (TypeVariable<?>) returnType);
            if (tmp != null) {
                returnType = tmp;
            }
            if (!(returnType instanceof TypeVariable)) {
                break;
            }
        }
        if (returnType instanceof TypeVariable) {
            throw new JsonbException("Could not resolve: " + unresolvedType);
        }
        return returnType;
    }

    private static Type resolveMostSpecificBound(WildcardType wildcardType, DeserializationContextImpl context) {
        Class<?> result = Object.class;
        for (Type upperBound : wildcardType.getUpperBounds()) {
            result = getMostSpecificBound(result, upperBound, context);
        }
        for (Type lowerBound : wildcardType.getLowerBounds()) {
            result = getMostSpecificBound(result, lowerBound, context);
        }
        return result;
    }

    private static Class<?> getMostSpecificBound(Class<?> result,
                                                 Type bound,
                                                 DeserializationContextImpl context) {
        if (bound == Object.class) {
            return result;
        }
        //if bound is type variable search recursively for wrapper generic expansion
        Type resolvedBoundType = bound instanceof TypeVariable
                ? resolveTypeVariable((TypeVariable<?>) bound, context)
                : bound;
        Class<?> boundRawType = ReflectionUtils.getRawType(resolvedBoundType);
        //resolved class is a subclass of a result candidate
        if (result.isAssignableFrom(boundRawType)) {
            result = boundRawType;
        }
        return result;
    }

}
