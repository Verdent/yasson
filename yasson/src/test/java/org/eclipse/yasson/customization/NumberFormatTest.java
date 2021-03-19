/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.eclipse.yasson.customization;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import org.eclipse.yasson.customization.model.CollectionsWithFormatters;
import org.eclipse.yasson.customization.model.NumberFormatPojo;
import org.eclipse.yasson.customization.model.NumberFormatPojoWithoutClassLevelFormatter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests number format.
 * @author Roman Grigoriadi
 */
public class NumberFormatTest {
    private static final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withLocale(Locale.US));

    @Test
    public void testSerialize() {
        NumberFormatPojo pojo = new NumberFormatPojo();
        pojo.bigDecimal = BigDecimal.TEN;
        pojo.bigInteger = BigInteger.ONE;
        pojo.aDouble = .1d;
        pojo.aFloat = .35f;
        pojo.aLong = Long.MAX_VALUE;
        pojo.integer = Integer.MAX_VALUE;
        pojo.aShort = 1;
        pojo.aByte = 127;
        pojo.setDoubleGetterFormatted(.1d);
        pojo.setDoubleSetterFormatted(.5d);
        pojo.setDoubleSetterAndPropertyFormatter(0.6d);

        String expectedJson = "{\"aByte\":\"127\",\"aDouble\":\"000.10000000\",\"aFloat\":\"000.34999999\",\"aLong\":\"9223372036854775807\",\"aShort\":\"00001\",\"bigDecimal\":\"00000010.000000\",\"bigInteger\":\"00000001\",\"doubleGetterFormatted\":\"000.10000000\",\"doubleSetterAndPropertyFormatter\":\"000.600\",\"doubleSetterFormatted\":\"0.5\",\"integer\":\"2147483647.0\"}";

        assertEquals(expectedJson, jsonb.toJson(pojo));
    }

    @Test
    public void testDeserializer() {
        String expectedJson = "{\"aByte\":\"127\",\"aDouble\":\"000.10000000\",\"aFloat\":\"000.34999999\",\"aLong\":\"9223372036854775807\",\"aShort\":\"00001\",\"bigDecimal\":\"00000010.000000\",\"bigInteger\":\"00000001\",\"doubleGetterFormatted\":\"000.10000\",\"doubleSetterFormatted\":\",005\",\"doubleSetterAndPropertyFormatter\":\"000,600\",\"integer\":\"2147483647.0\"}";
        NumberFormatPojo pojo = jsonb.fromJson(expectedJson, NumberFormatPojo.class);

        assertEquals(BigDecimal.TEN, pojo.bigDecimal);
        assertEquals(BigInteger.ONE, pojo.bigInteger);
        assertEquals(Byte.valueOf((byte) 127), pojo.aByte);
        assertEquals(Double.valueOf(.1d), pojo.aDouble);
        assertEquals(Float.valueOf(.35f), pojo.aFloat);
        assertEquals((Integer)Integer.MAX_VALUE, pojo.integer);
        assertEquals(Short.valueOf((short) 1), pojo.aShort);
        assertEquals((Long)Long.MAX_VALUE, pojo.aLong);
        assertEquals(Double.valueOf(.1d), pojo.getDoubleGetterFormatted());
        assertEquals(Double.valueOf(.005d), pojo.getDoubleSetterFormatted());
        assertEquals(Double.valueOf(.6d), pojo.getDoubleSetterAndPropertyFormatter());
    }

    @Test
    public void testDeserializerTTTT() {
        String json = "{\"doubleList\":[\"001.200\",\"003.400\",\"005.600\"],\"doubleList2\":[\"1,2\",\"3,4\",\"5,6\"],"
                + "\"doubleList3\":[\"001.200\",\"003.400\",\"005.600\"]}";
        CollectionsWithFormatters pojo = new CollectionsWithFormatters();
        pojo.doubleList = List.of(1.2, 3.4, 5.6);
        pojo.doubleList2 = List.of(1.2, 3.4, 5.6);
        pojo.doubleList3 = List.of(1.2, 3.4, 5.6);
        assertThat(jsonb.toJson(pojo), is(json));
        CollectionsWithFormatters deserialized = jsonb.fromJson(json, CollectionsWithFormatters.class);
        assertThat(deserialized.doubleList, is(pojo.doubleList));
        assertThat(deserialized.doubleList2, is(pojo.doubleList2));
        assertThat(deserialized.doubleList3, is(pojo.doubleList3));
    }

    @Test
    public void testSerializeWithoutClassLevelFormatter() {
        NumberFormatPojoWithoutClassLevelFormatter pojo = new NumberFormatPojoWithoutClassLevelFormatter();
        pojo.setDoubleGetterFormatted(.1d);
        pojo.setDoubleSetterFormatted(.5d);
        pojo.setDoubleSetterAndPropertyFormatter(0.6d);

        String expectedJson = "{\"doubleGetterFormatted\":\"000.10000000\",\"doubleSetterAndPropertyFormatter\":\"000.600\",\"doubleSetterFormatted\":0.5}";

        assertEquals(expectedJson, jsonb.toJson(pojo));
    }

    @Test
    public void testDeserializeWithoutClassLevelFormatter() {
        String expectedJson = "{\"doubleGetterFormatted\":\"000.10000\",\"doubleSetterFormatted\":\",005\",\"doubleSetterAndPropertyFormatter\":\"000,600\"}";
        NumberFormatPojoWithoutClassLevelFormatter pojo = jsonb.fromJson(expectedJson, NumberFormatPojoWithoutClassLevelFormatter.class);

        assertEquals(Double.valueOf(.1d), pojo.getDoubleGetterFormatted());
        assertEquals(Double.valueOf(.005d), pojo.getDoubleSetterFormatted());
        assertEquals(Double.valueOf(.6d), pojo.getDoubleSetterAndPropertyFormatter());
    }
}
