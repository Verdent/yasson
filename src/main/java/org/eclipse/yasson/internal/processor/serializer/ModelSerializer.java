package org.eclipse.yasson.internal.processor.serializer;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
public interface ModelSerializer {

    void serialize(Object value, JsonGenerator generator, SerializationContextImpl context);

}
