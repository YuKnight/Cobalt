package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.model.call.CallEndReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Calls2CallResult and CallEndReason wire contract")
class Calls2CallResultTest {
    @ParameterizedTest
    @EnumSource(Calls2CallResult.class)
    @DisplayName("projects every result onto a public end reason")
    void toEndReasonIsTotal(Calls2CallResult result) {
        assertNotNull(result.toEndReason());
    }

    @Test
    @DisplayName("collapses results onto the expected end reasons")
    void projectionSpotChecks() {
        assertSame(CallEndReason.UNKNOWN, Calls2CallResult.ACCEPTED.toEndReason());
        assertSame(CallEndReason.SETUP_FAILED, Calls2CallResult.SETUP_ERROR.toEndReason());
        assertSame(CallEndReason.SETUP_FAILED, Calls2CallResult.SERVER_NACK.toEndReason());
        assertSame(CallEndReason.SETUP_FAILED, Calls2CallResult.CALL_OFFER_ACK_NOT_RECEIVED.toEndReason());
        assertSame(CallEndReason.SETUP_FAILED, Calls2CallResult.PEER_SETUP_ERROR.toEndReason());
        assertSame(CallEndReason.ACCEPTED_ELSEWHERE, Calls2CallResult.ACCEPTED_ELSEWHERE.toEndReason());
        assertSame(CallEndReason.REJECTED_ELSEWHERE, Calls2CallResult.REJECTED_ELSEWHERE.toEndReason());
        assertSame(CallEndReason.SETUP_FAILED, Calls2CallResult.CALL_DOES_NOT_EXIST_FOR_REJOIN.toEndReason());
        assertSame(CallEndReason.SETUP_FAILED, Calls2CallResult.CALL_IS_FULL.toEndReason());
    }

    @Test
    @DisplayName("collapses the reject family onto the generic rejected reason")
    void rejectFamilyCollapses() {
        for (var result : new Calls2CallResult[]{
                Calls2CallResult.REJECTED_BY_USER,
                Calls2CallResult.REJECTED_BY_SERVER,
                Calls2CallResult.REJECTED_UNAVAILABLE,
                Calls2CallResult.REJECTED_TOS,
                Calls2CallResult.REJECTED_E2E}) {
            assertSame(CallEndReason.REJECTED, result.toEndReason(), () -> result + " must collapse to REJECTED");
        }
    }

    @ParameterizedTest
    @EnumSource(Calls2CallResult.class)
    @DisplayName("every result now carries a present wire code")
    void wireCodeIsAlwaysPresent(Calls2CallResult result) {
        assertTrue(result.wireCode().isPresent(), () -> result + " must carry a recovered wire code");
    }

    @ParameterizedTest
    @DisplayName("each result binds its recovered numeric code")
    @CsvSource({
            // engine call-result codes (result_to_str table @ 0x125a18, matches CALL_RESULT_TYPE)
            "ACCEPTED, 1", "SETUP_ERROR, 6", "SERVER_NACK, 7", "CALL_OFFER_ACK_NOT_RECEIVED, 8",
            "PEER_SETUP_ERROR, 19", "ACCEPTED_ELSEWHERE, 22", "REJECTED_ELSEWHERE, 23",
            // accept-ack NACK codes from handle_accept_ack (fn11502): 404 -> 0x1c, 434 -> 0x19
            "CALL_DOES_NOT_EXIST_FOR_REJOIN, 28", "CALL_IS_FULL, 25",
            // reject-family codes from result_to_str (table 0x125a18) and reject_reason_to_call_result (fn10924)
            "REJECTED_BY_USER, 2", "REJECTED_BY_SERVER, 3", "REJECTED_TOS, 15", "REJECTED_E2E, 16",
            "REJECTED_UNAVAILABLE, 17"
    })
    void recoveredWireCodes(Calls2CallResult result, int expected) {
        assertEquals(expected, result.wireCode().orElseThrow(),
                () -> result + " must bind its recovered call-result code");
    }

    @Test
    @DisplayName("maps accept-ack NACK errors onto the engine result, empty otherwise")
    void acceptAckNackResults() {
        assertSame(Calls2CallResult.CALL_DOES_NOT_EXIST_FOR_REJOIN,
                Calls2CallResult.fromAcceptAckError(404).orElseThrow());
        assertSame(Calls2CallResult.CALL_IS_FULL,
                Calls2CallResult.fromAcceptAckError(434).orElseThrow());
        assertTrue(Calls2CallResult.fromAcceptAckError(500).isEmpty());
        assertTrue(Calls2CallResult.fromAcceptAckError(0).isEmpty());
    }

    @Nested
    @DisplayName("CallEndReason wire round-trip")
    class CallEndReasonRoundTrip {
        @ParameterizedTest
        @EnumSource(CallEndReason.class)
        @DisplayName("every reason round-trips through its wire literal")
        void wireValueRoundTrip(CallEndReason reason) {
            assertSame(reason, CallEndReason.fromWireValue(reason.wireValue()));
        }

        @Test
        @DisplayName("resolves each of the eight calls2 additions")
        void newConstantsResolve() {
            assertSame(CallEndReason.SETUP_FAILED, CallEndReason.fromWireValue("setup_failed"));
            assertSame(CallEndReason.MEDIA_TX_TIMEOUT, CallEndReason.fromWireValue("media_tx_timeout"));
            assertSame(CallEndReason.MEDIA_RX_TIMEOUT, CallEndReason.fromWireValue("media_rx_timeout"));
            assertSame(CallEndReason.RELAY_BIND_FAILED, CallEndReason.fromWireValue("relay_bind_failed"));
            assertSame(CallEndReason.REJECTED_ELSEWHERE, CallEndReason.fromWireValue("rejected_elsewhere"));
            assertSame(CallEndReason.REJECTED, CallEndReason.fromWireValue("rejected"));
            assertSame(CallEndReason.AV_UPGRADABLE, CallEndReason.fromWireValue("av-upgradable"));
            assertSame(CallEndReason.AV_UPGRADE, CallEndReason.fromWireValue("av-upgrade"));
        }

        @Test
        @DisplayName("collapses an unknown literal and null to UNKNOWN")
        void unknownCollapse() {
            assertSame(CallEndReason.UNKNOWN, CallEndReason.fromWireValue("not_a_reason"));
            assertSame(CallEndReason.UNKNOWN, CallEndReason.fromWireValue(null));
            assertSame(CallEndReason.UNKNOWN, CallEndReason.fromWireValue(""));
        }

        @Test
        @DisplayName("UNKNOWN keeps its own stable wire literal")
        void unknownWireValue() {
            assertEquals("unknown", CallEndReason.UNKNOWN.wireValue());
        }
    }
}
