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

package org.eclipse.yasson.internal.processor.serializer.types;

import java.util.TimeZone;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.Marshaller;
import org.eclipse.yasson.internal.model.customization.Customization;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.serializer.AbstractValueTypeSerializer;

/**
 * Serializer for {@link TimeZone} type.
 */
class TimeZoneSerializer extends TypeSerializer<TimeZone> {

    TimeZoneSerializer(TypeSerializerBuilder serializerBuilder) {
        super(serializerBuilder);
    }

    @Override
    void serializeValue(TimeZone value, JsonGenerator generator, SerializationContextImpl context) {
        generator.write(value.getID());
    }
}
