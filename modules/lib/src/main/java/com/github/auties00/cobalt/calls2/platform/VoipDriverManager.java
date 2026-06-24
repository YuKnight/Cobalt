package com.github.auties00.cobalt.calls2.platform;

import com.github.auties00.cobalt.calls2.platform.AudioCaptureDriver.CapturedAudioSink;
import com.github.auties00.cobalt.calls2.platform.AudioPlaybackDriver.RenderedAudioSource;
import com.github.auties00.cobalt.calls2.platform.LiveDesktopCaptureDriver.ScreenSourceFactory;
import com.github.auties00.cobalt.calls2.platform.LiveVideoCaptureDriver.CameraSourceFactory;
import com.github.auties00.cobalt.calls2.platform.VideoCaptureDriver.State;
import com.github.auties00.cobalt.calls2.platform.VideoCaptureDriver.VideoCaptureCapability;
import com.github.auties00.cobalt.calls2.platform.VideoCaptureDriver.VideoSink;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Owns and routes the audio, video, and desktop capture drivers of the call engine on the JVM host.
 *
 * <p>This is the JVM analogue of the engine's {@code WasmVoipAVDriverManager}: the single registry that
 * holds every capture and playback driver, routes captured audio to the right driver by its source
 * kind, serves rendered-audio playback pulls, and hands out the camera and screen-share capture
 * drivers. {@link #initialize()} is the idempotent bring-up that registers the driver factories once; a
 * second call is a logged no-op, exactly as the engine manager logs "Already initialized". The manager
 * mediates the engine-host seam so the engine layer never touches a device or a driver state machine
 * directly: it installs its audio sink and source on the manager, asks the manager to start a capture,
 * and routes media through it.
 *
 * <p>The audio plane is two capture drivers plus one playback driver, matching the engine manager's
 * layout. {@link AudioDeviceType#MICROPHONE} captured chunks flow through the microphone capture driver
 * and {@link AudioDeviceType#SYSTEM_AUDIO} chunks through the system-audio capture driver, the two that
 * the engine routes by {@code sourceKind != 1} versus {@code == 1}; rendered playback is pulled through
 * the single playback driver. The engine installs one {@link CapturedAudioSink} that receives both
 * capture streams tagged by {@link AudioDeviceType}, and one {@link RenderedAudioSource} the playback
 * driver pulls from. The video plane is a camera {@link VideoCaptureDriver} and a screen-share
 * {@link DesktopCaptureDriver}, each built lazily from the source factory supplied at construction and
 * driven through start/stop, with the desktop driver reconfigurable in place.
 *
 * @apiNote This is an internal engine-host collaborator, not a public surface; embedders never call it.
 * The call engine constructs one manager per client, calls {@link #initialize()} during bring-up,
 * installs its audio sink and source, and routes capture media through it for the lifetime of the
 * client.
 * @implNote This implementation reproduces the engine manager from
 * xplat/wa-voip/platforms/wasm/drivers/WasmVoipAVDriverManager.cpp. The idempotent
 * {@link #initialize()} mirrors fn686, which registers the audio-capture, audio-playback, and
 * video-capture driver factories and wires the global driver-manager singleton on the first call and
 * logs "Already initialized" on a repeat; the native singleton's first byte is the initialized flag
 * every bridge entry point checks, modelled here by {@link #initialized}. The two capture drivers
 * mirror the native split of a microphone capture driver at manager offset {@code 0x4} and a
 * system-audio capture driver at manager offset {@code 0xc} that {@code processAudioCaptureData}
 * (fn11975) routes between by {@code sourceKind}; the playback pull mirrors {@code requestAudioData}
 * (fn11978). The pjlib reader/writer threads are replaced by the drivers' own virtual-thread pumps per
 * the Cobalt threading model.
 */
public final class VoipDriverManager {
    /**
     * Logs initialization and driver-lifecycle transitions, mirroring the engine manager's structured
     * logging.
     */
    private static final System.Logger LOGGER = System.getLogger(VoipDriverManager.class.getName());

    /**
     * Captures the microphone, the routing target for any source kind other than {@code 1}.
     *
     * <p>Corresponds to the engine manager's capture driver at offset {@code 0x4}.
     */
    private final AudioCaptureDriver microphoneCaptureDriver;

    /**
     * Captures the system-audio loopback, the routing target for source kind {@code 1}.
     *
     * <p>Corresponds to the engine manager's capture driver at offset {@code 0xc}.
     */
    private final AudioCaptureDriver systemAudioCaptureDriver;

    /**
     * Renders mixed remote audio to the playback device.
     *
     * <p>Pulled by the engine through {@link #requestAudioDataSource(RenderedAudioSource)} per
     * {@code requestAudioData}.
     */
    private final AudioPlaybackDriver playbackDriver;

    /**
     * Builds a camera {@link VideoCaptureDriver} when video capture is first started.
     */
    private final CameraSourceFactory cameraSourceFactory;

    /**
     * Builds a screen-share {@link DesktopCaptureDriver} when desktop capture is first started.
     */
    private final ScreenSourceFactory screenSourceFactory;

    /**
     * Guards {@link #initialized} and the lazily built {@link #videoDriver}/{@link #desktopDriver} so
     * concurrent engine calls observe a consistent registry.
     */
    private final ReentrantLock lock;

    /**
     * Tracks whether {@link #initialize()} has registered the driver factories, making the bring-up
     * idempotent.
     */
    private boolean initialized;

    /**
     * Holds the camera capture driver, built lazily on the first
     * {@link #startVideoCapture(String, VideoSink, VideoCaptureCapability)}, or {@code null} until then.
     */
    private LiveVideoCaptureDriver videoDriver;

    /**
     * Holds the screen-share capture driver, built lazily on the first
     * {@link #startDesktopCapture(String, VideoSink, VideoCaptureCapability)}, or {@code null} until
     * then.
     */
    private LiveDesktopCaptureDriver desktopDriver;

    /**
     * Creates a driver manager over the supplied audio drivers and video source factories.
     *
     * <p>The two capture drivers and the playback driver are injected so the manager owns and routes them
     * without constructing a particular backend; the video and desktop drivers are built lazily from the
     * source factories on first capture. The manager starts uninitialized; {@link #initialize()}
     * registers the drivers before any capture is started.
     *
     * @param microphoneCaptureDriver  the driver capturing the microphone; never {@code null}
     * @param systemAudioCaptureDriver the driver capturing system audio; never {@code null}
     * @param playbackDriver           the driver rendering remote audio; never {@code null}
     * @param cameraSourceFactory      the factory the camera driver opens its device through; never
     *                                 {@code null}
     * @param screenSourceFactory      the factory the desktop driver opens its surface through; never
     *                                 {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public VoipDriverManager(AudioCaptureDriver microphoneCaptureDriver,
                             AudioCaptureDriver systemAudioCaptureDriver,
                             AudioPlaybackDriver playbackDriver,
                             CameraSourceFactory cameraSourceFactory,
                             ScreenSourceFactory screenSourceFactory) {
        this.microphoneCaptureDriver = Objects.requireNonNull(microphoneCaptureDriver,
                "microphoneCaptureDriver cannot be null");
        this.systemAudioCaptureDriver = Objects.requireNonNull(systemAudioCaptureDriver,
                "systemAudioCaptureDriver cannot be null");
        this.playbackDriver = Objects.requireNonNull(playbackDriver, "playbackDriver cannot be null");
        this.cameraSourceFactory = Objects.requireNonNull(cameraSourceFactory,
                "cameraSourceFactory cannot be null");
        this.screenSourceFactory = Objects.requireNonNull(screenSourceFactory,
                "screenSourceFactory cannot be null");
        this.lock = new ReentrantLock();
    }

    /**
     * Registers the capture and playback drivers, idempotently bringing the manager up.
     *
     * <p>The first call marks the manager initialized so the engine may start capture and playback. A
     * subsequent call is a logged no-op, so the engine may call it defensively on every bring-up path
     * without double-registering.
     */
    public void initialize() {
        lock.lock();
        try {
            if (initialized) {
                LOGGER.log(System.Logger.Level.DEBUG, "VoipDriverManager already initialized");
                return;
            }
            this.initialized = true;
            LOGGER.log(System.Logger.Level.DEBUG, "VoipDriverManager initialized");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reports whether {@link #initialize()} has run.
     *
     * <p>Mirrors the engine manager's initialized flag every bridge entry point checks before routing a
     * call. Returns {@code false} on a freshly constructed manager and {@code true} after the first
     * {@link #initialize()}.
     *
     * @return {@code true} if the manager has been initialized, otherwise {@code false}
     */
    public boolean isInitialized() {
        lock.lock();
        try {
            return initialized;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the audio capture driver for the given device kind.
     *
     * <p>Resolves {@link AudioDeviceType#SYSTEM_AUDIO} to the system-audio capture driver and every other
     * kind to the microphone capture driver, the same routing the engine applies by {@code sourceKind}.
     *
     * @param deviceType the kind of capture endpoint; never {@code null}
     * @return the capture driver for that kind; never {@code null}
     * @throws NullPointerException if {@code deviceType} is {@code null}
     */
    public AudioCaptureDriver captureDriver(AudioDeviceType deviceType) {
        Objects.requireNonNull(deviceType, "deviceType cannot be null");
        return deviceType == AudioDeviceType.SYSTEM_AUDIO
                ? systemAudioCaptureDriver
                : microphoneCaptureDriver;
    }

    /**
     * Installs the engine sink that receives captured audio from both capture drivers.
     *
     * <p>Routes the sink to the microphone and system-audio capture drivers so every captured chunk
     * reaches the engine tagged with its {@link AudioDeviceType}, the host-to-engine handoff the engine
     * manager performs in {@code processAudioCaptureData}.
     *
     * @param sink the engine sink for captured audio; never {@code null}
     * @throws NullPointerException if {@code sink} is {@code null}
     */
    public void onCapturedAudio(CapturedAudioSink sink) {
        Objects.requireNonNull(sink, "sink cannot be null");
        microphoneCaptureDriver.onCapturedAudio(sink);
        systemAudioCaptureDriver.onCapturedAudio(sink);
    }

    /**
     * Installs the engine source the playback driver pulls rendered audio from.
     *
     * <p>Routes the source to the playback driver, which pulls it whenever the playback device needs more
     * samples, the engine-to-host handoff the engine manager performs in {@code requestAudioData}.
     *
     * @param source the engine source of rendered audio; never {@code null}
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public void requestAudioDataSource(RenderedAudioSource source) {
        Objects.requireNonNull(source, "source cannot be null");
        playbackDriver.onRequestAudio(source);
    }

    /**
     * Starts capturing the named audio endpoint at the given format.
     *
     * <p>Selects the capture driver for {@code deviceType}, {@linkplain AudioCaptureDriver#init binds and
     * formats} the device, and {@linkplain AudioCaptureDriver#start starts} the capture pump. Requires
     * the manager to be {@link #initialize() initialized}.
     *
     * @param deviceType      the kind of endpoint to capture from; never {@code null}
     * @param deviceId        the implementation-defined device identifier, or {@code null} for the
     *                        platform default
     * @param sampleRate      the capture sample rate in hertz
     * @param framesPerBuffer the device buffer size in samples
     * @param channelCount    the channel count
     * @throws NullPointerException  if {@code deviceType} is {@code null}
     * @throws IllegalStateException if the manager is not initialized, or the driver rejects the
     *                               transition
     */
    public void startAudioCapture(AudioDeviceType deviceType, String deviceId, int sampleRate,
                                  int framesPerBuffer, int channelCount) {
        Objects.requireNonNull(deviceType, "deviceType cannot be null");
        lock.lock();
        try {
            requireInitialized();
        } finally {
            lock.unlock();
        }
        var driver = captureDriver(deviceType);
        driver.init(deviceId, sampleRate, framesPerBuffer, channelCount, deviceType);
        driver.start();
    }

    /**
     * Stops capturing the named audio endpoint.
     *
     * <p>Delegates to {@link AudioCaptureDriver#stop()} on the capture driver for {@code deviceType}.
     *
     * @param deviceType the kind of endpoint to stop capturing; never {@code null}
     * @throws NullPointerException if {@code deviceType} is {@code null}
     */
    public void stopAudioCapture(AudioDeviceType deviceType) {
        Objects.requireNonNull(deviceType, "deviceType cannot be null");
        captureDriver(deviceType).stop();
    }

    /**
     * Starts audio playback at the given format.
     *
     * <p>{@linkplain AudioPlaybackDriver#init binds and formats} the playback device and
     * {@linkplain AudioPlaybackDriver#start starts} the playback pump, which then pulls the installed
     * {@link RenderedAudioSource}. Requires the manager to be {@link #initialize() initialized}.
     *
     * @param deviceId        the implementation-defined device identifier, or {@code null} for the
     *                        platform default
     * @param sampleRate      the playback sample rate in hertz
     * @param framesPerBuffer the device buffer size in samples
     * @param channelCount    the channel count
     * @throws IllegalStateException if the manager is not initialized, or the driver rejects the
     *                               transition
     */
    public void startPlayback(String deviceId, int sampleRate, int framesPerBuffer, int channelCount) {
        lock.lock();
        try {
            requireInitialized();
        } finally {
            lock.unlock();
        }
        playbackDriver.init(deviceId, sampleRate, framesPerBuffer, channelCount);
        playbackDriver.start();
    }

    /**
     * Stops audio playback.
     *
     * <p>Delegates to {@link AudioPlaybackDriver#stop()} on the playback driver.
     */
    public void stopPlayback() {
        playbackDriver.stop();
    }

    /**
     * Starts camera capture, building the camera driver on first use and forwarding frames to the sink.
     *
     * <p>Lazily constructs the {@link VideoCaptureDriver} from the camera source factory if it does not
     * yet exist, then drives it through {@link VideoCaptureDriver#initDriver(String, VideoSink)} and
     * {@link VideoCaptureDriver#start(VideoCaptureCapability)} so captured frames flow to {@code sink}.
     * Requires the manager to be {@link #initialize() initialized}.
     *
     * @param deviceId   the implementation-defined camera identifier; never {@code null}
     * @param sink       the engine sink captured frames are forwarded to; never {@code null}
     * @param capability the capture geometry and frame-rate ceiling; never {@code null}
     * @throws NullPointerException  if any argument is {@code null}
     * @throws IllegalStateException if the manager is not initialized, or the driver rejects the
     *                               transition
     */
    public void startVideoCapture(String deviceId, VideoSink sink, VideoCaptureCapability capability) {
        Objects.requireNonNull(deviceId, "deviceId cannot be null");
        Objects.requireNonNull(sink, "sink cannot be null");
        Objects.requireNonNull(capability, "capability cannot be null");
        VideoCaptureDriver driver;
        lock.lock();
        try {
            requireInitialized();
            if (videoDriver == null) {
                this.videoDriver = new LiveVideoCaptureDriver(cameraSourceFactory);
            }
            driver = videoDriver;
        } finally {
            lock.unlock();
        }
        driver.initDriver(deviceId, sink);
        driver.start(capability);
    }

    /**
     * Switches the active camera on the running capture driver.
     *
     * <p>Delegates to {@link VideoCaptureDriver#selectCamera(String)} on the camera driver. Requires
     * video capture to have been started so a driver exists.
     *
     * @param deviceId the implementation-defined camera identifier to switch to; never {@code null}
     * @throws NullPointerException  if {@code deviceId} is {@code null}
     * @throws IllegalStateException if video capture has not been started, or the driver rejects the
     *                               switch
     */
    public void selectCamera(String deviceId) {
        Objects.requireNonNull(deviceId, "deviceId cannot be null");
        VideoCaptureDriver driver;
        lock.lock();
        try {
            if (videoDriver == null) {
                throw new IllegalStateException("video capture not started");
            }
            driver = videoDriver;
        } finally {
            lock.unlock();
        }
        driver.selectCamera(deviceId);
    }

    /**
     * Stops camera capture if it is running.
     *
     * <p>Delegates to {@link VideoCaptureDriver#stop()} when a camera driver exists and is not already
     * stopped; otherwise it is a no-op, so the engine may call it on any teardown path.
     */
    public void stopVideoCapture() {
        VideoCaptureDriver driver;
        lock.lock();
        try {
            driver = videoDriver;
        } finally {
            lock.unlock();
        }
        if (driver != null && driver.state() != State.VOID && driver.state() != State.INITIALIZED) {
            driver.stop();
        }
    }

    /**
     * Starts screen-share capture, building the desktop driver on first use and forwarding frames to the
     * sink.
     *
     * <p>Lazily constructs the {@link DesktopCaptureDriver} from the screen source factory if it does not
     * yet exist, then drives it through {@link DesktopCaptureDriver#initDriver(String, VideoSink)} and
     * {@link DesktopCaptureDriver#start(VideoCaptureCapability)} so captured frames flow to {@code sink}.
     * Requires the manager to be {@link #initialize() initialized}.
     *
     * @param surfaceId  the implementation-defined screen-surface identifier; never {@code null}
     * @param sink       the engine sink captured frames are forwarded to; never {@code null}
     * @param capability the capture geometry and frame-rate ceiling; never {@code null}
     * @throws NullPointerException  if any argument is {@code null}
     * @throws IllegalStateException if the manager is not initialized, or the driver rejects the
     *                               transition
     */
    public void startDesktopCapture(String surfaceId, VideoSink sink, VideoCaptureCapability capability) {
        Objects.requireNonNull(surfaceId, "surfaceId cannot be null");
        Objects.requireNonNull(sink, "sink cannot be null");
        Objects.requireNonNull(capability, "capability cannot be null");
        DesktopCaptureDriver driver;
        lock.lock();
        try {
            requireInitialized();
            if (desktopDriver == null) {
                this.desktopDriver = new LiveDesktopCaptureDriver(screenSourceFactory);
            }
            driver = desktopDriver;
        } finally {
            lock.unlock();
        }
        driver.initDriver(surfaceId, sink);
        driver.start(capability);
    }

    /**
     * Reconfigures the running screen-share capture geometry in place.
     *
     * <p>Delegates to {@link DesktopCaptureDriver#setConfig(VideoCaptureCapability)} on the desktop
     * driver, which a screen share uses when the shared surface resizes or the rate ceiling changes.
     * Requires desktop capture to have been started so a driver exists.
     *
     * @param capability the new capture geometry and frame-rate ceiling; never {@code null}
     * @throws NullPointerException  if {@code capability} is {@code null}
     * @throws IllegalStateException if desktop capture has not been started, or the driver rejects the
     *                               reconfiguration
     */
    public void setDesktopConfig(VideoCaptureCapability capability) {
        Objects.requireNonNull(capability, "capability cannot be null");
        DesktopCaptureDriver driver;
        lock.lock();
        try {
            if (desktopDriver == null) {
                throw new IllegalStateException("desktop capture not started");
            }
            driver = desktopDriver;
        } finally {
            lock.unlock();
        }
        driver.setConfig(capability);
    }

    /**
     * Stops screen-share capture if it is running.
     *
     * <p>Delegates to {@link DesktopCaptureDriver#stop()} when a desktop driver exists and is not already
     * stopped; otherwise it is a no-op, so the engine may call it on any teardown path.
     */
    public void stopDesktopCapture() {
        DesktopCaptureDriver driver;
        lock.lock();
        try {
            driver = desktopDriver;
        } finally {
            lock.unlock();
        }
        if (driver != null && driver.state() != State.VOID && driver.state() != State.INITIALIZED) {
            driver.stop();
        }
    }

    /**
     * Throws unless the manager has been initialized.
     *
     * <p>Guards every capture- and playback-start entry point so a driver is never started before
     * {@link #initialize()} registered the drivers, matching the engine manager's initialized-flag check
     * on its bridge entry points. Must be called while holding {@link #lock}.
     *
     * @throws IllegalStateException if {@link #initialize()} has not run
     */
    private void requireInitialized() {
        if (!initialized) {
            throw new IllegalStateException("VoipDriverManager not initialized");
        }
    }
}
