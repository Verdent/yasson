package org.eclipse.yasson.customization.polymorphism;

import org.eclipse.yasson.adapters.model.Crate;
import org.eclipse.yasson.customization.TypeCustomization;
import org.junit.jupiter.api.Test;

/**
 * TODO javadoc
 */
public class ExistingClassCustomizationTest {

    @Test
    public void basicTest() {
        TypeCustomization.builder(Crate.class)
                .build();
    }

}
