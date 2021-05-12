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

package org.eclipse.yasson.mpconfig;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.yasson.YassonConfig;
import org.eclipse.yasson.spi.JsonbConfigDataProvider;

/**
 * Yasson Microprofile Config properties provider.
 * <br><p>
 * All of the properties are automatically converted to their supported types. In terms of
 * {@link JsonbSerializer}, {@link JsonbDeserializer} and {@link JsonbAdapter} public default constructor
 * is required.
 * </p><br>
 * Each of the config properties needs to correspond to the name used in {@link JsonbConfig} and {@link YassonConfig} constants
 * such as:
 * <pre><code>
 * jsonb.null-values=true
 * jsonb.adapters=some.TestAdapter,some.SecondTestAdapter
 * </code></pre>
 * It is also possible to set prefix where the configuration can be found using {@link #YASSON_CONFIG_PREFIX}.
 * <pre><code>
 * <b>Config prefix set to JsonbConfig:</b>
 * <code>new JsonbConfig().setProperty(MpConfigProvider.YASSON_CONFIG_PREFIX, "some.other.path");</code><br>
 * <b>MP Config:</b>
 * some.other.path.jsonb.null-values=true
 * some.other.path.jsonb.adapters=some.TestAdapter,some.SecondTestAdapter
 * </code></pre>
 * <b>Not supported config properties:</b>
 * <ul>
 *     <li>{@link YassonConfig#POLYMORPHISM_SUPPORT}</li>
 *     <li>{@link YassonConfig#USER_TYPE_MAPPING}</li>
 * </ul>
 */
public class MpConfigProvider implements JsonbConfigDataProvider {

    private static final Map<String, BiFunction<String, Config, Optional<?>>> CONFIG_VALUES;

    /**
     * Yasson property used to locate Json-b/Yasson configuration within MP config.
     */
    public static final String YASSON_CONFIG_PREFIX = "yasson.config-prefix";

    static {
        Map<String, BiFunction<String, Config, Optional<?>>> tmp = new HashMap<>();
        tmp.put(JsonbConfig.NULL_VALUES, MpConfigProvider::getBoolean);
        tmp.put(JsonbConfig.STRICT_IJSON, MpConfigProvider::getBoolean);
        tmp.put(JsonbConfig.DATE_FORMAT, MpConfigProvider::getString);
        tmp.put(JsonbConfig.BINARY_DATA_STRATEGY, MpConfigProvider::getString);
        tmp.put(JsonbConfig.PROPERTY_NAMING_STRATEGY, MpConfigProvider::getString);
        tmp.put(JsonbConfig.PROPERTY_ORDER_STRATEGY, MpConfigProvider::getString);
        tmp.put(JsonbConfig.ENCODING, MpConfigProvider::getString);
        tmp.put(JsonbConfig.PROPERTY_VISIBILITY_STRATEGY,
                (key, config) -> getInstance(key, config, PropertyVisibilityStrategy.class));
        tmp.put(JsonbConfig.ADAPTERS, (key, config) -> getInstances(key, config, JsonbAdapter.class));
        tmp.put(JsonbConfig.DESERIALIZERS, (key, config) -> getInstances(key, config, JsonbDeserializer.class));
        tmp.put(JsonbConfig.SERIALIZERS, (key, config) -> getInstances(key, config, JsonbSerializer.class));

        tmp.put(YassonConfig.FAIL_ON_UNKNOWN_PROPERTIES, MpConfigProvider::getBoolean);
        tmp.put(YassonConfig.ZERO_TIME_PARSE_DEFAULTING, MpConfigProvider::getBoolean);
        tmp.put(YassonConfig.NULL_ROOT_SERIALIZER, (key, config) -> getInstance(key, config, JsonbSerializer.class));
        tmp.put(YassonConfig.EAGER_PARSE_CLASSES, MpConfigProvider::getClasses);
        CONFIG_VALUES = Map.copyOf(tmp);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T[]> getInstances(String key, Config config, Class<T> clazz) {
        return getClasses(key, config)
                .map(classes -> Arrays.stream(classes)
                        .peek(c -> isAssignableFrom(c, clazz))
                        .map(MpConfigProvider::createInstance)
                        .map(clazz::cast)
                        .collect(Collectors.toSet())
                        .toArray((T[]) Array.newInstance(clazz, 0)));
    }

    private static <T> Optional<T> getInstance(String key, Config config, Class<T> clazz) {
        return getClass(key, config)
                .stream()
                .peek(c -> isAssignableFrom(c, clazz))
                .map(MpConfigProvider::createInstance)
                .map(clazz::cast)
                .findFirst();
    }

    private static Optional<Class<?>[]> getClasses(String key, Config config) {
        return config.getOptionalValues(key, String.class)
                .map(strings -> strings.stream()
                        .map(MpConfigProvider::parseClass)
                        .toArray(Class[]::new));
    }

    private static Optional<Class<?>> getClass(String key, Config config) {
        return config.getOptionalValue(key, String.class)
                .map(MpConfigProvider::parseClass);
    }

    private static Class<?> parseClass(String potentialClass) {
        try {
            //java.* , javax.* a com.sun.*
            return Class.forName(potentialClass);
        } catch (ClassNotFoundException e) {
            throw new JsonbException("Unknown class: " + potentialClass, e);
        }
    }

    private static void isAssignableFrom(Class<?> child, Class<?> parent) {
        if (!parent.isAssignableFrom(child)) {
            throw new JsonbException("Class " + child.getName() + " needs to implement " + parent.getName());
        }
    }

    private static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new JsonbException("Class " + clazz.getName() + " needs to have a public default constructor", e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JsonbException("There has been an error while creating an instance", e);
        }
    }

    private static Optional<Boolean> getBoolean(String key, Config config) {
        return config.getOptionalValue(key, Boolean.class);
    }

    private static Optional<String> getString(String key, Config config) {
        return config.getOptionalValue(key, String.class);
    }

    private static String castToString(Object it) {
        if (!(it instanceof String)) {
            throw new JsonbException("Property " + YASSON_CONFIG_PREFIX + " needs to be a String");
        }
        return (String) it;
    }

    @Override
    public void updateConfig(JsonbConfig config) {
        String prefix = config.getProperty(YASSON_CONFIG_PREFIX)
                .map(MpConfigProvider::castToString)
                .orElse("");
        extractProperties(prefix, config);
    }

    private void extractProperties(String prefix, JsonbConfig jsonbConfig) {
        Config config = ConfigProvider.getConfig();
        for (Map.Entry<String, BiFunction<String, Config, Optional<?>>> entry : CONFIG_VALUES.entrySet()) {
            String key = prefix.isBlank() ? entry.getKey() : prefix + "." + entry.getKey();
            entry.getValue().apply(key, config).ifPresent(value -> jsonbConfig.setProperty(entry.getKey(), value));
        }
    }

}
