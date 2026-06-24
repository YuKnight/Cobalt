package com.github.auties00.cobalt.calls2.media.sframe;


import java.util.Arrays;

/**
 * The sliding replay window that rejects already-seen or stale SFrame frame counters, backed by a
 * {@value #WINDOW_SIZE}-bit bitmap.
 *
 * <p>SFrame frame counters within one key id are monotonically increasing at the sender, but the
 * relay path can reorder or duplicate frames. The window tracks the highest accepted counter as the
 * upper edge and a bitmap of the {@value #WINDOW_SIZE} counters below it; a counter is acceptable if
 * it is newer than the upper edge, or if it falls inside the window and its bit is not yet set.
 * Counters equal to or older than the bottom of the window, and counters whose bit is already set,
 * are replays and are rejected. Accepting a counter newer than the upper edge slides the window up
 * and clears the bits of the slots that scroll out from the bottom.
 *
 * <p>The window also rejects a zero counter outright: the decoder treats {@code counter == 0} as
 * malformed because the sender's first frame uses a positive counter. An instance is single-writer,
 * driven by the decode path of one direction; it holds no internal lock.
 *
 * @implNote This implementation reproduces the {@code 0x800}-slot replay ring at
 * {@code SecureFrameTransform+0x38} and {@code sframe_replay_bitmap_setrange} (fn6945) consumed by
 * {@code wa_sframe_decrypt} (fn6900) of the wa-voip WASM module {@code ff-tScznZ8P}. The native
 * decoder rejects {@code counter == 0} with code {@code 0xb}, rejects a counter at or below the ring
 * base or whose bit is set as a replay (code {@code 0x9}), checks and sets the bit for a counter
 * inside {@code [base, base+0x800)}, and on a counter at or beyond {@code base+0x800} advances the
 * base and clears the vacated bits. The window size {@code 0x800} is the recovered ring width.
 */
public final class SFrameReplayWindow {
    /**
     * Holds the replay window width in counter slots ({@code 0x800} = 2048 bits = 256 bytes).
     */
    public static final int WINDOW_SIZE = 0x800;

    /**
     * Holds the bitmap, one bit per in-window counter slot, indexed by {@code counter mod WINDOW_SIZE}.
     */
    private final byte[] bitmap = new byte[WINDOW_SIZE / 8];

    /**
     * Holds the highest accepted counter, the upper edge of the window, or {@code -1} before any
     * frame has been accepted.
     */
    private long highest = -1;

    /**
     * Constructs an empty replay window with no accepted counters.
     */
    public SFrameReplayWindow() {
    }

    /**
     * Returns whether {@code counter} would be accepted right now without recording it.
     *
     * <p>A counter is acceptable when it is non-zero and either newer than the highest accepted
     * counter, or inside the window with its bit unset. A zero counter, a counter at or below the
     * bottom of the window, or a counter whose bit is set is a replay.
     *
     * @param counter the inbound frame counter
     * @return {@code true} if the counter is acceptable, {@code false} if it is a replay or zero
     */
    public boolean isAcceptable(long counter) {
        if (counter == 0) {
            return false;
        }
        if (highest < 0 || Long.compareUnsigned(counter, highest) > 0) {
            return true;
        }
        if (Long.compareUnsigned(highest - counter, WINDOW_SIZE) >= 0) {
            return false;
        }
        return !isSet(counter);
    }

    /**
     * Records {@code counter} as accepted, sliding the window up and clearing vacated slots if the
     * counter is newer than the current upper edge.
     *
     * <p>This must be called only after {@link #isAcceptable(long)} returns {@code true} for the same
     * counter, mirroring the native decoder marking the counter consumed after a successful
     * authentication and decrypt.
     *
     * @param counter the accepted frame counter
     */
    public void accept(long counter) {
        if (highest < 0 || Long.compareUnsigned(counter, highest) > 0) {
            slideUpTo(counter);
            highest = counter;
        }
        setBit(counter);
    }

    /**
     * Slides the window up to a new upper edge, clearing the bits of the counters that scroll out of
     * the window.
     *
     * <p>When the new edge is at least {@value #WINDOW_SIZE} above the old edge the whole window has
     * rolled over and every bit is cleared; otherwise only the slots between the old edge (exclusive)
     * and the new edge (exclusive) are cleared so they start fresh before {@link #accept(long)} sets
     * the new edge's own bit.
     *
     * @param newHighest the new upper edge counter
     */
    private void slideUpTo(long newHighest) {
        if (highest < 0 || Long.compareUnsigned(newHighest - highest, WINDOW_SIZE) >= 0) {
            Arrays.fill(bitmap, (byte) 0);
            return;
        }
        for (var counter = highest + 1; Long.compareUnsigned(counter, newHighest) < 0; counter++) {
            clearBit(counter);
        }
    }

    /**
     * Returns whether the bit for {@code counter}'s window slot is set.
     *
     * @param counter the counter whose slot is tested
     * @return {@code true} if the slot's bit is set
     */
    private boolean isSet(long counter) {
        var slot = slot(counter);
        return (bitmap[slot >>> 3] & (1 << (slot & 7))) != 0;
    }

    /**
     * Sets the bit for {@code counter}'s window slot.
     *
     * @param counter the counter whose slot is marked
     */
    private void setBit(long counter) {
        var slot = slot(counter);
        bitmap[slot >>> 3] |= (byte) (1 << (slot & 7));
    }

    /**
     * Clears the bit for {@code counter}'s window slot.
     *
     * @param counter the counter whose slot is cleared
     */
    private void clearBit(long counter) {
        var slot = slot(counter);
        bitmap[slot >>> 3] &= (byte) ~(1 << (slot & 7));
    }

    /**
     * Maps a counter to its window slot index, {@code counter mod WINDOW_SIZE}.
     *
     * @param counter the counter to map
     * @return the slot index in {@code [0, WINDOW_SIZE)}
     */
    private static int slot(long counter) {
        return (int) (counter & (WINDOW_SIZE - 1));
    }
}
