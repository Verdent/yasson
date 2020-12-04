package org.eclipse.yasson.internal.processor.serializer;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
public class KeyWriter implements ModelSerializer {

    private final ModelSerializer delegate;

    public KeyWriter(ModelSerializer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        if (context.getKey() != null) {
            generator.writeKey(context.getKey());
            context.setKey(null);
        }
        delegate.serialize(value, generator, context);
    }

}
