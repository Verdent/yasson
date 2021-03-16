package org.eclipse.yasson.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.yasson.PolymorphicType.Format;

/**
 * TODO javadoc
 */
public class Polymorphism {

    private final Class<?> clazz;
    private final String keyName;
    private final Map<Class<?>, String> aliases;
    private final Set<String> whitelistedPackages;
    private final Format format;
    private final Boolean useClassNames;

    private Polymorphism(Builder builder) {
        this.clazz = builder.clazz;
        this.keyName = builder.keyName;
        this.aliases = Map.copyOf(builder.aliases);
        this.whitelistedPackages = Set.copyOf(builder.whitelistedPackages);
        this.format = builder.format;
        this.useClassNames = builder.useClassNames;
    }

    public static Builder builder(Class<?> clazz) {
        return new Builder(clazz);
    }

    public Class<?> getBoundClass() {
        return clazz;
    }

    public Optional<String> getKeyName() {
        return Optional.ofNullable(keyName);
    }

    public Map<Class<?>, String> getAliases() {
        return aliases;
    }

    public Optional<Format> getFormat() {
        return Optional.ofNullable(format);
    }

    public Optional<Boolean> useClassNames() {
        return Optional.ofNullable(useClassNames);
    }

    public Set<String> getWhitelistedPackages() {
        return whitelistedPackages;
    }

    public static final class Builder {

        private final Class<?> clazz;
        private final Map<Class<?>, String> aliases = new HashMap<>();
        private final Set<String> whitelistedPackages = new HashSet<>();
        private String keyName;
        private Format format;
        private Boolean useClassNames;

        private Builder(Class<?> clazz) {
            this.clazz = Objects.requireNonNull(clazz, "Polymorphism has to be bound to the specific class.");
        }

        public Builder keyName(String keyName) {
            this.keyName = Objects.requireNonNull(keyName, "Field name cannot be null");
            return this;
        }

        public Builder alias(Class<?> clazz, String alias) {
            aliases.put(clazz, alias);
            return this;
        }

        public Builder format(Format format) {
            this.format = Objects.requireNonNull(format);
            return this;
        }

        public Builder useClassNames(boolean useClassNames) {
            this.useClassNames = useClassNames;
            return this;
        }

        public Builder whitelist(String... packages) {
            this.whitelistedPackages.addAll(Arrays.asList(packages));
            return this;
        }

        public Polymorphism build() {
            return new Polymorphism(this);
        }

    }

}
