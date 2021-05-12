package org.eclipse.yasson.mpconfig;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TODO javadoc
 */
public class ConfigTest {

    private static final MpConfigProvider PROVIDER_INSTANCE = new MpConfigProvider();
    private JsonbConfig jsonbConfig;

    @BeforeEach
    public void refreshJsonbConfig() {
        jsonbConfig = new JsonbConfig();
    }

    @Test
    public void configPrefixWasNotString() {
        JsonbConfig jsonbConfig = new JsonbConfig().setProperty(MpConfigProvider.YASSON_CONFIG_PREFIX, 123);
        JsonbException jsonbException = assertThrows(JsonbException.class, () -> PROVIDER_INSTANCE.updateConfig(jsonbConfig));
        assertThat(jsonbException.getMessage(),
                   is("Property " + MpConfigProvider.YASSON_CONFIG_PREFIX + " needs to be a String"));
    }

    @Test
    public void configValueConversion() {
        PROVIDER_INSTANCE.updateConfig(jsonbConfig);
//        assertThat(jsonbConfig.getProperty(JsonbConfig.NULL_VALUES), hasItem(true));
    }

    @Test
    public void simpleTest() {
        JsonbConfig jsonbConfig = new JsonbConfig();
        Jsonb jsonb = JsonbBuilder.create();
        assertThat(jsonb.toJson(new TestClass()), is("{\"nullValue\":null}"));
    }


    @Test
    public void simpleTest2() {
        Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().setProperty(MpConfigProvider.YASSON_CONFIG_PREFIX, "my.cool.yasson"));
//        Jsonb jsonb = JsonbBuilder.create();
        assertThat(jsonb.toJson(new TestClass()), is("{\"nullValue\":null}"));
    }

    public static class TestClass {

        public String nullValue;

    }

}
