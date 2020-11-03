package org.eclipse.yasson.internal.processor.types;

/**
 * TODO javadoc
 */
class IntegerDeserializer extends AbstractNumberDeserializer<Integer> {

    IntegerDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true, Integer.class);
    }

    @Override
    Integer parseNumberValue(String value) {
        return Integer.parseInt(value);
    }

}
