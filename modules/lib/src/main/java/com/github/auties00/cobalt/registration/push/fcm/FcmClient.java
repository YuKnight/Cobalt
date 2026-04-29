package com.github.auties00.cobalt.registration.push.fcm;

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
 * over Firebase Cloud Messaging while pretending to be the
 * {@code com.whatsapp} (or {@code com.whatsapp.w4b}) Android app.
 *
 * <p>Owns three single-responsibility collaborators and orchestrates
 * the lifecycle around them:
 * <ul>
 *   <li>{@link FcmRegistration} — runs the three-step Android
 *       registration handshake on demand;</li>
 *   <li>{@link FcmMcsConnection} — owns the long-lived MCS TLS
 *       stream and reconnect loop;</li>
 *   <li>{@link FcmPushCode} — single-value sync primitive that hands
 *       the verification code back to {@link #getPushCode()}.</li>
 * </ul>
 *
 * <p>State is owned by the caller via {@link #getSession()} and
 * {@link #loadSession(FcmSession)}; the client never touches the file
 * system. A typical usage:
 *
 * <pre>{@code
 *   try (var fcm = FcmClient.newSession()) {
 *       fcm.authenticate(device);                // device.platform() picks WA personal vs business
 *       String pushToken = fcm.getPushToken();   // hand to /v2/exist
 *       FcmSession saved = fcm.getSession();     // persist this somewhere
 *       String code = fcm.getPushCode();         // blocks for the push
 *   }
 * }</pre>
 *
 * <p>Subsequent runs skip the registration:
 *
 * <pre>{@code
 *   try (var fcm = FcmClient.loadSession(saved)) {
 *       String code = fcm.getPushCode();
 *   }
 * }</pre>
 */
public final class FcmClient implements WhatsAppDevicePushClient, AutoCloseable {
    /**
     * Cached, unmodifiable set of platforms this client can
     * authenticate against — the personal and business Android
     * variants. Returned by {@link #supportedPlatforms()}.
     */
    private static final Set<ClientPlatformType> SUPPORTED_PLATFORMS =
            Set.of(ClientPlatformType.ANDROID, ClientPlatformType.ANDROID_BUSINESS);

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
         * No session bound; only {@link #authenticate(WhatsAppDevice)}
         * and {@link #close()} are valid.
         */
        UNAUTHENTICATED,
        /**
         * {@link #authenticate(WhatsAppDevice)} is currently running on
         * another thread. Concurrent callers see this state and throw.
         */
        AUTHENTICATING,
        /**
         * Registration has succeeded; the MCS listener is running and
         * the read-only accessors are usable.
         */
        AUTHENTICATED,
        /**
         * {@link #close()} has been invoked; all accessors throw and
         * the listener thread is being torn down.
         */
        CLOSED
    }

    /**
     * Pre-built three-step registration helper bound to the
     * configured proxy. Stateless beyond its dependencies, so it can
     * be reused across retries.
     */
    private final FcmRegistration registration;

    /**
     * Single-value sync primitive that holds the verification code
     * once it arrives over MCS. Shared with {@link #connection} so
     * the MCS reader thread can hand the code straight to any
     * {@link #getPushCode()} caller.
     */
    private final FcmPushCode pushCode;

    /**
     * Lifecycle state, transitioned via
     * {@link AtomicReference#compareAndSet} so concurrent
     * {@link #authenticate(WhatsAppDevice)} callers see a consistent
     * view and only one wins the race.
     */
    private final AtomicReference<State> state;

    /**
     * Session bound during {@link #authenticate(WhatsAppDevice)} or
     * {@link #loadSession(FcmSession, URI)}. {@code null} until
     * authentication succeeds; reset to {@code null} on auth failure
     * so a retry sees a clean slate.
     */
    private volatile FcmSession session;

    /**
     * MCS connection owning the long-lived TLS stream. {@code null}
     * until authentication completes; closed and dereferenced by
     * {@link #close()}.
     */
    private volatile FcmMcsConnection connection;

    /**
     * Base constructor. Builds the I/O collaborators only; never
     * touches the network. Public callers go through the static
     * factories.
     *
     * @param proxy proxy URI, or {@code null} for direct
     */
    private FcmClient(URI proxy) {
        this.registration = new FcmRegistration(proxy);
        this.pushCode = new FcmPushCode();
        this.state = new AtomicReference<>(State.UNAUTHENTICATED);
    }

    /**
     * Creates a fresh, unauthenticated client. The caller must call
     * {@link #authenticate(WhatsAppDevice)} before any of the
     * read-only accessors become usable.
     *
     * @return a new unauthenticated client
     */
    public static FcmClient newSession() {
        return new FcmClient(null);
    }

    /**
     * Creates a fresh, unauthenticated client routed through
     * {@code proxy}.
     *
     * @param proxy proxy URI ({@code http(s)://...},
     *              {@code socks://...}), or {@code null} for direct
     * @return a new unauthenticated client
     */
    public static FcmClient newSession(URI proxy) {
        return new FcmClient(proxy);
    }

    /**
     * Restores a client from a previously captured {@link FcmSession}.
     * Re-runs FIS install if the cached auth token has expired and
     * starts the background MCS listener before returning. The
     * returned client is already in {@link State#AUTHENTICATED}, so
     * {@link #authenticate(WhatsAppDevice)} would throw.
     *
     * @param session the session previously obtained from
     *                {@link #getSession()}
     * @return a restored, listening client
     * @throws IOException if re-registration fails
     */
    public static FcmClient loadSession(FcmSession session) throws IOException {
        return loadSession(session, null);
    }

    /**
     * Restores a client from a previously captured {@link FcmSession},
     * routed through {@code proxy}.
     *
     * @param session the session previously obtained from
     *                {@link #getSession()}
     * @param proxy   proxy URI, or {@code null} for direct
     * @return a restored, listening client
     * @throws IOException if re-registration fails
     */
    public static FcmClient loadSession(FcmSession session, URI proxy) throws IOException {
        Objects.requireNonNull(session, "session");
        var client = new FcmClient(proxy);
        client.session = session;
        client.registration.ensureCredentials(session);
        client.state.set(State.AUTHENTICATED);
        client.connection = new FcmMcsConnection(session, client.pushCode);
        client.connection.start();
        return client;
    }

    /**
     * Returns the set of {@link ClientPlatformType} entries this FCM
     * client can authenticate against:
     * {@link ClientPlatformType#ANDROID} and
     * {@link ClientPlatformType#ANDROID_BUSINESS}.
     *
     * @return an unmodifiable two-element set containing the Android
     *         personal and business platforms
     */
    @Override
    public Set<ClientPlatformType> supportedPlatforms() {
        return SUPPORTED_PLATFORMS;
    }

    /**
     * Selects the {@link FcmConfig} matching {@code device.platform()}
     * ({@link FcmConfig#WHATSAPP_PERSONAL} for
     * {@link ClientPlatformType#ANDROID},
     * {@link FcmConfig#WHATSAPP_BUSINESS} for
     * {@link ClientPlatformType#ANDROID_BUSINESS}), runs the
     * three-step Android registration, and starts the background MCS
     * listener. After this call returns successfully,
     * {@link #isAuthenticated()} reports {@code true} and the
     * read-only accessors become usable.
     *
     * <p>Thread-safe via {@link AtomicReference#compareAndSet}: only
     * the first concurrent caller actually runs the registration; any
     * other caller observing {@link State#AUTHENTICATING} or
     * {@link State#AUTHENTICATED} throws
     * {@link IllegalStateException}. On failure the state reverts to
     * {@link State#UNAUTHENTICATED} so the caller may retry.
     *
     * @param device the device whose platform selects the WA config
     * @throws IllegalArgumentException if {@code device.platform()} is
     *                                  neither {@code ANDROID} nor
     *                                  {@code ANDROID_BUSINESS}
     * @throws IllegalStateException    if the client is already
     *                                  authenticating, authenticated,
     *                                  or closed
     * @throws UncheckedIOException     wrapping any HTTP or protocol
     *                                  failure
     */
    @Override
    public void authenticate(WhatsAppDevice device) {
        Objects.requireNonNull(device, "device");
        var platform = device.platform();
        var config = switch (platform) {
            case ANDROID -> FcmConfig.WHATSAPP_PERSONAL;
            case ANDROID_BUSINESS -> FcmConfig.WHATSAPP_BUSINESS;
            default -> throw new IllegalArgumentException(
                    "FcmClient.authenticate requires ANDROID or ANDROID_BUSINESS, got " + platform);
        };
        if (!state.compareAndSet(State.UNAUTHENTICATED, State.AUTHENTICATING)) {
            throw new IllegalStateException("FcmClient already " + state.get());
        }
        try {
            this.session = FcmSession.newSession(config);
            registration.ensureCredentials(session);
            this.connection = new FcmMcsConnection(session, pushCode);
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
     * {@link #loadSession(FcmSession)} factory) has placed the client
     * in {@link State#AUTHENTICATED}.
     *
     * @return {@code true} iff the client is authenticated and the
     *         MCS listener is running
     */
    @Override
    public boolean isAuthenticated() {
        return state.get() == State.AUTHENTICATED;
    }

    /**
     * Returns the live {@link FcmSession} backing this client.
     * Callers may pass it back to {@link #loadSession(FcmSession)} on
     * a future run, or hand it to
     * {@code FcmSessionSpec.encode(...)} themselves for byte-level
     * persistence.
     *
     * <p>The returned object is mutated by the MCS reader thread as
     * persistent ids arrive. Callers that serialise the session
     * concurrently with an active MCS connection should snapshot the
     * persistent-id list first.
     *
     * @return the live session
     * @throws IllegalStateException if the client is not authenticated
     */
    public FcmSession getSession() {
        requireAuthenticated();
        return session;
    }

    /**
     * Returns the FCM registration token established during
     * authentication. Idempotent and non-blocking.
     *
     * @return the FCM push token
     * @throws IllegalStateException if the client is not authenticated
     */
    @Override
    public String getPushToken() {
        requireAuthenticated();
        return session.fcmToken();
    }

    /**
     * Returns the WhatsApp verification code carried by the silent
     * FCM data push WhatsApp's server emits in response to the
     * {@code /v2/exist} call. Returns immediately if the push has
     * already arrived, or blocks the calling thread until either the
     * push arrives or {@link #close()} is invoked.
     *
     * <p>Safe to call from multiple threads concurrently; every
     * caller sees the same delivered code, since the WhatsApp
     * registration flow only ever sends one per session.
     *
     * @return the verification code from
     *         {@code app_data.registration_code}
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
            throw new RuntimeException("FcmClient.getPushCode interrupted", e);
        }
    }

    /**
     * Throws if the client is not in {@link State#AUTHENTICATED}.
     * Used to guard accessors that depend on a populated
     * {@link #session} and a running MCS listener.
     */
    private void requireAuthenticated() {
        var current = state.get();
        if (current != State.AUTHENTICATED) {
            throw new IllegalStateException(
                    "FcmClient must be authenticated first; current state=" + current);
        }
    }

    /**
     * Stops the background MCS reader and heartbeat threads, tears
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
