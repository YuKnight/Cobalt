package com.github.auties00.cobalt.model.business;

import com.github.auties00.cobalt.model.mixin.InstantSecondsMixin;
import it.auties.protobuf.annotation.ProtobufDeserializer;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.annotation.ProtobufSerializer;
import it.auties.protobuf.model.ProtobufType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Represents a signed certificate that attests a business's verified name on WhatsApp.
 *
 * <p>WhatsApp issues verified-name certificates to businesses that have passed identity
 * verification. The certificate is composed of three parts:
 * <ul>
 *   <li>{@link #details()} - an opaque byte payload containing a serialized
 *       {@link Details} message with the business name, serial number, issuer,
 *       localized name variants, and issuance timestamp.
 *   <li>{@link #signature()} - a client-side cryptographic signature over the
 *       details payload.
 *   <li>{@link #serverSignature()} - a server-side signature produced by WhatsApp
 *       to authenticate the certificate.
 * </ul>
 *
 * <p>To inspect the certificate metadata, deserialize the {@link #details()} bytes
 * into a {@link Details} instance. The {@link Details} contains the
 * {@link Details#issuer() issuer} which distinguishes enterprise (API) businesses
 * from small/medium businesses (SMB).
 *
 * @see Details
 * @see BusinessIdentityInfo
 * @see BusinessAccountPayload
 */
@ProtobufMessage(name = "VerifiedNameCertificate")
public final class BusinessVerifiedNameCertificate {
    /**
     * The serialized {@link Details} payload of this certificate.
     *
     * <p>This byte array contains a protobuf-encoded {@link Details} message.
     * Deserialize it to access the business name, serial number, issuer, and
     * other certificate metadata.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.BYTES)
    byte[] details;

    /**
     * The client-side cryptographic signature over the {@link #details()} payload.
     *
     * <p>This signature is produced by the business client to prove authenticity
     * of the certificate details.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    byte[] signature;

    /**
     * The server-side cryptographic signature produced by WhatsApp over the
     * {@link #details()} payload.
     *
     * <p>This signature is used to verify that WhatsApp has authenticated and
     * approved the certificate.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    byte[] serverSignature;

    /**
     * Constructs a new {@code BusinessVerifiedNameCertificate}.
     *
     * @param details         the serialized {@link Details} payload, or {@code null} if absent
     * @param signature       the client-side signature bytes, or {@code null} if absent
     * @param serverSignature the server-side signature bytes, or {@code null} if absent
     */
    BusinessVerifiedNameCertificate(byte[] details, byte[] signature, byte[] serverSignature) {
        this.details = details;
        this.signature = signature;
        this.serverSignature = serverSignature;
    }

    /**
     * Returns the serialized {@link Details} payload of this certificate.
     *
     * @return an {@code Optional} containing the details bytes, or empty if
     *         no details are present
     */
    public Optional<byte[]> details() {
        return Optional.ofNullable(details);
    }

    /**
     * Returns the client-side cryptographic signature over the details payload.
     *
     * @return an {@code Optional} containing the signature bytes, or empty if
     *         no signature is present
     */
    public Optional<byte[]> signature() {
        return Optional.ofNullable(signature);
    }

    /**
     * Returns the server-side cryptographic signature produced by WhatsApp.
     *
     * @return an {@code Optional} containing the server signature bytes, or empty
     *         if no server signature is present
     */
    public Optional<byte[]> serverSignature() {
        return Optional.ofNullable(serverSignature);
    }

    /**
     * Sets the serialized {@link Details} payload.
     *
     * @param details the details bytes to set, or {@code null} to clear
     */
    public void setDetails(byte[] details) {
        this.details = details;
    }

    /**
     * Sets the client-side cryptographic signature.
     *
     * @param signature the signature bytes to set, or {@code null} to clear
     */
    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    /**
     * Sets the server-side cryptographic signature.
     *
     * @param serverSignature the server signature bytes to set, or {@code null} to clear
     */
    public void setServerSignature(byte[] serverSignature) {
        this.serverSignature = serverSignature;
    }

    /**
     * Contains the decoded metadata of a {@link BusinessVerifiedNameCertificate}.
     *
     * <p>The details record the business's verified name, certificate serial number,
     * the issuer (enterprise or SMB), optional localized name variants for
     * different locales, and the timestamp when the certificate was issued.
     *
     * <p>The {@link #issuer()} field distinguishes how the business is registered:
     * <ul>
     *   <li>{@link CertificateIssuer.Enterprise} ({@code "ent:wa"}) for businesses
     *       using the WhatsApp Business API (Cloud API or On-Premises API).
     *   <li>{@link CertificateIssuer.SmallBusiness} ({@code "smb:wa"}) for businesses
     *       using the WhatsApp Business App.
     * </ul>
     *
     * @see CertificateIssuer
     * @see LocalizedName
     */
    @ProtobufMessage(name = "VerifiedNameCertificate.Details")
    public static final class Details {
        /**
         * The serial number of this certificate, uniquely identifying it within
         * the issuer's domain.
         *
         * <p>A change in serial number between updates indicates that the business
         * has been issued a new certificate, which may trigger re-validation of
         * the business identity.
         */
        @ProtobufProperty(index = 1, type = ProtobufType.UINT64)
        Long serial;

        /**
         * The issuer of this certificate, identifying the type of WhatsApp Business
         * product that the business uses.
         *
         * <p>Enterprise issuers ({@code "ent:wa"}) correspond to businesses using
         * the WhatsApp Business API, while small-business issuers ({@code "smb:wa"})
         * correspond to businesses using the WhatsApp Business App.
         *
         * @see CertificateIssuer
         */
        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        CertificateIssuer issuer;

        /**
         * The primary verified business name as approved by WhatsApp.
         *
         * <p>This is the canonical name displayed in the chat header and contact
         * information for verified business contacts.
         */
        @ProtobufProperty(index = 4, type = ProtobufType.STRING)
        String verifiedName;

        /**
         * The localized variants of the verified business name, each targeting
         * a specific language and country combination.
         *
         * <p>Businesses operating in multiple regions may provide translated
         * versions of their verified name. If empty, only the primary
         * {@link #verifiedName} is available.
         *
         * @see LocalizedName
         */
        @ProtobufProperty(index = 8, type = ProtobufType.MESSAGE)
        List<LocalizedName> localizedNames;

        /**
         * The timestamp at which this certificate was issued, represented as an
         * {@link Instant} converted from epoch seconds via {@link InstantSecondsMixin}.
         */
        @ProtobufProperty(index = 10, type = ProtobufType.UINT64, mixins = InstantSecondsMixin.class)
        Instant issueTime;

        /**
         * Constructs a new {@code Details}.
         *
         * @param serial         the certificate serial number, or {@code null} if absent
         * @param issuer         the certificate issuer, or {@code null} if absent
         * @param verifiedName   the primary verified business name, or {@code null} if absent
         * @param localizedNames the localized name variants, or {@code null} if none
         * @param issueTime      the time the certificate was issued, or {@code null} if absent
         */
        Details(Long serial, CertificateIssuer issuer, String verifiedName, List<LocalizedName> localizedNames, Instant issueTime) {
            this.serial = serial;
            this.issuer = issuer;
            this.verifiedName = verifiedName;
            this.localizedNames = localizedNames;
            this.issueTime = issueTime;
        }

        /**
         * Returns the serial number of this certificate.
         *
         * @return an {@code OptionalLong} containing the serial number,
         *         or empty if not set
         */
        public OptionalLong serial() {
            return serial == null ? OptionalLong.empty() : OptionalLong.of(serial);
        }

        /**
         * Returns the issuer of this certificate.
         *
         * @return an {@code Optional} containing the {@link CertificateIssuer},
         *         or empty if not set
         */
        public Optional<CertificateIssuer> issuer() {
            return Optional.ofNullable(issuer);
        }

        /**
         * Returns the primary verified business name as approved by WhatsApp.
         *
         * @return an {@code Optional} containing the verified name string,
         *         or empty if not set
         */
        public Optional<String> verifiedName() {
            return Optional.ofNullable(verifiedName);
        }

        /**
         * Returns the localized variants of the verified business name.
         *
         * @return an unmodifiable list of {@link LocalizedName} entries;
         *         never {@code null}, returns an empty list if no variants exist
         */
        public List<LocalizedName> localizedNames() {
            return localizedNames == null ? List.of() : Collections.unmodifiableList(localizedNames);
        }

        /**
         * Returns the timestamp at which this certificate was issued.
         *
         * @return an {@code Optional} containing the issue time as an {@link Instant},
         *         or empty if not set
         */
        public Optional<Instant> issueTime() {
            return Optional.ofNullable(issueTime);
        }

        /**
         * Sets the serial number of this certificate.
         *
         * @param serial the serial number to set, or {@code null} to clear
         */
        public void setSerial(Long serial) {
            this.serial = serial;
    }

        /**
         * Sets the issuer of this certificate.
         *
         * @param issuer the {@link CertificateIssuer} to set, or {@code null} to clear
         */
        public void setIssuer(CertificateIssuer issuer) {
            this.issuer = issuer;
    }

        /**
         * Sets the primary verified business name.
         *
         * @param verifiedName the verified name to set, or {@code null} to clear
         */
        public void setVerifiedName(String verifiedName) {
            this.verifiedName = verifiedName;
    }

        /**
         * Sets the localized name variants.
         *
         * @param localizedNames the list of {@link LocalizedName} entries to set,
         *                       or {@code null} to clear
         */
        public void setLocalizedNames(List<LocalizedName> localizedNames) {
            this.localizedNames = localizedNames;
    }

        /**
         * Sets the timestamp at which this certificate was issued.
         *
         * @param issueTime the issue time to set, or {@code null} to clear
         */
        public void setsueTime(Instant issueTime) {
            this.issueTime = issueTime;
    }
    }

    /**
     * Represents a localized variant of a verified business name.
     *
     * <p>Each instance pairs a verified business name with a specific locale
     * identified by a language code and country code. This allows a business
     * to present its name in multiple languages and regions. For example, a
     * business operating in both the US and Brazil might have localized names
     * with language/country pairs {@code "en"/"US"} and {@code "pt"/"BR"}.
     */
    @ProtobufMessage(name = "LocalizedName")
    public static final class LocalizedName {
        /**
         * The ISO 639-1 language code for this localized name, for example
         * {@code "en"}, {@code "es"}, or {@code "pt"}.
         */
        @ProtobufProperty(index = 1, type = ProtobufType.STRING)
        String languageCode;

        /**
         * The ISO 3166-1 alpha-2 country code for this localized name, for example
         * {@code "US"}, {@code "BR"}, or {@code "GB"}.
         */
        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        String countryCode;

        /**
         * The verified business name in the locale identified by
         * {@link #languageCode()} and {@link #countryCode()}.
         */
        @ProtobufProperty(index = 3, type = ProtobufType.STRING)
        String verifiedName;

        /**
         * Constructs a new {@code LocalizedName}.
         *
         * @param languageCode the ISO 639-1 language code, or {@code null} if absent
         * @param countryCode  the ISO 3166-1 alpha-2 country code, or {@code null} if absent
         * @param verifiedName the localized verified business name, or {@code null} if absent
         */
        LocalizedName(String languageCode, String countryCode, String verifiedName) {
            this.languageCode = languageCode;
            this.countryCode = countryCode;
            this.verifiedName = verifiedName;
        }

        /**
         * Returns the ISO 639-1 language code for this localized name.
         *
         * @return an {@code Optional} containing the language code
         *         (for example {@code "en"}), or empty if not set
         */
        public Optional<String> languageCode() {
            return Optional.ofNullable(languageCode);
        }

        /**
         * Returns the ISO 3166-1 alpha-2 country code for this localized name.
         *
         * @return an {@code Optional} containing the country code
         *         (for example {@code "US"}), or empty if not set
         */
        public Optional<String> countryCode() {
            return Optional.ofNullable(countryCode);
        }

        /**
         * Returns the verified business name in this locale.
         *
         * @return an {@code Optional} containing the localized verified name,
         *         or empty if not set
         */
        public Optional<String> verifiedName() {
            return Optional.ofNullable(verifiedName);
        }

        /**
         * Sets the ISO 639-1 language code for this localized name.
         *
         * @param languageCode the language code to set (for example {@code "en"}),
         *                     or {@code null} to clear
         */
        public void setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
    }

        /**
         * Sets the ISO 3166-1 alpha-2 country code for this localized name.
         *
         * @param countryCode the country code to set (for example {@code "US"}),
         *                    or {@code null} to clear
         */
        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
    }

        /**
         * Sets the verified business name in this locale.
         *
         * @param verifiedName the localized verified name to set,
         *                     or {@code null} to clear
         */
        public void setVerifiedName(String verifiedName) {
            this.verifiedName = verifiedName;
    }
    }

    /**
     * Identifies the issuer of a {@link BusinessVerifiedNameCertificate}, which
     * determines the type of WhatsApp Business product the business uses.
     *
     * <p>The issuer is serialized as a string on the wire and distinguishes two
     * categories of business accounts:
     * <ul>
     *   <li>{@link Enterprise} with wire value {@code "ent:wa"} for businesses
     *       using the WhatsApp Business API (Cloud API or On-Premises API).
     *   <li>{@link SmallBusiness} with wire value {@code "smb:wa"} for businesses
     *       using the WhatsApp Business App (small and medium businesses).
     * </ul>
     *
     * <p>Singleton instances are available via {@link #ENTERPRISE} and
     * {@link #SMALL_BUSINESS}. Deserialization from wire format is handled by
     * {@link #deserialize(String)}.
     */
    public sealed static interface CertificateIssuer {
        /**
         * Singleton instance for the enterprise (API) issuer.
         */
        Enterprise ENTERPRISE = new Enterprise();

        /**
         * Singleton instance for the small/medium business (App) issuer.
         */
        SmallBusiness SMALL_BUSINESS = new SmallBusiness();

        /**
         * Returns the wire-format string value for this issuer, used for
         * protobuf serialization.
         *
         * @return {@code "ent:wa"} for {@link Enterprise},
         *         {@code "smb:wa"} for {@link SmallBusiness}
         */
        @ProtobufSerializer
        String value();

        /**
         * Deserializes a wire-format string to the corresponding
         * {@code CertificateIssuer} instance.
         *
         * @param value the wire value to deserialize, may be {@code null}
         * @return the matching {@code CertificateIssuer} instance, or {@code null}
         *         if the input is {@code null}
         * @throws IllegalArgumentException if the value is not a recognized issuer
         *         string ({@code "ent:wa"} or {@code "smb:wa"})
         */
        @ProtobufDeserializer
        static CertificateIssuer deserialize(String value) {
            if (value == null) {
                return null;
            }
            return switch (value) {
                case "ent:wa" -> new Enterprise();
                case "smb:wa" -> new SmallBusiness();
                default -> throw new IllegalArgumentException("Unknown certificate issuer: " + value);
            };
        }

        /**
         * Represents a certificate issued to a business using the WhatsApp Business
         * API (Cloud API or On-Premises API), identified by the wire value
         * {@code "ent:wa"}.
         */
        final class Enterprise implements CertificateIssuer {
            /**
             * Constructs a new {@code Enterprise} issuer instance.
             */
            private Enterprise() {

            }

            /**
             * Returns the wire-format string {@code "ent:wa"}.
             *
             * @return the string {@code "ent:wa"}
             */
            @Override
            public String value() {
                return "ent:wa";
            }

            /**
             * Returns the wire-format string representation of this issuer.
             *
             * @return the string {@code "ent:wa"}
             */
            @Override
            public String toString() {
                return value();
            }
        }

        /**
         * Represents a certificate issued to a business using the WhatsApp Business
         * App (small and medium business tier), identified by the wire value
         * {@code "smb:wa"}.
         */
        final class SmallBusiness implements CertificateIssuer {
            /**
             * Constructs a new {@code SmallBusiness} issuer instance.
             */
            private SmallBusiness() {

            }

            /**
             * Returns the wire-format string {@code "smb:wa"}.
             *
             * @return the string {@code "smb:wa"}
             */
            @Override
            public String value() {
                return "smb:wa";
            }

            /**
             * Returns the wire-format string representation of this issuer.
             *
             * @return the string {@code "smb:wa"}
             */
            @Override
            public String toString() {
                return value();
            }
        }
    }
}
