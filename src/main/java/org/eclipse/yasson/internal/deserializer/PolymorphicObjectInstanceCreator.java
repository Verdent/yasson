package org.eclipse.yasson.internal.deserializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.PolymorphicType;
import org.eclipse.yasson.internal.DeserializationContextImpl;
import org.eclipse.yasson.internal.model.ClassModel;
import org.eclipse.yasson.internal.model.customization.PolymorphismConfig;

/**
 * TODO javadoc
 */
public class PolymorphicObjectInstanceCreator implements ModelDeserializer<JsonParser> {

    private final Map<String, Class<?>> resolvedClasses = new ConcurrentHashMap<>();
    private final ChainModelCreator chainModelCreator;
    private final PolymorphismConfig polymorphismConfig;
    private final ModelDeserializer<JsonParser> actualInstanceCreator;

    public PolymorphicObjectInstanceCreator(ChainModelCreator chainModelCreator,
                                            PolymorphismConfig polymorphismConfig) {
        this.chainModelCreator = chainModelCreator;
        this.polymorphismConfig = polymorphismConfig;
        this.actualInstanceCreator = actualCreator();
    }

    private ModelDeserializer<JsonParser> actualCreator() {
        if (polymorphismConfig.getAddAs() == PolymorphicType.Format.WRAPPING_OBJECT) {
            return new PolymorphicTypeAsKey();
        } else if (polymorphismConfig.getAddAs() == PolymorphicType.Format.WRAPPING_ARRAY) {
            return new PolymorphicTypeAsArrayValue();
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
                final JsonParser.Event next = parser.next();
                context.setLastValueEvent(next);
                if (done && next != JsonParser.Event.END_OBJECT) {
                    throw new JsonbException("Unexpected state: " + next);
                }
                switch (next) {
                case KEY_NAME:
                    alias = parser.getString();
                    break;
                case START_OBJECT:
                    Class<?> type = getPolymorphicTypeClass(alias);
                    ClassModel classModel = context.getMappingContext().getOrCreateClassModel(type);
                    ModelDeserializer<JsonParser> deserializer = chainModelCreator.deserializerChain(type, classModel);
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

    }

    private final class PolymorphicTypeAsArrayValue implements ModelDeserializer<JsonParser> {

        @Override
        public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
            boolean done = false;
            while (parser.hasNext()) {
                final JsonParser.Event next = parser.next();
                context.setLastValueEvent(next);
                if (done && (next != JsonParser.Event.END_ARRAY && next != JsonParser.Event.END_OBJECT)) {
                    throw new JsonbException("Unexpected state: " + next);
                }
                switch (next) {
                case KEY_NAME:
                    if (!parser.getString().equals(polymorphismConfig.getFieldName())) {
                        throw new JsonbException("Polymorphic field should be the first. Expected field: "
                                                         + polymorphismConfig.getFieldName());
                    }
                    break;
                case VALUE_STRING:
                    String alias = parser.getString();
                    Class<?> type = getPolymorphicTypeClass(alias);
                    ClassModel classModel = context.getMappingContext().getOrCreateClassModel(type);
                    ModelDeserializer<JsonParser> deserializer = chainModelCreator.deserializerChain(type, classModel);
                    deserializer.deserialize(parser, context);
                    done = true;
                    break;
                case END_OBJECT:
                case END_ARRAY:
                    return context.getInstance();
                default:
                    throw new JsonbException("Unexpected state: " + next);
                }
            }
            return context.getInstance();
        }
    }

    private final class PolymorphicTypeAsPropertyValue implements ModelDeserializer<JsonParser> {

        @Override
        public Object deserialize(JsonParser parser, DeserializationContextImpl context) {
            while (parser.hasNext()) {
                final JsonParser.Event next = parser.next();
                context.setLastValueEvent(next);
                switch (next) {
                case KEY_NAME:
                    if (!parser.getString().equals(polymorphismConfig.getFieldName())) {
                        throw new JsonbException("Polymorphic field should be the first. Expected field: "
                                                         + polymorphismConfig.getFieldName());
                    }
                    break;
                case VALUE_STRING:
                    String alias = parser.getString();
                    Class<?> type = getPolymorphicTypeClass(alias);
                    ClassModel classModel = context.getMappingContext().getOrCreateClassModel(type);
                    ModelDeserializer<JsonParser> deserializer = chainModelCreator.deserializerChain(type, classModel);
                    context.setDisableNextPositionCheck(true);
                    return deserializer.deserialize(parser, context);
                default:
                    throw new JsonbException("Unexpected state: " + next);
                }
            }
            return context.getInstance();
        }
    }
}
