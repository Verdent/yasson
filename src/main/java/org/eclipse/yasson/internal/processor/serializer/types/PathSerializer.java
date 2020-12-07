package org.eclipse.yasson.internal.processor.serializer.types;

import java.nio.file.Path;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class PathSerializer extends TypeSerializer<Path> {

    PathSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(Path value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.toString());
    }

}
