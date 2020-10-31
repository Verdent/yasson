package org.eclipse.yasson.internal.processor.serializer;

import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
public interface ModelSerializer<T> {

    T serialize(Object value, SerializationContextImpl context);

}
