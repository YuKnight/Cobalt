package com.github.auties00.cobalt.calls2.net.transport;

import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.CipherSuite;
import org.bouncycastle.tls.DefaultTlsClient;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsCredentials;
import org.bouncycastle.tls.TlsServerCertificate;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;

import java.io.IOException;
import java.util.Objects;

/**
 * The BouncyCastle DTLS client driving the WhatsApp Web relay leg's DTLS handshake.
 *
 * <p>The relay path runs DTLS with the relay as the server; this client presents a freshly generated
 * self-signed ECDSA P-256 certificate (WebRTC requires the client to authenticate too) and pins the
 * relay's server certificate to a fixed SHA-256 fingerprint rather than expecting a per-call signaled
 * {@code <certificate>}. It offers DTLS 1.2 and the ECDHE-ECDSA AES-GCM cipher suites WebRTC negotiates.
 *
 * <p>The complementary {@link VoipDtlsServer} drives the inverse role for the relay block's defensive
 * {@code enable_edgeray_dtls_active_mode}; both peers share {@link VoipDtlsCertificates} for the
 * certificate generation and the fingerprint pin so the two roles cannot drift apart.
 *
 * @implNote This implementation realises the DTLS client side of WhatsApp Web's synthesized relay answer:
 *           the answer carries {@code a=setup:passive} so the relay is the DTLS server and the web client is
 *           the DTLS client, and a hardcoded {@code a=fingerprint:sha-256} the client pins. The certificate
 *           generation and the pin check are delegated to {@link VoipDtlsCertificates}, the holder shared
 *           with {@link VoipDtlsServer}.
 */
final class VoipDtlsClient extends DefaultTlsClient {
    /**
     * The length, in bytes, of a SHA-256 certificate fingerprint.
     */
    static final int SHA256_FINGERPRINT_LENGTH = VoipDtlsCertificates.SHA256_FINGERPRINT_LENGTH;

    /**
     * The pinned SHA-256 fingerprint the relay server certificate is verified against.
     */
    private final byte[] pinnedFingerprint;

    /**
     * Constructs the relay DTLS client over a fresh pure-Java crypto context and the pinned fingerprint.
     *
     * @param pinnedFingerprint the SHA-256 fingerprint the relay certificate is pinned to, thirty-two raw
     *                          digest bytes
     * @throws NullPointerException     if {@code pinnedFingerprint} is {@code null}
     * @throws IllegalArgumentException if {@code pinnedFingerprint} is not exactly thirty-two bytes
     */
    VoipDtlsClient(byte[] pinnedFingerprint) {
        super(new BcTlsCrypto(RelayDataChannel.secureRandom()));
        Objects.requireNonNull(pinnedFingerprint, "pinnedFingerprint cannot be null");
        if (pinnedFingerprint.length != SHA256_FINGERPRINT_LENGTH) {
            throw new IllegalArgumentException(
                    "pinned fingerprint must be " + SHA256_FINGERPRINT_LENGTH + " bytes, got " + pinnedFingerprint.length);
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
     * @return the ECDHE-ECDSA AES-GCM cipher suites, matching the relay's self-signed ECDSA certificate
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
     * @return a {@link TlsAuthentication} that pins the server certificate and presents the client
     * certificate
     */
    @Override
    public TlsAuthentication getAuthentication() {
        return new TlsAuthentication() {
            @Override
            public void notifyServerCertificate(TlsServerCertificate serverCertificate) throws IOException {
                VoipDtlsCertificates.verifyPinnedCertificate(serverCertificate.getCertificate(), pinnedFingerprint);
            }

            @Override
            public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
                return VoipDtlsCertificates.buildCredentialedSigner((BcTlsCrypto) getCrypto(), context);
            }
        };
    }

}
