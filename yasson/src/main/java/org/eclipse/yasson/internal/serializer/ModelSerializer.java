package org.eclipse.yasson.internal.serializer;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * TODO javadoc
 */
public interface ModelSerializer {

    void serialize(Object value, JsonGenerator generator, SerializationContextImpl context);

}
