package org.eclipse.yasson.internal.customization;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * TODO javadoc
 */
class ScopelessCustomizationImpl extends YassonCustomizationImpl implements ScopelessCustomization {

    private final Boolean nillable;
    private final boolean ignoreNumberFormat;
    private final boolean ignoreDateFormat;
    private final boolean ignoreNillable;
    private final NumberFormat numberFormat;
    private final DateTimeFormatter dateFormat;

    ScopelessCustomizationImpl(YassonCustomizationBuilder<?, ?> builder) {
        super(builder);
        this.numberFormat = builder.getNumberFormat();
        this.dateFormat = builder.getDateFormat();
        this.nillable = builder.isNillable();
        this.ignoreDateFormat = builder.isIgnoreDateFormat();
        this.ignoreNumberFormat = builder.isIgnoreNumberFormat();
        this.ignoreNillable = builder.isIgnoreNillable();
    }

    @Override
    public Optional<NumberFormat> getNumberFormat() {
        return Optional.ofNullable(numberFormat);
    }

    @Override
    public Optional<DateTimeFormatter> getDateFormat() {
        return Optional.ofNullable(dateFormat);
    }

    @Override
    public Optional<Boolean> getNillable() {
        return Optional.ofNullable(nillable);
    }

    @Override
    public boolean ignoreNumberFormat() {
        return ignoreNumberFormat;
    }

    @Override
    public boolean ignoreDateFormat() {
        return ignoreDateFormat;
    }

    @Override
    public boolean ignoreNillable() {
        return ignoreNillable;
    }
}
