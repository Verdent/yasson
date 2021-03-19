package org.eclipse.yasson.customization.polymorphism;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.YassonConfig;
import org.eclipse.yasson.config.Polymorphism;
import org.eclipse.yasson.config.PolymorphismSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for verification of proper polymorphism handling based on annotation.
 */
public class ConfigPolymorphismTest {

    public static final String ARRAY_EXPECTED = "[{\"@type\":\"dog\",\"isDog\":true},{\"@type\":\"cat\",\"isCat\":true},"
            + "{\"@type\":\"dog\",\"isDog\":true}]";

    private static Jsonb jsonb;

    @BeforeAll
    public static void setUp() {
        YassonConfig yassonConfig = new YassonConfig()
                .withPolymorphism(PolymorphismSupport.builder()
                                          .polymorphism(Polymorphism.builder(Animal.class)
                                                                .alias(Dog.class, "dog")
                                                                .alias(Cat.class, "cat")
                                                                .build())
                                          .build());
        jsonb = JsonbBuilder.create(yassonConfig);
    }

    @Test
    public void testBasicSerialization() {
        Dog dog = new Dog();
        assertThat(jsonb.toJson(dog), is("{\"@type\":\"dog\",\"isDog\":true}"));
        Cat cat = new Cat();
        assertThat(jsonb.toJson(cat), is("{\"@type\":\"cat\",\"isCat\":true}"));
    }

    @Test
    public void testBasicDeserialization() {
        Animal dog = jsonb.fromJson("{\"@type\":\"dog\",\"isDog\":false}", Animal.class);
        assertThat(dog, instanceOf(Dog.class));
        assertThat(((Dog) dog).isDog, is(false));
        Animal cat = jsonb.fromJson("{\"@type\":\"cat\",\"isCat\":false}", Animal.class);
        assertThat(cat, instanceOf(Cat.class));
        assertThat(((Cat) cat).isCat, is(false));
    }

    @Test
    public void testExactTypeDeserialization() {
        Dog dog = jsonb.fromJson("{\"isDog\":false}", Dog.class);
        assertThat(dog.isDog, is(false));
    }

    @Test
    public void testUnknownAliasDeserialization() {
        JsonbException exception = assertThrows(JsonbException.class, () -> {
            jsonb.fromJson("{\"@type\":\"rat\",\"isDog\":false}", Animal.class);
        });
        assertThat(exception.getMessage(), startsWith("Unknown alias \"rat\" known aliases: ["));
    }

    @Test
    public void testUnknownAliasSerialization() {
        JsonbException exception = assertThrows(JsonbException.class, () -> jsonb.toJson(new Rat()));
        assertThat(exception.getMessage(),
                   is("Could not find proper alias for class: "
                              + "org.eclipse.yasson.customization.polymorphism.ConfigPolymorphismTest$Rat"));
    }

    @Test
    public void testArrayDeserialization() {
        Animal[] deserialized = jsonb.fromJson(ARRAY_EXPECTED, Animal[].class);
        assertThat(deserialized.length, is(3));
        assertThat(deserialized[0], instanceOf(Dog.class));
        assertThat(deserialized[1], instanceOf(Cat.class));
        assertThat(deserialized[2], instanceOf(Dog.class));
    }

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

}
