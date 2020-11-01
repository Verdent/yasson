package org.eclipse.yasson.internal.processor;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.MappingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.VariableTypeInheritanceSearch;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.PropertyModel;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertor;
import org.eclipse.yasson.internal.processor.convertor.TypeConvertors;
import org.eclipse.yasson.internal.processor.deserializer.CollectionDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.CollectionDynamicTypeDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.CollectionInstanceDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.DynamicTypeDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.FieldDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.JustReturn;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.processor.deserializer.ObjectDeserializer;
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
            Map<String, ModelDeserializer<JsonParser>> processors = new LinkedHashMap<>();
            for (PropertyModel propertyModel : classModel.getSortedProperties()) {
                ModelDeserializer<JsonParser> modelDeserializer = memberTypeProcessor(propertyModel, mappingContext);
                processors.put(propertyModel.getReadName(), modelDeserializer);
            }
            ModelDeserializer<JsonParser> typeWrapper = new ObjectDeserializer(processors);
            ModelDeserializer<JsonParser> instanceCreator = new ObjectInstanceDeserializer(typeWrapper, classModel.getType());
            DESERIALIZER_CHAINS.put(classModel.getType(), instanceCreator);
        }
        return DESERIALIZER_CHAINS.get(classModel.getType());
    }

    private static ModelDeserializer<JsonParser> memberTypeProcessor(PropertyModel propertyModel,
                                                                     MappingContext mappingContext) {
        ModelDeserializer<Object> memberDeserializer;
        Method setter = propertyModel.getSetter();
        Type type = propertyModel.getPropertyDeserializationType();
        if (setter != null) {
            memberDeserializer = new SetterDeserializer(setter);
        } else {
            memberDeserializer = new FieldDeserializer(propertyModel.getField());
        }
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
                return memberDeserializer.deserialize(modelDeserializer.deserialize(value, ctx, rType), context, rType);
            };
        } else {
            TypeConvertor<?> convertor = TypeConvertors.getConvertor(rawType);
            if (convertor == null) {
                ClassModel classModel = mappingContext.getClassModel(rawType);
                ModelDeserializer<JsonParser> chain = getOrCreateProcessorChain(classModel, mappingContext);
                return (value, context, rType) -> {
                    DeserializationContextImpl newContext = new DeserializationContextImpl(context);
                    return memberDeserializer.deserialize(chain.deserialize(value, newContext, type), context, rType);
                };
            }
            TypeDeserializer typeDeserializer = new TypeDeserializer(memberDeserializer,
                                                                     convertor);
            return new ValueExtractor(typeDeserializer);
        }
    }

}
