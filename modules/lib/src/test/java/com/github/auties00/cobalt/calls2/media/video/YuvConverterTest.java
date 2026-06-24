package com.github.auties00.cobalt.calls2.media.video;

import com.github.auties00.cobalt.calls2.stream.VideoFrame;
import com.github.auties00.cobalt.calls2.stream.VideoPixelFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adversarial verification of the libyuv-backed {@link YuvConverter} (SPEC 13 YUV color convert).
 *
 * <p>The two byte-repack methods ({@link YuvConverter#toI420(VideoFrame)} and
 * {@link YuvConverter#toNv12(VideoFrame)}) are loss-free and must round-trip exactly. The two color
 * transforms ({@link YuvConverter#toArgb(VideoFrame)} and
 * {@link YuvConverter#argbToI420(int[], int, int, long)}) are libyuv BT.601 limited-range and must
 * round-trip within a small per-channel tolerance: an ARGB picture taken to I420 and back, sampled at
 * its block top-left so 4:2:0 chroma decimation does not itself introduce error, must reproduce each
 * channel within {@link #MAX_ROUNDTRIP_ERROR}. libyuv's BT.601 limited-range matrix keeps a correct
 * round trip well under that bound; a chroma sign or clamp defect inflates it (a pure primary collapses
 * toward neutral gray), so the bound is the regression guard.
 */
@DisplayName("YuvConverter I420/NV12/ARGB conversion")
class YuvConverterTest {
    /**
     * Maximum allowed absolute per-channel error for an ARGB to I420 to ARGB round trip sampled at the
     * 2x2 block top-left. libyuv's BT.601 limited-range matrix lands a correct round trip in single
     * digits per channel; 16 leaves headroom for the studio-swing luma compression without admitting a
     * chroma collapse.
     */
    private static final int MAX_ROUNDTRIP_ERROR = 16;

    private final YuvConverter converter = new YuvConverter();

    private static int argb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Builds a {@code width x height} ARGB picture whose every 2x2 block is a single flat color, so the
     * converter's nearest-sample (top-left) chroma decimation is lossless with respect to the block and
     * the only error measured on round trip is the color-transform error, not subsampling.
     */
    private static int[] flatBlocks(int width, int height, List<int[]> colors) {
        var pixels = new int[width * height];
        var blocksPerRow = width / 2;
        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                var block = (y / 2) * blocksPerRow + (x / 2);
                var c = colors.get(block % colors.size());
                pixels[y * width + x] = argb(c[0], c[1], c[2]);
            }
        }
        return pixels;
    }

    private static List<int[]> paletteColors() {
        return List.of(
                new int[]{255, 0, 0},      // pure red
                new int[]{0, 255, 0},      // pure green
                new int[]{0, 0, 255},      // pure blue
                new int[]{255, 255, 0},    // yellow
                new int[]{0, 255, 255},    // cyan
                new int[]{255, 0, 255},    // magenta
                new int[]{128, 128, 128},  // mid gray
                new int[]{200, 50, 50},    // desaturated red
                new int[]{64, 128, 200},   // mixed
                new int[]{20, 40, 60},     // dark mixed
                new int[]{255, 255, 255},  // white
                new int[]{0, 0, 0}         // black
        );
    }

    @Nested
    @DisplayName("byte repacks are loss-free")
    class Repacks {
        @Test
        @DisplayName("I420 to NV12 to I420 reproduces the original bytes exactly")
        void i420Nv12RoundTrip() {
            var width = 8;
            var height = 6;
            var pixels = new byte[width * height + 2 * (width / 2) * (height / 2)];
            for (var i = 0; i < pixels.length; i++) {
                pixels[i] = (byte) (i * 13 + 7);
            }
            var i420 = new VideoFrame(pixels, VideoPixelFormat.I420, width, height, 1234L);
            var nv12 = converter.toNv12(i420);
            assertEquals(VideoPixelFormat.NV12, nv12.format());
            var back = converter.toI420(nv12);
            assertEquals(VideoPixelFormat.I420, back.format());
            assertArrayEquals(i420.pixels(), back.pixels());
            assertEquals(1234L, back.ptsMicros());
        }

        @Test
        @DisplayName("toI420 returns an already-I420 frame unchanged and toNv12 an already-NV12 frame unchanged")
        void identityRepacks() {
            var width = 4;
            var height = 4;
            var pixels = new byte[width * height + 2 * (width / 2) * (height / 2)];
            var i420 = new VideoFrame(pixels, VideoPixelFormat.I420, width, height, 0L);
            var nv12 = new VideoFrame(pixels.clone(), VideoPixelFormat.NV12, width, height, 0L);
            assertEquals(i420, converter.toI420(i420));
            assertEquals(nv12, converter.toNv12(nv12));
        }

        @Test
        @DisplayName("NV12 chroma is interleaved U,V and splits back to the separate planes in order")
        void nv12ChromaInterleaveLayout() {
            var width = 4;
            var height = 4;
            var lumaSize = width * height;
            var chromaSize = (width / 2) * (height / 2);
            var pixels = new byte[lumaSize + 2 * chromaSize];
            // Distinct U and V plane bytes so an interleave/deinterleave swap would be detected.
            for (var i = 0; i < chromaSize; i++) {
                pixels[lumaSize + i] = (byte) (0x10 + i);              // U plane
                pixels[lumaSize + chromaSize + i] = (byte) (0xA0 + i); // V plane
            }
            var i420 = new VideoFrame(pixels, VideoPixelFormat.I420, width, height, 0L);
            var nv12 = converter.toNv12(i420);
            // Interleaved chroma must be U0,V0,U1,V1,...
            for (var i = 0; i < chromaSize; i++) {
                assertEquals((byte) (0x10 + i), nv12.pixels()[lumaSize + i * 2], "U at pair " + i);
                assertEquals((byte) (0xA0 + i), nv12.pixels()[lumaSize + i * 2 + 1], "V at pair " + i);
            }
        }
    }

    @Nested
    @DisplayName("color transforms round-trip within tolerance (BT.601 limited range)")
    class ColorRoundTrip {
        @ParameterizedTest(name = "{0}")
        @MethodSource("com.github.auties00.cobalt.calls2.media.video.YuvConverterTest#primaryColors")
        @DisplayName("a flat primary survives ARGB -> I420 -> ARGB within MAX_ROUNDTRIP_ERROR")
        void primaryRoundTrip(String name, int r, int g, int b) {
            var width = 4;
            var height = 4;
            var argb = flatBlocks(width, height, List.of(new int[]{r, g, b}));
            var i420 = converter.argbToI420(argb, width, height, 99L);
            assertEquals(VideoPixelFormat.I420, i420.format());
            var back = converter.toArgb(i420);
            // Sample the block top-left (0,0) where chroma is taken, so only color-transform error shows.
            var px = back[0];
            var rr = (px >> 16) & 0xFF;
            var gg = (px >> 8) & 0xFF;
            var bb = px & 0xFF;
            assertChannelClose(r, rr, "R", name);
            assertChannelClose(g, gg, "G", name);
            assertChannelClose(b, bb, "B", name);
        }

        @Test
        @DisplayName("a full palette survives the round trip with bounded worst-case per-channel error")
        void paletteRoundTrip() {
            var colors = paletteColors();
            var width = colors.size() * 2;
            var height = 2;
            var argb = flatBlocks(width, height, colors);
            var i420 = converter.argbToI420(argb, width, height, 0L);
            var back = converter.toArgb(i420);
            var worst = 0;
            for (var block = 0; block < colors.size(); block++) {
                var c = colors.get(block);
                var px = back[block * 2]; // top-left of each block on row 0
                worst = Math.max(worst, Math.abs(((px >> 16) & 0xFF) - c[0]));
                worst = Math.max(worst, Math.abs(((px >> 8) & 0xFF) - c[1]));
                worst = Math.max(worst, Math.abs((px & 0xFF) - c[2]));
            }
            assertTrue(worst <= MAX_ROUNDTRIP_ERROR,
                    "worst-case per-channel round-trip error " + worst + " exceeds " + MAX_ROUNDTRIP_ERROR
                            + "; a value far above this means the chroma transform is wrong (a flipped"
                            + " coefficient sign or a clamp applied before the +128 bias would collapse the"
                            + " chroma planes toward neutral gray)");
        }

        @Test
        @DisplayName("toArgb always sets the alpha byte to fully opaque")
        void argbOpaque() {
            var width = 4;
            var height = 4;
            var argb = flatBlocks(width, height, List.of(new int[]{30, 200, 120}));
            var i420 = converter.argbToI420(argb, width, height, 0L);
            var back = converter.toArgb(i420);
            for (var px : back) {
                assertEquals(0xFF, (px >>> 24) & 0xFF, "alpha must be 0xFF");
            }
        }

        @Test
        @DisplayName("neutral gray maps to near-neutral chroma (U,V approximately 128)")
        void grayIsNeutral() {
            var width = 2;
            var height = 2;
            var argb = flatBlocks(width, height, List.of(new int[]{128, 128, 128}));
            var i420 = converter.argbToI420(argb, width, height, 0L);
            var lumaSize = width * height;
            var chromaSize = (width / 2) * (height / 2);
            var u = i420.pixels()[lumaSize] & 0xFF;
            var v = i420.pixels()[lumaSize + chromaSize] & 0xFF;
            // For mid gray the chroma residual is zero, so both chroma samples should sit at the neutral
            // 128. libyuv's BT.601 limited-range matrix maps gray(128,128,128) to U=V=128; the 2-LSB band
            // absorbs the matrix rounding without admitting a chroma collapse.
            assertTrue(Math.abs(u - 128) <= 2, "U for gray should be near 128, got " + u);
            assertTrue(Math.abs(v - 128) <= 2, "V for gray should be near 128, got " + v);
        }
    }

    private static void assertChannelClose(int expected, int actual, String channel, String color) {
        var err = Math.abs(expected - actual);
        assertTrue(err <= MAX_ROUNDTRIP_ERROR,
                color + " channel " + channel + " round-trip error " + err + " (expected " + expected
                        + ", got " + actual + ") exceeds " + MAX_ROUNDTRIP_ERROR);
    }

    static Stream<Arguments> primaryColors() {
        return Stream.of(
                Arguments.of("red", 255, 0, 0),
                Arguments.of("green", 0, 255, 0),
                Arguments.of("blue", 0, 0, 255),
                Arguments.of("yellow", 255, 255, 0),
                Arguments.of("cyan", 0, 255, 255),
                Arguments.of("magenta", 255, 0, 255),
                Arguments.of("white", 255, 255, 255),
                Arguments.of("black", 0, 0, 0)
        );
    }
}
