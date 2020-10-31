package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class FieldDeserializer implements ModelDeserializer<Object> {

    private Field field;

    public FieldDeserializer(Field field) {
        this.field = field;
    }

    @Override
    public Object deserialize(Object value, DeserializationContextImpl context, Type rType) {
        try {
            field.setAccessible(true);
            field.set(context.getInstance(), value);
            field.setAccessible(false);
            return value;
        } catch (IllegalAccessException e) {
            throw new JsonbException("There has been an error while setting the value to the field.", e);
        }
    }


}
