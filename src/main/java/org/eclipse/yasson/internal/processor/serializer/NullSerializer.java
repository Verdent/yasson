package org.eclipse.yasson.internal.processor.serializer;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
public class NullSerializer implements ModelSerializer {

    private final ModelSerializer delegate;
    private final ModelSerializer nullSerializer;
    private final ModelSerializer rootNullSerializer;

    public NullSerializer(ModelSerializer delegate,
                          Customization customization,
                          JsonbSerializer<?> userDefinedNullSerializer) {
        this.delegate = delegate;
        if (customization.isNillable()) {
            nullSerializer = new NullWritingEnabled();
        } else {
            nullSerializer = new NullWritingDisabled();
        }
        if (userDefinedNullSerializer != null) {
            rootNullSerializer = (value, generator, context) -> userDefinedNullSerializer.serialize(null, generator, context);
        } else {
            rootNullSerializer = nullSerializer;
        }
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        if (value == null) {
            if (context.isRoot()) {
                context.setRoot(false);
                rootNullSerializer.serialize(null, generator, context);
            } else {
                nullSerializer.serialize(null, generator, context);
            }
        } else {
            context.setRoot(false);
            delegate.serialize(value, generator, context);
        }
    }

    private static final class NullWritingEnabled implements ModelSerializer {

        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
            if (context.getKey() == null) {
                generator.writeNull();
            } else {
                generator.writeNull(context.getKey());
            }
        }

    }

    private static class NullWritingDisabled implements ModelSerializer {

        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
            if (context.isContainerWithNulls()) {
                if (context.getKey() == null) {
                    generator.writeNull();
                } else {
                    generator.writeNull(context.getKey());
                }
            }
            context.setKey(null);
            //Do nothing
        }

    }
}
