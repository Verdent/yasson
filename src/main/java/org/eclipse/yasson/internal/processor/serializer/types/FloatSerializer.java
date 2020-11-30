package org.eclipse.yasson.internal.processor.serializer.types;

import java.math.BigDecimal;

import jakarta.json.stream.JsonGenerator;

/**
 * TODO javadoc
 */
class FloatSerializer extends AbstractNumberSerializer<Float> {

    FloatSerializer(TypeSerializerBuilder builder) {
        super(builder);
    }

    @Override
    void writeValue(Float value, JsonGenerator generator) {
        //floats lose precision, after upcasting to doubles in jsonp
        generator.write(new BigDecimal(String.valueOf(value)));
    }

}
