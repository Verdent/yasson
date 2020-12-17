package org.eclipse.yasson.internal.deserializer;

import org.eclipse.yasson.internal.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public interface ModelDeserializer<T> {

    Object deserialize(T value, DeserializationContextImpl context);

}
