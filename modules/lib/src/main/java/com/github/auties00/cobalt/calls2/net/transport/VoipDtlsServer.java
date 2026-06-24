package com.github.auties00.cobalt.calls2.net.transport;

import org.bouncycastle.tls.Certificate;
import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.CipherSuite;
import org.bouncycastle.tls.ClientCertificateType;
import org.bouncycastle.tls.DefaultTlsServer;
import org.bouncycastle.tls.HashAlgorithm;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.SignatureAlgorithm;
import org.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.tls.TlsCredentialedSigner;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;

import java.io.IOException;
import java.util.Objects;
import java.util.Vector;

/**
 * The BouncyCastle DTLS server driving the WhatsApp Web relay leg's DTLS handshake when the relay takes the
 * DTLS client role.
 *
 * <p>The common relay leg is passive (the relay is the DTLS server and the web client is the DTLS client,
 * driven by {@link VoipDtlsClient}); when the relay block sets {@code enable_edgeray_dtls_active_mode}
 * WhatsApp Web's {@code createAnswerSdp} emits {@code a=setup:active}, which flips the relay to the DTLS
 * client and the web client to the DTLS server, the role this peer takes. The server presents a freshly
 * generated self-signed ECDSA P-256 certificate, offers DTLS 1.2 and the ECDHE-ECDSA AES-GCM cipher suites,
 * REQUESTS a client certificate (the relay's), and pins that client certificate to the same fixed SHA-256
 * fingerprint the {@link VoipDtlsClient} pins on the relay's server certificate. A relay that presents no
 * certificate, or one whose fingerprint does not pin, fails the handshake with a fatal bad-certificate
 * alert.
 *
 * <p>Everything after the DTLS handshake completes (the SCTP association, the pre-negotiated data channel,
 * and the media plane) is identical for both DTLS roles; only the handshake driver differs.
 *
 * @implNote This implementation is a defensive, uncaptured path: every live relay answer observed carries
 *           {@code a=setup:passive}, so the relay has never been seen taking the DTLS client role and this
 *           server handshake has never run against a real relay. It is implemented because the reverse
 *           engineering deterministically defines the inverse role
 *           (re/calls2-spec/web-transport-construction-RE.md): {@code a=setup:active} makes the relay the
 *           DTLS client, so the web client must be the DTLS server, present a certificate, request the
 *           relay's, and pin it to the same constant fingerprint. The certificate generation and the pin
 *           check are delegated to {@link VoipDtlsCertificates}, the holder shared with {@link VoipDtlsClient}
 *           so the two roles cannot drift apart. The cipher suites and the DTLS 1.2 floor match the client.
 */
final class VoipDtlsServer extends DefaultTlsServer {
    /**
     * The pinned SHA-256 fingerprint the relay client certificate is verified against.
     */
    private final byte[] pinnedFingerprint;

    /**
     * Constructs the relay DTLS server over a fresh pure-Java crypto context and the pinned fingerprint.
     *
     * @param pinnedFingerprint the SHA-256 fingerprint the relay certificate is pinned to, thirty-two raw
     *                          digest bytes
     * @throws NullPointerException     if {@code pinnedFingerprint} is {@code null}
     * @throws IllegalArgumentException if {@code pinnedFingerprint} is not exactly thirty-two bytes
     */
    VoipDtlsServer(byte[] pinnedFingerprint) {
        super(new BcTlsCrypto(RelayDataChannel.secureRandom()));
        Objects.requireNonNull(pinnedFingerprint, "pinnedFingerprint cannot be null");
        if (pinnedFingerprint.length != VoipDtlsCertificates.SHA256_FINGERPRINT_LENGTH) {
            throw new IllegalArgumentException("pinned fingerprint must be "
                    + VoipDtlsCertificates.SHA256_FINGERPRINT_LENGTH + " bytes, got " + pinnedFingerprint.length);
        }
        this.pinnedFingerprint = pinnedFingerprint.clone();
    }

    /**
     * {@inheritDoc}
     *
     * @return the DTLS 1.2 version only, the version WebRTC's relay leg negotiates
     */
    @Override
    public ProtocolVersion[] getProtocolVersions() {
        return new ProtocolVersion[]{ProtocolVersion.DTLSv12};
    }

    /**
     * {@inheritDoc}
     *
     * @return the ECDHE-ECDSA AES-GCM cipher suites, matching the self-signed ECDSA certificate the server
     * presents
     */
    @Override
    protected int[] getSupportedCipherSuites() {
        return new int[]{
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return the credentialed signer over a freshly generated self-signed ECDSA P-256 certificate
     * @throws IOException if the certificate or key cannot be generated or adopted
     */
    @Override
    protected TlsCredentialedSigner getECDSASignerCredentials() throws IOException {
        return VoipDtlsCertificates.buildCredentialedSigner((BcTlsCrypto) getCrypto(), context);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Requests an ECDSA client certificate from the relay so its certificate can be pinned. The request
     * advertises only the SHA-256-with-ECDSA signature algorithm and the {@code ecdsa_sign} certificate type,
     * matching the self-signed ECDSA P-256 certificate the relay presents, and names no certificate
     * authorities because the relay certificate is self-signed and validated by fingerprint rather than by
     * chain.
     *
     * @return the certificate request that compels the relay to present its client certificate
     */
    @Override
    public CertificateRequest getCertificateRequest() {
        var signatureAlgorithms = new Vector<SignatureAndHashAlgorithm>(1);
        signatureAlgorithms.add(new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.ecdsa));
        return new CertificateRequest(
                new short[]{ClientCertificateType.ecdsa_sign}, signatureAlgorithms, null);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Pins the relay's presented client certificate to the fixed SHA-256 fingerprint. A relay that
     * presents no certificate (an empty chain) or one whose leaf does not pin fails the handshake with a
     * fatal bad-certificate alert.
     *
     * @param clientCertificate {@inheritDoc}
     * @throws IOException if the chain is empty, the leaf certificate does not pin, or it cannot be encoded
     */
    @Override
    public void notifyClientCertificate(Certificate clientCertificate) throws IOException {
        VoipDtlsCertificates.verifyPinnedCertificate(clientCertificate, pinnedFingerprint);
    }
}
