package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.privacy.PrivacySettingEntryBuilder;
import com.github.auties00.cobalt.model.privacy.PrivacySettingType;
import com.github.auties00.cobalt.model.privacy.PrivacySettingValue;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.StatusPrivacyAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.List;

/**
 * Handles status privacy actions.
 *
 * <p>Per WhatsApp Web {@code WAWebStatusPrivacySettingSync}, only SET is
 * supported. On SET, validates that {@code statusPrivacy} is non-{@code null}
 * and that {@code statusPrivacy.mode} is non-{@code null}.
 *
 * <p>Index format: ["status_privacy"]
 */
public final class StatusPrivacyHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code StatusPrivacyHandler}.
     */
    public static final StatusPrivacyHandler INSTANCE = new StatusPrivacyHandler();

    private StatusPrivacyHandler() {

    }

    @Override
    public String actionName() {
        return StatusPrivacyAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return StatusPrivacyAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return StatusPrivacyAction.ACTION_VERSION;
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

        if (!(mutation.value().action().orElse(null) instanceof StatusPrivacyAction action)) {
            return MutationApplicationResult.malformed();
        }

        var mode = action.mode().orElse(null);
        if (mode == null) {
            return MutationApplicationResult.malformed();
        }

        var entry = switch (mode) {
            case CONTACTS -> new PrivacySettingEntryBuilder()
                    .type(PrivacySettingType.STATUS)
                    .value(PrivacySettingValue.CONTACTS)
                    .excluded(List.of())
                    .build();
            case ALLOW_LIST -> new PrivacySettingEntryBuilder()
                    .type(PrivacySettingType.STATUS)
                    .value(PrivacySettingValue.CONTACTS_ONLY)
                    .excluded(action.userJid())
                    .build();
            case DENY_LIST -> new PrivacySettingEntryBuilder()
                    .type(PrivacySettingType.STATUS)
                    .value(PrivacySettingValue.CONTACTS_EXCEPT)
                    .excluded(action.userJid())
                    .build();
            case CLOSE_FRIENDS -> new PrivacySettingEntryBuilder()
                    .type(PrivacySettingType.STATUS)
                    .value(PrivacySettingValue.CONTACTS)
                    .excluded(List.of())
                    .build();
        };
        client.store().addPrivacySetting(entry);
        return MutationApplicationResult.success();
    }
}
