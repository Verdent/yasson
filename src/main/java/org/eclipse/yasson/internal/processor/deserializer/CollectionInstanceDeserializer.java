package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class CollectionInstanceDeserializer implements ModelDeserializer<JsonParser> {

    private final CollectionDeserializer delegate;
    private final Class<?> clazz;

    public CollectionInstanceDeserializer(CollectionDeserializer delegate, Class<?> clazz) {
        this.delegate = delegate;
        this.clazz = implementationClass(clazz);
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            context.setInstance(instance);
            return delegate.deserialize(value, context, rType);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonbException("Cannot create new instance of " + clazz, e);
        }
    }

    private Class<?> implementationClass(Class<?> ifcType) {
        if (!ifcType.isInterface()) {
            return ifcType;
        }
        if (List.class.isAssignableFrom(ifcType)) {
            if (LinkedList.class == ifcType) {
                return LinkedList.class;
            }
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
        throw new JsonbException("Unknown collection interface -> " + ifcType);
    }
}
