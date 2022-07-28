package org.eclipse.yasson.customization;

import java.util.Map;
import java.util.Optional;

import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.json.bind.serializer.JsonbSerializer;

/**
 * TODO javadoc
 */
class TypeCustomizationImpl extends ScopelessCustomizationImpl implements TypeCustomization {

    private final Class<?> type;
    private final boolean ignoreSerializer;
    private final boolean ignoreOrder;
    private final boolean ignoreVisibleStrategy;
    private final boolean ignoreTypeInfo;
    private final JsonbSerializer<?> serializer;
    private final String[] propertyOrder;
    private final PropertyVisibilityStrategy visibilityStrategy;
    private final Map<String, PropertyCustomization> propertyCustomizations;
    private final CreatorCustomization creatorCustomization;
    private final TypeInfoCustomization typeInfoCustomization;

    TypeCustomizationImpl(TypeCustomizationBuilder builder) {
        super(builder);
        this.type = builder.getTypeClass();
        this.serializer = builder.getSerializer();
        this.ignoreSerializer = builder.isIgnoreSerializer();
        this.propertyOrder = builder.getPropertyOrder();
        this.visibilityStrategy = builder.getVisibleStrategy();
        this.ignoreOrder = builder.isIgnorePropertyOrder();
        this.ignoreVisibleStrategy = builder.isIgnoreVisibilityStrategy();
        this.propertyCustomizations = Map.copyOf(builder.getPropertyCustomizations());
        this.creatorCustomization = builder.getCreatorCustomization();
        this.typeInfoCustomization = builder.getTypeInfoCustomization();
        this.ignoreTypeInfo = builder.isIgnoreTypeInfo();
    }

    @Override
    public Optional<JsonbSerializer<?>> getSerializer() {
        return Optional.ofNullable(serializer);
    }

    @Override
    public boolean ignoreSerializer() {
        return ignoreSerializer;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Optional<String[]> getPropertyOrder() {
        return Optional.ofNullable(propertyOrder);
    }

    @Override
    public Optional<PropertyVisibilityStrategy> getVisibilityStrategy() {
        return Optional.ofNullable(visibilityStrategy);
    }

    @Override
    public Map<String, PropertyCustomization> getProperties() {
        return propertyCustomizations;
    }

    @Override
    public Optional<CreatorCustomization> getCreator() {
        return Optional.ofNullable(creatorCustomization);
    }

    @Override
    public Optional<TypeInfoCustomization> getTypeInfo() {
        return Optional.ofNullable(typeInfoCustomization);
    }

    @Override
    public boolean ignoreTypeInfo() {
        return ignoreTypeInfo;
    }

    @Override
    public boolean ignorePropertyOrder() {
        return ignoreOrder;
    }

    @Override
    public boolean ignoreVisibilityStrategy() {
        return ignoreVisibleStrategy;
    }
}
