package org.eclipse.yasson.internal.processor.types;

/**
 * TODO javadoc
 */
class ByteDeserializer extends AbstractNumberDeserializer<Byte> {

    ByteDeserializer(TypeDeserializerBuilder builder) {
        super(builder, true);
    }

    @Override
    Byte parseNumberValue(String value) {
        return Byte.parseByte(value);
    }

}