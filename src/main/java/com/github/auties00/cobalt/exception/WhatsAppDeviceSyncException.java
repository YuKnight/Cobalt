package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when device synchronization (USync) fails.
 * <p>
 * Per WhatsApp Web, USync can fail with two types of errors:
 * <ul>
 *   <li><b>Fatal (error.all):</b> Affects the entire sync request and prevents processing</li>
 *   <li><b>Non-fatal (error.devices):</b> Affects specific device queries but allows continuation</li>
 * </ul>
 */
public final class WhatsAppDeviceSyncException extends WhatsAppException {
    private final int errorCode;
    private final boolean fatal;

    /**
     * Constructs a new device sync exception.
     *
     * @param errorCode the error code from the server
     * @param errorText the error text/message from the server
     * @param fatal     whether this is a fatal error
     */
    public WhatsAppDeviceSyncException(int errorCode, String errorText, boolean fatal) {
        super("USync error " + errorCode + ": " + errorText);
        this.errorCode = errorCode;
        this.fatal = fatal;
    }

    /**
     * Returns the error code from the server.
     *
     * @return the error code
     */
    public int errorCode() {
        return errorCode;
    }

    @Override
    public boolean isFatal() {
        return fatal;
    }
}
