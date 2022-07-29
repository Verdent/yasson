package org.eclipse.yasson.customization;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.eclipse.yasson.YassonConfig;
import org.junit.jupiter.api.Test;

/**
 * TODO javadoc
 */
public class ExistingClassCustomizationTest {

//    @JsonbNillable
    public static class TestClass {

        public String fieldOne;
        public String fieldTwo;

        public static TestClass create(String fieldOne, String fieldTwo) {
            return new TestClass();
        }

    }

    @Test
    public void basicTest() {
        TypeCustomization typeCustomization = TypeCustomization.builder(TestClass.class)
                .nillable(true)
                .creator("create", builder -> builder.addParam(String.class, "fieldOne")
                        .addParam(String.class, "fieldTwo"))
                .property("fieldOne", builder -> builder.nillable(false))
                .build();

        Jsonb jsonb = JsonbBuilder.create(new YassonConfig().withTypeCustomization(typeCustomization));

        System.out.println(jsonb.toJson(new TestClass()));
    }

}
