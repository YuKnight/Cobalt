package com.github.auties00.cobalt.calls2.net.transport;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.Certificate;
import org.bouncycastle.tls.ClientCertificateType;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCertificate;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the relay DTLS server's handshake configuration and its client-certificate pinning. The active
 * mode this server drives (relay-as-DTLS-client) is the defensive, uncaptured path WhatsApp Web's
 * {@code a=setup:active} relay answer would select; the suite asserts the static handshake shape and the pin
 * decision without standing up a live relay, mirroring the role-inverse {@link VoipDtlsClient}. A real
 * self-signed ECDSA P-256 certificate is generated through the same JCA and BouncyCastle path the production
 * {@link VoipDtlsCertificates} helper uses so the SHA-256 pin can be exercised against a genuine leaf.
 */
@DisplayName("VoipDtlsServer relay DTLS active-mode handshake")
class VoipDtlsServerTest {
    private static final byte[] PIN = RelayDataChannel.RELAY_CERT_FINGERPRINT_SHA256;

    @Nested
    @DisplayName("constructor")
    class Construction {
        @Test
        @DisplayName("rejects a fingerprint that is not exactly 32 bytes")
        void rejectsShortFingerprint() {
            assertThrows(IllegalArgumentException.class, () -> new VoipDtlsServer(new byte[31]));
            assertThrows(IllegalArgumentException.class, () -> new VoipDtlsServer(new byte[33]));
        }

        @Test
        @DisplayName("rejects a null fingerprint")
        void rejectsNullFingerprint() {
            assertThrows(NullPointerException.class, () -> new VoipDtlsServer(null));
        }
    }

    @Nested
    @DisplayName("handshake configuration")
    class HandshakeConfig {
        @Test
        @DisplayName("offers DTLS 1.2 only, matching the relay leg")
        void offersDtls12Only() {
            var server = new VoipDtlsServer(PIN);
            assertArrayEquals(new ProtocolVersion[]{ProtocolVersion.DTLSv12}, server.getProtocolVersions());
        }

        @Test
        @DisplayName("offers the ECDHE-ECDSA AES-GCM cipher suites the client offers")
        void offersEcdheEcdsaAesGcm() {
            // The relay presents a self-signed ECDSA leaf, so both roles negotiate ECDHE-ECDSA AES-GCM; the
            // server's offered suites must match the VoipDtlsClient's so the inverse role interoperates.
            var server = new VoipDtlsServer(PIN);
            assertArrayEquals(new VoipDtlsClient(PIN).getSupportedCipherSuites(),
                    server.getSupportedCipherSuites());
        }

        @Test
        @DisplayName("requests an ECDSA client certificate so the relay's cert can be pinned")
        void requestsEcdsaClientCertificate() {
            var server = new VoipDtlsServer(PIN);
            var request = server.getCertificateRequest();
            assertArrayEquals(new short[]{ClientCertificateType.ecdsa_sign}, request.getCertificateTypes());
            assertEquals(1, request.getSupportedSignatureAlgorithms().size(),
                    "the request advertises exactly the SHA-256-with-ECDSA algorithm");
        }
    }

    @Nested
    @DisplayName("client-certificate pinning")
    class ClientCertificatePinning {
        @Test
        @DisplayName("accepts a client certificate whose SHA-256 fingerprint pins")
        void acceptsPinnedCertificate() throws Exception {
            var crypto = new BcTlsCrypto(new SecureRandom());
            var der = generateSelfSignedP256Der(crypto.getSecureRandom());
            var fingerprint = MessageDigest.getInstance("SHA-256").digest(der);
            var server = new VoipDtlsServer(fingerprint);
            var chain = chainOf(crypto, der);
            assertDoesNotThrow(() -> server.notifyClientCertificate(chain));
        }

        @Test
        @DisplayName("rejects a client certificate whose fingerprint does not pin")
        void rejectsUnpinnedCertificate() throws Exception {
            var crypto = new BcTlsCrypto(new SecureRandom());
            var der = generateSelfSignedP256Der(crypto.getSecureRandom());
            // PIN is the fixed relay fingerprint, which a freshly generated cert never matches.
            var server = new VoipDtlsServer(PIN);
            var chain = chainOf(crypto, der);
            var alert = assertThrows(TlsFatalAlert.class, () -> server.notifyClientCertificate(chain));
            assertEquals(AlertDescription.bad_certificate, alert.getAlertDescription());
        }

        @Test
        @DisplayName("rejects an empty certificate chain (a relay that declined to authenticate)")
        void rejectsEmptyChain() {
            var server = new VoipDtlsServer(PIN);
            var alert = assertThrows(TlsFatalAlert.class,
                    () -> server.notifyClientCertificate(Certificate.EMPTY_CHAIN));
            assertEquals(AlertDescription.bad_certificate, alert.getAlertDescription());
        }

        @Test
        @DisplayName("the pin is the same fixed fingerprint the client pins on the relay's server cert")
        void pinsSameFixedFingerprint() {
            // The role flip must not change the pinned constant: both the active-mode server and the passive
            // client verify the relay against RelayDataChannel.RELAY_CERT_FINGERPRINT_SHA256.
            assertTrue(RelayDataChannel.RELAY_CERT_FINGERPRINT_SHA256.length
                    == VoipDtlsCertificates.SHA256_FINGERPRINT_LENGTH);
            assertDoesNotThrow(() -> new VoipDtlsServer(RelayDataChannel.RELAY_CERT_FINGERPRINT_SHA256));
        }
    }

    /**
     * Builds a single-entry BouncyCastle TLS certificate chain from leaf DER bytes.
     */
    private static Certificate chainOf(BcTlsCrypto crypto, byte[] der) throws Exception {
        var leaf = new BcTlsCertificate(crypto, der);
        return new Certificate(new TlsCertificate[]{leaf});
    }

    /**
     * Generates a self-signed ECDSA P-256 certificate and returns its DER encoding, mirroring the production
     * certificate path so the pin can be exercised against a genuine leaf.
     */
    private static byte[] generateSelfSignedP256Der(SecureRandom random) throws Exception {
        var generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"), random);
        var keyPair = generator.generateKeyPair();
        var subject = new X500Name("CN=WebRTC");
        var serial = new BigInteger(64, random).abs().add(BigInteger.ONE);
        var notBefore = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
        var notAfter = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        var builder = new JcaX509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject, keyPair.getPublic());
        var signer = new JcaContentSignerBuilder("SHA256withECDSA")
                .setSecureRandom(random)
                .build(keyPair.getPrivate());
        return builder.build(signer).getEncoded();
    }

    @Test
    @DisplayName("the shared certificate helper rejects a mismatched fingerprint with bad_certificate")
    void sharedHelperRejectsMismatch() throws Exception {
        var crypto = new BcTlsCrypto(new SecureRandom());
        var der = generateSelfSignedP256Der(crypto.getSecureRandom());
        var fingerprint = MessageDigest.getInstance("SHA-256").digest(der);
        var wrong = fingerprint.clone();
        wrong[0] ^= 0x01;
        var chain = chainOf(crypto, der);
        // A one-bit perturbation of the correct fingerprint must still be rejected (constant-time compare).
        var alert = assertThrows(TlsFatalAlert.class,
                () -> VoipDtlsCertificates.verifyPinnedCertificate(chain, wrong));
        assertEquals(AlertDescription.bad_certificate, alert.getAlertDescription());
        assertDoesNotThrow(() -> VoipDtlsCertificates.verifyPinnedCertificate(chain, fingerprint));
        assertTrue(Arrays.equals(fingerprint, MessageDigest.getInstance("SHA-256").digest(der)));
    }
}
