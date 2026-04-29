package com.github.auties00.cobalt.registration.push.apns;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Full serializable state of an {@link ApnsClient}: the immutable
 * {@link ApnsConfig} plus the device-bound credentials accumulated
 * across the activation pipeline. Two of these credentials persist for
 * roughly three years (the keypair and the {@code deviceCertificate}
 * Apple signs against it); the third ({@code authToken}) is only
 * meaningful while a courier connection is alive.
 *
 * <p>This is the only class the caller needs to round-trip via
 * {@link ApnsClient#getSession()} /
 * {@link ApnsClient#loadSession(ApnsSession)} to keep the same APNS
 * push token across process restarts without re-running the
 * activation flow.
 */
@ProtobufMessage(name = "ApnsSession")
public final class ApnsSession {
    /**
     * The configuration the session was created with. Bundled in the
     * serialized output so a saved session loads back without the
     * caller having to remember which {@link ApnsConfig} it was
     * originally created against.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    ApnsConfig config;

    /**
     * PKCS#8-encoded RSA private key (DER). Generated once and bound to
     * {@link #deviceCertificate}. Empty until activation succeeds.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    byte[] privateKeyDer;

    /**
     * X.509-encoded RSA public key (DER), the {@code SubjectPublicKeyInfo}
     * that goes into the CSR. Empty until activation succeeds.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    byte[] publicKeyDer;

    /**
     * DER-encoded {@code DeviceCertificate} returned by
     * {@code albert.apple.com} after a successful activation. Valid
     * for ~3 years, after which {@link ApnsClient} must re-run the
     * activation flow. Empty until activation succeeds.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.BYTES)
    byte[] deviceCertificate;

    ApnsSession(ApnsConfig config, byte[] privateKeyDer, byte[] publicKeyDer, byte[] deviceCertificate) {
        this.config = config;
        this.privateKeyDer = privateKeyDer == null ? new byte[0] : privateKeyDer;
        this.publicKeyDer = publicKeyDer == null ? new byte[0] : publicKeyDer;
        this.deviceCertificate = deviceCertificate == null ? new byte[0] : deviceCertificate;
    }

    /**
     * Creates an empty session bound to {@code config} — every
     * credential field is zero-length. Used by
     * {@link ApnsClient#authenticate} on first run.
     *
     * @param config the topic list to bind the new session to
     * @return a fresh credential-less session
     */
    static ApnsSession newSession(ApnsConfig config) {
        return new ApnsSession(config, new byte[0], new byte[0], new byte[0]);
    }

    public ApnsConfig config() {
        return config;
    }

    public byte[] privateKeyDer() {
        return privateKeyDer;
    }

    public byte[] publicKeyDer() {
        return publicKeyDer;
    }

    public byte[] deviceCertificate() {
        return deviceCertificate;
    }

    void setPrivateKeyDer(byte[] privateKeyDer) {
        this.privateKeyDer = privateKeyDer;
    }

    void setPublicKeyDer(byte[] publicKeyDer) {
        this.publicKeyDer = publicKeyDer;
    }

    void setDeviceCertificate(byte[] deviceCertificate) {
        this.deviceCertificate = deviceCertificate;
    }
}
