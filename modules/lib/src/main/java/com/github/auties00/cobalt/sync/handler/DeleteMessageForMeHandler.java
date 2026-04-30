package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageKey;
import com.github.auties00.cobalt.model.sync.*;
import com.github.auties00.cobalt.model.sync.action.chat.DeleteMessageForMeAction;
import com.github.auties00.cobalt.sync.ConflictResolution;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.chat.DeleteMessageForMeActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles delete message for me sync actions.
 *
 * <p>This handler processes mutations that delete messages locally
 * (not for everyone in the chat). It extends the base message sync
 * action pattern with {@code collectionName = RegularHigh},
 * {@code chatJidIndex = 1}, {@code getVersion() = 3}, and
 * {@code getAction() = "deleteMessageForMe"}.
 *
 * <p>Index format: {@code ["deleteMessageForMe", chatJid, messageId, fromMe, participant]}
 *
 * @implNote WAWebDeleteMessageForMeSync — singleton instance exported as {@code default};
 *           extends {@code MessageSyncdActionBase} with
 *           {@code collectionName = RegularHigh}, {@code chatJidIndex = 1},
 *           {@code getVersion() = 3}, {@code getAction() = DeleteMessageForMe}
 */
@WhatsAppWebModule(moduleName = "WAWebDeleteMessageForMeSync")
public final class DeleteMessageForMeHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the delete message for me handler.
     *
     * <p>Per WhatsApp Web, {@code WAWebDeleteMessageForMeSync} exports a single
     * instance ({@code var S = new v(); l.default = S}).
     *
     * @implNote WAWebDeleteMessageForMeSync.default — module-level singleton
     */
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final DeleteMessageForMeHandler INSTANCE = new DeleteMessageForMeHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebDeleteMessageForMeSync — class constructor sets
     *           {@code collectionName = RegularHigh}, {@code chatJidIndex = 1}
     */
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private DeleteMessageForMeHandler() {

    }

    /**
     * Returns the action name for delete message for me actions.
     *
     * @implNote WAWebDeleteMessageForMeSync.getAction — returns
     *           {@code WASyncdConst.Actions.DeleteMessageForMe} ({@code "deleteMessageForMe"})
     * @return the action name {@code "deleteMessageForMe"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return DeleteMessageForMeAction.ACTION_NAME;
    }

    /**
     * Returns the sync collection for delete message for me actions.
     *
     * @implNote WAWebDeleteMessageForMeSync — constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.RegularHigh}
     * @return {@link SyncPatchType#REGULAR_HIGH}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return DeleteMessageForMeAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for delete message for me actions.
     *
     * @implNote WAWebDeleteMessageForMeSync.getVersion — returns {@code 3}
     * @return {@code 3}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return DeleteMessageForMeAction.ACTION_VERSION;
    }

    /**
     * Applies a single delete-message-for-me mutation by delegating to
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}.
     *
     * @implNote WAWebDeleteMessageForMeSync.applyMutations — per-mutation application
     *           logic within the batch handler
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a single delete-message-for-me mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebDeleteMessageForMeSync.applyMutations}:
     * <ul>
     *   <li>If the operation is not {@code SET}, returns {@code Unsupported}</li>
     *   <li>Extracts the four index parts: remote JID, message ID, fromMe, participant</li>
     *   <li>If any index part is missing or empty, returns {@code malformedActionIndex()}</li>
     *   <li>Builds a {@link MessageKey} via {@code syncKeyToMsgKey} for the orphan model ID</li>
     *   <li>Looks up the chat by JID; if not found, returns {@code Orphan} with the
     *       serialized MsgKey as model ID and {@code "Msg"} as model type</li>
     *   <li>Looks up the message by ID; if not found, returns {@code Orphan}</li>
     *   <li>If found, removes the message and returns {@code Success}</li>
     * </ul>
     *
     * @implNote WAWebDeleteMessageForMeSync.applyMutations — per-mutation logic in
     *           the {@code e.map} callback for SET operations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = {"applyMutations", "getMessageKey"}, adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var indexArray = JSON.parseArray(mutation.index());
        if (indexArray.size() < 5) {
            return malformedActionIndex();
        }

        var chatJidString = indexArray.getString(1);
        var messageId = indexArray.getString(2);
        var fromMeString = indexArray.getString(3);
        var participantString = indexArray.getString(4);

        if (isNullOrEmpty(chatJidString) || isNullOrEmpty(messageId)
                || isNullOrEmpty(fromMeString) || isNullOrEmpty(participantString)) {
            return malformedActionIndex();
        }

        var msgKey = SyncdIndexUtils.syncKeyToMsgKey(
                client.store(), chatJidString, messageId, fromMeString, participantString
        );
        if (msgKey.isEmpty()) {
            return malformedActionIndex();
        }

        // ADAPTED: Cobalt resolves chat directly from the index JID without the
        // incomingRemoteToLocalChatId cache used by WAWebSyncdResolveMessages
        var chatJid = Jid.of(chatJidString);
        var chat = client.store().findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return MutationApplicationResult.orphan(
                    SyncdIndexUtils.serializeMessageKey(msgKey.get()),
                    "Msg"
            );
        }

        // ADAPTED: Cobalt uses findMessageById + filter instead of msgKeyToDbIdWithoutFromMeParticipant prefix match
        var fromMe = "1".equals(fromMeString);
        var participant = !"0".equals(participantString) ? Jid.of(participantString) : null;
        var removed = client.store()
                .findMessageById(chat.get(), messageId)
                .filter(msg -> msg.key().fromMe() == fromMe)
                .filter(msg -> participant == null || participant.toUserJid().equals(msg.key().senderJid().map(Jid::toUserJid).orElse(null)))
                .flatMap(info -> info.key().id())
                .map(id -> {
                    chat.get().removeMessage(id);
                    return id;
                })
                .isPresent();

        return removed
                ? MutationApplicationResult.success()
                : MutationApplicationResult.orphan(
                        SyncdIndexUtils.serializeMessageKey(msgKey.get()),
                        "Msg"
                );
    }

    /**
     * Resolves conflicts based on the {@code deleteMedia} field.
     *
     * <p>Per WhatsApp Web {@code WAWebDeleteMessageForMeSync.resolveConflicts}:
     * <ul>
     *   <li>If the remote has {@code deleteMedia=false} and the local has
     *       {@code deleteMedia=true}: keep the local mutation (it is more
     *       aggressive), return {@code SKIP_REMOTE}</li>
     *   <li>In all other cases: drop both mutations, return
     *       {@code SKIP_REMOTE_DROP_LOCAL}</li>
     * </ul>
     *
     * <p>Notably, this handler never returns {@code APPLY_REMOTE_DROP_LOCAL}
     * and does not use timestamp comparison at all.
     *
     * @implNote WAWebDeleteMessageForMeSync.resolveConflicts
     * @param localMutation  the local pending mutation
     * @param remoteMutation the incoming remote mutation
     * @return the conflict resolution indicating which mutation to keep
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "resolveConflicts", adaptation = WhatsAppAdaptation.DIRECT)
    public ConflictResolution resolveConflicts(DecryptedMutation.Trusted localMutation, DecryptedMutation.Trusted remoteMutation) {
        var localDeleteMedia = localMutation.value().action()
                .filter(a -> a instanceof DeleteMessageForMeAction)
                .map(a -> ((DeleteMessageForMeAction) a).deleteMedia())
                .orElse(false); // ADAPTED: WANullthrows would throw on null; Cobalt coalesces to false
        var remoteDeleteMedia = remoteMutation.value().action()
                .filter(a -> a instanceof DeleteMessageForMeAction)
                .map(a -> ((DeleteMessageForMeAction) a).deleteMedia())
                .orElse(false); // ADAPTED: WANullthrows would throw on null; Cobalt coalesces to false

        if (!remoteDeleteMedia && localDeleteMedia) {
            return ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE);
        }

        return ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE_DROP_LOCAL);
    }

    /**
     * Builds a pending mutation for a delete-for-me action on a single message.
     *
     * <p>Per WhatsApp Web {@code WAWebDeleteMessageForMeSync.buildDeleteForMeMutation}:
     * constructs a {@code deleteMessageForMeAction} value with the given
     * {@code deleteMedia} and {@code messageTimestamp} fields, builds the index
     * via {@code WAWebSyncdActionUtils.buildMessageKey} using the remaining
     * parameters, and delegates to {@code WAWebSyncdActionUtils.buildPendingMutation}.
     *
     * @implNote WAWebDeleteMessageForMeSync.buildDeleteForMeMutation
     * @param timestamp        the mutation timestamp (current time)
     * @param deleteMedia      whether to also delete associated media
     * @param messageTimestamp the original message timestamp
     * @param remoteJid        the chat JID (resolved for mutation index)
     * @param id               the message ID
     * @param fromMe           whether the message was sent by the current user
     * @param participant      the participant JID for group messages, or {@code null}
     * @return the pending mutation for the delete-for-me action
     */
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "buildDeleteForMeMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation buildDeleteForMeMutation(
            Instant timestamp,
            boolean deleteMedia,
            Instant messageTimestamp,
            Jid remoteJid,
            String id,
            boolean fromMe,
            Jid participant
    ) {
        var action = new DeleteMessageForMeActionBuilder()
                .deleteMedia(deleteMedia)
                .messageTimestamp(messageTimestamp)
                .build();

        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .deleteMessageForMeAction(action)
                .build();

        var fromMeStr = fromMe ? "1" : "0";
        var participantStr = participant != null && !fromMe
                ? participant.toString()
                : "0";

        var index = JSON.toJSONString(List.of(
                actionName(),
                remoteJid.toString(),
                id,
                fromMeStr,
                participantStr
        ));

        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0);
    }

    /**
     * Builds pending mutations for deleting multiple messages for the current user.
     *
     * <p>Per WhatsApp Web {@code WAWebDeleteMessageForMeSync.getDeleteForMeMutations}:
     * iterates over each message, resolves its chat JID for the mutation index,
     * determines the sender, and delegates to {@link #buildDeleteForMeMutation}.
     *
     * <p>In Cobalt, the caller provides pre-resolved {@link MessageKey} instances
     * and the chat JID rather than raw message models, since Cobalt does not have
     * the WA Web message model accessors ({@code getSender}, {@code getT},
     * {@code getIsGroupMsg}).
     *
     * @implNote ADAPTED: WAWebDeleteMessageForMeSync.getDeleteForMeMutations — Cobalt
     *           takes pre-resolved keys instead of raw WA Web Msg models
     * @param keys        the message keys to delete
     * @param deleteMedia whether to also delete associated media
     * @param messageTimestamps the original message timestamps, parallel to {@code keys}
     * @param isGroupMessages whether each message is a group message, parallel to {@code keys}
     * @return the list of pending mutations for all messages
     */
    @WhatsAppWebExport(moduleName = "WAWebDeleteMessageForMeSync", exports = "getDeleteForMeMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public List<SyncPendingMutation> getDeleteForMeMutations(
            List<MessageKey> keys,
            boolean deleteMedia,
            List<Instant> messageTimestamps,
            List<Boolean> isGroupMessages
    ) {
        var now = Instant.now();
        var results = new ArrayList<SyncPendingMutation>(keys.size());
        for (var i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var messageTimestamp = messageTimestamps.get(i);
            var isGroup = isGroupMessages.get(i);

            var senderJid = key.senderJid().map(Jid::toUserJid).orElse(null); // ADAPTED: WAWebDeleteMessageForMeSync.getDeleteForMeMutations — getSender + widToUserJid

            var participant = isGroup && !key.fromMe() ? senderJid : null;

            // ADAPTED: Cobalt uses the message's parentJid directly
            var remoteJid = key.parentJid().orElse(null);
            if (remoteJid == null) {
                continue; // ADAPTED: defensive null check — WA Web would throw via getChatJidMutationIndexForChat
            }

            results.add(buildDeleteForMeMutation(
                    now,
                    deleteMedia,
                    messageTimestamp,
                    remoteJid,
                    key.id().orElse(""),
                    key.fromMe(),
                    participant
            ));
        }
        return results;
    }

    /**
     * Checks whether the given string is {@code null} or empty.
     *
     * <p>Used to replicate WA Web's JavaScript falsy check ({@code !value})
     * on index part strings.
     *
     * @implNote ADAPTED: WAWebDeleteMessageForMeSync.applyMutations — JS falsy check
     *           {@code !i || !s || !u || !f} maps to null/empty check in Java
     * @param value the string to check
     * @return {@code true} if the string is {@code null} or empty
     */
    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty(); // ADAPTED: JS !value is falsy for null/undefined/""
    }
}
