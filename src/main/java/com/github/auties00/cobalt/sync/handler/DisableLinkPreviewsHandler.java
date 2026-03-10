package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivacySettingDisableLinkPreviewsAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles disable link previews setting actions.
 *
 * <p>This handler processes mutations that control whether link previews are
 * disabled in chat messages. The mutation is acknowledged but not applied
 * locally.
 *
 * <p>Index format: ["setting_disableLinkPreviews"]
 */
public final class DisableLinkPreviewsHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code DisableLinkPreviewsHandler}.
     */
    public static final DisableLinkPreviewsHandler INSTANCE = new DisableLinkPreviewsHandler();

    private DisableLinkPreviewsHandler() {

    }

    @Override
    public String actionName() {
        return PrivacySettingDisableLinkPreviewsAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PrivacySettingDisableLinkPreviewsAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PrivacySettingDisableLinkPreviewsAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Per WhatsApp Web {@code WAWebDisableLinkPreviewsSync.applyMutations}: iterates
     * all mutations, accumulating the last valid {@code isPreviewsDisabled} value from
     * SET operations (non-SET and malformed mutations are skipped). After iteration,
     * persists the accumulated value once.
     */
    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        if (mutations.isEmpty()) {
            return List.of();
        }

        Boolean lastValid = null;
        var results = new ArrayList<MutationApplicationResult>(mutations.size());
        for (var mutation : mutations) {
            if (mutation.operation() != SyncdOperation.SET) {
                results.add(MutationApplicationResult.unsupported());
                continue;
            }

            if (mutation.value().action().orElse(null) instanceof PrivacySettingDisableLinkPreviewsAction action) {
                lastValid = action.isPreviewsDisabled();
                results.add(MutationApplicationResult.success());
            } else {
                results.add(MutationApplicationResult.malformed());
            }
        }

        if (lastValid != null) {
            client.store().setDisableLinkPreviews(lastValid);
        }

        return results;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof PrivacySettingDisableLinkPreviewsAction action)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setDisableLinkPreviews(action.isPreviewsDisabled());
        return MutationApplicationResult.success();
    }
}
