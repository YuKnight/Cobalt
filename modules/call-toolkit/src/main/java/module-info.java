/**
 * Convenience built-ins for working with Cobalt calls.
 *
 * <p>Discoverability: start at
 * {@link com.github.auties00.cobalt.call.toolkit.CallToolkit} —
 * a single facade that surfaces the common cases (microphone,
 * speaker, camera, screen, file playback, recording, mixing,
 * metering) with sensible defaults that already match the call
 * wire profile.
 *
 * <p>Apps that drive their own pipeline (a server bridging two
 * calls, a synthetic-audio bot, a file-replay tester) don't pull
 * this module. Apps that want the batteries-included path do.
 *
 * <p>The {@code input}, {@code output}, and {@code transform}
 * packages contain the concrete implementations behind the
 * facade. Pure-Java helpers (microphone, speaker, mixer,
 * resampler, mute switch, pipe, level meter, WAV file, tone /
 * silence generators) need no natives; FFmpeg-backed helpers
 * (camera, screen, media files, MKV recorder) load the FFmpeg
 * shared libraries via {@link com.github.auties00.cobalt.util.NativeLibLoader}
 * against this module's classifier-specific natives JAR or
 * download from this repo on first use.
 */
module com.github.auties00.cobalt.call.toolkit {
    requires com.github.auties00.cobalt;

    requires java.desktop;        // javax.sound.sampled (mic + speaker)
    requires java.logging;

    exports com.github.auties00.cobalt.call.toolkit;
    exports com.github.auties00.cobalt.call.toolkit.input;
    exports com.github.auties00.cobalt.call.toolkit.output;
    exports com.github.auties00.cobalt.call.toolkit.transform;
}
