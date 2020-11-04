package org.eclipse.yasson.internal.processor;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.ComponentMatcher;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.components.DeserializerBinding;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.CreatorModel;
import org.eclipse.yasson.internal.model.JsonbCreator;
import org.eclipse.yasson.internal.model.PropertyModel;
import org.eclipse.yasson.internal.model.customization.ClassCustomization;
import org.eclipse.yasson.internal.model.customization.ComponentBoundCustomization;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.model.customization.PropertyCustomization;
import org.eclipse.yasson.internal.processor.deserializer.CollectionDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.CollectionDynamicTypeDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.CollectionInstanceDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.DelayedDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.DynamicTypeDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.JustReturn;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.NullDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ObjectDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ObjectInstanceCreator;
import org.eclipse.yasson.internal.processor.deserializer.ObjectDefaultInstanceCreator;
import org.eclipse.yasson.internal.processor.deserializer.ValueSetterDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.UserDefinedDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ValueExtractor;
import org.eclipse.yasson.internal.processor.types.TypeDeserializers;

/**
 * TODO javadoc
 */
public class ChainModelCreator {

    private final Map<Class<?>, ModelDeserializer<JsonParser>> deserializerChain = new ConcurrentHashMap<>();

    private final JsonbContext jsonbContext;
    private final Map<Class<?>, Class<?>> userTypeMapping;

    public ChainModelCreator(JsonbContext jsonbContext) {
        this.jsonbContext = jsonbContext;
        this.userTypeMapping = jsonbContext.getConfigProperties().getUserTypeMapping();
    }

    public ModelDeserializer<JsonParser> deserializerChain(ClassModel classModel) {
        if (deserializerChain.containsKey(classModel.getType())) {
            return deserializerChain.get(classModel.getType());
        }
        if (userTypeMapping.containsKey(classModel.getType())) {
            return deserializerChain(jsonbContext.getMappingContext()
                                             .getOrCreateClassModel(userTypeMapping.get(classModel.getType())));
        } else if (Collection.class.isAssignableFrom(classModel.getType())) {
            return deserializerChain.computeIfAbsent(classModel.getType(), coll -> {
                CollectionDeserializer collectionDeserializer =
                        new CollectionDeserializer(new CollectionDynamicTypeDeserializer());
                return new CollectionInstanceDeserializer(collectionDeserializer, classModel.getType());
            });
        } else {
            ClassCustomization classCustomization = classModel.getClassCustomization();
            Optional<DeserializerBinding<?>> deserializerBinding = userDeserializer(classModel.getType(), classCustomization);
            if (deserializerBinding.isPresent()) {
                return new UserDefinedDeserializer(deserializerBinding.get().getJsonbDeserializer());
            }
            JsonbCreator creator = classCustomization.getCreator();
            boolean hasCreator = creator != null;
            List<String> params = hasCreator ? creatorParamsList(creator) : Collections.emptyList();
            Map<String, ModelDeserializer<JsonParser>> processors = new LinkedHashMap<>();
            for (PropertyModel propertyModel : classModel.getSortedProperties()) {
                if (!propertyModel.isWritable()) {
                    continue;
                }
                ModelDeserializer<JsonParser> modelDeserializer = memberTypeProcessor(propertyModel, hasCreator,
                                                                                      params.contains(propertyModel
                                                                                                              .getReadName()));
                processors.put(propertyModel.getReadName(), modelDeserializer);
            }
            for (String s : params) {
                if (!processors.containsKey(s)) {
                    CreatorModel creatorModel = creator.findByName(s);
                    ModelDeserializer<JsonParser> modelDeserializer = typeProcessor(creatorModel.getType(),
                                                                                    creatorModel.getCustomization(),
                                                                                    JustReturn.create());
                    processors.put(creatorModel.getName(), modelDeserializer);
                }
            }
            ModelDeserializer<JsonParser> instanceCreator;
            if (hasCreator) {
                instanceCreator = new ObjectInstanceCreator(processors, creator, classModel.getType());
            } else {
                ModelDeserializer<JsonParser> typeWrapper = new ObjectDeserializer(processors);
                instanceCreator = new ObjectDefaultInstanceCreator(typeWrapper, classModel.getType(),
                                                                   classModel.getDefaultConstructor());
            }
            deserializerChain.put(classModel.getType(), instanceCreator);
        }
        return deserializerChain.get(classModel.getType());
    }

    private Optional<DeserializerBinding<?>> userDeserializer(Type type, ComponentBoundCustomization classCustomization) {
        final ComponentMatcher componentMatcher = jsonbContext.getComponentMatcher();
        return componentMatcher.getDeserializerBinding(type, classCustomization);
    }

    private List<String> creatorParamsList(JsonbCreator creator) {
        return Arrays.stream(creator.getParams()).map(CreatorModel::getName).collect(Collectors.toList());
    }

    private ModelDeserializer<JsonParser> memberTypeProcessor(PropertyModel propertyModel,
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
        return typeProcessor(type, propertyModel.getCustomization(), memberDeserializer);
    }

    private ModelDeserializer<JsonParser> typeProcessor(Type type,
                                                        Customization customization,
                                                        ModelDeserializer<Object> memberDeserializer) {
        if (type instanceof TypeVariable || type instanceof WildcardType) {
            return new DynamicTypeDeserializer(memberDeserializer, type, customization);
        }
        Class<?> rawType = ReflectionUtils.getRawType(type);
        if (Collection.class.isAssignableFrom(rawType)) {
            ModelDeserializer<JsonParser> modelDeserializer = deserializerChain.computeIfAbsent(rawType, coll -> {
                CollectionDeserializer collectionDeserializer =
                        new CollectionDeserializer(new CollectionDynamicTypeDeserializer());
                return new CollectionInstanceDeserializer(collectionDeserializer, rawType);
            });
            return new NullDeserializer((value, context, rType) -> {
                DeserializationContextImpl ctx = new DeserializationContextImpl(context);
                return memberDeserializer.deserialize(modelDeserializer.deserialize(value, ctx, type), context, rType);
            }, memberDeserializer, rawType);
        } else {
            ModelDeserializer<String> typeDeserializer = TypeDeserializers
                    .getTypeDeserializer(rawType, customization, jsonbContext.getConfigProperties(), memberDeserializer);
            if (typeDeserializer == null) {
                Optional<DeserializerBinding<?>> deserializerBinding = userDeserializer(type,
                                                                                        (ComponentBoundCustomization) customization);
                if (deserializerBinding.isPresent()) {
                    return new UserDefinedDeserializer(deserializerBinding.get().getJsonbDeserializer());
                }
                Class<?> implClass = resolveImplClass(rawType, customization);
                ClassModel classModel = jsonbContext.getMappingContext()
                        .getOrCreateClassModel(implClass);
                ModelDeserializer<JsonParser> chain = deserializerChain(classModel);
                return new NullDeserializer((value, context, rType) -> {
                    DeserializationContextImpl newContext = new DeserializationContextImpl(context);
                    return memberDeserializer.deserialize(chain.deserialize(value, newContext, type), context, rType);
                }, memberDeserializer, rawType);
            }
            return new NullDeserializer(new ValueExtractor(typeDeserializer), memberDeserializer, rawType);
        }
    }

    private Class<?> resolveImplClass(Class<?> rawType, Customization customization) {
        if (customization instanceof PropertyCustomization) {
            Class<?> implClass = ((PropertyCustomization) customization).getImplementationClass();
            if (implClass != null) {
                return implClass;
            }
        }
        return rawType;
    }

}
