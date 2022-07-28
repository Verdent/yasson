/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.eclipse.yasson.customization;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import jakarta.json.bind.config.PropertyVisibilityStrategy;

/**
 * Builder of the specific type customization.
 */
public class TypeCustomizationBuilder extends SerializationCustomizationBuilder<TypeCustomizationBuilder, TypeCustomization> {

    private final Class<?> typeClass;
    private final Map<String, PropertyCustomization> propertyCustomizations = new HashMap<>();

    private String[] propertyOrder;
    private PropertyVisibilityStrategy visibilityStrategy;
    private boolean ignorePropertyOrder;
    private boolean ignoreVisibilityStrategy;
    private CreatorCustomization creatorCustomization;
    private TypeInfoCustomization typeInfoCustomization;
    private boolean ignoreTypeInfo;

    public TypeCustomizationBuilder(Class<?> typeClass) {
        this.typeClass = typeClass;
    }

    /**
     * Order of the properties to be used.
     *
     * Order in which properties are serialized. Names must correspond to original
     * names defined in the Java class before any customization applied.
     *
     * @param propertyOrder propertyOrder
     * @return updated builder instance
     */
    TypeCustomizationBuilder propertyOrder(String[] propertyOrder) {
        this.propertyOrder = propertyOrder;
        return this;
    }

    /**
     * Ignore set property order.
     *
     * @return updated builder instance
     */
    TypeCustomizationBuilder ignorePropertyOrder() {
        this.ignorePropertyOrder = true;
        return this;
    }

    /**
     * Set {@link PropertyVisibilityStrategy} which should be used.
     *
     * @param strategy property visibility strategy
     * @return updated builder instance
     */
    TypeCustomizationBuilder visibilityStrategy(PropertyVisibilityStrategy strategy) {
        this.visibilityStrategy = strategy;
        return this;
    }

    /**
     * Ignore visibility strategy.
     *
     * @return updated builder instance
     */
    public TypeCustomizationBuilder ignoreVisibilityStrategy() {
        this.ignoreVisibilityStrategy = true;
        return this;
    }

    /**
     * Add new {@link PropertyCustomization} of the property.
     *
     * @param propertyCustomization property customization
     * @return updated builder instance
     */
    public TypeCustomizationBuilder property(PropertyCustomization propertyCustomization) {
        propertyCustomizations.put(propertyCustomization.getPropertyName(), propertyCustomization);
        return this;
    }

    /**
     * Add new {@link PropertyCustomization} of the property.
     * <br>
     * Shortcut method to the {@link #property(PropertyCustomization)}. It is not required to create {@link PropertyCustomizationBuilder}
     * since this method will create it based on the provided property name and expose it over the propertyBuilder parameter.
     * <br>
     * Example usage:
     * <pre>{@code
     * typeBuilder.property("exampleProperty", propertyBuilder -> propertyBuilder.nillable(true));
     * }</pre>
     * @param propertyName name of the class property
     * @param propertyBuilder builder used to customize property
     * @return updated builder instance
     */
    public TypeCustomizationBuilder property(String propertyName, Consumer<PropertyCustomizationBuilder> propertyBuilder) {
        PropertyCustomizationBuilder builder = PropertyCustomization.builder(Objects.requireNonNull(propertyName));
        propertyBuilder.accept(builder);
        return property(builder.build());
    }

    /**
     * Add new {@link CreatorCustomization} of the type.
     *
     * @param creatorCustomization creator customization
     * @return updated builder instance
     */
    public TypeCustomizationBuilder creator(CreatorCustomization creatorCustomization) {
        this.creatorCustomization = creatorCustomization;
        return this;
    }

    /**
     * Add new {@link CreatorCustomization} of the type.
     * <br>
     * Shortcut method to the {@link #creator(CreatorCustomization)}. It is not required to create {@link CreatorCustomizationBuilder}
     * since this method will create it based on the creator method name and expose it over the creatorBuilder parameter.
     * <br>
     * Example usage:
     * <pre>{@code
     * typeBuilder.creator("factoryMethodName",
     *                     creatorBuilder -> creatorBuilder.addParameter(String.class, "firstParameter"));
     * }</pre>
     * @param creatorMethodName creator method name
     * @param creatorBuilder creator builder instance consumer
     * @return updated builder instance
     */
    public TypeCustomizationBuilder creator(String creatorMethodName, Consumer<CreatorCustomizationBuilder> creatorBuilder) {
        CreatorCustomizationBuilder builder = CreatorCustomization.builder(creatorMethodName);
        creatorBuilder.accept(builder);
        return creator(builder.build());
    }

    /**
     * Add new {@link CreatorCustomization} of the type.
     * <br>
     * Shortcut method to the {@link #creator(CreatorCustomization)}. It is not required to create {@link CreatorCustomizationBuilder}
     * since this method will create it and expose it over the creatorBuilder parameter. Since no factory method name is
     * provided, this creator builder is targeting constructors.
     * <br>
     * Example usage:
     * <pre>{@code
     * typeBuilder.creator(creatorBuilder -> creatorBuilder.addParameter(String.class, "firstParameter"));
     * }</pre>
     * @param creatorBuilder creator builder instance consumer
     * @return updated builder instance
     */
    public TypeCustomizationBuilder creator(Consumer<CreatorCustomizationBuilder> creatorBuilder) {
        CreatorCustomizationBuilder builder = CreatorCustomization.builder();
        creatorBuilder.accept(builder);
        return creator(builder.build());
    }

    public TypeCustomizationBuilder typeInfo(TypeInfoCustomization customization) {
        typeInfoCustomization = customization;
        return this;
    }

    public TypeCustomizationBuilder typeInfo(String fieldName, Consumer<TypeInfoCustomizationBuilder> builderConsumer) {
        TypeInfoCustomizationBuilder builder = TypeInfoCustomization.builder(fieldName);
        builderConsumer.accept(builder);
        return typeInfo(builder.build());
    }

    public TypeCustomizationBuilder ignoreTypeInfo() {
        this.ignoreTypeInfo = true;
        return this;
    }

    @Override
    public TypeCustomization build() {
        return new TypeCustomizationImpl(this);
    }

    Class<?> getTypeClass() {
        return typeClass;
    }

    String[] getPropertyOrder() {
        return propertyOrder;
    }

    PropertyVisibilityStrategy getVisibleStrategy() {
        return visibilityStrategy;
    }

    boolean isIgnorePropertyOrder() {
        return ignorePropertyOrder;
    }

    boolean isIgnoreVisibilityStrategy() {
        return ignoreVisibilityStrategy;
    }

    CreatorCustomization getCreatorCustomization() {
        return creatorCustomization;
    }

    Map<String, PropertyCustomization> getPropertyCustomizations() {
        return propertyCustomizations;
    }

    TypeInfoCustomization getTypeInfoCustomization() {
        return typeInfoCustomization;
    }

    boolean isIgnoreTypeInfo() {
        return ignoreTypeInfo;
    }

}
