package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.calls2.core.participant.CallMembership;
import com.github.auties00.cobalt.calls2.net.transport.StreamLayout;
import com.github.auties00.cobalt.model.call.datachannel.StreamSubscriptions;
import com.github.auties00.cobalt.model.call.datachannel.StreamSubscriptionsSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the runtime population of the fused {@code 0x4024} {@link StreamSubscriptions} matrix in
 * {@code LiveMediaSession.LiveMediaPlane#buildStreamSubscriptions} reproduces the live group-call capture:
 * the participant-less self block first, then one positionally-indexed block of audio plus simulcast-video
 * entries per connected peer, with each entry's SSRC the deterministic per-device value the roster tracks.
 *
 * <p>The gold case feeds the exact per-(participant, stream) SSRCs the connected three-way group-call capture
 * decodes to (re/calls2-spec/captures/webrtc-datachannel-transport-2026-06-21.md) and asserts the built matrix
 * encodes to the captured ninety-five-byte attribute value byte-for-byte, so the population's participant
 * indexing (self omitted, peers one-based by roster order) and stream indexing (audio absent, video
 * {@code 1}/{@code 2}) are tied to the capture rather than to the implementation.
 */
@DisplayName("calls2 fused 0x4024 stream-subscription population")
class Calls2StreamSubscriptionPopulationTest {
    private static final HexFormat HEX = HexFormat.of();

    // The captured 0x4024 attribute value: nine repeated field-1 entries {participant?, stream?, ssrc}, from
    // the connected 3-way group call (self + 2 peers), as decoded in Calls2TransportWireFormatTest.
    private static final byte[] CAPTURED_SUBSCRIPTION_VALUE = HEX.parseHex(
            "0a0618c3a2a8930f"
                    + "0a08100118bbd0fce00e"
                    + "0a08100218b9e4b9be01"
                    + "0a08080118b1f2c8c00a"
                    + "0a0a0801100118cbdc86bc01"
                    + "0a0a0801100218e597cef80b"
                    + "0a08080218d2c8bfbb05"
                    + "0a09080210011899d1f31b"
                    + "0a0a0802100218daa898a40c");

    // The self stream SSRCs the capture's first (participant-less) block decodes to: audio main, then the two
    // simulcast video primary SSRCs. These are the local send layout's SSRCs.
    private static final int SELF_AUDIO = 0xf26a1143;
    private static final int SELF_VIDEO0 = 0xec1f283b;
    private static final int SELF_VIDEO1 = 0x17ce7239;

    // The two connected peers' stream SSRCs the capture's participant=1 and participant=2 blocks decode to.
    private static final CallMembership.PeerStreamSsrcs PEER_1 =
            new CallMembership.PeerStreamSsrcs(0xa8123931, 0x1781ae4b, 0xbf138be5);
    private static final CallMembership.PeerStreamSsrcs PEER_2 =
            new CallMembership.PeerStreamSsrcs(0x576fe452, 0x037ce899, 0xc486145a);

    private static StreamLayout videoSelfLayout() {
        return new StreamLayout(SELF_AUDIO, SELF_VIDEO0, SELF_VIDEO1, StreamLayout.ABSENT_SSRC,
                StreamLayout.ABSENT_SSRC, StreamLayout.ABSENT_SSRC, StreamLayout.ABSENT_SSRC, false);
    }

    private static StreamSubscriptions build(StreamLayout self, List<CallMembership.PeerStreamSsrcs> peers,
                                             boolean video) {
        return LiveMediaSession.LiveMediaPlane.buildStreamSubscriptions(self, peers, video);
    }

    @Nested
    @DisplayName("captured 3-way video group call")
    class CapturedGroupCall {
        @Test
        @DisplayName("population of self + two peers encodes to the captured 0x4024 value byte-for-byte")
        void matchesCapture() {
            var built = build(videoSelfLayout(), List.of(PEER_1, PEER_2), true);
            var encoded = StreamSubscriptionsSpec.encode(built);
            assertEquals(95, encoded.length);
            assertArrayEquals(CAPTURED_SUBSCRIPTION_VALUE, encoded,
                    "the populated matrix must reproduce the captured 0x4024 value byte-for-byte");
        }

        @Test
        @DisplayName("the self block omits the participant field and carries the own WARP audio SSRC first")
        void selfBlockShape() {
            var entries = build(videoSelfLayout(), List.of(PEER_1, PEER_2), true).entries();
            var selfAudio = entries.getFirst();
            assertTrue(selfAudio.participant().isEmpty(), "the self audio entry carries no participant index");
            assertTrue(selfAudio.stream().isEmpty(), "the self audio entry carries no stream index");
            assertEquals(Integer.toUnsignedLong(SELF_AUDIO), selfAudio.ssrc().orElseThrow());
        }

        @Test
        @DisplayName("each peer block is tagged with its one-based roster position, not a relay PID")
        void peerBlocksArePositional() {
            var entries = build(videoSelfLayout(), List.of(PEER_1, PEER_2), true).entries();
            // 3 self entries, then 3 per peer: peer 1 at index 3, peer 2 at index 6.
            var peer1Audio = entries.get(3);
            var peer2Audio = entries.get(6);
            assertEquals(OptionalInt.of(1), peer1Audio.participant());
            assertEquals(Integer.toUnsignedLong(PEER_1.audioSsrc()), peer1Audio.ssrc().orElseThrow());
            assertEquals(OptionalInt.of(2), peer2Audio.participant());
            assertEquals(Integer.toUnsignedLong(PEER_2.audioSsrc()), peer2Audio.ssrc().orElseThrow());
        }

        @Test
        @DisplayName("video entries carry stream index 1 and 2 with the per-stream simulcast SSRCs")
        void videoStreamIndices() {
            var entries = build(videoSelfLayout(), List.of(PEER_1, PEER_2), true).entries();
            var peer1Video0 = entries.get(4);
            var peer1Video1 = entries.get(5);
            assertEquals(OptionalInt.of(1), peer1Video0.participant());
            assertEquals(OptionalInt.of(1), peer1Video0.stream());
            assertEquals(Integer.toUnsignedLong(PEER_1.videoStream0Ssrc()), peer1Video0.ssrc().orElseThrow());
            assertEquals(OptionalInt.of(2), peer1Video1.stream());
            assertEquals(Integer.toUnsignedLong(PEER_1.videoStream1Ssrc()), peer1Video1.ssrc().orElseThrow());
        }
    }

    @Nested
    @DisplayName("degenerate and audio-only layouts")
    class Edges {
        @Test
        @DisplayName("an audio-only call emits only audio entries, no stream-indexed video entries")
        void audioOnlyOmitsVideo() {
            var audioSelf = StreamLayout.audioOnly(SELF_AUDIO);
            // Peers still carry derived video SSRCs, but the audio-only (video=false) call omits all video.
            var built = build(audioSelf, List.of(PEER_1, PEER_2), false);
            var entries = built.entries();
            assertEquals(3, entries.size(), "one audio entry for self and one per peer");
            for (var entry : entries) {
                assertTrue(entry.stream().isEmpty(), "no video stream entries on an audio-only call");
            }
            assertEquals(Integer.toUnsignedLong(SELF_AUDIO), entries.get(0).ssrc().orElseThrow());
            assertEquals(OptionalInt.of(1), entries.get(1).participant());
            assertEquals(OptionalInt.of(2), entries.get(2).participant());
        }

        @Test
        @DisplayName("no connected peers yields the self block alone")
        void selfOnly() {
            var built = build(videoSelfLayout(), List.of(), true);
            var entries = built.entries();
            assertEquals(3, entries.size(), "self audio plus two self video entries");
            for (var entry : entries) {
                assertTrue(entry.participant().isEmpty(), "the self block carries no participant index");
            }
        }

        @Test
        @DisplayName("a peer missing a simulcast video SSRC omits only that video entry")
        void peerMissingVideoStream() {
            var partialPeer = new CallMembership.PeerStreamSsrcs(0xa8123931, 0x1781ae4b, 0);
            var built = build(StreamLayout.audioOnly(SELF_AUDIO), List.of(partialPeer), true);
            var entries = built.entries();
            // self audio (1) + peer audio + peer video stream 0 only.
            assertEquals(3, entries.size());
            assertEquals(OptionalInt.of(1), entries.get(2).participant());
            assertEquals(OptionalInt.of(1), entries.get(2).stream());
            assertEquals(Integer.toUnsignedLong(0x1781ae4b), entries.get(2).ssrc().orElseThrow());
        }
    }
}
