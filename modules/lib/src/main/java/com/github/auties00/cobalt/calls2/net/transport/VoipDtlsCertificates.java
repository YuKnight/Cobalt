package com.github.auties00.cobalt.calls2.net.transport;

import com.github.auties00.cobalt.util.X509CertificateGenerator;
import com.github.auties00.cobalt.util.X509CertificateSpec;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.Certificate;
import org.bouncycastle.tls.HashAlgorithm;
import org.bouncycastle.tls.SignatureAlgorithm;
import org.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.tls.TlsContext;
import org.bouncycastle.tls.TlsCredentialedSigner;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.bouncycastle.tls.crypto.TlsCryptoParameters;
import org.bouncycastle.tls.crypto.impl.bc.BcDefaultTlsCredentialedSigner;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCertificate;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.security.auth.x500.X500Principal;

/**
 * Shared certificate generation and fingerprint pinning for the WhatsApp Web relay leg's DTLS handshake.
 *
 * <p>Both relay DTLS roles present the same kind of credential and pin the relay's certificate to the same
 * fixed fingerprint: in the common passive mode the {@link VoipDtlsClient} is the DTLS client and pins the
 * relay's server certificate, and in the defensive active mode (the relay block's
 * {@code enable_edgeray_dtls_active_mode}) the {@link VoipDtlsServer} is the DTLS server and pins the relay's
 * client certificate. Both sides authenticate with a freshly generated self-signed ECDSA P-256 certificate,
 * the credential WebRTC carries, and both verify the relay's leaf certificate against the same SHA-256
 * fingerprint. This holder concentrates that common logic so the two peers cannot drift apart: the
 * key-pair-and-certificate generation, the adoption of the certificate into a BouncyCastle credentialed
 * signer, and the constant-time pin check.
 *
 * @implNote This implementation realises the certificate side of WhatsApp Web's synthesized relay answer,
 *           which carries one hardcoded {@code a=fingerprint:sha-256} and a self-signed ECDSA leaf for the
 *           browser end of the relay leg. The key pair is generated through the JCA
 *           ({@code KeyPairGenerator("EC")} on {@code secp256r1}) and the certificate through the JDK-only
 *           {@link X509CertificateGenerator}, then the DER bytes are adopted into the {@link BcTlsCrypto} so the
 *           pure-Java DTLS record layer signs with them; the relay certificate pin is the SHA-256 of the
 *           leaf certificate's DER encoding, the same digest the {@code a=fingerprint} carries. The pin
 *           comparison runs through {@link MessageDigest#isEqual(byte[], byte[])} so it is constant time and
 *           leaks no information about where two fingerprints first differ.
 */
final class VoipDtlsCertificates {
    /**
     * The length, in bytes, of a SHA-256 certificate fingerprint.
     */
    static final int SHA256_FINGERPRINT_LENGTH = 32;

    /**
     * The JCA standard name of the SHA-256 digest used to fingerprint the relay certificate.
     */
    private static final String SHA256_ALGORITHM = "SHA-256";

    /**
     * The named elliptic curve the self-signed certificate key pair is generated on.
     */
    private static final String P256_CURVE = "secp256r1";

    /**
     * The JCA key-pair algorithm of the self-signed certificate key.
     */
    private static final String EC_ALGORITHM = "EC";

    /**
     * The JCA signature algorithm the self-signed certificate is signed with.
     */
    private static final String CERT_SIGNATURE_ALGORITHM = "SHA256withECDSA";

    /**
     * The X.500 subject and issuer name of the self-signed certificate.
     *
     * @implNote This implementation uses a fixed common name; WebRTC certificates are self-signed and their
     * subject is never validated, only their fingerprint, so the name is cosmetic.
     */
    private static final String CERT_SUBJECT = "CN=WebRTC";

    /**
     * The validity window of the self-signed certificate, in days from issuance.
     */
    private static final int CERT_VALIDITY_DAYS = 30;

    /**
     * The number of days before issuance the certificate's not-before is backdated, to tolerate clock skew.
     */
    private static final int CERT_BACKDATE_DAYS = 1;

    /**
     * Prevents instantiation of this static holder.
     */
    private VoipDtlsCertificates() {
        throw new AssertionError("VoipDtlsCertificates is a utility holder and cannot be instantiated");
    }

    /**
     * Builds a credentialed ECDSA signer over a freshly generated self-signed P-256 certificate.
     *
     * <p>The returned signer carries a single-entry certificate chain whose leaf is a new self-signed ECDSA
     * P-256 certificate and the matching private key, signing with SHA-256 and ECDSA. It is the credential a
     * relay DTLS peer presents on the leg, identical whichever DTLS role the peer takes.
     *
     * @param crypto  the pure-Java crypto context the certificate is adopted into and signed through
     * @param context the TLS context the credentialed signer binds its signature parameters to
     * @return the credentialed signer the peer authenticates with
     * @throws IOException if the certificate or key cannot be generated or adopted
     */
    static TlsCredentialedSigner buildCredentialedSigner(BcTlsCrypto crypto, TlsContext context) throws IOException {
        try {
            var keyPair = generateKeyPair(crypto.getSecureRandom());
            var certificateDer = selfSignCertificate(keyPair, crypto.getSecureRandom());
            var tlsCertificate = new BcTlsCertificate(crypto, certificateDer);
            var chain = new Certificate(new TlsCertificate[]{tlsCertificate});
            var privateKey = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
            var algorithm = new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.ecdsa);
            return new BcDefaultTlsCredentialedSigner(
                    new TlsCryptoParameters(context), crypto, privateKey, chain, algorithm);
        } catch (NoSuchAlgorithmException | RuntimeException exception) {
            throw new TlsFatalAlert(AlertDescription.internal_error, exception);
        }
    }

    /**
     * Verifies that the relay's leaf certificate SHA-256 fingerprint matches the pinned value.
     *
     * <p>The same pin is enforced whether the relay presented its certificate as the DTLS server (the client
     * pins it through {@link org.bouncycastle.tls.TlsAuthentication#notifyServerCertificate}) or as the DTLS
     * client (the server pins it through {@link org.bouncycastle.tls.TlsServer#notifyClientCertificate}). An
     * empty chain, which a client that declined to authenticate would present, fails the pin.
     *
     * @param certificate       the certificate chain the relay presented
     * @param pinnedFingerprint the SHA-256 fingerprint the relay certificate is pinned to, thirty-two raw
     *                          digest bytes
     * @throws TlsFatalAlert if the chain is empty or the leaf certificate does not pin
     * @throws IOException   if the leaf certificate cannot be encoded
     */
    static void verifyPinnedCertificate(Certificate certificate, byte[] pinnedFingerprint) throws IOException {
        if (certificate == null || certificate.isEmpty()) {
            throw new TlsFatalAlert(AlertDescription.bad_certificate);
        }
        var leaf = certificate.getCertificateAt(0);
        var actual = sha256(leaf.getEncoded());
        if (!MessageDigest.isEqual(actual, pinnedFingerprint)) {
            throw new TlsFatalAlert(AlertDescription.bad_certificate);
        }
    }

    /**
     * Generates an ECDSA key pair on the P-256 curve.
     *
     * @param random the secure random source
     * @return the generated key pair
     * @throws NoSuchAlgorithmException if the platform lacks EC key generation
     */
    private static KeyPair generateKeyPair(SecureRandom random) throws NoSuchAlgorithmException {
        try {
            var generator = KeyPairGenerator.getInstance(EC_ALGORITHM);
            generator.initialize(new ECGenParameterSpec(P256_CURVE), random);
            return generator.generateKeyPair();
        } catch (java.security.InvalidAlgorithmParameterException exception) {
            throw new NoSuchAlgorithmException("EC P-256 key generation unavailable", exception);
        }
    }

    /**
     * Builds and self-signs an X.509 certificate over the key pair, returning its DER encoding.
     *
     * @param keyPair the certificate key pair, used as both subject key and signing key
     * @param random  the secure random source for the serial number
     * @return the DER-encoded self-signed certificate
     * @throws IOException if the certificate cannot be built or signed
     */
    private static byte[] selfSignCertificate(KeyPair keyPair, SecureRandom random) throws IOException {
        try {
            var serial = new BigInteger(64, random).abs().add(BigInteger.ONE);
            var now = Instant.now();
            var spec = X509CertificateSpec.selfSigned()
                    .keyPair(keyPair)
                    .subject(new X500Principal(CERT_SUBJECT))
                    .serialNumber(serial)
                    .validity(now.minus(CERT_BACKDATE_DAYS, ChronoUnit.DAYS), now.plus(CERT_VALIDITY_DAYS, ChronoUnit.DAYS))
                    .build();
            return X509CertificateGenerator.getInstance(CERT_SIGNATURE_ALGORITHM).generateEncoded(spec);
        } catch (GeneralSecurityException exception) {
            throw new IOException("could not generate the self-signed DTLS certificate", exception);
        }
    }

    /**
     * Computes the SHA-256 digest of the given bytes.
     *
     * @param data the bytes to digest
     * @return the thirty-two-byte SHA-256 digest
     * @throws IOException if the platform lacks SHA-256
     */
    private static byte[] sha256(byte[] data) throws IOException {
        try {
            return MessageDigest.getInstance(SHA256_ALGORITHM).digest(data);
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException("SHA-256 unavailable", exception);
        }
    }
}
