package com.github.auties00.cobalt.calls2.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aggregates the partitioned {@link VoipParamKey} catalogue into the union views the public
 * accessors return.
 *
 * <p>The key set is generated into several enum partitions to keep each enum's static
 * initializer within the JVM 64KB method-size limit; this holder unions them once at class
 * load so {@link VoipParamKey#values()} and {@link VoipParamKey#ofDottedPath(String)} answer
 * without rescanning each partition.
 */
final class VoipParamKeyCatalogue {
    /**
     * Every modelled key, unioned across all partitions in generation order.
     */
    static final List<VoipParamKey> ALL = Stream.<VoipParamKey[]>of(
            VoipParamKeyMedia1.values(),
            VoipParamKeyCall1.values(),
            VoipParamKeyCall2.values(),
            VoipParamKeyCall3.values(),
            VoipParamKeyTransport1.values()
    ).flatMap(Arrays::stream).toList();

    /**
     * The dotted-path lookup over {@link #ALL}.
     */
    static final Map<String, VoipParamKey> BY_DOTTED_PATH = ALL.stream()
            .collect(Collectors.toUnmodifiableMap(VoipParamKey::dottedPath, key -> key));

    /**
     * Prevents instantiation of this static holder.
     */
    private VoipParamKeyCatalogue() {
    }
}
