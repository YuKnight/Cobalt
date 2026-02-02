package com.github.auties00.cobalt.props;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.ABPropTypeMismatchException;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for managing A/B testing properties (AB props) received from WhatsApp servers.
 * <p>
 * AB props are feature flags and configuration values that control client behavior.
 * The service stores props by their numeric {@code code} and provides type-safe
 * accessors that return {@link Optional} values for graceful handling of missing props.
 * <p>
 * When querying props before the first sync completes, the query will automatically
 * wait (up to a configurable timeout) for sync to complete before returning.
 * <p>
 * This class is thread-safe.
 *
 * @see ABProp
 */
public final class ABPropsService {
    private static final System.Logger LOGGER = System.getLogger("ABPropsService");

    /**
     * Default timeout for waiting for initial sync when querying props.
     */
    private static final Duration DEFAULT_SYNC_TIMEOUT = Duration.ofSeconds(30);

    private final WhatsAppClient client;

    /**
     * Thread-safe map storing AB props by their config code.
     * Key: config code (unique identifier)
     * Value: ABProp containing the configuration value
     */
    private final Map<Integer, ABProp> props;

    /**
     * Current hash of the AB props state, used for delta updates.
     * Null if no props have been synced yet.
     */
    private volatile String currentHash;

    /**
     * Future that completes when sync finishes.
     * Stored in AtomicReference to allow resetting by creating a new future.
     */
    private final AtomicReference<CompletableFuture<Boolean>> syncFuture;

    /**
     * Timeout to wait for initial sync when querying.
     */
    private final Duration syncTimeout;

    /**
     * Creates a new AB props service that will sync props from the server.
     * Uses the default sync timeout {@link #DEFAULT_SYNC_TIMEOUT}
     *
     * @param client the WhatsApp client instance to use for communication
     */
    public ABPropsService(WhatsAppClient client) {
        this(client, DEFAULT_SYNC_TIMEOUT);
    }

    /**
     * Creates a new AB props service with a custom sync timeout.
     *
     * @param client        the WhatsApp client instance to use for communication
     * @param syncTimeout timeout in milliseconds to wait for sync when querying
     */
    public ABPropsService(WhatsAppClient client, Duration syncTimeout) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.props = new ConcurrentHashMap<>();
        this.syncFuture = new AtomicReference<>(new CompletableFuture<>());
        this.syncTimeout = syncTimeout;
    }

    /**
     * Synchronizes AB props from the server.
     * <p>
     * This method sends a sync request to WhatsApp servers and processes the response.
     * On first sync, all props are requested. On subsequent syncs, only the hash is sent
     * to enable delta updates (the server will only send changed props).
     * <p>
     * After sync completes, the sync future is completed, allowing any waiting
     * query operations to proceed.
     *
     * @return true if sync succeeded, false otherwise
     */
    public boolean sync() {
        try {
            var request = createSyncRequest();
            var response = client.sendNode(request);
            var success = process(response);
            completeSync(success);
            return success;
        } catch (Throwable throwable) {
            LOGGER.log(System.Logger.Level.ERROR, "AB props sync failed: {0}", throwable.getMessage());
            failSync(throwable);
            return false;
        }
    }

    /**
     * Completes the sync future with the given result.
     * <p>
     * This releases all threads waiting in query methods.
     *
     * @param success whether the sync succeeded
     */
    private void completeSync(boolean success) {
        syncFuture.get().complete(success);
    }

    /**
     * Completes the sync future exceptionally.
     * <p>
     * This ensures waiting threads don't hang when sync fails.
     *
     * @param throwable the exception that caused sync to fail
     */
    private void failSync(Throwable throwable) {
        syncFuture.get().completeExceptionally(throwable);
    }

    /**
     * Waits for the initial sync to complete, with a timeout.
     * <p>
     * This is called internally by query methods to ensure props are available
     * before returning results. If the sync hasn't completed yet, this will block
     * until either the sync completes or the timeout expires.
     *
     * @return true if sync completed successfully before timeout, false if timeout occurred or sync failed
     */
    private boolean awaitSync() {
        try {
            var result = syncFuture.get().get(syncTimeout.toMillis(), TimeUnit.MILLISECONDS);
            return result != null && result;
        } catch (TimeoutException e) {
            LOGGER.log(System.Logger.Level.DEBUG, "Timeout waiting for AB props sync");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(System.Logger.Level.WARNING, "Interrupted while waiting for AB props sync");
            return false;
        } catch (Throwable e) {
            LOGGER.log(System.Logger.Level.WARNING, "Error waiting for AB props sync: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Creates a WebSocket node request to sync AB props from the server.
     */
    private NodeBuilder createSyncRequest() {
        var propsNode = new NodeBuilder()
                .description("props")
                .attribute("protocol", "1");

        if (currentHash != null) {
            propsNode.attribute("hash", currentHash);
        }

        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "abt")
                .attribute("to", "s.whatsapp.net")
                .attribute("type", "get")
                .content(propsNode.build());
    }

    /**
     * Processes an AB props response received from the server.
     */
    public boolean process(Node response) {
        Objects.requireNonNull(response, "response cannot be null");

        var propsNode = response.getChild("props", null);
        if (propsNode == null) {
            LOGGER.log(System.Logger.Level.WARNING, "AB props response missing <props> node");
            return false;
        }

        // Update hash for future delta updates
        propsNode.getAttributeAsString("hash").ifPresent(hash -> {
            this.currentHash = hash;
            LOGGER.log(System.Logger.Level.DEBUG, "Updated AB props hash: {0}", hash);
        });

        // Check if this is a delta update
        var isDelta = propsNode.getAttributeAsBool("delta_update", false);
        if (!isDelta) {
            // Full update - clear existing props
            props.clear();
        }

        // Parse individual prop entries
        var propNodes = propsNode.getChildren("prop");
        var count = 0;
        for (var propNode : propNodes) {
            var configCode = propNode.getAttributeAsInt("config_code");
            if (configCode.isEmpty()) {
                LOGGER.log(System.Logger.Level.WARNING, "Skipping prop without config_code");
                continue;
            }

            var configValue = propNode.getAttributeAsString("config_value");
            if (configValue.isEmpty()) {
                LOGGER.log(System.Logger.Level.WARNING, "Skipping prop {0} without config_value", configCode.getAsInt());
                continue;
            }

            var exposureKey = propNode.getAttributeAsLong("config_expo_key");
            var exposureKeyId = exposureKey.isPresent() ? exposureKey.getAsLong() : null;

            var prop = new ABProp(
                    configCode.getAsInt(),
                    configValue.get(),
                    exposureKeyId
            );

            props.put(prop.code(), prop);
            count++;
        }

        LOGGER.log(System.Logger.Level.INFO, "Synced {0} AB props from server (delta={1})", count, isDelta);
        return true;
    }

    /**
     * Queries a boolean AB prop by its config code.
     * <p>
     * If the initial sync hasn't completed, this method will wait (up to the configured timeout)
     * for the sync to complete before querying.
     *
     * @param configCode the numeric identifier of the prop
     * @return an Optional containing the boolean value if the prop exists, or empty if not found or timeout occurred
     */
    public Optional<Boolean> getBool(int configCode) {
        if (!awaitSync()) {
            LOGGER.log(System.Logger.Level.DEBUG, "Timeout waiting for AB props sync before querying config {0}", configCode);
            return Optional.empty();
        }

        return Optional.ofNullable(props.get(configCode))
                .map(ABProp::asBoolean);
    }

    /**
     * Queries an integer AB prop by its config code.
     * <p>
     * If the initial sync hasn't completed, this method will wait (up to the configured timeout)
     * for the sync to complete before querying.
     *
     * @param configCode the numeric identifier of the prop
     * @return an Optional containing the integer value if the prop exists and is parseable, or empty otherwise
     */
    public Optional<Integer> getInt(int configCode) {
        if (!awaitSync()) {
            LOGGER.log(System.Logger.Level.DEBUG, "Timeout waiting for AB props sync before querying config {0}", configCode);
            return Optional.empty();
        }

        var prop = props.get(configCode);
        if (prop == null) {
            return Optional.empty();
        }

        var value = prop.asInt();
        return value.isPresent() ? Optional.of(value.getAsInt()) : Optional.empty();
    }

    /**
     * Queries a long AB prop by its config code.
     * <p>
     * If the initial sync hasn't completed, this method will wait (up to the configured timeout)
     * for the sync to complete before querying.
     *
     * @param configCode the numeric identifier of the prop
     * @return an Optional containing the long value if the prop exists and is parseable, or empty otherwise
     */
    public Optional<Long> getLong(int configCode) {
        if (!awaitSync()) {
            LOGGER.log(System.Logger.Level.DEBUG, "Timeout waiting for AB props sync before querying config {0}", configCode);
            return Optional.empty();
        }

        var prop = props.get(configCode);
        if (prop == null) {
            return Optional.empty();
        }

        var value = prop.asLong();
        return value.isPresent() ? Optional.of(value.getAsLong()) : Optional.empty();
    }

    /**
     * Queries a double (floating-point) AB prop by its config code.
     * <p>
     * If the initial sync hasn't completed, this method will wait (up to the configured timeout)
     * for the sync to complete before querying.
     *
     * @param configCode the numeric identifier of the prop
     * @return an Optional containing the double value if the prop exists and is parseable, or empty otherwise
     */
    public Optional<Double> getDouble(int configCode) {
        if (!awaitSync()) {
            LOGGER.log(System.Logger.Level.DEBUG, "Timeout waiting for AB props sync before querying config {0}", configCode);
            return Optional.empty();
        }

        var prop = props.get(configCode);
        if (prop == null) {
            return Optional.empty();
        }

        var value = prop.asDouble();
        return value.isPresent() ? Optional.of(value.getAsDouble()) : Optional.empty();
    }

    /**
     * Queries a string AB prop by its config code.
     * <p>
     * If the initial sync hasn't completed, this method will wait (up to the configured timeout)
     * for the sync to complete before querying.
     *
     * @param configCode the numeric identifier of the prop
     * @return an Optional containing the string value if the prop exists, or empty if not found or timeout occurred
     */
    public Optional<String> getString(int configCode) {
        if (!awaitSync()) {
            LOGGER.log(System.Logger.Level.DEBUG, "Timeout waiting for AB props sync before querying config {0}", configCode);
            return Optional.empty();
        }

        return Optional.ofNullable(props.get(configCode))
                .map(ABProp::asString);
    }

    /**
     * Queries an AB prop with type validation.
     * <p>
     * If the initial sync hasn't completed, this method will wait (up to the configured timeout)
     * for the sync to complete before querying.
     * <p>
     * This method retrieves a prop and validates that it can be converted to the requested type.
     * If the prop exists but cannot be converted to the expected type, an exception is thrown
     * to alert developers of configuration mismatches.
     *
     * @param configCode   the numeric identifier of the prop
     * @param expectedType the expected type (Boolean, Integer, Long, Double, or String)
     * @param <T>          the type parameter
     * @return an Optional containing the typed value if the prop exists, or empty if not found or timeout occurred
     * @throws ABPropTypeMismatchException if the prop exists but cannot be converted to the expected type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(int configCode, Class<T> expectedType) {
        Objects.requireNonNull(expectedType, "expectedType cannot be null");

        if (!awaitSync()) {
            LOGGER.log(System.Logger.Level.DEBUG, "Timeout waiting for AB props sync before querying config {0}", configCode);
            return Optional.empty();
        }

        var prop = props.get(configCode);
        if (prop == null) {
            return Optional.empty();
        }

        // Type-safe conversion with validation
        if (expectedType == Boolean.class || expectedType == boolean.class) {
            return (Optional<T>) Optional.of(prop.asBoolean());
        } else if (expectedType == Integer.class || expectedType == int.class) {
            var value = prop.asInt();
            if (value.isEmpty()) {
                throw new ABPropTypeMismatchException(configCode, expectedType, prop.value());
            }
            return (Optional<T>) Optional.of(value.getAsInt());
        } else if (expectedType == Long.class || expectedType == long.class) {
            var value = prop.asLong();
            if (value.isEmpty()) {
                throw new ABPropTypeMismatchException(configCode, expectedType, prop.value());
            }
            return (Optional<T>) Optional.of(value.getAsLong());
        } else if (expectedType == Double.class || expectedType == double.class) {
            var value = prop.asDouble();
            if (value.isEmpty()) {
                throw new ABPropTypeMismatchException(configCode, expectedType, prop.value());
            }
            return (Optional<T>) Optional.of(value.getAsDouble());
        } else if (expectedType == String.class) {
            return (Optional<T>) Optional.of(prop.asString());
        } else {
            throw new IllegalArgumentException("Unsupported type: " + expectedType.getName());
        }
    }

    /**
     * Returns the number of AB props currently stored.
     *
     * @return the count of props
     */
    public int size() {
        return props.size();
    }

    /**
     * Checks if any AB props have been synced.
     *
     * @return true if props are available, false if no sync has occurred
     */
    public boolean isEmpty() {
        return props.isEmpty();
    }

    /**
     * Clears all stored AB props and resets the sync future.
     * <p>
     * This is typically called when disconnecting or resetting the session.
     * After calling this method, a new sync must be performed and queries will
     * wait for the new sync to complete.
     * <p>
     * This method is fully resettable - the sync future is replaced with a new instance,
     * allowing subsequent syncs to work correctly.
     */
    public void clear() {
        props.clear();
        currentHash = null;
        syncFuture.set(new CompletableFuture<>());
        LOGGER.log(System.Logger.Level.DEBUG, "Cleared all AB props and reset sync state");
    }
}
