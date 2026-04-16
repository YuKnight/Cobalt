package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when device synchronization (USync) fails.
 *
 * <p>USync is the WhatsApp server-side mechanism used to retrieve and reconcile
 * device lists for a set of users. When the client needs to send a message to a
 * user it first queries USync to learn which devices belong to that user, so that
 * the message can be fanned out to every relevant Signal session.

 * <p>Per WhatsApp Web, USync can fail with two distinct classes of errors:
 * <ul>
 *   <li><b>Fatal ({@code error.all}):</b> Affects the entire sync request and prevents
 *       processing of every user in the batch.</li>
 *   <li><b>Non-fatal ({@code error.devices}):</b> Affects specific device queries but
 *       allows the rest of the batch to continue.</li>
 * </ul>
 */
public final class WhatsAppDeviceSyncException extends WhatsAppException {
    /**
     * The error code returned by the USync server response.
     */
    private final int errorCode;

    /**
     * Whether this particular USync failure should be treated as fatal.
     */
    private final boolean fatal;

    /**
     * Constructs a new device sync exception.
     *
     * @param errorCode the error code from the server
     * @param errorText the error text/message from the server
     * @param fatal     whether this is a fatal error (batch-wide) or a per-device
     *                  failure that allows the rest of the batch to succeed
     */
    public WhatsAppDeviceSyncException(int errorCode, String errorText, boolean fatal) {
        super("USync error " + errorCode + ": " + errorText);
        this.errorCode = errorCode;
        this.fatal = fatal;
    }

    /**
     * Returns the error code from the server.
     *
     * <p>The code originates from the {@code code} attribute of the USync {@code error}
     * node and can be used to distinguish between server-side failure modes.
     *
     * @return the error code
     */
    public int errorCode() {
        return errorCode;
    }

    /**
     * Returns whether this exception represents a fatal error.
     *
     * <p>Fatal USync errors indicate the entire batch request failed and cannot be
     * processed. Non-fatal errors indicate only a subset of the device queries failed
     * and the remaining devices can still be used.
     *
     * @return {@code true} if the USync response marked this as a batch-wide failure
     */
    @Override
    public boolean isFatal() {
        return fatal;
    }
}
