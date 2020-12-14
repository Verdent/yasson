package org.eclipse.yasson.defaultmapping.collections;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.yasson.Jsonbs;
import org.eclipse.yasson.TestTypeToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests to verify proper map key serialization and deserialization.
 */
public class MapKeyTypesTest {

    @Test
    public void uuidMapKey() {
        String expected = "{\"ccf3e2d3-3589-4c91-9ba9-1250ef515327\":{\"firstName\":\"FirstName1\",\"sureName\":\"SureName1\"},"
                + "\"0bb54cee-d538-428b-9719-5cc38c988522\":{\"firstName\":\"FirstName2\",\"sureName\":\"SureName2\"}}";
        Map<UUID, Person> map = new HashMap<>();
        Person person = new Person();
        person.firstName = "FirstName1";
        person.sureName = "SureName1";
        Person person2 = new Person();
        person2.firstName = "FirstName2";
        person2.sureName = "SureName2";
        map.put(UUID.fromString("ccf3e2d3-3589-4c91-9ba9-1250ef515327"), person);
        map.put(UUID.fromString("0bb54cee-d538-428b-9719-5cc38c988522"), person2);
        assertEquals(expected, Jsonbs.defaultJsonb.toJson(map));
        assertEquals(map, Jsonbs.defaultJsonb.fromJson(expected, new TestTypeToken<Map<UUID, Person>>() { }.getType()));
    }

    @Test
    public void zonedDateTimeMapKey() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2020, 9, 14,
                                                       9, 33, 12, 0,
                                                       ZoneId.systemDefault());
        ZonedDateTime zonedDateTime2 = ZonedDateTime.of(2019, 8, 13,
                                                        8, 32, 11, 1234,
                                                        ZoneId.systemDefault());
        String expected = "{\"2020-09-14T09:33:12+02:00[Europe/Prague]\":{\"firstName\":\"FirstName1\","
                + "\"sureName\":\"SureName1\"},"
                + "\"2019-08-13T08:32:11.000001234+02:00[Europe/Prague]\":{\"firstName\":\"FirstName2\","
                + "\"sureName\":\"SureName2\"}}";
        Map<ZonedDateTime, Person> map = new HashMap<>();
        Person person = new Person();
        person.firstName = "FirstName1";
        person.sureName = "SureName1";
        Person person2 = new Person();
        person2.firstName = "FirstName2";
        person2.sureName = "SureName2";
        map.put(zonedDateTime, person);
        map.put(zonedDateTime2, person2);
        assertEquals(expected, Jsonbs.defaultJsonb.toJson(map));
        assertEquals(map, Jsonbs.defaultJsonb.fromJson(expected, new TestTypeToken<Map<ZonedDateTime, Person>>() { }.getType()));
    }

    public static final class Person {

        public String firstName;
        public String sureName;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Person person = (Person) o;
            return Objects.equals(firstName, person.firstName) &&
                    Objects.equals(sureName, person.sureName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstName, sureName);
        }
    }

}
