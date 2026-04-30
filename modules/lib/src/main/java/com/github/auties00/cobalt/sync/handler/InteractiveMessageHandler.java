package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.InteractiveMessageAction;
import com.github.auties00.cobalt.model.sync.action.chat.InteractiveMessageAction.InteractiveMessageActionMode;
import com.github.auties00.cobalt.model.sync.action.chat.InteractiveMessageActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles interactive message sync actions.
 *
 * <p>This handler processes mutations that disable the call-to-action (CTA)
 * for Galaxy interactive messages. Per WhatsApp Web, the interactive message
 * action extends {@code MessageSyncdActionBase} with
 * {@code collectionName = RegularLow}, {@code chatJidIndex = 1},
 * {@code getVersion() = 1}, and
 * {@code getAction() = WASyncdConst.Actions.InteractiveMessageAction}.
 *
 * <p>Index format: {@code ["interactive_message_action", chatJid, messageId, fromMe, participant, subId]}
 *
 * @implNote WAWebInteractiveMessageSync — singleton instance exported as {@code default};
 *           extends {@code MessageSyncdActionBase} with
 *           {@code collectionName = RegularLow}, {@code chatJidIndex = 1},
 *           {@code getVersion() = 1}, {@code getAction() = InteractiveMessageAction}
 */
@WhatsAppWebModule(moduleName = "WAWebInteractiveMessageSync")
public final class InteractiveMessageHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the interactive message handler.
     *
     * <p>Per WhatsApp Web, {@code WAWebInteractiveMessageSync} exports a single
     * instance ({@code var _ = new p(); l.default = _}).
     *
     * @implNote WAWebInteractiveMessageSync.default — module-level singleton
     */
    @WhatsAppWebExport(moduleName = "WAWebInteractiveMessageSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final InteractiveMessageHandler INSTANCE = new InteractiveMessageHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebInteractiveMessageSync — class constructor sets
     *           {@code collectionName = RegularLow}, {@code chatJidIndex = 1}
     */
    @WhatsAppWebExport(moduleName = "WAWebInteractiveMessageSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private InteractiveMessageHandler() {

    }

    /**
     * Returns the action name for interactive message actions.
     *
     * @implNote WAWebInteractiveMessageSync.getAction — returns
     *           {@code WASyncdConst.Actions.InteractiveMessageAction}
     *           ({@code "interactive_message_action"})
     * @return the action name {@code "interactive_message_action"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebInteractiveMessageSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return InteractiveMessageAction.ACTION_NAME;
    }

    /**
     * Returns the sync collection for interactive message actions.
     *
     * @implNote WAWebInteractiveMessageSync — constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebInteractiveMessageSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return InteractiveMessageAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for interactive message actions.
     *
     * @implNote WAWebInteractiveMessageSync.getVersion — returns {@code 1}
     * @return {@code 1}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebInteractiveMessageSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return InteractiveMessageAction.ACTION_VERSION;
    }

    /**
     * Applies a single interactive message mutation by delegating to
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}.
     *
     * @implNote WAWebInteractiveMessageSync.applyMutations — per-mutation application
     *           logic within the batch handler
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebInteractiveMessageSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a single interactive message mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebInteractiveMessageSync.applyMutations}:
     * <ul>
     *   <li>If the operation is not {@code SET}, returns {@code Unsupported}</li>
     *   <li>Extracts the five index parts: chatJid, messageId, fromMe, participant, subId</li>
     *   <li>If any index part is missing/empty, returns {@code malformedActionIndex()}</li>
     *   <li>If the action value has no {@code interactiveMessageAction}, returns
     *       {@code malformedActionValue()}</li>
     *   <li>Builds a {@link com.github.auties00.cobalt.model.message.MessageKey}
     *       via {@code syncKeyToMsgKey} for the orphan model ID. If the key cannot
     *       be built, returns {@code malformedActionIndex()}</li>
     *   <li>Resolves the local chat by JID. If not found and {@code agmId} is
     *       {@code null}, returns {@code Orphan} with the serialized MsgKey as
     *       model ID and {@code "Msg"} as model type</li>
     *   <li>If the {@code agmId} is present: records the {@code agmId -> action}
     *       state (equivalent to WA Web's
     *       {@code frontendFireAndForget("addGalaxyDisableCTAByAgmId")})</li>
     *   <li>Looks up the message by ID within the resolved chat. If not found:
     *     <ul>
     *       <li>If {@code agmId} is present, returns {@code Success}</li>
     *       <li>Otherwise returns {@code Orphan}</li>
     *     </ul>
     *   </li>
     *   <li>If the action type is not {@code DISABLE_CTA}, returns {@code Skipped}</li>
     *   <li>Records the {@code messageId -> action} and
     *       {@code chatJid|messageId|fromMe|participant|subId -> action} state
     *       (equivalent to WA Web's
     *       {@code frontendFireAndForget("addGalaxyDisableCTAMessageId")})</li>
     *   <li>Returns {@code Success}</li>
     * </ul>
     *
     * <p>Per WhatsApp Web, the pre-resolved {@code incomingRemoteToLocalChatId}
     * map is produced by {@code WAWebSyncdResolveMessages.resolveMessagesForMutations}
     * before the batch is iterated. Cobalt collapses this into a direct
     * {@code findChatByJid} lookup per mutation, since both incoming and local
     * chat JIDs are canonicalized in the local store.
     *
     * @implNote WAWebInteractiveMessageSync.applyMutations — per-mutation logic in
     *           the {@code e.map} callback for SET operations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebInteractiveMessageSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        try {
            if (mutation.operation() != SyncdOperation.SET) {
                return MutationApplicationResult.unsupported();
            }

            var indexArray = JSON.parseArray(mutation.index());
            if (indexArray.size() < 6) {
                return malformedActionIndex();
            }

            var chatJidString = indexArray.getString(1);
            var messageId = indexArray.getString(2);
            var fromMeString = indexArray.getString(3);
            var participantString = indexArray.getString(4);
            var subIdString = indexArray.getString(5);

            if (isNullOrEmpty(chatJidString) || isNullOrEmpty(messageId)
                    || isNullOrEmpty(fromMeString) || isNullOrEmpty(participantString)
                    || isNullOrEmpty(subIdString)) {
                return malformedActionIndex();
            }

            if (!(mutation.value().action().orElse(null) instanceof InteractiveMessageAction action)) {
                return malformedActionValue();
            }

            var incomingMsgKey = SyncdIndexUtils.syncKeyToMsgKey(
                    client.store(), chatJidString, messageId, fromMeString, participantString
            );
            if (incomingMsgKey.isEmpty()) {
                return malformedActionIndex();
            }

            // ADAPTED: Cobalt resolves chat directly from the index JID without the
            // incomingRemoteToLocalChatId cache used by WAWebSyncdResolveMessages
            var chatJid = Jid.of(chatJidString);
            var localChat = client.store().findChatByJid(chatJid); // ADAPTED: WAWebSyncdResolveMessages.resolveMessagesForMutations -> WAWebSyncdGetChat.resolveChatForMutationIndex

            var agmId = action.agmId().orElse(null);

            if (localChat.isEmpty()) {
                return MutationApplicationResult.orphan(
                        SyncdIndexUtils.serializeMessageKey(incomingMsgKey.get()),
                        "Msg"
                );
            }

            // ADAPTED: Cobalt uses findMessageById directly since messages are keyed by (chatJid, messageId)
            // Note: findMessageById(Chat, String) returns Optional<ChatMessageInfo>, which is the
            // equivalent of WA Web's MsgCollection.get(E) — newsletter messages do not flow through
            // this path.
            var maybeMessage = client.store().findMessageById(localChat.get(), messageId);

            // ADAPTED: Cobalt backend records the agmId->action state directly (no frontend bridge).
            // Snapshot the current states once so both agmId and messageId writes land atomically.
            var states = new HashMap<>(client.store().interactiveMessageStates()); // ADAPTED: unmodifiable map -> mutable snapshot
            if (agmId != null) {
                states.put("agmId|" + agmId, action); // ADAPTED: WAWebBackendApi.frontendFireAndForget("addGalaxyDisableCTAByAgmId", {agmId: k, chatId: v})
            }

            if (maybeMessage.isEmpty()) {
                if (agmId != null) {
                    client.store().setInteractiveMessageStates(states); // ADAPTED: commit agmId state
                    return MutationApplicationResult.success();
                }

                return MutationApplicationResult.orphan(
                        SyncdIndexUtils.serializeMessageKey(incomingMsgKey.get()),
                        "Msg"
                );
            }

            var chatMessage = maybeMessage.get();

            if (action.type() != InteractiveMessageActionMode.DISABLE_CTA) {
                if (agmId != null) {
                    client.store().setInteractiveMessageStates(states); // ADAPTED: still commit agmId state before returning skipped
                }
                return MutationApplicationResult.skipped();
            }

            // ADAPTED: Cobalt backend records the messageId->action state and the full composite index->action state
            var messageKeyId = chatMessage.key().id().orElse(messageId);
            states.put("messageId|" + messageKeyId, action); // ADAPTED: WAWebBackendApi.frontendFireAndForget("addGalaxyDisableCTAMessageId", {messageId: I.id.toString()})
            states.put( // ADAPTED: composite index key for per-subId lookups
                    "%s|%s|%s|%s|%s".formatted(chatJidString, messageId, fromMeString, participantString, subIdString),
                    action
            );
            client.store().setInteractiveMessageStates(states); // ADAPTED: commit all state writes
            return MutationApplicationResult.success();
        } catch (Exception e) {
            return MutationApplicationResult.failed();
        }
    }

    /**
     * Builds an {@link InteractiveMessageAction} payload for a DISABLE_CTA mutation.
     *
     * <p>Per WhatsApp Web {@code WAWebInteractiveMessageSync.$InteractiveMessageSync$p_1}:
     * constructs a {@code {type, agmId}} action payload wrapped in a
     * {@code {interactiveMessageAction: ...}} sync action value, then delegates to
     * {@code WAWebSyncdActionUtils.buildPendingMutation} to build the pending
     * mutation with the resolved chat JID from
     * {@code WAWebSyncdGetChat.getChatJidMutationIndexForChat}.
     *
     * <p>In Cobalt, the pending mutation wiring (resolving chat JID, serializing
     * the sync action value, signing, etc.) is handled elsewhere in the
     * {@code WebAppStateSender} pipeline. This helper therefore only builds the
     * action payload itself, leaving downstream code to wrap it in a
     * {@link com.github.auties00.cobalt.model.sync.SyncActionValue} and construct
     * the {@link com.github.auties00.cobalt.model.sync.SyncPendingMutation}.
     *
     * @implNote WAWebInteractiveMessageSync.$InteractiveMessageSync$p_1 — Cobalt
     *           reduces the WA Web {@code p_1} helper to a payload constructor since
     *           mutation wrapping/signing is centralized
     * @param type  the interactive message action mode (typically
     *              {@link InteractiveMessageActionMode#DISABLE_CTA})
     * @param agmId the optional Galaxy AGM identifier, or {@code null}
     * @return the action payload
     */
    @WhatsAppWebExport(moduleName = "WAWebInteractiveMessageSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public InteractiveMessageAction buildDisableCTAAction(InteractiveMessageActionMode type, String agmId) {
        var builder = new InteractiveMessageActionBuilder().type(type);
        if (agmId != null) {
            builder.agmId(agmId);
        }
        return builder.build();
    }

    /**
     * Returns whether the given string is {@code null} or empty.
     *
     * <p>Used to replicate WA Web's JavaScript falsy check ({@code !value})
     * on index part strings.
     *
     * @implNote ADAPTED: WAWebInteractiveMessageSync.applyMutations — JS falsy check
     *           {@code !i || !s || !u || !c || !d} maps to null/empty check in Java
     * @param value the string to check
     * @return {@code true} if the string is {@code null} or empty
     */
    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty(); // ADAPTED: JS !value is falsy for null/undefined/""
    }

    /**
     * Returns an immutable snapshot of the currently-tracked interactive message
     * action states. Test hook that reflects the same map exposed by
     * {@link com.github.auties00.cobalt.store.WhatsAppStore#interactiveMessageStates()}.
     *
     * @implNote ADAPTED: WAWebInteractiveMessageSync does not expose a public
     *           accessor for its frontend-recorded disable-CTA state; Cobalt
     *           surfaces the state through the store directly
     * @param client the WhatsApp client whose store should be queried
     * @return the interactive message action states map
     */
    public Map<String, InteractiveMessageAction> interactiveMessageStates(WhatsAppClient client) {
        return client.store().interactiveMessageStates(); // ADAPTED: proxy to store for handler-level access
    }
}
