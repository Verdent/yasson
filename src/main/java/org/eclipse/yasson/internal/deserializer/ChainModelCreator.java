package org.eclipse.yasson.internal.deserializer;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.config.BinaryDataStrategy;
import jakarta.json.bind.config.PropertyNamingStrategy;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.ComponentMatcher;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.components.AdapterBinding;
import org.eclipse.yasson.internal.components.DeserializerBinding;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.CreatorModel;
import org.eclipse.yasson.internal.model.JsonbCreator;
import org.eclipse.yasson.internal.model.PropertyModel;
import org.eclipse.yasson.internal.model.customization.ClassCustomization;
import org.eclipse.yasson.internal.model.customization.ComponentBoundCustomization;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.model.customization.PropertyCustomization;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.deserializer.types.TypeDeserializers;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

import static jakarta.json.bind.JsonbConfig.PROPERTY_NAMING_STRATEGY;
import static jakarta.json.stream.JsonParser.Event;

/**
 * TODO javadoc
 */
public class ChainModelCreator {

    private static final Set<JsonParser.Event> MAP_KEY_EVENTS = new HashSet<>();

    static {
        MAP_KEY_EVENTS.add(Event.KEY_NAME);
        MAP_KEY_EVENTS.addAll(PositionChecker.Checker.VALUES.getEvents());
    }

    private final Map<Type, ModelDeserializer<JsonParser>> deserializerChain = new ConcurrentHashMap<>();

    private final JsonbContext jsonbContext;
    private final Map<Class<?>, Class<?>> userTypeMapping;

    public ChainModelCreator(JsonbContext jsonbContext) {
        this.jsonbContext = jsonbContext;
        this.userTypeMapping = jsonbContext.getConfigProperties().getUserTypeMapping();
    }

    public ModelDeserializer<JsonParser> deserializerChain(Type type, ClassModel classModel) {
        LinkedList<Type> chain = new LinkedList<>();
        return deserializerChain(chain, type, classModel.getClassCustomization(), classModel);
    }

    private ModelDeserializer<JsonParser> deserializerChain(LinkedList<Type> chain,
                                                            Type type,
                                                            Customization propertyCustomization,
                                                            ClassModel classModel) {
        if (chain.contains(type)) {
            return new CyclicReferenceDeserializer(type);
        }
        try {
            chain.add(type);
            return deserializerChainInternal(chain, type, propertyCustomization, classModel);
        } finally {
            chain.removeLast();
        }
    }

    private ModelDeserializer<JsonParser> deserializerChainInternal(LinkedList<Type> chain,
                                                                    Type type,
                                                                    Customization propertyCustomization,
                                                                    ClassModel classModel) {
        Class<?> rawType = classModel.getType();
        if (deserializerChain.containsKey(type)) {
            return deserializerChain.get(type);
        } else if (userTypeMapping.containsKey(rawType)) {
            Class<?> userTypeRaw = userTypeMapping.get(rawType);
            ClassModel userTypeModel = jsonbContext.getMappingContext().getOrCreateClassModel(userTypeRaw);
            ModelDeserializer<JsonParser> deserializer = deserializerChain(userTypeRaw, userTypeModel);
            deserializerChain.put(type, deserializer);
            return deserializer;
        }
        Optional<AdapterBinding> adapterBinding = adapterBinding(type, (ComponentBoundCustomization) propertyCustomization);
        if (adapterBinding.isPresent()) {
            AdapterBinding adapter = adapterBinding.get();
            Class<?> toType = ReflectionUtils.getRawType(adapter.getToType());
            ClassModel targetModel = jsonbContext.getMappingContext().getOrCreateClassModel(toType);
            ModelDeserializer<JsonParser> typeDeserializer = typeDeserializer(toType,
                                                                              targetModel.getClassCustomization(),
                                                                              JustReturn.create());
            if (typeDeserializer == null) {
                typeDeserializer = deserializerChain(adapter.getToType(), targetModel);
            }
            ModelDeserializer<JsonParser> targetAdapterModel = typeDeserializer;
            AdapterDeserializer adapterDeserializer = new AdapterDeserializer(adapter, JustReturn.create());
            ModelDeserializer<JsonParser> adapterDeser = (parser, context) -> {
                Object fromJson = targetAdapterModel.deserialize(parser, context);
                return adapterDeserializer.deserialize(fromJson, context);
            };
            deserializerChain.put(type, adapterDeser);
            return adapterDeser;
        }
        ModelDeserializer<JsonParser> typeDeserializer = typeDeserializer(rawType,
                                                                          propertyCustomization,
                                                                          JustReturn.create());
        if (typeDeserializer != null) {
            deserializerChain.put(type, typeDeserializer);
            return typeDeserializer;
        }
        JsonbConfigProperties configProperties = jsonbContext.getConfigProperties();
        if (Collection.class.isAssignableFrom(rawType)) {
            Type colType = type instanceof ParameterizedType
                    ? ((ParameterizedType) type).getActualTypeArguments()[0]
                    : Object.class;
            colType = ReflectionUtils.resolveType(chain, colType);
            ClassModel colClassModel = jsonbContext.getMappingContext()
                    .getOrCreateClassModel(ReflectionUtils.getRawType(colType));
            ModelDeserializer<JsonParser> typeProcessor = typeProcessor(chain,
                                                                        colType,
                                                                        colClassModel.getClassCustomization(),
                                                                        JustReturn.create());
            CollectionDeserializer collectionDeserializer = new CollectionDeserializer(typeProcessor);
            CollectionInstanceCreator instanceDeserializer = new CollectionInstanceCreator(collectionDeserializer, type);
            PositionChecker positionChecker = new PositionChecker(instanceDeserializer, rawType, Event.START_ARRAY);
            NullCheckDeserializer nullChecker = new NullCheckDeserializer(positionChecker, JustReturn.create(), rawType);
            deserializerChain.put(type, nullChecker);
            return nullChecker;
        } else if (Map.class.isAssignableFrom(rawType)) {
            Type keyType = type instanceof ParameterizedType
                    ? ((ParameterizedType) type).getActualTypeArguments()[0]
                    : Object.class;
            Type valueType = type instanceof ParameterizedType
                    ? ((ParameterizedType) type).getActualTypeArguments()[1]
                    : Object.class;
            //            ClassModel colTypeModel = jsonbContext.getMappingContext().getOrCreateClassModel(ReflectionUtils
            //            .getRawType(colType));

            ModelDeserializer<JsonParser> keyProcessor = typeProcessor(chain,
                                                                       keyType,
                                                                       ClassCustomization.empty(),
                                                                       JustReturn.create(),
                                                                       MAP_KEY_EVENTS);
            ModelDeserializer<JsonParser> valueProcessor = typeProcessor(chain,
                                                                         valueType,
                                                                         propertyCustomization,
                                                                         JustReturn.create());

            MapDeserializer mapDeserializer = new MapDeserializer(keyProcessor, valueProcessor);
            MapInstanceCreator mapInstanceCreator = new MapInstanceCreator(mapDeserializer,
                                                                           jsonbContext.getInstanceCreator(),
                                                                           jsonbContext.getConfigProperties(),
                                                                           rawType);
            PositionChecker positionChecker = new PositionChecker(mapInstanceCreator, rawType, PositionChecker.Checker.CONTAINER);
            NullCheckDeserializer nullChecker = new NullCheckDeserializer(positionChecker, JustReturn.create(), rawType);
            deserializerChain.put(type, nullChecker);
            return nullChecker;
        } else if (rawType.isArray()) {
            if (rawType.equals(byte[].class) && !configProperties.getBinaryDataStrategy().equals(BinaryDataStrategy.BYTE)) {
                String strategy = configProperties.getBinaryDataStrategy();
                ModelDeserializer<JsonParser> typeProcessor = typeProcessor(chain,
                                                                            String.class,
                                                                            propertyCustomization,
                                                                            JustReturn.create());
                ModelDeserializer<JsonParser> base64Deserializer = ArrayInstanceCreator
                        .createBase64Deserializer(strategy, typeProcessor);
                NullCheckDeserializer nullChecker = new NullCheckDeserializer(base64Deserializer, JustReturn.create(), rawType);
                deserializerChain.put(type, nullChecker);
                return nullChecker;
            }
            Class<?> arrayType = rawType.getComponentType();
            ModelDeserializer<JsonParser> typeProcessor = typeProcessor(chain,
                                                                        arrayType,
                                                                        propertyCustomization,
                                                                        JustReturn.create());
            ArrayDeserializer arrayDeserializer = new ArrayDeserializer(typeProcessor);
            ArrayInstanceCreator arrayInstanceCreator = ArrayInstanceCreator.create(rawType, arrayType, arrayDeserializer);
            PositionChecker positionChecker = new PositionChecker(arrayInstanceCreator, rawType, Event.START_ARRAY);
            NullCheckDeserializer nullChecker = new NullCheckDeserializer(positionChecker, JustReturn.create(), rawType);
            deserializerChain.put(type, nullChecker);
            return nullChecker;
        } else if (type instanceof GenericArrayType) {
            Class<?> component = ReflectionUtils.getRawType(((GenericArrayType) type).getGenericComponentType());
            ModelDeserializer<JsonParser> typeProcessor = typeProcessor(chain,
                                                                        ((GenericArrayType) type).getGenericComponentType(),
                                                                        propertyCustomization,
                                                                        JustReturn.create());
            ArrayDeserializer arrayDeserializer = new ArrayDeserializer(typeProcessor);
            ArrayInstanceCreator arrayInstanceCreator = ArrayInstanceCreator.create(rawType, component, arrayDeserializer);
            PositionChecker positionChecker = new PositionChecker(arrayInstanceCreator, rawType, Event.START_ARRAY);
            NullCheckDeserializer nullChecker = new NullCheckDeserializer(positionChecker, JustReturn.create(), rawType);
            deserializerChain.put(type, nullChecker);
            return nullChecker;
        } else if (Optional.class.isAssignableFrom(rawType)) {
            Type colType = type instanceof ParameterizedType
                    ? ((ParameterizedType) type).getActualTypeArguments()[0]
                    : Object.class;
            ModelDeserializer<JsonParser> typeProcessor = typeProcessor(chain,
                                                                        colType,
                                                                        propertyCustomization,
                                                                        JustReturn.create());
            OptionalDeserializer optionalDeserializer = new OptionalDeserializer(typeProcessor, JustReturn.create());
            deserializerChain.put(type, optionalDeserializer);
            return optionalDeserializer;
        } else {
            ClassCustomization classCustomization = classModel.getClassCustomization();
            Optional<DeserializerBinding<?>> deserializerBinding = userDeserializer(type,
                                                                                    (ComponentBoundCustomization) propertyCustomization);
            if (deserializerBinding.isPresent()) {
                UserDefinedDeserializer user = new UserDefinedDeserializer(deserializerBinding.get().getJsonbDeserializer(),
                                                                           JustReturn.create(), type, classCustomization);
                deserializerChain.put(type, user);
                return user;
            }
            JsonbCreator creator = classCustomization.getCreator();
            boolean hasCreator = creator != null;
            List<String> params = hasCreator ? creatorParamsList(creator) : Collections.emptyList();
            Function<String, String> renamer = propertyRenamer();
            Map<String, ModelDeserializer<JsonParser>> processors = new LinkedHashMap<>();
            for (PropertyModel propertyModel : classModel.getSortedProperties()) {
                if (!propertyModel.isWritable() || params.contains(propertyModel.getReadName())) {
                    continue;
                }
                ModelDeserializer<JsonParser> modelDeserializer = memberTypeProcessor(chain,
                                                                                      propertyModel, hasCreator,
                                                                                      false);
                processors.put(renamer.apply(propertyModel.getReadName()), modelDeserializer);
            }
            for (String s : params) {
                //                if (!processors.containsKey(s)) { //TODO analyze proper behavior with annotation overriding
                CreatorModel creatorModel = creator.findByName(s);
                ModelDeserializer<JsonParser> modelDeserializer = typeProcessor(chain,
                                                                                creatorModel.getType(),
                                                                                creatorModel.getCustomization(),
                                                                                JustReturn.create());
                processors.put(renamer.apply(creatorModel.getName()), modelDeserializer);
                //                }
            }
            ModelDeserializer<JsonParser> instanceCreator;
            if (hasCreator) {
                instanceCreator = new ObjectInstanceCreator(processors, creator, rawType, renamer);
            } else {
                ModelDeserializer<JsonParser> typeWrapper = new ObjectDeserializer(processors, renamer, rawType);
                instanceCreator = new ObjectDefaultInstanceCreator(typeWrapper, rawType,
                                                                   classModel.getDefaultConstructor());
            }
            PositionChecker positionChecker = new PositionChecker(instanceCreator, rawType, Event.START_OBJECT);
            ModelDeserializer<JsonParser> nullChecker = new NullCheckDeserializer(positionChecker,
                                                                                  JustReturn.create(),
                                                                                  rawType);
            deserializerChain.put(type, nullChecker);
            return nullChecker;
        }
    }

    private Function<String, String> propertyRenamer() {
        boolean isCaseInsensitive = jsonbContext.getConfig()
                .getProperty(PROPERTY_NAMING_STRATEGY)
                .filter(prop -> prop.equals(PropertyNamingStrategy.CASE_INSENSITIVE))
                .isPresent();

        return isCaseInsensitive
                ? String::toLowerCase
                : value -> value;
    }

    private Optional<AdapterBinding> adapterBinding(Type type, ComponentBoundCustomization classCustomization) {
        return jsonbContext.getComponentMatcher().getDeserializeAdapterBinding(type, classCustomization);
    }

    private Optional<DeserializerBinding<?>> userDeserializer(Type type, ComponentBoundCustomization classCustomization) {
        final ComponentMatcher componentMatcher = jsonbContext.getComponentMatcher();
        return componentMatcher.getDeserializerBinding(type, classCustomization);
    }

    private List<String> creatorParamsList(JsonbCreator creator) {
        return Arrays.stream(creator.getParams()).map(CreatorModel::getName).collect(Collectors.toList());
    }

    private ModelDeserializer<JsonParser> memberTypeProcessor(LinkedList<Type> chain,
                                                              PropertyModel propertyModel,
                                                              boolean hasCreator,
                                                              boolean isCreatorParam) {
        ModelDeserializer<Object> memberDeserializer;
        Type type = propertyModel.getPropertyDeserializationType();
        if (isCreatorParam) {
            memberDeserializer = JustReturn.create();
        } else {
            memberDeserializer = new ValueSetterDeserializer(propertyModel.getSetValueHandle());
        }
        if (hasCreator && !isCreatorParam) {
            memberDeserializer = new DelayedDeserializer(memberDeserializer);
        }
        return typeProcessor(chain, type, propertyModel.getCustomization(), memberDeserializer);
    }

    private ModelDeserializer<JsonParser> typeProcessor(LinkedList<Type> chain,
                                                        Type type,
                                                        Customization customization,
                                                        ModelDeserializer<Object> memberDeserializer) {
        return typeProcessor(chain, type, customization, memberDeserializer, PositionChecker.Checker.VALUES.getEvents());
    }

    private ModelDeserializer<JsonParser> typeProcessor(LinkedList<Type> chain,
                                                        Type type,
                                                        Customization customization,
                                                        ModelDeserializer<Object> memberDeserializer,
                                                        Set<Event> events) {
        Type resolved = ReflectionUtils.resolveType(chain, type);
        Class<?> rawType = ReflectionUtils.getRawType(resolved);
        Optional<DeserializerBinding<?>> deserializerBinding = userDeserializer(resolved,
                                                                                (ComponentBoundCustomization) customization);
        if (deserializerBinding.isPresent()) {
            return new UserDefinedDeserializer(deserializerBinding.get().getJsonbDeserializer(),
                                               memberDeserializer,
                                               resolved,
                                               customization);
        }
        Optional<AdapterBinding> adapterBinding = adapterBinding(resolved, (ComponentBoundCustomization) customization);
        if (adapterBinding.isPresent()) {
            AdapterBinding adapter = adapterBinding.get();
            ClassModel targetModel = jsonbContext.getMappingContext()
                    .getOrCreateClassModel(ReflectionUtils.getRawType(adapter.getToType()));

            ModelDeserializer<JsonParser> typeDeserializer = typeDeserializer(ReflectionUtils.getRawType(adapter.getToType()),
                                                                              customization,
                                                                              JustReturn.create(), events);
            if (typeDeserializer == null) {
                typeDeserializer = deserializerChain(adapter.getToType(), targetModel);
            }
            ModelDeserializer<JsonParser> targetAdapterModel = typeDeserializer;

            AdapterDeserializer adapterDeserializer = new AdapterDeserializer(adapter, memberDeserializer);
            return (parser, context) -> {
                DeserializationContextImpl newContext = new DeserializationContextImpl(context);
                Object fromJson = targetAdapterModel.deserialize(parser, newContext);
                return adapterDeserializer.deserialize(fromJson, context);
            };
        }
        ModelDeserializer<JsonParser> typeDeserializer = typeDeserializer(rawType, customization, memberDeserializer, events);
        if (typeDeserializer == null) {
            Class<?> implClass = resolveImplClass(rawType, customization);
            return createNewChain(chain, memberDeserializer, implClass, resolved, customization);
        }
        return typeDeserializer;
    }

    private ModelDeserializer<JsonParser> createNewChain(LinkedList<Type> chain,
                                                         ModelDeserializer<Object> memberDeserializer,
                                                         Class<?> rawType,
                                                         Type type,
                                                         Customization propertyCustomization) {
        ClassModel classModel = jsonbContext.getMappingContext().getOrCreateClassModel(rawType);
        ModelDeserializer<JsonParser> modelDeserializer = deserializerChain(chain, type, propertyCustomization, classModel);
        return new ContextSwitcher(memberDeserializer, modelDeserializer);
    }

    private ModelDeserializer<JsonParser> typeDeserializer(Class<?> rawType,
                                                           Customization customization,
                                                           ModelDeserializer<Object> delegate) {
        return typeDeserializer(rawType, customization, delegate, PositionChecker.Checker.VALUES.getEvents());
    }

    private ModelDeserializer<JsonParser> typeDeserializer(Class<?> rawType,
                                                           Customization customization,
                                                           ModelDeserializer<Object> delegate,
                                                           Set<JsonParser.Event> events) {
        return TypeDeserializers
                .getTypeDeserializer(rawType, customization, jsonbContext.getConfigProperties(), delegate, events);
    }

    private Class<?> resolveImplClass(Class<?> rawType, Customization customization) {
        if (rawType.isInterface()) {
            Class<?> implementationClass = null;
            //annotation
            if (customization instanceof PropertyCustomization) {
                implementationClass = ((PropertyCustomization) customization).getImplementationClass();
            }
            //JsonbConfig
            if (implementationClass == null) {
                implementationClass = jsonbContext.getConfigProperties().getUserTypeMapping().get(rawType);
            }
            if (implementationClass != null) {
                if (!rawType.isAssignableFrom(implementationClass)) {
                    throw new JsonbException(Messages.getMessage(MessageKeys.IMPL_CLASS_INCOMPATIBLE,
                                                                 implementationClass,
                                                                 rawType));
                }
                return implementationClass;
            }
        }
        return rawType;
    }

}