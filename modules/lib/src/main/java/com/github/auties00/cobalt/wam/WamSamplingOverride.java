package com.github.auties00.cobalt.wam;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides server-pushed overrides for WAM event sampling weights.
 *
 * <p>By default each event is sampled using the static
 * {@code releaseWeight()} declared in its {@code @WamEvent} annotation.
 * This class allows the runtime to register per-event-id overrides,
 * typically received via AB props or server configuration, that take
 * precedence over the annotation value. When no override is registered
 * for a given event id the caller falls back to the annotation weight.
 *
 * <p>This class is thread-safe. The backing map is a
 * {@link ConcurrentHashMap} and individual lookups are atomic.
 */
@WhatsAppWebModule(moduleName = "WAWebEventSamplingCache")
@WhatsAppWebModule(moduleName = "WAWebEventSampling")
final class WamSamplingOverride {
    /**
     * Map from event id to the overridden sampling weight. Absent keys
     * mean the default annotation weight applies.
     */
    private final Map<Integer, Integer> overrides;

    /**
     * Constructs a new {@code WamSamplingOverride} with no overrides.
     */
    WamSamplingOverride() {
        this.overrides = new ConcurrentHashMap<>();
    }

    /**
     * Registers or updates a sampling weight override for the given
     * event id.
     * @param eventId the numeric WAM event identifier
     * @param weight  the overridden sampling weight, must be positive
     */
    @WhatsAppWebExport(
            moduleName = "WAWebEventSamplingCache",
            exports = "updateEventSamplingFromStorage",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    void put(int eventId, int weight) {
        overrides.put(eventId, weight);
    }

    /**
     * Removes any sampling weight override for the given event id.
     * @param eventId the numeric WAM event identifier
     */
    void remove(int eventId) {
        overrides.remove(eventId);
    }

    /**
     * Replaces all current overrides with the entries from the given
     * map.
     * @param newOverrides a map from event id to sampling weight
     */
    @WhatsAppWebExport(
            moduleName = "WAWebEventSamplingCache",
            exports = "updateEventSamplingFromStorage",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    void replaceAll(Map<Integer, Integer> newOverrides) {
        overrides.clear();
        overrides.putAll(newOverrides);
    }

    /**
     * Returns the overridden sampling weight for the given event id,
     * or empty when no override is registered.
     * @param eventId the numeric WAM event identifier
     * @return an {@code OptionalInt} containing the raw override
     *         weight, or empty when no override is registered
     */
    @WhatsAppWebExport(
            moduleName = "WAWebEventSampling",
            exports = "getClientEventSamplingWeight",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    OptionalInt get(int eventId) {
        var weight = overrides.get(eventId);
        return weight != null ? OptionalInt.of(weight) : OptionalInt.empty();
    }
}
