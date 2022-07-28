package org.eclipse.yasson.customization;

import java.util.Optional;

/**
 * TODO javadoc
 */
class ScopelessCustomizationImpl extends YassonCustomizationImpl implements ScopelessCustomization {

    private final boolean ignoreNumberFormat;
    private final boolean ignoreDateFormat;
    private final NumberFormatCustomization numberFormat;
    private final DateFormatCustomization dateFormat;

    ScopelessCustomizationImpl(YassonCustomizationBuilder<?, ?> builder) {
        super(builder);
        this.numberFormat = builder.getNumberFormat();
        this.dateFormat = builder.getDateFormat();
        this.ignoreDateFormat = builder.isIgnoreDateFormat();
        this.ignoreNumberFormat = builder.isIgnoreNumberFormat();
    }

    @Override
    public Optional<NumberFormatCustomization> getNumberFormat() {
        return Optional.ofNullable(numberFormat);
    }

    @Override
    public Optional<DateFormatCustomization> getDateFormat() {
        return Optional.ofNullable(dateFormat);
    }

    @Override
    public boolean ignoreNumberFormat() {
        return ignoreNumberFormat;
    }

    @Override
    public boolean ignoreDateFormat() {
        return ignoreDateFormat;
    }

}
