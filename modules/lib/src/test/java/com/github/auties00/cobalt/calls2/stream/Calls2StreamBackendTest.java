package com.github.auties00.cobalt.calls2.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adversarial P10 verification of the ported {@code calls2.stream} backends: the synthetic capture sources
 * (tone, silence) and the manual buffered source/sink, plus the WAV playback sink. These are the concrete
 * implementations migrated from the legacy {@code call.stream} package, and this suite is the verifier's
 * independent proof that the port preserved the stream semantics (capture source feeds the call, playback
 * sink drains the call) and converted the per-frame clock to {@link AudioFrame#ptsMicros()} microseconds.
 *
 * <p>Only the device-free backends are exercised here. The microphone, speaker, camera, screen, and
 * media-file backends bind a real OS device or the bundled FFmpeg natives, so they are deliberately skipped:
 * the harness needs no hardware and never touches FFM. The round trips assert the call media format (16 kHz
 * mono signed 16-bit PCM), the monotonic microsecond presentation clock, and the two distinct backpressure
 * policies the bounded buffers implement (a capture source blocks the producer; a playback sink drops the
 * oldest frame to bound latency).
 */
@DisplayName("calls2.stream ported backends (synthetic capture, buffered, WAV sink)")
class Calls2StreamBackendTest {
    private static final int CALL_SAMPLE_RATE = 16_000;
    private static final int FRAME_SAMPLES = 160; // 10 ms at 16 kHz, the default synthetic frame
    private static final long FRAME_MICROS = 10_000; // 10 ms in the ptsMicros clock

    @Nested
    @DisplayName("ToneAudioOutput (synthetic tone capture source)")
    class Tone {
        @Test
        @DisplayName("emits 16 kHz 160-sample frames whose ptsMicros advances by 10 ms per frame")
        void toneGeometryAndClock() throws InterruptedException {
            var source = AudioOutput.tone(440.0);
            try {
                var first = source.take();
                var second = source.take();
                var third = source.take();
                assertNotNull(first, "a tone source never ends on its own");
                assertEquals(FRAME_SAMPLES, first.pcm().length, "default tone frame is 10 ms at 16 kHz");
                // The presentation clock is microseconds (calls2), not the legacy milliseconds: three
                // consecutive frames step by exactly 10000 us each, starting at zero.
                assertEquals(0L, first.ptsMicros());
                assertEquals(FRAME_MICROS, second.ptsMicros());
                assertEquals(2 * FRAME_MICROS, third.ptsMicros());
            } finally {
                source.shutdown();
            }
        }

        @Test
        @DisplayName("renders a real waveform with signal energy, not silence")
        void toneCarriesEnergy() throws InterruptedException {
            var source = AudioOutput.tone(440.0);
            try {
                var frame = source.take();
                assertTrue(energy(frame.pcm()) > 0, "a 440 Hz tone frame must carry signal energy");
            } finally {
                source.shutdown();
            }
        }

        @Test
        @DisplayName("is not live capture, so the engine applies no acoustic conditioning")
        void toneIsNotLiveCapture() {
            var source = AudioOutput.tone(440.0);
            try {
                assertFalse(source.isLiveCapture(),
                        "a synthetic tone is clean line-level audio; only a real microphone is live capture");
            } finally {
                source.shutdown();
            }
        }

        @Test
        @DisplayName("take() returns null once shut down")
        void toneEndsOnShutdown() throws InterruptedException {
            var source = AudioOutput.tone(440.0);
            source.shutdown();
            assertNull(source.take(), "a shut-down source signals end-of-stream with null");
        }

        @Test
        @DisplayName("rejects a non-positive frequency")
        void toneRejectsBadFrequency() {
            assertThrows(IllegalArgumentException.class, () -> AudioOutput.tone(0.0));
            assertThrows(IllegalArgumentException.class, () -> AudioOutput.tone(-440.0));
        }

        @Test
        @DisplayName("DTMF sums two tones and rejects a non-DTMF digit")
        void dtmfDualTone() throws InterruptedException {
            var dtmf = ToneAudioOutput.dtmf('5');
            try {
                assertTrue(energy(dtmf.take().pcm()) > 0, "a DTMF digit is an audible dual tone");
            } finally {
                dtmf.shutdown();
            }
            assertThrows(IllegalArgumentException.class, () -> ToneAudioOutput.dtmf('Z'));
        }

        @Test
        @DisplayName("ringback alternates a tone-on phase and a silence-off phase")
        void ringbackCadence() throws InterruptedException {
            var ringback = ToneAudioOutput.ringback();
            try {
                // The first 100 frames (one second of 10 ms frames) are the 425 Hz tone-on phase; a frame
                // well into the four-second off phase is digital silence. This proves the cadence machine
                // ported faithfully.
                var onFrame = ringback.take();
                assertTrue(energy(onFrame.pcm()) > 0, "the ringback on-phase carries the tone");
                short[] offFrame = null;
                for (var n = 0; n < 250; n++) {
                    offFrame = ringback.take().pcm();
                }
                assertEquals(0, energy(offFrame), "a frame in the ringback off-phase is digital silence");
            } finally {
                ringback.shutdown();
            }
        }
    }

    @Nested
    @DisplayName("SilenceAudioOutput (synthetic silence capture source)")
    class Silence {
        @Test
        @DisplayName("emits all-zero 16 kHz frames with a monotonic microsecond clock")
        void silenceFramesAreZeroAndClocked() throws InterruptedException {
            var source = AudioOutput.silence();
            try {
                var first = source.take();
                var second = source.take();
                assertEquals(FRAME_SAMPLES, first.pcm().length);
                assertEquals(0, energy(first.pcm()), "silence is true digital zero, not comfort noise");
                assertEquals(0L, first.ptsMicros());
                assertEquals(FRAME_MICROS, second.ptsMicros(),
                        "the silence clock advances 10 ms per frame in microseconds");
            } finally {
                source.shutdown();
            }
        }

        @Test
        @DisplayName("a custom geometry honours the requested frame size and duration")
        void silenceCustomGeometry() throws InterruptedException {
            var source = new SilenceAudioOutput(320, 20_000);
            try {
                var first = source.take();
                var second = source.take();
                assertEquals(320, first.pcm().length);
                assertEquals(20_000L, second.ptsMicros(), "a 20 ms frame steps the clock by 20000 us");
            } finally {
                source.shutdown();
            }
        }

        @Test
        @DisplayName("rejects a non-positive frame geometry")
        void silenceRejectsBadGeometry() {
            assertThrows(IllegalArgumentException.class, () -> new SilenceAudioOutput(0, 10_000));
            assertThrows(IllegalArgumentException.class, () -> new SilenceAudioOutput(160, 0));
        }
    }

    @Nested
    @DisplayName("BufferedAudioOutput (manual capture source)")
    class BufferedSource {
        @Test
        @DisplayName("a written frame round-trips out of take() unchanged")
        void writeThenTakeRoundTrip() throws InterruptedException {
            var source = AudioOutput.buffered();
            var pcm = ramp(FRAME_SAMPLES);
            source.write(new AudioFrame(pcm, 1_234L));
            var taken = source.take();
            assertNotNull(taken);
            assertEquals(1_234L, taken.ptsMicros(), "the microsecond timestamp survives the round trip");
            assertEquals(pcm.length, taken.pcm().length);
            assertEquals(pcm[42], taken.pcm()[42], "the samples survive the round trip");
            source.shutdown();
        }

        @Test
        @DisplayName("a manual buffered source is not live capture")
        void bufferedIsNotLiveCapture() {
            assertFalse(AudioOutput.buffered().isLiveCapture());
        }

        @Test
        @DisplayName("take() returns null after shutdown drains the buffer")
        void bufferedEndsOnShutdown() throws InterruptedException {
            var source = AudioOutput.buffered();
            source.shutdown();
            assertNull(source.take());
        }

        @Test
        @DisplayName("a write after shutdown is dropped rather than throwing")
        void writeAfterShutdownDropped() throws InterruptedException {
            var source = AudioOutput.buffered();
            source.shutdown();
            source.write(new AudioFrame(new short[FRAME_SAMPLES], 0L));
            assertNull(source.take(), "a frame written after end-of-stream is discarded");
        }

        @Test
        @Timeout(5)
        @DisplayName("write blocks once the bounded buffer is full and unblocks when the engine drains it")
        void backpressureBlocksProducer() throws InterruptedException {
            // CAPACITY is 10: the eleventh write must block until take() frees a slot. This is the capture
            // source's defining backpressure policy (it must NOT drop, unlike a playback sink).
            var source = AudioOutput.buffered();
            for (var n = 0; n < 10; n++) {
                source.write(new AudioFrame(new short[FRAME_SAMPLES], n));
            }
            var blocked = new AtomicBoolean(true);
            var entered = new CountDownLatch(1);
            var writer = Thread.ofVirtual().start(() -> {
                try {
                    entered.countDown();
                    source.write(new AudioFrame(new short[FRAME_SAMPLES], 99));
                    blocked.set(false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            // Give the eleventh write a moment to park; it must still be blocked because the buffer is full.
            Thread.sleep(150);
            assertTrue(blocked.get(), "the eleventh write must block while the bounded buffer is full");
            // Draining one frame frees a slot and the parked write completes.
            assertNotNull(source.take());
            writer.join(2_000);
            assertFalse(blocked.get(), "draining a frame must release the parked producer");
            source.shutdown();
        }
    }

    @Nested
    @DisplayName("BufferedAudioInput (manual playback sink)")
    class BufferedSink {
        @Test
        @DisplayName("an offered frame round-trips out of read() unchanged")
        void offerThenReadRoundTrip() throws InterruptedException {
            var sink = AudioInput.buffered();
            var pcm = ramp(FRAME_SAMPLES);
            sink.offer(new AudioFrame(pcm, 7_000L));
            var read = sink.read();
            assertNotNull(read);
            assertEquals(7_000L, read.ptsMicros());
            assertEquals(pcm[100], read.pcm()[100]);
            sink.shutdown();
        }

        @Test
        @DisplayName("read() returns null after shutdown drains the buffer")
        void sinkEndsOnShutdown() throws InterruptedException {
            var sink = AudioInput.buffered();
            sink.offer(new AudioFrame(new short[FRAME_SAMPLES], 0L));
            sink.shutdown();
            assertNotNull(sink.read(), "buffered frames drain before end-of-stream");
            assertNull(sink.read(), "then read signals end-of-stream with null");
        }

        @Test
        @Timeout(5)
        @DisplayName("offer drops the oldest frame when the consumer falls behind, keeping latency bounded")
        void freshnessDropsOldestFrame() throws InterruptedException {
            // The playback sink's defining policy is the opposite of the capture source: it must NOT block
            // the decoder; instead it drops the oldest buffered frame so playback latency stays bounded.
            // CAPACITY is 10; offering 13 frames with no reader keeps only the freshest 10 (timestamps 3..12).
            // Drain exactly those 10 (reading by count, not by waiting on a null terminator, since a sink
            // whose buffer is full at shutdown cannot also hold the end-of-stream sentinel).
            var sink = AudioInput.buffered();
            for (var n = 0; n < 13; n++) {
                sink.offer(new AudioFrame(new short[FRAME_SAMPLES], n));
            }
            var first = sink.read();
            assertNotNull(first);
            assertEquals(3L, first.ptsMicros(),
                    "the three oldest frames must have been dropped to bound latency");
            var last = first;
            for (var n = 0; n < 9; n++) {
                last = sink.read();
                assertNotNull(last, "the freshest ten frames must all be retained");
            }
            assertEquals(12L, last.ptsMicros(), "the newest offered frame must be retained");
            sink.shutdown();
        }

        @Test
        @Timeout(5)
        @DisplayName("read blocks until a frame is offered")
        void readBlocksUntilOffered() throws InterruptedException {
            var sink = AudioInput.buffered();
            var got = new AtomicReference<AudioFrame>();
            var reader = Thread.ofVirtual().start(() -> {
                try {
                    got.set(sink.read());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Thread.sleep(150);
            assertNull(got.get(), "read must park while the sink is empty");
            sink.offer(new AudioFrame(ramp(FRAME_SAMPLES), 555L));
            reader.join(2_000);
            assertNotNull(got.get(), "offering a frame must wake the parked reader");
            assertEquals(555L, got.get().ptsMicros());
            sink.shutdown();
        }
    }

    @Nested
    @DisplayName("WavFileAudioInput (WAV-file playback sink)")
    class WavSink {
        @Test
        @DisplayName("records offered frames as a canonical 16 kHz mono 16-bit PCM WAV the samples survive")
        void wavRoundTripSamples() throws IOException {
            var path = Files.createTempFile("calls2-stream-verify", ".wav");
            try {
                var pcm = ramp(FRAME_SAMPLES);
                var sink = AudioInput.wav(path);
                sink.offer(new AudioFrame(pcm, 0L));
                sink.offer(new AudioFrame(pcm, FRAME_MICROS));
                sink.shutdown();

                var bytes = Files.readAllBytes(path);
                // 44-byte canonical header + two frames of 160 little-endian 16-bit samples.
                var expectedSampleBytes = 2 * FRAME_SAMPLES * Short.BYTES;
                assertEquals(44 + expectedSampleBytes, bytes.length, "header plus the two frames' samples");

                var header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                assertEquals(0x46464952, header.getInt(0), "RIFF magic"); // 'RIFF' little-endian
                assertEquals(0x45564157, header.getInt(8), "WAVE form"); // 'WAVE' little-endian
                assertEquals(1, header.getShort(20), "PCM format code");
                assertEquals(1, header.getShort(22), "mono, the call channel layout");
                assertEquals(CALL_SAMPLE_RATE, header.getInt(24), "16 kHz, the call sample rate");
                assertEquals(16, header.getShort(34), "signed 16-bit samples");
                // The two patched size fields reflect the body length finalised on shutdown.
                assertEquals(36 + expectedSampleBytes, header.getInt(4), "RIFF chunk size");
                assertEquals(expectedSampleBytes, header.getInt(40), "data chunk size");

                // The first body sample is the first PCM sample, little-endian, proving the samples survive.
                var firstSample = (short) ((bytes[44] & 0xFF) | (bytes[45] << 8));
                assertEquals(pcm[0], firstSample, "the recorded samples must equal the offered samples");
            } finally {
                Files.deleteIfExists(path);
            }
        }

        @Test
        @DisplayName("shutdown is idempotent and a frame offered after it is dropped")
        void wavShutdownIdempotent() throws IOException {
            var path = Files.createTempFile("calls2-stream-verify-idem", ".wav");
            try {
                var sink = AudioInput.wav(path);
                sink.offer(new AudioFrame(ramp(FRAME_SAMPLES), 0L));
                sink.shutdown();
                var sizeAfterFirst = Files.size(path);
                sink.offer(new AudioFrame(ramp(FRAME_SAMPLES), FRAME_MICROS));
                sink.shutdown(); // second shutdown must not throw
                assertEquals(sizeAfterFirst, Files.size(path),
                        "a frame offered after shutdown must not be appended");
            } finally {
                Files.deleteIfExists(path);
            }
        }

        @Test
        @DisplayName("a null path is rejected by the factory")
        void wavRejectsNullPath() {
            assertThrows(NullPointerException.class, () -> AudioInput.wav(null));
        }
    }

    @Nested
    @DisplayName("AudioFrame value semantics")
    class Frame {
        @Test
        @DisplayName("rejects a null sample buffer and accepts an empty one")
        void frameValidation() {
            assertThrows(NullPointerException.class, () -> new AudioFrame(null, 0L));
            var empty = new AudioFrame(new short[0], 0L);
            assertEquals(0, empty.pcm().length, "an empty buffer is a legal zero-length frame");
        }

        @Test
        @DisplayName("references the supplied buffer without copying")
        void frameSharesBuffer() {
            var pcm = ramp(8);
            var frame = new AudioFrame(pcm, 0L);
            assertSame(pcm, frame.pcm(), "the frame shares the supplied array; ownership is transferred");
        }
    }

    private static long energy(short[] pcm) {
        long sum = 0;
        for (var s : pcm) {
            sum += (long) s * s;
        }
        return sum;
    }

    private static short[] ramp(int n) {
        var pcm = new short[n];
        for (var i = 0; i < n; i++) {
            pcm[i] = (short) (i * 37 - 1000);
        }
        return pcm;
    }
}
