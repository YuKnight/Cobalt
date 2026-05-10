# cobalt-call-toolkit

Convenience built-ins for Cobalt's call layer. Optional companion module
to `cobalt` that ships local-device, file, and recording Sources/Sinks
so applications can compose a working call without hand-rolling
microphone capture, frame conversion, or container muxing.

## Where to start

`com.github.auties00.cobalt.call.toolkit.CallToolkit` is the single
discoverable entry point — static factories cover the common cases with
sensible defaults that already match the call wire profile, so callers
don't have to know about Opus, VP8, sample rates, or I420.

```java
import com.github.auties00.cobalt.call.toolkit.CallToolkit;

var mic     = CallToolkit.microphone();   // 16 kHz mono
var speaker = CallToolkit.speaker();
var camera  = CallToolkit.camera();       // platform default
var screen  = CallToolkit.screen();       // platform default
```

## Package layout

| Package        | What it gives you                                                  |
|----------------|--------------------------------------------------------------------|
| `(toolkit)`    | `CallToolkit` facade — start here                                  |
| `input/`       | `Microphone`, `Camera`, `Screen`, `AudioFile`, `VideoFile`, `Tone`, `Silence` |
| `output/`      | `Speaker`, `WavFile`, `AudioMixer`, `LevelMeter`, `CallRecorder`   |
| `transform/`   | `MuteSwitch`, `AudioPipe`, `ResamplingSource`, `ResamplingSink`, `PixelFormats` |

Pure-Java helpers (everything in `transform/`, plus `Microphone`,
`Speaker`, `AudioMixer`, `LevelMeter`, `WavFile`, `Tone`, `Silence`)
need no natives. FFmpeg-backed helpers (`Camera`, `Screen`, `AudioFile`,
`VideoFile`, `CallRecorder`) load libavformat / libavcodec / libavdevice
/ libavutil / libswscale / libswresample on first use.

## Module coords

```xml
<dependency>
  <groupId>com.github.auties00</groupId>
  <artifactId>cobalt-call-toolkit</artifactId>
  <version>0.1.0</version>
</dependency>
```

Java module name: `com.github.auties00.cobalt.call.toolkit`.

## Native dependency

The toolkit ships its own `META-INF/native-checksums.json` declaring
the FFmpeg shared libraries it needs. The shared `NativeLibLoader` (in
the `cobalt` module) resolves them in order of classpath bundle →
on-disk cache → GitHub download → system fallback.

The default toolkit JAR ships **no binaries** — it relies on the
download path. Apps that want offline / Maven-Central-only deployments
can add the per-classifier natives JAR:

```xml
<dependency>
  <groupId>com.github.auties00</groupId>
  <artifactId>cobalt-call-toolkit</artifactId>
  <version>0.1.0</version>
  <classifier>natives-linux-x86_64</classifier>
</dependency>
```

Supported classifiers: `linux-x86_64`, `linux-aarch64`, `darwin-x86_64`,
`darwin-aarch64`, `windows-x86_64`, `windows-aarch64`.

## Building the natives

To produce the per-classifier FFmpeg drop locally:

```bash
# 1. Check out FFmpeg.
git clone https://git.ffmpeg.org/ffmpeg.git /tmp/ffmpeg && cd /tmp/ffmpeg

# 2. Drive the toolkit's minimal-build recipe.
FFMPEG_SRC=/tmp/ffmpeg modules/call-toolkit/scripts/build-ffmpeg.sh

# 3. Vendor the public headers (if they need refreshing).
FFMPEG_SRC=/tmp/ffmpeg modules/call-toolkit/scripts/vendor-ffmpeg-headers.sh

# 4. Regenerate the jextract bindings.
JEXTRACT_HOME=/path/to/jextract modules/call-toolkit/dependencies/ffmpeg/generate.sh

# 5. Update the manifest with the fresh sha256 + commit-pinned URL.
modules/call-toolkit/scripts/regenerate-natives-manifest.sh

# 6. Package the per-classifier JAR.
mvn -pl modules/call-toolkit -am package -Pnatives -Dnatives.classifier=linux-x86_64 -DskipTests
```

The build recipe enables only what the toolkit actually uses:

- decoders: aac, flac, mp3, pcm_s16le, pcm_s16be, pcm_u8, h264, opus, vorbis
- demuxers / muxers: matroska / mov / wav (mux) plus the corresponding
  demuxers
- input devices: avfoundation (macOS), dshow / gdigrab (Windows),
  v4l2 / xcbgrab / kmsgrab / pipewiregrab (Linux)
- libsws*ale + libswresample for in-flight format / rate conversion

No encoders. The Cobalt call layer already ships libopus, libvpx,
openh264, libspeexdsp via the lib module; re-encoding through
libavcodec would only add weight.

## Use cases

### Speak into a real call from a microphone

```java
var mic = CallToolkit.microphone();
mic.open();
call.start(mic, /* localVideoSource */ null);
```

### Play file audio into a call

```java
try (var file = CallToolkit.playAudio(Path.of("greeting.wav"))) {
    call.start(file, null);
}
```

### Record a call to MKV

```java
try (var recorder = CallToolkit.recordCall(Path.of("call.mkv"), 640, 480)) {
    call.onAudioPacket((payload, ptsMs) ->
            recorder.writeAudioPacket(payload, ptsMs));
    call.onVideoPacket((payload, ptsMs, kf) ->
            recorder.writeVideoPacket(payload, ptsMs, kf));
}
```

### Share the screen

```java
// Auto-detect platform default
var screen = CallToolkit.screen();

// Or pick a specific source
var screen = Screen.wayland("default");      // Linux Wayland
var screen = Screen.windowsWindow("Code");   // specific Windows window

call.upgradeToVideo(screen, /* sink */ remoteRender);
```

### Mute toggle in the UI

```java
var mic   = CallToolkit.microphone();
var muted = CallToolkit.muteSwitch(mic);     // wrap

muteButton.addActionListener(e -> muted.setMuted(!muted.muted()));
call.start(muted, null);
```

### Bridge two calls

Pure-Java helpers let you glue two calls together without any natives:

```java
var bridge = CallToolkit.mixer();
var aSink  = bridge.addPeer(callA);
var bSink  = bridge.addPeer(callB);
callA.start(bridge.mixedOutput(), null);  // A hears the mix of B
callB.start(bridge.mixedOutput(), null);  // B hears the mix of A
callA.onAudioFrame(aSink::write);
callB.onAudioFrame(bSink::write);
```
