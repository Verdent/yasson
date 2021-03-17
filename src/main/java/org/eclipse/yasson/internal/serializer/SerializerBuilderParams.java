package org.eclipse.yasson.internal.serializer;

import java.lang.reflect.Type;
import java.util.Objects;

import jakarta.enterprise.inject.Model;
import org.eclipse.yasson.internal.model.customization.ClassCustomization;
import org.eclipse.yasson.internal.model.customization.Customization;

/**
 * TODO javadoc
 */
public class SerializerBuilderParams {

    private final Type type;
    private final Customization customization;
    private final boolean root;
    private final boolean key;
    private final boolean resolveRootAdapter;
    private final ModelSerializer objectBaseSerializer;

    private SerializerBuilderParams(Builder builder) {
        this.type = builder.type;
        this.customization = builder.customization;
        this.root = builder.root;
        this.key = builder.key;
        this.resolveRootAdapter = builder.resolveRootAdapter;
        this.objectBaseSerializer = builder.objectBaseSerializer;
    }

    public static Builder builder(Type type) {
        return new Builder(type);
    }

    public Type getType() {
        return type;
    }

    public Customization getCustomization() {
        return customization;
    }

    public boolean isRoot() {
        return root;
    }

    public boolean isKey() {
        return key;
    }

    public boolean isResolveRootAdapter() {
        return resolveRootAdapter;
    }

    public ModelSerializer getObjectBaseSerializer() {
        return objectBaseSerializer;
    }

    public static final class Builder {

        private Type type;
        private Customization customization;
        private boolean root;
        private boolean key;
        private boolean resolveRootAdapter;
        private ModelSerializer objectBaseSerializer;

        private Builder(Type type) {
            this.type = Objects.requireNonNull(type);
            this.customization = ClassCustomization.empty();
            this.root = true;
            this.key = false;
        }

        public Builder type(Type type) {
            this.type = Objects.requireNonNull(type);
            return this;
        }

        public Builder customization(Customization customization) {
            this.customization = Objects.requireNonNull(customization);
            return this;
        }

        public Builder root(boolean root) {
            this.root = root;
            return this;
        }

        public Builder key(boolean key) {
            this.key = key;
            return this;
        }

        public Builder resolveRootAdapter(boolean resolveRootAdapter) {
            this.resolveRootAdapter = resolveRootAdapter;
            return this;
        }

        public Builder objectBaseSerializer(ModelSerializer objectBaseSerializer) {
            this.objectBaseSerializer = objectBaseSerializer;
            return this;
        }

        public SerializerBuilderParams build() {
            return new SerializerBuilderParams(this);
        }

    }

}
