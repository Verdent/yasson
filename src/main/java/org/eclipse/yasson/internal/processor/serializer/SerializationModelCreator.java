package org.eclipse.yasson.internal.processor.serializer;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.yasson.internal.ComponentMatcher;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.components.DeserializerBinding;
import org.eclipse.yasson.internal.components.SerializerBinding;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.PropertyModel;
import org.eclipse.yasson.internal.model.customization.ComponentBoundCustomization;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.ReflectionUtils;
import org.eclipse.yasson.internal.processor.serializer.types.TypeSerializers;

/**
 * TODO javadoc
 */
public class SerializationModelCreator {

    private final Map<Type, ModelSerializer> serializerChain = new ConcurrentHashMap<>();
    private final JsonbContext jsonbContext;

    public SerializationModelCreator(JsonbContext jsonbContext) {
        this.jsonbContext = jsonbContext;
    }

    public ModelSerializer serializerChain(Type type, boolean cache) {
        return serializerChain(type, Customization.empty(), cache);
    }

    public ModelSerializer serializerChain(Type type, Customization propertyCustomization, boolean cache) {
        LinkedList<Type> chain = new LinkedList<>();
        return serializerChain(chain, type, propertyCustomization, cache);
    }

    public ModelSerializer serializerChainRuntime(LinkedList<Type> chain,
                                                  Type type,
                                                  Customization propertyCustomization,
                                                  boolean cache) {
        if (chain.contains(type)) {
            return new CyclicReferenceSerializer(type);
        }
        //If the class instance and class of the field are the same and there has been generics specified for this field,
        //we need to use those instead of raw type.
        Class<?> rawType = ReflectionUtils.getRawType(type);
        Class<?> rawLast = ReflectionUtils.getRawType(chain.getLast());
        if (rawLast.equals(rawType)) {
            return serializerChainInternal(chain, chain.getLast(), propertyCustomization, cache);
        }
        return serializerChainInternal(chain, type, propertyCustomization, cache);
    }

    private ModelSerializer serializerChain(LinkedList<Type> chain,
                                            Type type,
                                            Customization propertyCustomization,
                                            boolean cache) {
        if (chain.contains(type)) {
            return new CyclicReferenceSerializer(type);
        }
        try {
            chain.add(type);
            return serializerChainInternal(chain, type, propertyCustomization, cache);
        } finally {
            chain.removeLast();
        }
    }

    private ModelSerializer serializerChainInternal(LinkedList<Type> chain,
                                                    Type type,
                                                    Customization propertyCustomization,
                                                    boolean cache) {
        if (serializerChain.containsKey(type)) {
            return serializerChain.get(type);
        }
        Class<?> rawType = ReflectionUtils.getRawType(type);
        ClassModel classModel = jsonbContext.getMappingContext().getOrCreateClassModel(rawType);
        ModelSerializer typeSerializer = TypeSerializers.getTypeSerializer(rawType, propertyCustomization, jsonbContext);
        if (typeSerializer != null) {
//            if (cache) {
//                serializerChain.put(type, typeSerializer);
//            }
            return typeSerializer;
        } else if (Collection.class.isAssignableFrom(rawType)) {
            return createCollectionSerializer(chain, type, propertyCustomization);
        } else if (Map.class.isAssignableFrom(rawType)) {
            return createMapSerializer(chain, type, propertyCustomization);
        } else if (rawType.isArray()) {
            return createArraySerializer(chain, rawType, propertyCustomization);
        } else if (Optional.class.equals(rawType)) {
            return createOptionalSerializer(chain, type, propertyCustomization);
        }
        return createObjectSerializer(chain, type, classModel);
    }

    private ModelSerializer createObjectSerializer(LinkedList<Type> chain,
                                                   Type type,
                                                   ClassModel classModel) {
        LinkedList<ModelSerializer> propertySerializers = new LinkedList<>();
        for (PropertyModel model : classModel.getSortedProperties()) {
            if (model.isReadable()) {
                ModelSerializer memberModel = memberSerializer(chain,
                                                               model.getPropertySerializationType(),
                                                               model.getCustomization(),
                                                               true);
                propertySerializers.add(new ValueGetterSerializer(model.getWriteName(), model.getGetValueHandle(), memberModel));
            }
        }
        ObjectSerializer objectSerializer = new ObjectSerializer(propertySerializers);
        KeyWriter keyWriter = new KeyWriter(objectSerializer);
        NullVisibilitySwitcher nullVisibilitySwitcher = new NullVisibilitySwitcher(false, keyWriter);
        NullSerializer nullSerializer = new NullSerializer(nullVisibilitySwitcher, classModel.getClassCustomization());
        serializerChain.put(type, nullSerializer);
        return nullSerializer;
    }

    private ModelSerializer createCollectionSerializer(LinkedList<Type> chain,
                                                       Type type,
                                                       Customization customization) {
        Type colType = type instanceof ParameterizedType
                ? ((ParameterizedType) type).getActualTypeArguments()[0]
                : Object.class;
        ModelSerializer typeSerializer = memberSerializer(chain, colType, customization, false);
        CollectionSerializer collectionSerializer = new CollectionSerializer(typeSerializer);
        KeyWriter keyWriter = new KeyWriter(collectionSerializer);
        NullVisibilitySwitcher nullVisibilitySwitcher = new NullVisibilitySwitcher(true, keyWriter);
        return new NullSerializer(nullVisibilitySwitcher, customization);
    }

    private ModelSerializer createMapSerializer(LinkedList<Type> chain, Type type, Customization propertyCustomization) {
        Type keyType = type instanceof ParameterizedType
                ? ((ParameterizedType) type).getActualTypeArguments()[0]
                : Object.class;
        Type valueType = type instanceof ParameterizedType
                ? ((ParameterizedType) type).getActualTypeArguments()[1]
                : Object.class;
        Type resolvedKey = ReflectionUtils.resolveType(chain, keyType);
        Class<?> rawClass = ReflectionUtils.getRawType(resolvedKey);
        ModelSerializer keySerializer = memberSerializer(chain, keyType, Customization.empty(), true);
        ModelSerializer valueSerializer = memberSerializer(chain, valueType, propertyCustomization, true);
        MapSerializer mapSerializer = MapSerializer.create(rawClass, keySerializer, valueSerializer);
        KeyWriter keyWriter = new KeyWriter(mapSerializer);
        NullVisibilitySwitcher nullVisibilitySwitcher = new NullVisibilitySwitcher(true, keyWriter);
        return new NullSerializer(nullVisibilitySwitcher, propertyCustomization);
    }

    private ModelSerializer createArraySerializer(LinkedList<Type> chain,
                                                  Class<?> raw,
                                                  Customization propertyCustomization) {
        Class<?> arrayComponent = raw.getComponentType();
        ModelSerializer modelSerializer = memberSerializer(chain, arrayComponent, propertyCustomization, false);
        ArraySerializer arraySerializer = ArraySerializer.create(raw, modelSerializer);
        KeyWriter keyWriter = new KeyWriter(arraySerializer);
        NullVisibilitySwitcher nullVisibilitySwitcher = new NullVisibilitySwitcher(true, keyWriter);
        return new NullSerializer(nullVisibilitySwitcher, propertyCustomization);
    }

    private ModelSerializer createOptionalSerializer(LinkedList<Type> chain, Type type, Customization propertyCustomization) {
        Type optType = type instanceof ParameterizedType
                ? ((ParameterizedType) type).getActualTypeArguments()[0]
                : Object.class;
        ModelSerializer modelSerializer = memberSerializer(chain, optType, propertyCustomization, false);
        return new OptionalSerializer(modelSerializer);
    }

    private ModelSerializer memberSerializer(LinkedList<Type> chain, Type type, Customization customization, boolean cache) {
        Type resolved = ReflectionUtils.resolveType(chain, type);
        Class<?> rawType = ReflectionUtils.getRawType(resolved);
        //Final classes dont have any child classes. It is safe to assume that there will be instance of that specific class.
        boolean isFinal = Modifier.isFinal(rawType.getModifiers());
        ModelSerializer typeSerializer = isFinal
                ? TypeSerializers.getTypeSerializer(chain, rawType, customization, jsonbContext)
                : null;
        if (typeSerializer == null) {
            if (isFinal
                    || Collection.class.isAssignableFrom(rawType)
                    || Map.class.isAssignableFrom(rawType)) {
                typeSerializer = serializerChain(chain, resolved, customization, cache);
            } else {
                //Needs to be dynamically resolved with special cache since possible inheritance problem.
                chain.add(resolved);
                typeSerializer = TypeSerializers.getTypeSerializer(chain, Object.class, customization, jsonbContext);
                chain.removeLast();
            }
        }
        return typeSerializer;
    }

    private Optional<SerializerBinding<?>> userSerializer(Type type, ComponentBoundCustomization classCustomization) {
        final ComponentMatcher componentMatcher = jsonbContext.getComponentMatcher();
        return componentMatcher.getSerializerBinding(type, classCustomization);
    }

}
