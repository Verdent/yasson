package org.eclipse.yasson.internal.processor.serializer;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.ComponentMatcher;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.components.AdapterBinding;
import org.eclipse.yasson.internal.components.SerializerBinding;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.PropertyModel;
import org.eclipse.yasson.internal.model.customization.ClassCustomization;
import org.eclipse.yasson.internal.model.customization.ComponentBoundCustomization;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.ReflectionUtils;
import org.eclipse.yasson.internal.processor.serializer.types.TypeSerializers;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
public class SerializationModelCreator {

    private final Map<Type, ModelSerializer> serializerChain = new ConcurrentHashMap<>();
    private final JsonbContext jsonbContext;

    public SerializationModelCreator(JsonbContext jsonbContext) {
        this.jsonbContext = jsonbContext;
    }

    public static ModelSerializer wrapInCommonSet(ModelSerializer modelSerializer, Customization customization) {
        return Stream.of(modelSerializer)
                .map(KeyWriter::new)
                .map(serializer -> new NullSerializer(serializer, customization))
                .findFirst()
                .get();
    }

    public ModelSerializer serializerChain(Type type, boolean rootValue) {
        Class<?> rawType = ReflectionUtils.getRawType(type);
        ClassModel classModel = jsonbContext.getMappingContext().getOrCreateClassModel(rawType);
        return serializerChain(type, classModel.getClassCustomization(), rootValue);
    }

    public ModelSerializer serializerChain(Type type, Customization propertyCustomization, boolean rootValue) {
        LinkedList<Type> chain = new LinkedList<>();
        return serializerChain(chain, type, propertyCustomization, rootValue);
    }

    public ModelSerializer serializerChainRuntime(LinkedList<Type> chain,
                                                  Type type,
                                                  Customization propertyCustomization,
                                                  boolean rootValue) {
        if (chain.contains(type)) {
            return new CyclicReferenceSerializer(type);
        }
        //If the class instance and class of the field are the same and there has been generics specified for this field,
        //we need to use those instead of raw type.
        Class<?> rawType = ReflectionUtils.getRawType(type);
        Class<?> rawLast = ReflectionUtils.getRawType(chain.getLast());
        if (rawLast.equals(rawType)) {
            return serializerChainInternal(chain, chain.getLast(), propertyCustomization, rootValue);
        }
        return serializerChainInternal(chain, type, propertyCustomization, rootValue);
    }

    private ModelSerializer serializerChain(LinkedList<Type> chain,
                                            Type type,
                                            Customization propertyCustomization,
                                            boolean rootValue) {
        if (chain.contains(type)) {
            return new CyclicReferenceSerializer(type);
        }
        try {
            chain.add(type);
            return serializerChainInternal(chain, type, propertyCustomization, rootValue);
        } finally {
            chain.removeLast();
        }
    }

    private ModelSerializer serializerChainInternal(LinkedList<Type> chain,
                                                    Type type,
                                                    Customization propertyCustomization,
                                                    boolean rootValue) {
        if (serializerChain.containsKey(type)) {
            return serializerChain.get(type);
        }
        Class<?> rawType = ReflectionUtils.getRawType(type);
        Optional<ModelSerializer> serializerBinding = userSerializer(type,
                                                                     (ComponentBoundCustomization) propertyCustomization);
        if (serializerBinding.isPresent()) {
            return serializerBinding.get();
        }
        Optional<AdapterBinding> maybeAdapter = adapterBinding(type, (ComponentBoundCustomization) propertyCustomization);
        if (maybeAdapter.isPresent()) {
            AdapterBinding adapterBinding = maybeAdapter.get();
            Type toType = adapterBinding.getToType();
            Class<?> rawToType = ReflectionUtils.getRawType(toType);
            ModelSerializer typeSerializer = TypeSerializers.getTypeSerializer(rawToType, propertyCustomization, jsonbContext);
            if (typeSerializer == null) {
                typeSerializer = serializerChain(toType, rootValue);
            }
            AdapterSerializer adapterSerializer = new AdapterSerializer(adapterBinding, typeSerializer);
            RecursionChecker recursionChecker = new RecursionChecker(adapterSerializer);
            NullSerializer nullSerializer = new NullSerializer(recursionChecker, propertyCustomization);
            serializerChain.put(type, nullSerializer);
            return nullSerializer;
        }

        ModelSerializer typeSerializer = null;
        if (!Object.class.equals(rawType)) {
            typeSerializer = TypeSerializers.getTypeSerializer(rawType, propertyCustomization, jsonbContext);
        }
        if (typeSerializer != null) {
            if (jsonbContext.getConfigProperties().isStrictIJson() && rootValue) {
                throw new JsonbException(Messages.getMessage(MessageKeys.IJSON_ENABLED_SINGLE_VALUE));
            }
            return typeSerializer;
        }
        ClassModel classModel = jsonbContext.getMappingContext().getOrCreateClassModel(rawType);
        if (Collection.class.isAssignableFrom(rawType)) {
            return createCollectionSerializer(chain, type, propertyCustomization);
        } else if (Map.class.isAssignableFrom(rawType)) {
            return createMapSerializer(chain, type, propertyCustomization);
        } else if (rawType.isArray()) {
            return createArraySerializer(chain, rawType, propertyCustomization);
        } else if (type instanceof GenericArrayType) {
            return createGenericArraySerializer(chain, type, propertyCustomization);
        } else if (Optional.class.equals(rawType)) {
            return createOptionalSerializer(chain, type, propertyCustomization);
        }
        return createObjectSerializer(chain, type, classModel);
    }

    private ModelSerializer createObjectSerializer(LinkedList<Type> chain,
                                                   Type type,
                                                   ClassModel classModel) {
        LinkedHashMap<String, ModelSerializer> propertySerializers = new LinkedHashMap<>();
        for (PropertyModel model : classModel.getSortedProperties()) {
            if (model.isReadable()) {
                String name = model.getWriteName();
                ModelSerializer memberModel = memberSerializer(chain,
                                                               model.getPropertySerializationType(),
                                                               model.getCustomization());
                propertySerializers.put(name, new ValueGetterSerializer(model.getGetValueHandle(), memberModel));
            }
        }
        ObjectSerializer objectSerializer = new ObjectSerializer(propertySerializers);
        RecursionChecker recursionChecker = new RecursionChecker(objectSerializer);
        KeyWriter keyWriter = new KeyWriter(recursionChecker);
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
        ModelSerializer typeSerializer = memberSerializer(chain, colType, customization);
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
        ModelSerializer keySerializer = memberSerializer(chain, keyType, ClassCustomization.empty());
        ModelSerializer valueSerializer = memberSerializer(chain, valueType, propertyCustomization);
        MapSerializer mapSerializer = MapSerializer.create(rawClass, keySerializer, valueSerializer);
        KeyWriter keyWriter = new KeyWriter(mapSerializer);
        NullVisibilitySwitcher nullVisibilitySwitcher = new NullVisibilitySwitcher(true, keyWriter);
        return new NullSerializer(nullVisibilitySwitcher, propertyCustomization);
    }

    private ModelSerializer createArraySerializer(LinkedList<Type> chain,
                                                  Class<?> raw,
                                                  Customization propertyCustomization) {
        Class<?> arrayComponent = raw.getComponentType();
        ModelSerializer modelSerializer = memberSerializer(chain, arrayComponent, propertyCustomization);
        ModelSerializer arraySerializer = ArraySerializer.create(raw, jsonbContext, modelSerializer);
        KeyWriter keyWriter = new KeyWriter(arraySerializer);
        NullVisibilitySwitcher nullVisibilitySwitcher = new NullVisibilitySwitcher(true, keyWriter);
        return new NullSerializer(nullVisibilitySwitcher, propertyCustomization);
    }

    private ModelSerializer createGenericArraySerializer(LinkedList<Type> chain,
                                                         Type type,
                                                         Customization propertyCustomization) {
        Class<?> raw = ReflectionUtils.getRawType(type);
        Class<?> component = ReflectionUtils.getRawType(((GenericArrayType) type).getGenericComponentType());
        ModelSerializer modelSerializer = memberSerializer(chain, component, propertyCustomization);
        ModelSerializer arraySerializer = ArraySerializer.create(raw, jsonbContext, modelSerializer);
        KeyWriter keyWriter = new KeyWriter(arraySerializer);
        NullVisibilitySwitcher nullVisibilitySwitcher = new NullVisibilitySwitcher(true, keyWriter);
        return new NullSerializer(nullVisibilitySwitcher, propertyCustomization);
    }

    private ModelSerializer createOptionalSerializer(LinkedList<Type> chain, Type type, Customization propertyCustomization) {
        Type optType = type instanceof ParameterizedType
                ? ((ParameterizedType) type).getActualTypeArguments()[0]
                : Object.class;
        ModelSerializer modelSerializer = memberSerializer(chain, optType, propertyCustomization);
        return new OptionalSerializer(modelSerializer);
    }

    private ModelSerializer memberSerializer(LinkedList<Type> chain, Type type, Customization customization) {
        Type resolved = ReflectionUtils.resolveType(chain, type);
        Class<?> rawType = ReflectionUtils.getRawType(resolved);

        Optional<ModelSerializer> serializerBinding = userSerializer(resolved,
                                                                     (ComponentBoundCustomization) customization);
        if (serializerBinding.isPresent()) {
            return serializerBinding.get();
        }
        Optional<AdapterBinding> maybeAdapter = adapterBinding(resolved, (ComponentBoundCustomization) customization);
        if (maybeAdapter.isPresent()) {
            AdapterBinding adapterBinding = maybeAdapter.get();
            Type toType = adapterBinding.getToType();
            Class<?> rawToType = ReflectionUtils.getRawType(toType);
            ModelSerializer typeSerializer = TypeSerializers.getTypeSerializer(rawToType, customization, jsonbContext);
            if (typeSerializer == null) {
                typeSerializer = serializerChain(toType, false);
            }
            AdapterSerializer adapterSerializer = new AdapterSerializer(adapterBinding, typeSerializer);
            return new NullSerializer(adapterSerializer, customization);
        }
        ModelSerializer typeSerializer = TypeSerializers.getTypeSerializer(chain, rawType, customization, jsonbContext);
        if (typeSerializer == null) {
            //Final classes dont have any child classes. It is safe to assume that there will be instance of that specific class.
            boolean isFinal = Modifier.isFinal(rawType.getModifiers());
            if (isFinal
                    || Collection.class.isAssignableFrom(rawType)
                    || Map.class.isAssignableFrom(rawType)) {
                typeSerializer = serializerChain(chain, resolved, customization, false);
            } else {
                //Needs to be dynamically resolved with special cache since possible inheritance problem.
                if (resolved instanceof Class) {
                    typeSerializer = TypeSerializers.getTypeSerializer(chain, Object.class, customization, jsonbContext);
                } else {
                    chain.add(resolved);
                    typeSerializer = TypeSerializers.getTypeSerializer(chain, Object.class, customization, jsonbContext);
                    chain.removeLast();
                }
            }
        }
        return typeSerializer;
    }

    private Optional<ModelSerializer> userSerializer(Type type, ComponentBoundCustomization classCustomization) {
        final ComponentMatcher componentMatcher = jsonbContext.getComponentMatcher();
        return componentMatcher.getSerializerBinding(type, classCustomization)
                .map(SerializerBinding::getJsonbSerializer)
                .map(UserDefinedSerializer::new)
                .map(RecursionChecker::new)
                .map(serializer -> SerializationModelCreator.wrapInCommonSet(serializer, (Customization) classCustomization));
    }

    private Optional<AdapterBinding> adapterBinding(Type type, ComponentBoundCustomization classCustomization) {
        return jsonbContext.getComponentMatcher().getSerializeAdapterBinding(type, classCustomization);
    }

}
