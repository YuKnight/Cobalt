package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.model.call.CallState;
import com.github.auties00.cobalt.model.jid.Jid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Calls2CallContext per-call aggregate")
class Calls2CallContextTest {
    private static final Jid PEER = Jid.of("258252122116273:94@lid");
    private static final Jid CREATOR = Jid.of("258252122116273:94@lid");
    private static final Jid SELF = Jid.of("39110693621863:0@lid");
    private static final Jid CHAT = Jid.of("258252122116273@lid");

    private static Calls2CallContext outgoing() {
        return new Calls2CallContext(Calls2CallContext.Calls2CallRole.PRIMARY,
                Calls2CallContext.Calls2CallDirection.OUTGOING, PEER, CREATOR, SELF, CHAT, false, false);
    }

    @Nested
    @DisplayName("call id generation")
    class CallId {
        @Test
        @DisplayName("produces a 32-character hex string")
        void length() {
            assertEquals(Calls2CallContext.CALL_ID_CHAR_LENGTH, Calls2CallContext.generateCallId().length());
            assertTrue(Calls2CallContext.generateCallId().matches("[0-9A-Fa-f]{32}"));
        }

        @Test
        @DisplayName("each byte renders an upper-case high nibble and a lower-case low nibble")
        void dualCaseNibbles() {
            // The high nibble indexes the upper-case alphabet half and the low nibble the lower-case half,
            // so for every byte the first hex digit is upper-case and the second is lower-case.
            var id = Calls2CallContext.generateCallId();
            for (var i = 0; i < id.length(); i += 2) {
                var high = id.charAt(i);
                var low = id.charAt(i + 1);
                assertTrue(Character.isDigit(high) || Character.isUpperCase(high),
                        () -> "high nibble char not upper/digit: " + high);
                assertTrue(Character.isDigit(low) || Character.isLowerCase(low),
                        () -> "low nibble char not lower/digit: " + low);
            }
        }

        @Test
        @DisplayName("draws distinct ids across many generations")
        void distinct() {
            var seen = new HashSet<String>();
            for (var i = 0; i < 1000; i++) {
                assertTrue(seen.add(Calls2CallContext.generateCallId()));
            }
        }
    }

    @Nested
    @DisplayName("public projection")
    class Projection {
        @Test
        @DisplayName("a fresh context projects NONE onto the ENDED public phase")
        void initialPhase() {
            assertSame(CallState.ENDED, outgoing().call().state());
        }

        @Test
        @DisplayName("setting the state republishes the projected public phase")
        void stateRepublishes() {
            var context = outgoing();
            context.state(Calls2CallState.CALLING);
            assertSame(CallState.RINGING, context.call().state());
            context.state(Calls2CallState.CALL_ACTIVE);
            assertSame(CallState.ACTIVE, context.call().state());
        }

        @Test
        @DisplayName("recording a result republishes the projected end reason")
        void resultRepublishes() {
            var context = outgoing();
            context.result(Calls2CallResult.SETUP_ERROR);
            assertSame(Calls2CallResult.SETUP_ERROR.toEndReason(), context.call().endReason().orElseThrow());
            assertSame(Calls2CallResult.SETUP_ERROR, context.result().orElseThrow());
        }

        @Test
        @DisplayName("the projected call carries the context identity and topology")
        void identity() {
            var context = outgoing();
            var call = context.call();
            assertEquals(context.callId(), call.callId());
            assertEquals(CHAT, call.chatJid());
            assertEquals(CREATOR, call.creator());
            assertTrue(call.isOutgoing());
        }
    }

    @Nested
    @DisplayName("duration segments")
    class Durations {
        @Test
        @DisplayName("a closed active segment accumulates its elapsed time")
        void activeSegment() {
            var context = outgoing();
            context.openActiveSegment(1_000L);
            assertEquals(4_000L, context.closeActiveSegment(5_000L));
            assertEquals(4_000L, context.activeDurationMillis());
        }

        @Test
        @DisplayName("closing with no open segment accumulates nothing")
        void noOpenSegment() {
            var context = outgoing();
            assertEquals(0L, context.closeActiveSegment(5_000L));
            assertEquals(0L, context.activeDurationMillis());
        }

        @Test
        @DisplayName("a backwards timestamp clamps the segment to zero")
        void backwardsClamp() {
            var context = outgoing();
            context.openLonelySegment(5_000L);
            assertEquals(0L, context.closeLonelySegment(1_000L));
            assertEquals(0L, context.lonelyDurationMillis());
        }
    }

    @Nested
    @DisplayName("connected-lonely config")
    class LonelyConfig {
        @Test
        @DisplayName("defaults carry the engine's parsed timeouts")
        void defaults() {
            var config = Calls2CallContext.ConnectedLonelyConfig.defaults();
            assertEquals(Calls2CallContext.CONNECTED_LONELY_DEFAULT_SHORT_MILLIS, config.shortMillis());
            assertEquals(Calls2CallContext.CONNECTED_LONELY_DEFAULT_LONG_MILLIS, config.longMillis());
            assertEquals(Calls2CallContext.CONNECTED_LONELY_DEFAULT_MAX_MILLIS, config.maxMillis());
        }

        @Test
        @DisplayName("picks the short interval for the caller and the long interval for the callee")
        void intervalByDirection() {
            var config = Calls2CallContext.ConnectedLonelyConfig.defaults();
            assertEquals(Calls2CallContext.CONNECTED_LONELY_DEFAULT_SHORT_MILLIS,
                    config.intervalForDirection(Calls2CallContext.Calls2CallDirection.OUTGOING));
            assertEquals(Calls2CallContext.CONNECTED_LONELY_DEFAULT_LONG_MILLIS,
                    config.intervalForDirection(Calls2CallContext.Calls2CallDirection.INCOMING));
        }
    }

    @Nested
    @DisplayName("resource teardown")
    class ResourceTeardown {
        @Test
        @DisplayName("close releases attached resources in reverse attachment order")
        void reverseOrderClose() {
            var context = outgoing();
            var order = new ArrayList<Integer>();
            context.attachResource(() -> order.add(1));
            context.attachResource(() -> order.add(2));
            context.attachResource(() -> order.add(3));
            context.close();
            assertEquals(java.util.List.of(3, 2, 1), order);
        }

        @Test
        @DisplayName("a resource that throws on close does not abort the teardown")
        void throwingResourceIsTolerated() {
            var context = outgoing();
            var closed = new ArrayList<Integer>();
            context.attachResource(() -> closed.add(1));
            context.attachResource(() -> {
                throw new IllegalStateException("boom");
            });
            context.attachResource(() -> closed.add(3));
            context.close();
            assertEquals(java.util.List.of(3, 1), closed);
        }
    }

    @Test
    @DisplayName("a known call id is pinned rather than generated")
    void pinnedCallId() {
        var context = new Calls2CallContext("ABCDEF0123456789abcdef0123456789",
                Calls2CallContext.Calls2CallRole.PRIMARY, Calls2CallContext.Calls2CallDirection.INCOMING,
                PEER, CREATOR, SELF, CHAT, true, true);
        assertEquals("ABCDEF0123456789abcdef0123456789", context.callId());
        assertEquals("ABCDEF0123456789abcdef0123456789", context.call().callId());
        assertTrue(context.group());
        assertTrue(context.video());
    }
}
