package com.github.auties00.cobalt.model.business;

import com.github.auties00.cobalt.model.mixin.InstantSecondsMixin;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Represents the identity and verification metadata for a WhatsApp Business account.
 *
 * <p>WhatsApp assigns each business account a verification identity that describes
 * how thoroughly the business has been verified ({@link VerificationLevel}), whether
 * the identity has been cryptographically {@link #signed() signed} by the business,
 * and whether it has been {@link #revoked() revoked} by WhatsApp.
 *
 * <p>This message also carries the business's messaging privacy configuration,
 * which is composed of three related fields that form the "privacy mode triplet":
 * <ul>
 *   <li>{@link #actualActors()} indicates who actually processes messages on
 *       behalf of the business (the business itself, or a third-party BSP).
 *   <li>{@link #hostStorage()} indicates where the business data is hosted
 *       (on-premises or on Meta infrastructure).
 *   <li>{@link #privacyModeTimestamp()} records when the privacy configuration
 *       was last changed.
 * </ul>
 *
 * <p>Together, these fields determine the messaging privacy level: end-to-end
 * encrypted (E2EE), BSP-mediated, Meta-hosted, or Cloud API (CAPI). WhatsApp
 * displays privacy-mode system messages in the chat when this configuration
 * changes.
 *
 * @see BusinessVerifiedNameCertificate
 * @see VerificationLevel
 * @see ActualActorsType
 * @see HostStorageType
 */
@ProtobufMessage(name = "BizIdentityInfo")
public final class BusinessIdentityInfo {
    /**
     * The verification level of this business identity, indicating how
     * thoroughly WhatsApp has verified the business's real-world identity.
     *
     * @see VerificationLevel
     */
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    VerificationLevel verificationLevel;

    /**
     * The verified-name certificate associated with this business identity,
     * containing the attested business name, signatures, and certificate
     * details.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    BusinessVerifiedNameCertificate verifiedNameCertificate;

    /**
     * Whether this identity information has been cryptographically signed
     * by the business.
     *
     * <p>A signed identity indicates that the business has acknowledged and
     * confirmed its identity information. When absent, the value defaults
     * to {@code false}.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.BOOL)
    Boolean signed;

    /**
     * Whether this business identity has been revoked by WhatsApp.
     *
     * <p>A revoked identity means WhatsApp has withdrawn the business's
     * verification status, typically due to policy violations or identity
     * disputes. When absent, the value defaults to {@code false}.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.BOOL)
    Boolean revoked;

    /**
     * The type of infrastructure hosting the business data.
     *
     * <p>This is part of the privacy mode triplet (together with
     * {@link #actualActors} and {@link #privacyModeTimestamp}) that
     * determines the messaging privacy level for this business.
     *
     * @see HostStorageType
     */
    @ProtobufProperty(index = 5, type = ProtobufType.ENUM)
    HostStorageType hostStorage;

    /**
     * The entity that actually processes messages on behalf of the business.
     *
     * <p>This is part of the privacy mode triplet (together with
     * {@link #hostStorage} and {@link #privacyModeTimestamp}) that determines
     * the messaging privacy level for this business.
     *
     * @see ActualActorsType
     */
    @ProtobufProperty(index = 6, type = ProtobufType.ENUM)
    ActualActorsType actualActors;

    /**
     * The timestamp at which the privacy mode configuration was last changed,
     * represented as an {@link Instant} converted from epoch seconds via
     * {@link InstantSecondsMixin}.
     *
     * <p>This is part of the privacy mode triplet (together with
     * {@link #actualActors} and {@link #hostStorage}). When a newer privacy
     * mode timestamp is received from the server, WhatsApp generates a system
     * message in the chat to notify users of the change.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.UINT64, mixins = InstantSecondsMixin.class)
    Instant privacyModeTimestamp;

    /**
     * A bitmask of feature control flags for this business identity.
     *
     * <p>These flags control various business-specific features and capabilities
     * that are enabled or disabled for this account. The specific bit positions
     * and their meanings are defined server-side.
     */
    @ProtobufProperty(index = 8, type = ProtobufType.UINT64)
    Long featureControls;

    /**
     * Constructs a new {@code BusinessIdentityInfo}.
     *
     * @param verificationLevel      the verification level, or {@code null} if absent
     * @param verifiedNameCertificate the verified-name certificate, or {@code null} if absent
     * @param signed                 whether the identity is signed, or {@code null} if absent
     * @param revoked                whether the identity is revoked, or {@code null} if absent
     * @param hostStorage            the hosting infrastructure type, or {@code null} if absent
     * @param actualActors           the entity processing messages, or {@code null} if absent
     * @param privacyModeTimestamp   the privacy mode change timestamp, or {@code null} if absent
     * @param featureControls        the feature control bitmask, or {@code null} if absent
     */
    BusinessIdentityInfo(VerificationLevel verificationLevel, BusinessVerifiedNameCertificate verifiedNameCertificate, Boolean signed, Boolean revoked, HostStorageType hostStorage, ActualActorsType actualActors, Instant privacyModeTimestamp, Long featureControls) {
        this.verificationLevel = verificationLevel;
        this.verifiedNameCertificate = verifiedNameCertificate;
        this.signed = signed;
        this.revoked = revoked;
        this.hostStorage = hostStorage;
        this.actualActors = actualActors;
        this.privacyModeTimestamp = privacyModeTimestamp;
        this.featureControls = featureControls;
    }

    /**
     * Returns the verification level of this business identity.
     *
     * @return an {@code Optional} containing the {@link VerificationLevel},
     *         or empty if no level has been assigned
     */
    public Optional<VerificationLevel> verificationLevel() {
        return Optional.ofNullable(verificationLevel);
    }

    /**
     * Returns the verified-name certificate associated with this identity.
     *
     * @return an {@code Optional} containing the {@link BusinessVerifiedNameCertificate},
     *         or empty if no certificate is present
     */
    public Optional<BusinessVerifiedNameCertificate> verifiedNameCertificate() {
        return Optional.ofNullable(verifiedNameCertificate);
    }

    /**
     * Returns whether this identity information has been cryptographically signed
     * by the business.
     *
     * @return {@code true} if the identity is signed, {@code false} if the field
     *         is absent or explicitly set to {@code false}
     */
    public boolean signed() {
        return signed != null && signed;
    }

    /**
     * Returns whether this business identity has been revoked by WhatsApp.
     *
     * @return {@code true} if the identity is revoked, {@code false} if the field
     *         is absent or explicitly set to {@code false}
     */
    public boolean revoked() {
        return revoked != null && revoked;
    }

    /**
     * Returns the type of infrastructure hosting the business data.
     *
     * @return an {@code Optional} containing the {@link HostStorageType},
     *         or empty if not set
     */
    public Optional<HostStorageType> hostStorage() {
        return Optional.ofNullable(hostStorage);
    }

    /**
     * Returns the entity that actually processes messages on behalf of the business.
     *
     * @return an {@code Optional} containing the {@link ActualActorsType},
     *         or empty if not set
     */
    public Optional<ActualActorsType> actualActors() {
        return Optional.ofNullable(actualActors);
    }

    /**
     * Returns the timestamp at which the privacy mode configuration was last changed.
     *
     * @return an {@code Optional} containing the privacy mode timestamp as an
     *         {@link Instant}, or empty if not set
     */
    public Optional<Instant> privacyModeTimestamp() {
        return Optional.ofNullable(privacyModeTimestamp);
    }

    /**
     * Returns the feature control bitmask for this business identity.
     *
     * @return an {@code OptionalLong} containing the bitmask value,
     *         or empty if not set
     */
    public OptionalLong featureControls() {
        return featureControls == null ? OptionalLong.empty() : OptionalLong.of(featureControls);
    }

    /**
     * Sets the verification level of this business identity.
     *
     * @param verificationLevel the {@link VerificationLevel} to set, or {@code null} to clear
     */
    public void setVerificationLevel(VerificationLevel verificationLevel) {
        this.verificationLevel = verificationLevel;
    }

    /**
     * Sets the verified-name certificate for this identity.
     *
     * @param verifiedNameCertificate the {@link BusinessVerifiedNameCertificate} to set,
     *                                or {@code null} to clear
     */
    public void setVerifiedNameCertificate(BusinessVerifiedNameCertificate verifiedNameCertificate) {
        this.verifiedNameCertificate = verifiedNameCertificate;
    }

    /**
     * Sets whether this identity is cryptographically signed.
     *
     * @param signed {@code true} if signed, {@code false} or {@code null} otherwise
     */
    public void setSigned(Boolean signed) {
        this.signed = signed;
    }

    /**
     * Sets whether this identity has been revoked.
     *
     * @param revoked {@code true} if revoked, {@code false} or {@code null} otherwise
     */
    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    /**
     * Sets the hosting infrastructure type.
     *
     * @param hostStorage the {@link HostStorageType} to set, or {@code null} to clear
     */
    public void setHostStorage(HostStorageType hostStorage) {
        this.hostStorage = hostStorage;
    }

    /**
     * Sets the entity that processes messages on behalf of the business.
     *
     * @param actualActors the {@link ActualActorsType} to set, or {@code null} to clear
     */
    public void setActualActors(ActualActorsType actualActors) {
        this.actualActors = actualActors;
    }

    /**
     * Sets the timestamp at which the privacy mode configuration was last changed.
     *
     * @param privacyModeTimestamp the timestamp to set, or {@code null} to clear
     */
    public void setPrivacyModeTimestamp(Instant privacyModeTimestamp) {
        this.privacyModeTimestamp = privacyModeTimestamp;
    }

    /**
     * Sets the feature control bitmask.
     *
     * @param featureControls the bitmask value to set, or {@code null} to clear
     */
    public void setFeatureControls(Long featureControls) {
        this.featureControls = featureControls;
    }

    /**
     * Identifies the entity that actually processes messages on behalf of a
     * WhatsApp Business account.
     *
     * <p>This field is part of the privacy mode triplet and directly affects the
     * messaging privacy level displayed to users:
     * <ul>
     *   <li>{@link #SELF} means the business itself handles messages, and
     *       end-to-end encryption (E2EE) is maintained between the user and
     *       the business.
     *   <li>{@link #BSP} means a Business Solution Provider handles messages
     *       on behalf of the business, which means the BSP has access to
     *       message content.
     * </ul>
     */
    @ProtobufEnum(name = "BizIdentityInfo.ActualActorsType")
    public enum ActualActorsType {
        /**
         * The business itself processes messages directly, maintaining end-to-end
         * encryption between the user and the business.
         */
        SELF(0),

        /**
         * A Business Solution Provider (BSP) processes messages on behalf of the
         * business, meaning a third party has access to message content.
         */
        BSP(1);

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
         * The protobuf wire index for this enum constant.
         */
        final int index;

        /**
         * Returns the protobuf wire index of this enum constant.
         *
         * @return the protobuf index
         */
        public int index() {
            return this.index;
        }
    }

    /**
     * Enumerates the types of infrastructure that can host a WhatsApp Business
     * account's data within a {@link BusinessIdentityInfo}.
     *
     * <p>This field is part of the privacy mode triplet and affects the messaging
     * privacy level:
     * <ul>
     *   <li>{@link #ON_PREMISE} means the business operates its own WhatsApp
     *       Business API infrastructure.
     *   <li>{@link #FACEBOOK} means the business uses Meta-hosted Cloud API
     *       infrastructure, with data stored on Meta servers.
     * </ul>
     */
    @ProtobufEnum(name = "BizIdentityInfo.HostStorageType")
    public enum HostStorageType {
        /**
         * Data is hosted on the business's own infrastructure (on-premises
         * deployment of the WhatsApp Business API).
         */
        ON_PREMISE(0),

        /**
         * Data is hosted on Meta (Facebook) infrastructure (Cloud API
         * deployment).
         */
        FACEBOOK(1);

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
         * The protobuf wire index for this enum constant.
         */
        final int index;

        /**
         * Returns the protobuf wire index of this enum constant.
         *
         * @return the protobuf index
         */
        public int index() {
            return this.index;
        }
    }

    /**
     * Represents the level of identity verification that WhatsApp has performed
     * on a business account.
     *
     * <p>Higher verification levels indicate stronger real-world identity checks.
     * WhatsApp displays a verification badge in the chat header and contact info
     * for businesses with {@link #HIGH} verification. The verification level is
     * received from the server in the {@code verified_level} attribute of
     * business contact information.
     */
    @ProtobufEnum(name = "BizIdentityInfo.VerifiedLevelValue")
    public enum VerificationLevel {
        /**
         * The verification level is unknown or has not been determined by WhatsApp.
         */
        UNKNOWN(0),

        /**
         * A low level of verification, indicating basic identity checks have been
         * performed (for example, phone number verification).
         */
        LOW(1),

        /**
         * A high level of verification, indicating thorough identity validation
         * has been performed. Businesses with this level display a green
         * verification badge.
         */
        HIGH(2);

        /**
         * Constructs a {@code VerificationLevel} enum constant with the given
         * protobuf index.
         *
         * @param index the protobuf wire index
         */
        VerificationLevel(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        /**
         * The protobuf wire index for this enum constant.
         */
        final int index;

        /**
         * Returns the protobuf wire index of this enum constant.
         *
         * @return the protobuf index
         */
        public int index() {
            return this.index;
        }
    }
}
