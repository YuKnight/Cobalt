package com.github.auties00.cobalt.socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Produces {@link SSLEngine} instances configured to mimic Chrome's TLS
 * client hello so JA3-fingerprinting endpoints (including WhatsApp) do
 * not reject the connection.
 *
 * <p>The engine advertises ALPN {@code http/1.1}, enables HTTPS
 * hostname verification, and pins the cipher suite ordering to the list
 * Chrome 136 advertises. Chrome's GREASE entries are intentionally
 * omitted because the JDK does not support them.
 */
final class ChromeSslEngineFactory implements WhatsAppSslEngineFactory {
    /**
     * Cipher suite ordering captured from a live Chrome 136 instance via
     * {@code https://www.howsmyssl.com/a/check}.
     *
     * <p>Servers that JA3-fingerprint the client hello reject any
     * ordering that does not match a known browser, so the order here
     * is load-bearing and must not be sorted.
     */
    private static final String[] CHROME_CIPHER_SUITES = {
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
            "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_256_CBC_SHA"
    };

    /**
     * The shared singleton instance.
     */
    static final ChromeSslEngineFactory INSTANCE = new ChromeSslEngineFactory();

    static {
        // Java 25 disables TLS_RSA_* by default because it lacks forward secrecy,
        // but Chrome still offers it. Re-enabling it is required to keep the
        // advertised cipher list identical to Chrome's JA3 fingerprint.
        var disabled = Security.getProperty("jdk.tls.disabledAlgorithms");
        if (disabled != null && disabled.contains("TLS_RSA_*")) {
            var updated = Arrays.stream(disabled.split(","))
                    .map(String::trim)
                    .filter(entry -> !entry.equals("TLS_RSA_*"))
                    .collect(Collectors.joining(", "));
            Security.setProperty("jdk.tls.disabledAlgorithms", updated);
        }

    }

    /**
     * Prevents external instantiation; callers obtain the factory through
     * {@link #INSTANCE}.
     */
    private ChromeSslEngineFactory() {

    }

    /**
     * Creates a Chrome-style {@link SSLEngine} bound to the given peer.
     *
     * @param address the remote endpoint used to seed SNI and hostname
     *                verification
     * @return a client-mode engine configured with Chrome's ALPN and
     *         cipher suite ordering
     * @throws IllegalStateException if the JDK cannot provide a TLS
     *         {@link SSLContext}
     */
    @Override
    public SSLEngine createSSLEngine(InetSocketAddress address) {
        try {
            var sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            var engine = sslContext.createSSLEngine(address.getHostString(), address.getPort());
            engine.setUseClientMode(true);
            var params = engine.getSSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            params.setApplicationProtocols(new String[]{"http/1.1"});
            params.setCipherSuites(CHROME_CIPHER_SUITES);
            engine.setSSLParameters(params);
            return engine;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Failed to create SSL context", e);
        }
    }
}
