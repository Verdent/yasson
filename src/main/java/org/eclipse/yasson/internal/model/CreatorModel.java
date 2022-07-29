/*
 * Copyright (c) 2016, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.eclipse.yasson.internal.model;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.eclipse.yasson.customization.ParamCustomization;
import org.eclipse.yasson.internal.AnnotationIntrospector;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.JsonbDateFormatter;
import org.eclipse.yasson.internal.JsonbNumberFormatter;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.components.AdapterBinding;
import org.eclipse.yasson.internal.components.DeserializerBinding;
import org.eclipse.yasson.internal.model.customization.CreatorCustomization;

/**
 * Parameter for creator constructor / method model.
 */
public class CreatorModel {

    private final String name;

    private final Type type;

    private final CreatorCustomization creatorCustomization;

    /**
     * Creates a new instance.
     *  @param name      Parameter name
     * @param parameter constructor parameter
     * @param executable creator executable
     * @param context   jsonb context
     */
    public CreatorModel(String name, Parameter parameter, Executable executable, JsonbContext context) {
        this.name = name;
        this.type = parameter.getParameterizedType();

        AnnotationIntrospector annotationIntrospector = context.getAnnotationIntrospector();

        JsonbAnnotatedElement<Parameter> annotated = new JsonbAnnotatedElement<>(parameter);
        boolean required = annotationIntrospector.requiredParameters(executable, annotated);
        JsonbNumberFormatter constructorNumberFormatter = annotationIntrospector.getConstructorNumberFormatter(annotated, false);
        JsonbDateFormatter constructorDateFormatter = annotationIntrospector.getConstructorDateFormatter(annotated, false);
        DeserializerBinding<?> deserializerBinding = annotationIntrospector.getDeserializerBinding(parameter, false);
        AdapterBinding adapterBinding = annotationIntrospector.getAdapterBinding(parameter, false);
        final JsonbAnnotatedElement<Class<?>> clsElement = annotationIntrospector.collectAnnotations(parameter.getType());
        deserializerBinding = deserializerBinding == null
                ? annotationIntrospector.getDeserializerBinding(clsElement, false)
                : deserializerBinding;
        adapterBinding = adapterBinding == null
                ? annotationIntrospector.getAdapterBinding(clsElement, false)
                : adapterBinding;
        this.creatorCustomization = CreatorCustomization.builder()
                .adapterBinding(adapterBinding)
                .deserializerBinding(deserializerBinding)
                .serializerBinding(annotationIntrospector.getSerializerBinding(clsElement, false))
                .numberFormatter(constructorNumberFormatter)
                .dateFormatter(constructorDateFormatter)
                .required(required)
                .build();
    }

    /**
     * Creates a new instance.
     *  @param name      Parameter name
     * @param parameter constructor parameter
     * @param executable creator executable
     * @param context   jsonb context
     * @param userDefined user defined customization
     */
    public CreatorModel(String name,
                        Parameter parameter,
                        Executable executable,
                        JsonbContext context,
                        ParamCustomization userDefined) {
        this.name = name;
        this.type = parameter.getParameterizedType();

        AnnotationIntrospector annotationIntrospector = context.getAnnotationIntrospector();

        JsonbAnnotatedElement<Parameter> annotated = new JsonbAnnotatedElement<>(parameter);
        boolean required = context.getAnnotationIntrospector().requiredParameters(executable, annotated);
        JsonbNumberFormatter constructorNumberFormatter = userDefined.getNumberFormat()
                .map(annotationIntrospector::toJsonbNumberFormatter)
                .orElseGet(() -> annotationIntrospector.getConstructorNumberFormatter(annotated, userDefined.ignoreDateFormat()));
        JsonbDateFormatter constructorDateFormatter = userDefined.getDateFormat()
                .map(annotationIntrospector::toJsonbDateFormatter)
                .orElseGet(() -> annotationIntrospector.getConstructorDateFormatter(annotated, userDefined.ignoreNumberFormat()));
        DeserializerBinding<?> deserializerBinding = userDefined.getDeserializer()
                .map(deser -> context.getComponentMatcher().introspectDeserializerBinding(deser.getClass(), deser))
                .orElseGet(() -> annotationIntrospector.getDeserializerBinding(parameter, userDefined.ignoreDeserializer()));
        AdapterBinding adapterBinding = userDefined.getAdapter()
                .map(adapter -> annotationIntrospector.getAdapterBinding(adapter, ReflectionUtils.getRawType(type)))
                .orElseGet(() -> annotationIntrospector.getAdapterBinding(parameter, userDefined.ignoreAdapter()));

        final JsonbAnnotatedElement<Class<?>> clsElement = annotationIntrospector.collectAnnotations(parameter.getType());
        deserializerBinding = deserializerBinding == null
                ? annotationIntrospector.getDeserializerBinding(clsElement, false)
                : deserializerBinding;
        adapterBinding = adapterBinding == null
                ? annotationIntrospector.getAdapterBinding(clsElement, false)
                : adapterBinding;
        this.creatorCustomization = CreatorCustomization.builder()
                .adapterBinding(adapterBinding)
                .deserializerBinding(deserializerBinding)
                .serializerBinding(annotationIntrospector.getSerializerBinding(clsElement, false))
                .numberFormatter(constructorNumberFormatter)
                .dateFormatter(constructorDateFormatter)
                .required(required)
                .build();
    }

    /**
     * Gets parameter name.
     *
     * @return Parameter name.
     */
    public String getName() {
        return name;
    }

    public CreatorCustomization getCustomization() {
        return creatorCustomization;
    }

    /**
     * Gets parameter type.
     *
     * @return Parameter type.
     */
    public Type getType() {
        return type;
    }

}
