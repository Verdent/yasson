package org.eclipse.yasson.internal.customization;

import java.util.List;
import java.util.Optional;

class CreatorCustomizationImpl implements CreatorCustomization {

    private final String methodName;
    private final List<ParamCustomization> paramCustomizations;

    CreatorCustomizationImpl(CreatorCustomizationBuilder builder) {
        this.methodName = builder.getMethodName();
        this.paramCustomizations = List.copyOf(builder.getParamCustomizations());
    }

    @Override
    public Optional<String> getFactoryMethodName() {
        return Optional.ofNullable(methodName);
    }

    @Override
    public List<ParamCustomization> getParams() {
        return paramCustomizations;
    }

}
