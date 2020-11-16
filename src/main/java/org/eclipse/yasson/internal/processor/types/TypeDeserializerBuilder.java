package org.eclipse.yasson.internal.processor.types;

import java.util.Objects;

import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.deserializer.ModelDeserializer;

/**
 * TODO javadoc
 */
class TypeDeserializerBuilder {

    private final Class<?> clazz;
    private final Customization customization;
    private final JsonbConfigProperties configProperties;
    private final ModelDeserializer<Object> delegate;

    TypeDeserializerBuilder(Class<?> clazz,
                            Customization customization,
                            JsonbConfigProperties configProperties,
                            ModelDeserializer<Object> delegate) {
        this.clazz = Objects.requireNonNull(clazz);
        this.customization = customization == null ? Customization.empty() : customization;
        this.configProperties = configProperties;
        this.delegate = Objects.requireNonNull(delegate);
    }

    public Class<?> getClazz() {
        return clazz;
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
