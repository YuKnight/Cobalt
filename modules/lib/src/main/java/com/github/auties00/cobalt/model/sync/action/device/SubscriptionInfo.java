package com.github.auties00.cobalt.model.sync.action.device;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Represents per-subscription metadata carried inside a
 * {@link SubscriptionsSyncV2Action}.
 *
 * <p>Each entry describes one active or historical paid subscription owned
 * by the authenticated business account, including its tier, status,
 * lifecycle timestamps and origin source.
 *
 * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo
 */
@ProtobufMessage(name = "SyncActionValue.SubscriptionsSyncV2Action.SubscriptionInfo")
public final class SubscriptionInfo {
    /**
     * The opaque subscription identifier issued by the server.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.id
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    String id;

    /**
     * The numeric tier of the subscription.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.tier
     */
    @ProtobufProperty(index = 2, type = ProtobufType.INT32)
    Integer tier;

    /**
     * The current status string for the subscription (server-defined enum).
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.status
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    String status;

    /**
     * The start timestamp of the current billing window, in seconds since
     * the epoch.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.startTime
     */
    @ProtobufProperty(index = 4, type = ProtobufType.INT64)
    Long startTime;

    /**
     * The end (expiration) timestamp of the current billing window, in
     * seconds since the epoch.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.endTime
     */
    @ProtobufProperty(index = 5, type = ProtobufType.INT64)
    Long endTime;

    /**
     * Whether the subscription's billing platform changed since it was
     * created.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.isPlatformChanged
     */
    @ProtobufProperty(index = 6, type = ProtobufType.BOOL)
    Boolean isPlatformChanged;

    /**
     * The origin source string for this subscription (server-defined).
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.source
     */
    @ProtobufProperty(index = 7, type = ProtobufType.STRING)
    String source;

    /**
     * The creation timestamp of the subscription, in seconds since the
     * epoch.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.creationTime
     */
    @ProtobufProperty(index = 8, type = ProtobufType.INT64)
    Long creationTime;

    /**
     * Constructs a new {@code SubscriptionInfo} from raw protobuf field
     * values.
     *
     * @param id                the subscription id
     * @param tier              the subscription tier
     * @param status            the subscription status
     * @param startTime         the billing window start timestamp
     * @param endTime           the billing window end timestamp
     * @param isPlatformChanged whether the platform changed
     * @param source            the origin source
     * @param creationTime      the creation timestamp
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo
     */
    SubscriptionInfo(String id, Integer tier, String status, Long startTime, Long endTime, Boolean isPlatformChanged, String source, Long creationTime) {
        this.id = id;
        this.tier = tier;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isPlatformChanged = isPlatformChanged;
        this.source = source;
        this.creationTime = creationTime;
    }

    /**
     * Returns the opaque subscription identifier, if present.
     *
     * @return an {@link Optional} containing the subscription id
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.id
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Returns the numeric tier of the subscription, if present.
     *
     * @return an {@link OptionalInt} containing the tier
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.tier
     */
    public OptionalInt tier() {
        return tier == null ? OptionalInt.empty() : OptionalInt.of(tier);
    }

    /**
     * Returns the current status string, if present.
     *
     * @return an {@link Optional} containing the status
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.status
     */
    public Optional<String> status() {
        return Optional.ofNullable(status);
    }

    /**
     * Returns the billing window start timestamp, in seconds, if present.
     *
     * @return an {@link OptionalLong} containing the start timestamp
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.startTime
     */
    public OptionalLong startTime() {
        return startTime == null ? OptionalLong.empty() : OptionalLong.of(startTime);
    }

    /**
     * Returns the billing window end (expiration) timestamp, in seconds,
     * if present.
     *
     * @return an {@link OptionalLong} containing the end timestamp
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.endTime
     */
    public OptionalLong endTime() {
        return endTime == null ? OptionalLong.empty() : OptionalLong.of(endTime);
    }

    /**
     * Returns whether the billing platform changed since creation.
     *
     * <p>Returns {@code false} if the field was unset on the wire.
     *
     * @return {@code true} if the platform changed
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.isPlatformChanged
     */
    public boolean isPlatformChanged() {
        return isPlatformChanged != null && isPlatformChanged;
    }

    /**
     * Returns the origin source string, if present.
     *
     * @return an {@link Optional} containing the source
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.source
     */
    public Optional<String> source() {
        return Optional.ofNullable(source);
    }

    /**
     * Returns the creation timestamp, in seconds, if present.
     *
     * @return an {@link OptionalLong} containing the creation timestamp
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.creationTime
     */
    public OptionalLong creationTime() {
        return creationTime == null ? OptionalLong.empty() : OptionalLong.of(creationTime);
    }

    /**
     * Sets the opaque subscription identifier.
     *
     * @param id the subscription id
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.id
     */
    public SubscriptionInfo setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the numeric tier of the subscription.
     *
     * @param tier the tier
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.tier
     */
    public SubscriptionInfo setTier(Integer tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Sets the current status string.
     *
     * @param status the status
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.status
     */
    public SubscriptionInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the billing window start timestamp.
     *
     * @param startTime the start timestamp in seconds
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.startTime
     */
    public SubscriptionInfo setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Sets the billing window end (expiration) timestamp.
     *
     * @param endTime the end timestamp in seconds
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.endTime
     */
    public SubscriptionInfo setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Sets the platform-changed flag.
     *
     * @param isPlatformChanged the platform-changed flag
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.isPlatformChanged
     */
    public SubscriptionInfo setPlatformChanged(Boolean isPlatformChanged) {
        this.isPlatformChanged = isPlatformChanged;
        return this;
    }

    /**
     * Sets the origin source string.
     *
     * @param source the source
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.source
     */
    public SubscriptionInfo setSource(String source) {
        this.source = source;
        return this;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param creationTime the creation timestamp in seconds
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action$SubscriptionInfo.creationTime
     */
    public SubscriptionInfo setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
        return this;
    }
}
