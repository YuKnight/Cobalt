package com.github.auties00.cobalt.device.timestamp;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.time.Instant;
import java.util.Optional;

/**
 * Holds the three expected-timestamp tracking fields produced while processing a
 * device list update.
 *
 * <p>WhatsApp's ADV (advanced device verification) system detects stale device lists
 * even when the device-hash check passes, by comparing incoming timestamps against
 * a locally tracked expected timestamp plus two auxiliary timestamps recording when
 * the expectation was last refreshed and when the last periodic ADV job ran. This
 * record bundles those three values together for transport through
 * {@link DeviceExpectedTsUtils} and {@link com.github.auties00.cobalt.device.DeviceService}.
 */
@WhatsAppWebModule(moduleName = "WAWebAdvExpectedTsApi")
public final class ExpectedTimestampResult {
    /**
     * The expected timestamp, or {@code null} when unset.
     */
    private final Instant expectedTimestamp;

    /**
     * The timestamp of the last ADV device job that observed
     * {@link #expectedTimestamp}, or {@code null} when unset.
     */
    private final Instant expectedTimestampLastDeviceJobTimestamp;

    /**
     * The instant at which {@link #expectedTimestamp} was last modified, or
     * {@code null} when unset.
     */
    private final Instant expectedTimestampUpdateTimestamp;

    /**
     * Constructs a new tracking-fields tuple.
     *
     * @param expectedTimestamp                       the expected timestamp, or {@code null}
     * @param expectedTimestampLastDeviceJobTimestamp the last ADV job timestamp, or {@code null}
     * @param expectedTimestampUpdateTimestamp        the last update instant, or {@code null}
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
     * Returns the expected timestamp.
     *
     * @return the expected timestamp, or empty when unset
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "expectedTs",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Instant> expectedTimestamp() {
        return Optional.ofNullable(expectedTimestamp);
    }

    /**
     * Returns the timestamp of the last ADV device job that observed the expected
     * timestamp.
     *
     * @return the last job timestamp, or empty when unset
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "expectedTsLastDeviceJobTs",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Instant> expectedTimestampLastDeviceJobTimestamp() {
        return Optional.ofNullable(expectedTimestampLastDeviceJobTimestamp);
    }

    /**
     * Returns the instant at which the expected timestamp was last modified.
     *
     * @return the update instant, or empty when unset
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "expectedTsUpdateTs",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Instant> expectedTimestampUpdateTimestamp() {
        return Optional.ofNullable(expectedTimestampUpdateTimestamp);
    }
}
