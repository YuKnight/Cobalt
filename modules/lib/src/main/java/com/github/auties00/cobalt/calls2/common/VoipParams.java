package com.github.auties00.cobalt.calls2.common;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds one parsed voip-param set as a typed key-to-value map.
 *
 * <p>This is the in-memory materialisation of a single {@code <voip_settings>} document.
 * The native engine fills a {@code 254KB} flat struct ({@code wa_voip_params}) from the
 * JSON document; Cobalt instead keeps a sparse map from {@link VoipParamKey} to a boxed
 * scalar or array value, which holds only the tunables that were actually present and that
 * Cobalt models. Modelled keys (the ones in {@link VoipParamKey}) are stored under their
 * enum constant and read back through the typed accessors; tunables not yet modelled are
 * retained under their raw dotted path so a value is never silently dropped during parsing,
 * even though the engine cannot yet branch on it.
 *
 * <p>Stored scalar values are boxed as {@link Long} for {@link VoipParamType#INTEGER},
 * {@link Double} for {@link VoipParamType#FLOAT}, and {@link String} for
 * {@link VoipParamType#STRING}; array values are boxed as a {@code long[]} or {@code double[]}.
 * The typed accessors coerce a stored integer to a long, a stored float to a double, and a
 * stored string to a string, and report {@linkplain Optional#empty() absent} when the key is
 * unset or holds a value of a different shape.
 *
 * <p>A set is mutable: the dynamic rate-control rule engine
 * ({@link DynVoipParamUpdater}) overwrites individual values on the live set each round,
 * and the manager copies a stored set before mutating it so the stored baseline is never
 * clobbered. The backing maps are {@link ConcurrentHashMap}s so a copy taken while a value
 * is being read does not fault, but a set is otherwise expected to be driven from the
 * single call transport thread.
 *
 * @implNote This implementation replaces the flat {@code wa_voip_params} struct (the
 * {@code 0x3f6d0}-byte block deep-copied by {@code store_raw_voip_params}) of the wa-voip
 * WASM module {@code ff-tScznZ8P} with a sparse typed map, mirroring the dotted-path lookup
 * of {@code get_voip_param_info_private} without reproducing the byte layout
 * (re/calls2-spec/SPEC.md sec 9.3; re/calls2-spec/parts/rev-common.json algorithms entry
 * {@code Dotted-path param lookup}).
 */
public final class VoipParams {
    /**
     * The modelled tunables, keyed by their {@link VoipParamKey} constant.
     */
    private final Map<VoipParamKey, Object> values;

    /**
     * The unmodelled tunables, keyed by their raw dotted path.
     */
    private final Map<String, Object> unmodelled;

    /**
     * Constructs an empty voip-param set.
     *
     * <p>A freshly constructed set holds no values; the deserializer populates it as it
     * walks the JSON document.
     */
    public VoipParams() {
        this.values = new ConcurrentHashMap<>();
        this.unmodelled = new ConcurrentHashMap<>();
    }

    /**
     * Constructs a voip-param set as a deep-ish copy of another set.
     *
     * <p>The new set carries the same key-to-value bindings as the source. Scalar values
     * are immutable boxed types and are shared directly; array values are cloned so an
     * override applied to the copy cannot mutate the source's array.
     *
     * @param source the set to copy
     */
    public VoipParams(VoipParams source) {
        this.values = new ConcurrentHashMap<>();
        this.unmodelled = new ConcurrentHashMap<>();
        source.values.forEach((key, value) -> values.put(key, copyValue(value)));
        source.unmodelled.forEach((key, value) -> unmodelled.put(key, copyValue(value)));
    }

    /**
     * Returns a copy of one stored value, cloning array values and sharing scalar values.
     *
     * @param value the stored value to copy
     * @return a copy safe to mutate independently of the source
     */
    private static Object copyValue(Object value) {
        return switch (value) {
            case long[] array -> array.clone();
            case double[] array -> array.clone();
            default -> value;
        };
    }

    /**
     * Stores an integer value under the given modelled key.
     *
     * @param key   the modelled key to set
     * @param value the integer value to store
     */
    public void putInteger(VoipParamKey key, long value) {
        values.put(key, value);
    }

    /**
     * Stores a floating-point value under the given modelled key.
     *
     * @param key   the modelled key to set
     * @param value the floating-point value to store
     */
    public void putDouble(VoipParamKey key, double value) {
        values.put(key, value);
    }

    /**
     * Stores a string value under the given modelled key.
     *
     * @param key   the modelled key to set
     * @param value the string value to store
     */
    public void putString(VoipParamKey key, String value) {
        values.put(key, value);
    }

    /**
     * Stores an integer-array value under the given modelled key.
     *
     * <p>The array is stored by reference; callers must not retain and mutate the passed
     * array after this call.
     *
     * @param key   the modelled key to set
     * @param value the integer-array value to store
     */
    public void putIntegerArray(VoipParamKey key, long[] value) {
        values.put(key, value);
    }

    /**
     * Stores a floating-point-array value under the given modelled key.
     *
     * <p>The array is stored by reference; callers must not retain and mutate the passed
     * array after this call.
     *
     * @param key   the modelled key to set
     * @param value the floating-point-array value to store
     */
    public void putDoubleArray(VoipParamKey key, double[] value) {
        values.put(key, value);
    }

    /**
     * Stores a value under a raw dotted path that no {@link VoipParamKey} models.
     *
     * <p>The deserializer calls this for every tunable outside the modelled subset so the
     * parsed document is preserved in full; the engine does not read these back, but they
     * remain available for inspection and for a future generated-catalogue upgrade.
     *
     * @param dottedPath the raw dotted path the engine addresses the tunable by
     * @param value      the boxed value to store
     */
    public void putUnmodelled(String dottedPath, Object value) {
        unmodelled.put(dottedPath, value);
    }

    /**
     * Returns the integer value stored under the given key.
     *
     * @param key the modelled key to read
     * @return the integer value, or {@link OptionalLong#empty()} if the key is unset or
     *         holds a non-integer value
     */
    public OptionalLong getInteger(VoipParamKey key) {
        return values.get(key) instanceof Long value ? OptionalLong.of(value) : OptionalLong.empty();
    }

    /**
     * Returns the floating-point value stored under the given key.
     *
     * @param key the modelled key to read
     * @return the floating-point value, or {@link OptionalDouble#empty()} if the key is
     *         unset or holds a non-floating-point value
     */
    public OptionalDouble getDouble(VoipParamKey key) {
        return values.get(key) instanceof Double value ? OptionalDouble.of(value) : OptionalDouble.empty();
    }

    /**
     * Returns the string value stored under the given key.
     *
     * @param key the modelled key to read
     * @return the string value, or {@link Optional#empty()} if the key is unset or holds a
     *         non-string value
     */
    public Optional<String> getString(VoipParamKey key) {
        return values.get(key) instanceof String value ? Optional.of(value) : Optional.empty();
    }

    /**
     * Returns the integer-array value stored under the given key.
     *
     * <p>The returned array is a defensive clone; mutating it does not change the stored
     * value.
     *
     * @param key the modelled key to read
     * @return the integer-array value, or {@link Optional#empty()} if the key is unset or
     *         holds a non-integer-array value
     */
    public Optional<long[]> getIntegerArray(VoipParamKey key) {
        return values.get(key) instanceof long[] value ? Optional.of(value.clone()) : Optional.empty();
    }

    /**
     * Returns the floating-point-array value stored under the given key.
     *
     * <p>The returned array is a defensive clone; mutating it does not change the stored
     * value.
     *
     * @param key the modelled key to read
     * @return the floating-point-array value, or {@link Optional#empty()} if the key is
     *         unset or holds a non-floating-point-array value
     */
    public Optional<double[]> getDoubleArray(VoipParamKey key) {
        return values.get(key) instanceof double[] value ? Optional.of(value.clone()) : Optional.empty();
    }

    /**
     * Returns whether a value is stored under the given modelled key.
     *
     * @param key the modelled key to test
     * @return {@code true} if the key holds a value, {@code false} otherwise
     */
    public boolean contains(VoipParamKey key) {
        return values.containsKey(key);
    }

    /**
     * Returns the number of modelled tunables currently held.
     *
     * @return the count of set modelled keys
     */
    public int size() {
        return values.size();
    }

    /**
     * Returns whether this set holds no modelled and no unmodelled values.
     *
     * @return {@code true} if the set is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return values.isEmpty() && unmodelled.isEmpty();
    }
}
