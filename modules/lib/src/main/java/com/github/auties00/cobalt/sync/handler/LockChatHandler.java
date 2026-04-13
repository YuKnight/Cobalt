package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.chat.LockChatAction;
import com.github.auties00.cobalt.model.sync.action.chat.LockChatActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles lock chat sync actions.
 *
 * <p>This handler processes incoming mutations that lock or unlock individual
 * chats. When a chat is locked it is also forcibly unarchived and unpinned so
 * that the three sticky-chat states ({@code archive}, {@code pin}, {@code lock})
 * remain mutually consistent.
 *
 * <p>The action is identified by the {@code "lock"} action name in
 * {@code SyncActionValue.lockChatAction}. The mutation index format is
 * {@code ["lock", chatJid]}.
 *
 * <p>Per WhatsApp Web {@code WAWebLockChatSync}, the handler extends
 * {@code ChatSyncdActionBase}. In Cobalt this inheritance is flattened: the
 * handler directly implements {@link WebAppStateActionHandler}, the chat JID
 * is extracted from the JSON-encoded index array, and the lock state is
 * applied directly to the in-memory chat model instead of going through a
 * dedicated chat table update layer.
 *
 * @implNote WAWebLockChatSync — singleton instance exported as {@code default}
 */
public final class LockChatHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the lock chat handler.
     *
     * <p>Per WhatsApp Web {@code WAWebLockChatSync}, the module exports a single
     * instance via {@code var p = new m(); l.default = p}.
     *
     * @implNote WAWebLockChatSync.default — module-level singleton
     */
    public static final LockChatHandler INSTANCE = new LockChatHandler();

    /**
     * Private constructor to enforce the singleton pattern.
     *
     * <p>Per WhatsApp Web, class {@code m} (extending {@code ChatSyncdActionBase})
     * has no custom constructor logic beyond initializing {@code chatJidIndex = 1}
     * and {@code collectionName = RegularLow}. Both values are represented in
     * Cobalt through the {@link #collectionName()} accessor and the fixed
     * {@code indexParts[1]} lookup used in {@link #applyMutationResult}.
     *
     * @implNote WAWebLockChatSync — class {@code m} constructor (no-op beyond base init)
     */
    private LockChatHandler() {

    }

    /**
     * Returns the action name for lock chat actions.
     *
     * @implNote WAWebLockChatSync.getAction — returns
     *           {@code WASyncdConst.Actions.LockChat} ({@code "lock"})
     * @return the action name {@code "lock"}
     */
    @Override
    public String actionName() {
        return LockChatAction.ACTION_NAME; // WAWebLockChatSync.getAction -> WASyncdConst.Actions.LockChat
    }

    /**
     * Returns the sync collection for lock chat actions.
     *
     * <p>Per WhatsApp Web, the lock handler's {@code collectionName} is set to
     * {@code WASyncdConst.CollectionName.RegularLow} in the constructor.
     *
     * @implNote WAWebLockChatSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return LockChatAction.COLLECTION_NAME; // WAWebLockChatSync.collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for lock chat actions.
     *
     * @implNote WAWebLockChatSync.getVersion — returns {@code 7}
     * @return the version number {@code 7}
     */
    @Override
    public int version() {
        return LockChatAction.ACTION_VERSION; // WAWebLockChatSync.getVersion -> 7
    }

    /**
     * Applies a lock chat mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link SyncActionState#SUCCESS}.
     *
     * @implNote WAWebLockChatSync.applyMutations — per-mutation inner logic,
     *           success check against the returned {@code syncApplyActionResult}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebLockChatSync.applyMutations
    }

    /**
     * Applies a lock chat mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebLockChatSync.applyMutations}, for each
     * mutation:
     * <ol>
     *   <li>If the operation is not {@code SET}, increments the unsupported
     *       counter and returns {@code Unsupported}.</li>
     *   <li>Extracts {@code chatJid = indexParts[1]} and
     *       {@code locked = value.lockChatAction?.locked}.</li>
     *   <li>If {@code locked == null}, increments the malformed-value counter
     *       and returns {@code malformedActionValue(collectionName)}.</li>
     *   <li>If the chat JID is not a valid WID, increments the malformed-index
     *       counter and returns {@code malformedActionIndex()}.</li>
     *   <li>Resolves the chat via
     *       {@code WAWebSyncdGetChat.resolveChatForMutationIndex}; if unsuccessful,
     *       returns {@code {actionState: Orphan, orphanModel: u.orphanModel}}.</li>
     *   <li>Queues an application record {@code {isLocked, chatId}} and returns
     *       {@code {actionState: Success}}.</li>
     * </ol>
     *
     * <p>After collecting the success list, WA Web iterates it and calls
     * {@code WAWebChatLockAction.setChatAsLocked(chatId, {syncWithPrimaries: false})}
     * or {@code WAWebChatLockAction.setChatAsUnlocked(chatId, {syncWithPrimaries: false})}.
     * With {@code syncWithPrimaries = false}, both routes collapse to the
     * internal chat-table updater that writes
     * {@code {isLocked: true, archive: false, pin: undefined}} when locking and
     * {@code {isLocked: false}} when unlocking. Cobalt inlines this final
     * write step directly on the in-memory {@code Chat} model because the
     * {@code ChatCollection}/{@code updateChatTable} indirection is not used.
     *
     * <p>Cobalt's {@link LockChatAction#locked()} accessor null-coalesces to
     * {@code false}, so the WA Web {@code locked == null} check is not
     * reachable: a missing {@code locked} field is treated as an unlock
     * request, consistent with how Cobalt handles other nullable boolean
     * sync fields. See {@code ArchiveChatAction.archived()} for the matching
     * pattern.
     *
     * @implNote WAWebLockChatSync.applyMutations — per-mutation inner function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebLockChatSync.applyMutations: if (e.operation !== "set") return {actionState: Unsupported}
            return MutationApplicationResult.unsupported(); // WAWebLockChatSync.applyMutations: l++, {actionState: SyncActionState.Unsupported}
        }

        if (!(mutation.value().action().orElse(null) instanceof LockChatAction action)) { // WAWebLockChatSync.applyMutations: var s = e.value.lockChatAction?.locked; if (s == null) return malformedActionValue
            return malformedActionValue(); // WAWebLockChatSync.applyMutations: p++, malformedActionValue(a.collectionName)
        }

        var chatJidString = JSON.parseArray(mutation.index()).getString(1); // WAWebLockChatSync.applyMutations: var n = e.indexParts[1]
        if (chatJidString == null || chatJidString.isEmpty()) { // WAWebLockChatSync.applyMutations: if (!isWid(n)) return this.malformedActionIndex()
            return malformedActionIndex(); // WAWebLockChatSync.applyMutations: _++, a.malformedActionIndex()
        }

        Jid chatJid;
        try {
            chatJid = Jid.of(chatJidString); // WAWebLockChatSync.applyMutations: createWid(n)
        } catch (Exception e) { // ADAPTED: WAWebWid.isWid would reject before reaching createWid; Cobalt catches parse failures
            return malformedActionIndex(); // WAWebLockChatSync.applyMutations: a.malformedActionIndex()
        }

        var chat = client.store().findChatByJid(chatJid); // WAWebLockChatSync.applyMutations: yield resolveChatForMutationIndex(createWid(n))
        if (chat.isEmpty()) { // WAWebLockChatSync.applyMutations: if (!u.success) return {actionState: Orphan, orphanModel: u.orphanModel}
            return MutationApplicationResult.orphan(chatJidString, "Chat"); // WAWebLockChatSync.applyMutations: SyncActionState.Orphan with orphan model
        }

        // WAWebLockChatSync.applyMutations: i.push({isLocked: s, chatId: ...}); return {actionState: Success}
        // Post-loop, WA Web iterates i and calls setChatAsLocked / setChatAsUnlocked with syncWithPrimaries: false.
        // Both collapse to WAWebChatLockAction.e() which writes
        //   {isLocked: true, archive: false, pin: undefined} when locking
        //   {isLocked: false}                                when unlocking
        // Cobalt inlines this write directly on the in-memory Chat model.
        chat.get().setLocked(action.locked()); // WAWebChatLockAction.e: {isLocked: t} / {isLocked: t, archive: !1, pin: void 0}
        if (action.locked()) { // WAWebChatLockAction.e: t ? {isLocked: t, archive: !1, pin: void 0} : {isLocked: t}
            chat.get().setArchived(false); // WAWebChatLockAction.e: archive: !1
            chat.get().setPinnedTimestamp(null); // WAWebChatLockAction.e: pin: void 0
        }
        return MutationApplicationResult.success(); // WAWebLockChatSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Builds a pending mutation that locks or unlocks a chat.
     *
     * <p>Per WhatsApp Web {@code WAWebLockChatSync.getChatLockMutation}:
     * <pre>{@code
     * getChatLockMutation(timestamp, locked, chatJid) {
     *   return buildPendingMutation({
     *     collection: this.collectionName,
     *     indexArgs: [yield getChatJidMutationIndexForChat(chatJid, Actions.LockChat)],
     *     value: {lockChatAction: {locked}},
     *     version: this.getVersion(),
     *     operation: SyncdMutation$SyncdOperation.SET,
     *     timestamp,
     *     action: this.getAction()
     *   });
     * }
     * }</pre>
     *
     * <p>The {@code indexArgs} list resolves the chat JID through
     * {@code getChatJidMutationIndexForChat}, which in WA Web swaps a PN for
     * its paired LID when LID1x1 migration is active. Cobalt does not yet
     * track the outgoing-mutation LID/PN swap at this layer, so the supplied
     * {@code chatJid} is used verbatim. Callers that need LID-aware indexing
     * should resolve the index JID before invoking this method.
     *
     * @implNote WAWebLockChatSync.getChatLockMutation, WAWebSyncdActionUtils.buildPendingMutation
     * @param timestamp the mutation timestamp
     * @param locked    whether the chat should be locked
     * @param chatJid   the JID of the chat to lock or unlock
     * @return the pending mutation for the lock action
     */
    public SyncPendingMutation getChatLockMutation(Instant timestamp, boolean locked, Jid chatJid) {
        var action = new LockChatActionBuilder() // WAWebLockChatSync.getChatLockMutation: value: {lockChatAction: {locked: t}}
                .locked(locked) // WAWebLockChatSync.getChatLockMutation: locked: t
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: i (encoder overlay)
                .lockChatAction(action) // WAWebLockChatSync.getChatLockMutation: lockChatAction from value
                .build();
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString())); // WAWebSyncdActionUtils.buildIndex: JSON.stringify([action].concat(indexArgs)); indexArgs = [getChatJidMutationIndexForChat(chatJid, Actions.LockChat)]
        var trusted = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index,
                value,
                SyncdOperation.SET, // WAWebLockChatSync.getChatLockMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp, // WAWebLockChatSync.getChatLockMutation: timestamp: e
                version() // WAWebLockChatSync.getChatLockMutation: version: this.getVersion()
        );
        return new SyncPendingMutation(trusted, 0); // ADAPTED: WA Web returns the raw mutation object; Cobalt wraps it in SyncPendingMutation for the outgoing queue
    }

    /**
     * Builds the set of pending mutations needed to lock or unlock a chat.
     *
     * <p>Per WhatsApp Web {@code WAWebLockChatSync.sendLockMutation}:
     * <pre>{@code
     * sendLockMutation(chatJid, {isLocked}) {
     *   var i = unixTimeMs();
     *   var l = [];
     *   if (isLocked) {
     *     l.push(WAWebArchiveChatSync.getArchiveChatMutation(i, false, chatJid),
     *            PinChatSync.getPinMutation(i, false, chatJid));
     *   }
     *   l.push(this.getChatLockMutation(i, isLocked, chatJid));
     *   yield WAWebSyncdCoreApi.lockForSync([], yield Promise.all(l), () => Promise.resolve());
     * }
     * }</pre>
     *
     * <p>In WA Web the three mutations are built concurrently and committed in
     * a single {@code lockForSync} transaction. Cobalt splits the "build the
     * mutation set" step from the "commit to the pending queue" step so that
     * callers (for example the outgoing send pipeline) can batch multiple
     * side effects before flushing.
     *
     * <p>The archive and pin mutations are only appended when locking. The
     * archive unset requires a {@code messageRange} which must be supplied by
     * the caller because Cobalt does not yet maintain active message ranges
     * (a browser-specific IndexedDB concern). When {@code messageRange} is
     * {@code null} the archive mutation is built with an absent range, which
     * matches how other callers of {@link ArchiveChatHandler#getArchiveChatMutation}
     * behave when the range is unavailable.
     *
     * <p>The pin mutation is intentionally omitted until
     * {@code PinChatHandler.getPinMutation} is exposed; the archive mutation
     * alone is sufficient to break the sticky-state invariant between archive
     * and lock. This mirrors the comment left in
     * {@link ArchiveChatHandler#getMutationsForArchive}.
     *
     * @implNote WAWebLockChatSync.sendLockMutation — mutation assembly only;
     *           {@code lockForSync} commit is handled by the outgoing sync
     *           pipeline in Cobalt, not inline in this handler
     * @param timestamp    the mutation timestamp
     * @param locked       whether the chat should be locked
     * @param chatJid      the JID of the chat to lock or unlock
     * @param messageRange the message range for the paired archive mutation,
     *                     ignored when {@code locked} is {@code false}; may be
     *                     {@code null} when unavailable
     * @return the list of pending mutations for the lock operation, in the
     *         order WA Web would submit them
     */
    public List<SyncPendingMutation> getMutationsForLock(
            Instant timestamp,
            boolean locked,
            Jid chatJid,
            com.github.auties00.cobalt.model.sync.SyncActionMessageRange messageRange
    ) {
        var mutations = new ArrayList<SyncPendingMutation>(); // WAWebLockChatSync.sendLockMutation: var l = []
        if (locked) { // WAWebLockChatSync.sendLockMutation: if (a) l.push(...)
            mutations.add(ArchiveChatHandler.INSTANCE.getArchiveChatMutation(timestamp, false, chatJid, messageRange)); // WAWebLockChatSync.sendLockMutation: WAWebArchiveChatSync.getArchiveChatMutation(i, false, e)
            // WAWebLockChatSync.sendLockMutation: PinChatSync.getPinMutation(i, false, e)
            // PinChatHandler does not yet expose a public getPinMutation method; callers that need
            // to break the sticky-state invariant between pin and lock should queue a pin-off
            // mutation separately once that helper becomes available.
        }
        mutations.add(getChatLockMutation(timestamp, locked, chatJid)); // WAWebLockChatSync.sendLockMutation: l.push(this.getChatLockMutation(i, a, e))
        return mutations; // ADAPTED: WA Web yields Promise.all(l) and hands the result to lockForSync; Cobalt returns the list so the caller controls the commit step
    }
}
