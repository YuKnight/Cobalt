package com.github.auties00.cobalt.net;

/**
 * In-memory {@link NetworkConnectivityMonitor} whose online state is driven
 * programmatically by {@link #setOnline(boolean)}, letting supervisor tests
 * exercise offline gating and the offline-to-online edge without any native
 * code.
 */
final class TestNetworkConnectivityMonitor implements NetworkConnectivityMonitor {
    private final Object lock = new Object();
    private volatile boolean online;
    private volatile Runnable regained;

    TestNetworkConnectivityMonitor(boolean initiallyOnline) {
        this.online = initiallyOnline;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void awaitOnline() throws InterruptedException {
        synchronized (lock) {
            while (!online) {
                lock.wait();
            }
        }
    }

    @Override
    public void start(Runnable onConnectivityRegained) {
        this.regained = onConnectivityRegained;
    }

    @Override
    public void close() {
        // No resources to release
    }

    /**
     * Sets the simulated online state, waking await-online waiters and firing
     * the regained callback on an offline-to-online edge.
     *
     * @param now the new online state
     */
    void setOnline(boolean now) {
        Runnable callback = null;
        synchronized (lock) {
            var was = online;
            online = now;
            if (now) {
                lock.notifyAll();
                if (!was) {
                    callback = regained;
                }
            }
        }
        if (callback != null) {
            callback.run();
        }
    }
}
