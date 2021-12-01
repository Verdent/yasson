package org.eclipse.yasson.customization.polymorphism;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.PolymorphicType;
import org.eclipse.yasson.SubType;
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
public class MixedPolymorphismTest {

    private static final String ARRAY_EXPECTED = "[{\"@type\":\"dog\",\"isDog\":true},{\"@type\":\"cat\",\"isCat\":true},"
            + "{\"@type\":\"dog\",\"isDog\":true}]";
    private static final String CLASS_USED = "{\"@type\":\"" + Rat.class.getName() + "\",\"isRat\":true}";

    private static Jsonb jsonb;
    private static Jsonb jsonbObjectWrapping;
    private static Jsonb jsonbArrayWrapping;
    private static Jsonb jsonbClassName;

    @BeforeAll
    public static void setUp() {
        YassonConfig yassonConfig = new YassonConfig()
                .withPolymorphism(PolymorphismSupport.builder()
                                          .polymorphism(Polymorphism.builder(Animal.class)
                                                                .alias(Rat.class, "rat")
                                                                .build())
                                          .build());
        jsonb = JsonbBuilder.create(yassonConfig);
        yassonConfig = new YassonConfig()
                .withPolymorphism(PolymorphismSupport.builder()
                                          .polymorphism(Polymorphism.builder(Animal.class)
                                                                .format(PolymorphicType.Format.WRAPPING_OBJECT)
                                                                .alias(Rat.class, "rat")
                                                                .whitelist("some.package")
                                                                .build())
                                          .whitelist("some.package")
                                          .build());
        jsonbObjectWrapping = JsonbBuilder.create(yassonConfig);
//        yassonConfig = new YassonConfig()
//                .withPolymorphism(PolymorphismSupport.builder()
//                                          .polymorphism(Polymorphism.builder(Animal.class)
//                                                                .keyName("@type")
//                                                                .format(PolymorphicType.Format.WRAPPING_ARRAY)
//                                                                .alias(Rat.class, "rat")
//                                                                .build())
//                                          .build());
        jsonbArrayWrapping = JsonbBuilder.create(yassonConfig);
        yassonConfig = new YassonConfig()
                .withPolymorphism(PolymorphismSupport.builder()
                                          .polymorphism(Polymorphism.builder(Animal.class)
                                                                .useClassNames(true)
                                                                .build())
                                          .build());
        jsonbClassName = JsonbBuilder.create(yassonConfig);
    }

    @Test
    public void testBasicSerialization() {
        Dog dog = new Dog();
        Cat cat = new Cat();
        Rat rat = new Rat();
        assertThat(jsonb.toJson(dog), is("{\"@type\":\"dog\",\"isDog\":true}"));
        assertThat(jsonbObjectWrapping.toJson(dog), is("{\"dog\":{\"isDog\":true}}"));
        assertThat(jsonbArrayWrapping.toJson(dog), is("[\"dog\",{\"isDog\":true}]"));

        assertThat(jsonb.toJson(cat), is("{\"@type\":\"cat\",\"isCat\":true}"));
        assertThat(jsonbObjectWrapping.toJson(cat), is("{\"cat\":{\"isCat\":true}}"));
        assertThat(jsonbArrayWrapping.toJson(cat), is("[\"cat\",{\"isCat\":true}]"));

        assertThat(jsonb.toJson(rat), is("{\"@type\":\"rat\",\"isRat\":true}"));
        assertThat(jsonbObjectWrapping.toJson(rat), is("{\"rat\":{\"isRat\":true}}"));
        assertThat(jsonbArrayWrapping.toJson(rat), is("[\"rat\",{\"isRat\":true}]"));

        assertThat(jsonbClassName.toJson(dog), is("{\"@type\":\"dog\",\"isDog\":true}"));
        assertThat(jsonbClassName.toJson(cat), is("{\"@type\":\"cat\",\"isCat\":true}"));
        assertThat(jsonbClassName.toJson(rat), is(CLASS_USED));
    }

    @Test
    public void testDeserializationClass() {
        Animal rat = jsonbClassName.fromJson(CLASS_USED, Animal.class);
        assertThat(rat, instanceOf(Rat.class));

        JsonbException exception = assertThrows(JsonbException.class, () -> jsonb.fromJson(CLASS_USED, Animal.class));
        assertThat(exception.getMessage(),
                   startsWith("Unknown alias \"org.eclipse.yasson.customization.polymorphism.MixedPolymorphismTest$Rat\" known "
                                      + "aliases: ["));
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
    public void testArrayDeserialization() {
        Animal[] deserialized = jsonb.fromJson(ARRAY_EXPECTED, Animal[].class);
        assertThat(deserialized.length, is(3));
        assertThat(deserialized[0], instanceOf(Dog.class));
        assertThat(deserialized[1], instanceOf(Cat.class));
        assertThat(deserialized[2], instanceOf(Dog.class));
    }

    @PolymorphicType(key = "@type", format = PolymorphicType.Format.PROPERTY)
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

    public static class Rat implements Animal {

        public boolean isRat = true;

    }

}
