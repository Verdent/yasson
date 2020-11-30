package org.eclipse.yasson.internal.processor.serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.PropertyModel;
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
        return serializerChain(type, null, cache);
    }

    public ModelSerializer serializerChain(Type type, Customization propertyCustomization, boolean cache) {
        LinkedList<Type> chain = new LinkedList<>();
        return serializerChain(chain, type, propertyCustomization, cache);
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
        ModelSerializer typeSerializer = TypeSerializers
                .getTypeSerializer(ReflectionUtils.getRawType(type), propertyCustomization, jsonbContext);
        if (typeSerializer != null) {
            if (cache) {
                serializerChain.put(type, typeSerializer);
            }
            return typeSerializer;
        } else if (Collection.class.isAssignableFrom(rawType)) {
            return createCollectionSerializer(chain, type, propertyCustomization, cache);
        }
        return createObjectSerializer(chain, type, classModel);
    }

    private ModelSerializer createObjectSerializer(LinkedList<Type> chain,
                                                   Type type,
                                                   ClassModel classModel) {
        LinkedList<ModelSerializer> propertySerializers = new LinkedList<>();
        for (PropertyModel model : classModel.getSortedProperties()) {
            ModelSerializer memberModel = memberSerializer(chain,
                                                           model.getPropertySerializationType(),
                                                           model.getCustomization(),
                                                           true);
            propertySerializers.add(new ValueGetterSerializer(model.getWriteName(), model.getGetValueHandle(), memberModel));
        }
        ObjectSerializer objectSerializer = new ObjectSerializer(propertySerializers);
        NullSerializer nullSerializer = new NullSerializer(objectSerializer, jsonbContext);
        serializerChain.put(type, nullSerializer);
        return nullSerializer;
    }

    private ModelSerializer createCollectionSerializer(LinkedList<Type> chain,
                                                       Type type,
                                                       Customization customization,
                                                       boolean cache) {
        Type colType = type instanceof ParameterizedType
                ? ((ParameterizedType) type).getActualTypeArguments()[0]
                : Object.class;
//        ClassModel colCM = jsonbContext.getMappingContext().getOrCreateClassModel(ReflectionUtils.getRawType(colType));
        ModelSerializer typeSerializer = memberSerializer(chain, colType, customization, false);
        CollectionSerializer colSerializer = new CollectionSerializer(typeSerializer);
//        if (cache) {
//            serializerChain.put(type, colSerializer);
//        }
        return colSerializer;
    }

    private ModelSerializer memberSerializer(LinkedList<Type> chain, Type type, Customization customization, boolean cache) {
        Type resolved = ReflectionUtils.resolveType(chain, type);
        Class<?> rawType = ReflectionUtils.getRawType(resolved);
        ModelSerializer typeSerializer = TypeSerializers
                .getTypeSerializer(rawType, customization, jsonbContext);
        if (typeSerializer == null) {
            typeSerializer = serializerChain(chain, resolved, customization, cache);
        }
        return typeSerializer;
    }

}
