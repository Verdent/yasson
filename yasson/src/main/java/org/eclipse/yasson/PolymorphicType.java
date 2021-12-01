package org.eclipse.yasson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PolymorphicType {

    /**
     * Key name which contains information for polymorphism handling.
     *
     * @return key name
     */
    String key() default "";

    /**
     * Whether exact class names should be processed if no alias is specified.
     *
     * If set to false, an exception will be thrown.
     *
     * Default is set false.
     *
     * @return class names should be processed
     */
    boolean classNames() default false;

    /**
     * Specification of how the polymorphic information will be stored in the resulting JSON.
     *
     * @return polymorphic information storage type
     */
    Format format() default Format.PROPERTY;

    String[] allowedPackages() default {};

    enum Format {

        WRAPPING_OBJECT,
        PROPERTY

    }

}
