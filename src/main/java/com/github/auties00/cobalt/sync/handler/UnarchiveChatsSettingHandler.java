package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.UnarchiveChatsSetting;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles unarchive chats setting changes.
 *
 * <p>This handler processes mutations that control whether archived chats
 * should be automatically unarchived when a new message arrives.
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
        if (mutation.operation() != SyncdOperation.SET) {
            return true;
        }

        if (!(mutation.value().action().orElse(null) instanceof UnarchiveChatsSetting setting)) {
            return false;
        }

        client.store()
                .setUnarchiveChats(setting.unarchiveChats());

        return true;
    }
}
