package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.NoteEditAction;
import com.github.auties00.cobalt.model.sync.action.media.NoteEditActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles note edit actions.
 */
public final class NoteEditHandler implements WebAppStateActionHandler {
    public static final NoteEditHandler INSTANCE = new NoteEditHandler();

    private NoteEditHandler() {

    }

    @Override
    public String actionName() {
        return NoteEditAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return NoteEditAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return NoteEditAction.ACTION_VERSION;
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

        var noteId = JSON.parseArray(mutation.index()).getString(1);
        if (noteId == null || noteId.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof NoteEditAction action)) {
            return MutationApplicationResult.malformed();
        }

        var states = new java.util.HashMap<>(client.store().noteStates());
        if (action.deleted()) {
            states.remove(noteId);
            states.keySet().removeIf(key -> key.endsWith("|" + noteId));
            client.store().setNoteStates(states);
            return MutationApplicationResult.success();
        }

        if (action.type().isEmpty() || action.chatJid().isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var chatJid = action.chatJid().orElse(Jid.of(""));
        var chat = client.store().findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return MutationApplicationResult.orphan(chatJid.toString(), "Chat");
        }

        var normalizedAction = new NoteEditActionBuilder()
                .type(action.type().orElseThrow())
                .chatJid(chat.get().jid())
                .createdAt(action.createdAt().orElse(0L))
                .deleted(false)
                .unstructuredContent(action.unstructuredContent().orElse(""))
                .build();
        states.put("%s|%s".formatted(chat.get().jid(), noteId), normalizedAction);
        client.store().setNoteStates(states);
        return MutationApplicationResult.success();
    }
}
