package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.YassonParser;

/**
 * TODO javadoc
 */
public class UserDefinedDeserializer implements ModelDeserializer<JsonParser> {

    private final JsonbDeserializer<?> userDefinedDeserializer;
    private final ModelDeserializer<Object> delegate;
    private final Type rType;
    private final Customization customization;

    public UserDefinedDeserializer(JsonbDeserializer<?> userDefinedDeserializer,
                                   ModelDeserializer<Object> delegate,
                                   Type rType,
                                   Customization customization) {
        this.userDefinedDeserializer = userDefinedDeserializer;
        this.delegate = delegate;
        this.rType = rType;
        this.customization = customization;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        DeserializationContextImpl newContext = new DeserializationContextImpl(context);
        newContext.setCustomization(customization);
        YassonParser yassonParser = new YassonParser(value, context.getLastValueEvent(), newContext);
        Object object = userDefinedDeserializer.deserialize(yassonParser, newContext, rType);
        yassonParser.skipRemaining();
        return delegate.deserialize(object, context);
    }

}
