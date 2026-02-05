package com.github.auties00.cobalt.device.result;

import java.time.Instant;
import java.util.Optional;

/**
 * Result of computing expected timestamp fields for a device record.
 *
 * @apiNote WAWebAdvExpectedTsApi: these three fields are tracked together to
 * detect staleness even when dhash matches.
 */
public final class ExpectedTimestampResult {
    private final Instant expectedTimestamp;
    private final Instant expectedTimestampLastDeviceJobTimestamp;
    private final Instant expectedTimestampUpdateTimestamp;

    /**
     * Creates a new computed expected timestamp result.
     *
     * @param expectedTimestamp                       the expected timestamp value, or {@code null}
     * @param expectedTimestampLastDeviceJobTimestamp the last ADV job timestamp, or {@code null}
     * @param expectedTimestampUpdateTimestamp        when expectedTs was last modified, or {@code null}
     */
    public ExpectedTimestampResult(
            Instant expectedTimestamp,
            Instant expectedTimestampLastDeviceJobTimestamp,
            Instant expectedTimestampUpdateTimestamp
    ) {
        this.expectedTimestamp = expectedTimestamp;
        this.expectedTimestampLastDeviceJobTimestamp = expectedTimestampLastDeviceJobTimestamp;
        this.expectedTimestampUpdateTimestamp = expectedTimestampUpdateTimestamp;
    }

    /**
     * Returns the expected timestamp value.
     *
     * @return an optional containing the expected timestamp, or empty if not set
     */
    public Optional<Instant> expectedTimestamp() {
        return Optional.ofNullable(expectedTimestamp);
    }

    /**
     * Returns the last ADV job timestamp.
     *
     * @return an optional containing the last ADV job timestamp, or empty if not set
     */
    public Optional<Instant> expectedTimestampLastDeviceJobTimestamp() {
        return Optional.ofNullable(expectedTimestampLastDeviceJobTimestamp);
    }

    /**
     * Returns when expectedTs was last modified.
     *
     * @return an optional containing the update timestamp, or empty if not set
     */
    public Optional<Instant> expectedTimestampUpdateTimestamp() {
        return Optional.ofNullable(expectedTimestampUpdateTimestamp);
    }
}
