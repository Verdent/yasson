package org.eclipse.yasson.customization;

import java.util.Map;

/**
 * TODO javadoc
 */
class TypeInfoCustomizationImpl implements TypeInfoCustomization {

    private final String fieldName;
    private final Map<String, Class<?>> aliases;

    TypeInfoCustomizationImpl(TypeInfoCustomizationBuilder builder) {
        this.fieldName = builder.getFieldName();
        this.aliases = Map.copyOf(builder.getAliases());
    }

    @Override
    public String fieldName() {
        return fieldName;
    }

    @Override
    public Map<String, Class<?>> aliases() {
        return aliases;
    }

}
