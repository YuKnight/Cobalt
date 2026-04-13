package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when the periodic ADV (Advanced Device Verification) check fails.
 * <p>
 * The ADV check scheduler runs every 24 hours to verify device list freshness and
 * prevent device list expiration. This exception is thrown when the check encounters
 * an error during processing.
 *
 * <h2>ADV Check Process</h2>
 * The check job performs the following operations:
 * <ol>
 *   <li><b>Expiration check:</b> Identifies device lists that have exceeded the
 *       expiration threshold (default 35 days)</li>
 *   <li><b>Staleness check:</b> Detects device lists with stale expectedTs values
 *       (not updated within 25 hours)</li>
 *   <li><b>Proactive sync:</b> Schedules device syncs for lists approaching expiration
 *       (within warning threshold, default 7 days before expiry)</li>
 *   <li><b>Cleanup:</b> Clears expired device records and associated Signal sessions</li>
 * </ol>
 *
 * <h2>Possible Causes</h2>
 * <ul>
 *   <li><b>Store access failure:</b> Unable to read device lists from storage</li>
 *   <li><b>AB prop retrieval failure:</b> Unable to fetch expiration thresholds</li>
 *   <li><b>Device sync failure:</b> Error scheduling proactive device syncs</li>
 *   <li><b>Session cleanup failure:</b> Error clearing expired Signal sessions</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * ADV check exceptions are non-fatal. The scheduler will retry on the next interval,
 * and the client can continue operating normally. Failed checks do not affect
 * message sending or receiving.
 *
 * @apiNote WAWebAdvDeviceInfoCheckJob: manages automated periodic verification and
 * expiration of device information lists for Advanced Device Verification.
 */
public final class WhatsAppAdvCheckException extends WhatsAppException {

    /**
     * Constructs a new ADV check exception with no detail message.
     */
    public WhatsAppAdvCheckException() {
        super();
    }

    /**
     * Constructs a new ADV check exception with the specified detail message.
     *
     * @param message the detail message describing the check failure
     */
    public WhatsAppAdvCheckException(String message) {
        super(message);
    }

    /**
     * Constructs a new ADV check exception with a detail message and cause.
     *
     * @param message the detail message describing the check failure
     * @param cause   the underlying cause of the check failure
     */
    public WhatsAppAdvCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ADV check exception wrapping the specified cause.
     *
     * @param cause the underlying cause of the check failure
     */
    public WhatsAppAdvCheckException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * ADV check exceptions are non-fatal. The scheduler will automatically
     * retry on the next 24-hour interval, and the client can continue
     * operating normally in the meantime.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
