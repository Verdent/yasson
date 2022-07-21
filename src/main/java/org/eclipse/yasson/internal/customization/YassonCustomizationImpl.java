package org.eclipse.yasson.internal.customization;

import java.util.Optional;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.serializer.JsonbDeserializer;

/**
 * TODO javadoc
 */
class YassonCustomizationImpl implements YassonCustomization {

    private final JsonbDeserializer<?> deserializer;
    private final JsonbAdapter<?, ?> adapter;
    private final boolean ignoreDeserializer;
    private final boolean ignoreAdapter;

    YassonCustomizationImpl(YassonCustomizationBuilder<?,?> builder) {
        this.deserializer = builder.getDeserializer();
        this.adapter = builder.getAdapter();
        this.ignoreDeserializer = builder.isIgnoreDeserializer();
        this.ignoreAdapter = builder.isIgnoreAdapter();
    }

    @Override
    public Optional<JsonbDeserializer<?>> getDeserializer() {
        return Optional.ofNullable(deserializer);
    }

    @Override
    public Optional<JsonbAdapter<?, ?>> getAdapter() {
        return Optional.ofNullable(adapter);
    }

    @Override
    public boolean ignoreDeserializer() {
        return ignoreDeserializer;
    }

    @Override
    public boolean ignoreAdapter() {
        return ignoreAdapter;
    }

}
