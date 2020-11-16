package org.eclipse.yasson.internal.processor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;

import static jakarta.json.stream.JsonParser.Event.START_ARRAY;
import static jakarta.json.stream.JsonParser.Event.START_OBJECT;

/**
 * TODO javadoc
 */
public class YassonParser implements JsonParser {

    private final JsonParser delegate;
    private final DeserializationContextImpl context;
    private int level;

    public YassonParser(JsonParser delegate, Event firstEvent, DeserializationContextImpl context) {
        this.delegate = delegate;
        this.context = context;
        this.level = determineLevelValue(firstEvent);
    }

    private int determineLevelValue(Event firstEvent) {
        switch (firstEvent) {
        case START_ARRAY:
        case START_OBJECT:
            return 1; //container start, there will be more events to come
        default:
            return 0; //just this single value, do not allow reading more
        }
    }

    public void skipRemaining() {
        while(hasNext()) {
            next();
        }
    }

    @Override
    public boolean hasNext() {
        if (level < 1) {
            return false;
        }
        return delegate.hasNext();
    }

    @Override
    public Event next() {
        validate();
        Event next = delegate.next();
        context.setLastValueEvent(next);
        switch (next) {
        case START_OBJECT:
        case START_ARRAY:
            level++;
            break;
        case END_OBJECT:
        case END_ARRAY:
            level--;
            break;
        }
        return next;
    }

    @Override
    public String getString() {
        return delegate.getString();
    }

    @Override
    public boolean isIntegralNumber() {
        return delegate.isIntegralNumber();
    }

    @Override
    public int getInt() {
        return delegate.getInt();
    }

    @Override
    public long getLong() {
        return delegate.getLong();
    }

    @Override
    public BigDecimal getBigDecimal() {
        return delegate.getBigDecimal();
    }

    @Override
    public JsonLocation getLocation() {
        return delegate.getLocation();
    }

    @Override
    public JsonObject getObject() {
        validate();
        level--;
        return delegate.getObject();
    }

    @Override
    public JsonValue getValue() {
        return delegate.getValue();
    }

    @Override
    public JsonArray getArray() {
        validate();
        level--;
        return delegate.getArray();
    }

    @Override
    public Stream<JsonValue> getArrayStream() {
        validate();
        level--;
        return delegate.getArrayStream();
    }

    @Override
    public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
        return delegate.getObjectStream();
    }

    @Override
    public Stream<JsonValue> getValueStream() {
        return delegate.getValueStream();
    }

    @Override
    public void skipArray() {
        validate();
        level--;
        delegate.skipArray();
    }

    @Override
    public void skipObject() {
        validate();
        level--;
        delegate.skipObject();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    private void validate() {
        if (level < 1) {
            throw new NoSuchElementException("There are no more elements available!");
        }
    }
}
