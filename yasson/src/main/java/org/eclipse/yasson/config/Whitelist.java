package org.eclipse.yasson.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO javadoc
 */
public class Whitelist {

    private final Set<String> allowedPackages;
    private final Set<Class<?>> allowedTypes;

    private Whitelist(Builder builder) {
        this.allowedPackages = Collections.unmodifiableSet(builder.allowedPackages);
        this.allowedTypes = Collections.unmodifiableSet(builder.allowedTypes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<String> getAllowedPackages() {
        return allowedPackages;
    }

    public Set<Class<?>> getAllowedTypes() {
        return allowedTypes;
    }

    public static class Builder {

        private final Set<String> allowedPackages = new HashSet<>();
        private final Set<Class<?>> allowedTypes = new HashSet<>();

        private Builder() {
        }

        public Builder packageName(String packageName) {
            allowedPackages.add(packageName);
            return this;
        }

        public Builder type(Class<?> clazz) {
            allowedTypes.add(clazz);
            return this;
        }

        public Whitelist build() {
            return new Whitelist(this);
        }

    }


}
