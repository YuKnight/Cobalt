package com.github.auties00.cobalt.call.toolkit;

import com.github.auties00.cobalt.call.io.AudioSink;
import com.github.auties00.cobalt.call.io.AudioSource;
import com.github.auties00.cobalt.call.io.VideoSource;
import com.github.auties00.cobalt.call.toolkit.input.AudioFile;
import com.github.auties00.cobalt.call.toolkit.input.Camera;
import com.github.auties00.cobalt.call.toolkit.input.Microphone;
import com.github.auties00.cobalt.call.toolkit.input.Screen;
import com.github.auties00.cobalt.call.toolkit.input.Silence;
import com.github.auties00.cobalt.call.toolkit.input.Tone;
import com.github.auties00.cobalt.call.toolkit.input.VideoFile;
import com.github.auties00.cobalt.call.toolkit.output.AudioMixer;
import com.github.auties00.cobalt.call.toolkit.output.CallRecorder;
import com.github.auties00.cobalt.call.toolkit.output.LevelMeter;
import com.github.auties00.cobalt.call.toolkit.output.Speaker;
import com.github.auties00.cobalt.call.toolkit.output.WavFile;
import com.github.auties00.cobalt.call.toolkit.transform.AudioPipe;
import com.github.auties00.cobalt.call.toolkit.transform.MuteSwitch;
import com.github.auties00.cobalt.call.toolkit.transform.ResamplingSink;
import com.github.auties00.cobalt.call.toolkit.transform.ResamplingSource;

import java.nio.file.Path;

/**
 * Single discoverable entry point for the call-toolkit. Static
 * factories cover the common cases — local microphone / speaker,
 * file playback / recording, screen capture, mixing, metering —
 * with sensible defaults that match the Cobalt call wire profile,
 * so callers don't have to know about Opus, VP8, sample rates, or
 * I420.
 *
 * <p>Power users can always construct the underlying classes
 * directly (e.g. {@code new Camera("v4l2", "/dev/video1")} for an
 * unusual device URL); this facade is the friendly path.
 */
public final class CallToolkit {
    /**
     * Prevents instantiation.
     */
    private CallToolkit() {
        throw new AssertionError("CallToolkit is not instantiable");
    }

    /**
     * Returns a {@link Microphone} configured for the call wire
     * profile (16 kHz mono, 10 ms frames, JVM-default mixer). Caller
     * still needs to invoke {@link Microphone#open()} before reading.
     *
     * @return the microphone source
     */
    public static Microphone microphone() {
        return new Microphone();
    }

    /**
     * Returns an {@link AudioFile} source playing back the given
     * media file as 16 kHz mono PCM.
     *
     * @param path the media file
     * @return the source (close it when done)
     */
    public static AudioFile playAudio(Path path) {
        return new AudioFile(path);
    }

    /**
     * Returns a continuous sine-wave {@link Tone} at the given
     * frequency.
     *
     * @param frequencyHz the tone frequency in Hz
     * @return the source
     */
    public static Tone sine(int frequencyHz) {
        return Tone.sine(frequencyHz);
    }

    /**
     * Returns a Western-European ringback {@link Tone} (425 Hz
     * on 1 s / off 4 s).
     *
     * @return the source
     */
    public static Tone ringback() {
        return Tone.ringback();
    }

    /**
     * Returns a DTMF {@link Tone} for the given digit.
     *
     * @param digit one of {@code 0-9 * # A B C D}
     * @return the source
     */
    public static Tone dtmf(char digit) {
        return Tone.dtmf(digit);
    }

    /**
     * Returns a {@link Silence} source emitting zero-PCM frames
     * at the call's cadence.
     *
     * @return the source
     */
    public static Silence silence() {
        return new Silence();
    }

    /**
     * Opens the platform's default camera as a {@link Camera}
     * source.
     *
     * @return the source (close it when done)
     */
    public static Camera camera() {
        return new Camera();
    }

    /**
     * Opens a camera by device URL or name (platform-aware:
     * device path on Linux, index on macOS, friendly name on
     * Windows).
     *
     * @param deviceUrl the device URL or name
     * @return the source
     */
    public static Camera camera(String deviceUrl) {
        return new Camera(deviceUrl);
    }

    /**
     * Returns the platform's primary-screen capture as a
     * {@link VideoSource}. On Linux this assumes X11; Wayland
     * users should call {@link Screen#wayland(String)} directly.
     *
     * @return the source
     */
    public static VideoSource screen() {
        return Screen.primary();
    }

    /**
     * Returns a {@link VideoFile} source playing back the video
     * track of the given media file as I420 frames.
     *
     * @param path the media file
     * @return the source (close it when done)
     */
    public static VideoFile playVideo(Path path) {
        return new VideoFile(path);
    }

    /**
     * Returns a {@link Speaker} configured for the call wire
     * profile. Caller invokes {@link Speaker#open()} before
     * writing.
     *
     * @return the speaker sink
     */
    public static Speaker speaker() {
        return new Speaker();
    }

    /**
     * Returns a {@link WavFile} sink that writes 16 kHz mono PCM
     * to a WAV file.
     *
     * @param path the output file
     * @return the sink (close it to finalise the file)
     */
    public static WavFile recordWav(Path path) {
        return new WavFile(path);
    }

    /**
     * Returns a fresh {@link AudioMixer} for fan-in mixing of
     * group-call peer audio.
     *
     * @return the mixer
     */
    public static AudioMixer mixer() {
        return new AudioMixer();
    }

    /**
     * Returns a meter-only {@link LevelMeter} — useful for VU
     * displays where the audio is consumed elsewhere.
     *
     * @return the meter
     */
    public static LevelMeter levelMeter() {
        return new LevelMeter();
    }

    /**
     * Returns a {@link LevelMeter} that taps into the audio path
     * and forwards every frame to {@code downstream}.
     *
     * @param downstream the next sink in the chain
     * @return the meter
     */
    public static LevelMeter levelMeter(AudioSink downstream) {
        return new LevelMeter(downstream);
    }

    /**
     * Returns a {@link CallRecorder} configured to mux the
     * Cobalt call wire defaults — Opus 48 kHz mono audio + VP8
     * video at the given dimensions — into an MKV file.
     *
     * @param path        the output MKV file
     * @param videoWidth  the video frame width
     * @param videoHeight the video frame height
     * @return the recorder
     */
    public static CallRecorder recordCall(Path path, int videoWidth, int videoHeight) {
        return new CallRecorder(path,
                CallRecorder.AudioCodec.OPUS, 48_000, 1,
                CallRecorder.VideoCodec.VP8, videoWidth, videoHeight);
    }

    /**
     * Returns an audio-only {@link CallRecorder} muxing Opus
     * 48 kHz mono into MKV.
     *
     * @param path the output MKV file
     * @return the recorder
     */
    public static CallRecorder recordVoiceCall(Path path) {
        return new CallRecorder(path,
                CallRecorder.AudioCodec.OPUS, 48_000, 1,
                null, 0, 0);
    }

    /**
     * Wraps {@code source} with a software mute switch. The
     * returned wrapper is itself an {@link AudioSource} — start
     * and stop muting via
     * {@link MuteSwitch#setMuted(boolean)}.
     *
     * @param source the source to wrap
     * @return the mute switch wrapper
     */
    public static MuteSwitch muteSwitch(AudioSource source) {
        return new MuteSwitch(source);
    }

    /**
     * Returns a fresh in-process {@link AudioPipe} — a
     * connected sink + source pair.
     *
     * @return the pipe
     */
    public static AudioPipe pipe() {
        return new AudioPipe();
    }

    /**
     * Wraps {@code source} with a sample-rate converter that
     * emits frames of {@code outFrameSize} samples at
     * {@code outSampleRate}. Use this when a producer captures at
     * one rate (e.g. 48 kHz mic) and the call expects another
     * (e.g. 16 kHz wire).
     *
     * @param source        the underlying source
     * @param inSampleRate  input rate
     * @param outSampleRate output rate
     * @param outFrameSize  samples per emitted frame
     * @return the resampling wrapper
     */
    public static AudioSource resample(AudioSource source, int inSampleRate, int outSampleRate, int outFrameSize) {
        return new ResamplingSource(source, inSampleRate, outSampleRate, outFrameSize);
    }

    /**
     * Wraps {@code sink} with a sample-rate converter that
     * accepts frames at {@code inSampleRate} and forwards them at
     * {@code outSampleRate}.
     *
     * @param sink          the downstream sink
     * @param inSampleRate  input rate
     * @param outSampleRate output rate
     * @return the resampling wrapper
     */
    public static AudioSink resample(AudioSink sink, int inSampleRate, int outSampleRate) {
        return new ResamplingSink(sink, inSampleRate, outSampleRate);
    }
}
