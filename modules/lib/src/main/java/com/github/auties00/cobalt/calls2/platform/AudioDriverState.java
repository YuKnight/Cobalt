package com.github.auties00.cobalt.calls2.platform;

import java.util.Optional;

/**
 * Enumerates the three lifecycle states shared by the call audio capture and playback drivers.
 *
 * <p>The wa-voip audio drivers are a strict three-state machine. A freshly constructed driver is
 * {@link #UNINITIALIZED}; the engine first calls its initialization downcall to bind a device and
 * record the requested format, moving it to {@link #INITIALIZED}; it then issues a start downcall to
 * begin streaming samples, moving it to {@link #ACTIVE}; a stop downcall returns it to
 * {@link #INITIALIZED}. The transitions are linear and adjacent: there is no path that skips a state,
 * and a start before initialization is the recoverable {@code Driver not initialized} fault rather than
 * a state change. Each constant binds the {@link #code() native integer code} the driver struct stores
 * in its state field.
 *
 * <p>The {@link #code()} coincides with this enum's {@link Enum#ordinal()} because the native values are
 * contiguous from {@code 0} to {@code 2} and are declared here in numeric order; the explicit accessor
 * is nonetheless the contract, since declaration order is not a protocol guarantee. This type only names
 * the states shared by {@link AudioCaptureDriver} and {@link AudioPlaybackDriver}; the per-driver
 * methods enforce which transitions are legal.
 *
 * @implNote This implementation ports the audio-driver state field stored at driver-context offset
 * {@code 0x8} in module {@code ff-tScznZ8P} (wa-voip {@code drivers/WasmAudioDriver.cpp}): the
 * initialization downcall ({@code WasmAudioDriver::init_capture_driver}, fn11847) sets it to {@code 1},
 * and the start downcall ({@code WasmAudioDriver::start_capture}, fn11854) sets it to {@code 2}. The
 * engine logs {@code already initialized} when re-initializing and {@code Driver not initialized} when
 * starting from {@code 0}; this enum models the values and {@link #ofCode(int)} resolves an unknown code
 * to an empty result. Confirmed against the live capture in the P4/P5 phase.
 */
public enum AudioDriverState {
    /**
     * Represents a constructed driver that holds no device and no format: the initial state.
     *
     * <p>Only an initialization call is legal here; a start in this state is rejected as the
     * recoverable {@code Driver not initialized} fault.
     */
    UNINITIALIZED(0),

    /**
     * Represents a driver that has bound a device and recorded its capture or playback format but is not
     * streaming samples.
     *
     * <p>A start moves the driver to {@link #ACTIVE}; a stop from {@link #ACTIVE} returns it here; a
     * second initialization re-binds the device and stays here.
     */
    INITIALIZED(1),

    /**
     * Represents a driver that is streaming samples to or from its bound device.
     *
     * <p>This is the running state entered by a start call. A stop returns the driver to
     * {@link #INITIALIZED} without releasing the device.
     */
    ACTIVE(2);

    /**
     * Holds the native integer code the driver context stores in its state field.
     */
    private final int code;

    /**
     * Constructs a state constant bound to its native code.
     *
     * @param code the native integer code stored in the driver context state field
     */
    AudioDriverState(int code) {
        this.code = code;
    }

    /**
     * Returns the native integer code the driver context stores in its state field.
     *
     * @return the native state code, in the range {@code 0} to {@code 2}
     */
    public int code() {
        return code;
    }

    /**
     * Looks up the state for a native state code.
     *
     * <p>The lookup is keyed on the protocol value. A value outside the range {@code 0} to {@code 2}
     * yields an empty result, mirroring the engine's unknown-state handling.
     *
     * @param code the native state code
     * @return the matching state, or an empty result when the value is out of range
     */
    public static Optional<AudioDriverState> ofCode(int code) {
        if (code < 0 || code >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[code]);
    }
}
