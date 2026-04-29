package com.github.auties00.cobalt.registration.push.apns.courier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Crypto helpers used exclusively by the courier connection flow:
 * restoring the device keypair from the persisted DER blobs,
 * building and signing the connect-time nonce, re-encoding the
 * Apple-signed device certificate into the canonical wire form, and
 * computing the SHA-1 topic hashes that gate
 * {@link ApnsPayloadTag#FILTER} subscriptions and
 * {@link ApnsPayloadTag#GET_TOKEN} requests.
 *
 * <p>Lives in the {@code courier} subpackage alongside the wire
 * model classes so the courier-specific crypto and protocol code
 * stay together. The complementary activation-side helpers
 * (FairPlay key, CSR generation, RSA keypair generation,
 * activation-info signing) live in
 * {@code apns.activation.ApnsActivationCrypto}.
 */
public final class ApnsCourierCrypto {
    /**
     * Length of the connect-time nonce in bytes: one version byte,
     * eight Unix-ms timestamp bytes, eight random bytes.
     */
    private static final int NONCE_LENGTH = 17;

    /**
     * Two-byte algorithm tag prepended to the nonce signature so the
     * courier's signature parser knows which scheme produced the
     * trailing bytes ({@code 0x01 0x01} = SHA-1 RSA).
     */
    private static final byte[] NONCE_SIGNATURE_TAG = {0x01, 0x01};

    private ApnsCourierCrypto() {
    }

    /**
     * Reconstructs an RSA {@link KeyPair} from the X.509 / PKCS#8
     * DER blobs persisted in the APNS session.
     *
     * @param publicKeyDer  the X.509-encoded public key bytes
     * @param privateKeyDer the PKCS#8-encoded private key bytes
     * @return the materialised keypair
     * @throws IOException if either DER blob is invalid for an RSA
     *                     key
     */
    public static KeyPair restoreKeyPair(byte[] publicKeyDer, byte[] privateKeyDer) throws IOException {
        try {
            var keyFactory = KeyFactory.getInstance("RSA");
            var pub = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyDer));
            var priv = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyDer));
            return new KeyPair(pub, priv);
        } catch (GeneralSecurityException e) {
            throw new IOException("APNS keypair restore failed", e);
        }
    }

    /**
     * Builds the {@value #NONCE_LENGTH}-byte connect-time nonce: one
     * version byte ({@code 0x00}), an 8-byte big-endian Unix-ms
     * timestamp, then 8 cryptographically random bytes.
     *
     * @param random the source of randomness
     * @return the freshly built nonce
     */
    public static byte[] createNonce(SecureRandom random) {
        var buf = ByteBuffer.allocate(NONCE_LENGTH);
        buf.putLong(1, System.currentTimeMillis());
        var rnd = new byte[8];
        random.nextBytes(rnd);
        buf.put(9, rnd);
        return buf.array();
    }

    /**
     * Signs the connect-time {@code nonce} with the device's RSA
     * private key, prefixed with the {@code 0x01 0x01} algorithm tag
     * the courier expects.
     *
     * @param keyPair the device keypair
     * @param nonce   the nonce bytes from {@link #createNonce}
     * @return the {@code 0x01 0x01}-prefixed signature bytes
     */
    public static byte[] signNonce(KeyPair keyPair, byte[] nonce) {
        try {
            var sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(keyPair.getPrivate());
            sig.update(nonce);
            var raw = sig.sign();
            var out = new byte[raw.length + NONCE_SIGNATURE_TAG.length];
            System.arraycopy(NONCE_SIGNATURE_TAG, 0, out, 0, NONCE_SIGNATURE_TAG.length);
            System.arraycopy(raw, 0, out, NONCE_SIGNATURE_TAG.length, raw.length);
            return out;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("nonce signature failed", e);
        }
    }

    /**
     * Re-encodes {@code certificateBytes} (PEM or DER from
     * {@code albert.apple.com}) into the canonical DER form the
     * courier {@link ApnsPayloadTag#CONNECT} packet expects.
     *
     * @param certificateBytes the certificate blob in PEM or DER form
     * @return the canonical DER encoding
     * @throws CertificateException if the input is not a valid X.509
     *                              certificate
     */
    public static byte[] reencodeDeviceCertificate(byte[] certificateBytes) throws CertificateException {
        try (var in = new ByteArrayInputStream(certificateBytes)) {
            var factory = CertificateFactory.getInstance("X.509");
            var cert = (X509Certificate) factory.generateCertificate(in);
            return cert.getEncoded();
        } catch (IOException e) {
            throw new CertificateException(e);
        }
    }

    /**
     * Computes the SHA-1 of {@code value} (UTF-8). Used for the
     * topic hashes the courier expects in
     * {@link ApnsPayloadTag#FILTER} and
     * {@link ApnsPayloadTag#GET_TOKEN}.
     *
     * @param value the bundle id (or any string) to hash
     * @return the 20-byte SHA-1 digest
     */
    public static byte[] sha1(String value) {
        try {
            var md = MessageDigest.getInstance("SHA-1");
            return md.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 unavailable", e);
        }
    }
}
