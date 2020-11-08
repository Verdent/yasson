package org.eclipse.yasson.internal.processor.deserializer;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public interface ModelDeserializer<T> {

    Object deserialize(T value, DeserializationContextImpl context);

}
