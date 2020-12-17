package org.eclipse.yasson.internal.deserializer.types;

/**
 * TODO javadoc
 */
class FloatDeserializer extends AbstractNumberDeserializer<Float> {

    FloatDeserializer(TypeDeserializerBuilder builder) {
        super(builder, false);
    }

    @Override
    Float parseNumberValue(String value) {
        return Float.parseFloat(value);
    }

}
