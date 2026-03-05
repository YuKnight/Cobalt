package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.chat.ChatMute;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.MuteAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;

/**
 * Handles mute chat actions.
 *
 * <p>This handler processes mutations that mute or unmute chat notifications.
 *
 * <p>Index format: ["muteAction", "chatJid"]
 */
public final class MuteChatHandler implements WebAppStateActionHandler {
    public static final MuteChatHandler INSTANCE = new MuteChatHandler();

    private MuteChatHandler() {

    }

    @Override
    public String actionName() {
        return MuteAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return MuteAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return MuteAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof MuteAction action)) {
            return false;
        }

        if (action.muted() && action.muteEndTimestamp().isEmpty()) {
            return false;
        }

        var chatJidString = JSON.parseArray(mutation.index())
                .getString(1);
        var chatJid = Jid.of(chatJidString);

        var chat = client.store()
                .findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return false;
        }

        var muteEndSeconds = action.muteEndTimestamp()
                .map(Instant::getEpochSecond)
                .orElse(0L);
        if (muteEndSeconds > 0 && muteEndSeconds < Instant.now().getEpochSecond()) {
            muteEndSeconds = 0L;
        }

        chat.get().setMute(ChatMute.mutedUntil(muteEndSeconds));

        return true;
    }
}
