package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LidContactAction;
import com.github.auties00.cobalt.model.sync.action.contact.UserStatusMuteAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles LID contact actions.
 */
public final class LidContactHandler implements WebAppStateActionHandler {
    public static final LidContactHandler INSTANCE = new LidContactHandler();

    private LidContactHandler() {

    }

    @Override
    public String actionName() {
        return LidContactAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return LidContactAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return LidContactAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.USERNAME_CONTACT_SYNCD_SUPPORT_ENABLE)) {
            return MutationApplicationResult.unsupported();
        }

        var lidJidString = JSON.parseArray(mutation.index()).getString(1);
        if (lidJidString == null || lidJidString.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var lidJid = Jid.of(lidJidString);
        if (!lidJid.hasLidServer()) {
            return MutationApplicationResult.malformed();
        }

        return switch (mutation.operation()) {
            case SET -> {
                if (!(mutation.value().action().orElse(null) instanceof LidContactAction action)) {
                    yield MutationApplicationResult.malformed();
                }

                var contact = client.store().findContactByJid(lidJid)
                        .orElseGet(() -> client.store().addNewContact(lidJid));
                var fullName = action.fullName().orElse("");
                var shortName = action.firstName().orElseGet(() -> {
                    if (fullName.isBlank()) {
                        return "";
                    }

                    var separator = fullName.indexOf(' ');
                    return separator == -1 ? fullName : fullName.substring(0, separator);
                });
                contact.setFullName(fullName);
                contact.setShortName(shortName);
                if (client.abPropsService().getBool(ABProp.USERNAME_CONTACT_DISPLAY)) {
                    var username = action.username()
                            .map(entry -> entry.startsWith("@") ? entry.substring(1) : entry)
                            .orElse(null);
                    contact.setUsername(username);
                    contact.setAddedByUsername(username != null && !username.isBlank());
                }
                retryOrphanStatusMutes(client, lidJidString);
                yield MutationApplicationResult.success();
            }
            case REMOVE -> {
                client.store().findContactByJid(lidJid).ifPresent(contact -> {
                    if (contact.isAddedByUsername()) {
                        contact.setFullName(null);
                        contact.setShortName(null);
                        contact.setUsername(null);
                        contact.setAddedByUsername(false);
                    }
                });
                yield MutationApplicationResult.success();
            }
            default -> MutationApplicationResult.unsupported();
        };
    }

    private void retryOrphanStatusMutes(WhatsAppClient client, String lidJidString) {
        var entries = client.store().findOrphanMutationsByModel(UserStatusMuteAction.COLLECTION_NAME, lidJidString);
        if (entries.isEmpty()) {
            return;
        }

        var applied = new java.util.ArrayList<com.github.auties00.cobalt.model.sync.OrphanMutationEntry>();
        for (var entry : entries) {
            var mutation = new DecryptedMutation.Trusted(
                    entry.index(),
                    entry.value(),
                    entry.operation(),
                    entry.timestamp(),
                    entry.actionVersion()
            );
            var result = UserStatusMuteHandler.INSTANCE.applyMutationResult(client, mutation);
            if (result.actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS) {
                applied.add(entry);
            }
        }

        if (!applied.isEmpty()) {
            client.store().removeOrphanMutations(UserStatusMuteAction.COLLECTION_NAME, applied);
        }
    }
}
