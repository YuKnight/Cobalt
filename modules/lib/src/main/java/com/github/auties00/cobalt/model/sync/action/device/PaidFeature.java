package com.github.auties00.cobalt.model.sync.action.device;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Represents a single paid business feature flag carried inside a
 * {@link SubscriptionsSyncV2Action}.
 *
 * <p>Each entry advertises whether a named paid feature is enabled for the
 * authenticated business account, optionally bounded by a usage limit and
 * an expiration timestamp.
 *
 * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature
 */
@ProtobufMessage(name = "SyncActionValue.SubscriptionsSyncV2Action.PaidFeature")
public final class PaidFeature {
    /**
     * The canonical paid feature name reported by the server.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.name
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    String name;

    /**
     * Whether the feature is currently enabled for the account.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.enabled
     */
    @ProtobufProperty(index = 2, type = ProtobufType.BOOL)
    Boolean enabled;

    /**
     * The optional usage limit attached to this feature.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.limit
     */
    @ProtobufProperty(index = 3, type = ProtobufType.INT32)
    Integer limit;

    /**
     * The optional expiration timestamp, in seconds since the epoch.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.expirationTime
     */
    @ProtobufProperty(index = 4, type = ProtobufType.INT64)
    Long expirationTime;

    /**
     * Constructs a new {@code PaidFeature} from raw protobuf field values.
     *
     * @param name           the feature name
     * @param enabled        the enabled flag
     * @param limit          the usage limit
     * @param expirationTime the expiration timestamp in seconds
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature
     */
    PaidFeature(String name, Boolean enabled, Integer limit, Long expirationTime) {
        this.name = name;
        this.enabled = enabled;
        this.limit = limit;
        this.expirationTime = expirationTime;
    }

    /**
     * Returns the canonical paid feature name, if present.
     *
     * @return an {@link Optional} containing the feature name
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.name
     */
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * Returns whether this feature is enabled.
     *
     * <p>Returns {@code false} if the field was unset on the wire.
     *
     * @return {@code true} if the feature is enabled
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.enabled
     */
    public boolean enabled() {
        return enabled != null && enabled;
    }

    /**
     * Returns the usage limit attached to this feature, if any.
     *
     * @return an {@link OptionalInt} containing the limit
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.limit
     */
    public OptionalInt limit() {
        return limit == null ? OptionalInt.empty() : OptionalInt.of(limit);
    }

    /**
     * Returns the expiration timestamp, in seconds since the epoch, if any.
     *
     * @return an {@link OptionalLong} containing the expiration timestamp
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.expirationTime
     */
    public OptionalLong expirationTime() {
        return expirationTime == null ? OptionalLong.empty() : OptionalLong.of(expirationTime);
    }

    /**
     * Sets the canonical paid feature name.
     *
     * @param name the feature name
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.name
     */
    public PaidFeature setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the enabled flag.
     *
     * @param enabled the enabled flag
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.enabled
     */
    public PaidFeature setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets the usage limit.
     *
     * @param limit the limit
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.limit
     */
    public PaidFeature setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the expiration timestamp.
     *
     * @param expirationTime the expiration timestamp in seconds
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$PaidFeature.expirationTime
     */
    public PaidFeature setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }
}
