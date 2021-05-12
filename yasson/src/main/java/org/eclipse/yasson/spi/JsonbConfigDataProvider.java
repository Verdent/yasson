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

package org.eclipse.yasson.spi;

import jakarta.json.bind.JsonbConfig;

/**
 * Extension which enables to configure {@link JsonbConfig}.
 */
public interface JsonbConfigDataProvider {

    /**
     * Register JSON-B config properties to the {@link JsonbConfig}.
     * <br>
     * Called when {@link jakarta.json.bind.Jsonb} instance is being created.
     *
     * @param config JSON-B config
     */
    void updateConfig(JsonbConfig config);

}
