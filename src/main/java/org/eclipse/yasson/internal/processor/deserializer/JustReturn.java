package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class JustReturn implements ModelDeserializer<Object> {

    private static final JustReturn INSTANCE = new JustReturn();

    private JustReturn() {
    }

    public static JustReturn create() {
        return INSTANCE;
    }

    @Override
    public Object deserialize(Object value, DeserializationContextImpl context, Type rType) {
        return value;
    }
}
