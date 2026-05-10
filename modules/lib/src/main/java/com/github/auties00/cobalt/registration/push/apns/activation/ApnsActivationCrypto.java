package com.github.auties00.cobalt.registration.push.apns.activation;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * Crypto helpers used exclusively by the
 * {@code albert.apple.com} activation flow: the leaked FairPlay
 * private key and its signature primitive, generation of the device
 * RSA keypair, and DER encoding of the PKCS#10 certificate request
 * that the activation endpoint signs.
 *
 * <p>Lives in the {@code activation} subpackage alongside the plist
 * model that mirrors what the endpoint returns. The complementary
 * courier-side helpers (nonce signing, device-cert re-encoding,
 * SHA-1 topic hashes, keypair restore) live in
 * {@code apns.courier.ApnsCourierCrypto}.
 *
 * <p>The DER encoding of the CSR is hand-rolled rather than pulled
 * from BouncyCastle
 */
public final class ApnsActivationCrypto {
    /**
     * Modulus of the leaked FairPlay private key. Built from a
     * signed-byte two's-complement big-endian array. The leading
     * {@code 0x00} keeps the high bit clear so the value stays
     * positive.
     */
    private static final BigInteger FAIRPLAY_PRIVATE_KEY_MODULUS = new BigInteger(new byte[]{0, -73, 4, -86, -53, 60, -128, 90, 110, 26, 107, -5, -124, -81, -71, 1, -25, 108, 93, 44, -78, -92, 72, 67, -82, -1, -43, -76, -33, 2, -75, 110, 80, 94, -87, -10, -57, 92, -65, -67, 13, -6, -65, 50, 23, 23, -84, 14, 106, 83, -104, -88, -83, 44, -3, 58, -77, 50, -115, 0, -19, 100, -28, -107, 100, -9, -13, -11, 122, 60, -12, -16, 19, 30, -11, -32, 27, -30, 59, -128, -123, 85, -99, 124, 2, 50, 104, -98, -20, 24, 88, -34, 72, -67, 53, -26, 96, 47, -81, 20, -59, 112, -29, 53, -48, 95, -22, 74, 80, 17, 6, 39, 28, -60, 120, -49, 94, -37, -106, 86, -67, 31, 44, 106, -31, 51, -17, -64, 49});
    /**
     * Public exponent of the FairPlay key, the canonical
     * {@code 0x010001}.
     */
    private static final BigInteger FAIRPLAY_PRIVATE_KEY_PUBLIC_EXPONENT = new BigInteger(new byte[]{1, 0, 1});
    /**
     * Private exponent {@code d} of the FairPlay key.
     */
    private static final BigInteger FAIRPLAY_PRIVATE_KEY_PRIVATE_EXPONENT = new BigInteger(new byte[]{8, 101, -73, 108, 113, -49, 53, -42, -3, 113, 92, -19, -2, -98, 15, 127, 77, -46, -116, -99, 121, -70, 51, 24, -47, 118, 61, -63, 73, -65, -121, 91, 58, -84, -77, -68, -5, -3, 116, 48, 51, 4, 24, -55, 68, 117, -55, -121, -119, 100, 100, -64, -27, 98, -115, 17, -15, -52, -44, 113, 16, 3, 8, -13, -80, 43, -70, -121, -31, -115, -41, 91, 53, 82, -19, -22, -102, 86, -38, 114, -62, 43, -73, 4, 13, 2, 69, 106, 20, -42, -12, 100, 5, -50, -107, 81, 65, 117, -33, -58, -66, 28, -77, 85, -9, -110, -21, 101, 7, -85, -104, -39, 81, -91, -73, 20, 5, 98, -56, -7, 118, 59, -5, 15, -32, 59, -40, 13});
    /**
     * Prime {@code p} of the FairPlay CRT private key.
     */
    private static final BigInteger FAIRPLAY_PRIVATE_KEY_PRIME_P = new BigInteger(new byte[]{0, -39, -113, 84, 70, 119, 22, -34, 58, -114, 23, -49, 23, -64, -22, 126, 54, 26, -106, 90, -67, 57, 76, -35, 12, -58, 22, -128, 73, -128, 42, -86, 86, -19, 73, 64, -20, -24, -96, 3, 17, 79, -29, 79, 126, 10, 4, -13, -21, -84, 33, 85, 65, -32, -118, 52, 108, 37, 19, 88, -70, 100, 19, 43, 83});
    /**
     * Prime {@code q} of the FairPlay CRT private key.
     */
    private static final BigInteger FAIRPLAY_PRIVATE_KEY_PRIME_Q = new BigInteger(new byte[]{0, -41, 90, -13, 34, -111, -120, -124, 89, 15, -76, -41, 63, -33, 119, -17, 4, 88, 48, 81, -69, 35, 78, -51, 85, -28, 110, -77, -26, 92, -65, 94, 113, -67, 16, -41, -53, 110, 88, 4, -95, -78, -26, 74, 108, -9, -118, -69, -63, 69, 96, 3, -117, 5, 49, -25, 54, 58, 20, -33, -61, -102, -40, -71, -21});
    /**
     * Exponent {@code dP = d mod (p-1)} of the FairPlay CRT key.
     */
    private static final BigInteger FAIRPLAY_PRIVATE_KEY_PRIME_EXPONENT_P = new BigInteger(new byte[]{118, 36, -100, 106, 75, -97, 114, 124, -81, -49, 4, 25, -19, 28, 41, -1, -83, 126, 122, -74, 9, 24, -47, 109, 111, 96, -90, -73, -61, 78, -24, 3, -98, -123, -54, 41, 28, -58, 80, 4, 37, -78, -43, -25, 38, -1, -69, -119, -2, -122, 119, 106, -9, -55, 117, 96, 72, -35, -15, -81, -2, 74, 94, -101});
    /**
     * Exponent {@code dQ = d mod (q-1)} of the FairPlay CRT key.
     */
    private static final BigInteger FAIRPLAY_PRIVATE_KEY_PRIME_EXPONENT_Q = new BigInteger(new byte[]{24, -59, -14, -96, 48, 99, -90, -19, -29, -37, -90, -61, 71, 62, -79, -75, 43, 59, -21, -70, -2, 85, -53, 83, 45, 34, -6, -8, -18, 4, 105, -91, -27, -36, -15, 38, 10, -68, 127, 83, -26, -109, -115, 78, 57, -81, -80, -25, -117, -58, 126, -63, -40, 72, 36, 83, -35, -100, -105, 29, 22, 76, 6, 31});
    /**
     * Coefficient {@code qInv = q^-1 mod p} of the FairPlay CRT key.
     */
    private static final BigInteger FAIRPLAY_PRIVATE_KEY_CRT_EXPONENT = new BigInteger(new byte[]{61, 15, -80, 86, -92, 99, 115, -120, 119, 31, 11, -68, 35, -87, 101, -109, -36, 33, -92, -81, 78, -17, 65, 75, -93, 81, 76, 85, -42, -78, -76, 73, 76, -54, -84, -48, -37, -3, 57, 124, -58, -5, 23, -84, -102, 90, 27, -66, 67, 97, -122, 94, -9, 101, 81, 24, -128, -34, -42, 52, 10, -57, -52, -45});

    /**
     * DER of an empty {@code AlgorithmIdentifier} for
     * sha256WithRSAEncryption (OID 1.2.840.113549.1.1.11). Inline
     * because we only ever emit one.
     */
    private static final byte[] SHA256_WITH_RSA_ALGORITHM_ID = {
            0x30, 0x0D,
            0x06, 0x09, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x0B,
            0x05, 0x00
    };

    /**
     * DER for an empty {@code [0] IMPLICIT SET OF Attribute}
     * (PKCS#10 attributes block). The activation flow needs no
     * attributes, so the value is a constant.
     */
    private static final byte[] EMPTY_ATTRIBUTES = {(byte) 0xA0, 0x00};

    /**
     * Number of base64 characters per line of the PEM-wrapped CSR.
     * Matches the conventional 64-char width OpenSSL produces.
     */
    private static final int CSR_LINE_BLOCK = 64;

    /**
     * Modulus length of the device-bound RSA key. Matches the value
     * the iPhone Device CA expects in the CSR. Lower would be
     * rejected, higher would slow the activation handshake without
     * the server caring.
     */
    private static final int RSA_KEY_SIZE = 2048;

    /**
     * The leaked FairPlay private key, materialised once on class
     * load.
     */
    private static final PrivateKey FAIRPLAY_PRIVATE_KEY = loadFairplayPrivateKey();


    /**
     * Materialises the leaked FairPlay key from the inline CRT
     * components. Called once on class load.
     *
     * @return the loaded FairPlay private key
     */
    private static PrivateKey loadFairplayPrivateKey() {
        try {
            var spec = new RSAPrivateCrtKeySpec(
                    FAIRPLAY_PRIVATE_KEY_MODULUS,
                    FAIRPLAY_PRIVATE_KEY_PUBLIC_EXPONENT,
                    FAIRPLAY_PRIVATE_KEY_PRIVATE_EXPONENT,
                    FAIRPLAY_PRIVATE_KEY_PRIME_P,
                    FAIRPLAY_PRIVATE_KEY_PRIME_Q,
                    FAIRPLAY_PRIVATE_KEY_PRIME_EXPONENT_P,
                    FAIRPLAY_PRIVATE_KEY_PRIME_EXPONENT_Q,
                    FAIRPLAY_PRIVATE_KEY_CRT_EXPONENT);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("FairPlay private key load failed", e);
        }
    }

    private ApnsActivationCrypto() {
        throw new UnsupportedOperationException("ApnsActivationCrypto is a utility class and cannot be initialized");
    }

    /**
     * Generates a fresh device-bound RSA keypair used for both the
     * activation CSR and the connect-time nonce signature.
     *
     * @return a freshly generated 2048-bit RSA keypair
     */
    public static KeyPair newRsaKeyPair() {
        try {
            var gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(RSA_KEY_SIZE);
            return gen.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("RSA generator unavailable", e);
        }
    }

    /**
     * Builds a PEM-encoded PKCS#10 certificate request for
     * {@code keyPair} with a random subject CN. The activation
     * endpoint embeds the resulting CSR in the request body and
     * returns a signed device certificate carrying our public key.
     *
     * @param keyPair the device keypair the CSR is bound to
     * @return the PEM-wrapped CSR bytes
     */
    public static byte[] generateCsr(KeyPair keyPair) {
        try {
            var subject = new X500Principal(
                    "C=US,ST=CA,L=Cupertino,O=Apple Inc.,OU=iPhone,CN=" + UUID.randomUUID());
            var requestInfo = encodeCertificationRequestInfo(
                    subject.getEncoded(), keyPair.getPublic());
            var signature = signSha256(keyPair.getPrivate(), requestInfo);
            var certificationRequest = encodeCertificationRequest(requestInfo, signature);
            return wrapAsPem(
                    "-----BEGIN CERTIFICATE REQUEST-----",
                    certificationRequest,
                    "-----END CERTIFICATE REQUEST-----");
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("CSR generation failed", e);
        }
    }

    /**
     * Signs {@code activationInfoXml} with the leaked FairPlay
     * private key. The output is the {@code FairPlaySignature} field
     * of the activation request body.
     *
     * @param activationInfoXml the inner activation plist bytes
     * @return the SHA-1-RSA signature over the input
     */
    public static byte[] signActivationInfo(byte[] activationInfoXml) {
        try {
            var sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(FAIRPLAY_PRIVATE_KEY);
            sig.update(activationInfoXml);
            return sig.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("FairPlay signature failed", e);
        }
    }

    /**
     * Computes a SHA-256-with-RSA signature over {@code data} using
     * {@code key}. Used internally to sign the
     * {@code CertificationRequestInfo} body of the CSR.
     *
     * @param key  the RSA private key to sign with
     * @param data the bytes to sign
     * @return the signature bytes
     * @throws GeneralSecurityException if the JCA call fails
     */
    private static byte[] signSha256(PrivateKey key, byte[] data) throws GeneralSecurityException {
        var sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(key);
        sig.update(data);
        return sig.sign();
    }

    /**
     * Encodes the {@code CertificationRequestInfo} ASN.1 structure
     * (PKCS#10 §4.1) into DER. The structure carries the version,
     * subject, subjectPublicKeyInfo and an empty attributes set.
     *
     * @param subjectDer the DER-encoded subject distinguished name
     * @param publicKey  the public key to embed
     * @return the DER-encoded request info
     * @throws IOException if the underlying writer fails
     */
    private static byte[] encodeCertificationRequestInfo(byte[] subjectDer, PublicKey publicKey) throws IOException {
        var inner = new ByteArrayOutputStream();
        // version INTEGER 0
        writeDer(inner, (byte) 0x02, new byte[]{0x00});
        // subject Name (already DER)
        inner.write(subjectDer);
        // subjectPublicKeyInfo (already DER from X.509)
        inner.write(publicKey.getEncoded());
        // attributes [0] IMPLICIT, empty set
        inner.write(EMPTY_ATTRIBUTES);
        return wrapInSequence(inner.toByteArray());
    }

    /**
     * Encodes the outer {@code CertificationRequest} ASN.1 structure
     * (PKCS#10 §4.2) wrapping {@code requestInfo}, the
     * SHA-256-with-RSA algorithm identifier, and {@code signature}.
     *
     * @param requestInfo the DER-encoded request info from
     *                    {@link #encodeCertificationRequestInfo}
     * @param signature   the SHA-256-with-RSA signature over
     *                    {@code requestInfo}
     * @return the DER-encoded full PKCS#10 request
     * @throws IOException if the underlying writer fails
     */
    private static byte[] encodeCertificationRequest(byte[] requestInfo, byte[] signature) throws IOException {
        var inner = new ByteArrayOutputStream();
        inner.write(requestInfo);
        inner.write(SHA256_WITH_RSA_ALGORITHM_ID);
        // BIT STRING: 0x03 LENGTH 0x00 <signature>
        var bitString = new byte[signature.length + 1];
        bitString[0] = 0x00; // unused bits in last byte
        System.arraycopy(signature, 0, bitString, 1, signature.length);
        writeDer(inner, (byte) 0x03, bitString);
        return wrapInSequence(inner.toByteArray());
    }

    /**
     * Wraps {@code payload} in an ASN.1 {@code SEQUENCE} (tag
     * {@code 0x30}) with a DER length prefix.
     *
     * @param payload the contents of the SEQUENCE
     * @return the DER-encoded SEQUENCE
     * @throws IOException if the underlying writer fails
     */
    private static byte[] wrapInSequence(byte[] payload) throws IOException {
        var out = new ByteArrayOutputStream();
        writeDer(out, (byte) 0x30, payload);
        return out.toByteArray();
    }

    /**
     * Writes one DER TLV record into {@code out}: the tag byte,
     * either a short-form length (single byte) or long-form length
     * ({@code 0x80 | n}, then {@code n} big-endian bytes), then the
     * payload.
     *
     * @param out     the destination stream
     * @param tag     the ASN.1 tag byte
     * @param payload the value bytes
     * @throws IOException if the underlying writer fails
     */
    private static void writeDer(ByteArrayOutputStream out, byte tag, byte[] payload) throws IOException {
        out.write(tag);
        var len = payload.length;
        if (len < 0x80) {
            out.write(len);
        } else {
            // long form: 0x80 | <num length-bytes>, then the length itself big-endian
            var lenBytes = new ByteArrayOutputStream();
            for (var v = len; v > 0; v >>>= 8) {
                lenBytes.write(v & 0xFF);
            }
            var raw = lenBytes.toByteArray();
            out.write(0x80 | raw.length);
            for (var i = raw.length - 1; i >= 0; i--) {
                out.write(raw[i] & 0xFF);
            }
        }
        out.write(payload);
    }

    /**
     * Wraps DER bytes in a PEM envelope: {@code header} line, then
     * the base64-encoded DER broken into {@link #CSR_LINE_BLOCK}-char
     * lines, then {@code footer} line.
     *
     * @param header the {@code -----BEGIN ...-----} line
     * @param der    the DER bytes to wrap
     * @param footer the {@code -----END ...-----} line
     * @return the PEM-wrapped UTF-8 bytes
     */
    private static byte[] wrapAsPem(String header, byte[] der, String footer) {
        var base64 = Base64.getEncoder().encodeToString(der);
        var sb = new StringBuilder(header.length() + footer.length() + base64.length() + 64);
        sb.append(header).append('\n');
        for (var i = 0; i < base64.length(); i += CSR_LINE_BLOCK) {
            sb.append(base64, i, Math.min(base64.length(), i + CSR_LINE_BLOCK)).append('\n');
        }
        sb.append(footer).append('\n');
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
