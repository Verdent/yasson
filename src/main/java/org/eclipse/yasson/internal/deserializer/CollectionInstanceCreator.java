package org.eclipse.yasson.internal.deserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.InstanceCreator;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class CollectionInstanceCreator implements ModelDeserializer<JsonParser> {

    private final CollectionDeserializer delegate;
    private final Type type;
    private final Class<?> clazz;
    private final boolean isEnumSet;

    public CollectionInstanceCreator(CollectionDeserializer delegate, Type type) {
        this.delegate = delegate;
        this.type = type;
        this.clazz = implementationClass(ReflectionUtils.getRawType(type));
        this.isEnumSet = EnumSet.class.isAssignableFrom(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        Object instance;
        if (isEnumSet) {
            Type resolvedType = ((ParameterizedType) type).getActualTypeArguments()[0];
            instance = EnumSet.noneOf((Class<Enum>) resolvedType);
        } else {
            instance = InstanceCreator.createInstance(clazz);
        }
        context.setInstance(instance);
        return delegate.deserialize(value, context);
    }

    private Class<?> implementationClass(Class<?> type) {
        if (type.isInterface()) {
            return createInterfaceInstance(type);
        }
        return type;
    }

    private Class<?> createInterfaceInstance(Class<?> ifcType) {
        if (List.class.isAssignableFrom(ifcType)) {
            return ArrayList.class;
        }
        if (Set.class.isAssignableFrom(ifcType)) {
            if (SortedSet.class.isAssignableFrom(ifcType)) {
                return TreeSet.class;
            }
            return HashSet.class;
        }
        if (Queue.class.isAssignableFrom(ifcType)) {
            return ArrayDeque.class;
        }
        if (Collection.class == ifcType) {
            return ArrayList.class;
        }
        return ifcType;
    }
}
