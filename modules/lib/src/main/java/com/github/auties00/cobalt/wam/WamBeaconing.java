package com.github.auties00.cobalt.wam;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.util.DataUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages daily-sampled sequence numbers for WAM event beaconing.
 *
 * <p>At the start of each calendar day in UTC, there is a one percent
 * chance that beaconing is activated for the current session. When
 * activated, each call to {@link #nextSequenceNumber(String)} returns a
 * monotonically increasing sequence number that is written as global
 * field {@code 3433} ({@code beaconSessionId}) before each event.
 *
 * <p>If beaconing is not activated for the current day the method
 * returns an empty {@link OptionalInt} and no beacon global is written.
 *
 * <p>Beaconing state is tracked independently per buffer key. WhatsApp
 * Web defines ten buffer keys, namely {@code "regular"},
 * {@code "realtime"}, and the eight private-stats id key names such as
 * {@code "DefaultPsId"} or {@code "IdTtlDaily"}.
 *
 * <p>This class is not thread-safe. All calls must be made from the
 * single WAM flush thread.
 */
@WhatsAppWebModule(moduleName = "WAWebWamBeaconing")
final class WamBeaconing {
    /**
     * Probability that beaconing is activated on any given day.
     */
    private static final double ACTIVATION_PROBABILITY = 0.01;

    /**
     * Per-buffer-key beaconing state, keyed by the buffer key string.
     */
    private final ConcurrentMap<String, ChannelState> states;

    /**
     * Constructs a new {@code WamBeaconing} instance with no active
     * beaconing session.
     */
    WamBeaconing() {
        this.states = new ConcurrentHashMap<>();
    }

    /**
     * Returns the next beaconing sequence number when beaconing is
     * active for the current day, otherwise returns an empty value.
     *
     * <p>On the first call of a new calendar day a random check
     * determines whether beaconing is activated. When activated the
     * sequence counter resets to {@code 1} and increments on each
     * subsequent call within the same day.
     * @param bufferKey the buffer key identifying the beaconing track,
     *                  for example {@code "regular"}, {@code "realtime"},
     *                  or a private-stats id key name
     * @return an {@code OptionalInt} containing the sequence number
     *         when beaconing is active, or empty otherwise
     */
    @WhatsAppWebExport(moduleName = "WAWebWamBeaconing", exports = "maybeGetEventSequenceNumber", adaptation = WhatsAppAdaptation.ADAPTED)
    OptionalInt nextSequenceNumber(String bufferKey) {
        var state = states.computeIfAbsent(bufferKey, _ -> new ChannelState());
        var currentDayEpoch = Instant.now().truncatedTo(ChronoUnit.DAYS).getEpochSecond();
        if (currentDayEpoch != state.activationDayEpoch) {
            state.activationDayEpoch = currentDayEpoch;
            state.active = DataUtils.randomDouble() <= ACTIVATION_PROBABILITY;
            state.sequenceNumber = 0;
        }

        if (!state.active) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(++state.sequenceNumber);
    }

    /**
     * Per-buffer-key beaconing state, holding the activation day, the
     * activation flag, and the running sequence counter.
     */
    private static final class ChannelState {
        /**
         * Epoch seconds of the calendar day for which {@link #active}
         * was last decided. Initialised to {@code -1} so the first
         * call always re-rolls activation.
         */
        long activationDayEpoch = -1;

        /**
         * {@code true} when beaconing is active for the current day.
         */
        boolean active = false;

        /**
         * Monotonic counter incremented on each successful call. Reset
         * to {@code 0} at the start of every new day.
         */
        int sequenceNumber = 0;
    }
}
