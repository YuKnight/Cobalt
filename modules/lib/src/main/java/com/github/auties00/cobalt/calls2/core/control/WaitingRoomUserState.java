package com.github.auties00.cobalt.calls2.core.control;

import java.util.Optional;

/**
 * Enumerates the lifecycle state the engine tracks for a participant waiting in a call's lobby.
 *
 * <p>While a waiting-room participant moves from requesting admission to joining or being turned away, the
 * engine stores a small integer state for it. Each constant binds the {@link #code() integer code} the
 * engine stores. The state advances as the host acts on the participant: a fresh request is
 * {@link #OUTGOING}, a delivered request is {@link #RECEIPT}, an admitted participant that joins becomes
 * {@link #JOINED}, and a denied or expired participant becomes {@link #TERMINATED}.
 *
 * @implNote This implementation ports the {@code WaitingRoomUserState} values of the wa-voip WASM module
 * {@code ff-tScznZ8P} ({@code waiting_room.cc}): {@code outgoing=1}, {@code receipt=2}, {@code terminated=3},
 * {@code joined=4}. The codes start at {@code 1}; there is no zero state in the recovered table, so
 * {@link #ofCode(int)} yields an empty result for {@code 0} and any value outside {@code 1..4}.
 */
public enum WaitingRoomUserState {
    /**
     * Represents a waiting-room admission request the host has issued but not yet had delivered.
     */
    OUTGOING(1),

    /**
     * Represents a waiting-room admission request whose delivery the engine has acknowledged.
     */
    RECEIPT(2),

    /**
     * Represents a waiting-room participant that has been denied admission or whose request expired.
     */
    TERMINATED(3),

    /**
     * Represents a waiting-room participant that has been admitted and has joined the call.
     */
    JOINED(4);

    /**
     * Holds the integer code the engine stores for this waiting-room state.
     */
    private final int code;

    /**
     * Constructs a waiting-room state constant bound to its engine code.
     *
     * @param code the integer code the engine stores
     */
    WaitingRoomUserState(int code) {
        this.code = code;
    }

    /**
     * Returns the integer code the engine stores for this waiting-room state.
     *
     * @return the engine code, in the range {@code 1} to {@code 4}
     */
    public int code() {
        return code;
    }

    /**
     * Looks up the waiting-room state whose {@linkplain #code() code} equals the given value.
     *
     * <p>The codes start at {@code 1}; a value of {@code 0} or any value outside {@code 1..4} yields an
     * empty result.
     *
     * @param code the engine code to resolve
     * @return the matching waiting-room state, or an empty result when no state carries the code
     */
    public static Optional<WaitingRoomUserState> ofCode(int code) {
        for (var state : values()) {
            if (state.code == code) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
