package org.eclipse.yasson.internal.processor.serializer.types;

import java.net.URL;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class UrlSerializer extends TypeSerializer<URL> {

    UrlSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(URL value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.toString());
    }

}
