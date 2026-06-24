package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Owns the at-most-two live call contexts of a client: one primary call and one optional secondary
 * (dual) call.
 *
 * <p>This is the call engine's {@code call_manager}: the single owner of every {@link Calls2CallContext}.
 * It holds two slots, a {@linkplain #primary() primary} call and an optional
 * {@linkplain #secondary() secondary} call, and the operations that fill and clear them:
 * {@link #startCall(Calls2CallContext.Calls2CallDirection, Jid, Jid, Jid, Jid, boolean, boolean)}
 * allocates the primary call,
 * {@link #startDualCall(Calls2CallContext.Calls2CallDirection, Jid, Jid, Jid, Jid, boolean, boolean)}
 * allocates the secondary call while the primary is busy, {@link #getByCallId(String)} resolves either
 * slot by its call id, and {@link #endCall(String)} ends a call by id, tearing down the secondary before
 * the primary so a dual call collapses back to its primary cleanly.
 *
 * <p>The manager has its own lock that serializes slot allocation and clearing, distinct from each
 * context's {@linkplain Calls2CallContext#lock() info-lock}: the manager lock guards which contexts
 * occupy the two slots, while a context lock guards that context's own runtime fields. The manager lock
 * is never held while a context lock is taken in the reverse order, so the two locks do not deadlock; a
 * teardown takes the manager lock to clear the slot and the context's lock (through
 * {@link Calls2CallContext#close()}) to release the call's resources, in that fixed order.
 *
 * <p>This is an internal engine collaborator; embedders never see it. One manager exists per client and
 * lives for the client's lifetime, holding no call when idle.
 *
 * @implNote This implementation reproduces the {@code call_manager} singleton (the {@code 0x18}-byte
 * structure at {@code DAT_ram_001508a4}, created by {@code call_manager_create}, fn10728) of the wa-voip
 * WASM module {@code ff-tScznZ8P}: the primary slot ({@code manager+8},
 * {@code call_manager_get_primary_call_context} fn10729), the optional secondary slot
 * ({@code manager+0xc}, {@code call_manager_get_secondary_call_context} fn10730), the by-id resolver that
 * checks the secondary then the primary ({@code call_manager_get_context_by_call_id} fn10731, matching the
 * {@code 0x40}-byte call id at {@code ctx+0x564}), the dual-call allocation
 * ({@code call_manager_start_dual_call_from_context}, the {@code role/dir 2/2} branch of
 * {@code wa_call_start_call} fn10719), and the secondary-first teardown ({@code call_manager_end_call}
 * fn10733, which ends {@code manager+0xc} before {@code manager+8}). The native {@code call-info-mutex}
 * the manager creates and hands out per context ({@code call_manager_get_call_info_mutex_for_context}
 * fn10732) is modeled by each {@link Calls2CallContext}'s own lock rather than by a manager-held mutex
 * array, since a Cobalt context carries its lock; the manager keeps a separate
 * {@linkplain #lock manager lock} for the slot-assignment serialization the native pool implies.
 */
public final class Calls2CallManager {
    /**
     * Logs slot allocation, dual-call starts, and teardowns, mirroring the engine manager's logging.
     */
    private static final System.Logger LOGGER = System.getLogger(Calls2CallManager.class.getName());

    /**
     * Serializes allocation and clearing of the two slots.
     *
     * <p>Held for the duration of each slot-changing operation so a start, a dual start, and a teardown
     * never race to assign or clear the slots; distinct from each context's info-lock.
     */
    private final ReentrantLock lock;

    /**
     * The seam invoked with a context after it is allocated into a slot, so the lifecycle controller can
     * wire its timers, transport, and signaling, or {@code null} when none is wired.
     */
    private Consumer<Calls2CallContext> onContextAllocated;

    /**
     * The primary call context, or {@code null} when there is no primary call.
     */
    private Calls2CallContext primary;

    /**
     * The secondary (dual) call context, or {@code null} when there is no dual call.
     */
    private Calls2CallContext secondary;

    /**
     * Constructs an idle call manager holding no call.
     *
     * <p>Both slots start empty; a call is allocated through
     * {@link #startCall(Calls2CallContext.Calls2CallDirection, Jid, Jid, Jid, Jid, boolean, boolean)} or
     * one of its overloads.
     */
    public Calls2CallManager() {
        this.lock = new ReentrantLock();
        this.onContextAllocated = null;
        this.primary = null;
        this.secondary = null;
    }

    /**
     * Registers the seam invoked with each context just after it is allocated into a slot.
     *
     * <p>The manager invokes this seam, still holding the manager lock, once a new context occupies the
     * primary or secondary slot, so the lifecycle controller can attach the context's timers, transport,
     * and signaling before any inbound signal can reach it. A second registration replaces the first;
     * passing {@code null} clears the seam.
     *
     * @param listener the allocation seam, or {@code null} to clear it
     */
    public void onContextAllocated(Consumer<Calls2CallContext> listener) {
        lock.lock();
        try {
            this.onContextAllocated = listener;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the primary call context, if there is a primary call.
     *
     * @return an {@link Optional} holding the primary context, or empty when there is no primary call
     */
    public Optional<Calls2CallContext> primary() {
        lock.lock();
        try {
            return Optional.ofNullable(primary);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the secondary (dual) call context, if there is a dual call.
     *
     * @return an {@link Optional} holding the secondary context, or empty when there is no dual call
     */
    public Optional<Calls2CallContext> secondary() {
        lock.lock();
        try {
            return Optional.ofNullable(secondary);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of live calls the manager currently holds.
     *
     * @return {@code 0} when idle, {@code 1} with only a primary call, or {@code 2} with a dual call
     */
    public int callCount() {
        lock.lock();
        try {
            return (primary == null ? 0 : 1) + (secondary == null ? 0 : 1);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns whether the manager holds a primary call, so a further call must be a dual call.
     *
     * @return {@code true} when a primary call is present
     */
    public boolean hasPrimary() {
        lock.lock();
        try {
            return primary != null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the call context with the given call id, checking the secondary slot before the primary.
     *
     * <p>The lookup matches the {@code call_id} the inbound signal carries against each occupied slot,
     * checking the secondary first and then the primary, matching the engine's by-id resolver; a call id
     * matching neither slot yields an empty result.
     *
     * @param callId the call id to resolve
     * @return an {@link Optional} holding the matching context, or empty when neither slot matches
     * @throws NullPointerException if {@code callId} is {@code null}
     */
    public Optional<Calls2CallContext> getByCallId(String callId) {
        Objects.requireNonNull(callId, "callId cannot be null");
        lock.lock();
        try {
            if (secondary != null && secondary.callId().equals(callId)) {
                return Optional.of(secondary);
            }
            if (primary != null && primary.callId().equals(callId)) {
                return Optional.of(primary);
            }
            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns whether a call context with the given call id occupies either slot.
     *
     * <p>This is the existence predicate the signaling layer consults to decide whether an inbound
     * payload is processed against a live call or buffered.
     *
     * @param callId the call id to test
     * @return {@code true} when a context with the call id occupies the primary or secondary slot
     * @throws NullPointerException if {@code callId} is {@code null}
     */
    public boolean contains(String callId) {
        return getByCallId(callId).isPresent();
    }

    /**
     * Allocates the primary call context for a new call and returns it.
     *
     * <p>A freshly generated call id is drawn for the call. The primary slot must be empty; an attempt to
     * start a primary call while one is already present is refused with an {@link IllegalStateException},
     * because a second concurrent call must go through
     * {@link #startDualCall(Calls2CallContext.Calls2CallDirection, Jid, Jid, Jid, Jid, boolean, boolean)}
     * instead. The allocated context is published to the {@linkplain #onContextAllocated(Consumer)
     * allocation seam} before this returns.
     *
     * @param direction the direction of the call
     * @param peer      the peer device JID
     * @param creator   the creator device JID
     * @param self      the local self JID
     * @param chatJid   the chat the call belongs to
     * @param group     whether this is a group call
     * @param video     whether the call carries video
     * @return the allocated primary call context
     * @throws NullPointerException  if any reference argument is {@code null}
     * @throws IllegalStateException if a primary call is already present
     */
    public Calls2CallContext startCall(Calls2CallContext.Calls2CallDirection direction,
                                       Jid peer, Jid creator, Jid self, Jid chatJid,
                                       boolean group, boolean video) {
        var context = new Calls2CallContext(Calls2CallContext.Calls2CallRole.PRIMARY, direction,
                peer, creator, self, chatJid, group, video);
        return adoptPrimary(context);
    }

    /**
     * Adopts an externally constructed context into the primary slot and returns it.
     *
     * <p>This overload accepts a context the caller already built, for an inbound call whose context is
     * created from the peer's offer (so it carries the peer-assigned call id rather than a generated one).
     * The context's {@linkplain Calls2CallContext#role() role} must be
     * {@link Calls2CallContext.Calls2CallRole#PRIMARY} and the primary slot must be empty.
     *
     * @param context the primary-role context to adopt
     * @return the adopted context
     * @throws NullPointerException     if {@code context} is {@code null}
     * @throws IllegalArgumentException if the context's role is not {@link Calls2CallContext.Calls2CallRole#PRIMARY}
     * @throws IllegalStateException    if a primary call is already present
     */
    public Calls2CallContext startCall(Calls2CallContext context) {
        Objects.requireNonNull(context, "context cannot be null");
        if (context.role() != Calls2CallContext.Calls2CallRole.PRIMARY) {
            throw new IllegalArgumentException("context role must be PRIMARY but was " + context.role());
        }
        return adoptPrimary(context);
    }

    /**
     * Allocates the secondary (dual) call context while the primary is busy and returns it.
     *
     * <p>A freshly generated call id is drawn for the dual call. A primary call must already be present
     * and the secondary slot must be empty; an attempt to start a dual call with no primary, or with a
     * secondary already present, is refused with an {@link IllegalStateException}. The allocated context
     * is published to the {@linkplain #onContextAllocated(Consumer) allocation seam} before this returns.
     *
     * @param direction the direction of the dual call
     * @param peer      the peer device JID
     * @param creator   the creator device JID
     * @param self      the local self JID
     * @param chatJid   the chat the dual call belongs to
     * @param group     whether the dual call is a group call
     * @param video     whether the dual call carries video
     * @return the allocated secondary call context
     * @throws NullPointerException  if any reference argument is {@code null}
     * @throws IllegalStateException if there is no primary call, or a secondary call is already present
     */
    public Calls2CallContext startDualCall(Calls2CallContext.Calls2CallDirection direction,
                                           Jid peer, Jid creator, Jid self, Jid chatJid,
                                           boolean group, boolean video) {
        var context = new Calls2CallContext(Calls2CallContext.Calls2CallRole.SECONDARY, direction,
                peer, creator, self, chatJid, group, video);
        return adoptSecondary(context);
    }

    /**
     * Adopts an externally constructed context into the secondary slot and returns it.
     *
     * <p>This overload accepts a context the caller already built, for an inbound dual call created from
     * a second peer's offer while the primary is busy. The context's {@linkplain Calls2CallContext#role()
     * role} must be {@link Calls2CallContext.Calls2CallRole#SECONDARY}, a primary call must be present,
     * and the secondary slot must be empty.
     *
     * @param context the secondary-role context to adopt
     * @return the adopted context
     * @throws NullPointerException     if {@code context} is {@code null}
     * @throws IllegalArgumentException if the context's role is not {@link Calls2CallContext.Calls2CallRole#SECONDARY}
     * @throws IllegalStateException    if there is no primary call, or a secondary call is already present
     */
    public Calls2CallContext startDualCall(Calls2CallContext context) {
        Objects.requireNonNull(context, "context cannot be null");
        if (context.role() != Calls2CallContext.Calls2CallRole.SECONDARY) {
            throw new IllegalArgumentException("context role must be SECONDARY but was " + context.role());
        }
        return adoptSecondary(context);
    }

    /**
     * Ends the call with the given call id, returning the context that was ended.
     *
     * <p>The call id is matched against the secondary slot first and then the primary, and the matching
     * context is removed from its slot and {@linkplain Calls2CallContext#close() closed}. When the call id
     * matches neither slot the call is reported as not found with an empty result, matching the engine's
     * {@code does not match active call id} path. Ending the primary while a secondary is present leaves
     * the secondary in place; a caller tearing down both ends the secondary first.
     *
     * <p>The slot is cleared under the manager lock and the context is closed afterward, still under the
     * lock, so no inbound signal can resolve the just-ended call id while teardown runs.
     *
     * @param callId the call id of the call to end
     * @return an {@link Optional} holding the ended context, or empty when neither slot matched
     * @throws NullPointerException if {@code callId} is {@code null}
     */
    public Optional<Calls2CallContext> endCall(String callId) {
        Objects.requireNonNull(callId, "callId cannot be null");
        lock.lock();
        try {
            if (secondary != null && secondary.callId().equals(callId)) {
                var ended = secondary;
                secondary = null;
                closeQuietly(ended);
                LOGGER.log(System.Logger.Level.DEBUG, "Ended secondary call {0}", callId);
                return Optional.of(ended);
            }
            if (primary != null && primary.callId().equals(callId)) {
                var ended = primary;
                primary = null;
                closeQuietly(ended);
                LOGGER.log(System.Logger.Level.DEBUG, "Ended primary call {0}", callId);
                return Optional.of(ended);
            }
            LOGGER.log(System.Logger.Level.DEBUG, "End requested for {0} does not match active call id", callId);
            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Ends every live call, tearing down the secondary before the primary.
     *
     * <p>This is the manager-wide teardown the client runs on disconnect: it ends the secondary call (if
     * any) and then the primary call (if any), each {@linkplain Calls2CallContext#close() closed} after
     * its slot is cleared. After this returns both slots are empty.
     */
    public void endAll() {
        lock.lock();
        try {
            if (secondary != null) {
                var ended = secondary;
                secondary = null;
                closeQuietly(ended);
            }
            if (primary != null) {
                var ended = primary;
                primary = null;
                closeQuietly(ended);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adopts a context into the primary slot under the manager lock, refusing a duplicate primary.
     *
     * @param context the primary-role context to adopt
     * @return the adopted context
     * @throws IllegalStateException if a primary call is already present
     */
    private Calls2CallContext adoptPrimary(Calls2CallContext context) {
        lock.lock();
        try {
            if (primary != null) {
                throw new IllegalStateException(
                        "cannot start a primary call while call " + primary.callId() + " is active");
            }
            primary = context;
            LOGGER.log(System.Logger.Level.DEBUG, "Allocated primary call {0}", context.callId());
            notifyAllocated(context);
            return context;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adopts a context into the secondary slot under the manager lock, refusing it when there is no
     * primary or a secondary already exists.
     *
     * @param context the secondary-role context to adopt
     * @return the adopted context
     * @throws IllegalStateException if there is no primary call, or a secondary call is already present
     */
    private Calls2CallContext adoptSecondary(Calls2CallContext context) {
        lock.lock();
        try {
            if (primary == null) {
                throw new IllegalStateException("cannot start a dual call with no primary call");
            }
            if (secondary != null) {
                throw new IllegalStateException(
                        "cannot start a dual call while secondary call " + secondary.callId() + " is active");
            }
            secondary = context;
            LOGGER.log(System.Logger.Level.DEBUG, "Allocated secondary call {0}", context.callId());
            notifyAllocated(context);
            return context;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Publishes a newly allocated context to the allocation seam, if one is wired.
     *
     * <p>Invoked under the manager lock so the seam runs before any other slot operation can observe the
     * new context; with no seam wired this is a no-op.
     *
     * @param context the freshly allocated context
     */
    private void notifyAllocated(Calls2CallContext context) {
        if (onContextAllocated != null) {
            onContextAllocated.accept(context);
        }
    }

    /**
     * Closes a context, logging and swallowing any failure so a teardown of one slot does not leave the
     * other slot in an inconsistent state.
     *
     * @param context the context to close
     */
    private void closeQuietly(Calls2CallContext context) {
        context.lock().lock();
        try {
            context.close();
        } catch (RuntimeException exception) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to close call context " + context.callId(), exception);
        } finally {
            context.lock().unlock();
        }
    }
}
