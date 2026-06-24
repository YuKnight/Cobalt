package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.calls2.util.TimerEntry;
import com.github.auties00.cobalt.calls2.util.TimerHeap;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Owns the eleven per-call timers of one wa-voip call context, scheduled on a single virtual-thread
 * driver that polls a {@link TimerHeap} and fires each due timeout on the same thread.
 *
 * <p>A call drives its lifecycle from a handful of self-rescheduling and one-shot timeouts: a periodic
 * watchdog that sweeps for unanswered peers and expired setup deadlines, the caller and connected lonely
 * timeouts that end an unanswered or peer-less call, the group heartbeat and key-rotation ticks, the
 * waiting-room lobby poll, the reaction-clear and video-upgrade and end-to-end-encryption-restore
 * one-shots, the relay-handshake ohai retry, and the application-data stream test. This class names each
 * of those timeouts as a member of {@link Timer} and arms, cancels, or reschedules it through one shared
 * {@link TimerHeap}. The native engine runs the same eleven entries on a dedicated pjlib timer worker
 * thread per call; this port replaces that worker with one virtual thread per call that repeatedly polls
 * the heap for the next deadline, sleeps until it, then drains and fires every due entry, so the firing
 * order is exactly the heap's deadline-then-schedule order.
 *
 * <p>Arming a timer that is already armed cancels the previous deadline first, so each {@link Timer} has
 * at most one outstanding deadline at any moment, matching the native {@code reschedule_call_timer_entry}
 * that cancels before it reschedules. Each timeout's callback runs on the driver thread, so a callback
 * may freely re-arm its own timer (the self-rescheduling watchdog, heartbeat, lobby, and connected-lonely
 * timers do exactly this) without racing the driver. The deadlines live in the {@link System#nanoTime()}
 * timebase the {@link TimerHeap} requires; the public arm methods accept a {@link Duration} delay and
 * convert it against the current reading.
 *
 * <p>The driver thread is started lazily on the first arm and runs until {@link #stop()}, which cancels
 * every outstanding timer and joins the thread so no callback runs after teardown. The class is
 * thread-safe: a single {@link ReentrantLock} guards the heap, the per-timer entry handles, and the
 * running flag, and the driver waits on a {@link Condition} signalled whenever a new earliest deadline is
 * armed so it never sleeps past a freshly armed timeout. Because callbacks run while the driver holds no
 * lock, a callback that arms or cancels a timer re-enters the lock without deadlock.
 *
 * @implNote This implementation ports {@code call_timer.cc} from the wa-voip WASM module
 * {@code ff-tScznZ8P}: the timer initialiser {@code init_call_timer} (fn10930), the eleven callbacks
 * ({@code periodic_call_timer_callback} fn10932, {@code caller_lonely_state_timer_callback} fn10934,
 * {@code ohai_request_timer_callback} fn10935, {@code heartbeat_callback} fn10936,
 * {@code lobby_timer_callback} fn10937, {@code connected_lonely_state_timer_callback} fn10939,
 * {@code update_encryption_key_timer_callback} fn10940, {@code call_reaction_clear_timer_callback}
 * fn10941, {@code call_video_upgrade_timer_callback} fn10943, {@code e2ee_restore_timer_callback}
 * fn10944, and the {@code app_data_stream_test_callback}), the schedule/reschedule helpers (fn10947 to
 * fn10952), and the teardown {@code stop_call_timer_worker_thread} (fn10952) that cancels every entry at
 * the context offsets {@code 0x188, 0x198, 0x1a8, 0x1c8, 0x1d8, 0x1e8, 0x1f8, 0x208, 0x218, 0x228,
 * 0x238, 0x278, 0x1c, 0x1b8}. The native per-call entries are {@code pj_timer_entry} slots on the call
 * context; this port keeps one {@link TimerEntry} handle per {@link Timer} in an {@link EnumMap} on the
 * heap whose ordering the {@link TimerHeap} javadoc documents as a faithful {@code pj_timer_heap} port.
 * The recovered timeout constants are carried on the {@link Timer} members and on
 * {@link #DEFAULT_CONNECTED_LONELY_INTERVALS_MS}; the native worker thread is replaced by one virtual
 * thread per call rather than a shared pool, because the virtual-thread blocking model the engine uses
 * makes a per-call driver cheap and keeps each call's timeouts isolated.
 */
public final class Calls2CallTimers {
    /**
     * Names the eleven per-call timers and binds each to its recovered nominal period.
     *
     * <p>Each member corresponds to one {@code pj_timer_entry} slot on the native call context and one of
     * the eleven {@code call_timer.cc} callbacks. The {@link #defaultPeriod()} a member carries is the
     * recovered nominal delay the native engine arms it with when the per-call configuration supplies no
     * override; a member whose period the engine computes per call from negotiated configuration (the
     * group {@link #HEARTBEAT} cadence and the {@link #LOBBY} poll) carries {@link Duration#ZERO} as a
     * "configured per call" sentinel and is always armed with an explicit delay through
     * {@link Calls2CallTimers#arm(Timer, Duration, Runnable)}. The {@link #CONNECTED_LONELY} member's
     * period is the first of the {@link Calls2CallTimers#DEFAULT_CONNECTED_LONELY_INTERVALS_MS} interval
     * array rather than a single value, because the connected-lonely timeout walks an interval sequence.
     */
    public enum Timer {
        /**
         * The one-second self-rescheduling watchdog that sweeps for unanswered group-call peers past the
         * {@link Calls2CallTimers#UNANSWERED_GROUP_OFFER_TIMEOUT} cutoff, unanswered mute requests, and the
         * two call-setup deadlines, then reschedules itself.
         *
         * @implNote This implementation binds the {@code periodic_call_timer_callback} (fn10932)
         * one-thousand-millisecond cadence ({@code periodic_period_ms = 1000}) at native context offset
         * {@code 0x188}.
         */
        PERIODIC(Duration.ofMillis(1000)),

        /**
         * The one-to-one caller timeout that ends an outbound call whose callee never answers within the
         * caller lonely-state window.
         *
         * @implNote This implementation binds the {@code caller_lonely_state_timer_callback} (fn10934) at
         * native context offset {@code 0x198}; the period defaults to the recovered short lonely-state
         * timeout {@link Calls2CallTimers#DEFAULT_CONNECTED_LONELY_SHORT_MS} (thirty seconds) and is reset
         * per call from the parsed {@code parse_lonely_state_timeouts} caller value.
         */
        CALLER_LONELY(Duration.ofMillis(Calls2CallTimers.DEFAULT_CONNECTED_LONELY_SHORT_MS)),

        /**
         * The relay-handshake retry that re-sends the relay reachability probe when the relay does not
         * answer the first one.
         *
         * @implNote This implementation binds the {@code ohai_request_timer_callback} (fn10935) at native
         * context offset {@code 0x1a8}; the engine computes its retry delay from the relay configuration,
         * so the default period is the one-second watchdog cadence as a conservative fallback until the
         * caller supplies the negotiated delay.
         */
        OHAI(Duration.ofMillis(1000)),

        /**
         * The group-call heartbeat that periodically emits a heartbeat signal to keep the call's
         * membership fresh.
         *
         * @implNote This implementation binds the {@code heartbeat_callback} (fn10936) at native context
         * offset {@code 0x1c8}; the native cadence is read per call from context offset {@code 0x3728} or
         * {@code 0x372c} in seconds, both seeded from the {@code heartbeat_interval_s} app setting that
         * {@code VoipInit::get_app_settings} (fn653) queries from the AB-prop bridge. That AB-prop (code
         * {@code 1430}, {@code int}) carries a live value and default both of {@code 10}, so the configured
         * cadence is {@code 10} seconds. This member still carries {@link Duration#ZERO} and is always armed
         * with that configured delay through {@link Calls2CallTimers#arm(Timer, Duration, Runnable)}, since
         * the native reads it per call rather than from a fixed constant.
         */
        HEARTBEAT(Duration.ZERO),

        /**
         * The waiting-room lobby poll that re-evaluates the local lobby state while the call sits in the
         * waiting room.
         *
         * @implNote This implementation binds the {@code lobby_timer_callback} (fn10937) at native context
         * offset {@code 0x1d8}; the native period is read per call from context offset {@code 0x3730} in
         * minutes, so this member carries {@link Duration#ZERO} and is always armed with an explicit
         * configured delay.
         */
        LOBBY(Duration.ZERO),

        /**
         * The connected lonely-state timeout that ends a connected call which has had no peer for the full
         * interval sequence.
         *
         * @implNote This implementation binds the {@code connected_lonely_state_timer_callback} (fn10939)
         * at native context offset {@code 0x1e8}; the callback walks the
         * {@link Calls2CallTimers#DEFAULT_CONNECTED_LONELY_INTERVALS_MS} interval array, so this member's
         * default period is the first interval ({@link Calls2CallTimers#DEFAULT_CONNECTED_LONELY_LONG_MS},
         * two hundred seventy seconds) and successive intervals are armed by the callback.
         */
        CONNECTED_LONELY(Duration.ofMillis(Calls2CallTimers.DEFAULT_CONNECTED_LONELY_LONG_MS)),

        /**
         * The group-call key-rotation tick that periodically rotates the SFrame end-to-end key.
         *
         * @implNote This implementation binds the {@code update_encryption_key_timer_callback} (fn10940)
         * at native context offset {@code 0x1f8}; the rotation cadence is configured per call, so this
         * member carries {@link Duration#ZERO} and is armed with an explicit configured delay.
         */
        UPDATE_ENCRYPTION_KEY(Duration.ZERO),

        /**
         * The reaction-clear one-shot that drops a displayed in-call reaction after its hold window.
         *
         * @implNote This implementation binds the {@code call_reaction_clear_timer_callback} (fn10941) at
         * native context offset {@code 0x1c} (the shared label region); the hold window is configured per
         * reaction, so this member carries {@link Duration#ZERO} and is armed with an explicit delay.
         */
        REACTION_CLEAR(Duration.ZERO),

        /**
         * The video-upgrade one-shot that downgrades a pending video upgrade the peer never accepted.
         *
         * @implNote This implementation binds the {@code call_video_upgrade_timer_callback} (fn10943) at
         * native context offset {@code 0x208}; the request timeout is configured per upgrade, so this
         * member carries {@link Duration#ZERO} and is armed with an explicit delay.
         */
        VIDEO_UPGRADE(Duration.ZERO),

        /**
         * The end-to-end-encryption-restore one-shot that re-enables end-to-end encryption after a
         * transient bot or escalation that disabled it.
         *
         * @implNote This implementation binds the {@code e2ee_restore_timer_callback} (fn10944) at native
         * context offset {@code 0x1c} (the shared label region {@code schedule_e2ee_restore_timer} reuses);
         * the restore delay is configured per call, so this member carries {@link Duration#ZERO} and is
         * armed with an explicit delay.
         */
        E2EE_RESTORE(Duration.ZERO),

        /**
         * The application-data stream test that probes the SCTP data channel after it opens.
         *
         * @implNote This implementation binds the {@code app_data_stream_test_callback} at native context
         * offset {@code 0x278}; the probe delay is configured per call, so this member carries
         * {@link Duration#ZERO} and is armed with an explicit delay.
         */
        APP_DATA_STREAM_TEST(Duration.ZERO);

        /**
         * Holds the recovered nominal period this timer is armed with when no override is supplied, or
         * {@link Duration#ZERO} when the engine computes the period per call.
         */
        private final Duration defaultPeriod;

        /**
         * Constructs a timer member bound to its recovered nominal period.
         *
         * @param defaultPeriod the nominal arm delay, or {@link Duration#ZERO} when configured per call
         */
        Timer(Duration defaultPeriod) {
            this.defaultPeriod = defaultPeriod;
        }

        /**
         * Returns the recovered nominal period this timer is armed with when no override is supplied.
         *
         * <p>A {@link Duration#ZERO} result is the "configured per call" sentinel for a timer whose period
         * the engine computes per call from negotiated configuration; such a timer must be armed through
         * {@link Calls2CallTimers#arm(Timer, Duration, Runnable)} with an explicit delay rather than
         * {@link Calls2CallTimers#armDefault(Timer, Runnable)}.
         *
         * @return the nominal arm delay, never {@code null}
         */
        public Duration defaultPeriod() {
            return defaultPeriod;
        }
    }

    /**
     * The recovered short connected lonely-state timeout, in milliseconds.
     *
     * @implNote This implementation binds {@code connected_lonely_default_short_ms} (native call context
     * word {@code ctx[0x28b89]}), the default thirty-second short interval parsed by
     * {@code parse_lonely_state_timeouts} in {@code call_membership.cc}.
     */
    static final long DEFAULT_CONNECTED_LONELY_SHORT_MS = 30000L;

    /**
     * The recovered long connected lonely-state interval, in milliseconds.
     *
     * @implNote This implementation binds {@code connected_lonely_default_long_ms} (native call context
     * word {@code ctx[0x28b84]}), the default two-hundred-seventy-second first interval of
     * {@code connected_lonely_state_timer_intervals_ms}.
     */
    static final long DEFAULT_CONNECTED_LONELY_LONG_MS = 270000L;

    /**
     * The recovered maximum connected lonely-state interval, in milliseconds.
     *
     * @implNote This implementation binds {@code connected_lonely_default_max_ms} (native call context
     * word {@code ctx[0x28b85]}), the default three-hundred-second final interval of
     * {@code connected_lonely_state_timer_intervals_ms}.
     */
    static final long DEFAULT_CONNECTED_LONELY_MAX_MS = 300000L;

    /**
     * The recovered default connected lonely-state interval sequence, in milliseconds, that
     * {@link Timer#CONNECTED_LONELY} walks before ending the call.
     *
     * <p>The connected-lonely callback arms the first interval on entry and each later interval on its
     * own expiry; on the final interval it sets the lonely-timeout log state and ends the call. This array
     * is the default sequence the engine uses when the per-call configuration supplies none, with the long
     * interval repeated up to the maximum so the call survives a transient peer absence yet still tears
     * down after the bounded sequence.
     *
     * @implNote This implementation binds the default {@code connected_lonely_state_timer_intervals_ms}
     * array (native call context offset {@code 0x22f40}, current index at {@code ctx+0x2a8}): the long
     * interval {@link #DEFAULT_CONNECTED_LONELY_LONG_MS} followed by the maximum interval
     * {@link #DEFAULT_CONNECTED_LONELY_MAX_MS}. The native array length and short-interval placement vary
     * by call direction (caller index zero, callee index one) under
     * {@code parse_lonely_state_timeouts}; this default carries the long-then-max callee sequence, and the
     * caller path arms {@link Timer#CALLER_LONELY} with {@link #DEFAULT_CONNECTED_LONELY_SHORT_MS}
     * instead.
     */
    static final long[] DEFAULT_CONNECTED_LONELY_INTERVALS_MS = {
            DEFAULT_CONNECTED_LONELY_LONG_MS,
            DEFAULT_CONNECTED_LONELY_MAX_MS
    };

    /**
     * The recovered per-peer unanswered-offer cutoff the {@link Timer#PERIODIC} watchdog enforces for a
     * group call: a peer that has not accepted within this window is marked and terminated.
     *
     * @implNote This implementation binds {@code unanswered_group_offer_timeout_ms} (the
     * {@code periodic_call_timer_callback} fn10932 {@code elapsed > 44999} check); the cutoff is
     * forty-five seconds and the strict {@code > 44999} comparison is reproduced by a forty-five-second
     * duration tested with strict-greater-than at the watchdog call site.
     */
    public static final Duration UNANSWERED_GROUP_OFFER_TIMEOUT = Duration.ofMillis(45000);

    /**
     * Name prefix for the per-call driver thread, with the call identifier appended for diagnosis.
     */
    private static final String DRIVER_THREAD_NAME_PREFIX = "cobalt-calls2-timers-";

    /**
     * Pairs a heap handle with the callback the driver runs when the handle's deadline fires.
     *
     * <p>The {@link TimerHeap} returns a {@link TimerEntry} handle from scheduling that is used only to
     * cancel the deadline; the callback the driver must run for that handle is held here so the driver
     * need not read the heap's internal per-entry callback. The handle and its callback are kept together
     * per armed {@link Timer} and looked up by handle identity when the heap reports an entry due.
     *
     * @param entry    the heap handle for the armed deadline; never {@code null}
     * @param callback the task the driver runs when the deadline fires; never {@code null}
     */
    private record Armed(TimerEntry entry, Runnable callback) {
    }

    /**
     * The call identifier this timer set belongs to, used only to name the driver thread.
     */
    private final String callId;

    /**
     * Guards the heap, the per-timer entry handles, the running flag, and the driver thread reference.
     */
    private final ReentrantLock lock;

    /**
     * Signalled whenever a newly armed timer may have advanced the earliest deadline, so the driver
     * re-reads the heap instead of sleeping past the new timeout, and signalled on {@link #stop()} to wake
     * the driver for exit.
     */
    private final Condition wakeup;

    /**
     * The single timer heap holding every outstanding deadline for this call in deadline order.
     */
    private final TimerHeap heap;

    /**
     * Holds the outstanding heap handle and callback for each armed {@link Timer}, or no mapping when the
     * timer is not currently armed.
     *
     * <p>Each {@link Armed} pairs the {@link TimerHeap} handle used to cancel the deadline with the
     * callback the driver runs when the deadline fires. The callback is held here rather than read back
     * from the {@link TimerEntry}, because the heap's per-entry callback accessor is internal to the timer
     * utility package; this map is the driver's own record of what to run for each fired entry.
     */
    private final Map<Timer, Armed> entries;

    /**
     * Whether the driver loop is running; cleared by {@link #stop()} to make the driver exit.
     */
    private boolean running;

    /**
     * The driver thread, retained so {@link #stop()} can signal and join it; {@code null} until the first
     * arm starts it and after {@link #stop()} clears it.
     */
    private Thread driver;

    /**
     * Constructs an idle timer set for one call with no timers armed and no driver thread started.
     *
     * <p>The driver thread is started lazily by the first arm, so a call that never arms a timer pays for
     * no thread. The {@code callId} is retained only to name the driver thread for diagnosis.
     *
     * @param callId the call identifier this timer set belongs to; must not be {@code null}
     * @throws NullPointerException if {@code callId} is {@code null}
     */
    public Calls2CallTimers(String callId) {
        this.callId = Objects.requireNonNull(callId, "callId cannot be null");
        this.lock = new ReentrantLock();
        this.wakeup = lock.newCondition();
        this.heap = new TimerHeap();
        this.entries = new EnumMap<>(Timer.class);
        this.running = false;
        this.driver = null;
    }

    /**
     * Arms a timer with its recovered {@link Timer#defaultPeriod()} so the callback fires once that delay
     * elapses.
     *
     * <p>Cancels any previous deadline for the same timer first, then schedules the callback at the
     * default period and starts the driver thread if it is not yet running. This convenience is for the
     * timers whose default period is a recovered constant ({@link Timer#PERIODIC},
     * {@link Timer#CALLER_LONELY}, {@link Timer#CONNECTED_LONELY}, {@link Timer#OHAI}); a timer whose
     * {@link Timer#defaultPeriod()} is {@link Duration#ZERO} carries no recovered constant and must be
     * armed through {@link #arm(Timer, Duration, Runnable)} with an explicit delay instead.
     *
     * @param timer    the timer to arm; must not be {@code null}
     * @param callback the task to run when the timer fires; must not be {@code null}
     * @throws NullPointerException     if {@code timer} or {@code callback} is {@code null}
     * @throws IllegalArgumentException if the timer's {@link Timer#defaultPeriod()} is
     *                                  {@link Duration#ZERO}, indicating it must be armed with an explicit
     *                                  delay
     */
    public void armDefault(Timer timer, Runnable callback) {
        Objects.requireNonNull(timer, "timer cannot be null");
        if (timer.defaultPeriod().isZero()) {
            throw new IllegalArgumentException("Timer " + timer + " has no default period and must be armed with an explicit delay");
        }
        arm(timer, timer.defaultPeriod(), callback);
    }

    /**
     * Arms a timer with an explicit delay so the callback fires once that delay elapses.
     *
     * <p>Cancels any previous deadline for the same timer first so each timer has at most one outstanding
     * deadline, schedules the callback at {@code now + delay} in the {@link System#nanoTime()} timebase,
     * starts the driver thread if it is not yet running, and signals the driver so it re-reads the heap
     * rather than sleeping past the new deadline. A non-positive delay arms a deadline that is immediately
     * due and fires on the driver's next poll. The callback runs on the driver thread and may re-arm this
     * or any other timer.
     *
     * @param timer    the timer to arm; must not be {@code null}
     * @param delay    the delay before the callback fires; must not be {@code null}
     * @param callback the task to run when the timer fires; must not be {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public void arm(Timer timer, Duration delay, Runnable callback) {
        Objects.requireNonNull(timer, "timer cannot be null");
        Objects.requireNonNull(delay, "delay cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");
        lock.lock();
        try {
            if (!running) {
                startDriver();
            }
            cancelLocked(timer);
            var entry = heap.schedule(System.nanoTime(), delay, callback);
            entries.put(timer, new Armed(entry, callback));
            wakeup.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Arms a timer with an explicit delay only while the driver is still running, otherwise does nothing.
     *
     * <p>Unlike {@link #arm(Timer, Duration, Runnable)} this never starts a stopped driver: it schedules
     * the deadline only when the driver is currently running and returns {@code false} without scheduling
     * when {@link #stop()} has already stopped it. This is the variant a self-rescheduling callback uses to
     * re-arm itself, so that a re-arm racing a concurrent {@link #stop()} cannot resurrect a stopped driver
     * (which would spawn a new driver thread and leave a {@link #stop()} joining the old one forever). The
     * check and the schedule are atomic under the same lock {@link #stop()} takes, so the re-arm either wins
     * the race and is then cancelled by the imminent {@link #stop()}, or loses it and is a no-op. The
     * callback runs on the driver thread and may itself re-arm through this method.
     *
     * @param timer    the timer to arm; must not be {@code null}
     * @param delay    the delay before the callback fires; must not be {@code null}
     * @param callback the task to run when the timer fires; must not be {@code null}
     * @return {@code true} if the timer was armed, {@code false} if the driver was already stopped
     * @throws NullPointerException if any argument is {@code null}
     */
    public boolean armIfRunning(Timer timer, Duration delay, Runnable callback) {
        Objects.requireNonNull(timer, "timer cannot be null");
        Objects.requireNonNull(delay, "delay cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");
        lock.lock();
        try {
            if (!running) {
                return false;
            }
            cancelLocked(timer);
            var entry = heap.schedule(System.nanoTime(), delay, callback);
            entries.put(timer, new Armed(entry, callback));
            wakeup.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancels a timer so its callback never runs, if it is currently armed.
     *
     * <p>Removes the timer's outstanding deadline from the heap and forgets its handle; cancelling a timer
     * that is not armed is a no-op. The driver thread is left running because other timers may still be
     * armed; the thread idles on the heap until it is empty and a deadline is next armed.
     *
     * @param timer the timer to cancel; must not be {@code null}
     * @return {@code true} if this call cancelled an armed timer, {@code false} if it was not armed
     * @throws NullPointerException if {@code timer} is {@code null}
     */
    public boolean cancel(Timer timer) {
        Objects.requireNonNull(timer, "timer cannot be null");
        lock.lock();
        try {
            return cancelLocked(timer);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns whether a timer currently has an outstanding deadline.
     *
     * @param timer the timer to query; must not be {@code null}
     * @return {@code true} if the timer is armed, {@code false} otherwise
     * @throws NullPointerException if {@code timer} is {@code null}
     */
    public boolean isArmed(Timer timer) {
        Objects.requireNonNull(timer, "timer cannot be null");
        lock.lock();
        try {
            var armed = entries.get(timer);
            return armed != null && armed.entry().isScheduled();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancels every armed timer and stops the driver thread.
     *
     * <p>Clears the running flag, cancels every outstanding deadline, signals the driver to wake from its
     * sleep, and joins the driver thread so no callback runs after this method returns. Idempotent:
     * stopping an already-stopped timer set has no effect. A timer set stopped this way can be reused by a
     * later arm, which starts a fresh driver thread.
     *
     * @apiNote The call manager invokes this when a call reaches its terminal state, ending the secondary
     *          call context before the primary, so the per-call timer driver threads are torn down in the
     *          same order the contexts are freed.
     */
    public void stop() {
        Thread current;
        lock.lock();
        try {
            if (!running) {
                return;
            }
            running = false;
            for (var timer : Timer.values()) {
                cancelLocked(timer);
            }
            current = driver;
            driver = null;
            wakeup.signalAll();
        } finally {
            lock.unlock();
        }
        joinDriver(current);
    }

    /**
     * Cancels a timer's outstanding deadline while the lock is held.
     *
     * <p>Removes the timer's handle from the entry map and cancels it on the heap; returns whether a
     * still-scheduled entry was removed so the public {@link #cancel(Timer)} can report it. This method
     * must only be called while holding {@link #lock}.
     *
     * @param timer the timer to cancel
     * @return {@code true} if an armed entry was cancelled, {@code false} if the timer was not armed
     */
    private boolean cancelLocked(Timer timer) {
        var armed = entries.remove(timer);
        return armed != null && armed.entry().cancel();
    }

    /**
     * Starts the driver thread while the lock is held.
     *
     * <p>Sets the running flag and starts one virtual thread bound to {@link #drive()}, named with this
     * call's identifier for diagnosis. This method must only be called while holding {@link #lock} and
     * only when the driver is not already running.
     */
    private void startDriver() {
        running = true;
        driver = Thread.ofVirtual()
                .name(DRIVER_THREAD_NAME_PREFIX + callId)
                .start(this::drive);
    }

    /**
     * Joins the driver thread outside the lock, ignoring an interrupt while waiting.
     *
     * <p>Joining outside the lock avoids holding the monitor while the driver finishes a callback. A
     * {@code null} thread (the driver never started, or another {@link #stop()} already cleared it) is a
     * no-op. If the joining thread is interrupted, its interrupt status is restored and the join is
     * abandoned so the caller is not blocked indefinitely.
     *
     * @param current the driver thread to join, or {@code null}
     */
    private void joinDriver(Thread current) {
        if (current == null || current == Thread.currentThread()) {
            return;
        }
        try {
            current.join();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * The driver thread body: repeatedly waits for the next deadline, then fires every due timer.
     *
     * <p>Each iteration computes, under the lock, the nanoseconds until the earliest deadline, then awaits
     * the {@link #wakeup} condition for that bound (or indefinitely when the heap is empty). A wakeup
     * arrives either because the bound elapsed or because a newly armed earlier deadline signalled the
     * condition; on waking the loop drains and fires every entry the heap reports due. Firing happens
     * outside the lock so a callback may re-enter the lock to arm or cancel a timer, and a callback that
     * throws is swallowed so one failing timeout does not stop the driver or skip the remaining due
     * timers. The loop exits when {@link #stop()} clears the running flag and signals the condition.
     */
    private void drive() {
        while (true) {
            Runnable due;
            lock.lock();
            try {
                if (!running) {
                    return;
                }
                var wait = heap.poll(System.nanoTime());
                if (wait > 0) {
                    awaitWait(wait);
                    continue;
                }
                var entry = heap.pollDue(System.nanoTime());
                if (entry == null) {
                    continue;
                }
                due = takeCallback(entry);
                if (due == null) {
                    continue;
                }
            } finally {
                lock.unlock();
            }
            fire(due);
        }
    }

    /**
     * Awaits the {@link #wakeup} condition for the given nanosecond bound while the lock is held.
     *
     * <p>Used when the next deadline is in the future and the heap is not empty: the driver sleeps up to
     * the bound, waking early if a newer earlier deadline is armed. A {@link Long#MAX_VALUE} bound (the
     * empty-heap signal from {@link TimerHeap#poll(long)}) awaits indefinitely until a deadline is armed
     * or {@link #stop()} signals. An interrupt restores the interrupt status and returns so the next loop
     * iteration re-checks the running flag. This method must only be called while holding {@link #lock}.
     *
     * @param waitNanos the nanoseconds until the earliest deadline, or {@link Long#MAX_VALUE} when the
     *                  heap is empty
     */
    private void awaitWait(long waitNanos) {
        try {
            if (waitNanos == Long.MAX_VALUE) {
                wakeup.await();
            } else {
                wakeup.awaitNanos(waitNanos);
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Removes the fired entry's mapping and returns the callback to run, while the lock is held.
     *
     * <p>Finds the {@link Armed} whose heap handle is the fired entry, removes it from {@link #entries} so
     * the now-stale handle is forgotten, and returns its callback for the driver to run outside the lock.
     * Returns {@code null} when no mapping points at the fired entry, which happens only when the timer was
     * cancelled and re-armed to a fresh entry between the heap firing and this lookup; in that case the
     * fired entry's callback must not run because a newer deadline has superseded it. The scan is over the
     * small fixed set of timers and so is constant work. This method must only be called while holding
     * {@link #lock}.
     *
     * @param fired the entry the heap just reported due
     * @return the callback to run, or {@code null} when the fired entry has been superseded
     */
    private Runnable takeCallback(TimerEntry fired) {
        var iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            var armed = iterator.next().getValue();
            if (armed.entry() == fired) {
                iterator.remove();
                return armed.callback();
            }
        }
        return null;
    }

    /**
     * Runs a fired timer's callback outside the lock, swallowing any thrown runtime exception.
     *
     * <p>A callback that throws is logged and dropped so one failing timeout neither stops the driver nor
     * prevents the remaining due timers from firing; the engine's per-timer callbacks are likewise
     * isolated from each other on the native worker thread.
     *
     * @param callback the fired timer's callback
     */
    private void fire(Runnable callback) {
        try {
            callback.run();
        } catch (RuntimeException exception) {
            System.getLogger(Calls2CallTimers.class.getName())
                    .log(System.Logger.Level.WARNING, "Call timer callback threw for call " + callId, exception);
        }
    }
}
