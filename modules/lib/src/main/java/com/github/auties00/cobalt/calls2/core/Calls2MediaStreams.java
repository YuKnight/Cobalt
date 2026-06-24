package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.calls2.stream.AudioInput;
import com.github.auties00.cobalt.calls2.stream.AudioOutput;
import com.github.auties00.cobalt.calls2.stream.VideoInput;
import com.github.auties00.cobalt.calls2.stream.VideoOutput;

/**
 * Carries the four application-supplied media streams of one call from the service layer into the media
 * plane, grouped by their media-plane role rather than by their public parameter name.
 *
 * <p>The public call API takes four streams: a capture audio source, a playback audio sink, an optional
 * capture video source, and an optional playback video sink. This record is the engine-side bundle the
 * lifecycle controller threads into {@link Calls2MediaPlane#bringUp} so the brought-up
 * {@link Calls2MediaPlane.Session} can drive the encode path from the capture sources and deliver decoded
 * media to the playback sinks. The fields are named for the direction of media flow:
 * <ul>
 *   <li>{@code audioCapture} and {@code videoCapture} are the sources the engine pulls local media from
 *       and encodes ({@link AudioOutput#take()} / {@link VideoOutput#take()}); they correspond to the
 *       public {@code audioOut} / {@code videoOut} parameters.</li>
 *   <li>{@code audioPlayback} and {@code videoPlayback} are the sinks the engine delivers decoded remote
 *       media to ({@link AudioInput#offer(com.github.auties00.cobalt.calls2.stream.AudioFrame)} /
 *       {@link VideoInput#offer(com.github.auties00.cobalt.calls2.stream.VideoFrame)}); they correspond to
 *       the public {@code audioIn} / {@code videoIn} parameters.</li>
 * </ul>
 *
 * <p>A device-backed stream (a microphone capture source, a speaker playback sink) carries the platform
 * device behind the same interface, so the media plane needs no separate device path when a stream is
 * supplied: pulling the capture source captures from its device, and offering to the playback sink renders
 * to its device. A {@code null} field marks an absent stream; {@link #none()} supplies an all-{@code null}
 * bundle for a bring-up that has no application streams (the assembler's media-plane probe), in which case
 * the media plane falls back to opening a platform capture and playback device directly. An audio-only call
 * leaves {@link #videoCapture()} and {@link #videoPlayback()} {@code null}.
 *
 * @apiNote This is an internal engine value carried only between the call service and the media plane;
 * embedders never construct it.
 * @param audioCapture  the local-audio capture source the engine encodes, or {@code null} to fall back to
 *                      a platform capture device
 * @param audioPlayback the remote-audio playback sink the engine renders decoded audio to, or {@code null}
 *                      to fall back to a platform playback device
 * @param videoCapture  the local-video capture source the engine encodes, or {@code null} on an audio-only
 *                      call or when no video source was supplied
 * @param videoPlayback the remote-video playback sink the engine renders decoded video to, or {@code null}
 *                      on an audio-only call or when no video sink was supplied
 */
public record Calls2MediaStreams(AudioOutput audioCapture, AudioInput audioPlayback,
                                 VideoOutput videoCapture, VideoInput videoPlayback) {
    /**
     * The shared empty bundle returned by {@link #none()}.
     *
     * <p>All four fields are {@code null}, so the media plane opens a platform capture and playback device
     * directly rather than driving an application stream.
     */
    private static final Calls2MediaStreams NONE = new Calls2MediaStreams(null, null, null, null);

    /**
     * Returns the all-{@code null} bundle for a bring-up with no application streams.
     *
     * <p>Used by a media-plane bring-up that carries no application capture or playback streams, such as the
     * engine assembler's probe; the media plane then opens a platform capture and playback device directly.
     *
     * @return the shared empty media-streams bundle
     */
    public static Calls2MediaStreams none() {
        return NONE;
    }
}
