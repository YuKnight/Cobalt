package com.github.auties00.cobalt.device.timestamp;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.time.Instant;
import java.util.Optional;

/**
 * Carries the three expected-timestamp tracking fields produced while processing a
 * device list update.
 *
 * <p>WhatsApp's ADV (advanced device verification) system detects stale device lists
 * even when the cheap device-hash (dhash) check passes, by comparing incoming
 * timestamps against a locally tracked "expected timestamp" plus two auxiliary
 * timestamps that record when the expectation was last refreshed and when the last
 * periodic ADV job ran. This record bundles those three values so
 * {@link DeviceExpectedTsUtils} and {@link com.github.auties00.cobalt.device.DeviceService}
 * can carry them through the update pipeline without spreading null-checks.
 *
 * @implNote WAWebAdvExpectedTsApi: these three fields are tracked together in a
 * plain JS object {@code {expectedTs, expectedTsLastDeviceJobTs, expectedTsUpdateTs}}
 * to detect staleness even when dhash matches.
 */
@WhatsAppWebModule(moduleName = "WAWebAdvExpectedTsApi")
public final class ExpectedTimestampResult {
    /**
     * The expected timestamp value, or {@code null} if not set.
     *
     * @implNote WAWebAdvExpectedTsApi: maps to the {@code expectedTs} field in the
     * return object.
     */
    private final Instant expectedTimestamp;

    /**
     * The last ADV device job timestamp, or {@code null} if not set.
     *
     * @implNote WAWebAdvExpectedTsApi: maps to the {@code expectedTsLastDeviceJobTs}
     * field in the return object.
     */
    private final Instant expectedTimestampLastDeviceJobTimestamp;

    /**
     * When the expected timestamp was last modified, or {@code null} if not set.
     *
     * @implNote WAWebAdvExpectedTsApi: maps to the {@code expectedTsUpdateTs} field
     * in the return object.
     */
    private final Instant expectedTimestampUpdateTimestamp;

    /**
     * Creates a new computed expected timestamp result.
     *
     * @implNote WAWebAdvExpectedTsApi.computeNewExpectedTs: corresponds to the
     * construction of the return object {@code {expectedTs, expectedTsLastDeviceJobTs,
     * expectedTsUpdateTs}}.
     * @param expectedTimestamp                       the expected timestamp value, or {@code null}
     * @param expectedTimestampLastDeviceJobTimestamp the last ADV job timestamp, or {@code null}
     * @param expectedTimestampUpdateTimestamp        when expectedTs was last modified, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "computeNewExpectedTs",
            adaptation = WhatsAppAdaptation.ADAPTED)
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
     * @implNote WAWebAdvExpectedTsApi.expectedTs
     * @return an optional containing the expected timestamp, or empty if not set
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "computeNewExpectedTs",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<Instant> expectedTimestamp() {
        return Optional.ofNullable(expectedTimestamp);
    }

    /**
     * Returns the last ADV job timestamp.
     *
     * @implNote WAWebAdvExpectedTsApi.expectedTsLastDeviceJobTs
     * @return an optional containing the last ADV job timestamp, or empty if not set
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "computeNewExpectedTs",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<Instant> expectedTimestampLastDeviceJobTimestamp() {
        return Optional.ofNullable(expectedTimestampLastDeviceJobTimestamp);
    }

    /**
     * Returns when the expected timestamp was last modified.
     *
     * @implNote WAWebAdvExpectedTsApi.expectedTsUpdateTs
     * @return an optional containing the update timestamp, or empty if not set
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "computeNewExpectedTs",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<Instant> expectedTimestampUpdateTimestamp() {
        return Optional.ofNullable(expectedTimestampUpdateTimestamp);
    }
}
