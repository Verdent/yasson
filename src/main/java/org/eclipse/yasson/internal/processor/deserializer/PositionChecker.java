package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

import static jakarta.json.stream.JsonParser.Event;

/**
 * TODO javadoc
 */
public class PositionChecker implements ModelDeserializer<JsonParser> {

    private static final Map<Event, Event> CLOSING_EVENTS = new HashMap<>();

    static {
        CLOSING_EVENTS.put(Event.START_ARRAY, Event.END_ARRAY);
        CLOSING_EVENTS.put(Event.START_OBJECT, Event.END_OBJECT);
    }

    private final Set<Event> expectedEvents;
    private final ModelDeserializer<JsonParser> delegate;
    private final Type rType;

    public PositionChecker(ModelDeserializer<JsonParser> delegate, Type rType, Checker checker) {
        this(checker.events, delegate, rType);
    }

    public PositionChecker(ModelDeserializer<JsonParser> delegate, Type rType, Event... events) {
        this(new HashSet<>(Arrays.asList(events)), delegate, rType);
    }

    private PositionChecker(Set<Event> expectedEvents,
                           ModelDeserializer<JsonParser> delegate, Type rType) {
        this.expectedEvents = expectedEvents;
        this.delegate = delegate;
        this.rType = rType;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        Event original = context.getLastValueEvent();
        Event startEvent = original;
        if (!expectedEvents.contains(startEvent)) {
            startEvent = value.next();
            context.setLastValueEvent(startEvent);
            if (!expectedEvents.contains(startEvent)) {
                throw new JsonbException("Incorrect position for processing type: " + rType + ". "
                                                 + "Received event: " + original + " "
                                                 + "Allowed: " + expectedEvents);
            }
        }
        Object o = delegate.deserialize(value, context);
        if (CLOSING_EVENTS.containsKey(startEvent)
                && CLOSING_EVENTS.get(startEvent) != context.getLastValueEvent()) {
            throw new JsonbException("Incorrect parser position after processing of the type: " + rType + ". "
                                             + "Start event: " + startEvent
                                             + "After processing event: " + context.getLastValueEvent());
        }
        return o;
    }

    public enum Checker {

        VALUES(Event.VALUE_FALSE,
               Event.VALUE_TRUE,
               Event.VALUE_STRING,
               Event.VALUE_NUMBER,
               Event.VALUE_NULL),
        CONTAINER(Event.START_OBJECT,
                  Event.START_ARRAY);

        private final Set<Event> events;

        Checker(Event...events) {
            this.events = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(events)));
        }

    }

}
