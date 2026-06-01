package com.github.auties00.cobalt.net;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Selects the {@link NetworkConnectivityMonitor} implementation for the running
 * operating system.
 *
 * <p>Each native monitor is constructed inside a guarded block so a missing
 * system library, an unresolved symbol, or an unsupported platform degrades to
 * the {@link NoOpNetworkConnectivityMonitor} (reconnection still works through
 * backoff, just without connectivity gating) rather than failing the client.
 * Only the chosen implementation class is referenced from its branch, so a
 * monitor that links against another platform's system symbols is never loaded
 * on the wrong OS.
 */
public final class NetworkConnectivityMonitors {
    /**
     * Prevents instantiation of this static factory.
     */
    private NetworkConnectivityMonitors() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns a connectivity monitor appropriate for the current operating
     * system, or a {@link NoOpNetworkConnectivityMonitor} when none can be
     * constructed.
     *
     * @return a started-on-demand connectivity monitor; never {@code null}
     */
    public static NetworkConnectivityMonitor systemDefault() {
        var os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return createOrNoOp(WindowsNetworkConnectivityMonitor::new);
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return createOrNoOp(MacosNetworkConnectivityMonitor::new);
        }
        if (os.contains("nux") || os.contains("nix") || os.contains("aix")) {
            return createOrNoOp(LinuxNetworkConnectivityMonitor::new);
        }
        return new NoOpNetworkConnectivityMonitor();
    }

    /**
     * Constructs the given native monitor, falling back to the no-op monitor if
     * its system library or symbols cannot be resolved on this host.
     *
     * @param factory the native monitor constructor
     * @return the native monitor, or a no-op monitor on failure
     */
    private static NetworkConnectivityMonitor createOrNoOp(Supplier<NetworkConnectivityMonitor> factory) {
        try {
            return factory.get();
        } catch (Throwable throwable) {
            System.getLogger(NetworkConnectivityMonitors.class.getName())
                    .log(System.Logger.Level.WARNING, "Native connectivity monitor unavailable; reconnection falls back to backoff only", throwable);
            return new NoOpNetworkConnectivityMonitor();
        }
    }
}
