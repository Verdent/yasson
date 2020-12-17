package org.eclipse.yasson.internal.serializer.types;

import java.time.Period;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * TODO javadoc
 */
class PeriodSerializer extends TypeSerializer<Period> {

    PeriodSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(Period value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.toString());
    }

}
