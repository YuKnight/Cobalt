package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when the client's own device list has become stale and needs refresh.
 * <p>
 * In WhatsApp's multi-device architecture, each device maintains a list of all devices
 * associated with the account. This list has a maximum staleness threshold, and when
 * exceeded, the client must refresh the list before continuing certain operations.
 *
 * <h2>Device List Architecture</h2>
 * The device list system works as follows:
 * <ul>
 *   <li>Each device tracks when it last synchronized its device list</li>
 *   <li>The list includes all companion devices and the primary device</li>
 *   <li>A staleness threshold determines when the list needs refresh</li>
 *   <li>The list is used for message encryption routing decisions</li>
 * </ul>
 *
 * <h2>When This Occurs</h2>
 * This exception is thrown during ADV (Authenticated Device Verification) checks when:
 * <ul>
 *   <li>The device list hasn't been updated for too long</li>
 *   <li>The staleness threshold has been exceeded</li>
 *   <li>A message would be sent to a potentially outdated device list</li>
 * </ul>
 *
 * <h2>Staleness Thresholds</h2>
 * WhatsApp uses different thresholds depending on context:
 * <ul>
 *   <li>Normal operations: ~7 days</li>
 *   <li>Security-sensitive operations: Shorter intervals</li>
 *   <li>Server may request immediate refresh in some cases</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * The fatality of this exception depends on the context:
 * <ul>
 *   <li><b>Fatal:</b> When the staleness indicates a serious sync issue requiring logout</li>
 *   <li><b>Non-fatal:</b> When a simple device list refresh can resolve the issue</li>
 * </ul>
 *
 * <h2>Recovery</h2>
 * <ul>
 *   <li><b>Non-fatal:</b> Refresh the device list from the server and retry</li>
 *   <li><b>Fatal:</b> Log out and re-authenticate to re-establish device relationships</li>
 * </ul>
 */
public final class WhatsAppOwnDeviceListExpiredException extends WhatsAppException {
    /**
     * Constructs a new device list expired exception.
     */
    public WhatsAppOwnDeviceListExpiredException() {
        super("Own device list has expired");
    }

    /**
     * Returns whether this exception represents a fatal error.
     *
     * <p>The client's own device list has exceeded the server-enforced staleness
     * threshold and cannot be used for message routing without a full refresh.
     * Cobalt treats this condition as fatal and delegates the recovery policy
     * (refresh list, reconnect, or log out) to the configurable error handler.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }
}
