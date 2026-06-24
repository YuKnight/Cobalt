package com.github.auties00.cobalt.calls2.core;

/**
 * Enumerates the eleven per-call timers the wa-voip engine arms over a call's lifetime.
 *
 * <p>Each call owns its own set of timers on the engine's timer heap; in Cobalt they run on a
 * virtual-thread scheduler with the same faithful constants. This enum names the eleven timers so the
 * {@link Calls2LifecycleController} can arm and cancel them by kind through
 * {@link Calls2CallTimerScheduler} without depending on the timer unit's internal scheduling. The
 * controller arms the lifecycle-relevant timers directly (the caller-lonely timer when an outbound call
 * starts ringing, the 1000ms watchdog while a call is being set up) and cancels them all when the call
 * tears down; the connected-lonely timer is armed by the state-transition guard as a call enters the
 * connected-lonely state rather than by the controller, matching the engine.
 *
 * @implNote This implementation ports the timer set of {@code call_timer.cc} in the wa-voip WASM module
 * {@code ff-tScznZ8P}: {@link #PERIODIC} ({@code periodic_call_timer_callback}, fn10932, the 1000ms
 * watchdog at {@code call_context+0x188}), {@link #CALLER_LONELY} ({@code caller_lonely_state_timer_callback},
 * fn10934), {@link #OHAI} ({@code ohai_request_timer_callback}, fn10935), {@link #HEARTBEAT}
 * ({@code heartbeat_callback}, fn10936, the group-call heartbeat), {@link #LOBBY}
 * ({@code lobby_timer_callback}, fn10937, the waiting-room poll), {@link #CONNECTED_LONELY}
 * ({@code connected_lonely_state_timer_callback}, fn10939), {@link #UPDATE_ENCRYPTION_KEY}
 * ({@code update_encryption_key_timer_callback}, fn10940, the group SFrame rotation), {@link #REACTION_CLEAR}
 * ({@code call_reaction_clear} timer, fn10941), {@link #VIDEO_UPGRADE}
 * ({@code call_video_upgrade_timer_callback}, fn10943), {@link #E2EE_RESTORE}
 * ({@code e2ee_restore_timer_callback}, fn10944), and {@link #APP_DATA_STREAM_TEST}
 * ({@code app_data_stream_test_callback}). The native teardown {@code stop_call_timer_worker_thread}
 * (fn10952) cancels every one of these entries; {@link Calls2CallTimerScheduler#cancelAll(String)} is its
 * Cobalt analogue.
 */
public enum Calls2CallTimerKind {
    /**
     * Identifies the 1000ms self-rescheduling watchdog that enforces per-peer and per-call setup
     * deadlines.
     *
     * <p>The watchdog sweeps for unanswered group-call offers past the per-peer timeout, unanswered mute
     * requests, and the two call-setup deadlines, then reschedules itself; the controller arms it while a
     * call is being set up.
     */
    PERIODIC,

    /**
     * Identifies the timer that ends an outbound one-to-one call whose peer never answers.
     *
     * <p>The controller arms this when an outbound call begins ringing; on expiry the call ends with a
     * lonely-state timeout.
     */
    CALLER_LONELY,

    /**
     * Identifies the timer guarding an outstanding {@code ohai} keepalive request.
     */
    OHAI,

    /**
     * Identifies the group-call heartbeat timer that periodically re-sends a heartbeat signal.
     */
    HEARTBEAT,

    /**
     * Identifies the waiting-room lobby poll timer, scheduled in minutes while a pending call sits in the
     * lobby.
     */
    LOBBY,

    /**
     * Identifies the timer that ends a connected-but-alone call when no peer connects through all of its
     * intervals.
     *
     * <p>Armed by the state-transition guard as a call enters {@link Calls2CallState#CONNECTED_LONELY} and
     * cancelled as it leaves; the controller cancels it on teardown but does not arm it.
     */
    CONNECTED_LONELY,

    /**
     * Identifies the group-call SFrame key-rotation tick that periodically rotates the end-to-end key.
     */
    UPDATE_ENCRYPTION_KEY,

    /**
     * Identifies the timer that clears a delivered reaction's local state after its display window.
     */
    REACTION_CLEAR,

    /**
     * Identifies the timer that downgrades a video-upgrade request the peer never accepts.
     */
    VIDEO_UPGRADE,

    /**
     * Identifies the timer that restores end-to-end encryption after a transient escalation.
     */
    E2EE_RESTORE,

    /**
     * Identifies the timer that probes the application-data stream during setup.
     */
    APP_DATA_STREAM_TEST
}
