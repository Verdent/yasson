package org.eclipse.yasson.internal.processor.types;

/**
 * TODO javadoc
 */
class ShortDeserializer extends AbstractNumberDeserializer<Short> {

    ShortDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true);
    }

    @Override
    Short parseNumberValue(String value) {
        return Short.parseShort(value);
    }

}
