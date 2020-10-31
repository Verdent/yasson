/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.eclipse.yasson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import jakarta.json.bind.annotation.JsonbProperty;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.eclipse.yasson.Jsonbs.*;

/**
 * @author Roman Grigoriadi
 */
public class SimpleTest {

    @Test
    public void testSimpleSerialize() {
        final StringWrapper wrapper = new StringWrapper();
        wrapper.setValue("abc");
        bindingJsonb.toJson(wrapper);
        final String val = bindingJsonb.toJson(wrapper);
        assertEquals("{\"value\":\"abc\"}", val);
    }

    @Test
    public void testSimpleDeserializer() {
        Set<Integer> collection = defaultJsonb.fromJson("[1, 2, 3]", TreeSet.class);
        System.out.println();
//        final StringWrapper stringWrapper = defaultJsonb.fromJson("{\"value\":\"abc\", \"test\": [1, 2, 3]}", StringWrapper.class);
//        assertEquals("abc", stringWrapper.value);
    }

    public static class StringWrapper {

        private String value;

        public Collection<String> test;

//        public String getValue() {
//            return value;
//        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
