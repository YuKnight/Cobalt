package com.github.auties00.cobalt.wam.type;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Maps a raw integer count (for example a fanout device count) to the
 * {@link SizeBucket} enum used by WAM telemetry.
 *
 * <p>Mirrors the cascading ternary of {@code WAWebWamNumberToSizeBucket},
 * which is the helper used by virtually every WAM emission that carries a
 * {@code deviceSizeBucket} property.
 *
 * @implNote WAWebWamNumberToSizeBucket: the single default-exported function
 *     returns the first matching {@link SizeBucket} in ascending-threshold
 *     order and falls through to {@link SizeBucket#LARGEST_BUCKET} for values
 *     at or above 5000.
 */
@WhatsAppWebModule(moduleName = "WAWebWamNumberToSizeBucket")
public final class WamSizeBuckets {
    /**
     * Private constructor to prevent instantiation.
     */
    private WamSizeBuckets() {
    }

    /**
     * Maps a count to the matching {@link SizeBucket}.
     *
     * <p>Buckets are exclusive upper bounds: {@code count=31} returns
     * {@link SizeBucket#LT32}, {@code count=1024} returns
     * {@link SizeBucket#LT1500}, and any count {@code >= 5000} returns
     * {@link SizeBucket#LARGEST_BUCKET}.
     *
     * @param count the count to classify
     * @return the matching {@link SizeBucket}; never {@code null}
     *
     * @implNote WAWebWamNumberToSizeBucket: the cascading ternary on the
     *     default export; Cobalt mirrors the exact thresholds.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamNumberToSizeBucket",
            exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static SizeBucket numberToSizeBucket(int count) {
        // WAWebWamNumberToSizeBucket: e<32 ? LT32 : e<64 ? LT64 : ... : LARGEST_BUCKET
        if (count < 32) return SizeBucket.LT32;
        if (count < 64) return SizeBucket.LT64;
        if (count < 128) return SizeBucket.LT128;
        if (count < 256) return SizeBucket.LT256;
        if (count < 512) return SizeBucket.LT512;
        if (count < 1024) return SizeBucket.LT1024;
        if (count < 1500) return SizeBucket.LT1500;
        if (count < 2000) return SizeBucket.LT2000;
        if (count < 2500) return SizeBucket.LT2500;
        if (count < 3000) return SizeBucket.LT3000;
        if (count < 3500) return SizeBucket.LT3500;
        if (count < 4000) return SizeBucket.LT4000;
        if (count < 4500) return SizeBucket.LT4500;
        if (count < 5000) return SizeBucket.LT5000;
        return SizeBucket.LARGEST_BUCKET;
    }
}
