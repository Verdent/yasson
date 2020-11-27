package org.eclipse.yasson.internal.processor.deserializer.types;

import jakarta.json.JsonValue;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class JsonValueDeserializer implements ModelDeserializer<JsonParser> {

    private final ModelDeserializer<Object> delegate;

    public JsonValueDeserializer(TypeDeserializerBuilder builder) {
        delegate = builder.getDelegate();
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        JsonParser.Event last = context.getLastValueEvent();
        return delegate.deserialize(deserializeValue(last, value), context);
    }

    private JsonValue deserializeValue(JsonParser.Event last, JsonParser parser) {
        switch (last) {
        case VALUE_TRUE:
            return JsonValue.TRUE;
        case VALUE_FALSE:
            return JsonValue.FALSE;
        case VALUE_NULL:
            return JsonValue.NULL;
        case VALUE_STRING:
        case VALUE_NUMBER:
            return parser.getValue();
        case START_OBJECT:
            return parser.getObject();
        case START_ARRAY:
            return parser.getArray();
        default:
            throw new JsonbException(Messages.getMessage(MessageKeys.INTERNAL_ERROR, "Unknown JSON value: " + last));
        }
    }
}
