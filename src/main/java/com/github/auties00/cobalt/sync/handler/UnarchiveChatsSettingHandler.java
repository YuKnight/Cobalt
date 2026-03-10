package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.UnarchiveChatsSetting;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles unarchive chats setting changes.
 */
public final class UnarchiveChatsSettingHandler implements WebAppStateActionHandler {
    public static final UnarchiveChatsSettingHandler INSTANCE = new UnarchiveChatsSettingHandler();

    private UnarchiveChatsSettingHandler() {

    }

    @Override
    public String actionName() {
        return UnarchiveChatsSetting.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return UnarchiveChatsSetting.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return UnarchiveChatsSetting.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        if (mutations.isEmpty()) {
            return List.of();
        }

        var results = new ArrayList<MutationApplicationResult>(mutations.size());
        for (int i = 0; i < mutations.size() - 1; i++) {
            results.add(MutationApplicationResult.skipped());
        }
        results.add(applyMutationResult(client, mutations.getLast()));
        return results;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof UnarchiveChatsSetting setting)) {
            return MutationApplicationResult.malformed();
        }

        var unarchiveChats = setting.unarchiveChats();
        client.store().setUnarchiveChats(unarchiveChats);
        updateSideEffectOnChats(client, unarchiveChats);
        return MutationApplicationResult.success();
    }

    private void updateSideEffectOnChats(WhatsAppClient client, boolean unarchiveChats) {
        if (!unarchiveChats) {
            return;
        }

        for (var chat : client.store().chats()) {
            if (chat.archived() && chat.unreadCount().orElse(0) > 0) {
                chat.setArchived(false);
            }
        }
    }
}
