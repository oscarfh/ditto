/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
package org.eclipse.ditto.model.connectivity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonMissingFieldException;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonParseException;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.auth.AuthorizationModelFactory;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;

/**
 * Immutable implementation of {@link Connection}.
 */
@Immutable
final class ImmutableConnection implements Connection {

    private static final Pattern URI_REGEX_PATTERN = Pattern.compile(Connection.UriRegex.REGEX);

    private final String id;
    private final ConnectionType connectionType;
    private final AuthorizationContext authorizationContext;
    private final String uri;
    private final String protocol;
    @Nullable private final String username;
    @Nullable private final String password;
    private final String hostname;
    private final int port;
    private final String path;

    private final Set<Source> sources;
    private final Set<Target> targets;
    private final boolean failoverEnabled;
    private final boolean validateCertificate;
    private final int throttle;
    private final int processorPoolSize;

    ImmutableConnection(final ImmutableConnectionBuilder builder) {
        this.id = builder.id;
        this.connectionType = builder.connectionType;
        this.uri = builder.uri;
        this.authorizationContext = builder.authorizationContext;
        checkSourceAndTargetAreValid(builder);
        this.sources = Collections.unmodifiableSet(new HashSet<>(builder.sources));
        this.targets = Collections.unmodifiableSet(new HashSet<>(builder.targets));
        this.failoverEnabled = builder.failoverEnabled;
        this.validateCertificate = builder.validateCertificate;
        this.throttle = builder.throttle;
        this.processorPoolSize = builder.processorPoolSize;

        final Matcher matcher = URI_REGEX_PATTERN.matcher(uri);

        if (matcher.matches()) {
            protocol = matcher.group(Connection.UriRegex.PROTOCOL_REGEX_GROUP);
            username = matcher.group(Connection.UriRegex.USERNAME_REGEX_GROUP);
            password = matcher.group(Connection.UriRegex.PASSWORD_REGEX_GROUP);
            hostname = matcher.group(Connection.UriRegex.HOSTNAME_REGEX_GROUP);
            port = Integer.parseInt(matcher.group(Connection.UriRegex.PORT_REGEX_GROUP));
            path = matcher.group(Connection.UriRegex.PATH_REGEX_GROUP);
        } else {
            throw ConnectionUriInvalidException.newBuilder(uri).build();
        }
    }

    private void checkSourceAndTargetAreValid(final ImmutableConnectionBuilder builder) {
        if (builder.sources.isEmpty() && builder.targets.isEmpty()) {
            throw ConnectionConfigurationInvalidException
                    .newBuilder("Either a source or a target must be specified " +
                            "in the configuration of a connection.")
                    .build();
        }
    }

    /**
     * Creates a new {@code Connection} object from the specified JSON object.
     *
     * @param jsonObject a JSON object which provides the data for the Connection to be created.
     * @return a new Connection which is initialised with the extracted data from {@code jsonObject}.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if {@code jsonObject} is not an appropriate JSON object.
     */
    public static Connection fromJson(final JsonObject jsonObject) {
        final String readId = jsonObject.getValueOrThrow(JsonFields.ID);
        final String readConnectionTypeStr = jsonObject.getValueOrThrow(JsonFields.CONNECTION_TYPE);
        final ConnectionType readConnectionType = ConnectionType.forName(readConnectionTypeStr)
                .orElseThrow(() -> JsonParseException.newBuilder()
                        .message("Invalid connection type: " + readConnectionTypeStr)
                        .build());
        final String readUri = jsonObject.getValueOrThrow(JsonFields.URI);
        final JsonArray authContext = jsonObject.getValue(JsonFields.AUTHORIZATION_CONTEXT)
                .orElseGet(() ->
                        jsonObject.getValue(
                                "authorizationSubject") // as a fallback use the already persisted "authorizationSubject" field
                                .filter(JsonValue::isString)
                                .map(JsonValue::asString)
                                .map(str -> JsonArray.newBuilder().add(str).build())
                                .orElseThrow(() -> new JsonMissingFieldException(JsonFields.AUTHORIZATION_CONTEXT))
                );
        final List<AuthorizationSubject> authorizationSubjects = authContext.stream()
                .filter(JsonValue::isString)
                .map(JsonValue::asString)
                .map(AuthorizationSubject::newInstance)
                .collect(Collectors.toList());
        final AuthorizationContext readAuthorizationContext =
                AuthorizationModelFactory.newAuthContext(authorizationSubjects);
        final Set<Source> readSources = jsonObject.getValue(JsonFields.CONSUME)
                .map(array -> array.stream()
                        .filter(JsonValue::isObject)
                        .map(JsonValue::asObject)
                        .map(ImmutableSource::fromJson)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        final Set<Target> readTargets = jsonObject.getValue(JsonFields.PUBLISH)
                .map(array -> array.stream()
                        .filter(JsonValue::isObject)
                        .map(JsonValue::asObject)
                        .map(ImmutableTarget::fromJson)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        final Optional<Boolean> readFailoverEnabled = jsonObject.getValue(JsonFields.FAILOVER_ENABLED);
        final Optional<Boolean> readValidateCertificates = jsonObject.getValue(JsonFields.VALIDATE_CERTIFICATES);
        final Optional<Integer> readThrottle = jsonObject.getValue(JsonFields.THROTTLE);
        final Optional<Integer> readProcessorPoolSize = jsonObject.getValue(JsonFields.PROCESSOR_POOL_SIZE);

        final ConnectionBuilder builder =
                ImmutableConnectionBuilder.of(readId, readConnectionType, readUri, readAuthorizationContext);

        builder.sources(readSources);
        builder.targets(readTargets);
        readThrottle.ifPresent(builder::throttle);
        readFailoverEnabled.ifPresent(builder::failoverEnabled);
        readValidateCertificates.ifPresent(builder::validateCertificate);
        readProcessorPoolSize.ifPresent(builder::processorPoolSize);
        return builder.build();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ConnectionType getConnectionType() {
        return connectionType;
    }

    @Override
    public AuthorizationContext getAuthorizationContext() {
        return authorizationContext;
    }

    @Override
    public Set<Source> getSources() {
        return sources;
    }

    @Override
    public Set<Target> getTargets() {
        return targets;
    }

    @Override
    public boolean isFailoverEnabled() {
        return failoverEnabled;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public int getThrottle() {
        return throttle;
    }

    @Override
    public boolean isValidateCertificates() {
        return validateCertificate;
    }

    @Override
    public int getProcessorPoolSize() {
        return processorPoolSize;
    }

    @Override
    public JsonObject toJson(final JsonSchemaVersion schemaVersion, final Predicate<JsonField> thePredicate) {
        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        final JsonObjectBuilder jsonObjectBuilder = JsonFactory.newObjectBuilder();

        jsonObjectBuilder.set(JsonFields.SCHEMA_VERSION, schemaVersion.toInt(), predicate);
        jsonObjectBuilder.set(JsonFields.ID, id, predicate);
        jsonObjectBuilder.set(JsonFields.CONNECTION_TYPE, connectionType.getName(), predicate);
        jsonObjectBuilder.set(JsonFields.URI, uri, predicate);
        jsonObjectBuilder.set(JsonFields.AUTHORIZATION_CONTEXT, authorizationContext.stream()
                .map(AuthorizationSubject::getId)
                .map(JsonFactory::newValue)
                .collect(JsonCollectors.valuesToArray()), predicate);
        jsonObjectBuilder.set(JsonFields.CONSUME, sources.stream()
                .map(source -> source.toJson(schemaVersion, thePredicate))
                .collect(JsonCollectors.valuesToArray()), predicate.and(Objects::nonNull));
        jsonObjectBuilder.set(JsonFields.PUBLISH, targets.stream()
                .map(source -> source.toJson(schemaVersion, thePredicate))
                .collect(JsonCollectors.valuesToArray()), predicate.and(Objects::nonNull));
        jsonObjectBuilder.set(JsonFields.FAILOVER_ENABLED, failoverEnabled, predicate);
        jsonObjectBuilder.set(JsonFields.VALIDATE_CERTIFICATES, validateCertificate, predicate);
        jsonObjectBuilder.set(JsonFields.THROTTLE, throttle, predicate);
        jsonObjectBuilder.set(JsonFields.PROCESSOR_POOL_SIZE, processorPoolSize, predicate);
        return jsonObjectBuilder.build();
    }

    @SuppressWarnings("OverlyComplexMethod")
    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        final ImmutableConnection that = (ImmutableConnection) o;
        return failoverEnabled == that.failoverEnabled &&
                port == that.port &&
                Objects.equals(id, that.id) &&
                Objects.equals(connectionType, that.connectionType) &&
                Objects.equals(authorizationContext, that.authorizationContext) &&
                Objects.equals(sources, that.sources) &&
                Objects.equals(targets, that.targets) &&
                Objects.equals(uri, that.uri) &&
                Objects.equals(protocol, that.protocol) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(path, that.path) &&
                Objects.equals(throttle, that.throttle) &&
                Objects.equals(processorPoolSize, that.processorPoolSize) &&
                Objects.equals(validateCertificate, that.validateCertificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, connectionType, authorizationContext, sources, targets,
                failoverEnabled, uri, protocol, username, password, hostname, path, port, validateCertificate, throttle,
                processorPoolSize);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "id=" + id +
                ", connectionType=" + connectionType +
                ", authorizationContext=" + authorizationContext +
                ", failoverEnabled=" + failoverEnabled +
                ", uri=" + uri +
                ", protocol=" + protocol +
                ", username=" + username +
                ", password=" + password +
                ", hostname=" + hostname +
                ", port=" + port +
                ", path=" + path +
                ", sources=" + sources +
                ", targets=" + targets +
                ", validateCertificate=" + validateCertificate +
                ", throttle=" + throttle +
                ", processorPoolSize=" + processorPoolSize +
                "]";
    }
}