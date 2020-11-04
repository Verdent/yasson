package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.YassonParser;

/**
 * TODO javadoc
 */
public class UserDefinedDeserializer implements ModelDeserializer<JsonParser> {

    private final JsonbDeserializer<?> userDefinedDeserializer;
    private final ModelDeserializer<Object> delegate;

    public UserDefinedDeserializer(JsonbDeserializer<?> userDefinedDeserializer,
                                   ModelDeserializer<Object> delegate) {
        this.userDefinedDeserializer = userDefinedDeserializer;
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context, Type rType) {
        YassonParser yassonParser = new YassonParser(value, context.getLastValueEvent());
        DeserializationContextImpl newContext = new DeserializationContextImpl(context);
        Object object = userDefinedDeserializer.deserialize(yassonParser, newContext, rType);
        yassonParser.skipRemaining();
        return delegate.deserialize(object, context, rType);
    }

}
