package org.eclipse.yasson.internal.processor.serializer.types;

import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.model.customization.Customization;

/**
 * TODO javadoc
 */
class TypeSerializerBuilder {

    private final Class<?> clazz;
    private final Customization customization;
    private final JsonbContext jsonbContext;

    TypeSerializerBuilder(Class<?> clazz, Customization customization, JsonbContext jsonbContext) {
        this.clazz = clazz;
        this.customization = customization;
        this.jsonbContext = jsonbContext;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Customization getCustomization() {
        return customization;
    }

    public JsonbContext getJsonbContext() {
        return jsonbContext;
    }
}
