package com.github.auties00.cobalt.calls2.net.transport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("SctpDtlsBridge.Relay no-DTLS passthrough")
class SctpDtlsBridgeRelayTest {
    @Test
    @DisplayName("returns the outbound SCTP packet unchanged for the caller to ship")
    void passesOutboundThrough() {
        var bridge = new SctpDtlsBridge.Relay(null);
        var packet = new byte[]{1, 2, 3, 4};
        assertSame(packet, bridge.wrapOutbound(packet),
                "the relay path carries SCTP datagrams verbatim with no DTLS wrapping");
    }

    @Test
    @DisplayName("drops an inbound datagram when no association is wired")
    void dropsInboundWithoutAssociation() {
        var bridge = new SctpDtlsBridge.Relay(null);
        // The capture shows the relay path has no SCTP DataChannel; with no association the feed is a no-op.
        assertDoesNotThrow(() -> bridge.feedInbound(new byte[]{9, 9}));
    }

    @Test
    @DisplayName("close is a no-op and is idempotent")
    void closeIsNoOp() {
        var bridge = new SctpDtlsBridge.Relay(null);
        assertDoesNotThrow(bridge::close);
        assertDoesNotThrow(bridge::close);
    }

    @Test
    @DisplayName("round-trips bytes through the envelope and back on the relay path")
    void envelopeRoundTrip() {
        // The relay packet of a DTLS envelope is the raw payload; a passthrough bridge ships exactly that.
        var dtlsRecord = new byte[]{0x16, (byte) 0xfe, (byte) 0xfd, 0x00};
        var relayBytes = DtlsPacketEnvelope.of(dtlsRecord).relayPacket();
        var bridge = new SctpDtlsBridge.Relay(null);
        assertArrayEquals(dtlsRecord, bridge.wrapOutbound(relayBytes));
    }
}
