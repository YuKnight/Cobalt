package com.github.auties00.cobalt.net;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * Observes the host operating system's network connectivity and reports when it
 * is present and when it transitions from absent to present.
 *
 * <p>The {@link ReconnectSupervisor} consumes a monitor to avoid two
 * pathologies of a blind backoff loop: it parks on {@link #awaitOnline()} so it
 * does not burn reconnect attempts while the machine has no network at all, and
 * it registers a {@link #start(Runnable) regained callback} so a returning
 * network triggers an immediate attempt instead of waiting out the current
 * backoff. Connectivity here means OS-level reachability, not a confirmed route
 * to WhatsApp; the connect attempt itself remains the source of truth, so a
 * monitor that reports online while the server is unreachable simply lets the
 * attempt fail and fall back to backoff.
 *
 * <p>Implementations are native and platform-specific (see
 * {@link NetworkConnectivityMonitors#systemDefault()}); the
 * {@link NoOpNetworkConnectivityMonitor} is the degradation used when no native
 * monitor can be created.
 *
 * @implSpec Implementations must be safe to call from threads other than the one
 * that runs the native event source: {@link #isOnline()}, {@link #awaitOnline()},
 * and {@link #close()} are invoked from the supervisor thread while the regained
 * callback fires from the implementation's own event thread.
 */
@WhatsAppWebModule(moduleName = "WAWebNetworkStatus")
@WhatsAppWebModule(moduleName = "WAWebEventsWaitForOffline")
public interface NetworkConnectivityMonitor extends AutoCloseable {
    /**
     * Returns whether the operating system currently reports network
     * connectivity.
     *
     * @return {@code true} if the host appears to be online, {@code false}
     *         otherwise
     */
    boolean isOnline();

    /**
     * Blocks the calling thread until the host is online, returning immediately
     * if it already is.
     *
     * @throws InterruptedException if the calling thread is interrupted while
     *         waiting (the supervisor interrupts it on cancellation)
     */
    void awaitOnline() throws InterruptedException;

    /**
     * Starts the native event source and registers the callback invoked on
     * every offline-to-online transition.
     *
     * <p>Called once per monitor, before the first {@link #awaitOnline()}. The
     * callback runs on the implementation's event thread, so it must not block.
     *
     * @param onConnectivityRegained the action to run when connectivity returns;
     *                               must not be {@code null}
     */
    void start(Runnable onConnectivityRegained);

    /**
     * Stops the native event source and releases any native and thread
     * resources, waking any thread parked in {@link #awaitOnline()}.
     *
     * <p>Idempotent: closing an already-closed monitor has no effect.
     */
    @Override
    void close();
}
