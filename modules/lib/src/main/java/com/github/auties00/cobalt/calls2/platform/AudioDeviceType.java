package com.github.auties00.cobalt.calls2.platform;

/**
 * Distinguishes the two kinds of audio endpoint a call capture driver can bind.
 *
 * <p>A capture driver captures either the local {@link #MICROPHONE} or the machine's
 * {@link #SYSTEM_AUDIO} (the loopback of what the operating system is playing, used for screen sharing
 * with audio). The kind gates two things: which capture device the driver opens, and the
 * {@code sourceKind} tag the engine routes captured chunks by. The engine treats microphone audio as
 * live acoustic input that needs echo cancellation and mic conditioning, and system audio as already
 * clean line-level audio that does not.
 *
 * <p>Each constant carries the {@link #sourceKind() integer source kind} the engine uses to route a
 * captured chunk to the right capture driver. The convention is that {@link #SYSTEM_AUDIO} is source
 * kind {@code 1} and every other value, including {@link #MICROPHONE}, is not; {@link #ofSourceKind(int)}
 * therefore always resolves and has no error sentinel.
 *
 * @implNote This implementation ports the {@code device_type} field stored at driver-context offset
 * {@code 0x4} in module {@code ff-tScznZ8P} (wa-voip {@code drivers/WasmAudioDriver.cpp}
 * {@code WasmAudioDriver::set_device_type}, fn11846, and the routing in
 * {@code WasmVoipAVDriverManager::processAudioCaptureData}, fn11975). The recovered routing maps
 * {@code sourceKind == 1} to the system-audio capture driver (manager offset {@code 0xc}) and any other
 * source kind to the microphone capture driver (manager offset {@code 0x4}); the device-type field in
 * turn selects the {@code aud_share_avg_loudness_mic} versus {@code system_audio} capture path in
 * {@code start_capture} (fn11854). Confirmed against the live capture in the P4/P5 phase.
 */
public enum AudioDeviceType {
    /**
     * The local microphone: live acoustic capture that needs echo cancellation and mic conditioning.
     *
     * <p>This is the default capture endpoint of a call and the routing target for any source kind
     * other than {@code 1}.
     */
    MICROPHONE(0),

    /**
     * The machine's system audio loopback: the audio the operating system is playing back, captured for
     * screen sharing with audio.
     *
     * <p>This is already clean line-level audio and is the routing target for source kind {@code 1}.
     */
    SYSTEM_AUDIO(1);

    /**
     * Holds the integer source kind the engine uses to route a captured chunk to this driver kind.
     */
    private final int sourceKind;

    /**
     * Constructs a device-type constant bound to its routing source kind.
     *
     * @param sourceKind the integer source kind the engine routes captured chunks by
     */
    AudioDeviceType(int sourceKind) {
        this.sourceKind = sourceKind;
    }

    /**
     * Returns the integer source kind the engine uses to route a captured chunk to this driver kind.
     *
     * @return the routing source kind: {@code 1} for {@link #SYSTEM_AUDIO}, otherwise {@code 0}
     */
    public int sourceKind() {
        return sourceKind;
    }

    /**
     * Returns the device type selected by the given routing source kind.
     *
     * <p>A source kind of {@code 1} resolves to {@link #SYSTEM_AUDIO}; every other value resolves to
     * {@link #MICROPHONE}, matching the engine's default routing.
     *
     * @param sourceKind the routing source kind to classify
     * @return the matching device type, never {@code null}
     */
    public static AudioDeviceType ofSourceKind(int sourceKind) {
        return sourceKind == SYSTEM_AUDIO.sourceKind ? SYSTEM_AUDIO : MICROPHONE;
    }
}
