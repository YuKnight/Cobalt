package com.github.auties00.cobalt.message.send.error;

/**
 * Retry strategies for different failure types.
 */
public enum MessageRetryStrategy {
    /**
     * No retry - error is not recoverable.
     */
    NONE,

    /**
     * Refresh device lists and recalculate phash.
     */
    REFRESH_DEVICE_LIST,

    /**
     * Fetch prekey bundles for devices.
     */
    FETCH_PREKEYS,

    /**
     * Distribute sender keys to new devices.
     */
    DISTRIBUTE_SENDER_KEY,

    /**
     * Requires user confirmation (e.g., identity changed).
     */
    USER_CONFIRMATION,

    /**
     * Retry with exponential backoff.
     */
    EXPONENTIAL_BACKOFF
}
