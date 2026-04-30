package com.github.auties00.cobalt.exception;

/**
 * Thrown when a USync device-list query against the WhatsApp servers
 * returns an error.
 *
 * <p>USync is the request Cobalt issues before sending a message in
 * order to learn the set of devices that belong to each recipient. The
 * server can reject the request as a whole (a batch-wide failure that
 * blocks the entire send) or report a per-device issue inside an
 * otherwise successful response (a partial failure that lets other
 * recipients still be addressed). The {@code fatal} flag passed to the
 * constructor mirrors that distinction and is reflected in
 * {@link #isFatal()}.
 */
public final class WhatsAppDeviceSyncException extends WhatsAppException {
    /**
     * The numeric error code returned in the USync error stanza.
     */
    private final int errorCode;

    /**
     * Whether the USync server response marked this failure as
     * batch-wide.
     */
    private final boolean fatal;

    /**
     * Constructs a new device sync exception.
     *
     * @param errorCode the numeric error code returned by the server
     * @param errorText the human-readable description returned by the server
     * @param fatal     {@code true} when the failure affects the whole batch,
     *                  {@code false} when only a subset of devices failed
     */
    public WhatsAppDeviceSyncException(int errorCode, String errorText, boolean fatal) {
        super("USync error " + errorCode + ": " + errorText);
        this.errorCode = errorCode;
        this.fatal = fatal;
    }

    /**
     * Returns the numeric error code returned by the USync server.
     *
     * <p>The code is taken verbatim from the {@code code} attribute of
     * the USync error stanza and can be used to disambiguate the
     * server-side failure mode.
     *
     * @return the error code
     */
    public int errorCode() {
        return errorCode;
    }

    /**
     * Returns whether the failure invalidates the current session.
     *
     * <p>USync errors are reported as fatal when the entire batch was
     * rejected and as non-fatal when only a subset of devices failed.
     *
     * @return {@code true} for batch-wide rejections, {@code false}
     *         when the rest of the response can still be used
     */
    @Override
    public boolean isFatal() {
        return fatal;
    }
}
