package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class CyrcularReferenceDeserializer implements ModelDeserializer<JsonParser> {

    private final Type type;
    private ModelDeserializer<JsonParser> delegate;

    public CyrcularReferenceDeserializer(Type type) {
        this.type = type;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        synchronized (type) {
            if (delegate == null) {
                Class<?> clazz = ReflectionUtils.getRawType(type);
                ClassModel classModel = context.getMappingContext().getOrCreateClassModel(clazz);
                delegate = context.getJsonbContext().getChainModelCreator().deserializerChain(type, classModel);
            }
        }
        return delegate.deserialize(value, context, rType);
    }
}
