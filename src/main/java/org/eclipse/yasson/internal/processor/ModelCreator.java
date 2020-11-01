package org.eclipse.yasson.internal.processor;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.MappingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.CreatorModel;
import org.eclipse.yasson.internal.model.JsonbCreator;
import org.eclipse.yasson.internal.model.PropertyModel;
import org.eclipse.yasson.internal.model.customization.ClassCustomization;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertor;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertors;
import org.eclipse.yasson.internal.processor.deserializer.CollectionDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.CollectionDynamicTypeDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.CollectionInstanceDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.DelayedDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.DynamicTypeDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.FieldDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.JustReturn;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ObjectDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ObjectInstanceCreator;
import org.eclipse.yasson.internal.processor.deserializer.ObjectInstanceDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.SetterDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.TypeDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ValueExtractor;

/**
 * TODO javadoc
 */
public class ModelCreator {

    private static final Map<Class<?>, ModelDeserializer<JsonParser>> DESERIALIZER_CHAINS = new ConcurrentHashMap<>();

    public static ModelDeserializer<JsonParser> getOrCreateProcessorChain(ClassModel classModel, MappingContext mappingContext) {
        if (DESERIALIZER_CHAINS.containsKey(classModel.getType())) {
            return DESERIALIZER_CHAINS.get(classModel.getType());
        }
        if (Collection.class.isAssignableFrom(classModel.getType())) {
            return DESERIALIZER_CHAINS.computeIfAbsent(classModel.getType(), coll -> {
                CollectionDeserializer collectionDeserializer =
                        new CollectionDeserializer(new CollectionDynamicTypeDeserializer());
                return new CollectionInstanceDeserializer(collectionDeserializer, classModel.getType());
            });
        } else {
            ClassCustomization classCustomization = classModel.getClassCustomization();
            JsonbCreator creator = classCustomization.getCreator();
            boolean hasCreator = creator != null;
            List<String> params = hasCreator ? creatorParamsList(creator) : Collections.emptyList();
            Map<String, ModelDeserializer<JsonParser>> processors = new LinkedHashMap<>();
            for (PropertyModel propertyModel : classModel.getSortedProperties()) {
                ModelDeserializer<JsonParser> modelDeserializer = memberTypeProcessor(propertyModel, mappingContext, hasCreator,
                                                                                      params.contains(propertyModel
                                                                                                              .getReadName()));
                processors.put(propertyModel.getReadName(), modelDeserializer);
            }
            for (String s : params) {
                if (!processors.containsKey(s)) {
                    CreatorModel creatorModel = creator.findByName(s);
                    ModelDeserializer<JsonParser> modelDeserializer = typeProcessor(mappingContext,
                                                                                    creatorModel.getType(),
                                                                                    JustReturn.create());
                    processors.put(creatorModel.getName(), modelDeserializer);
                }
            }
            ModelDeserializer<JsonParser> instanceCreator;
            if (hasCreator) {
                instanceCreator = new ObjectInstanceCreator(processors, creator, classModel.getType());
            } else {
                ModelDeserializer<JsonParser> typeWrapper = new ObjectDeserializer(processors);
                instanceCreator = new ObjectInstanceDeserializer(typeWrapper, classModel.getType());
            }
            DESERIALIZER_CHAINS.put(classModel.getType(), instanceCreator);
        }
        return DESERIALIZER_CHAINS.get(classModel.getType());
    }

    private static List<String> creatorParamsList(JsonbCreator creator) {
        return Arrays.stream(creator.getParams()).map(CreatorModel::getName).collect(Collectors.toList());
    }

    private static ModelDeserializer<JsonParser> memberTypeProcessor(PropertyModel propertyModel,
                                                                     MappingContext mappingContext,
                                                                     boolean hasCreator,
                                                                     boolean isCreatorParam) {
        ModelDeserializer<Object> memberDeserializer;
        Method setter = propertyModel.getSetter();
        Type type = propertyModel.getPropertyDeserializationType();
        if (isCreatorParam) {
            memberDeserializer = JustReturn.create();
        } else if (setter != null) {
            memberDeserializer = new SetterDeserializer(setter);
        } else {
            memberDeserializer = new FieldDeserializer(propertyModel.getField());
        }
        if (hasCreator && !isCreatorParam) {
            memberDeserializer = new DelayedDeserializer(memberDeserializer);
        }
        return typeProcessor(mappingContext, type, memberDeserializer);
    }

    private static ModelDeserializer<JsonParser> typeProcessor(MappingContext mappingContext,
                                                               Type type,
                                                               ModelDeserializer<Object> memberDeserializer) {
        if (type instanceof TypeVariable || type instanceof WildcardType) {
            return new DynamicTypeDeserializer(memberDeserializer, type);
        }
        Class<?> rawType = ReflectionUtils.getRawType(type);
        if (Collection.class.isAssignableFrom(rawType)) {
            ModelDeserializer<JsonParser> modelDeserializer = DESERIALIZER_CHAINS.computeIfAbsent(rawType, coll -> {
                CollectionDeserializer collectionDeserializer =
                        new CollectionDeserializer(new CollectionDynamicTypeDeserializer());
                return new CollectionInstanceDeserializer(collectionDeserializer, rawType);
            });
            return (value, context, rType) -> {
                DeserializationContextImpl ctx = new DeserializationContextImpl(context);
                return memberDeserializer.deserialize(modelDeserializer.deserialize(value, ctx, type), context, rType);
            };
        } else {
            TypeConvertor<?> convertor = TypeConvertors.getConvertor(rawType);
            if (convertor == null) {
                ClassModel classModel = mappingContext.getOrCreateClassModel(rawType);
                ModelDeserializer<JsonParser> chain = getOrCreateProcessorChain(classModel, mappingContext);
                return (value, context, rType) -> {
                    DeserializationContextImpl newContext = new DeserializationContextImpl(context);
                    return memberDeserializer.deserialize(chain.deserialize(value, newContext, type), context, rType);
                };
            }
            TypeDeserializer typeDeserializer = new TypeDeserializer(memberDeserializer, convertor);
            return new ValueExtractor(typeDeserializer);
        }
    }

}
