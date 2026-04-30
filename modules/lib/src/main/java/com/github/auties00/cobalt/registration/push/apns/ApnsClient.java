package com.github.auties00.cobalt.registration.push.apns;

import com.github.auties00.cobalt.client.WhatsAppDevice;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.client.WhatsAppDevicePushClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Public façade for receiving WhatsApp's silent verification push
 * over Apple Push Notification service while pretending to be the
 * {@code net.whatsapp.WhatsApp} (or {@code net.whatsapp.WhatsAppSMB})
 * iOS app.
 *
 * <p>Owns three single-responsibility collaborators and orchestrates
 * the lifecycle around them:
 * <ul>
 *   <li>{@link ApnsActivation} runs the FairPlay-signed
 *       activation handshake against {@code albert.apple.com} on
 *       demand.</li>
 *   <li>{@link ApnsCourierConnection} owns the long-lived TLS
 *       courier stream, the request multiplexer, and the keep-alive
 *       loop.</li>
 *   <li>{@link ApnsPushCode} is the single-value sync primitive that
 *       hands the verification code back to
 *       {@link #getPushCode()}.</li>
 * </ul>
 *
 * <p>State is owned by the caller via {@link #getSession()} and
 * {@link #loadSession(ApnsSession)}. The client never touches the
 * file system. A typical usage:
 *
 * <pre>{@code
 *   try (var apns = ApnsClient.newSession()) {
 *       apns.authenticate(device);                // device.platform() picks WA personal vs business
 *       String pushToken = apns.getPushToken();   // hand to /v2/exist
 *       ApnsSession saved = apns.getSession();    // persist this somewhere
 *       String code = apns.getPushCode();         // blocks for the push
 *   }
 * }</pre>
 *
 * <p>Subsequent runs reuse the activation cert and skip the
 * {@code albert.apple.com} round-trip:
 *
 * <pre>{@code
 *   try (var apns = ApnsClient.loadSession(saved)) {
 *       String code = apns.getPushCode();
 *   }
 * }</pre>
 */
public final class ApnsClient implements WhatsAppDevicePushClient, AutoCloseable {
    /**
     * Cached, unmodifiable set of platforms this client can
     * authenticate against, namely the personal and business iOS
     * variants. Returned by {@link #supportedPlatforms()}.
     */
    private static final Set<ClientPlatformType> SUPPORTED_PLATFORMS =
            Set.of(ClientPlatformType.IOS, ClientPlatformType.IOS_BUSINESS);

    /**
     * Lifecycle states of the client. Transitions:
     * <pre>
     *   UNAUTHENTICATED --authenticate()--> AUTHENTICATING --ok-->   AUTHENTICATED
     *                                                       \--err->  UNAUTHENTICATED
     *   any            --close()-------> CLOSED
     * </pre>
     */
    private enum State {
        /**
         * No session bound. Only {@link #authenticate(WhatsAppDevice)}
         * and {@link #close()} are valid.
         */
        UNAUTHENTICATED,
        /**
         * {@link #authenticate(WhatsAppDevice)} is currently running
         * on another thread. Concurrent callers see this state and
         * throw.
         */
        AUTHENTICATING,
        /**
         * Activation has succeeded and the courier is running. The
         * read-only accessors are usable.
         */
        AUTHENTICATED,
        /**
         * {@link #close()} has been invoked. All accessors throw and
         * the courier is being torn down.
         */
        CLOSED
    }

    /**
     * Pre-built activation helper bound to the configured proxy.
     * Stateless beyond its dependencies, so it can be reused across
     * retries.
     */
    private final ApnsActivation activation;

    /**
     * Single-value sync primitive that holds the verification code
     * once it arrives over the courier stream. Shared with
     * {@link #connection} so the read pump can hand the code
     * straight to any {@link #getPushCode()} caller.
     */
    private final ApnsPushCode pushCode;

    /**
     * Proxy URI handed to {@link #connection} when it is created in
     * {@link #authenticate(WhatsAppDevice)} or
     * {@link #loadSession(ApnsSession, URI)}. {@code null} for
     * direct connections.
     */
    private final URI proxy;

    /**
     * Lifecycle state, transitioned via
     * {@link AtomicReference#compareAndSet} so concurrent
     * {@link #authenticate(WhatsAppDevice)} callers see a consistent
     * view and only one wins the race.
     */
    private final AtomicReference<State> state;

    /**
     * Session bound during {@link #authenticate(WhatsAppDevice)} or
     * {@link #loadSession(ApnsSession, URI)}. {@code null} until
     * authentication succeeds. Reset to {@code null} on auth failure
     * so a retry sees a clean slate.
     */
    private volatile ApnsSession session;

    /**
     * Courier connection owning the long-lived TLS stream.
     * {@code null} until authentication completes. Closed and
     * dereferenced by {@link #close()}.
     */
    private volatile ApnsCourierConnection connection;

    /**
     * Base constructor. Builds the activation helper and the
     * push-code holder. No socket is opened. Public callers go
     * through the static factories.
     *
     * @param proxy proxy URI, or {@code null} for direct
     */
    private ApnsClient(URI proxy) {
        this.proxy = proxy;
        this.activation = new ApnsActivation(proxy);
        this.pushCode = new ApnsPushCode();
        this.state = new AtomicReference<>(State.UNAUTHENTICATED);
    }

    /**
     * Creates a fresh, unauthenticated client. The caller must call
     * {@link #authenticate(WhatsAppDevice)} before any of the
     * read-only accessors become usable.
     *
     * @return a new unauthenticated client
     */
    public static ApnsClient newSession() {
        return new ApnsClient(null);
    }

    /**
     * Creates a fresh, unauthenticated client routed through
     * {@code proxy}.
     *
     * @param proxy proxy URI ({@code http(s)://...},
     *              {@code socks://...}), or {@code null} for direct
     * @return a new unauthenticated client
     */
    public static ApnsClient newSession(URI proxy) {
        return new ApnsClient(proxy);
    }

    /**
     * Restores a client from a previously captured
     * {@link ApnsSession}. Skips the activation handshake (the
     * device certificate persists for ~3 years) and immediately
     * starts the courier connection. The returned client is already
     * in {@link State#AUTHENTICATED}, so
     * {@link #authenticate(WhatsAppDevice)} would throw.
     *
     * @param session the session previously obtained from
     *                {@link #getSession()}
     * @return a restored, listening client
     * @throws IOException if the courier handshake fails
     */
    public static ApnsClient loadSession(ApnsSession session) throws IOException {
        return loadSession(session, null);
    }

    /**
     * Restores a client from a previously captured
     * {@link ApnsSession}, routed through {@code proxy}.
     *
     * @param session the session previously obtained from
     *                {@link #getSession()}
     * @param proxy   proxy URI, or {@code null} for direct
     * @return a restored, listening client
     * @throws IOException if the courier handshake fails
     */
    public static ApnsClient loadSession(ApnsSession session, URI proxy) throws IOException {
        Objects.requireNonNull(session, "session");
        var client = new ApnsClient(proxy);
        client.session = session;
        client.activation.activate(session);
        client.connection = new ApnsCourierConnection(session, client.pushCode, proxy);
        client.connection.start();
        client.state.set(State.AUTHENTICATED);
        return client;
    }

    /**
     * Returns the set of {@link ClientPlatformType} entries this APNS
     * client can authenticate against:
     * {@link ClientPlatformType#IOS} and
     * {@link ClientPlatformType#IOS_BUSINESS}.
     *
     * @return an unmodifiable two-element set containing the iOS
     *         personal and business platforms
     */
    @Override
    public Set<ClientPlatformType> supportedPlatforms() {
        return SUPPORTED_PLATFORMS;
    }

    /**
     * Selects the {@link ApnsConfig} matching {@code device.platform()}
     * ({@link ApnsConfig#WHATSAPP_PERSONAL} for
     * {@link ClientPlatformType#IOS},
     * {@link ApnsConfig#WHATSAPP_BUSINESS} for
     * {@link ClientPlatformType#IOS_BUSINESS}), runs the activation
     * handshake, and starts the courier connection. After this call
     * returns successfully, {@link #isAuthenticated()} reports
     * {@code true} and the read-only accessors become usable.
     *
     * <p>Thread-safe via {@link AtomicReference#compareAndSet}: only
     * the first concurrent caller actually runs the activation. Any
     * other caller observing {@link State#AUTHENTICATING} or
     * {@link State#AUTHENTICATED} throws
     * {@link IllegalStateException}. On failure the state reverts to
     * {@link State#UNAUTHENTICATED} so the caller may retry.
     *
     * @param device the device whose platform selects the WA config
     * @throws IllegalArgumentException if {@code device.platform()}
     *                                  is neither {@code IOS} nor
     *                                  {@code IOS_BUSINESS}
     * @throws IllegalStateException    if the client is already
     *                                  authenticating, authenticated,
     *                                  or closed
     * @throws UncheckedIOException     wrapping any HTTP, TLS, or
     *                                  protocol failure
     */
    @Override
    public void authenticate(WhatsAppDevice device) {
        Objects.requireNonNull(device, "device");
        var platform = device.platform();
        var config = switch (platform) {
            case IOS -> ApnsConfig.WHATSAPP_PERSONAL;
            case IOS_BUSINESS -> ApnsConfig.WHATSAPP_BUSINESS;
            default -> throw new IllegalArgumentException(
                    "ApnsClient.authenticate requires IOS or IOS_BUSINESS, got " + platform);
        };
        if (!state.compareAndSet(State.UNAUTHENTICATED, State.AUTHENTICATING)) {
            throw new IllegalStateException("ApnsClient already " + state.get());
        }
        try {
            this.session = ApnsSession.newSession(config);
            activation.activate(session);
            this.connection = new ApnsCourierConnection(session, pushCode, proxy);
            this.connection.start();
            state.set(State.AUTHENTICATED);
        } catch (IOException e) {
            rollbackAuthentication();
            throw new UncheckedIOException(e);
        } catch (RuntimeException | Error e) {
            rollbackAuthentication();
            throw e;
        }
    }

    /**
     * Wipes the partial state left by a failed
     * {@link #authenticate(WhatsAppDevice)} attempt and reverts the
     * lifecycle back to {@link State#UNAUTHENTICATED} so the caller
     * may retry.
     */
    private void rollbackAuthentication() {
        this.session = null;
        this.connection = null;
        state.set(State.UNAUTHENTICATED);
    }

    /**
     * Reports whether {@link #authenticate(WhatsAppDevice)} (or the
     * {@link #loadSession(ApnsSession)} factory) has placed the
     * client in {@link State#AUTHENTICATED}.
     *
     * @return {@code true} iff the client is authenticated and the
     *         courier is running
     */
    @Override
    public boolean isAuthenticated() {
        return state.get() == State.AUTHENTICATED;
    }

    /**
     * Returns the live {@link ApnsSession} backing this client.
     * Callers may pass it back to {@link #loadSession(ApnsSession)}
     * on a future run, or hand it to
     * {@code ApnsSessionSpec.encode(...)} themselves for byte-level
     * persistence.
     *
     * @return the live session
     * @throws IllegalStateException if the client is not authenticated
     */
    public ApnsSession getSession() {
        requireAuthenticated();
        return session;
    }

    /**
     * Requests the push token for the primary topic of the
     * configured WhatsApp flavor. The first entry in
     * {@code session.config().topics()} (e.g.
     * {@code "net.whatsapp.WhatsApp"} for personal,
     * {@code "net.whatsapp.WhatsAppSMB"} for business).
     *
     * <p>Blocking. Safe to call repeatedly. Subsequent calls reuse
     * the existing courier connection and return quickly.
     *
     * @return the hex-encoded push token bytes
     * @throws UncheckedIOException  wrapping any courier or protocol
     *                               failure
     * @throws IllegalStateException if the client is not authenticated
     */
    @Override
    public String getPushToken() {
        requireAuthenticated();
        var topics = session.config().topics();
        if (topics.isEmpty()) {
            throw new IllegalStateException("ApnsClient session has no topics");
        }
        try {
            return connection.requestToken(topics.get(0));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns the WhatsApp verification code carried by the silent
     * APNS notification WhatsApp's server emits in response to the
     * {@code /v2/exist} call. Returns immediately if the
     * notification has already arrived, or blocks the calling
     * thread until either the notification arrives or
     * {@link #close()} is invoked.
     *
     * <p>Safe to call from multiple threads concurrently. Every
     * caller sees the same delivered code, since the WhatsApp
     * registration flow only ever sends one per session.
     *
     * @return the verification code from the notification's
     *         {@code regcode} JSON field
     * @throws UncheckedIOException  if the client has been closed
     *                               before a code was delivered
     * @throws RuntimeException      if the caller is interrupted
     *                               while waiting (the interrupt
     *                               flag is restored before the
     *                               exception is thrown)
     * @throws IllegalStateException if the client is not authenticated
     */
    @Override
    public String getPushCode() {
        requireAuthenticated();
        try {
            return pushCode.waitForCode();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("ApnsClient.getPushCode interrupted", e);
        }
    }

    /**
     * Throws if the client is not in {@link State#AUTHENTICATED}.
     * Used to guard accessors that depend on a populated
     * {@link #session} and a running courier connection.
     */
    private void requireAuthenticated() {
        var current = state.get();
        if (current != State.AUTHENTICATED) {
            throw new IllegalStateException(
                    "ApnsClient must be authenticated first; current state=" + current);
        }
    }

    /**
     * Stops the courier read pump and keep-alive threads, tears
     * down the TLS socket, transitions the lifecycle to
     * {@link State#CLOSED}, and unblocks every pending
     * {@link #getPushCode()} caller. Idempotent.
     */
    @Override
    public void close() {
        var prev = state.getAndSet(State.CLOSED);
        if (prev == State.CLOSED) {
            return;
        }
        var c = connection;
        if (c != null) {
            c.close();
        }
        pushCode.close();
    }
}
