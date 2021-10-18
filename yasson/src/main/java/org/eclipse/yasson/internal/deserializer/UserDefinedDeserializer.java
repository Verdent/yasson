package org.eclipse.yasson.internal.deserializer;

import java.lang.reflect.Type;

import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.YassonParser;

/**
 * TODO javadoc
 */
class UserDefinedDeserializer implements ModelDeserializer<JsonParser> {

    private final JsonbDeserializer<?> userDefinedDeserializer;
//    private final ModelDeserializer<JsonParser> exactType;
    private final ModelDeserializer<Object> delegate;
    private final Type rType;
    private final Customization customization;

    //TODO remove or not? deserializer cycle
//    public UserDefinedDeserializer(JsonbDeserializer<?> userDefinedDeserializer,
//                                   ModelDeserializer<JsonParser> exactType,
//                                   ModelDeserializer<Object> delegate,
//                                   Type rType,
//                                   Customization customization) {
//        this.userDefinedDeserializer = userDefinedDeserializer;
//        this.exactType = exactType;
//        this.delegate = delegate;
//        this.rType = rType;
//        this.customization = customization;
//    }
    public UserDefinedDeserializer(JsonbDeserializer<?> userDefinedDeserializer,
//                                   ModelDeserializer<JsonParser> exactType,
                                   ModelDeserializer<Object> delegate,
                                   Type rType,
                                   Customization customization) {
        this.userDefinedDeserializer = userDefinedDeserializer;
//        this.exactType = exactType;
        this.delegate = delegate;
        this.rType = rType;
        this.customization = customization;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        DeserializationContextImpl newContext = new DeserializationContextImpl(context);
        newContext.setCustomization(customization);
        //TODO remove or not? deserializer cycle
//        if (context.getUserProcessorChain().contains(userDefinedDeserializer.getClass())) {
//            if (context.getLastValueEvent() != JsonParser.Event.START_ARRAY
//                    && context.getLastValueEvent() != JsonParser.Event.START_OBJECT) {
//                newContext.setDisableNextPositionCheck(true);
//            }
//            return exactType.deserialize(value, newContext);
//        }
        newContext.getUserProcessorChain().add(userDefinedDeserializer.getClass());
        YassonParser yassonParser = new YassonParser(value, context.getLastValueEvent(), newContext);
        Object object = userDefinedDeserializer.deserialize(yassonParser, newContext, rType);
        yassonParser.skipRemaining();
        context.setLastValueEvent(newContext.getLastValueEvent());
        return delegate.deserialize(object, context);
    }

}
