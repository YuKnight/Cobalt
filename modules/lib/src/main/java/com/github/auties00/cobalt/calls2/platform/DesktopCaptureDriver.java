package com.github.auties00.cobalt.calls2.platform;

import com.github.auties00.cobalt.calls2.platform.VideoCaptureDriver.State;
import com.github.auties00.cobalt.calls2.platform.VideoCaptureDriver.VideoCaptureCapability;
import com.github.auties00.cobalt.calls2.platform.VideoCaptureDriver.VideoSink;
import com.github.auties00.cobalt.calls2.stream.VideoFrame;

/**
 * Defines the engine-facing screen-share capture endpoint of a call: the host-side driver the call
 * engine drives to obtain uncompressed frames of a screen, window, or display surface.
 *
 * <p>This is the JVM analogue of the engine's {@code WasmDesktopCaptureDriver}. It shares the
 * four-state lifecycle and the frame-forwarding contract of {@link VideoCaptureDriver}, reusing that
 * type's {@link State}, {@link VideoSink}, and {@link VideoCaptureCapability} so a screen source and a
 * camera source forward frames identically: {@link State#VOID} holds no surface;
 * {@link #initDriver(String, VideoSink)} acquires the named surface and latches the sink, reaching
 * {@link State#INITIALIZED}; {@link #start(VideoCaptureCapability)} begins capture, reaching
 * {@link State#RUNNING}; {@link #stop()} releases capture to {@link State#INITIALIZED}; and
 * {@link State#INTERRUPTED} is the host-loss state a running capture enters when the operating system
 * revokes the surface (the user stops sharing or the shared window closes). While {@link State#RUNNING},
 * {@link #sendVideoToSink(VideoFrame)} forwards each captured frame, dropping a frame whose dimensions
 * changed and dropping any frame offered while not running or with no sink latched.
 *
 * <p>The one capability a screen source has that a camera source does not is runtime reconfiguration:
 * {@link #setConfig(VideoCaptureCapability)} updates the capture geometry and frame-rate ceiling of an
 * already-initialized driver in place, which a screen share needs because the shared surface's pixel
 * dimensions and the rate the bandwidth estimator allows both change during a call, whereas a camera is
 * reconfigured only by a fresh {@link #start(VideoCaptureCapability)}.
 *
 * <p>This interface is sealed and permits only {@link LiveDesktopCaptureDriver}, the production
 * implementation, per the {@code Live*} convention.
 *
 * @apiNote Embedders do not implement this interface; it is the engine-host seam, and the engine owns
 * the driver and the {@link VideoSink}. An embedder that wants to supply its own screen pictures uses a
 * screen-backed {@link com.github.auties00.cobalt.calls2.stream.VideoOutput} source instead; this
 * driver is the internal capture side the engine wires onto an operating-system display surface.
 * @implNote This implementation mirrors the engine's {@code whatsapp::wasm::WasmDesktopCaptureDriver}
 * from xplat/wa-voip/platforms/wasm/drivers/WasmDesktopCaptureDriver.cpp. No
 * {@code com.github.auties00.cobalt.meta} provenance annotation is attached because that driver is a
 * portable wa-voip C++ class compiled into the WASM engine, not a {@code WAWeb*} JavaScript module nor a
 * named native mobile class, so it maps to no annotation target; the engine-source citation here is the
 * provenance.
 */
public sealed interface DesktopCaptureDriver permits LiveDesktopCaptureDriver {
    /**
     * Returns the driver's current lifecycle state.
     *
     * <p>The returned value reflects the most recent successful transition driven by
     * {@link #initDriver(String, VideoSink)}, {@link #start(VideoCaptureCapability)}, or
     * {@link #stop()}, or a host-loss transition into {@link State#INTERRUPTED}. Never {@code null}.
     *
     * @return the current {@link State}; never {@code null}
     */
    State state();

    /**
     * Acquires the named screen-share surface, latches the engine sink, and moves the driver to
     * {@link State#INITIALIZED}.
     *
     * <p>Accepted only while the driver is {@link State#VOID}. The {@code surfaceId} names the
     * operating-system display, window, or capture target to bind in an implementation-defined form, and
     * {@code sink} is the engine callback every subsequently captured frame is forwarded to. On success
     * the driver holds the surface but is not yet capturing; {@link #start(VideoCaptureCapability)}
     * begins capture.
     *
     * @param surfaceId the implementation-defined identifier of the screen surface to bind; never
     *                  {@code null}
     * @param sink      the engine sink captured frames are forwarded to; never {@code null}
     * @throws NullPointerException  if {@code surfaceId} or {@code sink} is {@code null}
     * @throws IllegalStateException if the driver is not {@link State#VOID}, or the surface cannot be
     *                               acquired
     */
    void initDriver(String surfaceId, VideoSink sink);

    /**
     * Begins capturing the surface at the given geometry and moves the driver to {@link State#RUNNING}.
     *
     * <p>Accepted only while the driver is {@link State#INITIALIZED}. The {@code capability} sizes the
     * capture and bounds its frame rate. After this returns, the driver forwards captured frames to the
     * latched sink through {@link #sendVideoToSink(VideoFrame)} until {@link #stop()} or a host-loss
     * transition to {@link State#INTERRUPTED}.
     *
     * @param capability the capture geometry and frame-rate ceiling; never {@code null}
     * @throws NullPointerException  if {@code capability} is {@code null}
     * @throws IllegalStateException if the driver is not {@link State#INITIALIZED}, or capture cannot be
     *                               started
     */
    void start(VideoCaptureCapability capability);

    /**
     * Updates the capture geometry and frame-rate ceiling of an initialized driver in place.
     *
     * <p>Accepted while the driver is {@link State#INITIALIZED} or {@link State#RUNNING}; it is rejected
     * while {@link State#VOID} because no surface is held. A screen share calls this when the shared
     * surface resizes or the bandwidth estimator changes the rate ceiling, so capture continues on the
     * same surface at the new geometry without the {@link #stop()} then
     * {@link #start(VideoCaptureCapability)} a camera would require. When the driver is
     * {@link State#RUNNING}, the new geometry takes effect on the next captured frame.
     *
     * @param capability the new capture geometry and frame-rate ceiling; never {@code null}
     * @throws NullPointerException  if {@code capability} is {@code null}
     * @throws IllegalStateException if the driver is {@link State#VOID}
     */
    void setConfig(VideoCaptureCapability capability);

    /**
     * Forwards one captured frame to the latched engine sink while the driver is running.
     *
     * <p>Forwards {@code frame} to the sink only while the driver is {@link State#RUNNING} and a sink is
     * latched. A frame whose {@link VideoFrame#width()} by {@link VideoFrame#height()} differs from the
     * dimensions of the frame most recently forwarded is dropped; the engine drives a deliberate
     * resolution change through {@link #setConfig(VideoCaptureCapability)}, after which the first frame
     * at the new size re-seeds the size baseline. A frame offered while the driver is not
     * {@link State#RUNNING}, or while no sink is latched, is dropped. The returned value is the latched
     * sink's result code for a forwarded frame ({@code 0} on success), or a non-zero sentinel when the
     * frame was dropped without reaching the sink.
     *
     * @param frame the captured frame to forward; never {@code null}
     * @return the sink's result code if the frame was forwarded, or a non-zero sentinel if it was
     * dropped
     * @throws NullPointerException if {@code frame} is {@code null}
     */
    int sendVideoToSink(VideoFrame frame);

    /**
     * Stops capturing and releases the driver back to {@link State#INITIALIZED}.
     *
     * <p>Accepted while the driver is {@link State#RUNNING}, {@link State#INTERRUPTED}, or
     * {@link State#INITIALIZED}; in the last case it is a no-op. After this returns the driver still
     * holds its surface and sink and may be {@link #start(VideoCaptureCapability) started} again. This
     * does not release the surface; only discarding the driver after a {@link #stop()} relinquishes it.
     *
     * @throws IllegalStateException if the driver is {@link State#VOID}
     */
    void stop();
}
