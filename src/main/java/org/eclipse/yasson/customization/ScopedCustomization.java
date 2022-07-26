/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.eclipse.yasson.customization;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Customization methods with the scope.
 */
public interface ScopedCustomization extends SerializationCustomization {

    /**
     * Return {@link NumberFormat} specified to the required {@link Scope}.
     *
     * @param scope required scope
     * @return specified {@link NumberFormat} instance, otherwise empty
     */
    Optional<NumberFormat> getNumberFormat(Scope scope);

    /**
     * Return {@link DateFormat} specified to the required {@link Scope}.
     *
     * @param scope required scope
     * @return specified {@link DateFormat} instance, otherwise empty
     */
    Optional<DateTimeFormatter> getDateFormat(Scope scope);

    /**
     * Return whether the component can be nillable in the given {@link Scope}.
     * If no explicit value has been set, empty optional is returned.
     *
     * @param scope required scope
     * @return property nillable state, if not explicitly set empty is returned
     */
    Optional<Boolean> getNillable(Scope scope);

    /**
     * Whether number format specified on the customized component in the given {@link Scope} should be ignored.
     *
     * @param scope required scope
     * @return number format should be ignored
     */
    boolean ignoreNumberFormat(Scope scope);

    /**
     * Whether date format specified on the customized component in the given {@link Scope} should be ignored.
     *
     * @param scope required scope
     * @return date format should be ignored
     */
    boolean ignoreDateFormat(Scope scope);

    /**
     * Whether nillable specified on the customized component in the given {@link Scope} should be ignored.
     *
     * @param scope required scope
     * @return nillable should be ignored
     */
    boolean ignoreNillable(Scope scope);

}
