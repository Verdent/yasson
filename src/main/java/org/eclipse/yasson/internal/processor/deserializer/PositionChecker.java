package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
import java.util.function.Function;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class PositionChecker implements ModelDeserializer<JsonParser> {

    private final Checker checker;
    private final ModelDeserializer<JsonParser> delegate;
    private final Type rType;

    public PositionChecker(Checker checker,
                           ModelDeserializer<JsonParser> delegate, Type rType) {
        this.checker = checker;
        this.delegate = delegate;
        this.rType = rType;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        if (!checker.check.apply(context.getLastValueEvent())) {
            JsonParser.Event next = value.next();
            context.setLastValueEvent(next);
            if (!checker.check.apply(next)) {
                throw new JsonbException("Incorrect position for processing type: " + rType + ". "
                                                 + "Received event: " + next);
            }
        }
        return delegate.deserialize(value, context);
    }

    public enum Checker {

        VALUE(event -> event == JsonParser.Event.VALUE_FALSE
                || event == JsonParser.Event.VALUE_TRUE
                || event == JsonParser.Event.VALUE_STRING
                || event == JsonParser.Event.VALUE_NUMBER
                || event == JsonParser.Event.VALUE_NULL),
        CONTAINER(event -> event == JsonParser.Event.START_OBJECT
                || event == JsonParser.Event.START_ARRAY);

        private final Function<JsonParser.Event, Boolean> check;

        Checker(Function<JsonParser.Event, Boolean> check) {
            this.check = check;
        }

    }

}
