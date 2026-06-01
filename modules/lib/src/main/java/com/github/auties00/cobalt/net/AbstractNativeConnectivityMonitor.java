package com.github.auties00.cobalt.net;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for native {@link NetworkConnectivityMonitor}s that learn of
 * connectivity changes by blocking on an operating-system event source and then
 * re-querying the current state.
 *
 * <p>This factors out the parts shared by every platform: the {@code online}
 * flag and its monitor, {@link #awaitOnline()}, the offline-to-online edge
 * detection that fires the regained callback, and the single daemon event
 * thread. A subclass supplies only the native specifics through
 * {@link #runEventLoop()} (open the event source, then loop blocking on a change
 * and calling {@link #setOnline(boolean)}) and {@link #stopEventSource()}
 * (best-effort unblock and cleanup on {@link #close()}).
 *
 * <p>The event thread is a platform thread, not a virtual one, because it spends
 * the session blocked inside a native downcall and must not pin a Loom carrier;
 * it is a daemon so a thread still parked in an uninterruptible native call
 * (for example Windows {@code NotifyAddrChange}) cannot keep the JVM alive.
 *
 * @implNote The {@code online} flag starts optimistically {@code true} so that
 * {@link #awaitOnline()} does not block before the subclass has run its first
 * probe; the first {@link #setOnline(boolean)} inside {@link #runEventLoop()}
 * corrects it. The regained callback runs outside the monitor lock so it cannot
 * deadlock against supervisor code that calls back into this monitor.
 */
abstract class AbstractNativeConnectivityMonitor implements NetworkConnectivityMonitor {
    /**
     * Monitor guarding {@link #online} and backing {@link #awaitOnline()}.
     */
    private final Object lock = new Object();

    /**
     * Name given to the daemon event thread, for diagnostics.
     */
    private final String threadName;

    /**
     * Set once {@link #close()} has run so the event loop and waiters can stop.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Whether the host currently appears online; guarded by {@link #lock} for
     * writes, read without locking via the {@code volatile} semantics.
     */
    private volatile boolean online = true;

    /**
     * Callback invoked on every offline-to-online transition, supplied by
     * {@link #start(Runnable)}.
     */
    private Runnable onConnectivityRegained;

    /**
     * Constructs a base monitor whose event thread carries the given name.
     *
     * @param threadName the diagnostic name for the daemon event thread
     */
    AbstractNativeConnectivityMonitor(String threadName) {
        this.threadName = threadName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isOnline() {
        return online;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void awaitOnline() throws InterruptedException {
        synchronized (lock) {
            while (!online && !closed.get()) {
                lock.wait();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void start(Runnable onConnectivityRegained) {
        this.onConnectivityRegained = Objects.requireNonNull(onConnectivityRegained, "onConnectivityRegained cannot be null");
        Thread.ofPlatform()
                .name(threadName)
                .daemon(true)
                .start(() -> {
                    try {
                        runEventLoop();
                    } catch (Throwable throwable) {
                        if (!closed.get()) {
                            System.getLogger(getClass().getName())
                                    .log(System.Logger.Level.WARNING, "Native connectivity monitor stopped; reconnection falls back to backoff only", throwable);
                        }
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        synchronized (lock) {
            lock.notifyAll();
        }
        try {
            stopEventSource();
        } catch (Throwable _) {
            // Best-effort: close must not throw
        }
    }

    /**
     * Returns whether {@link #close()} has been called.
     *
     * @return {@code true} once closed; subclasses poll this to exit
     *         {@link #runEventLoop()}
     */
    protected final boolean isClosed() {
        return closed.get();
    }

    /**
     * Records the latest probed connectivity state, waking
     * {@link #awaitOnline()} waiters and firing the regained callback on an
     * offline-to-online edge.
     *
     * <p>Subclasses call this from {@link #runEventLoop()} after each native
     * change (and once for the initial probe).
     *
     * @param now the freshly probed online state
     */
    protected final void setOnline(boolean now) {
        Runnable regained = null;
        synchronized (lock) {
            var was = online;
            online = now;
            if (now) {
                lock.notifyAll();
                if (!was) {
                    regained = onConnectivityRegained;
                }
            }
        }
        if (regained != null) {
            regained.run();
        }
    }

    /**
     * Runs the native event loop: opens the OS event source, performs an
     * initial {@link #setOnline(boolean) probe}, then blocks on each
     * connectivity change and re-probes until {@link #isClosed()} is true.
     *
     * @throws Exception if the native event source cannot be opened or read
     */
    protected abstract void runEventLoop() throws Exception;

    /**
     * Best-effort unblocks and releases the native event source, invoked by
     * {@link #close()}.
     *
     * <p>May be a no-op when the underlying native call cannot be interrupted;
     * in that case the daemon event thread exits on its next natural wakeup.
     */
    protected abstract void stopEventSource();
}
