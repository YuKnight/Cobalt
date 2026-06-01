package com.github.auties00.cobalt.net;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the connectivity-monitor factory and the no-op fallback. The native
 * Windows monitor is exercised only on Windows; other platforms currently fall
 * back to the no-op monitor.
 */
@DisplayName("NetworkConnectivityMonitors")
class NetworkConnectivityMonitorsTest {
    @Test
    @DisplayName("systemDefault returns a usable monitor on every platform")
    void systemDefaultIsUsable() {
        var monitor = NetworkConnectivityMonitors.systemDefault();
        assertNotNull(monitor, "factory must never return null");
        assertDoesNotThrow(() -> {
            monitor.start(() -> {});
            monitor.isOnline();
            monitor.close();
        });
    }

    @Test
    @DisplayName("the no-op monitor is always online and never blocks")
    void noOpIsAlwaysOnline() throws InterruptedException {
        var monitor = new NoOpNetworkConnectivityMonitor();
        monitor.start(() -> {});
        assertTrue(monitor.isOnline(), "no-op monitor is always online");
        monitor.awaitOnline();
        monitor.close();
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("Windows reports a connectivity state without throwing")
    void windowsMonitorSmoke() {
        var monitor = NetworkConnectivityMonitors.systemDefault();
        assertDoesNotThrow(() -> {
            monitor.start(() -> {});
            // Allow the event thread to run its initial probe
            Thread.sleep(200);
            monitor.isOnline();
            monitor.close();
        });
    }
}
