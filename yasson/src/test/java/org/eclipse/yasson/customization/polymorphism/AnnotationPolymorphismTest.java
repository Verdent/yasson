package org.eclipse.yasson.customization.polymorphism;

import java.time.LocalDate;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbProperty;
import org.eclipse.yasson.Jsonbs;
import org.eclipse.yasson.PolymorphicType;
import org.eclipse.yasson.SubType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for verification of proper polymorphism handling based on annotation.
 */
public class AnnotationPolymorphismTest {

    public static final String ARRAY_EXPECTED = "[{\"@type\":\"dog\",\"isDog\":true},{\"@type\":\"cat\",\"isCat\":true},"
            + "{\"@type\":\"dog\",\"isDog\":true}]";

    @Test
    public void testBasicSerialization() {
        Dog dog = new Dog();
        assertThat(Jsonbs.defaultJsonb.toJson(dog), is("{\"@type\":\"dog\",\"isDog\":true}"));
        Cat cat = new Cat();
        assertThat(Jsonbs.defaultJsonb.toJson(cat), is("{\"@type\":\"cat\",\"isCat\":true}"));
    }

    @Test
    public void testBasicDeserialization() {
        Animal dog = Jsonbs.defaultJsonb.fromJson("{\"@type\":\"dog\",\"isDog\":false}", Animal.class);
        assertThat(dog, instanceOf(Dog.class));
        assertThat(((Dog) dog).isDog, is(false));
        Animal cat = Jsonbs.defaultJsonb.fromJson("{\"@type\":\"cat\",\"isCat\":false}", Animal.class);
        assertThat(cat, instanceOf(Cat.class));
        assertThat(((Cat) cat).isCat, is(false));
    }

    @Test
    public void testExactTypeDeserialization() {
        Dog dog = Jsonbs.defaultJsonb.fromJson("{\"isDog\":false}", Dog.class);
        assertThat(dog.isDog, is(false));
        dog = Jsonbs.defaultJsonb.fromJson("{\"@type\":\"dog\", \"isDog\":false}", Dog.class);
        assertThat(dog.isDog, is(false));
    }

    @Test
    public void testUnknownAliasDeserialization() {
        JsonbException exception = assertThrows(JsonbException.class,
                                                () -> Jsonbs.defaultJsonb.fromJson("{\"@type\":\"rat\",\"isDog\":false}",
                                                                                   Animal.class));
        assertThat(exception.getMessage(), startsWith("Unknown alias \"rat\" known aliases: ["));
    }

    @Test
    public void testUnknownAliasSerialization() {
        JsonbException exception = assertThrows(JsonbException.class, () -> Jsonbs.defaultJsonb.toJson(new Rat()));
        assertThat(exception.getMessage(),
                   is("Could not find proper alias for class: "
                              + "org.eclipse.yasson.customization.polymorphism.AnnotationPolymorphismTest$Rat"));
    }

    @Test
    public void testCreatorDeserialization() {
        SomeDateType creator = Jsonbs.defaultJsonb
                .fromJson("{\"@dateType\":\"constructor\",\"localDate\":\"26-02-2021\"}", SomeDateType.class);
        assertThat(creator, instanceOf(DateConstructor.class));
    }

    @Test
    public void testArraySerialization() {
        Animal[] animals = new Animal[] {new Dog(), new Cat(), new Dog()};
        assertThat(Jsonbs.defaultJsonb.toJson(animals), is(ARRAY_EXPECTED));
    }

    @Test
    public void testArrayDeserialization() {
        Animal[] deserialized = Jsonbs.defaultJsonb.fromJson(ARRAY_EXPECTED, Animal[].class);
        assertThat(deserialized.length, is(3));
        assertThat(deserialized[0], instanceOf(Dog.class));
        assertThat(deserialized[1], instanceOf(Cat.class));
        assertThat(deserialized[2], instanceOf(Dog.class));
    }

    @PolymorphicType(key = "@type")
    @SubType(alias = "dog", type = Dog.class)
    @SubType(alias = "cat", type = Cat.class)
    public interface Animal {

    }

    public static class Dog implements Animal {

        public boolean isDog = true;

    }

    public static class Cat implements Animal {

        public boolean isCat = true;

    }

    @SubType(alias = "rat", type = Rat.class)
    public static class Rat implements Animal {

        public boolean isRat = true;

    }

    @PolymorphicType(key = "@dateType")
    @SubType(alias = "constructor", type = DateConstructor.class)
    public interface SomeDateType {

    }

    public static final class DateConstructor implements SomeDateType {

        public LocalDate localDate;

        @JsonbCreator
        public DateConstructor(@JsonbProperty("localDate") @JsonbDateFormat(value = "dd-MM-yyyy", locale = "nl-NL") LocalDate localDate) {
            this.localDate = localDate;
        }

    }

}
