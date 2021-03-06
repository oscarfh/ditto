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
package org.eclipse.ditto.signals.commands.live.modify;

import javax.annotation.Nonnull;

import org.eclipse.ditto.signals.commands.live.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.signals.commands.live.base.LiveCommandResponseFactory;
import org.eclipse.ditto.signals.commands.live.base.LiveEventFactory;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.CreateThingResponse;
import org.eclipse.ditto.signals.events.things.ThingCreated;

/**
 * LiveCommandAnswer builder for producing {@code CommandResponse}s and {@code Event}s for {@link CreateThing}
 * commands.
 */
public interface CreateThingLiveCommandAnswerBuilder extends
        LiveCommandAnswerBuilder.ModifyCommandResponseStep<CreateThingLiveCommandAnswerBuilder.ResponseFactory,
                CreateThingLiveCommandAnswerBuilder.EventFactory> {

    /**
     * Factory for {@code CommandResponse}s to {@link CreateThing} command.
     */
    interface ResponseFactory extends LiveCommandResponseFactory {

        /**
         * Creates a {@link CreateThingResponse} using the values of the {@code Command}.
         *
         * @return the response.
         */
        @Nonnull
        CreateThingResponse created();

        /**
         * Creates a {@link ThingErrorResponse} indicating a conflict.
         *
         * @return the response.
         * @see org.eclipse.ditto.signals.commands.things.exceptions.ThingConflictException ThingConflictException
         */
        @Nonnull
        ThingErrorResponse thingConflictError();
    }

    /**
     * Factory for events triggered by {@link CreateThing} commands.
     */
    @SuppressWarnings("squid:S1609")
    interface EventFactory extends LiveEventFactory {

        /**
         * Creates a {@link ThingCreated} event using the values of the {@code Command}.
         *
         * @return the event.
         */
        @Nonnull
        ThingCreated created();
    }

}
