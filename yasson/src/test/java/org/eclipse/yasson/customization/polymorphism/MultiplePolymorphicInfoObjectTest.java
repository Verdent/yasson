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

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbPolymorphicType;
import jakarta.json.bind.annotation.JsonbSubtype;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * TODO javadoc
 */
public class MultiplePolymorphicInfoObjectTest {

    private static final Jsonb JSONB = JsonbBuilder.create();

    @Test
    public void testMultiplePolymorphicInfoObjectSerialization() {
        String expected = "{\"animal\":{\"dog\":{\"labrador\":{\"isLabrador\":true}}}}";
        Labrador labrador = new Labrador();
        assertThat(JSONB.toJson(labrador), is(expected));
    }

    @Test
    public void testMultiplePolymorphicInfoObjectDeserialization() {
        String json = "{\"animal\":{\"dog\":{\"labrador\":{\"isLabrador\":true}}}}";
        assertThat(JSONB.fromJson(json, Labrador.class), instanceOf(Labrador.class));
    }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT, value = {
            @JsonbSubtype(alias = "animal", type = Animal.class)
    })
    public interface Something { }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT, value = {
            @JsonbSubtype(alias = "dog", type = Dog.class)
    })
    public interface Animal extends Something { }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT, value = {
            @JsonbSubtype(alias = "labrador", type = Labrador.class)
    })
    public interface Dog extends Animal { }

    public static class Labrador implements Dog {

        public boolean isLabrador = true;

    }

    @Test
    public void testDeserializeMultiplePolyTypesInSingleChain() {
        String json = "{\"vehicle\":{\"car\":{"
                + "\"machineProperty\":\"machineProperty\","
                + "\"vehicleProperty\":\"vehicleProperty\","
                + "\"carProperty\":\"carProperty\""
                + "}}}";
        Machine machine = JSONB.fromJson(json, Machine.class);
        assertThat(machine, instanceOf(Car.class));
        Vehicle vehicle = JSONB.fromJson(json, Vehicle.class);
        assertThat(vehicle, instanceOf(Car.class));
    }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT, value = {
            @JsonbSubtype(alias = "vehicle", type = Vehicle.class)
    })
    public static class Machine {
        public String machineProperty = "machineProperty";
    }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT, value = {
            @JsonbSubtype(alias = "car", type = Car.class)
    })
    public static class Vehicle extends Machine {
        public String vehicleProperty = "vehicleProperty";
    }

    public static class Car extends Vehicle {
        public String carProperty = "carProperty";
    }

}
