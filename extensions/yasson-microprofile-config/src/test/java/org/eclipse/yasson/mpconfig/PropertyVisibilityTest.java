package org.eclipse.yasson.mpconfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.json.bind.config.PropertyVisibilityStrategy;

/**
 * TODO javadoc
 */
public class PropertyVisibilityTest implements PropertyVisibilityStrategy {
    @Override
    public boolean isVisible(Field field) {
        return true;
    }

    @Override
    public boolean isVisible(Method method) {
        return true;
    }
}
