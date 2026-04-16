package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when a WhatsApp store cannot be deserialized due to data corruption.
 *
 * <p>This exception indicates that the persisted session data (credentials, keys, chat
 * history, identity state) has become corrupted and cannot be loaded. This is a fatal
 * condition that typically requires creating a new session by logging out and re-linking
 * the device.

 * <p>Cobalt stores session data in a single flattened {@code AbstractWhatsAppStore}.
 * If that store has been tampered with on disk, truncated, or serialized with an
 * incompatible format, the deserializer raises this exception so the application can
 * surface a clean restart flow instead of silently continuing with corrupted cryptographic
 * material.
 */
public final class WhatsAppCorruptedStoreException extends WhatsAppException {
    /**
     * Constructs a new corrupted store exception wrapping the specified cause.
     *
     * @param cause the underlying cause of the corruption (typically a deserialization
     *              exception)
     */
    public WhatsAppCorruptedStoreException(Throwable cause) {
        super("Store data is corrupted and cannot be deserialized", cause);
    }

    /**
     * Returns whether this exception represents a fatal error.
     *
     * <p>Corrupted store exceptions are always fatal because the cryptographic material
     * required to resume the session cannot be trusted. The client must log out and
     * start a new session.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }
}
