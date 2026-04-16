package com.github.auties00.cobalt.client;

/**
 * Enumerates the reasons for which a WhatsApp session can be terminated.
 *
 * <p>A {@code WhatsAppClientDisconnectReason} is supplied whenever the
 * {@link WhatsAppClient} tears down a connection, either autonomously (for
 * example on server-side logout or ban) or in response to an explicit user
 * request. Listeners registered via
 * {@link WhatsAppClient#addListener(WhatsAppClientListener)} receive the
 * reason through
 * {@link WhatsAppClientListener#onDisconnected(WhatsAppClient, WhatsAppClientDisconnectReason)}
 * and should use it to drive their reconnection and persistence logic.
 *
 * <p>The reason also feeds into the {@link WhatsAppClientErrorHandler.Result}
 * mapping: a handler returning {@code LOG_OUT} disconnects with
 * {@link #LOGGED_OUT}, {@code BAN} with {@link #BANNED},
 * {@code RECONNECT} with {@link #RECONNECTING}, and {@code DISCONNECT}
 * with {@link #DISCONNECTED}.
 *
 * @see WhatsAppClient#disconnect(WhatsAppClientDisconnectReason)
 * @see WhatsAppClientListener#onDisconnected(WhatsAppClient, WhatsAppClientDisconnectReason)
 * @see WhatsAppClientErrorHandler
 */
public enum WhatsAppClientDisconnectReason {
    /**
     * Signals a normal disconnection initiated by the user or the library.
     *
     * <p>This is the default reason when no specific cause applies, for
     * instance when the application calls {@link WhatsAppClient#disconnect()}
     * directly or when the library decides to tear down the session without
     * intending to reconnect or clear credentials.
     */
    DISCONNECTED,

    /**
     * Signals that the session is being torn down so that a fresh
     * connection can immediately be established.
     *
     * <p>Typical triggers include recoverable stream errors and
     * socket-level close events raised by
     * {@link com.github.auties00.cobalt.socket.WhatsAppSocketClient}. The
     * {@link WhatsAppClient} re-establishes the connection automatically
     * after emitting this reason to listeners.
     */
    RECONNECTING,

    /**
     * Signals that the account has logged out of WhatsApp.
     *
     * <p>This can be the result of an explicit {@link WhatsAppClient#logout()}
     * call or of a server-initiated unpair. Session credentials are deleted
     * from the store so re-authentication is required to use the account
     * again.
     */
    LOGGED_OUT,

    /**
     * Signals that the account has been banned from WhatsApp.
     *
     * <p>This is a terminal state: reconnection attempts would be refused
     * and the user must contact WhatsApp support to recover the account. The
     * session store is deleted when this reason is emitted.
     */
    BANNED
}
