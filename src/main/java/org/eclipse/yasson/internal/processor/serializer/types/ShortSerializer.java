package org.eclipse.yasson.internal.processor.serializer.types;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class ShortSerializer extends AbstractNumberSerializer<Short> {

    ShortSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(Short value, JsonGenerator generator) {
        generator.write(value);
    }

}
