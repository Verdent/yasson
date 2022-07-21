package org.eclipse.yasson.internal.customization;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Optional;

import jakarta.json.bind.serializer.JsonbSerializer;

/**
 * TODO javadoc
 */
class PropertyCustomizationImpl extends YassonCustomizationImpl implements PropertyCustomization {

    PropertyCustomizationImpl(PropertyCustomizationBuilder builder) {
        super(builder);
    }

    @Override
    public Optional<NumberFormat> getNumberFormat(Scope scope) {
        return Optional.empty();
    }

    @Override
    public Optional<DateFormat> getDateFormat(Scope scope) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getNillable(Scope scope) {
        return Optional.empty();
    }

    @Override
    public boolean ignoreNumberFormat(Scope scope) {
        return false;
    }

    @Override
    public boolean ignoreDateFormat(Scope scope) {
        return false;
    }

    @Override
    public boolean ignoreNillable(Scope scope) {
        return false;
    }

    @Override
    public Optional<JsonbSerializer<?>> getSerializer() {
        return Optional.empty();
    }

    @Override
    public boolean ignoreSerializer() {
        return false;
    }

    @Override
    public String getPropertyName() {
        return null;
    }

    @Override
    public Optional<Boolean> getTransientProperty(Scope scope) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getName(Scope scope) {
        return Optional.empty();
    }

    @Override
    public boolean ignoreName(Scope scope) {
        return false;
    }

    @Override
    public boolean ignoreTransientProperty(Scope scope) {
        return false;
    }
}
