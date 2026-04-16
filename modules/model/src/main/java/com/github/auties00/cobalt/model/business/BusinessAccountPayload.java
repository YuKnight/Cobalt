package com.github.auties00.cobalt.model.business;

import com.github.auties00.cobalt.model.mixin.InstantSecondsMixin;
import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Bundles a {@link BusinessVerifiedNameCertificate} together with the serialized
 * account-link information for a WhatsApp Business account.
 *
 * <p>During business account provisioning, WhatsApp transmits both the
 * verified-name certificate and the raw bytes of an {@link AccountLinkInfo}
 * inside a single protobuf envelope. The certificate attests to the business's
 * verified name, while the account-link info describes the association between
 * a WhatsApp phone number and a Facebook Business ID.
 *
 * <p>Callers typically deserialize the {@link #accountLinkInfo()} bytes into an
 * {@link AccountLinkInfo} message to inspect the linked Facebook Business ID,
 * phone number, and hosting configuration.
 *
 * @see BusinessVerifiedNameCertificate
 * @see AccountLinkInfo
 */
@ProtobufMessage(name = "BizAccountPayload")
public final class BusinessAccountPayload {
    /**
     * The verified-name certificate for this business account, containing the
     * business's attested name, client signature, and server signature.
     *
     * <p>May be absent if the payload was constructed without certificate data.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    BusinessVerifiedNameCertificate verifiedNameCertificate;

    /**
     * The serialized protobuf bytes of the {@link AccountLinkInfo} message that
     * links this WhatsApp account to its corresponding Facebook Business account.
     *
     * <p>Deserialize these bytes into an {@link AccountLinkInfo} to access the
     * Facebook Business ID, phone number, issue time, and hosting configuration.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    byte[] accountLinkInfo;

    /**
     * Constructs a new {@code BusinessAccountPayload}.
     *
     * @param verifiedNameCertificate the verified-name certificate, or {@code null} if absent
     * @param accountLinkInfo         the serialized {@link AccountLinkInfo} bytes,
     *                                or {@code null} if absent
     */
    BusinessAccountPayload(BusinessVerifiedNameCertificate verifiedNameCertificate, byte[] accountLinkInfo) {
        this.verifiedNameCertificate = verifiedNameCertificate;
        this.accountLinkInfo = accountLinkInfo;
    }

    /**
     * Returns the verified-name certificate for this business account.
     *
     * @return an {@code Optional} containing the {@link BusinessVerifiedNameCertificate},
     *         or empty if no certificate is present
     */
    public Optional<BusinessVerifiedNameCertificate> verifiedNameCertificate() {
        return Optional.ofNullable(verifiedNameCertificate);
    }

    /**
     * Returns the serialized {@link AccountLinkInfo} bytes.
     *
     * @return an {@code Optional} containing the raw protobuf bytes,
     *         or empty if no account-link info is present
     */
    public Optional<byte[]> accountLinkInfo() {
        return Optional.ofNullable(accountLinkInfo);
    }

    /**
     * Sets the verified-name certificate for this business account.
     *
     * @param verifiedNameCertificate the certificate to set, or {@code null} to clear
     */
    public void setVerifiedNameCertificate(BusinessVerifiedNameCertificate verifiedNameCertificate) {
        this.verifiedNameCertificate = verifiedNameCertificate;
    }

    /**
     * Sets the serialized {@link AccountLinkInfo} bytes.
     *
     * @param accountLinkInfo the raw protobuf bytes to set, or {@code null} to clear
     */
    public void setAccountLinkInfo(byte[] accountLinkInfo) {
        this.accountLinkInfo = accountLinkInfo;
    }

    /**
     * Describes the association between a WhatsApp phone number and a Facebook
     * Business account.
     *
     * <p>This message records the Facebook Business ID (FBID) that owns the
     * WhatsApp Business account, the phone number linked to it, the timestamp
     * when the link was established, and the infrastructure and account type
     * configuration. These fields are used during business verification and
     * provisioning to confirm that a WhatsApp number is legitimately operated
     * by a specific Facebook Business entity.
     *
     * @see BusinessAccountPayload
     */
    @ProtobufMessage(name = "BizAccountLinkInfo")
    public static final class AccountLinkInfo {
        /**
         * The Facebook Business ID (FBID) of the linked business account.
         *
         * <p>This is the unique numeric identifier assigned by Meta to the
         * business entity, for example {@code 123456789012345}. It establishes
         * the ownership relationship between the Facebook Business Manager
         * and this WhatsApp Business account.
         */
        @ProtobufProperty(index = 1, type = ProtobufType.UINT64)
        Long facebookBusinessId;

        /**
         * The WhatsApp phone number associated with this business account,
         * in E.164-like format without the leading plus sign (for example
         * {@code "15551234567"}).
         */
        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        String phoneNumber;

        /**
         * The timestamp at which the account link was established, represented
         * as an {@link Instant} converted from epoch seconds via
         * {@link InstantSecondsMixin}.
         */
        @ProtobufProperty(index = 3, type = ProtobufType.UINT64, mixins = InstantSecondsMixin.class)
        Instant issueTime;

        /**
         * The type of infrastructure hosting the business data, indicating
         * whether the data resides on the business's own servers or on
         * Meta's infrastructure.
         */
        @ProtobufProperty(index = 4, type = ProtobufType.ENUM)
        HostStorageType hostStorage;

        /**
         * The type of business account, indicating the tier of the WhatsApp
         * Business product being used (for example, enterprise API accounts).
         */
        @ProtobufProperty(index = 5, type = ProtobufType.ENUM)
        AccountType accountType;

        /**
         * Constructs a new {@code AccountLinkInfo}.
         *
         * @param facebookBusinessId the Facebook Business ID, or {@code null} if absent
         * @param phoneNumber        the WhatsApp phone number, or {@code null} if absent
         * @param issueTime          the time the link was established, or {@code null} if absent
         * @param hostStorage        the hosting infrastructure type, or {@code null} if absent
         * @param accountType        the business account type, or {@code null} if absent
         */
        AccountLinkInfo(Long facebookBusinessId, String phoneNumber, Instant issueTime, HostStorageType hostStorage, AccountType accountType) {
            this.facebookBusinessId = facebookBusinessId;
            this.phoneNumber = phoneNumber;
            this.issueTime = issueTime;
            this.hostStorage = hostStorage;
            this.accountType = accountType;
        }

        /**
         * Returns the Facebook Business ID (FBID) of the linked business account.
         *
         * @return the FBID as an {@code OptionalLong}, or empty if not set
         */
        public OptionalLong facebookBusinessId() {
            return facebookBusinessId == null ? OptionalLong.empty() : OptionalLong.of(facebookBusinessId);
        }

        /**
         * Returns the WhatsApp phone number associated with this business account.
         *
         * @return an {@code Optional} containing the phone number string
         *         (for example {@code "15551234567"}), or empty if not set
         */
        public Optional<String> phoneNumber() {
            return Optional.ofNullable(phoneNumber);
        }

        /**
         * Returns the timestamp at which the account link was established.
         *
         * @return an {@code Optional} containing the issue time as an {@link Instant},
         *         or empty if not set
         */
        public Optional<Instant> issueTime() {
            return Optional.ofNullable(issueTime);
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
         * Returns the type of business account.
         *
         * @return an {@code Optional} containing the {@link AccountType},
         *         or empty if not set
         */
        public Optional<AccountType> accountType() {
            return Optional.ofNullable(accountType);
        }

        /**
         * Sets the Facebook Business ID.
         *
         * @param facebookBusinessId the FBID to set, or {@code null} to clear
         */
        public void setFacebookBusinessId(Long facebookBusinessId) {
            this.facebookBusinessId = facebookBusinessId;
    }

        /**
         * Sets the WhatsApp phone number.
         *
         * @param phoneNumber the phone number to set, or {@code null} to clear
         */
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
    }

        /**
         * Sets the timestamp at which the account link was established.
         *
         * @param issueTime the issue time to set, or {@code null} to clear
         */
        public void setsueTime(Instant issueTime) {
            this.issueTime = issueTime;
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
         * Sets the business account type.
         *
         * @param accountType the {@link AccountType} to set, or {@code null} to clear
         */
        public void setAccountType(AccountType accountType) {
            this.accountType = accountType;
    }

        /**
         * Enumerates the tiers of WhatsApp Business accounts used in account
         * link information.
         *
         * <p>Currently, only the {@link #ENTERPRISE} tier is defined, which
         * corresponds to businesses using the WhatsApp Business API (Cloud API
         * or On-Premises API).
         */
        @ProtobufEnum(name = "BizAccountLinkInfo.AccountType")
        public enum AccountType {
            /**
             * An enterprise-tier WhatsApp Business account, typically using the
             * WhatsApp Business API (Cloud API or On-Premises API) for
             * programmatic messaging at scale.
             */
            ENTERPRISE(0);

            /**
             * Constructs an {@code AccountType} enum constant with the given
             * protobuf index.
             *
             * @param index the protobuf wire index
             */
            AccountType(@ProtobufEnumIndex int index) {
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
         * account's data.
         *
         * <p>This determines where message data and business information are stored
         * and processed:
         * <ul>
         *   <li>{@link #ON_PREMISE} indicates that data is hosted on the business's
         *       own infrastructure (on-premises deployment of the WhatsApp Business API).
         *   <li>{@link #FACEBOOK} indicates that data is hosted on Meta (Facebook)
         *       infrastructure (Cloud API deployment).
         * </ul>
         */
        @ProtobufEnum(name = "BizAccountLinkInfo.HostStorageType")
        public enum HostStorageType {
            /**
             * Data is hosted on the business's own infrastructure (on-premises
             * deployment).
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
    }
}
