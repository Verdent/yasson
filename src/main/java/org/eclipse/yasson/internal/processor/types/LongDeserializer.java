package org.eclipse.yasson.internal.processor.types;

/**
 * TODO javadoc
 */
class LongDeserializer extends AbstractNumberDeserializer<Long> {

    LongDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true);
    }

    @Override
    Long parseNumberValue(String value) {
        return Long.parseLong(value);
    }

}
