package org.eclipse.yasson.internal.processor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.ProcessingContext;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.customization.ClassCustomization;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
public class DeserializationContextImpl extends ProcessingContext implements DeserializationContext {

    private static final Logger LOGGER = Logger.getLogger(DeserializationContextImpl.class.getName());

    private final List<Runnable> delayedSetters = new ArrayList<>();
    private JsonParser.Event lastValueEvent;
    private Customization customization = ClassCustomization.empty();

    /**
     * Parent instance for marshaller and unmarshaller.
     *
     * @param jsonbContext context of Jsonb
     */
    public DeserializationContextImpl(JsonbContext jsonbContext) {
        super(jsonbContext);
    }

    public DeserializationContextImpl(DeserializationContextImpl context) {
        super(context.getJsonbContext());
        this.lastValueEvent = context.lastValueEvent;
    }

    public List<Runnable> getDelayedSetters() {
        return delayedSetters;
    }

    public JsonParser.Event getLastValueEvent() {
        return lastValueEvent;
    }

    public void setLastValueEvent(JsonParser.Event lastValueEvent) {
        this.lastValueEvent = lastValueEvent;
    }

    public Customization getCustomization() {
        return customization;
    }

    public void setCustomization(Customization customization) {
        this.customization = customization;
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
            if (lastValueEvent == null) {
                lastValueEvent = parser.next();
                checkState();
            }
            Class<?> rawType = ReflectionUtils.getRawType(type);
            ClassModel classModel = getMappingContext().getOrCreateClassModel(rawType);
            ModelDeserializer<JsonParser> modelDeserializer = getJsonbContext().getChainModelCreator()
                    .deserializerChain(type, classModel);
            return (T) modelDeserializer.deserialize(parser, this);
        } catch (JsonbException e) {
            LOGGER.severe(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            throw new JsonbException(Messages.getMessage(MessageKeys.INTERNAL_ERROR, e.getMessage()), e);
        }
    }

    private void checkState() {
        if (lastValueEvent == JsonParser.Event.KEY_NAME) {
            throw new JsonbException("JsonParser has incorrect position as the first event: KEY_NAME");
        }
    }
}
