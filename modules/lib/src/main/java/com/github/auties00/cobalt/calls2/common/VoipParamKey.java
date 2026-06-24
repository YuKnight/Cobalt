package com.github.auties00.cobalt.calls2.common;

import java.util.List;
import java.util.Optional;

/**
 * Enumerates the native voip-param registry keys recovered from WhatsApp Web's
 * {@code voip_param_entry} descriptor writes.
 *
 * <p>The keys are generated directly from the wa-voip WASM module. Each key is backed by an
 * observed write to the 20-byte native descriptor shape used by {@code reg_param_entry_impl};
 * the value type, byte width, and rate-control flag are copied from those writes, not
 * inferred from names or JSON values.
 *
 * <p>The catalogue contains descriptor entries only. JSON section roots such as
 * {@code p-&gt;aec} and metadata fields such as {@code voip_settings_version} are not native
 * descriptor entries and are retained by {@link VoipParamJsonDeserializer} only as flattened
 * unmodelled values.
 *
 * <p>The keys are partitioned across the permitted enums by namespace ({@code p-&gt;},
 * {@code mvp-&gt;}/{@code vp-&gt;}, {@code tp-&gt;}); the {@code p-&gt;} namespace is split further so
 * that no generated enum's static initializer exceeds the JVM 64KB method-size limit. The
 * full key set is the union of every partition, exposed through {@link #values()}.
 */
public sealed interface VoipParamKey permits VoipParamKeyMedia1, VoipParamKeyCall1, VoipParamKeyCall2, VoipParamKeyCall3, VoipParamKeyTransport1 {
    /**
     * Returns the fully-qualified dotted path the engine addresses this tunable by.
     *
     * @return the dotted path, such as {@code "p-&gt;conds.cond_range_ul_bwe"}
     */
    String dottedPath();

    /**
     * Returns the native descriptor value type for this tunable.
     *
     * @return the native descriptor value type
     */
    VoipParamType type();

    /**
     * Returns the serialized byte width recorded in the native descriptor.
     *
     * @return the serialized byte width
     */
    int byteWidth();

    /**
     * Returns whether the native descriptor marks this tunable as rate-control related.
     *
     * @return {@code true} if this is a rate-control tunable, {@code false} otherwise
     */
    boolean bweParam();

    /**
     * Returns every modelled key, unioned across all partitions in generation order.
     *
     * @return an unmodifiable list of all modelled keys
     */
    static List<VoipParamKey> values() {
        return VoipParamKeyCatalogue.ALL;
    }

    /**
     * Returns the key whose {@linkplain #dottedPath() dotted path} equals the given value.
     *
     * @param dottedPath the dotted path to resolve
     * @return the matching key, or {@link Optional#empty()} if the path is not modelled
     */
    static Optional<VoipParamKey> ofDottedPath(String dottedPath) {
        return Optional.ofNullable(VoipParamKeyCatalogue.BY_DOTTED_PATH.get(dottedPath));
    }
}
