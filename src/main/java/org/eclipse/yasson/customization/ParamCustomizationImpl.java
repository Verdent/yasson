package org.eclipse.yasson.customization;

class ParamCustomizationImpl extends ScopelessCustomizationImpl implements ParamCustomization {

    private final String jsonName;
    private final Class<?> parameterType;

    ParamCustomizationImpl(ParamCustomizationBuilder builder) {
        super(builder);
        this.jsonName = builder.getJsonName();
        this.parameterType = builder.getParamClass();
    }

    @Override
    public String getJsonName() {
        return jsonName;
    }

    @Override
    public Class<?> getParameterType() {
        return parameterType;
    }

}
