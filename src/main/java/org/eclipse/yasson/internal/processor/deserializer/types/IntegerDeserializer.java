package org.eclipse.yasson.internal.processor.deserializer.types;

/**
 * TODO javadoc
 */
class IntegerDeserializer extends AbstractNumberDeserializer<Integer> {

    IntegerDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true);
    }

    @Override
    Integer parseNumberValue(String value) {
        return Integer.parseInt(value);
    }

}
