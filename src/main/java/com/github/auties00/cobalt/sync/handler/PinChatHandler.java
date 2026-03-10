package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.PinAction;
import com.github.auties00.cobalt.model.sync.action.contact.PinActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.Comparator;

/**
 * Handles pin chat actions.
 */
public final class PinChatHandler implements WebAppStateActionHandler {
    private static final int MAX_PINNED_NEWSLETTERS = 2;

    public static final PinChatHandler INSTANCE = new PinChatHandler();

    private PinChatHandler() {

    }

    @Override
    public String actionName() {
        return PinAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PinAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PinAction.ACTION_VERSION;
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

        if (!(mutation.value().action().orElse(null) instanceof PinAction action)) {
            return MutationApplicationResult.malformed();
        }

        var chatJidString = JSON.parseArray(mutation.index()).getString(1);
        if (chatJidString == null || chatJidString.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var chat = client.store().findChatByJid(Jid.of(chatJidString));
        if (chat.isEmpty()) {
            var newsletter = client.store().findNewsletterByJid(Jid.of(chatJidString));
            if (newsletter.isEmpty()) {
                return MutationApplicationResult.orphan(chatJidString, "Chat");
            }

            return applyNewsletterPinMutation(client, newsletter.get().jid(), action, mutation.timestamp());
        }

        if (!action.pinned()) {
            chat.get().setPinnedTimestamp(null);
            return MutationApplicationResult.success();
        }

        var currentPin = mutation.timestamp();
        var maxPinnedChats = Math.max(1, client.abPropsService().getInt(ABProp.MAX_PINNED_CHATS_COUNT));
        var pinnedChats = client.store().chats().stream()
                .filter(entry -> entry.pinnedTimestamp().isPresent())
                .filter(entry -> !entry.jid().equals(chat.get().jid()))
                .sorted(Comparator.comparing(entry -> entry.pinnedTimestamp().orElse(java.time.Instant.EPOCH)))
                .toList();
        if (pinnedChats.size() >= maxPinnedChats) {
            var oldestPinned = pinnedChats.getFirst();
            var oldestTimestamp = oldestPinned.pinnedTimestamp().orElse(java.time.Instant.EPOCH);
            if (!oldestTimestamp.isBefore(currentPin)) {
                queueUnpinMutation(client, chat.get().jid(), currentPin);
                return MutationApplicationResult.success();
            }

            oldestPinned.setPinnedTimestamp(null);
            queueUnpinMutation(client, oldestPinned.jid(), currentPin);
        }

        chat.get().setPinnedTimestamp(currentPin);
        chat.get().setArchived(false);
        return MutationApplicationResult.success();
    }

    private MutationApplicationResult applyNewsletterPinMutation(
            WhatsAppClient client,
            Jid newsletterJid,
            PinAction action,
            java.time.Instant timestamp
    ) {
        var states = new java.util.HashMap<>(client.store().newsletterPinStates());
        if (!action.pinned()) {
            states.remove(newsletterJid.toString());
            client.store().setNewsletterPinStates(states);
            return MutationApplicationResult.success();
        }

        if (!states.containsKey(newsletterJid.toString()) && states.size() >= MAX_PINNED_NEWSLETTERS) {
            var oldest = states.entrySet().stream()
                    .min(java.util.Map.Entry.comparingByValue())
                    .orElse(null);
            if (oldest != null && !oldest.getValue().isBefore(timestamp)) {
                queueUnpinMutation(client, newsletterJid, timestamp);
                return MutationApplicationResult.success();
            }

            if (oldest != null) {
                states.remove(oldest.getKey());
                queueUnpinMutation(client, Jid.of(oldest.getKey()), timestamp);
            }
        }

        states.put(newsletterJid.toString(), timestamp);
        client.store().setNewsletterPinStates(states);
        return MutationApplicationResult.success();
    }

    private void queueUnpinMutation(WhatsAppClient client, Jid chatJid, java.time.Instant timestamp) {
        var pinAction = new PinActionBuilder()
                .pinned(false)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .pinAction(pinAction)
                .build();
        var index = JSON.toJSONString(java.util.List.of(actionName(), chatJid.toString()));
        var mutation = new DecryptedMutation.Trusted(index, value, SyncdOperation.SET, timestamp, version());
        client.store().addPendingMutations(collectionName(), java.util.List.of(new SyncPendingMutation(mutation, 0)));
    }
}
