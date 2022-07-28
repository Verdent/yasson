package org.eclipse.yasson.customization;

import java.util.Map;

/**
 * TODO javadoc
 */
public interface TypeInfoCustomization {

    static TypeInfoCustomizationBuilder builder(String fieldName) {
        return new TypeInfoCustomizationBuilder(fieldName);
    }

    String fieldName();

    Map<String, Class<?>> aliases();

}
