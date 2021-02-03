package org.eclipse.yasson.internal.serializer;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.SerializationContextImpl;

/**
 * Switching mechanism for default null value visibility in the JSON.
 *
 * Some of the constructs such as arrays, collections etc. require to have nulls serialized into the JSON by default.
 * This class switches from the default parent null visibility to the current construct visibility. As soon as the current
 * construct is serialized, visibility is switched back to the parent ones.
 */
class NullVisibilitySwitcher implements ModelSerializer {

    private final boolean nullsEnabled;
    private final ModelSerializer delegate;

    NullVisibilitySwitcher(boolean nullsEnabled, ModelSerializer delegate) {
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
