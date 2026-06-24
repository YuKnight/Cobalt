package com.github.auties00.cobalt.calls2.media.audio;

/**
 * Enumerates the four per-sample-rate default Opus configurations the call engine seeds an encoder
 * from, the typed form of the static {@code opus_default_attr[4]} table.
 *
 * <p>When a stream negotiates a sample rate, the engine looks up the row whose {@link #sampleRate()}
 * matches and copies its default, minimum, and maximum bitrate, its bandwidth-enum column, and its
 * frame-time ceiling into the encoder parameters. The minimum-bitrate floor is uniformly 6000 bps
 * across all rows; the default and maximum scale with the sample rate. The bandwidth-enum column is a
 * per-row index distinct from the {@link OpusBandwidth} libopus codes: it is the value the native
 * table stores ({@code 0/2/3/4}), which the engine maps onto a maximum-bandwidth ceiling.
 *
 * @implNote This implementation reproduces the {@code opus_default_attr[4]} table at DAT
 * {@code 0x136240} (stride {@code 0x1c == 28}, 4 entries) of the wa-voip WASM module
 * {@code ff-tScznZ8P}, selected by {@code fn6253} on a sample-rate match against column {@code +0x18}.
 * The native row layout is {@code {Fs, default_bps, min_bps, max_bps, name_ptr, frame_ms_ceiling=120,
 * bandwidth_enum}}; the in-memory row order is WB, NB, SWB, FB, but the constants here are declared
 * ascending by sample rate (NB, WB, SWB, FB) for readability. The name-pointer column is omitted as it
 * carries no runtime value beyond logging. The {@code 120} frame-time ceiling column is retained as
 * {@link #frameMillisCeiling()}; resolving its exact unit semantics is an open question on the
 * recovered table (the SPEC marks it as a frame-size ceiling).
 */
public enum OpusDefaultAttr {
    /**
     * Narrowband at 8 kHz: default 12000 bps, range {@code 6000..16000}, bandwidth-enum column
     * {@code 0}.
     */
    NB(8000, 12000, 6000, 16000, 0),

    /**
     * Wideband at 16 kHz: default 25000 bps, range {@code 6000..32000}, bandwidth-enum column
     * {@code 2}.
     */
    WB(16000, 25000, 6000, 32000, 2),

    /**
     * Super-wideband at 24 kHz: default 32000 bps, range {@code 6000..32000}, bandwidth-enum column
     * {@code 3}.
     */
    SWB(24000, 32000, 6000, 32000, 3),

    /**
     * Fullband at 48 kHz: default 40000 bps, range {@code 6000..40000}, bandwidth-enum column
     * {@code 4}.
     */
    FB(48000, 40000, 6000, 40000, 4);

    /**
     * The minimum bitrate floor, in bits per second, shared by every row of the table.
     *
     * <p>Equal to 6000 bps; the native table stores this value in the minimum-bitrate column of all
     * four rows.
     */
    public static final int MIN_BITRATE_FLOOR = 6000;

    /**
     * The frame-time ceiling column value shared by every row of the native table.
     *
     * <p>Equal to {@code 120}; surfaced as {@link #frameMillisCeiling()} on each row.
     */
    public static final int FRAME_MILLIS_CEILING = 120;

    /**
     * Holds the input sample rate, in Hz, this row is keyed on.
     */
    private final int sampleRate;

    /**
     * Holds the default target bitrate, in bits per second, for this row.
     */
    private final int defaultBitrate;

    /**
     * Holds the minimum target bitrate, in bits per second, for this row.
     */
    private final int minBitrate;

    /**
     * Holds the maximum target bitrate, in bits per second, for this row.
     */
    private final int maxBitrate;

    /**
     * Holds the native bandwidth-enum column value for this row.
     */
    private final int bandwidthEnum;

    /**
     * Constructs a row bound to its sample rate, bitrate triplet, and bandwidth-enum column.
     *
     * @param sampleRate     the input sample rate in Hz
     * @param defaultBitrate the default target bitrate in bits per second
     * @param minBitrate     the minimum target bitrate in bits per second
     * @param maxBitrate     the maximum target bitrate in bits per second
     * @param bandwidthEnum  the native bandwidth-enum column value
     */
    OpusDefaultAttr(int sampleRate, int defaultBitrate, int minBitrate, int maxBitrate, int bandwidthEnum) {
        this.sampleRate = sampleRate;
        this.defaultBitrate = defaultBitrate;
        this.minBitrate = minBitrate;
        this.maxBitrate = maxBitrate;
        this.bandwidthEnum = bandwidthEnum;
    }

    /**
     * Returns the input sample rate, in Hz, this row is keyed on.
     *
     * @return the sample rate in Hz
     */
    public int sampleRate() {
        return sampleRate;
    }

    /**
     * Returns the default target bitrate, in bits per second, for this row.
     *
     * @return the default bitrate in bits per second
     */
    public int defaultBitrate() {
        return defaultBitrate;
    }

    /**
     * Returns the minimum target bitrate, in bits per second, for this row.
     *
     * <p>Equal to {@link #MIN_BITRATE_FLOOR} for every row.
     *
     * @return the minimum bitrate in bits per second
     */
    public int minBitrate() {
        return minBitrate;
    }

    /**
     * Returns the maximum target bitrate, in bits per second, for this row.
     *
     * @return the maximum bitrate in bits per second
     */
    public int maxBitrate() {
        return maxBitrate;
    }

    /**
     * Returns the native bandwidth-enum column value for this row.
     *
     * <p>This is the per-row index the wa table stores ({@code 0/2/3/4}), not the {@link OpusBandwidth}
     * libopus code; it is the value the engine maps onto a maximum-bandwidth ceiling.
     *
     * @return the bandwidth-enum column value
     */
    public int bandwidthEnum() {
        return bandwidthEnum;
    }

    /**
     * Returns the frame-time ceiling column value for this row.
     *
     * @return {@link #FRAME_MILLIS_CEILING}
     */
    public int frameMillisCeiling() {
        return FRAME_MILLIS_CEILING;
    }

    /**
     * Returns the default-attribute row for the given sample rate.
     *
     * @param sampleRate the input sample rate in Hz; one of {@code 8000}, {@code 16000},
     *                   {@code 24000}, {@code 48000}
     * @return the matching row
     * @throws IllegalArgumentException if no row is keyed on {@code sampleRate}
     */
    public static OpusDefaultAttr ofSampleRate(int sampleRate) {
        for (var attr : values()) {
            if (attr.sampleRate == sampleRate) {
                return attr;
            }
        }
        throw new IllegalArgumentException("No Opus default-attr row for sample rate: " + sampleRate);
    }
}
