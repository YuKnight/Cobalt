package com.github.auties00.cobalt.model.business;

import com.github.auties00.cobalt.model.jid.Jid;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A verified business name record, associating a contact with its
 * verified business identity and privacy mode.
 *
 * <p>This record is populated from verified name syncs and biz profile
 * queries.  The privacy mode fields ({@link #hostStorage()},
 * {@link #actualActors()}, {@link #privacyModeTimestamp()}) are used
 * at send time to populate the {@code <biz>} stanza child node.
 *
 * @apiNote WAWebSchemaVerifiedBusinessName: the IndexedDB table that
 * stores these records.
 * WAWebApiVerifiedBusinessName.getPrivacyMode: queries the record and
 * converts the privacy mode fields via
 * WAWebHandleMsgTypes.flow.ActualActorsEnumType/HostStorageEnumType.
 * WAWebBizVerifiedNameAction.handleVerifiedNameSync: populates these
 * records from verified name sync payloads.
 */
@ProtobufMessage
public final class VerifiedBusinessName {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final Jid jid;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    String name;

    @ProtobufProperty(index = 3, type = ProtobufType.INT32)
    int level;

    @ProtobufProperty(index = 4, type = ProtobufType.INT64)
    long serial;

    @ProtobufProperty(index = 5, type = ProtobufType.BOOL)
    boolean isApi;

    @ProtobufProperty(index = 6, type = ProtobufType.BOOL)
    boolean isSmb;

    @ProtobufProperty(index = 7, type = ProtobufType.ENUM)
    HostStorageType hostStorage;

    @ProtobufProperty(index = 8, type = ProtobufType.ENUM)
    ActualActorsType actualActors;

    @ProtobufProperty(index = 9, type = ProtobufType.INT64)
    Long privacyModeTimestampSeconds;

    VerifiedBusinessName(Jid jid, String name, int level, long serial, boolean isApi, boolean isSmb, HostStorageType hostStorage, ActualActorsType actualActors, Long privacyModeTimestampSeconds) {
        this.jid = Objects.requireNonNull(jid, "jid");
        this.name = name;
        this.level = level;
        this.serial = serial;
        this.isApi = isApi;
        this.isSmb = isSmb;
        this.hostStorage = hostStorage;
        this.actualActors = actualActors;
        this.privacyModeTimestampSeconds = privacyModeTimestampSeconds;
    }

    /**
     * Returns the JID this record is associated with.
     *
     * @return the contact JID
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Returns the verified business name.
     *
     * @return the name, or empty if not set
     */
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * Returns the verification level.
     *
     * @return the level
     */
    public int level() {
        return level;
    }

    /**
     * Returns the certificate serial number.
     *
     * @return the serial
     */
    public long serial() {
        return serial;
    }

    /**
     * Returns whether this is a business API account.
     *
     * @return {@code true} if API-based
     */
    public boolean isApi() {
        return isApi;
    }

    /**
     * Returns whether this is a small/medium business account.
     *
     * @return {@code true} if SMB
     */
    public boolean isSmb() {
        return isSmb;
    }

    /**
     * Returns the host storage type.
     *
     * @return the host storage, or empty if not set
     *
     * @apiNote WAWebHandleMsgTypes.flow.HostStorageEnumType:
     * used in the {@code <biz host_storage="...">} stanza attribute.
     */
    public Optional<HostStorageType> hostStorage() {
        return Optional.ofNullable(hostStorage);
    }

    /**
     * Returns the actual actors type.
     *
     * @return the actual actors, or empty if not set
     *
     * @apiNote WAWebHandleMsgTypes.flow.ActualActorsEnumType:
     * used in the {@code <biz actual_actors="...">} stanza attribute.
     */
    public Optional<ActualActorsType> actualActors() {
        return Optional.ofNullable(actualActors);
    }

    /**
     * Returns the timestamp when the privacy mode was last updated.
     *
     * @return the timestamp, or empty if not set
     */
    public Optional<Instant> privacyModeTimestamp() {
        return Optional.ofNullable(privacyModeTimestampSeconds)
                .map(Instant::ofEpochSecond);
    }

    /**
     * Returns whether this record has a complete privacy mode.
     *
     * @return {@code true} if all three privacy mode fields are present
     *
     * @apiNote WAWebApiVerifiedBusinessName.convertPrivacyModeFromStorageType:
     * returns {@code null} if actualActors or hostStorage is null after cast.
     */
    public boolean hasPrivacyMode() {
        return hostStorage != null && actualActors != null && privacyModeTimestampSeconds != null;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VerifiedBusinessName that
                && Objects.equals(jid, that.jid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jid);
    }

    @Override
    public String toString() {
        return "VerifiedBusinessName[" +
                "jid=" + jid +
                ", name=" + name +
                ", level=" + level +
                ", isApi=" + isApi +
                ", hostStorage=" + hostStorage +
                ", actualActors=" + actualActors +
                ", privacyModeTs=" + privacyModeTimestampSeconds +
                ']';
    }

    /**
     * Where messages for this business contact are stored.
     *
     * <p>These are the stanza-level values used in the {@code <biz>}
     * node's {@code host_storage} attribute.
     *
     * @apiNote WAWebHandleMsgTypes.flow.HostStorageEnumType:
     * OnPremise=1, Facebook=2.
     */
    @ProtobufEnum
    public enum HostStorageType {
        /**
         * Messages stored on-premise by the business.
         */
        ON_PREMISE(1),

        /**
         * Messages stored on Facebook/Meta infrastructure.
         */
        FACEBOOK(2);

        final int index;

        HostStorageType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        /**
         * Returns the stanza-level integer value.
         *
         * @return the integer value
         */
        public int index() {
            return index;
        }
    }

    /**
     * Who can act on messages for this business contact.
     *
     * <p>These are the stanza-level values used in the {@code <biz>}
     * node's {@code actual_actors} attribute.
     *
     * @apiNote WAWebHandleMsgTypes.flow.ActualActorsEnumType:
     * Self=1, Bsp=2, Capi=3.
     */
    @ProtobufEnum
    public enum ActualActorsType {
        /**
         * Only the business itself can act on messages.
         */
        SELF(1),

        /**
         * A Business Solution Provider can act on messages.
         */
        BSP(2),

        /**
         * Conversational API actors can act on messages.
         */
        CAPI(3);

        final int index;

        ActualActorsType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        /**
         * Returns the stanza-level integer value.
         *
         * @return the integer value
         */
        public int index() {
            return index;
        }
    }
}
