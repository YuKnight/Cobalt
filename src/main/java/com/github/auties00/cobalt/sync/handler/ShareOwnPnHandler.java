package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles share own phone number actions.
 *
 * <p>Per WhatsApp Web {@code WAWebShareOwnPnSync}, only SET operations are
 * supported. The handler validates that index[1] is a valid LID JID.
 *
 * <p>Index format: ["shareOwnPn", "lidJid"]
 */
public final class ShareOwnPnHandler implements WebAppStateActionHandler {
    public static final ShareOwnPnHandler INSTANCE = new ShareOwnPnHandler();

    private ShareOwnPnHandler() {

    }

    @Override
    public String actionName() {
        return "shareOwnPn";
    }

    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR;
    }

    @Override
    public int version() {
        return 8;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.SHARE_OWN_PN_SYNC)) {
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var indexArray = JSON.parseArray(mutation.index());
        if (indexArray.size() < 2) {
            return MutationApplicationResult.malformed();
        }

        var lidJidString = indexArray.getString(1);
        if (lidJidString == null || lidJidString.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var lidJid = Jid.of(lidJidString);
        if (!lidJid.hasLidServer()) {
            return MutationApplicationResult.malformed();
        }

        var states = new java.util.HashMap<>(client.store().shareOwnPnStates());
        states.put(lidJidString, true);
        client.store().setShareOwnPnStates(states);

        var contact = client.store().findContactByJid(lidJid);
        contact.ifPresent(entry -> entry.setPhoneNumberShared(true));
        client.store().findChatByJid(lidJid)
                .ifPresent(chat -> chat.setShareOwnPhoneNumber(true));
        return MutationApplicationResult.success();
    }
}
