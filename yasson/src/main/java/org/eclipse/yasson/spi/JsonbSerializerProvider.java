package org.eclipse.yasson.spi;

import java.util.Set;

import jakarta.json.bind.serializer.JsonbSerializer;

/**
 * TODO javadoc
 */
public interface JsonbSerializerProvider {

    Set<JsonbSerializer<?>> createSerializers();

}
