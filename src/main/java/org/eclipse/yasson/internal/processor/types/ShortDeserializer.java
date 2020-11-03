package org.eclipse.yasson.internal.processor.types;

/**
 * TODO javadoc
 */
class ShortDeserializer extends AbstractNumberDeserializer<Short> {

    ShortDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true, Short.class);
    }

    @Override
    Short parseNumberValue(String value) {
        return Short.parseShort(value);
    }

}
