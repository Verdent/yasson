package org.eclipse.yasson.customization;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import jakarta.json.bind.serializer.JsonbSerializer;

/**
 * TODO javadoc
 */
class PropertyCustomizationImpl extends YassonCustomizationImpl implements PropertyCustomization {

    private final String propertyName;
    private final String deserializationName;
    private final String serializationName;
    private final NumberFormat serializationNumberFormat;
    private final NumberFormat deserializationNumberFormat;
    private final DateTimeFormatter serializationDateFormat;
    private final DateTimeFormatter deserializationDateFormat;
    private final JsonbSerializer<?> serializer;
    private final Boolean deserializationNillable;
    private final Boolean serializationNillable;
    private final Boolean deserializationTransient;
    private final Boolean serializationTransient;
    private final boolean ignoreSerializer;
    private final boolean ignoreDeserializationName;
    private final boolean ignoreSerializationName;
    private final boolean ignoreDeserializationNillable;
    private final boolean ignoreSerializationNillable;
    private final boolean ignoreDeserializationNumberFormat;
    private final boolean ignoreSerializationNumberFormat;
    private final boolean ignoreDeserializationDateFormat;
    private final boolean ignoreSerializationDateFormat;
    private final boolean ignoreDeserializationTransient;
    private final boolean ignoreSerializationTransient;

    PropertyCustomizationImpl(PropertyCustomizationBuilder builder) {
        super(builder);
        this.propertyName = builder.getPropertyName();
        this.serializationName = builder.getSerializationName();
        this.deserializationName = builder.getDeserializationName();
        this.serializationNumberFormat = builder.getSerializationNumberFormat();
        this.deserializationNumberFormat = builder.getDeserializationNumberFormat();
        this.serializationDateFormat = builder.getSerializationDateFormat();
        this.deserializationDateFormat = builder.getDeserializationDateFormat();
        this.serializer = builder.getSerializer();
        this.ignoreSerializer = builder.isIgnoreSerializer();
        this.serializationNillable = builder.isSerializationNillable();
        this.deserializationNillable = builder.isDeserializationNillable();
        this.serializationTransient = builder.isSerializationTransient();
        this.deserializationTransient = builder.isDeserializationTransient();
        this.ignoreSerializationName = builder.isIgnoreSerializationName();
        this.ignoreDeserializationName = builder.isIgnoreDeserializationName();
        this.ignoreSerializationNillable = builder.isIgnoreSerializationNillable();
        this.ignoreDeserializationNillable = builder.isIgnoreDeserializationNillable();
        this.ignoreSerializationNumberFormat = builder.isIgnoreSerializationNumberFormat();
        this.ignoreDeserializationNumberFormat = builder.isIgnoreDeserializationNumberFormat();
        this.ignoreSerializationDateFormat = builder.isIgnoreSerializationDateFormat();
        this.ignoreDeserializationDateFormat = builder.isIgnoreDeserializationDateFormat();
        this.ignoreSerializationTransient = builder.isIgnoreSerializationTransient();
        this.ignoreDeserializationTransient = builder.isIgnoreDeserializationTransient();
    }

    @Override
    public Optional<NumberFormat> getNumberFormat(Scope scope) {
        return Optional.ofNullable(getValue(scope, serializationNumberFormat, deserializationNumberFormat));
    }

    @Override
    public Optional<DateTimeFormatter> getDateFormat(Scope scope) {
        return Optional.ofNullable(getValue(scope, serializationDateFormat, deserializationDateFormat));
    }

    @Override
    public Optional<Boolean> getNillable(Scope scope) {
        return Optional.ofNullable(getValue(scope, serializationNillable, deserializationNillable));
    }

    @Override
    public boolean ignoreNumberFormat(Scope scope) {
        return getValue(scope, ignoreSerializationNumberFormat, ignoreDeserializationNumberFormat);
    }

    @Override
    public boolean ignoreDateFormat(Scope scope) {
        return getValue(scope, ignoreSerializationDateFormat, ignoreDeserializationDateFormat);
    }

    @Override
    public boolean ignoreNillable(Scope scope) {
        return getValue(scope, ignoreSerializationNillable, ignoreDeserializationNillable);
    }

    @Override
    public Optional<JsonbSerializer<?>> getSerializer() {
        return Optional.ofNullable(serializer);
    }

    @Override
    public boolean ignoreSerializer() {
        return ignoreSerializer;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public Optional<Boolean> getTransientProperty(Scope scope) {
        return Optional.ofNullable(getValue(scope, serializationTransient, deserializationTransient));
    }

    @Override
    public Optional<String> getName(Scope scope) {
        return Optional.ofNullable(getValue(scope, serializationName, deserializationName));
    }

    @Override
    public boolean ignoreName(Scope scope) {
        return getValue(scope, ignoreSerializationName, ignoreDeserializationName);
    }

    @Override
    public boolean ignoreTransientProperty(Scope scope) {
        return getValue(scope, ignoreSerializationTransient, ignoreDeserializationTransient);
    }

    private <T> T getValue(Scope scope, T serializationValue, T deserializationValue) {
        return scope == Scope.SERIALIZATION ? serializationValue : deserializationValue;
    }

}
