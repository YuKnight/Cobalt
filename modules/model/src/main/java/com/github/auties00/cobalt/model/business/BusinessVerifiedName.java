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
 * Represents the locally-stored verified business name record for a WhatsApp Business
 * contact.
 *
 * <p>WhatsApp maintains a per-contact record of verified business information that is
 * persisted in the "verified-business-name" table. This record is populated from
 * the {@code verified_name} stanza received during USync (contact synchronization)
 * and from message acknowledgments containing business identity data.
 *
 * <p>Each record is uniquely identified by its {@link #jid()} and contains:
 * <ul>
 *   <li>The {@link #name() verified business name} as approved by WhatsApp.
 *   <li>A {@link #level() verification level} (numeric) indicating how thoroughly
 *       the business has been verified.
 *   <li>A {@link #serial() certificate serial number} used to detect certificate changes.
 *   <li>Flags indicating whether the business uses the WhatsApp Business API
 *       ({@link #isApi()}) or the WhatsApp Business App for small businesses
 *       ({@link #isSmb()}).
 *   <li>A privacy mode configuration ({@link #hostStorage()}, {@link #actualActors()},
 *       {@link #privacyModeTimestamp()}) that describes who processes messages on
 *       behalf of the business and where business data is hosted.
 * </ul>
 *
 * <p>Two instances are considered equal if and only if they share the same {@link #jid()}.
 *
 * @see BusinessVerifiedNameCertificate
 * @see BusinessIdentityInfo
 */
@ProtobufMessage
public final class BusinessVerifiedName {
    /**
     * The JID (Jabber ID) that uniquely identifies this verified business name record.
     *
     * <p>This is the primary key for the record. For LID-migrated accounts, this
     * may be a LID-based JID; otherwise it is the user portion of the phone-number
     * based JID. This field is required and must not be {@code null}.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final Jid jid;

    /**
     * The verified business name as approved by WhatsApp.
     *
     * <p>This is the primary display name extracted from the business's
     * verified-name certificate. It appears in the chat header and contact
     * info for verified business contacts.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    String name;

    /**
     * The numeric verification level for this business, corresponding to the
     * {@code verified_level} attribute received from the server.
     *
     * <p>Common values are 0 (unknown), 1 (low), and 2 (high). The level
     * determines whether WhatsApp displays a verification badge for the
     * business.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.INT32)
    int level;

    /**
     * The serial number of the business's verified-name certificate.
     *
     * <p>This value is used to detect when a business's certificate has been
     * updated. A change in serial number triggers re-validation of the
     * business identity.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.INT64)
    long serial;

    /**
     * Whether this business uses the WhatsApp Business API (Cloud API or
     * On-Premises API) rather than the WhatsApp Business App.
     *
     * <p>Determined from the certificate issuer: {@code true} if the issuer
     * is {@code "ent:wa"} (enterprise), {@code false} otherwise.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.BOOL)
    boolean isApi;

    /**
     * Whether this business uses the WhatsApp Business App (small and medium
     * business tier) rather than the WhatsApp Business API.
     *
     * <p>Determined from the certificate issuer: {@code true} if the issuer
     * is {@code "smb:wa"} (small/medium business), {@code false} otherwise.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.BOOL)
    boolean isSmb;

    /**
     * The type of infrastructure hosting the business data, or {@code null}
     * if not specified.
     *
     * <p>Together with {@link #actualActors} and {@link #privacyModeTimestampSeconds},
     * this field forms the privacy mode triplet used to determine the business's
     * messaging privacy level (E2EE, BSP-mediated, Meta-hosted, or CAPI).
     *
     * @see HostStorageType
     */
    @ProtobufProperty(index = 7, type = ProtobufType.ENUM)
    HostStorageType hostStorage;

    /**
     * The entity that actually processes messages on behalf of the business,
     * or {@code null} if not specified.
     *
     * <p>Together with {@link #hostStorage} and {@link #privacyModeTimestampSeconds},
     * this field forms the privacy mode triplet.
     *
     * @see ActualActorsType
     */
    @ProtobufProperty(index = 8, type = ProtobufType.ENUM)
    ActualActorsType actualActors;

    /**
     * The timestamp (in epoch seconds) at which the privacy mode configuration
     * was last changed, or {@code null} if no privacy mode is configured.
     *
     * <p>When all three privacy mode fields ({@link #hostStorage},
     * {@link #actualActors}, and this timestamp) are non-null, the record
     * has a complete privacy mode that can be resolved to a reduced privacy
     * level (E2EE, BSP, FB, or HOSTED_GROUP).
     */
    @ProtobufProperty(index = 9, type = ProtobufType.INT64)
    Long privacyModeTimestampSeconds;

    /**
     * Constructs a new {@code BusinessVerifiedName}.
     *
     * @param jid                         the JID identifying this record; must not be {@code null}
     * @param name                        the verified business name, or {@code null} if absent
     * @param level                       the numeric verification level
     * @param serial                      the certificate serial number
     * @param isApi                       {@code true} if the business uses the Business API
     * @param isSmb                       {@code true} if the business uses the Business App (SMB)
     * @param hostStorage                 the hosting infrastructure type, or {@code null} if absent
     * @param actualActors                the message-processing entity, or {@code null} if absent
     * @param privacyModeTimestampSeconds the privacy mode timestamp in epoch seconds,
     *                                    or {@code null} if absent
     * @throws NullPointerException if {@code jid} is {@code null}
     */
    BusinessVerifiedName(Jid jid, String name, int level, long serial, boolean isApi, boolean isSmb,
                         HostStorageType hostStorage, ActualActorsType actualActors,
                         Long privacyModeTimestampSeconds) {
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
     * Returns the JID that uniquely identifies this verified business name record.
     *
     * @return the JID, never {@code null}
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Returns the verified business name as approved by WhatsApp.
     *
     * @return an {@code Optional} containing the business name,
     *         or empty if no name is available
     */
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * Returns the numeric verification level for this business.
     *
     * <p>Common values: 0 = unknown, 1 = low, 2 = high.
     *
     * @return the verification level
     */
    public int level() {
        return level;
    }

    /**
     * Returns the serial number of the business's verified-name certificate.
     *
     * @return the certificate serial number
     */
    public long serial() {
        return serial;
    }

    /**
     * Returns whether this business uses the WhatsApp Business API.
     *
     * @return {@code true} if the business uses the API (enterprise tier),
     *         {@code false} otherwise
     */
    public boolean isApi() {
        return isApi;
    }

    /**
     * Returns whether this business uses the WhatsApp Business App (SMB tier).
     *
     * @return {@code true} if the business uses the Business App (small/medium
     *         business tier), {@code false} otherwise
     */
    public boolean isSmb() {
        return isSmb;
    }

    /**
     * Returns the type of infrastructure hosting the business data.
     *
     * @return an {@code Optional} containing the {@link HostStorageType},
     *         or empty if not configured
     */
    public Optional<HostStorageType> hostStorage() {
        return Optional.ofNullable(hostStorage);
    }

    /**
     * Returns the entity that actually processes messages on behalf of the business.
     *
     * @return an {@code Optional} containing the {@link ActualActorsType},
     *         or empty if not configured
     */
    public Optional<ActualActorsType> actualActors() {
        return Optional.ofNullable(actualActors);
    }

    /**
     * Returns the timestamp at which the privacy mode was last changed, as an
     * {@link Instant} derived from the stored epoch seconds.
     *
     * @return an {@code Optional} containing the privacy mode timestamp,
     *         or empty if no privacy mode is configured
     */
    public Optional<Instant> privacyModeTimestamp() {
        return Optional.ofNullable(privacyModeTimestampSeconds)
                .map(Instant::ofEpochSecond);
    }

    /**
     * Returns whether this record has a complete privacy mode configuration.
     *
     * <p>A complete privacy mode requires all three triplet fields to be present:
     * {@link #hostStorage()}, {@link #actualActors()}, and
     * {@link #privacyModeTimestamp() privacyModeTimestampSeconds}. When all three
     * are present, the privacy mode can be resolved to a reduced level (E2EE, BSP,
     * FB, or HOSTED_GROUP).
     *
     * @return {@code true} if all three privacy mode fields are non-null,
     *         {@code false} otherwise
     */
    public boolean hasPrivacyMode() {
        return hostStorage != null && actualActors != null && privacyModeTimestampSeconds != null;
    }

    /**
     * Compares this record to another object for equality based solely on
     * the {@link #jid()}.
     *
     * @param o the object to compare
     * @return {@code true} if {@code o} is a {@code BusinessVerifiedName} with the
     *         same JID, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof BusinessVerifiedName that && Objects.equals(jid, that.jid);
    }

    /**
     * Returns a hash code derived solely from the {@link #jid()}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(jid);
    }

    /**
     * Returns a string representation of this verified business name record,
     * including the JID, name, level, API/SMB flags, hosting, actors, and
     * privacy mode timestamp.
     *
     * @return a human-readable string representation
     */
    @Override
    public String toString() {
        return "VerifiedBusinessName[jid=" + jid + ", name=" + name + ", level=" + level +
               ", isApi=" + isApi + ", hostStorage=" + hostStorage +
               ", actualActors=" + actualActors + ", privacyModeTs=" + privacyModeTimestampSeconds + ']';
    }

    /**
     * Enumerates the types of infrastructure that can host a WhatsApp Business
     * account's data, as stored in the verified-business-name record.
     *
     * <p>This enum is part of the privacy mode triplet. Together with
     * {@link ActualActorsType} and the privacy mode timestamp, it determines
     * the reduced privacy mode (E2EE, BSP, FB, or HOSTED_GROUP).
     */
    @ProtobufEnum
    public enum HostStorageType {
        /**
         * Data is hosted on the business's own infrastructure (on-premises
         * deployment of the WhatsApp Business API).
         */
        ON_PREMISE(1),

        /**
         * Data is hosted on Meta (Facebook) infrastructure (Cloud API
         * deployment).
         */
        FACEBOOK(2);

        /**
         * The protobuf wire index for this enum constant.
         */
        final int index;

        /**
         * Constructs a {@code HostStorageType} enum constant with the given
         * protobuf index.
         *
         * @param index the protobuf wire index
         */
        HostStorageType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        /**
         * Returns the protobuf wire index of this enum constant.
         *
         * @return the protobuf index
         */
        public int index() {
            return index;
        }
    }

    /**
     * Identifies the entity that actually processes messages on behalf of a
     * WhatsApp Business account, as stored in the verified-business-name record.
     *
     * <p>This enum is part of the privacy mode triplet. Together with
     * {@link HostStorageType} and the privacy mode timestamp, it determines
     * the reduced privacy mode:
     * <ul>
     *   <li>{@link #SELF} with on-premise hosting resolves to E2EE.
     *   <li>{@link #BSP} resolves to BSP-mediated messaging.
     *   <li>{@link #CAPI} resolves to Cloud API (HOSTED_GROUP) messaging.
     * </ul>
     */
    @ProtobufEnum
    public enum ActualActorsType {
        /**
         * The business itself processes messages directly, maintaining end-to-end
         * encryption between the user and the business.
         */
        SELF(1),

        /**
         * A Business Solution Provider (BSP) processes messages on behalf of the
         * business, meaning a third party has access to message content.
         */
        BSP(2),

        /**
         * Cloud API (CAPI) processes messages, indicating the business uses
         * Meta-hosted infrastructure for message handling.
         */
        CAPI(3);

        /**
         * The protobuf wire index for this enum constant.
         */
        final int index;

        /**
         * Constructs an {@code ActualActorsType} enum constant with the given
         * protobuf index.
         *
         * @param index the protobuf wire index
         */
        ActualActorsType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        /**
         * Returns the protobuf wire index of this enum constant.
         *
         * @return the protobuf index
         */
        public int index() {
            return index;
        }
    }
}
