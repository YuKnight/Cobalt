package com.github.auties00.cobalt.node.usync;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Per-protocol backoff timer for USync.
 *
 * <p>When the relay returns a per-protocol error with an {@code error_backoff}
 * attribute, the client suppresses further USync requests for that protocol
 * until the backoff window has elapsed. WhatsApp Web stores the backoff state
 * in a module-level {@code Map} keyed by protocol name. This class is the
 * Cobalt equivalent.
 *
 * <p>Backoff is consulted by {@link #waitForBackoff(UsyncQuery)} prior to
 * dispatch. {@link UsyncContext#INTERACTIVE} skips the wait entirely;
 * {@link UsyncContext#MESSAGE} and {@link UsyncContext#VOIP} exempt the
 * {@code devices} protocol because the resulting send would otherwise be
 * impossible to encrypt.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncBackoff")
public final class UsyncBackoff {
    /**
     * Logger used for backoff lifecycle messages, mirroring the
     * {@code WALogger.LOG} traces emitted by WhatsApp Web.
     */
    private static final Logger LOGGER = Logger.getLogger(UsyncBackoff.class.getName());

    /**
     * Holds the absolute expiry instant for each protocol with an active
     * backoff window.
     */
    private final ConcurrentMap<String, Instant> backoffs;

    /**
     * Creates a new, empty backoff registry.
     */
    public UsyncBackoff() {
        this.backoffs = new ConcurrentHashMap<>();
    }

    /**
     * Records a backoff window for the given protocol.
     *
     * @param protocolName the protocol name (e.g. {@code "devices"},
     *                     {@code "contact"})
     * @param backoffMs    the duration of the backoff window, in milliseconds
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncBackoff",
            exports = "setProtocolBackoffMs", adaptation = WhatsAppAdaptation.ADAPTED)
    public void setProtocolBackoffMs(String protocolName, long backoffMs) {
        var expiry = Instant.now().plusMillis(backoffMs);
        backoffs.put(protocolName, expiry);
        LOGGER.fine(() -> "usync: " + protocolName + " protocol: " + backoffMs + "ms backoff started");
    }

    /**
     * Blocks the current thread until every backoff window relevant to the
     * given query has elapsed.
     *
     * <p>Three cases short-circuit the wait. {@link UsyncContext#INTERACTIVE}
     * skips backoff entirely because the user is waiting on the result. For
     * {@link UsyncContext#MESSAGE} or {@link UsyncContext#VOIP}, the
     * {@code devices} protocol is exempt because failing here would block
     * message encryption. Protocols whose backoff windows have already elapsed
     * are removed from the map and skipped.
     *
     * @param query the query about to be dispatched
     * @throws InterruptedException if the current thread is interrupted while
     *                              sleeping
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncBackoff",
            exports = "waitForBackoff", adaptation = WhatsAppAdaptation.ADAPTED)
    public void waitForBackoff(UsyncQuery query) throws InterruptedException {
        if (query.context() == UsyncContext.INTERACTIVE) {
            return;
        }

        for (var protocol : query.protocols()) {
            var name = protocol.name();
            if ((query.context() == UsyncContext.MESSAGE || query.context() == UsyncContext.VOIP)
                    && "devices".equals(name)) {
                continue;
            }
            var expiry = backoffs.get(name);
            if (expiry == null) {
                continue;
            }
            var remaining = Duration.between(Instant.now(), expiry);
            if (remaining.isZero() || remaining.isNegative()) {
                backoffs.remove(name, expiry);
                LOGGER.fine(() -> "usync: " + name + " protocol backoff ended");
                continue;
            }
            Thread.sleep(remaining);
            backoffs.remove(name, expiry);
            LOGGER.fine(() -> "usync: " + name + " protocol backoff ended");
        }
    }

    /**
     * Removes any active backoff for the given protocol. Visible for testing
     * and for explicit invalidation paths.
     *
     * @param protocolName the protocol name
     */
    public void clear(String protocolName) {
        backoffs.remove(protocolName);
    }

    /**
     * Removes every active backoff. Visible for testing and for the logout or
     * reconnect reset path.
     */
    public void clearAll() {
        backoffs.clear();
    }
}
