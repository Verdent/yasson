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

/**
 * Yasson Microprofile Config extension.
 */
module org.eclipse.yasson.microprofile.config {

    requires jakarta.json.bind;
    requires microprofile.config.api;
    requires org.eclipse.yasson;

    exports org.eclipse.yasson.mpconfig;

    provides org.eclipse.yasson.spi.JsonbConfigDataProvider with org.eclipse.yasson.mpconfig.MpConfigProvider;
}
