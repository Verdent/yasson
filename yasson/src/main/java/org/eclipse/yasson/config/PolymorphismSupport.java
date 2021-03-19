package org.eclipse.yasson.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * TODO javadoc
 */
public class PolymorphismSupport {

    private final Map<Class<?>, Polymorphism> classPolymorphism;
    private final Set<String> whitelistedPackages;
    private final String keyName;
    private final Boolean useClassNames;

    private PolymorphismSupport(Builder builder) {
        this.classPolymorphism = builder.classPolymorphism;
        this.keyName = builder.keyName;
        this.useClassNames = builder.useClassNames;
        this.whitelistedPackages = Set.copyOf(builder.whitelistedPackages);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<Polymorphism> getClassPolymorphism(Class<?> clazz) {
        if (classPolymorphism.containsKey(clazz)) {
            return Optional.of(classPolymorphism.get(clazz));
        } else {
            for (Map.Entry<Class<?>, Polymorphism> entry : classPolymorphism.entrySet()) {
                if (entry.getKey().isAssignableFrom(clazz)) {
                    return Optional.of(entry.getValue());
                }
            }
        }
        return Optional.empty();
    }

    public Optional<String> getKeyName() {
        return Optional.ofNullable(keyName);
    }

    public Optional<Boolean> useClassNames() {
        return Optional.ofNullable(useClassNames);
    }

    public Set<String> getWhitelistedPackages() {
        return whitelistedPackages;
    }

    public static final class Builder {

        private final Map<Class<?>, Polymorphism> classPolymorphism = new HashMap<>();
        private final Set<String> whitelistedPackages = new HashSet<>();
        private String keyName;
        private Boolean useClassNames;

        private Builder() {
        }

        public Builder keyName(String keyName) {
            this.keyName = Objects.requireNonNull(keyName);
            return this;
        }

        public Builder useClassNames(boolean useClassNames) {
            this.useClassNames = useClassNames;
            return this;
        }

        public Builder polymorphism(Polymorphism polymorphism) {
            Objects.requireNonNull(polymorphism);
            this.classPolymorphism.put(polymorphism.getBoundClass(), polymorphism);
            return this;
        }

        public Builder whitelist(String... packages) {
            this.whitelistedPackages.addAll(Arrays.asList(packages));
            return this;
        }

        public PolymorphismSupport build() {
            return new PolymorphismSupport(this);
        }

    }

}
