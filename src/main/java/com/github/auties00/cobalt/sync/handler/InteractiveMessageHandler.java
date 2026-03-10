package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.InteractiveMessageAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles interactive message actions.
 *
 * <p>Index format: ["interactive_message_action", "chatJid", "messageId", "fromMe", "participant", "subId"]
 */
public final class InteractiveMessageHandler implements WebAppStateActionHandler {
    public static final InteractiveMessageHandler INSTANCE = new InteractiveMessageHandler();

    private InteractiveMessageHandler() {

    }

    @Override
    public String actionName() {
        return InteractiveMessageAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return InteractiveMessageAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return InteractiveMessageAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var indexArray = JSON.parseArray(mutation.index());
        if (indexArray.size() < 6) {
            return MutationApplicationResult.malformed();
        }

        var chatJid = indexArray.getString(1);
        var messageId = indexArray.getString(2);
        var fromMe = indexArray.getString(3);
        var participant = indexArray.getString(4);
        var subId = indexArray.getString(5);
        if (chatJid == null || chatJid.isEmpty()
                || messageId == null || messageId.isEmpty()
                || fromMe == null || fromMe.isEmpty()
                || participant == null || participant.isEmpty()
                || subId == null || subId.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof InteractiveMessageAction action)) {
            return MutationApplicationResult.malformed();
        }

        var states = new java.util.HashMap<>(client.store().interactiveMessageStates());
        action.agmId().ifPresent(agmId -> states.put("agmId|" + agmId, action));

        var message = client.store().findMessageById(Jid.of(chatJid), messageId).orElse(null);
        if (message == null) {
            if (action.agmId().isPresent()) {
                client.store().setInteractiveMessageStates(states);
                return MutationApplicationResult.success();
            }

            return MutationApplicationResult.orphan("%s:%s:%s:%s".formatted(chatJid, messageId, fromMe, participant), "Msg");
        }

        if (action.type() != InteractiveMessageAction.InteractiveMessageActionMode.DISABLE_CTA) {
            return MutationApplicationResult.skipped();
        }

        if (!(message instanceof com.github.auties00.cobalt.model.chat.ChatMessageInfo chatMessageInfo)) {
            return MutationApplicationResult.skipped();
        }

        states.put("messageId|" + chatMessageInfo.key().id(), action);
        states.put("%s|%s|%s|%s|%s".formatted(chatJid, messageId, fromMe, participant, subId), action);
        client.store().setInteractiveMessageStates(states);
        return MutationApplicationResult.success();
    }
}
