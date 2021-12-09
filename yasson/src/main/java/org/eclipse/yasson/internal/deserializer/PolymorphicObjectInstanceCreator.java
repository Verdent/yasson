package org.eclipse.yasson.internal.deserializer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbPolymorphicType;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;

import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.jsonstructure.JsonStructureToParserAdapter;
import org.eclipse.yasson.internal.model.customization.PolymorphismConfig;

import static jakarta.json.stream.JsonParser.Event;

/**
 * TODO javadoc
 */
class PolymorphicObjectInstanceCreator implements ModelDeserializer<JsonParser> {

    private final Class<?> processedType;
    private final Map<String, Class<?>> resolvedClasses = new ConcurrentHashMap<>();
    private final ChainModelCreator chainModelCreator;
    private final PolymorphismConfig polymorphismConfig;
    private final ModelDeserializer<JsonParser> actualInstanceCreator;
    private final ModelDeserializer<JsonParser> defaultProcessor;

    public PolymorphicObjectInstanceCreator(Class<?> processedType,
                                            ChainModelCreator chainModelCreator,
                                            PolymorphismConfig polymorphismConfig,
                                            ModelDeserializer<JsonParser> defaultProcessor) {
        this.processedType = processedType;
        this.chainModelCreator = chainModelCreator;
        this.polymorphismConfig = polymorphismConfig;
        this.defaultProcessor = defaultProcessor;
        this.actualInstanceCreator = actualCreator();
    }

    private ModelDeserializer<JsonParser> actualCreator() {
        if (polymorphismConfig.getAddAs() == JsonbPolymorphicType.Format.WRAPPING_OBJECT) {
            return new PolymorphicTypeAsKey();
        }
        return new PolymorphicTypeAsPropertyValue();
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
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
        if (polymorphismConfig.getWhitelistedPackages().isEmpty()) {
            throw new JsonbException("Class \"" + processedType.getName() + "\" does support class name processing, "
                                             + "but does not contain allowed packages");
        }
        for (String pack : polymorphismConfig.getWhitelistedPackages()) {
            if (packageName.equals(pack)) {
                return;
            } else if (pack.endsWith(".*") && packageName.startsWith(pack.substring(0, pack.lastIndexOf(".")))) {
                return;
            }
        }
        throw new JsonbException("Class \"" + className + "\" does not belong to any allowed package: "
                                         + polymorphismConfig.getWhitelistedPackages());
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
                    if (type.equals(processedType)) {
                        defaultProcessor.deserialize(parser, context);
                    } else {
                        ModelDeserializer<JsonParser> deserializer = chainModelCreator.deserializerChain(type);
                        deserializer.deserialize(parser, context);
                    }
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
            JsonObject object = parser.getObject();
            alias = object.getString(polymorphismKeyName, null);
            JsonObject newJsonObject = context.getJsonbContext().getJsonProvider().createObjectBuilder(object)
                    .remove(polymorphismKeyName)
                    .build();
            jsonParser = new JsonStructureToParserAdapter(newJsonObject);
            Event event = jsonParser.next();//To get to the first event
            context.setLastValueEvent(event);
            Class<?> polymorphicTypeClass;
            if (alias == null) {
                return defaultProcessor.deserialize(jsonParser, context);
            }
            polymorphicTypeClass = getPolymorphicTypeClass(alias);
            if (polymorphicTypeClass.equals(processedType)) {
                return defaultProcessor.deserialize(jsonParser, context);
            }
            ModelDeserializer<JsonParser> deserializer = chainModelCreator.deserializerChain(polymorphicTypeClass);
            return deserializer.deserialize(jsonParser, context);
        }

        @Override
        public String toString() {
            return "Property " + polymorphismConfig.getFieldName() + " polymorphic information handler";
        }
    }

    private static final class DelayedParser implements JsonParser {

        private final JsonParser parser;
        private final Event startEvent;
        private boolean called = false;

        private DelayedParser(JsonParser parser, Event startEvent) {
            this.parser = parser;
            this.startEvent = startEvent;
        }

        @Override
        public boolean hasNext() {
            return called || parser.hasNext();
        }

        @Override
        public Event next() {
            if (!called) {
                called = true;
                return startEvent;
            }
            return parser.next();
        }

        @Override
        public String getString() {
            if (!called) {
                throw new IllegalStateException("Method getString() allowed only on events KEY_NAME, VALUE_STRING and VALUE_NUMBER");
            }
            return parser.getString();
        }

        @Override
        public boolean isIntegralNumber() {
            if (!called) {
                throw new IllegalStateException("Method isIntegralNumber() allowed only on events VALUE_NUMBER");
            }
            return parser.isIntegralNumber();
        }

        @Override
        public int getInt() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            return parser.getInt();
        }

        @Override
        public long getLong() {
            if (!called) {
                throw new IllegalStateException("Method getLong() allowed only on events VALUE_NUMBER");
            }
            return parser.getLong();
        }

        @Override
        public BigDecimal getBigDecimal() {
            if (!called) {
                throw new IllegalStateException("Method getBigDecimal() allowed only on events VALUE_NUMBER");
            }
            return parser.getBigDecimal();
        }

        @Override
        public JsonLocation getLocation() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            return parser.getLocation();
        }

        @Override
        public JsonObject getObject() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            return parser.getObject();
        }

        @Override
        public JsonValue getValue() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            return parser.getValue();
        }

        @Override
        public JsonArray getArray() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            return parser.getArray();
        }

        @Override
        public Stream<JsonValue> getArrayStream() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            return parser.getArrayStream();
        }

        @Override
        public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            return parser.getObjectStream();
        }

        @Override
        public Stream<JsonValue> getValueStream() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            return parser.getValueStream();
        }

        @Override
        public void skipArray() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            parser.skipArray();
        }

        @Override
        public void skipObject() {
            if (!called) {
                throw new IllegalStateException("Method getInt() allowed only on events VALUE_NUMBER");
            }
            parser.skipObject();
        }

        @Override
        public void close() {
            parser.close();
        }
    }
}
