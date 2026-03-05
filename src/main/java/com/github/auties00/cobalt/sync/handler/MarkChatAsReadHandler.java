package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.ConflictResolutionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.MarkChatAsReadAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles mark chat as read actions.
 *
 * <p>This handler processes mutations that mark all messages in a chat as read.
 *
 * <p>Index format: ["markChatAsReadAction", "chatJid"]
 */
public final class MarkChatAsReadHandler implements WebAppStateActionHandler {
    public static final MarkChatAsReadHandler INSTANCE = new MarkChatAsReadHandler();

    private MarkChatAsReadHandler() {

    }

    @Override
    public String actionName() {
        return MarkChatAsReadAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return MarkChatAsReadAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return MarkChatAsReadAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof MarkChatAsReadAction action)) {
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

        if (action.read()) {
            chat.get().setMarkedAsUnread(false);
            chat.get().setUnreadCount(0);
        } else {
            chat.get().setMarkedAsUnread(true);
            chat.get().setUnreadCount(-1);
        }

        return true;
    }

    /**
     * Resolves conflicts using message range comparison.
     *
     * <p>Per WhatsApp Web {@code WAWebMarkChatAsReadSync.resolveConflicts}:
     * the mutation whose message range covers a broader scope of messages
     * wins. When ranges are equal, timestamp is used as a tiebreaker.
     *
     * @param localMutation  the local pending mutation
     * @param remoteMutation the incoming remote mutation
     * @return the resolution state indicating which mutation to keep
     */
    @Override
    public ConflictResolutionState resolveConflicts(DecryptedMutation.Trusted localMutation, DecryptedMutation.Trusted remoteMutation) {
        var localRange = localMutation.value().action()
                .filter(a -> a instanceof MarkChatAsReadAction)
                .map(a -> (MarkChatAsReadAction) a)
                .flatMap(MarkChatAsReadAction::messageRange)
                .orElse(null);
        var remoteRange = remoteMutation.value().action()
                .filter(a -> a instanceof MarkChatAsReadAction)
                .map(a -> (MarkChatAsReadAction) a)
                .flatMap(MarkChatAsReadAction::messageRange)
                .orElse(null);

        if (localRange == null || remoteRange == null) {
            return remoteMutation.timestamp().compareTo(localMutation.timestamp()) >= 0
                    ? ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL
                    : ConflictResolutionState.SKIP_REMOTE;
        }

        return switch (MessageRangeUtils.compareMessageRanges(remoteRange, localRange)) {
            case RANGE_A_ENCLOSES_RANGE_B -> ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL;
            case RANGE_B_ENCLOSES_RANGE_A -> ConflictResolutionState.SKIP_REMOTE;
            case RANGES_ARE_EQUAL, RANGES_NOT_ENCLOSING ->
                    localMutation.timestamp().compareTo(remoteMutation.timestamp()) <= 0
                            ? ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL
                            : ConflictResolutionState.SKIP_REMOTE;
        };
    }
}
