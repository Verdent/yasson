package org.eclipse.yasson.internal.processor.serializer;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
class NullVisibilitySwitcher implements ModelSerializer {

    private final boolean nullsEnabled;
    private final ModelSerializer delegate;

    public NullVisibilitySwitcher(boolean nullsEnabled, ModelSerializer delegate) {
        this.nullsEnabled = nullsEnabled;
        this.delegate = delegate;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        boolean previous = context.isContainerWithNulls();
        context.setContainerWithNulls(nullsEnabled);
        delegate.serialize(value, generator, context);
        context.setContainerWithNulls(previous);
    }
}
