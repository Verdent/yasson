package org.eclipse.yasson.internal.processor.deserializer.types;

/**
 * TODO javadoc
 */
class DoubleDeserializer extends AbstractNumberDeserializer<Double> {

    DoubleDeserializer(TypeDeserializerBuilder builder) {
        super(builder, false);
    }

    @Override
    Double parseNumberValue(String value) {
        return Double.parseDouble(value);
    }

}
