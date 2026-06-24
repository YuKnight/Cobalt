package com.github.auties00.cobalt.calls2.platform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adversarial verification of {@link SctpRingBuffer} against SPEC Section 14.7 (SCTP outbound ring).
 *
 * <p>The harness never trusts the class under test to validate itself: it owns an independent
 * spec-built encoder ({@link #encodeFrame}) and reader ({@link SpecReader}) reconstructed solely from
 * the recovered native contract (fn9329 write, fn9325 wrap-aware copy, the {@code (readIdx + ~writeIdx
 * + cap) % cap} free-space formula, and the five-byte little-endian header), then cross-checks raw
 * segment bytes, decoded records, wrap-around splits, full-buffer no-overwrite behaviour, the guard
 * bounds, and a single-producer single-consumer fuzz against that independent oracle.
 */
@DisplayName("SctpRingBuffer (SPEC 14.7 outbound ring)")
class SctpRingBufferTest {
    private static final long WRITE_INDEX_OFFSET = 0L;
    private static final long READ_INDEX_OFFSET = 4L;
    private static final long DATA_OFFSET = 8L;
    private static final int HEADER_SIZE = 5;

    private static int readU32(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_INT, offset);
    }

    private static int dataByte(MemorySegment segment, int capacity, int dataIndex) {
        return segment.get(ValueLayout.JAVA_BYTE, DATA_OFFSET + Math.floorMod(dataIndex, capacity)) & 0xFF;
    }

    /**
     * Encodes a datagram into the exact native frame bytes per SPEC 14.7, independent of the class
     * under test: {@code [addrLen u8][port_lo][port_hi][totalLen_lo][totalLen_hi][addr][payload]},
     * with {@code totalLen} a 16-bit little-endian value equal to {@code payloadLen + addrLen + 5}.
     */
    private static byte[] encodeFrame(byte[] address, int port, byte[] payload) {
        var totalLen = payload.length + address.length + HEADER_SIZE;
        var frame = new byte[totalLen];
        frame[0] = (byte) address.length;
        frame[1] = (byte) (port & 0xFF);
        frame[2] = (byte) ((port >>> 8) & 0xFF);
        frame[3] = (byte) (totalLen & 0xFF);
        frame[4] = (byte) ((totalLen >>> 8) & 0xFF);
        System.arraycopy(address, 0, frame, HEADER_SIZE, address.length);
        System.arraycopy(payload, 0, frame, HEADER_SIZE + address.length, payload.length);
        return frame;
    }

    private static MemorySegment seg(byte[] bytes) {
        return MemorySegment.ofArray(bytes);
    }

    private static byte[] bytes(int... values) {
        var out = new byte[values.length];
        for (var i = 0; i < values.length; i++) {
            out[i] = (byte) values[i];
        }
        return out;
    }

    private static byte[] pattern(int length, int seed) {
        var out = new byte[length];
        for (var i = 0; i < length; i++) {
            out[i] = (byte) (seed + i);
        }
        return out;
    }

    /**
     * One decoded record captured from a {@link SctpRingBuffer.FrameSink}.
     */
    private record Record(byte[] address, int port, byte[] payload) {
    }

    private static SctpRingBuffer.FrameSink collector(List<Record> out) {
        return (address, port, payload) -> out.add(new Record(address, port, payload));
    }

    @Nested
    @DisplayName("backing-segment layout")
    class Layout {
        @Test
        @DisplayName("indices live at 0 and 4, data starts at 8, total size is capacity + 8")
        void offsets() {
            try (var ring = new SctpRingBuffer(64)) {
                var segment = ring.segment();
                assertEquals(DATA_OFFSET + 64, segment.byteSize());
                assertEquals(0, readU32(segment, WRITE_INDEX_OFFSET));
                assertEquals(0, readU32(segment, READ_INDEX_OFFSET));
                assertEquals(64, ring.capacity());
            }
        }

        @Test
        @DisplayName("the backing segment is 4-byte aligned for the native co-producer")
        void alignment() {
            try (var ring = new SctpRingBuffer(64)) {
                assertEquals(0, ring.segment().address() % 4);
            }
        }

        @Test
        @DisplayName("rejects a capacity smaller than two")
        void capacityFloor() {
            assertThrows(IllegalArgumentException.class, () -> new SctpRingBuffer(1));
            assertThrows(IllegalArgumentException.class, () -> new SctpRingBuffer(0));
            assertThrows(IllegalArgumentException.class, () -> new SctpRingBuffer(-3));
        }
    }

    @Nested
    @DisplayName("frame byte layout (raw segment inspection)")
    class FrameLayout {
        @Test
        @DisplayName("write lays the 5-byte header, address, then payload exactly per SPEC 14.7")
        void exactBytes() {
            try (var ring = new SctpRingBuffer(256)) {
                var address = bytes(0x11, 0x22, 0x33);
                var payload = bytes(0xAA, 0xBB, 0xCC, 0xDD);
                var port = 0xBEEF;
                assertTrue(ring.write(seg(address), port, seg(payload)));

                var expected = encodeFrame(address, port, payload);
                var segment = ring.segment();
                // writeIdx advanced by the whole record; readIdx untouched.
                assertEquals(expected.length, readU32(segment, WRITE_INDEX_OFFSET));
                assertEquals(0, readU32(segment, READ_INDEX_OFFSET));
                for (var i = 0; i < expected.length; i++) {
                    assertEquals(expected[i] & 0xFF, dataByte(segment, 256, i),
                            "data byte " + i + " diverges from the spec frame");
                }
            }
        }

        @Test
        @DisplayName("totalLen field equals payloadLen + addrLen + 5 and is little-endian")
        void totalLenField() {
            try (var ring = new SctpRingBuffer(0x4000)) {
                var address = pattern(7, 1);
                var payload = pattern(300, 40); // forces totalLen > 255 to exercise the high byte
                assertTrue(ring.write(seg(address), 0x0102, seg(payload)));

                var segment = ring.segment();
                var totalLen = dataByte(segment, 0x4000, 3) | (dataByte(segment, 0x4000, 4) << 8);
                assertEquals(payload.length + address.length + HEADER_SIZE, totalLen);
                assertEquals(address.length, dataByte(segment, 0x4000, 0));
                assertEquals(0x02, dataByte(segment, 0x4000, 1));
                assertEquals(0x01, dataByte(segment, 0x4000, 2));
            }
        }

        @Test
        @DisplayName("a zero-length address yields a header-only prefix with addrLen 0")
        void emptyAddress() {
            try (var ring = new SctpRingBuffer(64)) {
                var payload = bytes(1, 2, 3);
                assertTrue(ring.write(seg(new byte[0]), 0x0A0B, seg(payload)));
                var segment = ring.segment();
                assertEquals(0, dataByte(segment, 64, 0));
                assertEquals(payload.length + HEADER_SIZE, readU32(segment, WRITE_INDEX_OFFSET));

                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                assertEquals(1, got.size());
                assertEquals(0, got.getFirst().address().length);
                assertEquals(0x0A0B, got.getFirst().port());
                assertArrayEquals(payload, got.getFirst().payload());
            }
        }
    }

    @Nested
    @DisplayName("round-trip through poll")
    class RoundTrip {
        @Test
        @DisplayName("poll returns the address, port, and payload that were written")
        void single() {
            try (var ring = new SctpRingBuffer(128)) {
                var address = bytes(0xDE, 0xAD);
                var payload = bytes(1, 2, 3, 4, 5, 6, 7, 8);
                assertTrue(ring.write(seg(address), 0x1234, seg(payload)));

                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                assertEquals(1, got.size());
                assertArrayEquals(address, got.getFirst().address());
                assertEquals(0x1234, got.getFirst().port());
                assertArrayEquals(payload, got.getFirst().payload());
            }
        }

        @Test
        @DisplayName("poll on an empty ring returns false and delivers nothing")
        void emptyPoll() {
            try (var ring = new SctpRingBuffer(64)) {
                var got = new ArrayList<Record>();
                assertFalse(ring.poll(collector(got)));
                assertTrue(got.isEmpty());
            }
        }

        @Test
        @DisplayName("only the low sixteen bits of the port are recorded")
        void portTruncation() {
            try (var ring = new SctpRingBuffer(64)) {
                assertTrue(ring.write(seg(new byte[0]), 0x7FAB_CDEF, seg(bytes(9))));
                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                assertEquals(0xCDEF, got.getFirst().port());
            }
        }

        @Test
        @DisplayName("records drain first-in-first-out and drain reports the count")
        void fifoDrain() {
            try (var ring = new SctpRingBuffer(512)) {
                for (var i = 0; i < 6; i++) {
                    assertTrue(ring.write(seg(bytes(i)), 0x1000 + i, seg(pattern(3 + i, i * 10))));
                }
                var got = new ArrayList<Record>();
                assertEquals(6, ring.drain(collector(got)));
                assertEquals(6, got.size());
                for (var i = 0; i < 6; i++) {
                    assertArrayEquals(bytes(i), got.get(i).address());
                    assertEquals(0x1000 + i, got.get(i).port());
                    assertArrayEquals(pattern(3 + i, i * 10), got.get(i).payload());
                }
                assertEquals(0, ring.drain(collector(got)));
            }
        }

        @Test
        @DisplayName("poll drains one record at a time, freeing space progressively")
        void partialDrain() {
            try (var ring = new SctpRingBuffer(64)) {
                assertTrue(ring.write(seg(bytes(1)), 1, seg(pattern(10, 1))));
                assertTrue(ring.write(seg(bytes(2)), 2, seg(pattern(10, 50))));

                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                assertEquals(1, got.size());
                assertArrayEquals(pattern(10, 1), got.getFirst().payload());
                // second record still present
                assertTrue(ring.poll(collector(got)));
                assertEquals(2, got.size());
                assertArrayEquals(pattern(10, 50), got.get(1).payload());
                assertFalse(ring.poll(collector(got)));
            }
        }
    }

    @Nested
    @DisplayName("wrap-around at the data-region boundary")
    class WrapAround {
        @Test
        @DisplayName("a record that straddles the end wraps its header, address, and payload")
        void straddle() {
            // Capacity 32: pre-advance both indices to 30 so the next record wraps after 2 bytes.
            try (var ring = new SctpRingBuffer(32)) {
                primeIndices(ring, 30, 30);
                var address = bytes(0xA1, 0xA2);
                var payload = bytes(0xF0, 0xF1, 0xF2, 0xF3, 0xF4);
                assertTrue(ring.write(seg(address), 0x0809, seg(payload)));

                var expected = encodeFrame(address, 0x0809, payload);
                var segment = ring.segment();
                // bytes land starting at data index 30 and wrap to 0.
                for (var i = 0; i < expected.length; i++) {
                    assertEquals(expected[i] & 0xFF, dataByte(segment, 32, 30 + i),
                            "wrapped byte " + i + " diverges");
                }
                var expectedWrite = (30 + expected.length) % 32;
                assertEquals(expectedWrite, readU32(segment, WRITE_INDEX_OFFSET));

                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                assertArrayEquals(address, got.getFirst().address());
                assertArrayEquals(payload, got.getFirst().payload());
                assertEquals(0x0809, got.getFirst().port());
            }
        }

        @Test
        @DisplayName("the header itself splits across the boundary")
        void headerSplit() {
            // Put writeIdx at cap-2 so the 5-byte header straddles: 2 bytes before wrap, 3 after.
            try (var ring = new SctpRingBuffer(64)) {
                primeIndices(ring, 62, 62);
                var address = bytes(0x01);
                var payload = bytes(0x10, 0x20, 0x30);
                assertTrue(ring.write(seg(address), 0x4142, seg(payload)));

                var expected = encodeFrame(address, 0x4142, payload);
                var segment = ring.segment();
                for (var i = 0; i < expected.length; i++) {
                    assertEquals(expected[i] & 0xFF, dataByte(segment, 64, 62 + i));
                }
                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                assertArrayEquals(address, got.getFirst().address());
                assertArrayEquals(payload, got.getFirst().payload());
            }
        }

        @Test
        @DisplayName("writeIdx wraps exactly to zero when a record ends on the boundary")
        void endsExactlyOnBoundary() {
            try (var ring = new SctpRingBuffer(64)) {
                // total record length 12; place it so it ends exactly at index 64 -> wraps to 0.
                primeIndices(ring, 52, 52);
                var payload = pattern(5, 7); // total = 5 + 2 + 5 = 12
                var address = bytes(0xBB, 0xCC);
                assertTrue(ring.write(seg(address), 0x0102, seg(payload)));
                assertEquals(0, readU32(ring.segment(), WRITE_INDEX_OFFSET));

                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                assertEquals(0, readU32(ring.segment(), READ_INDEX_OFFSET));
                assertArrayEquals(payload, got.getFirst().payload());
            }
        }

        /**
         * Advances the ring's persisted write and read indices to {@code value} by reflecting them
         * directly into the backing segment, so a subsequent write begins near the wrap boundary
         * without having to fill and drain the ring first.
         */
        private static void primeIndices(SctpRingBuffer ring, int writeIdx, int readIdx) {
            ring.segment().set(ValueLayout.JAVA_INT, WRITE_INDEX_OFFSET, writeIdx);
            ring.segment().set(ValueLayout.JAVA_INT, READ_INDEX_OFFSET, readIdx);
        }
    }

    @Nested
    @DisplayName("full-buffer behaviour (no overwrite of unread data)")
    class NoOverwrite {
        @Test
        @DisplayName("a write that does not fit returns false and leaves the ring untouched")
        void rejectWhenFull() {
            try (var ring = new SctpRingBuffer(64)) {
                // Fill with equal records until one is rejected.
                var written = new ArrayList<byte[]>();
                var address = bytes(0x01);
                var port = 0x2222;
                while (true) {
                    var payload = pattern(8, written.size() * 7 + 1);
                    if (!ring.write(seg(address), port, seg(payload))) {
                        break;
                    }
                    written.add(payload);
                }
                assertFalse(written.isEmpty());

                // The rejected write must not have advanced writeIdx or corrupted earlier records.
                var got = new ArrayList<Record>();
                assertEquals(written.size(), ring.drain(collector(got)));
                for (var i = 0; i < written.size(); i++) {
                    assertArrayEquals(written.get(i), got.get(i).payload(),
                            "record " + i + " was clobbered by the rejected write");
                    assertArrayEquals(address, got.get(i).address());
                    assertEquals(port, got.get(i).port());
                }
            }
        }

        @Test
        @DisplayName("a write into a full non-power-of-two ring is rejected (no overwrite)")
        void rejectWhenFullNonPow2() {
            // Capacity 1000 (not a power of two): drive both indices to writeIdx=0, readIdx=1, the
            // canonical full state where exactly one byte is reserved so the true free space is 0 and
            // SPEC 14.7 must reject any write. The recovered native free formula
            // free = (readIdx + ~writeIdx + cap) % cap yields 0 here, so no record may be enqueued
            // without clobbering the unread byte at index 0.
            try (var ring = new SctpRingBuffer(1000)) {
                ring.segment().set(ValueLayout.JAVA_INT, WRITE_INDEX_OFFSET, 0);
                ring.segment().set(ValueLayout.JAVA_INT, READ_INDEX_OFFSET, 1);
                assertFalse(ring.write(seg(new byte[0]), 0x1234, seg(bytes(1))),
                        "a full ring (true free 0) must reject the write rather than overwrite "
                                + "unread data; an over-reporting free-space modulo would accept it");
                // writeIdx must not have moved into the unread region.
                assertEquals(0, readU32(ring.segment(), WRITE_INDEX_OFFSET));
            }
        }

        @Test
        @DisplayName("a near-full non-power-of-two ring rejects an oversized write (no overwrite)")
        void rejectOversizedNonPow2() {
            // Capacity 1000, writeIdx=10, readIdx=12: true free = (12 - 10 - 1 + 1000) % 1000 = 1, so
            // only a record that does not exist (totalLen <= 1) could fit; any real record (totalLen
            // >= 6) must be rejected. A free formula that over-reports would corrupt the unread bytes.
            try (var ring = new SctpRingBuffer(1000)) {
                ring.segment().set(ValueLayout.JAVA_INT, WRITE_INDEX_OFFSET, 10);
                ring.segment().set(ValueLayout.JAVA_INT, READ_INDEX_OFFSET, 12);
                assertFalse(ring.write(seg(new byte[0]), 1, seg(bytes(0xAA))),
                        "true free is 1 byte; a 6-byte record must be rejected, not overwrite unread data");
                assertEquals(10, readU32(ring.segment(), WRITE_INDEX_OFFSET));
            }
        }

        @Test
        @DisplayName("a rejected write does not move writeIdx")
        void rejectedWriteKeepsWriteIdx() {
            try (var ring = new SctpRingBuffer(32)) {
                // One record that leaves too little room for a second of the same size.
                var payload = pattern(20, 3); // total = 20 + 1 + 5 = 26, free after = 31 - 26 = 5 < 26
                assertTrue(ring.write(seg(bytes(0x09)), 1, seg(payload)));
                var writeIdxAfterFirst = readU32(ring.segment(), WRITE_INDEX_OFFSET);
                assertFalse(ring.write(seg(bytes(0x09)), 1, seg(payload)));
                assertEquals(writeIdxAfterFirst, readU32(ring.segment(), WRITE_INDEX_OFFSET));
            }
        }

        @Test
        @DisplayName("space frees as records are drained, then accepts new writes")
        void recoverAfterDrain() {
            try (var ring = new SctpRingBuffer(64)) {
                var payload = pattern(40, 1); // total = 46; only one fits at a time
                assertTrue(ring.write(seg(bytes(1)), 1, seg(payload)));
                assertFalse(ring.write(seg(bytes(2)), 2, seg(payload)));

                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                // after draining the first, the second now fits
                assertTrue(ring.write(seg(bytes(2)), 2, seg(payload)));
                assertTrue(ring.poll(collector(got)));
                assertEquals(2, got.size());
                assertEquals(2, got.get(1).port());
            }
        }
    }

    @Nested
    @DisplayName("largest enqueueable record and the free-space formula")
    class FreeSpace {
        @ParameterizedTest(name = "capacity {0}")
        @ValueSource(ints = {16, 64, 256, 1024, 4096})
        @DisplayName("a record of total length capacity - 1 fits on an empty ring (power-of-two)")
        void maxRecordPow2(int capacity) {
            try (var ring = new SctpRingBuffer(capacity)) {
                // total record = cap - 1, so payload = cap - 1 - addrLen - 5, addrLen = 0
                var payload = pattern(capacity - 1 - HEADER_SIZE, 1);
                assertTrue(ring.write(seg(new byte[0]), 1, seg(payload)),
                        "a record of capacity - 1 bytes must fit per the documented contract");
                // one more byte must not fit
                assertFalse(ring.write(seg(new byte[0]), 1, seg(bytes(0xFF))));

                var got = new ArrayList<Record>();
                assertTrue(ring.poll(collector(got)));
                assertArrayEquals(payload, got.getFirst().payload());
            }
        }

        @ParameterizedTest(name = "capacity {0}")
        @ValueSource(ints = {100, 1000, 4095, 5000})
        @DisplayName("a record of total length capacity - 1 fits on an empty ring (non-power-of-two)")
        void maxRecordNonPow2(int capacity) {
            try (var ring = new SctpRingBuffer(capacity)) {
                // The class javadoc states an empty ring reports cap - 1 free and the largest
                // enqueueable record is cap - 1 bytes, independent of whether cap is a power of two.
                var payload = pattern(capacity - 1 - HEADER_SIZE, 1);
                assertTrue(ring.write(seg(new byte[0]), 1, seg(payload)),
                        "SPEC 14.7 free = (readIdx + ~writeIdx + cap) % cap leaves exactly one byte "
                                + "free, so a cap - 1 record must fit even when cap is not a power of two");
            }
        }

        @ParameterizedTest(name = "capacity {0}")
        @ValueSource(ints = {100, 1000})
        @DisplayName("an empty non-power-of-two ring reports cap - 1 free via the largest accepted record")
        void emptyFreeIsCapMinusOneNonPow2(int capacity) {
            // Binary-search the largest payload accepted on an empty ring; per spec it is
            // cap - 1 - HEADER_SIZE. A divergent free-space modulo would accept fewer bytes.
            var largest = largestAcceptedPayloadEmpty(capacity);
            assertEquals(capacity - 1 - HEADER_SIZE, largest,
                    "largest accepted payload on an empty ring should equal cap - 1 - header");
        }

        @ParameterizedTest(name = "capacity {0}")
        @ValueSource(ints = {16, 64, 256})
        @DisplayName("largest accepted payload on an empty ring equals cap - 1 - header (power-of-two)")
        void emptyFreeIsCapMinusOnePow2(int capacity) {
            assertEquals(capacity - 1 - HEADER_SIZE, largestAcceptedPayloadEmpty(capacity));
        }

        @Test
        @DisplayName("the largest accepted record matches the spec free formula at every writeIdx")
        void freeFormulaAcrossWriteIndex() {
            var capacity = 256;
            for (var start = 0; start < capacity; start++) {
                try (var ring = new SctpRingBuffer(capacity)) {
                    ring.segment().set(ValueLayout.JAVA_INT, WRITE_INDEX_OFFSET, start);
                    ring.segment().set(ValueLayout.JAVA_INT, READ_INDEX_OFFSET, start);
                    // Independent spec free space for an empty ring (read == write): cap - 1.
                    var specFree = specFreeSpace(start, start, capacity);
                    assertEquals(capacity - 1, specFree);
                    // The largest record that fits has total length == specFree.
                    var payloadLen = (int) (specFree - HEADER_SIZE);
                    var payload = pattern(payloadLen, start);
                    assertTrue(ring.write(seg(new byte[0]), 1, seg(payload)),
                            "writeIdx=" + start + ": a record of free bytes must fit");
                }
            }
        }

        /**
         * Computes the spec free-space value with the recovered native formula
         * {@code (readIdx + ~writeIdx + cap) % cap} evaluated under 32-bit unsigned wrap, mirroring
         * fn9329; this is the independent oracle the implementation must match.
         */
        private static long specFreeSpace(int writeIdx, int readIdx, int capacity) {
            var w = writeIdx & 0xFFFFFFFFL;
            var r = readIdx & 0xFFFFFFFFL;
            var notWrite = (~w) & 0xFFFFFFFFL;
            var sum = (r + notWrite + (capacity & 0xFFFFFFFFL)) & 0xFFFFFFFFL;
            return Long.remainderUnsigned(sum, capacity & 0xFFFFFFFFL);
        }

        private static int largestAcceptedPayloadEmpty(int capacity) {
            var lo = 1;
            var hi = capacity; // payload cannot exceed capacity
            var best = 0;
            while (lo <= hi) {
                var mid = (lo + hi) >>> 1;
                try (var ring = new SctpRingBuffer(capacity)) {
                    if (ring.write(seg(new byte[0]), 1, seg(new byte[mid]))) {
                        best = mid;
                        lo = mid + 1;
                    } else {
                        hi = mid - 1;
                    }
                }
            }
            return best;
        }
    }

    @Nested
    @DisplayName("write guard bounds (SPEC: 1 <= payloadLen < 0x10000 && totalLen < 0x2001)")
    class Guards {
        @Test
        @DisplayName("an empty payload is rejected")
        void emptyPayload() {
            try (var ring = new SctpRingBuffer(256)) {
                assertFalse(ring.write(seg(bytes(1)), 1, seg(new byte[0])));
                assertEquals(0, readU32(ring.segment(), WRITE_INDEX_OFFSET));
            }
        }

        @Test
        @DisplayName("a total record reaching 0x2001 is rejected even when the ring is huge")
        void totalLenCeiling() {
            try (var ring = new SctpRingBuffer(0x8000)) {
                // totalLen = payload + addr + 5; pick payload so totalLen == 0x2001 (rejected)
                var payloadLen = 0x2001 - HEADER_SIZE; // addrLen 0
                assertFalse(ring.write(seg(new byte[0]), 1, seg(new byte[payloadLen])));
                // one byte fewer (totalLen == 0x2000) is accepted
                assertTrue(ring.write(seg(new byte[0]), 1, seg(new byte[payloadLen - 1])));
            }
        }

        @Test
        @DisplayName("totalLen ceiling counts the address bytes")
        void totalLenCeilingWithAddress() {
            try (var ring = new SctpRingBuffer(0x8000)) {
                var address = pattern(10, 1);
                // totalLen == 0x2001 with addr counted -> rejected
                var payloadLen = 0x2001 - HEADER_SIZE - address.length;
                assertFalse(ring.write(seg(address), 1, seg(new byte[payloadLen])));
                assertTrue(ring.write(seg(address), 1, seg(new byte[payloadLen - 1])));
            }
        }

        @Test
        @DisplayName("a write after close is rejected")
        void rejectAfterClose() {
            var ring = new SctpRingBuffer(64);
            ring.close();
            assertFalse(ring.isInitialized());
            assertFalse(ring.write(seg(bytes(1)), 1, seg(bytes(2))));
        }

        @Test
        @DisplayName("an address longer than 255 bytes is rejected with an exception")
        void addressTooLong() {
            try (var ring = new SctpRingBuffer(0x8000)) {
                assertThrows(IllegalArgumentException.class,
                        () -> ring.write(seg(new byte[256]), 1, seg(bytes(1))));
            }
        }

        @Test
        @DisplayName("null address or payload throws")
        void nullArguments() {
            try (var ring = new SctpRingBuffer(64)) {
                assertThrows(NullPointerException.class, () -> ring.write(null, 1, seg(bytes(1))));
                assertThrows(NullPointerException.class, () -> ring.write(seg(bytes(1)), 1, null));
            }
        }

        @Test
        @DisplayName("null sink throws on poll and drain")
        void nullSink() {
            try (var ring = new SctpRingBuffer(64)) {
                assertThrows(NullPointerException.class, () -> ring.poll(null));
                assertThrows(NullPointerException.class, () -> ring.drain(null));
            }
        }
    }

    @Nested
    @DisplayName("cross-check against an independent spec reader")
    class IndependentReader {
        @Test
        @DisplayName("an independent reader decodes the same records poll does, including wraps")
        void mirrorsSpecReader() {
            // A power-of-two capacity keeps this a pure decoder-equivalence oracle: the dedicated
            // FreeSpace and NoOverwrite tests carry the non-power-of-two free-space divergence, so
            // here both decoders must agree on a ring whose write path is correct.
            var capacity = 128;
            var random = new Random(0xC0FFEE);
            // Drive a single producer; mirror every accepted frame into the spec reader's expectation
            // and confirm poll and the spec reader agree byte-for-byte.
            try (var ring = new SctpRingBuffer(capacity)) {
                var specReader = new SpecReader(ring.segment(), capacity);
                var expected = new ArrayDeque<Record>();
                for (var round = 0; round < 5000; round++) {
                    if (random.nextBoolean() && !expected.isEmpty()) {
                        // Decode the next record with the independent reader first, while it is still
                        // unread and the producer cannot reclaim its bytes; then drain the same record
                        // through poll. The shadow index and the real read index advance in lockstep,
                        // so both decode the same logical record.
                        var viaSpec = specReader.read();
                        var viaPoll = new ArrayList<Record>();
                        var polled = ring.poll(collector(viaPoll));
                        assertTrue(polled);
                        var want = expected.poll();
                        assertArrayEquals(want.address(), viaPoll.getFirst().address());
                        assertEquals(want.port(), viaPoll.getFirst().port());
                        assertArrayEquals(want.payload(), viaPoll.getFirst().payload());
                        // the independent reader, advancing its own shadow readIdx, sees the same bytes
                        assertArrayEquals(want.address(), viaSpec.address());
                        assertEquals(want.port(), viaSpec.port());
                        assertArrayEquals(want.payload(), viaSpec.payload());
                    } else {
                        var addrLen = random.nextInt(4);
                        var payloadLen = 1 + random.nextInt(20);
                        var address = pattern(addrLen, random.nextInt(200));
                        var payload = pattern(payloadLen, random.nextInt(200));
                        var port = random.nextInt(0x10000);
                        if (ring.write(seg(address), port, seg(payload))) {
                            expected.add(new Record(address, port, payload));
                        }
                    }
                }
            }
        }

        /**
         * A drain-side decoder reconstructed only from SPEC 14.7: it reads the five-byte little-endian
         * header at its own shadow read index, recovers the address and payload across the wrap
         * boundary, and advances its shadow index by {@code totalLen} modulo capacity, exactly as a
         * native host drain loop would. It reads the producer's segment but never touches the segment's
         * real read index; the caller advances that separately via {@link SctpRingBuffer#poll}, so the
         * two decoders walk the same logical byte stream from the same start position independently.
         */
        private static final class SpecReader {
            private final MemorySegment segment;
            private final int capacity;
            private long shadowReadIdx;

            private SpecReader(MemorySegment segment, int capacity) {
                this.segment = segment;
                this.capacity = capacity;
                this.shadowReadIdx = 0L;
            }

            private int at(long index) {
                return segment.get(ValueLayout.JAVA_BYTE, DATA_OFFSET + Math.floorMod(index, capacity)) & 0xFF;
            }

            private byte[] slice(long index, int length) {
                var out = new byte[length];
                for (var i = 0; i < length; i++) {
                    out[i] = (byte) at(index + i);
                }
                return out;
            }

            private Record read() {
                var readIdx = shadowReadIdx;
                var addrLen = at(readIdx);
                var port = at(readIdx + 1) | (at(readIdx + 2) << 8);
                var totalLen = at(readIdx + 3) | (at(readIdx + 4) << 8);
                var payloadLen = totalLen - addrLen - HEADER_SIZE;
                var address = slice(readIdx + HEADER_SIZE, addrLen);
                var payload = slice(readIdx + HEADER_SIZE + addrLen, payloadLen);
                this.shadowReadIdx = (readIdx + totalLen) % capacity;
                return new Record(address, port, payload);
            }
        }
    }

    @Nested
    @DisplayName("single-producer single-consumer concurrency")
    class Concurrency {
        @Test
        @DisplayName("a producer and consumer exchange every record intact under interleaving")
        void spscFuzz() throws InterruptedException {
            var capacity = 4096;
            var totalRecords = 200_000;
            var failure = new AtomicReference<Throwable>();

            try (var ring = new SctpRingBuffer(capacity)) {
                var consumed = new ArrayList<Record>(totalRecords);
                var consumer = Thread.ofVirtual().unstarted(() -> {
                    try {
                        var sink = (SctpRingBuffer.FrameSink) (address, port, payload) ->
                                consumed.add(new Record(address, port, payload));
                        while (consumed.size() < totalRecords) {
                            if (ring.poll(sink) == false) {
                                Thread.onSpinWait();
                            }
                        }
                    } catch (Throwable t) {
                        failure.compareAndSet(null, t);
                    }
                });

                var expected = new ArrayList<Record>(totalRecords);
                var random = new Random(0xABCDEF);
                for (var i = 0; i < totalRecords; i++) {
                    var addrLen = random.nextInt(5);
                    var payloadLen = 1 + random.nextInt(50);
                    var address = pattern(addrLen, i);
                    var payload = pattern(payloadLen, i * 3 + 1);
                    var port = i & 0xFFFF;
                    expected.add(new Record(address, port, payload));
                }

                consumer.start();
                var producer = Thread.ofVirtual().start(() -> {
                    try {
                        for (var i = 0; i < totalRecords; i++) {
                            var record = expected.get(i);
                            while (!ring.write(seg(record.address()), record.port(), seg(record.payload()))) {
                                Thread.onSpinWait();
                            }
                        }
                    } catch (Throwable t) {
                        failure.compareAndSet(null, t);
                    }
                });

                producer.join();
                consumer.join();

                assertNull(failure.get(), () -> "thread failed: " + failure.get());
                assertEquals(totalRecords, consumed.size());
                for (var i = 0; i < totalRecords; i++) {
                    assertArrayEquals(expected.get(i).address(), consumed.get(i).address(),
                            "address of record " + i + " corrupted under concurrency");
                    assertEquals(expected.get(i).port(), consumed.get(i).port(),
                            "port of record " + i + " corrupted under concurrency");
                    assertArrayEquals(expected.get(i).payload(), consumed.get(i).payload(),
                            "payload of record " + i + " corrupted under concurrency");
                }
            }
        }

        @Test
        @DisplayName("interleaved drain never reports more records than were written")
        void drainBounded() throws InterruptedException {
            var capacity = 2048;
            var totalRecords = 50_000;
            var failure = new AtomicReference<Throwable>();

            try (var ring = new SctpRingBuffer(capacity)) {
                var drainedCount = new int[]{0};
                var consumer = Thread.ofVirtual().unstarted(() -> {
                    try {
                        var sink = (SctpRingBuffer.FrameSink) (a, p, pl) -> {
                        };
                        while (drainedCount[0] < totalRecords) {
                            drainedCount[0] += ring.drain(sink);
                        }
                    } catch (Throwable t) {
                        failure.compareAndSet(null, t);
                    }
                });
                consumer.start();

                var producer = Thread.ofVirtual().start(() -> {
                    try {
                        var random = new Random(7);
                        for (var i = 0; i < totalRecords; i++) {
                            var payload = pattern(1 + random.nextInt(30), i);
                            while (!ring.write(seg(new byte[random.nextInt(4)]), i & 0xFFFF, seg(payload))) {
                                Thread.onSpinWait();
                            }
                        }
                    } catch (Throwable t) {
                        failure.compareAndSet(null, t);
                    }
                });

                producer.join();
                consumer.join();
                assertNull(failure.get(), () -> "thread failed: " + failure.get());
                assertEquals(totalRecords, drainedCount[0]);
            }
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class Lifecycle {
        @Test
        @DisplayName("a fresh ring is initialized and empty")
        void freshState() {
            try (var ring = new SctpRingBuffer(64)) {
                assertTrue(ring.isInitialized());
                assertEquals(64, ring.capacity());
                assertEquals(0, ring.drain((a, p, pl) -> {
                }));
            }
        }

        @Test
        @DisplayName("close clears initialization and is idempotent")
        void closeIdempotent() {
            var ring = new SctpRingBuffer(64);
            ring.close();
            assertFalse(ring.isInitialized());
            ring.close();
            assertFalse(ring.isInitialized());
        }
    }
}
