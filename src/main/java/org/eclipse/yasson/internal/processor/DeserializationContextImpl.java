package org.eclipse.yasson.internal.processor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.ProcessingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
public class DeserializationContextImpl extends ProcessingContext implements DeserializationContext {

    private static final Logger LOGGER = Logger.getLogger(DeserializationContextImpl.class.getName());

    private final List<Type> rtypeChain;

    /**
     * Parent instance for marshaller and unmarshaller.
     *
     * @param jsonbContext context of Jsonb
     */
    public DeserializationContextImpl(JsonbContext jsonbContext) {
        super(jsonbContext);
        this.rtypeChain = new ArrayList<>();
    }

    public DeserializationContextImpl(DeserializationContextImpl context) {
        super(context.getJsonbContext());
        this.rtypeChain = context.getRtypeChain();
    }

    public List<Type> getRtypeChain() {
        return rtypeChain;
    }

    @Override
    public <T> T deserialize(Class<T> clazz, JsonParser parser) {
        return deserializeItem(clazz, parser);
    }

    @Override
    public <T> T deserialize(Type type, JsonParser parser) {
        return deserializeItem(type, parser);
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeItem(Type type, JsonParser parser) {
        try {
            parser.next();
            Class<?> rawType = ReflectionUtils.getRawType(type);
            ClassModel classModel = getMappingContext().getOrCreateClassModel(rawType);
            ModelDeserializer<JsonParser> modelDeserializer = ModelCreator.getOrCreateProcessorChain(classModel, getMappingContext());
            modelDeserializer.deserialize(parser, this, type);
            return (T) getInstance();
        } catch (JsonbException e) {
            LOGGER.severe(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            throw new JsonbException(Messages.getMessage(MessageKeys.INTERNAL_ERROR, e.getMessage()), e);
        }
    }

    private <T> JsonbDeserializer<T> initDeserializer(ClassModel classModel) {
        return null;
    }
}