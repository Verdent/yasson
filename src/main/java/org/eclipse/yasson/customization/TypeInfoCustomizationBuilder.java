package org.eclipse.yasson.customization;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TODO javadoc
 */
public class TypeInfoCustomizationBuilder {

    private final String fieldName;
    private final Map<String, Class<?>> aliases = new HashMap<>();

    TypeInfoCustomizationBuilder(String fieldName) {
        this.fieldName = Objects.requireNonNull(fieldName, "Field name is required to be set");
    }

    public TypeInfoCustomizationBuilder addAlias(String alias, Class<?> type) {
        aliases.put(alias, type);
        return this;
    }

    public TypeInfoCustomization build() {
        return new TypeInfoCustomizationImpl(this);
    }

    String getFieldName() {
        return fieldName;
    }

    Map<String, Class<?>> getAliases() {
        return aliases;
    }

}
