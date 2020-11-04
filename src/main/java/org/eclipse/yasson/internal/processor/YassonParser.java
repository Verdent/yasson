package org.eclipse.yasson.internal.processor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;

/**
 * TODO javadoc
 */
public class YassonParser implements JsonParser {

    private final JsonParser delegate;
    private final Event firstEvent;
    private int level;

    public YassonParser(JsonParser delegate, Event firstEvent) {
        this.delegate = delegate;
        this.firstEvent = firstEvent;
        this.level = determineLevelValue();
    }

    private int determineLevelValue() {
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
        if (level < 1) {
            throw new NoSuchElementException("There are no more elements available!");
        }
        Event next = delegate.next();
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
        return delegate.getObject();
    }

    @Override
    public JsonValue getValue() {
        return delegate.getValue();
    }

    @Override
    public JsonArray getArray() {
        return delegate.getArray();
    }

    @Override
    public Stream<JsonValue> getArrayStream() {
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
        delegate.skipArray();
    }

    @Override
    public void skipObject() {
        delegate.skipObject();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
