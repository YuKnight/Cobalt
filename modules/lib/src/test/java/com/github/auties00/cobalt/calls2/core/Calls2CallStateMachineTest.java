package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.model.jid.Jid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Calls2CallStateMachine transition guard (change_call_state_no_event)")
class Calls2CallStateMachineTest {
    private static final Jid PEER = Jid.of("258252122116273:94@lid");
    private static final Jid CREATOR = Jid.of("258252122116273:94@lid");
    private static final Jid SELF = Jid.of("39110693621863:0@lid");
    private static final Jid CHAT = Jid.of("258252122116273@lid");

    private final Calls2CallManager manager = new Calls2CallManager();
    private final Calls2CallStateMachine machine = new Calls2CallStateMachine(manager);

    private static Calls2CallContext context(Calls2CallContext.Calls2CallDirection direction) {
        return new Calls2CallContext(Calls2CallContext.Calls2CallRole.PRIMARY, direction,
                PEER, CREATOR, SELF, CHAT, false, false);
    }

    // Forces the context into a state directly, bypassing the guard, so a transition rule can be tested
    // from any starting state.
    private static void force(Calls2CallContext context, Calls2CallState state) {
        context.state(state);
    }

    @Nested
    @DisplayName("self-transition")
    class SelfTransition {
        @ParameterizedTest
        @EnumSource(Calls2CallState.class)
        @DisplayName("to the current state is an accepted no-op carrying no event")
        void selfTransitionIsNoOp(Calls2CallState state) {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, state);
            var transition = machine.transition(context, state);
            assertTrue(transition.accepted());
            assertFalse(transition.changedState());
            assertFalse(transition.shouldEmitEvent());
            assertSame(state, context.state());
        }
    }

    @Nested
    @DisplayName("ConnectedLonely closed transition set")
    class FromConnectedLonely {
        @Test
        @DisplayName("to None is legal")
        void toNone() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CONNECTED_LONELY);
            assertTrue(machine.transition(context, Calls2CallState.NONE).accepted());
        }

        @Test
        @DisplayName("to CallActive is legal and emits the state-changed event")
        void toCallActive() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CONNECTED_LONELY);
            var transition = machine.transition(context, Calls2CallState.CALL_ACTIVE);
            assertTrue(transition.accepted());
            assertTrue(transition.shouldEmitEvent());
            assertSame(CallEventType.CALL_STATE_CHANGED, transition.event().orElseThrow());
            assertSame(Calls2CallState.CALL_ACTIVE, context.state());
        }

        @ParameterizedTest
        @EnumSource(value = Calls2CallState.class, names = {"NONE", "CALL_ACTIVE", "CONNECTED_LONELY"},
                mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("to any other state is rejected without mutation")
        void otherTargetsRejected(Calls2CallState target) {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CONNECTED_LONELY);
            var transition = machine.transition(context, target);
            assertFalse(transition.accepted());
            assertSame(Calls2CallState.CONNECTED_LONELY, context.state());
        }
    }

    @Nested
    @DisplayName("CallActive closed transition set")
    class FromCallActive {
        @Test
        @DisplayName("to None is legal")
        void toNone() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CALL_ACTIVE);
            assertTrue(machine.transition(context, Calls2CallState.NONE).accepted());
        }

        @Test
        @DisplayName("to ConnectedLonely is legal")
        void toConnectedLonely() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CALL_ACTIVE);
            assertTrue(machine.transition(context, Calls2CallState.CONNECTED_LONELY).accepted());
        }

        @Test
        @DisplayName("to Ending is rejected because the legal-set check precedes the silent-ending shortcut")
        void toEndingRejected() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CALL_ACTIVE);
            var transition = machine.transition(context, Calls2CallState.ENDING);
            assertFalse(transition.accepted());
            assertSame(Calls2CallState.CALL_ACTIVE, context.state());
        }

        @ParameterizedTest
        @EnumSource(value = Calls2CallState.class, names = {"NONE", "CALL_ACTIVE", "CONNECTED_LONELY"},
                mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("to any other state is rejected without mutation")
        void otherTargetsRejected(Calls2CallState target) {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CALL_ACTIVE);
            var transition = machine.transition(context, target);
            assertFalse(transition.accepted());
            assertSame(Calls2CallState.CALL_ACTIVE, context.state());
        }
    }

    @Nested
    @DisplayName("silent transitions")
    class Silent {
        @Test
        @DisplayName("into Link applies the state but emits no event")
        void enterLink() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CALLING);
            var transition = machine.transition(context, Calls2CallState.LINK);
            assertTrue(transition.accepted());
            assertFalse(transition.shouldEmitEvent());
            assertSame(Calls2CallState.LINK, context.state());
        }

        @Test
        @DisplayName("Link to None is a silent teardown")
        void linkTeardown() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.LINK);
            var transition = machine.transition(context, Calls2CallState.NONE);
            assertTrue(transition.accepted());
            assertFalse(transition.shouldEmitEvent());
            assertSame(Calls2CallState.NONE, context.state());
        }

        @Test
        @DisplayName("into Ending applies the state but emits no event")
        void enterEnding() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CALLING);
            var transition = machine.transition(context, Calls2CallState.ENDING);
            assertTrue(transition.accepted());
            assertFalse(transition.shouldEmitEvent());
            assertSame(Calls2CallState.ENDING, context.state());
        }

        @Test
        @DisplayName("Ending to None is a silent teardown")
        void endingTeardown() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.ENDING);
            var transition = machine.transition(context, Calls2CallState.NONE);
            assertTrue(transition.accepted());
            assertFalse(transition.shouldEmitEvent());
            assertSame(Calls2CallState.NONE, context.state());
        }
    }

    @Nested
    @DisplayName("duration accounting")
    class DurationAccounting {
        @Test
        @DisplayName("leaving CallActive accumulates the active segment elapsed time")
        void activeSegmentAccumulates() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.CONNECTED_LONELY);
            machine.transition(context, Calls2CallState.CALL_ACTIVE, 1_000L);
            machine.transition(context, Calls2CallState.CONNECTED_LONELY, 6_000L);
            assertEquals(5_000L, context.activeDurationMillis());
        }

        @Test
        @DisplayName("leaving ConnectedLonely accumulates the lonely segment elapsed time")
        void lonelySegmentAccumulates() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            // Enter ConnectedLonely from a setup leg so the lonely segment opens, then leave to CallActive.
            force(context, Calls2CallState.ACCEPT_SENT);
            machine.transition(context, Calls2CallState.CONNECTED_LONELY, 2_000L);
            machine.transition(context, Calls2CallState.CALL_ACTIVE, 9_000L);
            assertEquals(7_000L, context.lonelyDurationMillis());
        }

        @Test
        @DisplayName("active and lonely durations accumulate across multiple segments")
        void accumulatesAcrossSegments() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            force(context, Calls2CallState.ACCEPT_SENT);
            machine.transition(context, Calls2CallState.CALL_ACTIVE, 0L);          // active opens
            machine.transition(context, Calls2CallState.CONNECTED_LONELY, 1_000L); // +1000 active, lonely opens
            machine.transition(context, Calls2CallState.CALL_ACTIVE, 3_000L);      // +2000 lonely, active opens
            machine.transition(context, Calls2CallState.NONE, 6_000L);             // +3000 active
            assertEquals(4_000L, context.activeDurationMillis());
            assertEquals(2_000L, context.lonelyDurationMillis());
        }
    }

    @Nested
    @DisplayName("connected-lonely timer seams")
    class LonelyTimerSeams {
        @Test
        @DisplayName("entering ConnectedLonely fires the scheduler and entering CallActive cancels it")
        void scheduleThenCancel() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            var scheduled = new AtomicInteger();
            var cancelled = new AtomicInteger();
            context.onScheduleConnectedLonelyTimer(ctx -> scheduled.incrementAndGet());
            context.onCancelConnectedLonelyTimer(cancelled::incrementAndGet);

            force(context, Calls2CallState.ACCEPT_SENT);
            machine.transition(context, Calls2CallState.CONNECTED_LONELY);
            assertEquals(1, scheduled.get());

            machine.transition(context, Calls2CallState.CALL_ACTIVE);
            // Leaving lonely cancels once; entering active cancels once more (idempotent guard).
            assertEquals(2, cancelled.get());
        }

        @Test
        @DisplayName("the scheduler observes the new ConnectedLonely state when it fires")
        void schedulerSeesNewState() {
            var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
            context.onScheduleConnectedLonelyTimer(ctx ->
                    assertSame(Calls2CallState.CONNECTED_LONELY, ctx.state()));
            force(context, Calls2CallState.ACCEPT_SENT);
            machine.transition(context, Calls2CallState.CONNECTED_LONELY);
        }
    }

    @Nested
    @DisplayName("Calls2CallStateTransition seam (by call id)")
    class ByCallId {
        @Test
        @DisplayName("an accepted transition returns the prior state")
        void acceptedReturnsPriorState() {
            var primary = manager.startCall(Calls2CallContext.Calls2CallDirection.OUTGOING,
                    PEER, CREATOR, SELF, CHAT, false, false);
            primary.state(Calls2CallState.CALLING);
            var prior = machine.transition(primary.callId(), Calls2CallState.ACCEPT_RECEIVED);
            assertSame(Calls2CallState.CALLING, prior.orElseThrow());
            assertSame(Calls2CallState.ACCEPT_RECEIVED, primary.state());
        }

        @Test
        @DisplayName("a no-op self-transition returns the unchanged current state")
        void selfTransitionReturnsCurrent() {
            var primary = manager.startCall(Calls2CallContext.Calls2CallDirection.OUTGOING,
                    PEER, CREATOR, SELF, CHAT, false, false);
            primary.state(Calls2CallState.CALL_ACTIVE);
            assertSame(Calls2CallState.CALL_ACTIVE,
                    machine.transition(primary.callId(), Calls2CallState.CALL_ACTIVE).orElseThrow());
        }

        @Test
        @DisplayName("a rejected transition returns empty and leaves the state unchanged")
        void rejectedReturnsEmpty() {
            var primary = manager.startCall(Calls2CallContext.Calls2CallDirection.OUTGOING,
                    PEER, CREATOR, SELF, CHAT, false, false);
            primary.state(Calls2CallState.CALL_ACTIVE);
            assertTrue(machine.transition(primary.callId(), Calls2CallState.RECEIVED_CALL).isEmpty());
            assertSame(Calls2CallState.CALL_ACTIVE, primary.state());
        }

        @Test
        @DisplayName("an unknown call id returns empty")
        void unknownCallIdReturnsEmpty() {
            assertTrue(machine.transition("00000000000000000000000000000000",
                    Calls2CallState.CALL_ACTIVE).isEmpty());
        }
    }

    @Test
    @DisplayName("an accepted state-changing transition reports the prior and new states")
    void reportsEndpoints() {
        var context = context(Calls2CallContext.Calls2CallDirection.OUTGOING);
        force(context, Calls2CallState.CALLING);
        var transition = machine.transition(context, Calls2CallState.ACCEPT_RECEIVED);
        assertSame(Calls2CallState.CALLING, transition.oldState());
        assertSame(Calls2CallState.ACCEPT_RECEIVED, transition.newState());
        assertTrue(transition.changedState());
    }
}
