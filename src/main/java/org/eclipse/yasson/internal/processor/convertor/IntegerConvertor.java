package org.eclipse.yasson.internal.processor.convertor;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class IntegerConvertor implements TypeConvertor<Integer> {

    IntegerConvertor() {
    }

    @Override
    public String serialize(Integer object, SerializationContextImpl context) {
        return null;
    }

    @Override
    public Integer deserialize(String object, DeserializationContextImpl context) {
        //TODO formatovani cistel
        return Integer.parseInt(object);
    }
}
