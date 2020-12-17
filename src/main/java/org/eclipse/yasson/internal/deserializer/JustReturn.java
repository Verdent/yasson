package org.eclipse.yasson.internal.deserializer;

import org.eclipse.yasson.internal.DeserializationContextImpl;

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
    public Object deserialize(Object value, DeserializationContextImpl context) {
        return value;
    }
}
