package org.eclipse.yasson.internal.processor.convertor;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
public interface TypeConvertor<T> {

    String serialize(T object, SerializationContextImpl context);

    T deserialize(String object, DeserializationContextImpl context);

}
