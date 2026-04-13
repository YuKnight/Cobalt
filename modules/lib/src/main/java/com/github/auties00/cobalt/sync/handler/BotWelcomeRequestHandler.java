package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.bot.BotWelcomeRequestAction;
import com.github.auties00.cobalt.model.sync.action.bot.BotWelcomeRequestActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * Handles bot welcome request sync actions.
 *
 * <p>This handler processes incoming mutations that track whether a bot welcome
 * message has been requested for a given chat. The action is identified by the
 * {@code "bot_welcome_request"} action name in
 * {@code SyncActionValue.botWelcomeRequestAction}. The mutation index format is
 * {@code ["bot_welcome_request", chatJid]}.
 *
 * <p>Per WhatsApp Web, this handler extends {@code ChatSyncdActionBase}, which
 * provides shared chat-based mutation processing. The handler's
 * {@code chatJidIndex} is {@code 1}, {@code collectionName} is
 * {@code RegularLow}, and {@code getVersion()} returns {@code 2}.
 *
 * @implNote WAWebBotWelcomeRequestSync — singleton instance exported as {@code default}
 */
public final class BotWelcomeRequestHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the bot welcome request handler.
     *
     * <p>Per WhatsApp Web, {@code WAWebBotWelcomeRequestSync} exports a single
     * instance ({@code var d = new c(); l.default = d}).
     *
     * @implNote WAWebBotWelcomeRequestSync.default — module-level singleton
     */
    public static final BotWelcomeRequestHandler INSTANCE = new BotWelcomeRequestHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebBotWelcomeRequestSync — class {@code c} constructor
     *           ({@code chatJidIndex = 1, collectionName = RegularLow})
     */
    private BotWelcomeRequestHandler() {

    }

    /**
     * Returns the action name for bot welcome request actions.
     *
     * @implNote WAWebBotWelcomeRequestSync.getAction — returns
     *           {@code WASyncdConst.Actions.BotWelcomeRequest} ({@code "bot_welcome_request"})
     * @return the action name {@code "bot_welcome_request"}
     */
    @Override
    public String actionName() {
        return BotWelcomeRequestAction.ACTION_NAME; // WAWebBotWelcomeRequestSync.getAction -> WASyncdConst.Actions.BotWelcomeRequest
    }

    /**
     * Returns the sync collection for bot welcome request actions.
     *
     * <p>Per WhatsApp Web, the bot welcome request handler's {@code collectionName}
     * is set to {@code WASyncdConst.CollectionName.RegularLow} in the constructor.
     *
     * @implNote WAWebBotWelcomeRequestSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return BotWelcomeRequestAction.COLLECTION_NAME; // WAWebBotWelcomeRequestSync.collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for bot welcome request actions.
     *
     * @implNote WAWebBotWelcomeRequestSync.getVersion — returns {@code 2}
     * @return the version number {@code 2}
     */
    @Override
    public int version() {
        return BotWelcomeRequestAction.ACTION_VERSION; // WAWebBotWelcomeRequestSync.getVersion -> 2
    }

    /**
     * Applies a bot welcome request mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link com.github.auties00.cobalt.model.sync.SyncActionState#SUCCESS}.
     *
     * @implNote WAWebBotWelcomeRequestSync.applyMutations — per-mutation inner logic,
     *           success check on the returned {@code SyncActionState}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // WAWebBotWelcomeRequestSync.applyMutations
    }

    /**
     * Applies a bot welcome request mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebBotWelcomeRequestSync.applyMutations}, for each
     * mutation with {@code operation === "set"}:
     * <ol>
     *   <li>Extracts the chat JID from {@code indexParts[1]}</li>
     *   <li>Validates the JID is present; returns {@code malformedActionIndex()} if missing</li>
     *   <li>Extracts {@code botWelcomeRequestAction.isSent}; returns
     *       {@code malformedActionValue()} if {@code null}</li>
     *   <li>Resolves the chat via {@code resolveChatForMutationIndex(createWid(u))}</li>
     *   <li>Updates the chat table with {@code hasRequestedWelcomeMsg: isSent}</li>
     *   <li>Fires a frontend {@code chatCollectionUpdate} event</li>
     * </ol>
     *
     * <p>{@code "remove"} operations return {@code Unsupported}. Any other operation
     * triggers an error (caught by the surrounding try/catch, returning {@code Failed}).
     *
     * @implNote WAWebBotWelcomeRequestSync.applyMutations — per-mutation inner function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // WAWebBotWelcomeRequestSync.applyMutations: match on operation
        if (mutation.operation() == SyncdOperation.REMOVE) { // WAWebBotWelcomeRequestSync.applyMutations: e.operation === "remove" -> i++, {actionState: Unsupported}
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) { // WAWebBotWelcomeRequestSync.applyMutations: throw Error("Match: No case...")
            return MutationApplicationResult.failed(); // ADAPTED: WA Web throws inside try/catch, caught as Failed
        }

        try { // WAWebBotWelcomeRequestSync.applyMutations: try/catch wrapping per-mutation logic
            var indexArray = JSON.parseArray(mutation.index()); // WAWebBotWelcomeRequestSync.applyMutations: var l = t.indexParts
            var chatJidString = indexArray.getString(1); // WAWebBotWelcomeRequestSync.applyMutations: var u = l[1]
            if (chatJidString == null || chatJidString.isEmpty()) { // WAWebBotWelcomeRequestSync.applyMutations: if (!u) return this.malformedActionIndex()
                return malformedActionIndex(); // WAWebBotWelcomeRequestSync.applyMutations: return r.malformedActionIndex()
            }

            if (!(mutation.value().action().orElse(null) instanceof BotWelcomeRequestAction action)) { // WAWebBotWelcomeRequestSync.applyMutations: var n = s.botWelcomeRequestAction
                return malformedActionValue(); // WAWebBotWelcomeRequestSync.applyMutations: malformedActionValue(r.collectionName)
            }

            // ADAPTED: WAWebBotWelcomeRequestSync.applyMutations: var c = n?.isSent; if (c == null) return malformedActionValue
            // WA Web checks if isSent is null and returns malformedActionValue. In Cobalt,
            // BotWelcomeRequestAction.isSent() coalesces null to false per project convention
            // (nullable Boolean accessors return primitive boolean). The raw Boolean field is
            // package-private and inaccessible from this package. See Issues in Context Files.

            var chatJid = Jid.of(chatJidString); // WAWebBotWelcomeRequestSync.applyMutations: createWid(u)
            var chat = client.store().findChatByJid(chatJid); // WAWebBotWelcomeRequestSync.applyMutations: yield resolveChatForMutationIndex(createWid(u))
            if (chat.isEmpty()) { // WAWebBotWelcomeRequestSync.applyMutations: if (!d.success) return {actionState: Orphan, orphanModel: d.orphanModel}
                return MutationApplicationResult.orphan(chatJidString, "Chat"); // WAWebBotWelcomeRequestSync.applyMutations: {actionState: Orphan, orphanModel: d.orphanModel}
            }

            // WAWebBotWelcomeRequestSync.applyMutations: var m = createWid(d.chat.id)
            // WAWebBotWelcomeRequestSync.applyMutations: yield updateChatTable(m, {hasRequestedWelcomeMsg: c})
            // ADAPTED: Cobalt stores hasRequestedWelcomeMsg in a separate map rather than on the chat record
            var resolvedJid = chat.get().toJid().toString(); // WAWebBotWelcomeRequestSync.applyMutations: createWid(d.chat.id)
            var states = new HashMap<>(client.store().botWelcomeRequestStates()); // ADAPTED: Cobalt uses a separate map for bot welcome request states
            states.put(resolvedJid, action.isSent()); // WAWebBotWelcomeRequestSync.applyMutations: {hasRequestedWelcomeMsg: c}
            client.store().setBotWelcomeRequestStates(states); // WAWebDBUpdateChatTable.updateChatTable(m, {hasRequestedWelcomeMsg: c})

            // WAWebBotWelcomeRequestSync.applyMutations: frontendFireAndForget("chatCollectionUpdate", {updates: [{id: m, hasRequestedWelcomeMsg: c}]})
            // ADAPTED: Cobalt does not have frontend event dispatching; the store update is sufficient
            return MutationApplicationResult.success(); // WAWebBotWelcomeRequestSync.applyMutations: {actionState: Success}
        } catch (Exception e) { // WAWebBotWelcomeRequestSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebBotWelcomeRequestSync.applyMutations: {actionState: SyncActionState.Failed}
        }
    }

    /**
     * Builds a pending mutation for setting the bot welcome request state on a chat.
     *
     * <p>Per WhatsApp Web {@code WAWebBotWelcomeRequestSync.getBotWelcomeRequestSetMutation}:
     * <ol>
     *   <li>Constructs the value with {@code {botWelcomeRequestAction: {isSent: t}}}</li>
     *   <li>Resolves the chat JID for mutation index via
     *       {@code WAWebSyncdGetChat.getChatJidMutationIndexForChat(e, Actions.BotWelcomeRequest)}</li>
     *   <li>Builds the pending mutation via {@code WAWebSyncdActionUtils.buildPendingMutation}
     *       with collection, index, value, version, operation SET, and current unix time</li>
     * </ol>
     *
     * @implNote WAWebBotWelcomeRequestSync.getBotWelcomeRequestSetMutation
     * @param chatJid the JID of the bot chat
     * @param isSent  whether the welcome message has been sent
     * @return the pending mutation for the bot welcome request action
     */
    public SyncPendingMutation getBotWelcomeRequestSetMutation(Jid chatJid, boolean isSent) {
        var action = new BotWelcomeRequestActionBuilder() // WAWebBotWelcomeRequestSync.getBotWelcomeRequestSetMutation: {botWelcomeRequestAction: {isSent: t}}
                .isSent(isSent) // WAWebBotWelcomeRequestSync.getBotWelcomeRequestSetMutation: isSent: t
                .build();
        var timestamp = Instant.now(); // WAWebBotWelcomeRequestSync.getBotWelcomeRequestSetMutation: timestamp: unixTime()
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: i
                .botWelcomeRequestAction(action) // WAWebBotWelcomeRequestSync.getBotWelcomeRequestSetMutation: {botWelcomeRequestAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs))
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, ... }
                index,
                value,
                SyncdOperation.SET, // WAWebBotWelcomeRequestSync.getBotWelcomeRequestSetMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
