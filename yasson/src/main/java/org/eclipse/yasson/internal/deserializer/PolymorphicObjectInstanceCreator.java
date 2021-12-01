package org.eclipse.yasson.internal.deserializer;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;

import org.eclipse.yasson.PolymorphicType;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.model.customization.PolymorphismConfig;

import static jakarta.json.stream.JsonParser.Event;

/**
 * TODO javadoc
 */
class PolymorphicObjectInstanceCreator implements ModelDeserializer<JsonParser> {

    private final Map<String, Class<?>> resolvedClasses = new ConcurrentHashMap<>();
    private final ChainModelCreator chainModelCreator;
    private final PolymorphismConfig polymorphismConfig;
    private final ModelDeserializer<JsonParser> actualInstanceCreator;
    private final ModelDeserializer<JsonParser> defaultProcessor;

    public PolymorphicObjectInstanceCreator(ChainModelCreator chainModelCreator,
                                            PolymorphismConfig polymorphismConfig,
                                            ModelDeserializer<JsonParser> defaultProcessor) {
        this.chainModelCreator = chainModelCreator;
        this.polymorphismConfig = polymorphismConfig;
        this.defaultProcessor = defaultProcessor;
        this.actualInstanceCreator = actualCreator();
    }

    private ModelDeserializer<JsonParser> actualCreator() {
        if (polymorphismConfig.getAddAs() == PolymorphicType.Format.WRAPPING_OBJECT) {
            return new PolymorphicTypeAsKey();
        }
        return new PolymorphicTypeAsPropertyValue();
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
        if (context.isLastPolymorphismProcessed()) {
            context.setLastPolymorphismProcessed(false);
            return defaultProcessor.deserialize(parser, context);
        }
        return actualInstanceCreator.deserialize(parser, context);
    }

    private Class<?> getPolymorphicTypeClass(String alias) {
        Class<?> type = null;
        for (Map.Entry<Class<?>, String> entry : polymorphismConfig.getAliases().entrySet()) {
            if (entry.getValue().equals(alias)) {
                type = entry.getKey();
                break;
            }
        }
        if (type == null) {
            if (polymorphismConfig.useClassNames()) {
                if (resolvedClasses.containsKey(alias)) {
                    return resolvedClasses.get(alias);
                }
                checkWhitelistedClass(alias);
                try {
                    type = Class.forName(alias);
                    resolvedClasses.put(alias, type);
                } catch (ClassNotFoundException e) {
                    throw new JsonbException("Unknown alias \"" + alias + "\" or invalid class name. Known aliases: "
                                                     + polymorphismConfig.getAliases().values(), e);
                }
            } else {
                throw new JsonbException("Unknown alias \"" + alias + "\" known aliases: "
                                                 + polymorphismConfig.getAliases().values());
            }
        }
        return type;
    }

    private void checkWhitelistedClass(String className) {
        //if whitelist is empty, we accept everything
        String packageName = className.substring(0, className.lastIndexOf("."));
        if (!polymorphismConfig.getWhitelistedPackages().isEmpty()) {
            for (String pack : polymorphismConfig.getWhitelistedPackages()) {
                if (packageName.equals(pack)) {
                    return;
                } else if (pack.endsWith(".*") && packageName.startsWith(pack.substring(0, pack.lastIndexOf(".")))) {
                    return;
                }
            }
            throw new JsonbException("Class \"" + className + "\" does not belong to any whitelisted package: "
                                             + polymorphismConfig.getWhitelistedPackages());
        }
    }

    private final class PolymorphicTypeAsKey implements ModelDeserializer<JsonParser> {

        @Override
        public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
            String alias = null;
            boolean done = false;
            while (parser.hasNext()) {
                final Event next = parser.next();
                context.setLastValueEvent(next);
                if (done && next != Event.END_OBJECT) {
                    throw new JsonbException("Unexpected state: " + next);
                }
                switch (next) {
                case KEY_NAME:
                    alias = parser.getString();
                    break;
                case START_OBJECT:
                    Class<?> type = getPolymorphicTypeClass(alias);
                    //The ModelDeserializer which will be used for the polymorphic type will have polymorphic handling set up also.
                    //We want to avoid processing it again.
                    context.setLastPolymorphismProcessed(true);
                    ModelDeserializer<JsonParser> deserializer = chainModelCreator.deserializerChain(type);
                    deserializer.deserialize(parser, context);
                    done = true;
                    break;
                case END_OBJECT:
                    return context.getInstance();
                default:
                    throw new JsonbException("Unexpected state: " + next);
                }
            }
            return context.getInstance();
        }

        @Override
        public String toString() {
            return "Wrapping object polymorphic information handler";
        }
    }

    private final class PolymorphicTypeAsPropertyValue implements ModelDeserializer<JsonParser> {

        @Override
        public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
            String alias;
            JsonParser jsonParser;
            String polymorphismKeyName = polymorphismConfig.getFieldName();
            if (parser instanceof ObjectParser) {
                alias = ((ObjectParser) parser).getAlias(polymorphismKeyName);
                jsonParser = parser;
            } else {
                JsonObject object = parser.getObject();
                alias = object.getString(polymorphismKeyName, null);
                JsonObject newJsonObject = context.getJsonbContext().getJsonProvider().createObjectBuilder(object)
                        .remove(polymorphismKeyName)
                        .build();
                jsonParser = context.getJsonbContext().getJsonParserFactory().createParser(newJsonObject);
            }
            Class<?> polymorphicTypeClass;
            if (alias == null) {
                return defaultProcessor.deserialize(jsonParser, context);
            }
            polymorphicTypeClass = getPolymorphicTypeClass(alias);
            ModelDeserializer<JsonParser> deserializer = chainModelCreator.deserializerChain(polymorphicTypeClass);
            //The ModelDeserializer which will be used for the polymorphic type will have polymorphic handling set up also.
            //We want to avoid processing it again.
            context.setLastPolymorphismProcessed(true);
            return deserializer.deserialize(jsonParser, context);
        }

        @Override
        public String toString() {
            return "Property " + polymorphismConfig.getFieldName() + " polymorphic information handler";
        }
    }

    private static final class ObjectParser implements JsonParser {

        private final JsonObject jsonObject;
        private final JsonParser parser;

        private ObjectParser(JsonObject jsonObject, JsonParser parser) {
            this.jsonObject = jsonObject;
            this.parser = parser;
        }

        String getAlias(String propertyKey) {
            return jsonObject.getString(propertyKey, null);
        }

        @Override
        public boolean hasNext() {
            return parser.hasNext();
        }

        @Override
        public Event next() {
            return parser.next();
        }

        @Override
        public String getString() {
            return parser.getString();
        }

        @Override
        public boolean isIntegralNumber() {
            return parser.isIntegralNumber();
        }

        @Override
        public int getInt() {
            return parser.getInt();
        }

        @Override
        public long getLong() {
            return parser.getLong();
        }

        @Override
        public BigDecimal getBigDecimal() {
            return parser.getBigDecimal();
        }

        @Override
        public JsonLocation getLocation() {
            return parser.getLocation();
        }

        @Override
        public JsonObject getObject() {
            return parser.getObject();
        }

        @Override
        public JsonValue getValue() {
            return parser.getValue();
        }

        @Override
        public JsonArray getArray() {
            return parser.getArray();
        }

        @Override
        public Stream<JsonValue> getArrayStream() {
            return parser.getArrayStream();
        }

        @Override
        public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
            return parser.getObjectStream();
        }

        @Override
        public Stream<JsonValue> getValueStream() {
            return parser.getValueStream();
        }

        @Override
        public void skipArray() {
            parser.skipArray();
        }

        @Override
        public void skipObject() {
            parser.skipObject();
        }

        @Override
        public void close() {
            parser.close();
        }

        @Override
        public String toString() {
            return "JsonParser based on the buffered JsonObject";
        }
    }

    private static final class DelayedParser implements JsonParser {

        private final JsonParser parser;
        private final DeserializationContextImpl context;
        private final Queue<Map.Entry<Event, JsonValue>> postponedValues;
        private Map.Entry<Event, JsonValue> current = new AbstractMap.SimpleEntry<>(Event.START_OBJECT, null);

        private DelayedParser(JsonParser parser,
                              DeserializationContextImpl context,
                              Queue<Map.Entry<Event, JsonValue>> postponedValues) {
            this.parser = parser;
            this.context = context;
            this.postponedValues = postponedValues;
        }

        @Override
        public boolean hasNext() {
            return !postponedValues.isEmpty() || parser.hasNext();
        }

        @Override
        public Event next() {
            if (!postponedValues.isEmpty()) {
                current = postponedValues.poll();
                context.setLastValueEvent(current.getKey());
                return current.getKey();
            }
            current = null;
            Event next = parser.next();
            context.setLastValueEvent(next);
            return next;
        }

        @Override
        public String getString() {
            if (current == null) {
                return parser.getString();
            }
            Event currentEvent = current.getKey();
            if (currentEvent == Event.KEY_NAME
                    || currentEvent == Event.VALUE_STRING
                    || currentEvent == Event.VALUE_NUMBER) {
                return ((JsonString) current.getValue()).getString();
            }
            throw new IllegalStateException("Method getString() allowed only on events KEY_NAME, VALUE_STRING and VALUE_NUMBER");
        }

        @Override
        public boolean isIntegralNumber() {
            if (current == null) {
                return parser.isIntegralNumber();
            }
            Event currentEvent = current.getKey();
            if (currentEvent != Event.VALUE_NUMBER) {
                throw new IllegalStateException("Method isIntegralNumber() allowed only on events VALUE_NUMBER");
            }
            return ((JsonNumber) current.getValue()).isIntegral();
        }

        @Override
        public int getInt() {
            return parser.getInt();
        }

        @Override
        public long getLong() {
            return parser.getLong();
        }

        @Override
        public BigDecimal getBigDecimal() {
            return parser.getBigDecimal();
        }

        @Override
        public JsonLocation getLocation() {
            return parser.getLocation();
        }

        @Override
        public JsonObject getObject() {
            return parser.getObject();
        }

        @Override
        public JsonValue getValue() {
            return parser.getValue();
        }

        @Override
        public JsonArray getArray() {
            return parser.getArray();
        }

        @Override
        public Stream<JsonValue> getArrayStream() {
            return parser.getArrayStream();
        }

        @Override
        public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
            return parser.getObjectStream();
        }

        @Override
        public Stream<JsonValue> getValueStream() {
            return parser.getValueStream();
        }

        @Override
        public void skipArray() {
            parser.skipArray();
        }

        @Override
        public void skipObject() {
            parser.skipObject();
        }

        @Override
        public void close() {
            parser.close();
        }
    }
}
