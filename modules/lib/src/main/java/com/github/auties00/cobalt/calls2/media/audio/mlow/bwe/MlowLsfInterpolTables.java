package com.github.auties00.cobalt.calls2.media.audio.mlow.bwe;

/**
 * Per-subframe LSF interpolation factor rows shared by the low band and the high band, the native
 * {@code smpl_lsf_interpol_1} / {@code smpl_lsf_interpol_2} / {@code smpl_lsf_interpol_4}
 * ({@code smpl_tables.c}).
 *
 * <p>The high-band LSF interpolation reuses the same per-subframe interpolation-factor rows the low band uses,
 * selected by the frame's {@code lsf_interpol_idx} and the subframe count, the native
 * {@code p_lsf_interpol + lsf_interpol_idx * num_subframes} pointer arithmetic. This holder supplies that
 * factor row to the high-band interpolator so it threads the same blend weights the low-band decode applied;
 * only the non-silence-insertion (non-SID) rows are needed because the high band is engaged only on
 * normally-coded frames.
 *
 * @implNote This implementation transcribes the native interpolation tables verbatim. The single-subframe row
 * is the scalar {@code smpl_lsf_interpol_1} = {@code 0.95f} held as a one-entry row; the two-subframe and
 * four-subframe rows are {@code smpl_lsf_interpol_2} and {@code smpl_lsf_interpol_4}. The comfort-noise
 * ({@code _dtx}) rows are out of scope here because the high band is not synthesized for SID frames.
 */
final class MlowLsfInterpolTables {
    /**
     * The single-subframe interpolation factor, the native {@code smpl_lsf_interpol_1}.
     */
    private static final float[] INTERPOL_1 = {0.95f};

    /**
     * The two-subframe interpolation factor rows, the native {@code smpl_lsf_interpol_2[idx]}.
     */
    private static final float[][] INTERPOL_2 = {
            {0.75f, 1.0f},
            {0.4f, 0.95f}
    };

    /**
     * The four-subframe interpolation factor rows, the native {@code smpl_lsf_interpol_4[idx]}.
     */
    private static final float[][] INTERPOL_4 = {
            {0.55f, 0.88f, 1.0f, 1.0f},
            {0.3f, 0.65f, 0.95f, 1.0f}
    };

    /**
     * Non-instantiable table holder.
     */
    private MlowLsfInterpolTables() {
    }

    /**
     * Returns the per-subframe interpolation factor row for a frame, the native
     * {@code p_lsf_interpol + lsf_interpol_idx * num_subframes}.
     *
     * <p>Selects the table by the subframe count and the row by {@code interpolIdx}; the single-subframe frame
     * has one fixed row regardless of the index.
     *
     * @param interpolIdx  the LSF interpolation index of the frame
     * @param numSubframes the subframe count of the frame; 1, 2, or 4
     * @return the interpolation factor row, {@code numSubframes} entries
     * @throws IllegalArgumentException if {@code numSubframes} is not 1, 2, or 4
     */
    static float[] factors(int interpolIdx, int numSubframes) {
        return switch (numSubframes) {
            case 1 -> INTERPOL_1;
            case 2 -> INTERPOL_2[interpolIdx];
            case 4 -> INTERPOL_4[interpolIdx];
            default -> throw new IllegalArgumentException("invalid subframe count " + numSubframes);
        };
    }
}
