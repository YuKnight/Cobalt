package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.calls2.core.participant.CallMembership;
import com.github.auties00.cobalt.model.call.Call;
import com.github.auties00.cobalt.model.jid.Jid;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * The per-call mutable aggregate the call engine keeps for one in-progress call.
 *
 * <p>This is the engine's {@code call_context}: the single mutable object that holds everything about
 * one call's lifecycle. It binds the call's immutable identity (the {@linkplain #callId() 64-character
 * call id}, the {@linkplain #role() manager role}, the {@linkplain #direction() direction}, the
 * {@linkplain #peer() peer}, {@linkplain #creator() creator}, {@linkplain #self() self}, and
 * {@linkplain #chatJid() chat} JIDs, and the {@linkplain #group() group}/{@linkplain #video() video}/
 * {@linkplain #outgoing() outgoing} topology) to its mutable runtime fields (the internal
 * {@linkplain #state() state}, the {@linkplain #linkState() link sub-state}, the
 * {@linkplain #result() result}, the {@linkplain #acceptReceived() accept-received} and
 * {@linkplain #mediaStarted() media-started} flags, and the {@linkplain #activeDurationMillis() active}
 * and {@linkplain #lonelyDurationMillis() lonely} duration accumulators), its
 * {@linkplain #membership() membership} set, and the {@linkplain #connectedLonelyConfig() connected-lonely
 * timeout configuration} the state machine reads when entering the lonely state.
 *
 * <p>Every mutation and every read of the runtime fields is serialized behind the context's own
 * {@linkplain #lock() lock}, the engine's per-call {@code call-info-mutex}: the state machine, the timer
 * callbacks, and the lifecycle controller all take it for the duration of an operation, so a state
 * transition and a duration-accounting update never interleave with a concurrent snapshot. The
 * {@link Calls2CallManager} pairs each of its two slots with one such lock; this class owns the lock so
 * the same instance can be moved between manager slots (a dual-call promotion) without rebinding it.
 *
 * <p>The context projects its public view onto the reused {@link Call} model: the same {@link Call}
 * instance the application observes is updated whenever this context's state, mute flags, or end reason
 * change, so a listener reading {@link Call#state()} sees the projection of {@link #state()} through
 * {@link Calls2CallState#toPublic()}. The heavyweight media and transport objects, and the eleven
 * per-call timers, are not modeled as typed fields here because they are owned by sibling subsystems;
 * the context holds them only as opaque {@linkplain #attachResource(AutoCloseable) resource} slots it
 * closes on teardown and as the {@linkplain #onScheduleConnectedLonelyTimer(Consumer) lonely-timer}
 * scheduling and cancelling seams the state machine drives, so this class stays decoupled from those
 * subsystems while still owning their lifecycle.
 *
 * <p>This class is not thread-safe by itself; callers serialize through {@link #lock()}. It is an
 * internal engine collaborator and is never exposed to embedders, who observe a call only through the
 * projected {@link Call}.
 *
 * @implNote This implementation composes the wa-voip WASM module {@code ff-tScznZ8P}
 * {@code call_context} struct (the {@code ~0x2c000}-int per-call blob laid out by {@code init_local_state},
 * fn10719, the 24-argument constructor) as discrete Java fields rather than as one flat blob: the
 * internal state ({@code call_context[0]}), the call result ({@code ctx+0x478}), the call id
 * ({@code ctx+0x564}, sixteen random bytes hex-encoded), the peer/creator/self JIDs
 * ({@code ctx+0x40}/{@code +0x44}/{@code +0x514}), the group marker ({@code ctx[0x28528]}), the link
 * sub-state ({@code ctx[0x28530]}), the accept-received flag ({@code ctx+0x118}), the media-started flag
 * ({@code ctx+0x170}), the companion-terminate flag ({@code ctx[0x11c]}), the active/lonely duration
 * accumulators ({@code ctx+0x12c} region), and the connected-lonely timeout defaults
 * ({@code ctx[0x28b84]}/{@code [0x28b85]}/{@code [0x28b89]}). The native struct additionally inlines the
 * eleven timer handles, the media and transport handles, and the participant slot array, which Cobalt
 * owns in the timer, media/transport, and {@link CallMembership} subsystems respectively; this aggregate
 * holds the lifecycle-owning state and the opaque resource and timer seams those subsystems plug into.
 */
public final class Calls2CallContext {
    /**
     * Logs context allocation and teardown, mirroring the engine's per-call lifecycle logging.
     */
    private static final System.Logger LOGGER = System.getLogger(Calls2CallContext.class.getName());

    /**
     * The shared call-id random source.
     *
     * <p>A single {@link SecureRandom} draws the sixteen call-id bytes for every context, replacing the
     * engine's once-seeded pseudo-random generator with a cryptographically strong source.
     */
    private static final SecureRandom CALL_ID_RANDOM = new SecureRandom();

    /**
     * The number of random bytes drawn for a call id.
     *
     * <p>The engine draws sixteen bytes and hex-encodes each into two characters.
     */
    public static final int CALL_ID_BYTE_LENGTH = 16;

    /**
     * The character length of a hex-encoded call id.
     *
     * <p>Each of the {@value #CALL_ID_BYTE_LENGTH} bytes encodes to two characters, giving a
     * {@code 0x20}-character identifier; the native string is null-terminated at offset {@code 0x20}.
     */
    public static final int CALL_ID_CHAR_LENGTH = CALL_ID_BYTE_LENGTH * 2;

    /**
     * The dual-case hex alphabet the engine uses to encode a call id.
     *
     * <p>The high nibble of each byte indexes the first sixteen (upper-case) characters and the low
     * nibble indexes the trailing sixteen (lower-case) characters, so a single byte's two hex digits
     * differ in case and the case pattern varies across the thirty-two characters.
     */
    private static final char[] CALL_ID_ALPHABET = "0123456789ABCDEF0123456789abcdef".toCharArray();

    /**
     * The default short connected-lonely interval, in milliseconds.
     *
     * <p>This is the engine's parsed default for the first connected-lonely interval; the state machine
     * picks it for the caller direction when entering the connected-lonely state.
     */
    public static final long CONNECTED_LONELY_DEFAULT_SHORT_MILLIS = 30_000L;

    /**
     * The default long connected-lonely interval, in milliseconds.
     *
     * <p>This is the engine's parsed default for the long connected-lonely interval, picked for the
     * callee direction when entering the connected-lonely state.
     */
    public static final long CONNECTED_LONELY_DEFAULT_LONG_MILLIS = 270_000L;

    /**
     * The default maximum connected-lonely interval, in milliseconds.
     *
     * <p>This is the engine's parsed default ceiling for the connected-lonely timer, the last interval
     * the timer steps through before the call ends on a lonely timeout.
     */
    public static final long CONNECTED_LONELY_DEFAULT_MAX_MILLIS = 300_000L;

    /**
     * The fixed call identifier, sixteen random bytes hex-encoded into thirty-two characters.
     */
    private final String callId;

    /**
     * The manager role this context occupies (primary or secondary).
     */
    private final Calls2CallRole role;

    /**
     * The direction of this call (outbound or inbound).
     */
    private final Calls2CallDirection direction;

    /**
     * The peer device JID, the {@code call_context} peer field.
     */
    private final Jid peer;

    /**
     * The creator device JID, the user who started the call with its device suffix.
     */
    private final Jid creator;

    /**
     * The local self JID for this call.
     */
    private final Jid self;

    /**
     * The chat the call belongs to: the peer JID for a one-to-one call or the group JID for a group call.
     */
    private final Jid chatJid;

    /**
     * Whether this is a group call.
     */
    private final boolean group;

    /**
     * Whether this call was placed or accepted with video enabled.
     */
    private final boolean video;

    /**
     * Whether the local user placed this call.
     */
    private final boolean outgoing;

    /**
     * The per-call lock, the engine's {@code call-info-mutex}.
     */
    private final ReentrantLock lock;

    /**
     * The membership set of this call.
     */
    private final CallMembership membership;

    /**
     * The connected-lonely timeout configuration the state machine reads on entering the lonely state.
     */
    private final ConnectedLonelyConfig connectedLonelyConfig;

    /**
     * The public projection of this call, the {@link Call} the application observes.
     */
    private final Call call;

    /**
     * The opaque per-call resources closed on teardown, in attachment order.
     */
    private final List<AutoCloseable> resources;

    /**
     * The current internal call state.
     */
    private Calls2CallState state;

    /**
     * The current link-join sub-state, meaningful only while {@link #state} is {@link Calls2CallState#LINK}.
     */
    private Calls2CallLinkState linkState;

    /**
     * The current call result, or {@code null} until a result is recorded.
     */
    private Calls2CallResult result;

    /**
     * Whether an accept has been received for this call.
     */
    private boolean acceptReceived;

    /**
     * Whether media and transport have been brought up, guarding re-accept.
     */
    private boolean mediaStarted;

    /**
     * Whether a companion device of the local account has terminated this call, so a later companion
     * terminate is ignored.
     */
    private boolean companionTerminated;

    /**
     * The accumulated time, in milliseconds, the call has spent in the active state across segments.
     */
    private long activeDurationMillis;

    /**
     * The accumulated time, in milliseconds, the call has spent in the connected-lonely state across
     * segments.
     */
    private long lonelyDurationMillis;

    /**
     * The wall-clock millisecond timestamp at which the current active segment began, or {@code -1} when
     * the call is not active.
     */
    private long activeSegmentStartMillis;

    /**
     * The wall-clock millisecond timestamp at which the current connected-lonely segment began, or
     * {@code -1} when the call is not in the connected-lonely state.
     */
    private long lonelySegmentStartMillis;

    /**
     * The seam invoked to schedule the connected-lonely timer on entering the lonely state, or
     * {@code null} when no scheduler is wired.
     */
    private Consumer<Calls2CallContext> scheduleConnectedLonelyTimer;

    /**
     * The seam invoked to cancel the connected-lonely timer on leaving the lonely state, or {@code null}
     * when no canceller is wired.
     */
    private Runnable cancelConnectedLonelyTimer;

    /**
     * Constructs a call context with a freshly generated call id and the given identity and topology.
     *
     * <p>The context starts in {@link Calls2CallState#NONE} with {@link Calls2CallLinkState#NONE}, no
     * recorded result, no accept received, no media started, zeroed duration accumulators, the default
     * {@linkplain ConnectedLonelyConfig#defaults() connected-lonely configuration}, and a fresh
     * {@link CallMembership}. The projected {@link Call} is created in the public phase the initial state
     * projects to. The lonely-timer seams are unset until {@link #onScheduleConnectedLonelyTimer(Consumer)}
     * and {@link #onCancelConnectedLonelyTimer(Runnable)} wire them.
     *
     * @param role     the manager role this context occupies
     * @param direction the direction of the call
     * @param peer     the peer device JID
     * @param creator  the creator device JID
     * @param self     the local self JID
     * @param chatJid  the chat the call belongs to
     * @param group    whether this is a group call
     * @param video    whether the call carries video
     * @throws NullPointerException if {@code role}, {@code direction}, {@code peer}, {@code creator},
     *                              {@code self}, or {@code chatJid} is {@code null}
     */
    public Calls2CallContext(Calls2CallRole role, Calls2CallDirection direction, Jid peer, Jid creator,
                             Jid self, Jid chatJid, boolean group, boolean video) {
        this(generateCallId(), role, direction, peer, creator, self, chatJid, group, video);
    }

    /**
     * Constructs a call context bound to a known call id and the given identity and topology.
     *
     * <p>This overload pins the call id rather than generating one, for an inbound call whose id the peer
     * assigned in its offer; the runtime fields start in the same initial state as the generating
     * constructor.
     *
     * @param callId    the fixed call identifier
     * @param role      the manager role this context occupies
     * @param direction the direction of the call
     * @param peer      the peer device JID
     * @param creator   the creator device JID
     * @param self      the local self JID
     * @param chatJid   the chat the call belongs to
     * @param group     whether this is a group call
     * @param video     whether the call carries video
     * @throws NullPointerException if any reference argument is {@code null}
     */
    public Calls2CallContext(String callId, Calls2CallRole role, Calls2CallDirection direction, Jid peer,
                             Jid creator, Jid self, Jid chatJid, boolean group, boolean video) {
        this.callId = Objects.requireNonNull(callId, "callId cannot be null");
        this.role = Objects.requireNonNull(role, "role cannot be null");
        this.direction = Objects.requireNonNull(direction, "direction cannot be null");
        this.peer = Objects.requireNonNull(peer, "peer cannot be null");
        this.creator = Objects.requireNonNull(creator, "creator cannot be null");
        this.self = Objects.requireNonNull(self, "self cannot be null");
        this.chatJid = Objects.requireNonNull(chatJid, "chatJid cannot be null");
        this.group = group;
        this.video = video;
        this.outgoing = direction == Calls2CallDirection.OUTGOING;
        this.lock = new ReentrantLock();
        this.membership = new CallMembership(callId);
        this.connectedLonelyConfig = ConnectedLonelyConfig.defaults();
        this.state = Calls2CallState.NONE;
        this.linkState = Calls2CallLinkState.NONE;
        this.result = null;
        this.acceptReceived = false;
        this.mediaStarted = false;
        this.companionTerminated = false;
        this.activeDurationMillis = 0L;
        this.lonelyDurationMillis = 0L;
        this.activeSegmentStartMillis = -1L;
        this.lonelySegmentStartMillis = -1L;
        this.resources = new ArrayList<>();
        this.call = new Call(callId, peer.toUserJid(), chatJid, creator, outgoing, group, video,
                state.toPublic());
    }

    /**
     * Generates a fresh call id: sixteen random bytes hex-encoded into thirty-two dual-case characters.
     *
     * <p>Each byte's high nibble selects an upper-case hex character and its low nibble selects a
     * lower-case hex character from the {@value #CALL_ID_CHAR_LENGTH}-character {@link #CALL_ID_ALPHABET},
     * so the case alternates within each byte and the case pattern varies across the identifier, exactly
     * as the engine renders it.
     *
     * @return a freshly generated {@value #CALL_ID_CHAR_LENGTH}-character call id
     * @implNote This implementation reproduces {@code generate_call_id} (fn10675 seed, fn10731 encode) of
     * the wa-voip WASM module {@code ff-tScznZ8P}: the encode hex-table is the dual-case alphabet
     * {@code 0123456789ABCDEF0123456789abcdef}, indexed by the high nibble then the low nibble of each of
     * the sixteen bytes to produce the {@code 0x40}-byte (thirty-two character plus terminator) string.
     * Where the engine seeds a pseudo-random generator with {@code (os_rand & 0x0fffffff) | 0xa0000000}
     * and draws the sixteen bytes from it, Cobalt draws the sixteen bytes from a {@link SecureRandom}.
     */
    public static String generateCallId() {
        var bytes = new byte[CALL_ID_BYTE_LENGTH];
        CALL_ID_RANDOM.nextBytes(bytes);
        var chars = new char[CALL_ID_CHAR_LENGTH];
        for (var i = 0; i < CALL_ID_BYTE_LENGTH; i++) {
            var value = bytes[i] & 0xff;
            chars[i * 2] = CALL_ID_ALPHABET[value >>> 4];
            chars[i * 2 + 1] = CALL_ID_ALPHABET[16 + (value & 0x0f)];
        }
        return new String(chars);
    }

    /**
     * Returns the fixed call identifier.
     *
     * @return the thirty-two character call id, never {@code null}
     */
    public String callId() {
        return callId;
    }

    /**
     * Returns the manager role this context occupies.
     *
     * @return the role, never {@code null}
     */
    public Calls2CallRole role() {
        return role;
    }

    /**
     * Returns the direction of this call.
     *
     * @return the direction, never {@code null}
     */
    public Calls2CallDirection direction() {
        return direction;
    }

    /**
     * Returns the peer device JID.
     *
     * @return the peer JID, never {@code null}
     */
    public Jid peer() {
        return peer;
    }

    /**
     * Returns the creator device JID.
     *
     * @return the creator JID, never {@code null}
     */
    public Jid creator() {
        return creator;
    }

    /**
     * Returns the local self JID for this call.
     *
     * @return the self JID, never {@code null}
     */
    public Jid self() {
        return self;
    }

    /**
     * Returns the chat the call belongs to.
     *
     * @return the chat JID, never {@code null}
     */
    public Jid chatJid() {
        return chatJid;
    }

    /**
     * Returns whether this is a group call.
     *
     * @return {@code true} for a group call
     */
    public boolean group() {
        return group;
    }

    /**
     * Returns whether this call carries video.
     *
     * @return {@code true} for a video call
     */
    public boolean video() {
        return video;
    }

    /**
     * Returns whether the local user placed this call.
     *
     * @return {@code true} for an outbound call
     */
    public boolean outgoing() {
        return outgoing;
    }

    /**
     * Returns this context's lock, the engine's per-call {@code call-info-mutex}.
     *
     * <p>Callers take this lock for the duration of any operation that reads or mutates the runtime
     * fields, so a state transition, a duration update, and a snapshot never interleave. The lock is
     * reentrant, so a caller already holding it may re-enter the context.
     *
     * @return the per-call lock, never {@code null}
     */
    public ReentrantLock lock() {
        return lock;
    }

    /**
     * Returns the membership set of this call.
     *
     * @return the membership manager, never {@code null}
     */
    public CallMembership membership() {
        return membership;
    }

    /**
     * Returns the connected-lonely timeout configuration.
     *
     * @return the connected-lonely configuration, never {@code null}
     */
    public ConnectedLonelyConfig connectedLonelyConfig() {
        return connectedLonelyConfig;
    }

    /**
     * Returns the public projection of this call.
     *
     * <p>The same {@link Call} instance is updated as this context's state, mute flags, and end reason
     * change, so the application observes the projection through it; it is the only call object the engine
     * exposes outside the membership and lifecycle layers.
     *
     * @return the projected call, never {@code null}
     */
    public Call call() {
        return call;
    }

    /**
     * Returns the current internal call state.
     *
     * @return the current state, never {@code null}
     */
    public Calls2CallState state() {
        return state;
    }

    /**
     * Sets the internal call state and republishes the projected public phase.
     *
     * <p>This setter writes the raw state field and projects it onto the {@link Call} model through
     * {@link Calls2CallState#toPublic()}; it performs no transition guarding or duration accounting, which
     * are the {@link Calls2CallStateMachine}'s responsibility. Callers other than the state machine should
     * drive transitions through {@link Calls2CallStateMachine#transition(Calls2CallContext, Calls2CallState)}
     * rather than this setter. The caller holds {@link #lock()}.
     *
     * @param state the new internal state
     * @throws NullPointerException if {@code state} is {@code null}
     */
    public void state(Calls2CallState state) {
        this.state = Objects.requireNonNull(state, "state cannot be null");
        call.setState(state.toPublic());
    }

    /**
     * Returns the current link-join sub-state.
     *
     * @return the link sub-state, never {@code null}
     */
    public Calls2CallLinkState linkState() {
        return linkState;
    }

    /**
     * Sets the link-join sub-state.
     *
     * <p>The sub-state is meaningful only while {@link #state()} is {@link Calls2CallState#LINK}; outside
     * that phase it stays {@link Calls2CallLinkState#NONE}. The caller holds {@link #lock()}.
     *
     * @param linkState the new link sub-state
     * @throws NullPointerException if {@code linkState} is {@code null}
     */
    public void linkState(Calls2CallLinkState linkState) {
        this.linkState = Objects.requireNonNull(linkState, "linkState cannot be null");
    }

    /**
     * Returns the current call result, if one has been recorded.
     *
     * <p>The result records why a call ended (or that it succeeded) and is tracked separately from the
     * call state, matching the engine's distinct {@code set_call_result} field; it is absent until a
     * result is recorded.
     *
     * @return an {@link Optional} holding the current result, or empty when none has been recorded
     */
    public Optional<Calls2CallResult> result() {
        return Optional.ofNullable(result);
    }

    /**
     * Records the call result and republishes the projected end reason.
     *
     * <p>This writes the result field, distinct from the state field, and projects it onto the
     * {@link Call} model through {@link Calls2CallResult#toEndReason()}. The caller holds {@link #lock()}.
     *
     * @param result the result to record
     * @throws NullPointerException if {@code result} is {@code null}
     */
    public void result(Calls2CallResult result) {
        this.result = Objects.requireNonNull(result, "result cannot be null");
        call.setEndReason(result.toEndReason());
    }

    /**
     * Returns whether an accept has been received for this call.
     *
     * @return {@code true} once an accept has been received
     */
    public boolean acceptReceived() {
        return acceptReceived;
    }

    /**
     * Sets whether an accept has been received for this call.
     *
     * @param acceptReceived whether an accept has been received
     */
    public void acceptReceived(boolean acceptReceived) {
        this.acceptReceived = acceptReceived;
    }

    /**
     * Returns whether media and transport have been brought up for this call.
     *
     * @return {@code true} once media and transport are up
     */
    public boolean mediaStarted() {
        return mediaStarted;
    }

    /**
     * Sets whether media and transport have been brought up for this call.
     *
     * <p>The engine sets this flag once accept handling brings up media and transport, and guards
     * re-accept on it so a duplicate accept does not restart the media plane.
     *
     * @param mediaStarted whether media and transport are up
     */
    public void mediaStarted(boolean mediaStarted) {
        this.mediaStarted = mediaStarted;
    }

    /**
     * Returns whether a companion device of the local account has terminated this call.
     *
     * @return {@code true} once a companion device has terminated the call
     */
    public boolean companionTerminated() {
        return companionTerminated;
    }

    /**
     * Sets whether a companion device of the local account has terminated this call.
     *
     * <p>The engine reads this flag in terminate handling to ignore a later companion-device terminate
     * once the call is already being torn down by one.
     *
     * @param companionTerminated whether a companion device has terminated the call
     */
    public void companionTerminated(boolean companionTerminated) {
        this.companionTerminated = companionTerminated;
    }

    /**
     * Returns the accumulated active duration, in milliseconds, across all active segments.
     *
     * <p>This does not include the open active segment, if any; it is the sum of completed active
     * segments closed by {@link #closeActiveSegment(long)}.
     *
     * @return the accumulated active duration in milliseconds, never negative
     */
    public long activeDurationMillis() {
        return activeDurationMillis;
    }

    /**
     * Returns the accumulated connected-lonely duration, in milliseconds, across all lonely segments.
     *
     * <p>This does not include the open lonely segment, if any; it is the sum of completed lonely
     * segments closed by {@link #closeLonelySegment(long)}.
     *
     * @return the accumulated lonely duration in milliseconds, never negative
     */
    public long lonelyDurationMillis() {
        return lonelyDurationMillis;
    }

    /**
     * Opens an active-duration segment at the given timestamp.
     *
     * <p>Called by the state machine on entering {@link Calls2CallState#CALL_ACTIVE} to capture the
     * segment start; the segment is later closed by {@link #closeActiveSegment(long)}, which adds the
     * elapsed time to {@link #activeDurationMillis()}. The caller holds {@link #lock()}.
     *
     * @param nowMillis the wall-clock millisecond timestamp at which the segment begins
     */
    public void openActiveSegment(long nowMillis) {
        this.activeSegmentStartMillis = nowMillis;
    }

    /**
     * Closes the open active-duration segment at the given timestamp, accumulating its elapsed time.
     *
     * <p>Called by the state machine on leaving {@link Calls2CallState#CALL_ACTIVE}: the elapsed time
     * since the segment was opened is added to {@link #activeDurationMillis()} and the segment is cleared.
     * A close with no open segment, or with a timestamp before the open timestamp, accumulates nothing.
     * The caller holds {@link #lock()}.
     *
     * @param nowMillis the wall-clock millisecond timestamp at which the segment ends
     * @return the elapsed milliseconds added to the accumulator, never negative
     */
    public long closeActiveSegment(long nowMillis) {
        if (activeSegmentStartMillis < 0) {
            return 0L;
        }
        var elapsed = Math.max(0L, nowMillis - activeSegmentStartMillis);
        activeDurationMillis += elapsed;
        activeSegmentStartMillis = -1L;
        return elapsed;
    }

    /**
     * Opens a connected-lonely-duration segment at the given timestamp.
     *
     * <p>Called by the state machine on entering {@link Calls2CallState#CONNECTED_LONELY} to capture the
     * segment start; the segment is later closed by {@link #closeLonelySegment(long)}. The caller holds
     * {@link #lock()}.
     *
     * @param nowMillis the wall-clock millisecond timestamp at which the segment begins
     */
    public void openLonelySegment(long nowMillis) {
        this.lonelySegmentStartMillis = nowMillis;
    }

    /**
     * Closes the open connected-lonely-duration segment at the given timestamp, accumulating its elapsed
     * time.
     *
     * <p>Called by the state machine on leaving {@link Calls2CallState#CONNECTED_LONELY}: the elapsed
     * time since the segment was opened is added to {@link #lonelyDurationMillis()} and the segment is
     * cleared. A close with no open segment, or with a timestamp before the open timestamp, accumulates
     * nothing. The caller holds {@link #lock()}.
     *
     * @param nowMillis the wall-clock millisecond timestamp at which the segment ends
     * @return the elapsed milliseconds added to the accumulator, never negative
     */
    public long closeLonelySegment(long nowMillis) {
        if (lonelySegmentStartMillis < 0) {
            return 0L;
        }
        var elapsed = Math.max(0L, nowMillis - lonelySegmentStartMillis);
        lonelyDurationMillis += elapsed;
        lonelySegmentStartMillis = -1L;
        return elapsed;
    }

    /**
     * Registers the seam that schedules the connected-lonely timer on entering the lonely state.
     *
     * <p>The state machine invokes this seam, passing this context, when a transition enters
     * {@link Calls2CallState#CONNECTED_LONELY}; the lifecycle controller wires it to the call's timer
     * subsystem. A second registration replaces the first; passing {@code null} clears the seam, after
     * which entering the lonely state schedules nothing.
     *
     * @param scheduler the scheduling seam, or {@code null} to clear it
     */
    public void onScheduleConnectedLonelyTimer(Consumer<Calls2CallContext> scheduler) {
        this.scheduleConnectedLonelyTimer = scheduler;
    }

    /**
     * Registers the seam that cancels the connected-lonely timer on leaving the lonely state.
     *
     * <p>The state machine invokes this seam when a transition leaves {@link Calls2CallState#CONNECTED_LONELY}
     * or enters {@link Calls2CallState#CALL_ACTIVE}; the lifecycle controller wires it to the call's timer
     * subsystem. A second registration replaces the first; passing {@code null} clears the seam, after
     * which leaving the lonely state cancels nothing.
     *
     * @param canceller the cancelling seam, or {@code null} to clear it
     */
    public void onCancelConnectedLonelyTimer(Runnable canceller) {
        this.cancelConnectedLonelyTimer = canceller;
    }

    /**
     * Invokes the connected-lonely scheduling seam, if one is wired.
     *
     * <p>Called by the state machine on entering the connected-lonely state. With no scheduler wired this
     * is a no-op, so the state machine can drive the transition before the timer subsystem is attached.
     */
    void fireScheduleConnectedLonelyTimer() {
        if (scheduleConnectedLonelyTimer != null) {
            scheduleConnectedLonelyTimer.accept(this);
        }
    }

    /**
     * Invokes the connected-lonely cancelling seam, if one is wired.
     *
     * <p>Called by the state machine on leaving the connected-lonely state or entering the active state.
     * With no canceller wired this is a no-op.
     */
    void fireCancelConnectedLonelyTimer() {
        if (cancelConnectedLonelyTimer != null) {
            cancelConnectedLonelyTimer.run();
        }
    }

    /**
     * Attaches a per-call resource to be closed when this context tears down.
     *
     * <p>The context closes attached resources in reverse attachment order during {@link #close()},
     * giving the lifecycle controller one place to register the media engine, the transport, the data
     * channel, and any other {@link AutoCloseable} the call owns without this aggregate depending on
     * their types. The caller holds {@link #lock()}.
     *
     * @param resource the resource to close on teardown
     * @throws NullPointerException if {@code resource} is {@code null}
     */
    public void attachResource(AutoCloseable resource) {
        Objects.requireNonNull(resource, "resource cannot be null");
        resources.add(resource);
    }

    /**
     * Closes all attached resources and clears the lonely-timer seams.
     *
     * <p>Resources are closed in reverse attachment order so a resource that depends on an earlier one is
     * torn down first; a resource that throws on close is logged and the teardown continues with the
     * remaining resources. After this returns the resource list is empty and the lonely-timer seams are
     * cleared. The caller holds {@link #lock()}.
     *
     * @implNote This implementation models the resource-release portion of the engine's
     * {@code call_lifecycle} end path (fn10714) reached from {@code call_manager_end_call} (fn10733): the
     * native path stops the timers and frees the media, transport, and participant sub-objects in
     * reverse-dependency order. The eleven timers are stopped by the timer subsystem; this aggregate
     * closes the opaque resource slots the lifecycle controller attached.
     */
    public void close() {
        for (var index = resources.size() - 1; index >= 0; index--) {
            var resource = resources.get(index);
            try {
                resource.close();
            } catch (Exception exception) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to close call resource for call " + callId, exception);
            }
        }
        resources.clear();
        scheduleConnectedLonelyTimer = null;
        cancelConnectedLonelyTimer = null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Calls2CallContext that && this.callId.equals(that.callId));
    }

    @Override
    public int hashCode() {
        return callId.hashCode();
    }

    @Override
    public String toString() {
        return "Calls2CallContext[callId=" + callId
                + ", role=" + role
                + ", direction=" + direction
                + ", state=" + state
                + ", group=" + group
                + ']';
    }

    /**
     * Enumerates the manager slot a call context occupies.
     *
     * <p>The {@link Calls2CallManager} holds at most a {@link #PRIMARY} call and one optional
     * {@link #SECONDARY} (dual) call; the role records which slot a context belongs to so teardown can end
     * the secondary before the primary and a dual-call promotion can move a context between slots.
     *
     * @implNote This implementation names the two native call-manager slots
     * ({@code call_manager_get_primary_call_context}, fn10729, at {@code manager+8}, and
     * {@code call_manager_get_secondary_call_context}, fn10730, at {@code manager+0xc}) of the wa-voip
     * WASM module {@code ff-tScznZ8P}; the engine's {@code role/dir} argument to {@code init_local_state}
     * is {@code 1/1} for the primary and {@code 2/2} for the secondary.
     */
    public enum Calls2CallRole {
        /**
         * The primary (first) call context, the engine's {@code manager+8} slot.
         */
        PRIMARY,

        /**
         * The optional secondary (dual) call context, the engine's {@code manager+0xc} slot.
         */
        SECONDARY
    }

    /**
     * Enumerates the direction of a call.
     *
     * <p>The direction selects which connected-lonely interval the state machine picks on entering the
     * lonely state (the short interval for the caller, the long interval for the callee) and is projected
     * into the {@link Call#isOutgoing()} flag.
     */
    public enum Calls2CallDirection {
        /**
         * An outbound call the local user placed.
         */
        OUTGOING,

        /**
         * An inbound call the local user received.
         */
        INCOMING
    }

    /**
     * Holds the connected-lonely timeout configuration of a call.
     *
     * <p>The state machine reads this configuration on entering {@link Calls2CallState#CONNECTED_LONELY}
     * to pick the interval by direction and schedule the connected-lonely timer. The three values are the
     * engine's parsed connected-lonely timeouts: the {@linkplain #shortMillis() short} interval for the
     * caller, the {@linkplain #longMillis() long} interval for the callee, and the
     * {@linkplain #maxMillis() maximum} ceiling the timer steps up to.
     *
     * @param shortMillis the short connected-lonely interval, in milliseconds
     * @param longMillis  the long connected-lonely interval, in milliseconds
     * @param maxMillis   the maximum connected-lonely interval, in milliseconds
     * @implNote This implementation models the connected-lonely timeouts {@code parse_lonely_state_timeouts}
     * parses into the {@code call_context} at {@code ctx[0x28b84]} ({@value #CONNECTED_LONELY_DEFAULT_LONG_MILLIS}),
     * {@code ctx[0x28b85]} ({@value #CONNECTED_LONELY_DEFAULT_MAX_MILLIS}), and {@code ctx[0x28b89]}
     * ({@value #CONNECTED_LONELY_DEFAULT_SHORT_MILLIS}) in the wa-voip WASM module {@code ff-tScznZ8P}.
     */
    public record ConnectedLonelyConfig(long shortMillis, long longMillis, long maxMillis) {
        /**
         * Returns the default connected-lonely configuration.
         *
         * <p>The defaults are the engine's parsed connected-lonely timeouts: a
         * {@value #CONNECTED_LONELY_DEFAULT_SHORT_MILLIS} ms short interval, a
         * {@value #CONNECTED_LONELY_DEFAULT_LONG_MILLIS} ms long interval, and a
         * {@value #CONNECTED_LONELY_DEFAULT_MAX_MILLIS} ms maximum.
         *
         * @return the default connected-lonely configuration, never {@code null}
         */
        public static ConnectedLonelyConfig defaults() {
            return new ConnectedLonelyConfig(CONNECTED_LONELY_DEFAULT_SHORT_MILLIS,
                    CONNECTED_LONELY_DEFAULT_LONG_MILLIS, CONNECTED_LONELY_DEFAULT_MAX_MILLIS);
        }

        /**
         * Returns the connected-lonely interval to use for the given direction.
         *
         * <p>The caller direction uses the {@linkplain #shortMillis() short} interval and the callee
         * direction uses the {@linkplain #longMillis() long} interval, mirroring the engine's
         * pick-by-direction on entering the lonely state.
         *
         * @param direction the call direction
         * @return the connected-lonely interval in milliseconds for the direction
         * @throws NullPointerException if {@code direction} is {@code null}
         */
        public long intervalForDirection(Calls2CallDirection direction) {
            Objects.requireNonNull(direction, "direction cannot be null");
            return direction == Calls2CallDirection.OUTGOING ? shortMillis : longMillis;
        }
    }
}
