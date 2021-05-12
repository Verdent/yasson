/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.eclipse.yasson.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.ServiceLoader;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.spi.JsonProvider;
import org.eclipse.yasson.spi.JsonbAdapterProvider;
import org.eclipse.yasson.spi.JsonbConfigDataProvider;
import org.eclipse.yasson.spi.JsonbDeserializerProvider;
import org.eclipse.yasson.spi.JsonbSerializerProvider;

/**
 * JsonbBuilder implementation.
 */
public class JsonBindingBuilder implements JsonbBuilder {
    private JsonbConfig config = new JsonbConfig();
    private JsonProvider provider = null;

    @Override
    public JsonbBuilder withConfig(JsonbConfig config) {
        this.config = config;
        return this;
    }

    @Override
    public JsonbBuilder withProvider(JsonProvider jsonpProvider) {
        this.provider = jsonpProvider;
        return this;
    }

    /**
     * Gets configuration.
     *
     * @return configuration.
     */
    public JsonbConfig getConfig() {
        return config;
    }

    /**
     * Gets provider.
     *
     * @return Provider.
     */
    public Optional<JsonProvider> getProvider() {
        return Optional.ofNullable(provider);
    }

    @Override
    public Jsonb build() {
        initJsonbSerializersFromSpi();
        initJsonbDeserializersFromSpi();
        initJsonbAdaptersFromSpi();
        setupConfigValuesFromSpi();
        return new JsonBinding(this);
    }

    private void initJsonbSerializersFromSpi() {
        ServiceLoader<JsonbSerializerProvider> loader = AccessController
                .doPrivileged((PrivilegedAction<ServiceLoader<JsonbSerializerProvider>>) () -> ServiceLoader
                        .load(JsonbSerializerProvider.class));
        loader.forEach(provider -> config.withSerializers(provider.createSerializers().toArray(new JsonbSerializer[0])));
    }

    private void initJsonbDeserializersFromSpi() {
        ServiceLoader<JsonbDeserializerProvider> loader = AccessController
                .doPrivileged((PrivilegedAction<ServiceLoader<JsonbDeserializerProvider>>) () -> ServiceLoader
                        .load(JsonbDeserializerProvider.class));
        loader.forEach(provider -> config.withDeserializers(provider.createDeserializers().toArray(new JsonbDeserializer[0])));
    }

    private void initJsonbAdaptersFromSpi() {
        ServiceLoader<JsonbAdapterProvider> loader = AccessController
                .doPrivileged((PrivilegedAction<ServiceLoader<JsonbAdapterProvider>>) () -> ServiceLoader
                        .load(JsonbAdapterProvider.class));
        loader.forEach(provider -> config.withAdapters(provider.createAdapters().toArray(new JsonbAdapter[0])));
    }

    private void setupConfigValuesFromSpi() {
        ServiceLoader<JsonbConfigDataProvider> loader = AccessController
                .doPrivileged((PrivilegedAction<ServiceLoader<JsonbConfigDataProvider>>) () -> ServiceLoader
                        .load(JsonbConfigDataProvider.class));
        loader.forEach(provider -> provider.updateConfig(config));
    }
}
