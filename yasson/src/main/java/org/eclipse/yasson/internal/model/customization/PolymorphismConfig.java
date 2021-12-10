package org.eclipse.yasson.internal.model.customization;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.json.bind.annotation.JsonbPolymorphicType;

/**
 * TODO javadoc
 */
public class PolymorphismConfig {

    private final String fieldName;
    private final boolean inherited;
    private final Map<Class<?>, String> aliases;
    private final Class<?> definedType;
    private final PolymorphismConfig parentConfig;

    private PolymorphismConfig(Builder builder) {
        this.fieldName = builder.fieldName;
        this.inherited = builder.inherited;
        this.aliases = Map.copyOf(builder.aliases);
        this.parentConfig = builder.parentConfig;
        this.definedType = builder.definedType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isInherited() {
        return inherited;
    }

    public Map<Class<?>, String> getAliases() {
        return aliases;
    }

    public Class<?> getDefinedType() {
        return definedType;
    }

    public PolymorphismConfig getParentConfig() {
        return parentConfig;
    }

    public static final class Builder {

        private Map<Class<?>, String> aliases = new HashMap<>();
        private String fieldName = JsonbPolymorphicType.DEFAULT_KEY_NAME;
        private boolean inherited = false;
        private Class<?> definedType;
        private PolymorphismConfig parentConfig;

        private Builder() {
        }

        public Builder inherited(boolean inherited) {
            this.inherited = inherited;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = Objects.requireNonNull(fieldName);
            return this;
        }

        public Builder alias(Class<?> clazz, String alias) {
            this.aliases.put(clazz, alias);
            return this;
        }

        public Builder parentConfig(PolymorphismConfig parentConfig) {
            this.parentConfig = parentConfig;
            return this;
        }

        public Builder definedType(Class<?> definedType) {
            this.definedType = definedType;
            return this;
        }

        public Builder of(PolymorphismConfig polymorphismConfig) {
            this.fieldName = polymorphismConfig.fieldName;
            this.aliases = new HashMap<>(polymorphismConfig.aliases);
            this.inherited = polymorphismConfig.inherited;
            this.parentConfig = polymorphismConfig.parentConfig;
            this.definedType = polymorphismConfig.definedType;
            return this;
        }

        public PolymorphismConfig build() {
            return new PolymorphismConfig(this);
        }
    }

}
