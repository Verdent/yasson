package org.eclipse.yasson.internal.processor.types;

import java.util.Objects;

import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
class TypeDeserializerBuilder {

    private final Customization customization;
    private final JsonbConfigProperties configProperties;
    private final ModelDeserializer<Object> delegate;

    TypeDeserializerBuilder(Customization customization,
                            JsonbConfigProperties configProperties,
                            ModelDeserializer<Object> delegate) {
        this.customization = Objects.requireNonNull(customization);
        this.configProperties = Objects.requireNonNull(configProperties);
        this.delegate = Objects.requireNonNull(delegate);
    }

    public JsonbConfigProperties getConfigProperties() {
        return configProperties;
    }

    public ModelDeserializer<Object> getDelegate() {
        return delegate;
    }

    public Customization getCustomization() {
        return customization;
    }

}
