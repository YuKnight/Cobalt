package com.github.auties00.cobalt.exception;

/**
 * Thrown when the persisted Cobalt store cannot be loaded because its
 * bytes are corrupt.
 *
 * <p>Cobalt keeps a single on-disk store containing the registration
 * credentials, Signal protocol keys, identity material, and the cached
 * chat state. When the store is truncated, tampered with, or written by
 * an incompatible version, the deserializer raises this exception
 * instead of silently continuing with cryptographic material that
 * cannot be trusted.
 *
 * <p>The failure is fatal. The application has to log the device out
 * and pair it again before any session can resume.
 */
public final class WhatsAppCorruptedStoreException extends WhatsAppException {
    /**
     * Constructs a new corrupted store exception wrapping the specified cause.
     *
     * @param cause the underlying deserialization failure
     */
    public WhatsAppCorruptedStoreException(Throwable cause) {
        super("Store data is corrupted and cannot be deserialized", cause);
    }

    /**
     * Returns whether the failure invalidates the current session.
     *
     * <p>The store contains the keys needed to resume the session, so a
     * corrupted store leaves the application with no usable identity.
     * The error is always fatal.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }
}
