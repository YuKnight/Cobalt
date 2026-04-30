package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionMessageRange;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.chat.LockChatAction;
import com.github.auties00.cobalt.model.sync.action.chat.LockChatActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

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
@WhatsAppWebModule(moduleName = "WAWebLockChatSync")
public final class LockChatHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the lock chat handler.
     *
     * <p>Per WhatsApp Web {@code WAWebLockChatSync}, the module exports a single
     * instance via {@code var p = new m(); l.default = p}.
     *
     * @implNote WAWebLockChatSync.default — module-level singleton
     */
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return LockChatAction.ACTION_NAME;
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
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return LockChatAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for lock chat actions.
     *
     * @implNote WAWebLockChatSync.getVersion — returns {@code 7}
     * @return the version number {@code 7}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return LockChatAction.ACTION_VERSION;
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
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
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
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof LockChatAction action)) {
            return malformedActionValue();
        }

        var chatJidString = JSON.parseArray(mutation.index()).getString(1);
        if (chatJidString == null || chatJidString.isEmpty()) {
            return malformedActionIndex();
        }

        Jid chatJid;
        try {
            chatJid = Jid.of(chatJidString);
        } catch (Exception e) { // ADAPTED: WAWebWid.isWid would reject before reaching createWid; Cobalt catches parse failures
            return malformedActionIndex();
        }

        var chat = client.store().findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return MutationApplicationResult.orphan(chatJidString, "Chat");
        }

        // Post-loop, WA Web iterates i and calls setChatAsLocked / setChatAsUnlocked with syncWithPrimaries: false.
        // Both collapse to WAWebChatLockAction.e() which writes
        //   {isLocked: true, archive: false, pin: undefined} when locking
        //   {isLocked: false}                                when unlocking
        // Cobalt inlines this write directly on the in-memory Chat model.
        chat.get().setLocked(action.locked());
        if (action.locked()) {
            chat.get().setArchived(false);
            chat.get().setPinnedTimestamp(null);
        }
        return MutationApplicationResult.success();
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
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "getChatLockMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getChatLockMutation(Instant timestamp, boolean locked, Jid chatJid) {
        var action = new LockChatActionBuilder()
                .locked(locked)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .lockChatAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString()));
        var trusted = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
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
    @WhatsAppWebExport(moduleName = "WAWebLockChatSync", exports = "sendLockMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public List<SyncPendingMutation> getMutationsForLock(
            Instant timestamp,
            boolean locked,
            Jid chatJid,
            SyncActionMessageRange messageRange
    ) {
        var mutations = new ArrayList<SyncPendingMutation>();
        if (locked) {
            mutations.add(ArchiveChatHandler.INSTANCE.getArchiveChatMutation(timestamp, false, chatJid, messageRange));
            mutations.add(PinChatHandler.INSTANCE.getPinMutation(timestamp, false, chatJid));
        }
        mutations.add(getChatLockMutation(timestamp, locked, chatJid));
        return mutations; // ADAPTED: WA Web yields Promise.all(l) and hands the result to lockForSync; Cobalt returns the list so the caller controls the commit step
    }
}
