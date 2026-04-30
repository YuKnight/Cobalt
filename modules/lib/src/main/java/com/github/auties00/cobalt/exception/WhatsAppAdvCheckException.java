package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * Thrown when the periodic Advanced Device Verification (ADV) maintenance
 * job fails.
 *
 * <p>WhatsApp tracks the freshness of every device list (the set of
 * companion devices linked to an account) and runs a daily check that
 * marks expired entries, schedules proactive resyncs for entries about
 * to expire, and clears the Signal sessions that belonged to evicted
 * devices. When that job cannot complete because the local store is
 * unreachable, an AB prop is missing, or a triggered resync fails, this
 * exception is raised so the configurable error handler can decide
 * whether to log the failure, retry early, or escalate.
 *
 * <p>The failure does not break the session. {@link #isFatal()} always
 * returns {@code false}: the next scheduled run can recover and message
 * traffic in the meantime is unaffected.
 */
@WhatsAppWebModule(moduleName = "WAWebAdvDeviceInfoCheckJob")
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
     * Returns whether the failure invalidates the current session.
     *
     * <p>The periodic ADV check runs in the background and a single
     * failure does not affect message sending or receiving.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
