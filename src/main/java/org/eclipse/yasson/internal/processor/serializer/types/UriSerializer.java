package org.eclipse.yasson.internal.processor.serializer.types;

import java.net.URI;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class UriSerializer extends TypeSerializer<URI> {

    UriSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(URI value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.toString());
    }

}
