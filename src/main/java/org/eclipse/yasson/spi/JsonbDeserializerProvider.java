package org.eclipse.yasson.spi;

import java.util.Set;

import jakarta.json.bind.serializer.JsonbDeserializer;

/**
 * TODO javadoc
 */
public interface JsonbDeserializerProvider {

    Set<JsonbDeserializer<?>> createDeserializers();

}
