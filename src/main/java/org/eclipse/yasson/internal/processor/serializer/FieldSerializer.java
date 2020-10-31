package org.eclipse.yasson.internal.processor.serializer;

import java.lang.reflect.Field;

import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
public final class FieldSerializer implements ModelSerializer<Object> {

    private Field field;

    public FieldSerializer(Field field) {
        this.field = field;
    }

    @Override
    public Object serialize(Object value, SerializationContextImpl context) {
        return null;
    }
}
