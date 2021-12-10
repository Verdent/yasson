package org.eclipse.yasson.internal.deserializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.JsonObject;
import jakarta.json.bind.JsonbException;
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
    private final ModelDeserializer<JsonParser> defaultProcessor;

    public PolymorphicObjectInstanceCreator(Class<?> processedType,
                                            ChainModelCreator chainModelCreator,
                                            PolymorphismConfig polymorphismConfig,
                                            ModelDeserializer<JsonParser> defaultProcessor) {
        this.processedType = processedType;
        this.chainModelCreator = chainModelCreator;
        this.polymorphismConfig = polymorphismConfig;
        this.defaultProcessor = defaultProcessor;
    }

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

    private Class<?> getPolymorphicTypeClass(String alias) {
        if (resolvedClasses.containsKey(alias)) {
            return resolvedClasses.get(alias);
        }
        for (Map.Entry<Class<?>, String> entry : polymorphismConfig.getAliases().entrySet()) {
            if (entry.getValue().equals(alias)) {
                resolvedClasses.put(alias, entry.getKey());
                return entry.getKey();
            }
        }
        throw new JsonbException("Unknown alias \"" + alias + "\" known aliases: "
                                         + polymorphismConfig.getAliases().values());
    }

}
