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
package org.eclipse.ditto.signals.commands.things;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonParseOptions;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.auth.AuthorizationModelFactory;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.auth.DittoAuthorizationContextType;
import org.eclipse.ditto.model.base.headers.DittoHeaderDefinition;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.AccessControlList;
import org.eclipse.ditto.model.things.AclEntry;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.FeatureDefinition;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Permission;
import org.eclipse.ditto.model.things.ThingDefinition;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingLifecycle;
import org.eclipse.ditto.model.things.ThingRevision;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.things.exceptions.AttributeNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.AttributeNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.AttributePointerInvalidException;
import org.eclipse.ditto.signals.commands.things.exceptions.AttributesNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.AttributesNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeatureDefinitionNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeatureDefinitionNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeatureNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeatureNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeaturePropertiesNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeaturePropertiesNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeaturePropertyNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeaturePropertyNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeaturesNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.FeaturesNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.MissingThingIdsException;
import org.eclipse.ditto.signals.commands.things.exceptions.PolicyIdNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.PolicyIdNotAllowedException;
import org.eclipse.ditto.signals.commands.things.exceptions.PolicyIdNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.PolicyInvalidException;
import org.eclipse.ditto.signals.commands.things.exceptions.PolicyNotAllowedException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingConflictException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingDefinitionNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingIdNotExplicitlySettableException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingNotCreatableException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingNotDeletableException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingNotModifiableException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingPreconditionFailedException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingPreconditionNotModifiedException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingTooManyModifyingRequestsException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingUnavailableException;

/**
 * Defines constants for testing.
 */
public final class TestConstants {

    public static final long THING_SIZE_LIMIT_BYTES = Long.parseLong(
            System.getProperty(ThingCommandSizeValidator.DITTO_LIMITS_THINGS_MAX_SIZE_BYTES, "-1"));

    /**
     * A known correlation id for testing.
     */
    public static final String CORRELATION_ID = "a780b7b5-fdd2-4864-91fc-80df6bb0a636";

    /**
     * Known command headers.
     */
    public static final DittoHeaders DITTO_HEADERS = DittoHeaders.newBuilder()
            .correlationId(CORRELATION_ID)
            .authorizationContext(AuthorizationContext.newInstance(DittoAuthorizationContextType.UNSPECIFIED,
                    AuthorizationSubject.newInstance("the_subject"),
                    AuthorizationSubject.newInstance("another_subject")))
            .build();

    /**
     * Empty command headers.
     */
    public static final DittoHeaders EMPTY_DITTO_HEADERS = DittoHeaders.empty();

    /**
     * A known timestamp.
     */
    public static final Instant TIMESTAMP = Instant.EPOCH;

    /**
     * Known JSON parse options.
     */
    public static final JsonParseOptions JSON_PARSE_OPTIONS =
            JsonFactory.newParseOptionsBuilder().withoutUrlDecoding().build();

    /**
     * A known JSON field selector.
     */
    public static final JsonFieldSelector JSON_FIELD_SELECTOR_ATTRIBUTES =
            JsonFactory.newFieldSelector("attributes(location,maker)", JSON_PARSE_OPTIONS);

    /**
     * A known JSON field selector.
     */
    public static final JsonFieldSelector JSON_FIELD_SELECTOR_ATTRIBUTES_WITH_THING_ID =
            JsonFactory.newFieldSelector("thingId,attributes(location,maker)", JSON_PARSE_OPTIONS);

    /**
     * A known JSON field selector.
     */
    public static final JsonFieldSelector JSON_FIELD_SELECTOR_FEATURE_PROPERTIES =
            JsonFactory.newFieldSelector("properties/target_year_1", JSON_PARSE_OPTIONS);

    private TestConstants() {
        throw new AssertionError();
    }

    /**
     * Authorization-related test constants.
     */
    public static final class Authorization {

        /**
         * A known Authorization Subject for testing.
         */
        public static final AuthorizationSubject AUTH_SUBJECT_OLDMAN =
                AuthorizationModelFactory.newAuthSubject("JohnOldman");

        /**
         * Another known AuthorizationSubject for testing.
         */
        public static final AuthorizationSubject AUTH_SUBJECT_GRIMES =
                AuthorizationModelFactory.newAuthSubject("FrankGrimes");

        /**
         * An Authorization Context which contains all known Authorization Subjects.
         */
        public static final AuthorizationContext AUTH_CONTEXT =
                AuthorizationModelFactory.newAuthContext(DittoAuthorizationContextType.UNSPECIFIED,
                        AUTH_SUBJECT_OLDMAN, AUTH_SUBJECT_GRIMES);

        public static final List<AuthorizationSubject> AUTHORIZATION_SUBJECTS =
                Arrays.asList(AUTH_SUBJECT_OLDMAN, AUTH_SUBJECT_GRIMES);

        /**
         * The known ACL entry of John Oldman.
         */
        public static final AclEntry ACL_ENTRY_OLDMAN =
                ThingsModelFactory.newAclEntry(Authorization.AUTH_SUBJECT_OLDMAN, Permission.READ, Permission.WRITE,
                        Permission.ADMINISTRATE);

        /**
         * The known ACL entry of Frank Grimes.
         */
        public static final AclEntry ACL_ENTRY_GRIMES =
                ThingsModelFactory.newAclEntry(Authorization.AUTH_SUBJECT_GRIMES, Permission.READ);

        private Authorization() {
            throw new AssertionError();
        }
    }


    /**
     * Thing-related test constants.
     */
    public static final class Thing {

        /**
         * A known Thing ID for testing.
         */
        public static final ThingId THING_ID = ThingId.of("example.com", "testThing");

        /**
         * A known PolicyID of a Thing.
         */
        public static final PolicyId POLICY_ID = PolicyId.of("example.com:testPolicy");

        /**
         * A known Definition of a Thing.
         */
        public static final ThingDefinition DEFINITION = ThingsModelFactory.newDefinition("example:test" +
                ":definition");

        /**
         * A known lifecycle of a Thing.
         */
        public static final ThingLifecycle LIFECYCLE = ThingLifecycle.ACTIVE;

        /**
         * A known Access Control List of a Thing.
         */
        public static final AccessControlList ACL =
                ThingsModelFactory.newAcl(Authorization.ACL_ENTRY_OLDMAN, Authorization.ACL_ENTRY_GRIMES);

        public static final JsonPointer LOCATION_ATTRIBUTE_POINTER = JsonFactory.newPointer("location");

        /**
         * A known location attribute for testing.
         */
        public static final JsonObject LOCATION_ATTRIBUTE_VALUE = JsonFactory.newObjectBuilder()
                .set("latitude", 44.673856)
                .set("longitude", 8.261719)
                .build();

        /**
         * Known attributes of a Thing.
         */
        public static final Attributes ATTRIBUTES = ThingsModelFactory.newAttributesBuilder()
                .set(LOCATION_ATTRIBUTE_POINTER, LOCATION_ATTRIBUTE_VALUE)
                .set("maker", "Bosch")
                .build();

        /**
         * A known revision number of a Thing.
         */
        public static final long REVISION_NUMBER = 0;

        /**
         * A known revision of a Thing.
         */
        public static final ThingRevision REVISION = ThingsModelFactory.newThingRevision(REVISION_NUMBER);

        /**
         * A known Thing for testing.
         */
        public static final org.eclipse.ditto.model.things.Thing THING = ThingsModelFactory.newThingBuilder()
                .setId(THING_ID)
                .setAttributes(ATTRIBUTES)
                .setDefinition(DEFINITION)
                .setFeatures(Feature.FEATURES)
                .setLifecycle(LIFECYCLE)
                .setPolicyId(POLICY_ID)
                .build();

        /**
         * A known {@code ThingConflictException}.
         */
        public static final ThingConflictException THING_CONFLICT_EXCEPTION =
                ThingConflictException.newBuilder(THING_ID).build();

        /**
         * A known {@code ThingIdNotExplicitlySettableException}.
         */
        public static final ThingIdNotExplicitlySettableException THING_ID_NOT_EXPLICITLY_SETTABLE_EXCEPTION =
                ThingIdNotExplicitlySettableException.forPostMethod().build();

        /**
         * A known {@code ThingPreconditionFailedException}.
         */
        public static final ThingPreconditionFailedException THING_PRECONDITION_FAILED_EXCEPTION =
                ThingPreconditionFailedException
                        .newBuilder(DittoHeaderDefinition.IF_MATCH.getKey(), "\"rev:1\"", "\"rev:2\"")
                        .build();

        /**
         * A known {@code ThingPreconditionNotModifiedException}.
         */
        public static final ThingPreconditionNotModifiedException THING_PRECONDITION_NOT_MODIFIED_EXCEPTION =
                ThingPreconditionNotModifiedException
                        .newBuilder("\"rev:1\"", "*")
                        .build();

        /**
         * A known {@code ThingNotAccessibleException}.
         */
        public static final ThingNotAccessibleException THING_NOT_ACCESSIBLE_EXCEPTION =
                ThingNotAccessibleException.newBuilder(THING_ID).build();

        /**
         * A known {@code ThingNotDeletableException}.
         */
        public static final ThingNotDeletableException THING_NOT_DELETABLE_EXCEPTION =
                ThingNotDeletableException.newBuilder(THING_ID).build();

        /**
         * A known {@code ThingNotModifiableException}.
         */
        public static final ThingNotCreatableException THING_NOT_CREATABLE_EXCEPTION =
                ThingNotCreatableException.newBuilderForPolicyMissing(THING_ID, POLICY_ID).build();

        /**
         * A known {@code ThingNotModifiableException}.
         */
        public static final ThingNotModifiableException THING_NOT_MODIFIABLE_EXCEPTION =
                ThingNotModifiableException.newBuilder(THING_ID).build();

        /**
         * A known {@code PolicyIdNotModifiableException}.
         */
        public static final PolicyIdNotModifiableException POLICY_ID_NOT_MODIFIABLE_EXCEPTION =
                PolicyIdNotModifiableException.newBuilder(THING_ID).build();

        /**
         * A known {@code PolicyIdNotAllowedException}.
         */
        public static final PolicyIdNotAllowedException POLICY_ID_NOT_ALLOWED_EXCEPTION =
                PolicyIdNotAllowedException.newBuilder(THING_ID).build();
        /**
         * A known {@code PolicyNotAllowedException}.
         */
        public static final PolicyNotAllowedException POLICY_NOT_ALLOWED_EXCEPTION =
                PolicyNotAllowedException.newBuilder(THING_ID).build();

        /**
         * A known {@code PolicyIdNotAccessibleException}.
         */
        public static final PolicyIdNotAccessibleException POLICY_ID_NOT_ACCESSIBLE_EXCEPTION =
                PolicyIdNotAccessibleException.newBuilder(THING_ID).build();

        /**
         * A known {@code ThingDefinitionNotAccessibleException}.
         */
        public static final ThingDefinitionNotAccessibleException THING_DEFINITION_NOT_ACCESSIBLE_EXCEPTION =
                ThingDefinitionNotAccessibleException.newBuilder(THING_ID).build();

        /**
         * A known {@code AttributesNotAccessibleException}.
         */
        public static final AttributesNotAccessibleException ATTRIBUTES_NOT_ACCESSIBLE_EXCEPTION =
                AttributesNotAccessibleException.newBuilder(THING_ID).build();
        /**
         * A known {@code AttributesNotModifiableException}.
         */
        public static final AttributesNotModifiableException ATTRIBUTES_NOT_MODIFIABLE_EXCEPTION =
                AttributesNotModifiableException.newBuilder(THING_ID).build();
        /**
         * A known {@code AttributeNotAccessibleException}.
         */
        public static final AttributeNotAccessibleException ATTRIBUTE_NOT_ACCESSIBLE_EXCEPTION =
                AttributeNotAccessibleException.newBuilder(THING_ID, LOCATION_ATTRIBUTE_POINTER).build();
        /**
         * A known {@code AttributeNotModifiableException}.
         */
        public static final AttributeNotModifiableException ATTRIBUTE_NOT_MODIFIABLE_EXCEPTION =
                AttributeNotModifiableException.newBuilder(THING_ID, LOCATION_ATTRIBUTE_POINTER).build();
        /**
         * A known {@code AttributePointerInvalidException}.
         */
        public static final AttributePointerInvalidException ATTRIBUTE_POINTER_INVALID_EXCEPTION =
                AttributePointerInvalidException.newBuilder(LOCATION_ATTRIBUTE_POINTER).build();
        /**
         * A known {@code ThingUnavailableException}.
         */
        public static final ThingUnavailableException THING_UNAVAILABLE_EXCEPTION =
                ThingUnavailableException.newBuilder(THING_ID).build();
        /**
         * A known {@code ThingTooManyModifyingRequestsException}.
         */
        public static final ThingTooManyModifyingRequestsException THING_TOO_MANY_MODIFYING_REQUESTS_EXCEPTION =
                ThingTooManyModifyingRequestsException.newBuilder(THING_ID).build();
        public static final MissingThingIdsException MISSING_THING_IDS_EXCEPTION =
                MissingThingIdsException.newBuilder().build();
        /**
         * List of required policy permissions for a Thing.
         */
        public static Collection<String> REQUIRED_THING_PERMISSIONS = Arrays.asList("READ", "WRITE");
        /**
         * A known {@code PolicyInvalidException}.
         */
        public static final PolicyInvalidException POLICY_INVALID_EXCEPTION =
                PolicyInvalidException.newBuilder(REQUIRED_THING_PERMISSIONS, THING_ID).build();

        private Thing() {
            throw new AssertionError();
        }
    }

    /**
     * Feature-related test constants.
     */
    public static final class Feature {

        /**
         * A known ID of a Feature.
         */
        public static final String FLUX_CAPACITOR_ID = "FluxCapacitor";

        /**
         * Pointer of a known Feature Property.
         */
        public static final JsonPointer FLUX_CAPACITOR_PROPERTY_POINTER = JsonFactory.newPointer("target_year_1");

        /**
         * Value of a known Feature Property.
         */
        public static final JsonValue FLUX_CAPACITOR_PROPERTY_VALUE = JsonFactory.newValue(1955);

        /**
         * Properties of a known Feature.
         */
        public static final FeatureDefinition FLUX_CAPACITOR_DEFINITION =
                FeatureDefinition.fromIdentifier("org.eclipse.ditto:fluxcapacitor:1.0.0");

        /**
         * Properties of a known Feature.
         */
        public static final FeatureProperties FLUX_CAPACITOR_PROPERTIES =
                ThingsModelFactory.newFeaturePropertiesBuilder()
                        .set("target_year_1", 1955)
                        .set("target_year_2", 2015)
                        .set("target_year_3", 1885)
                        .build();

        /**
         * A known Feature which is required for time travel.
         */
        public static final org.eclipse.ditto.model.things.Feature FLUX_CAPACITOR =
                ThingsModelFactory.newFeatureBuilder()
                        .properties(FLUX_CAPACITOR_PROPERTIES)
                        .withId(FLUX_CAPACITOR_ID)
                        .build();

        /**
         * Known features of a Thing.
         */
        public static final Features FEATURES = ThingsModelFactory.newFeatures(FLUX_CAPACITOR);

        /**
         * A known {@code FeaturesNotAccessibleException}.
         */
        public static final FeaturesNotAccessibleException FEATURES_NOT_ACCESSIBLE_EXCEPTION =
                FeaturesNotAccessibleException.newBuilder(Thing.THING_ID).build();

        /**
         * A known {@code FeaturesNotModifiableException}.
         */
        public static final FeaturesNotModifiableException FEATURES_NOT_MODIFIABLE_EXCEPTION =
                FeaturesNotModifiableException.newBuilder(Thing.THING_ID).build();

        /**
         * A known {@code FeatureNotAccessibleException}.
         */
        public static final FeatureNotAccessibleException FEATURE_NOT_ACCESSIBLE_EXCEPTION =
                FeatureNotAccessibleException.newBuilder(Thing.THING_ID, FLUX_CAPACITOR_ID).build();

        /**
         * A known {@code FeatureNotModifiableException}.
         */
        public static final FeatureNotModifiableException FEATURE_NOT_MODIFIABLE_EXCEPTION =
                FeatureNotModifiableException.newBuilder(Thing.THING_ID, FLUX_CAPACITOR_ID).build();

        /**
         * A known {@code FeatureDefinitionNotAccessibleException}.
         */
        public static final FeatureDefinitionNotAccessibleException FEATURE_DEFINITION_NOT_ACCESSIBLE_EXCEPTION =
                FeatureDefinitionNotAccessibleException.newBuilder(Thing.THING_ID, FLUX_CAPACITOR_ID).build();

        /**
         * A known {@code FeatureDefinitionNotModifiableException}.
         */
        public static final FeatureDefinitionNotModifiableException FEATURE_DEFINITION_NOT_MODIFIABLE_EXCEPTION =
                FeatureDefinitionNotModifiableException.newBuilder(Thing.THING_ID, FLUX_CAPACITOR_ID).build();

        /**
         * A known {@code FeaturePropertiesNotAccessibleException}.
         */
        public static final FeaturePropertiesNotAccessibleException FEATURE_PROPERTIES_NOT_ACCESSIBLE_EXCEPTION =
                FeaturePropertiesNotAccessibleException.newBuilder(Thing.THING_ID, FLUX_CAPACITOR_ID).build();

        /**
         * A known {@code FeaturePropertiesNotModifiableException}.
         */
        public static final FeaturePropertiesNotModifiableException FEATURE_PROPERTIES_NOT_MODIFIABLE_EXCEPTION =
                FeaturePropertiesNotModifiableException.newBuilder(Thing.THING_ID, FLUX_CAPACITOR_ID).build();


        /**
         * A known {@code FeaturePropertyNotAccessibleException}.
         */
        public static final FeaturePropertyNotAccessibleException FEATURE_PROPERTY_NOT_ACCESSIBLE_EXCEPTION =
                FeaturePropertyNotAccessibleException
                        .newBuilder(Thing.THING_ID, FLUX_CAPACITOR_ID, FLUX_CAPACITOR_PROPERTY_POINTER).build();

        /**
         * A known {@code FeaturePropertyNotModifiableException}.
         */
        public static final FeaturePropertyNotModifiableException FEATURE_PROPERTY_NOT_MODIFIABLE_EXCEPTION =
                FeaturePropertyNotModifiableException
                        .newBuilder(Thing.THING_ID, FLUX_CAPACITOR_ID, FLUX_CAPACITOR_PROPERTY_POINTER).build();

        private Feature() {
            throw new AssertionError();
        }
    }

    public static class Pointer {

        public static final JsonPointer EMPTY_JSON_POINTER = JsonFactory.emptyPointer();
        public static final JsonPointer VALID_JSON_POINTER = JsonFactory.newPointer("properties/foo");
        public static final JsonPointer INVALID_JSON_POINTER = JsonFactory.newPointer("key1/äöü/foo");
    }

}
