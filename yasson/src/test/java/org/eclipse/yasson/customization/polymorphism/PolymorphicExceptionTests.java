/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.eclipse.yasson.customization.polymorphism;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbPolymorphicType;

import org.eclipse.yasson.Jsonbs;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TODO javadoc
 */
public class PolymorphicExceptionTests {

    @Test
    public void testSerializationClassNamesWithoutAllowedPackages() {
        String expected = "{\"@type\":\"org.eclipse.yasson.customization.polymorphism."
                + "PolymorphicExceptionTests$ChildClassNamesWithoutAllowed\",\"parent\":1,\"child\":2}";
        assertThat(Jsonbs.defaultJsonb.toJson(new ChildClassNamesWithoutAllowed()), is(expected));
    }

    @Test
    public void testDeserializationClassNamesWithoutAllowedPackages() {
        String json = "{\"@type\":\"org.eclipse.yasson.customization.polymorphism."
                + "PolymorphicExceptionTests$ChildClassNamesWithoutAllowed\",\"parent\":1,\"child\":2}";
        assertThrows(JsonbException.class, () -> Jsonbs.defaultJsonb.fromJson(json, ParentClassNamesWithoutAllowed.class));
    }

    @JsonbPolymorphicType(classNames = true)
    public static class ParentClassNamesWithoutAllowed {
        public int parent = 1;
    }

    public static class ChildClassNamesWithoutAllowed extends ParentClassNamesWithoutAllowed {
        public int child = 2;
    }
}
