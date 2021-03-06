/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.things.persistence.actors.strategies.commands;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAttributes;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAttributesResponse;
import org.eclipse.ditto.signals.events.things.ThingEvent;

/**
 * This strategy handles the {@link RetrieveAttributes} command.
 */
@Immutable
final class RetrieveAttributesStrategy extends AbstractThingCommandStrategy<RetrieveAttributes> {

    /**
     * Constructs a new {@code RetrieveAttributesStrategy} object.
     */
    RetrieveAttributesStrategy() {
        super(RetrieveAttributes.class);
    }

    @Override
    protected Result<ThingEvent> doApply(final Context<ThingId> context, @Nullable final Thing thing,
            final long nextRevision, final RetrieveAttributes command) {
        final ThingId thingId = context.getState();
        final DittoHeaders dittoHeaders = command.getDittoHeaders();

        return extractAttributes(thing)
                .map(attributes -> getAttributesJson(attributes, command))
                .map(attributesJson -> RetrieveAttributesResponse.of(thingId, attributesJson, dittoHeaders))
                .<Result<ThingEvent>>map(response ->
                        ResultFactory.newQueryResult(command, appendETagHeaderIfProvided(command, response, thing))
                )
                .orElseGet(() ->
                        ResultFactory.newErrorResult(ExceptionFactory.attributesNotFound(thingId, dittoHeaders),
                                command)
                );
    }

    private Optional<Attributes> extractAttributes(final @Nullable Thing thing) {
        return getEntityOrThrow(thing).getAttributes();
    }

    private static JsonObject getAttributesJson(final Attributes attributes, final RetrieveAttributes command) {
        return command.getSelectedFields()
                .map(selectedFields -> attributes.toJson(command.getImplementedSchemaVersion(), selectedFields))
                .orElseGet(() -> attributes.toJson(command.getImplementedSchemaVersion()));
    }


    @Override
    public Optional<EntityTag> previousEntityTag(final RetrieveAttributes command, @Nullable final Thing previousEntity) {
        return nextEntityTag(command, previousEntity);
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final RetrieveAttributes command, @Nullable final Thing newEntity) {
        return extractAttributes(newEntity).flatMap(EntityTag::fromEntity);
    }
}
