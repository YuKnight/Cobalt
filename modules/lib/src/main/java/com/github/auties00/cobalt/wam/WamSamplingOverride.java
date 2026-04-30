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
 *
 * @implNote Collapses two WhatsApp Web modules into a single storage
 *     class. {@code WAWebEventSamplingCache} owns the
 *     {@code Map<eventId, weight>} and is populated from
 *     {@code WAWebApiAbPropEventSamplingConfig};
 *     {@code WAWebEventSampling} is a thin facade exposing
 *     {@code getClientEventSamplingWeight(id)} as
 *     {@code Math.abs(impl(id))}, where {@code impl} is installed by
 *     {@code initializeEventSamplingCache} via
 *     {@code setGetEventSamplingConfigValueImpl}. Cobalt removes the
 *     pluggable-function seam because only one implementation exists
 *     in practice. The caller, {@link WamService#commit}, reads
 *     directly from this cache and applies {@code Math.abs} locally,
 *     which means {@code setGetEventSamplingConfigValueImpl} has no
 *     Java analogue and {@code getClientEventSamplingWeight} is split
 *     across {@link #get} for the map lookup and {@link WamService}
 *     for the absolute-value normalization plus annotation fallback.
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
     *
     * @implNote Mirrors the per-entry insert that
     *     {@code updateEventSamplingFromStorage} performs inside its
     *     {@code forEach(t -> e.set(t.eventCode, t.samplingWeight))}
     *     loop, exposed here as a direct setter so callers can stage
     *     overrides individually rather than only through a bulk
     *     replace.
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
     *
     * @implNote No WhatsApp Web counterpart.
     *     {@code WAWebEventSamplingCache} only supports wholesale
     *     rebuild of the map via {@code updateEventSamplingFromStorage}.
     *     Cobalt adds a targeted removal so the surrounding
     *     {@link WamService} can expose a symmetric add and remove API
     *     without forcing a full rebuild on every toggle.
     * @param eventId the numeric WAM event identifier
     */
    void remove(int eventId) {
        overrides.remove(eventId);
    }

    /**
     * Replaces all current overrides with the entries from the given
     * map.
     *
     * @implNote Mirrors the body of
     *     {@code updateEventSamplingFromStorage}, which clears and
     *     repopulates the backing map with the
     *     {@code (eventCode, samplingWeight)} pairs returned by
     *     {@code WAWebApiAbPropEventSamplingConfig.getEventSamplingConfigs}
     *     and then flips the cache-ready flag. WhatsApp Web's flag
     *     gates {@code u(t) = s ? e.get(t) : null}. Cobalt omits the
     *     flag because the map is only consulted after explicit
     *     seeding, so an empty map already produces the correct
     *     {@code null} semantics.
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
     *
     * @implNote Implements the raw lookup portion of
     *     {@code WAWebEventSampling.getClientEventSamplingWeight},
     *     which reads {@code impl(id)} where {@code impl} is the
     *     cache-backed closure installed by
     *     {@code WAWebEventSamplingCache.initializeEventSamplingCache}
     *     ({@code function u(t) { return s ? e.get(t) : null; }}). The
     *     {@code Math.abs} normalization that WhatsApp Web applies on
     *     top of {@code impl} is performed by the caller in
     *     {@link WamService#commit} so the two pieces stay adjacent to
     *     the release-weight fallback.
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
