package com.github.auties00.cobalt.net;

/**
 * A {@link NetworkConnectivityMonitor} that always reports the host as online
 * and never signals a connectivity change.
 *
 * <p>This is the degradation used by {@link NetworkConnectivityMonitors} when no
 * native monitor can be constructed for the running platform (an unsupported
 * operating system, a missing system symbol, or a Windows build predating the
 * connectivity-hint API). With this monitor the {@link ReconnectSupervisor}
 * still recovers from drops through its unbounded backoff loop; it simply loses
 * the ability to park while the machine is offline and to reconnect the instant
 * the network returns.
 *
 * @implNote It is deliberately not a pure-Java {@code NetworkInterface} poller:
 * the design calls for native detection only, and an always-online stub keeps
 * the supervisor's backoff behaviour correct without pretending to observe the
 * OS.
 */
final class NoOpNetworkConnectivityMonitor implements NetworkConnectivityMonitor {
    /**
     * Constructs the no-op monitor.
     */
    NoOpNetworkConnectivityMonitor() {
        // No state to initialise
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code true}
     */
    @Override
    public boolean isOnline() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns immediately because this monitor is always online.
     */
    @Override
    public void awaitOnline() {
        // Always online: never blocks
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ignores the callback because connectivity changes are never observed.
     */
    @Override
    public void start(Runnable onConnectivityRegained) {
        // No event source to start
    }

    /**
     * {@inheritDoc}
     *
     * <p>No-op: there are no resources to release.
     */
    @Override
    public void close() {
        // Nothing to close
    }
}
