package com.github.auties00.cobalt.device.model;

import com.github.auties00.cobalt.model.jid.Jid;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * Represents a pending device sync request that needs to be completed.
 * Used to persist failed device fetches for retry on reconnect.
 *
 * @param userJids   the JIDs to sync
 * @param context    the sync context (e.g., "message", "interactive")
 * @param timestamp  when the sync was requested (epoch millis)
 * @param retryCount number of retry attempts
 */
public record PendingDeviceSync(
        List<Jid> userJids,
        String context,
        long timestamp,
        int retryCount
) implements Serializable {
    private static final int MAX_RETRIES = 3;
    private static final long EXPIRY_DURATION_MS = Duration.ofHours(24).toMillis();

    /**
     * Creates a new pending device sync request.
     *
     * @param userJids the user JIDs to sync
     * @param context  the sync context
     * @return new pending sync
     */
    public static PendingDeviceSync of(Collection<Jid> userJids, String context) {
        return new PendingDeviceSync(
                List.copyOf(userJids),
                context,
                System.currentTimeMillis(),
                0
        );
    }

    /**
     * Creates a new pending sync with incremented retry count.
     *
     * @return pending sync with retry count incremented
     */
    public PendingDeviceSync nextRetry() {
        return new PendingDeviceSync(userJids, context, timestamp, retryCount + 1);
    }

    /**
     * Checks if this sync should be retried.
     *
     * @return true if retry count is below maximum
     */
    public boolean shouldRetry() {
        return retryCount < MAX_RETRIES;
    }

    /**
     * Checks if this sync has expired.
     *
     * @return true if older than 24 hours
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > EXPIRY_DURATION_MS;
    }
}
