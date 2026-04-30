package com.github.auties00.cobalt.exception;

/**
 * Thrown when the cached list of devices linked to this account has
 * become too stale to be used for routing messages.
 *
 * <p>Every device in a WhatsApp account keeps its own record of the
 * full set of devices linked to that account, refreshed periodically
 * from the server. The Advanced Device Verification job watches the
 * age of that record and raises this exception when it has exceeded
 * the staleness threshold the server is willing to accept. While the
 * record is in this state, sending a message would risk targeting a
 * device that is no longer authorized or omitting one that has just
 * been added.
 *
 * <p>The error is fatal in Cobalt: until the device list is refreshed
 * the local view of the account cannot be trusted. The configurable
 * error handler decides whether to refresh and reconnect or to log the
 * device out.
 */
public final class WhatsAppOwnDeviceListExpiredException extends WhatsAppException {
    /**
     * Constructs a new device list expired exception.
     */
    public WhatsAppOwnDeviceListExpiredException() {
        super("Own device list has expired");
    }

    /**
     * Returns whether the failure invalidates the current session.
     *
     * <p>The local device list is required for correctly routing
     * outgoing messages. While it is expired the session cannot be
     * trusted and the failure is reported as fatal.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }
}
