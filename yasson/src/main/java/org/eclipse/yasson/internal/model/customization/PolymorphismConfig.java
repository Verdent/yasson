package org.eclipse.yasson.internal.model.customization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.json.bind.annotation.JsonbPolymorphicType;

/**
 * TODO javadoc
 */
public class PolymorphismConfig {

    private final String fieldName;
    private final boolean useClassNames;
    private final boolean inherited;
    private final JsonbPolymorphicType.Format format;
    private final Map<Class<?>, String> aliases;
    private final Set<String> whitelistedPackages;
    private final PolymorphismConfig parentConfig;

    private PolymorphismConfig(Builder builder) {
        this.fieldName = builder.fieldName;
        this.inherited = builder.inherited;
        this.useClassNames = builder.useClassNames;
        this.format = builder.format;
        this.aliases = Map.copyOf(builder.aliases);
        this.whitelistedPackages = Set.copyOf(builder.whitelistedPackages);
        this.parentConfig = builder.parentConfig;
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

    public boolean useClassNames() {
        return useClassNames;
    }

    public JsonbPolymorphicType.Format getAddAs() {
        return format;
    }

    public Map<Class<?>, String> getAliases() {
        return aliases;
    }

    public Set<String> getWhitelistedPackages() {
        return whitelistedPackages;
    }

    public PolymorphismConfig getParentConfig() {
        return parentConfig;
    }

    public static final class Builder {

        public static final String DEFAULT_KEY_NAME = "@type";

        private Map<Class<?>, String> aliases = new HashMap<>();
        private Set<String> whitelistedPackages = new HashSet<>();
        private String fieldName = DEFAULT_KEY_NAME;
        private boolean useClassNames = false;
        private boolean inherited = false;
        private PolymorphismConfig parentConfig;
        private JsonbPolymorphicType.Format format = JsonbPolymorphicType.Format.PROPERTY;

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

        public Builder useClassNames(boolean useClassNames) {
            this.useClassNames = useClassNames;
            return this;
        }

        public Builder format(JsonbPolymorphicType.Format format) {
            this.format = format;
            return this;
        }

        public Builder alias(Class<?> clazz, String alias) {
            this.aliases.put(clazz, alias);
            return this;
        }

        public Builder whitelistedPackages(Set<String> whitelistedPackages) {
            this.whitelistedPackages.addAll(whitelistedPackages);
            return this;
        }

        public Builder parentConfig(PolymorphismConfig parentConfig) {
            this.parentConfig = parentConfig;
            return this;
        }

        public Builder of(PolymorphismConfig polymorphismConfig) {
            this.fieldName = polymorphismConfig.fieldName;
            this.aliases = new HashMap<>(polymorphismConfig.aliases);
            this.whitelistedPackages = new HashSet<>(polymorphismConfig.whitelistedPackages);
            this.format = polymorphismConfig.format;
            this.useClassNames = polymorphismConfig.useClassNames;
            this.inherited = polymorphismConfig.inherited;
            this.parentConfig = polymorphismConfig.parentConfig;
            return this;
        }

        public PolymorphismConfig build() {
            return new PolymorphismConfig(this);
        }
    }

}
