package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.model.call.CallState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Calls2CallState machine")
class Calls2CallStateTest {
    @Test
    @DisplayName("declares the fifteen engine states")
    void cardinality() {
        assertEquals(15, Calls2CallState.values().length);
    }

    @ParameterizedTest
    @EnumSource(Calls2CallState.class)
    @DisplayName("each state round-trips through its wire ordinal")
    void wireOrdinalRoundTrip(Calls2CallState state) {
        assertSame(state, Calls2CallState.ofWireOrdinal(state.wireOrdinal()).orElseThrow());
    }

    @ParameterizedTest
    @EnumSource(Calls2CallState.class)
    @DisplayName("projects every internal state onto a public phase")
    void toPublicIsTotal(Calls2CallState state) {
        assertNotNull(state.toPublic());
    }

    @Test
    @DisplayName("collapses the internal states onto the expected public phases")
    void projectionSpotChecks() {
        assertEquals(CallState.RINGING, Calls2CallState.CALLING.toPublic());
        assertEquals(CallState.RINGING, Calls2CallState.RECEIVED_CALL.toPublic());
        assertEquals(CallState.CONNECTING, Calls2CallState.ACCEPT_SENT.toPublic());
        assertEquals(CallState.CONNECTING, Calls2CallState.LINK.toPublic());
        assertEquals(CallState.ACTIVE, Calls2CallState.CALL_ACTIVE.toPublic());
        assertEquals(CallState.ACTIVE, Calls2CallState.CONNECTED_LONELY.toPublic());
        assertEquals(CallState.RECONNECTING, Calls2CallState.REJOINING.toPublic());
        assertEquals(CallState.ENDED, Calls2CallState.NONE.toPublic());
        assertEquals(CallState.ENDED, Calls2CallState.ENDING.toPublic());
        assertEquals(CallState.ENDED, Calls2CallState.CALL_ACTIVE_ELSEWHERE.toPublic());
    }

    @Test
    @DisplayName("an out-of-range wire ordinal resolves to empty")
    void outOfRangeIsEmpty() {
        assertTrue(Calls2CallState.ofWireOrdinal(-1).isEmpty());
        assertTrue(Calls2CallState.ofWireOrdinal(15).isEmpty());
    }
}
