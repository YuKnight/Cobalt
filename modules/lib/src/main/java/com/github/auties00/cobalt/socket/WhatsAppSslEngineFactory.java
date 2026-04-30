package com.github.auties00.cobalt.socket;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;

/**
 * Creates {@link SSLEngine} instances configured for outbound WhatsApp
 * socket connections.
 *
 * <p>The factory abstraction lets the socket layer pick a Chrome-like
 * engine for endpoints behind JA3 fingerprinting and a stock JDK engine
 * for ordinary peers without leaking the choice into the caller.
 */
@FunctionalInterface
public interface WhatsAppSslEngineFactory {
    /**
     * Creates an {@link SSLEngine} configured for the given peer address.
     *
     * @param address the remote endpoint, used for hostname verification
     *                and the SNI extension
     * @return a configured {@link SSLEngine} in client mode
     */
    SSLEngine createSSLEngine(InetSocketAddress address);

    /**
     * Returns the default Chrome-like factory that reproduces the cipher
     * suite ordering Chrome advertises so JA3-fingerprinting endpoints
     * accept the connection.
     *
     * @return the singleton Chrome-style factory
     */
    static WhatsAppSslEngineFactory chrome() {
        return ChromeSslEngineFactory.INSTANCE;
    }
}
