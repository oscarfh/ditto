/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.connectivity.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.atteo.classindex.IndexAnnotated;

/**
 * Classes annotated with {@link PayloadMapper} are indexed and loaded on startup to be referenced by its alias in
 * payload mapping definitions of a {@link org.eclipse.ditto.model.connectivity.Connection}.
 * If the mapper requires no {@link org.eclipse.ditto.model.connectivity.MappingContext} for initialization it can also
 * be directly used in the list of mappings of a {@link org.eclipse.ditto.model.connectivity.Source} or a
 * {@link org.eclipse.ditto.model.connectivity.Target} using one of the defined aliases.
 */
@Target(ElementType.TYPE)
@IndexAnnotated
@Retention(RetentionPolicy.RUNTIME)
public @interface PayloadMapper {

    /**
     * @return the aliases which can be used to reference this {@link PayloadMapper}.
     */
    String[] alias();

    /**
     * @return {@code true} if the mapper requires mandatory {@code config} options for initialization,
     * i.e. it cannot be used directly as a mapping without providing the
     * {@link org.eclipse.ditto.model.connectivity.MappingContext#getOptionsAsJson()}.
     */
    boolean requiresMandatoryConfiguration() default false;
}
