package org.eclipse.yasson.internal.processor.serializer.types;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.model.customization.Customization;

/**
 * TODO javadoc
 */
class TypeSerializerBuilder {

    private final List<Type> chain;
    private final Class<?> clazz;
    private final Customization customization;
    private final JsonbContext jsonbContext;

    TypeSerializerBuilder(List<Type> chain,
                          Class<?> clazz,
                          Customization customization,
                          JsonbContext jsonbContext) {
        this.chain = chain;
        this.clazz = clazz;
        this.customization = customization;
        this.jsonbContext = jsonbContext;
    }

    public List<Type> getChain() {
        return chain;
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
