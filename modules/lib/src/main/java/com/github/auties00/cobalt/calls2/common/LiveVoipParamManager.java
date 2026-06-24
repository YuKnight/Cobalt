package com.github.auties00.cobalt.calls2.common;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Owns the voip-param sets for one call: the stored raw sets keyed by settings type, the
 * selected active set, the participant-count override, and the dynamic rate-control engine.
 *
 * <p>This manager drives the voip-param lifecycle the specification describes. A parsed
 * {@link VoipParams} set is stored under a {@link VoipSettingsType} key with
 * {@link #store(VoipSettingsType, VoipParams)}; the in-use set is chosen from the stored
 * sets with {@link #selectActive(VoipSettingsType)}, which copies the stored baseline so the
 * stored set is never mutated by later overrides; the active set is retuned for the current
 * call size with {@link #overrideForParticipantCount(int, List)}; and each rate-control
 * round its dynamic overrides are written onto the active set through
 * {@link #applyDynamicRules(List)} under the round guard of the manager's
 * {@link DynVoipParamUpdater}.
 *
 * <p>Selecting an active set always starts from a fresh copy of the stored baseline, so
 * participant-count and dynamic overrides accumulate only onto the current selection and are
 * discarded when a different settings type is selected. If a requested settings type has no
 * stored set, selection falls back to the mandatory {@link VoipSettingsType#NONE} default
 * set; selecting when even the default is absent leaves the manager with no active set.
 *
 * <p>The manager is guarded by a single {@link ReentrantLock} so the rate-control tick, the
 * participant-count retune, and any reconfiguration that restores stored sets observe a
 * consistent active set. Reads of the active set return a copy, so a caller never holds a
 * reference that a concurrent override can mutate underneath it.
 *
 * @implNote This implementation reproduces the {@code wa_call_voip_params_manager_*} surface
 * of {@code voip_param_internal.cc} from the wa-voip WASM module {@code ff-tScznZ8P}:
 * {@code store_raw_voip_params} (store per settings type),
 * {@code wa_call_voip_params_manager_select_active_raw_voip_params} (select),
 * {@code override_voip_params_based_on_participant_count} (count retune), and the dynamic
 * rule pass driven by {@code wa_dyn_voip_param_updater_update_with_dyn_rules}. The native
 * code deep-copies the {@code 254KB} struct on store and select; Cobalt copies the sparse
 * {@link VoipParams} map instead (re/calls2-spec/SPEC.md sec 9.3;
 * re/calls2-spec/parts/rev-common.json stateMachine and wireProtocol entries for the
 * voip-params manager).
 */
public final class LiveVoipParamManager {
    /**
     * The stored raw parameter sets, keyed by settings type.
     */
    private final Map<VoipSettingsType, VoipParams> stored;

    /**
     * The dynamic rate-control updater whose round guard spans one rate-control tick.
     */
    private final DynVoipParamUpdater dynamicUpdater;

    /**
     * The lock serialising store, select, count-override, and dynamic-rule passes.
     */
    private final ReentrantLock lock;

    /**
     * The settings type currently selected as active, or {@code null} when none is selected.
     */
    private VoipSettingsType activeType;

    /**
     * The live active parameter set, or {@code null} when none is selected.
     */
    private VoipParams active;

    /**
     * Constructs a voip-param manager with no stored sets and no active selection.
     *
     * <p>The new manager holds an empty store and a fresh {@link DynVoipParamUpdater};
     * callers populate it by storing parsed sets and then selecting one active.
     */
    public LiveVoipParamManager() {
        this.stored = new java.util.EnumMap<>(VoipSettingsType.class);
        this.dynamicUpdater = new DynVoipParamUpdater();
        this.lock = new ReentrantLock();
    }

    /**
     * Stores a parsed parameter set under the given settings type.
     *
     * <p>A previously stored set under the same type is replaced. Storing does not change
     * the active selection; a subsequent {@link #selectActive(VoipSettingsType)} is needed to
     * promote the stored set.
     *
     * @param type   the settings type to store under
     * @param params the parsed parameter set to store
     * @throws NullPointerException if {@code type} or {@code params} is {@code null}
     */
    public void store(VoipSettingsType type, VoipParams params) {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        if (params == null) {
            throw new NullPointerException("params must not be null");
        }
        lock.lock();
        try {
            stored.put(type, params);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Selects the stored set of the given settings type as the active set.
     *
     * <p>The active set is initialised from a fresh copy of the stored baseline, so prior
     * participant-count or dynamic overrides do not carry across selections, and the
     * dynamic-rule round guard is reset. If the requested type has no stored set, the
     * mandatory {@link VoipSettingsType#NONE} default is selected instead; if even the
     * default is absent, no active set is selected and {@link #activeType()} becomes empty.
     *
     * @param type the settings type to promote
     * @return the settings type actually selected, or {@link Optional#empty()} if neither the
     *         requested type nor the default has a stored set
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public Optional<VoipSettingsType> selectActive(VoipSettingsType type) {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        lock.lock();
        try {
            var chosenType = type;
            var baseline = stored.get(chosenType);
            if (baseline == null) {
                chosenType = VoipSettingsType.NONE;
                baseline = stored.get(chosenType);
            }
            if (baseline == null) {
                activeType = null;
                active = null;
                return Optional.empty();
            }
            activeType = chosenType;
            active = new VoipParams(baseline);
            dynamicUpdater.beginRound();
            return Optional.of(chosenType);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Applies participant-count overrides onto the active set for the current call size.
     *
     * <p>The supplied overrides are the count-dependent parameter values for a call of the
     * given participant count; they are written onto the active set directly, outside the
     * dynamic-rule round guard, because the count retune is a one-shot reconfiguration rather
     * than a per-round adjustment. With no active set, this is a no-op.
     *
     * @param participantCount the current number of participants in the call
     * @param overrides        the count-dependent overrides to write
     * @return the count of overrides written, or {@code 0} if no set is active
     * @throws NullPointerException     if {@code overrides} is {@code null}
     * @throws IllegalArgumentException if {@code participantCount} is negative
     */
    public int overrideForParticipantCount(int participantCount, List<DynVoipParamUpdater.DynRuleEntry> overrides) {
        if (overrides == null) {
            throw new NullPointerException("overrides must not be null");
        }
        if (participantCount < 0) {
            throw new IllegalArgumentException("participant count must not be negative");
        }
        lock.lock();
        try {
            if (active == null) {
                return 0;
            }
            var written = 0;
            for (var override : overrides) {
                override.writeOnto(active);
                written++;
            }
            return written;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Applies one rate-control round's matched dynamic overrides onto the active set.
     *
     * <p>The round guard is reset before the overrides are written, so each round reflects
     * the rules matched this tick without carrying the previous tick's writes, and within the
     * round the {@link DynVoipParamUpdater}'s already-updated guard prevents a lower-priority
     * override from clobbering a higher-priority one. With no active set, this is a no-op.
     *
     * @param overrides the matched overrides for this round, in priority order
     * @return the count of overrides written, or {@code 0} if no set is active
     * @throws NullPointerException if {@code overrides} is {@code null}
     */
    public int applyDynamicRules(List<DynVoipParamUpdater.DynRuleEntry> overrides) {
        if (overrides == null) {
            throw new NullPointerException("overrides must not be null");
        }
        lock.lock();
        try {
            if (active == null) {
                return 0;
            }
            dynamicUpdater.beginRound();
            return dynamicUpdater.apply(active, overrides);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a copy of the live active parameter set.
     *
     * <p>The returned set is a snapshot copy taken under the lock; mutating it does not affect
     * the manager's active set, and a concurrent override does not mutate the returned copy.
     *
     * @return a copy of the active set, or {@link Optional#empty()} if no set is active
     */
    public Optional<VoipParams> activeParams() {
        lock.lock();
        try {
            return active == null ? Optional.empty() : Optional.of(new VoipParams(active));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the settings type currently selected as active.
     *
     * @return the active settings type, or {@link Optional#empty()} if no set is active
     */
    public Optional<VoipSettingsType> activeType() {
        lock.lock();
        try {
            return Optional.ofNullable(activeType);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns whether a parameter set is stored under the given settings type.
     *
     * @param type the settings type to test
     * @return {@code true} if a set is stored under the type, {@code false} otherwise
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public boolean hasStored(VoipSettingsType type) {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        lock.lock();
        try {
            return stored.containsKey(type);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clears all stored sets and the active selection.
     *
     * <p>After this call the manager holds no stored sets and has no active set, matching the
     * engine's clear path at the end of a call.
     */
    public void clear() {
        lock.lock();
        try {
            stored.clear();
            activeType = null;
            active = null;
            dynamicUpdater.beginRound();
        } finally {
            lock.unlock();
        }
    }
}
