package org.eclipse.yasson.customization.polymorphism;

import java.time.LocalDate;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbPolymorphicType;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbSubtype;

import org.eclipse.yasson.Jsonbs;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for verification of proper polymorphism handling based on annotation.
 */
public class AnnotationPolymorphismObjectTest {

    public static final String ARRAY_EXPECTED = "[{\"dog\":{\"isDog\":true}},{\"cat\":{\"isCat\":true}},"
            + "{\"dog\":{\"isDog\":true}}]";

    @Test
    public void testBasicSerialization() {
        Dog dog = new Dog();
        assertThat(Jsonbs.defaultJsonb.toJson(dog), is("{\"dog\":{\"isDog\":true}}"));
        Cat cat = new Cat();
        assertThat(Jsonbs.defaultJsonb.toJson(cat), is("{\"cat\":{\"isCat\":true}}"));
    }

    @Test
    public void testBasicDeserialization() {
        Animal dog = Jsonbs.defaultJsonb.fromJson("{\"dog\":{\"isDog\":false}}", Animal.class);
        assertThat(dog, instanceOf(Dog.class));
        assertThat(((Dog) dog).isDog, is(false));
        Animal cat = Jsonbs.defaultJsonb.fromJson("{\"cat\":{\"isCat\":false}}", Animal.class);
        assertThat(cat, instanceOf(Cat.class));
        assertThat(((Cat) cat).isCat, is(false));
    }

    @Test
    public void testExactTypeDeserialization() {
        Dog dog = Jsonbs.defaultJsonb.fromJson("{\"isDog\":false}", Dog.class);
        assertThat(dog.isDog, is(false));
        Animal dog2 = Jsonbs.defaultJsonb.fromJson("{\"dog\":{\"isDog\":false}}", Dog.class);
    }

    @Test
    public void testUnknownAliasDeserialization() {
        JsonbException exception = assertThrows(JsonbException.class,
                                                () -> Jsonbs.defaultJsonb.fromJson("{\"rat\":{\"isRat\":true}}", Animal.class));
        assertThat(exception.getMessage(), startsWith("Unknown alias \"rat\" known aliases: ["));
    }

    @Test
    public void testUnknownAliasSerialization() {
        JsonbException exception = assertThrows(JsonbException.class, () -> Jsonbs.defaultJsonb.toJson(new Rat()));
        assertThat(exception.getMessage(),
                   is("Could not find proper alias for class: "
                              + "org.eclipse.yasson.customization.polymorphism.AnnotationPolymorphismObjectTest$Rat"));
    }

    @Test
    public void testCreatorDeserialization() {
        SomeDateType creator = Jsonbs.defaultJsonb
                .fromJson("{\"constructor\":{\"localDate\":\"26-02-2021\"}}", SomeDateType.class);
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

    @Test
    public void testSerializationClassNamesWithCorrectAllowedPackages() {
        String expected = "{\"org.eclipse.yasson.customization.polymorphism."
                + "AnnotationPolymorphismObjectTest$ChildClassNamesWithCorrectAllowed\":{\"parent\":1,\"child\":2}}";
        assertThat(Jsonbs.defaultJsonb.toJson(new ChildClassNamesWithCorrectAllowed()), is(expected));
    }

    @Test
    public void testDeserializationClassNamesWithCorrectAllowedPackages() {
        String json = "{\"org.eclipse.yasson.customization.polymorphism."
                + "AnnotationPolymorphismObjectTest$ChildClassNamesWithCorrectAllowed\":{\"parent\":3,\"child\":4}}";
        ParentClassNamesWithCorrectAllowed deserialized = Jsonbs.defaultJsonb.fromJson(json, ParentClassNamesWithCorrectAllowed.class);
        assertThat(deserialized, instanceOf(ChildClassNamesWithCorrectAllowed.class));
        assertThat(deserialized.parent, is(3));
        assertThat(((ChildClassNamesWithCorrectAllowed)deserialized).child, is(4));
    }

    @Test
    public void testSerializationClassNamesWithIncorrectAllowedPackages() {
        String expected = "{\"org.eclipse.yasson.customization.polymorphism."
                + "AnnotationPolymorphismObjectTest$ChildClassNamesWithIncorrectAllowed\":{\"parent\":1,\"child\":2}}";
        assertThat(Jsonbs.defaultJsonb.toJson(new ChildClassNamesWithIncorrectAllowed()), is(expected));
    }

    @Test
    public void testDeserializationClassNamesWithIncorrectAllowedPackages() {
        String json = "{\"org.eclipse.yasson.customization.polymorphism."
                + "AnnotationPolymorphismObjectTest$ChildClassNamesWithIncorrectAllowed\":{\"parent\":1,\"child\":2}}";
        assertThrows(JsonbException.class, () -> Jsonbs.defaultJsonb.fromJson(json, ParentClassNamesWithIncorrectAllowed.class));
    }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT,
                          value = {
                                  @JsonbSubtype(alias = "dog", type = Dog.class),
                                  @JsonbSubtype(alias = "cat", type = Cat.class)
                          })
    public interface Animal {

    }

    public static class Dog implements Animal {

        public boolean isDog = true;

    }

    public static class Cat implements Animal {

        public boolean isCat = true;

    }

    public static class Rat implements Animal {

        public boolean isRat = true;

    }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT, value = {
            @JsonbSubtype(alias = "constructor", type = DateConstructor.class)
    })
    public interface SomeDateType {

    }

    public static final class DateConstructor implements SomeDateType {

        public LocalDate localDate;

        @JsonbCreator
        public DateConstructor(@JsonbProperty("localDate") @JsonbDateFormat(value = "dd-MM-yyyy", locale = "nl-NL") LocalDate localDate) {
            this.localDate = localDate;
        }

    }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT,
                          classNames = true, allowedPackages = {"org.eclipse.yasson.customization.polymorphism"})
    public static class ParentClassNamesWithCorrectAllowed {
        public int parent = 1;
    }

    public static class ChildClassNamesWithCorrectAllowed extends ParentClassNamesWithCorrectAllowed {
        public int child = 2;
    }

    @JsonbPolymorphicType(format = JsonbPolymorphicType.Format.WRAPPING_OBJECT,
                          classNames = true, allowedPackages = {"org.eclipse.incorrect"})
    public static class ParentClassNamesWithIncorrectAllowed {
        public int parent = 1;
    }

    public static class ChildClassNamesWithIncorrectAllowed extends ParentClassNamesWithIncorrectAllowed {
        public int child = 2;
    }

}
