package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when a WhatsApp store cannot be deserialized due to data corruption.
 * <p>
 * This exception indicates that the persisted session data (credentials, keys, chat history,
 * etc.) has become corrupted and cannot be loaded. This is a fatal condition that typically
 * requires creating a new session.
 */
public final class WhatsAppCorruptedStoreException extends WhatsAppException {
    /**
     * Constructs a new corrupted store exception wrapping the specified cause.
     *
     * @param cause the underlying cause of the corruption
     */
    public WhatsAppCorruptedStoreException(Throwable cause) {
        super("Store data is corrupted and cannot be deserialized", cause);
    }

    @Override
    public boolean isFatal() {
        return true;
    }
}
