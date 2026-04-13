package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivacySettingDisableLinkPreviewsAction;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivacySettingDisableLinkPreviewsActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles disable link previews setting sync actions.
 *
 * <p>This handler processes mutations that control whether link previews are
 * disabled in chat messages. It maps to the singleton instance exported
 * as {@code default} from the WA Web module, which extends
 * {@code AccountSyncdActionBase} with collection {@code Regular},
 * version {@code 8}, and action {@code "setting_disableLinkPreviews"}.
 *
 * <p>Index format: {@code ["setting_disableLinkPreviews"]}
 *
 * @implNote WAWebDisableLinkPreviewsSync.default (singleton instance of the
 *           DisableLinkPreviewsSync class extending AccountSyncdActionBase)
 */
public final class DisableLinkPreviewsHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code DisableLinkPreviewsHandler}.
     *
     * @implNote WAWebDisableLinkPreviewsSync.default — {@code var m = new d; l.default = m}
     */
    public static final DisableLinkPreviewsHandler INSTANCE = new DisableLinkPreviewsHandler();

    /**
     * Creates a new {@code DisableLinkPreviewsHandler}.
     *
     * @implNote WAWebDisableLinkPreviewsSync — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     */
    private DisableLinkPreviewsHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebDisableLinkPreviewsSync.getAction — returns
     *           {@code WASyncdConst.Actions.DisableLinkPreviews} which is
     *           {@code "setting_disableLinkPreviews"}
     */
    @Override
    public String actionName() {
        return PrivacySettingDisableLinkPreviewsAction.ACTION_NAME; // WAWebDisableLinkPreviewsSync.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebDisableLinkPreviewsSync — constructor field
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     *           which is {@code "regular"}
     */
    @Override
    public SyncPatchType collectionName() {
        return PrivacySettingDisableLinkPreviewsAction.COLLECTION_NAME; // WAWebDisableLinkPreviewsSync.collectionName
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebDisableLinkPreviewsSync.getVersion — returns {@code 8}
     */
    @Override
    public int version() {
        return PrivacySettingDisableLinkPreviewsAction.ACTION_VERSION; // WAWebDisableLinkPreviewsSync.getVersion
    }

    /**
     * Applies a single disable link previews mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebDisableLinkPreviewsSync.applyMutations — WA Web returns
     *           {@code SyncActionState} values directly; Cobalt wraps in
     *           {@link MutationApplicationResult} for type safety
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebDisableLinkPreviewsSync.applyMutations
    }

    /**
     * {@inheritDoc}
     *
     * <p>Per WhatsApp Web {@code WAWebDisableLinkPreviewsSync.applyMutations}: iterates
     * all mutations, accumulating the last valid {@code isPreviewsDisabled} value from
     * SET operations. Non-SET operations are counted and logged, returning
     * {@code Unsupported}. Mutations where {@code isPreviewsDisabled} is {@code null}
     * are counted and logged, returning {@code Malformed} via
     * {@code WAWebSyncdIndexUtils.malformedActionValue}. After iteration,
     * persists the accumulated value once via
     * {@code WAWebDisableLinkPreviewsAction.setDisableLinkPreviewsToUserPrefs}.
     *
     * @implNote WAWebDisableLinkPreviewsSync.applyMutations
     * @param client    the WhatsApp client instance
     * @param mutations the batch of mutations to apply
     * @return a list of results parallel to the input
     */
    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        if (mutations.isEmpty()) {
            return List.of();
        }

        Boolean lastValid = null; // WAWebDisableLinkPreviewsSync.applyMutations: var r
        var results = new ArrayList<MutationApplicationResult>(mutations.size()); // WAWebDisableLinkPreviewsSync.applyMutations: var a = [], c = t.map(function(e) { ... })
        for (var mutation : mutations) { // WAWebDisableLinkPreviewsSync.applyMutations: t.map(function(e) { ... })
            if (mutation.operation() != SyncdOperation.SET) { // WAWebDisableLinkPreviewsSync.applyMutations: if (e.operation !== "set") return i++, ... {actionState: Unsupported}
                results.add(MutationApplicationResult.unsupported()); // WAWebDisableLinkPreviewsSync.applyMutations: {actionState: SyncActionState.Unsupported}
                continue;
            }

            if (mutation.value().action().orElse(null) instanceof PrivacySettingDisableLinkPreviewsAction action) { // WAWebDisableLinkPreviewsSync.applyMutations: var s = (t = e.value.privacySettingDisableLinkPreviewsAction) == null ? void 0 : t.isPreviewsDisabled
                lastValid = action.isPreviewsDisabled(); // WAWebDisableLinkPreviewsSync.applyMutations: r = s
                results.add(MutationApplicationResult.success()); // WAWebDisableLinkPreviewsSync.applyMutations: {actionState: SyncActionState.Success}
            } else {
                results.add(MutationApplicationResult.malformed()); // WAWebDisableLinkPreviewsSync.applyMutations: malformedActionValue(n.collectionName)
            }
        }

        if (lastValid != null) { // WAWebDisableLinkPreviewsSync.applyMutations: r != null
            client.store().setDisableLinkPreviews(lastValid); // ADAPTED: WAWebDisableLinkPreviewsAction.setDisableLinkPreviewsToUserPrefs(r) -> direct store call
        }

        return results; // WAWebDisableLinkPreviewsSync.applyMutations: return c
    }

    /**
     * Applies a single disable link previews mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebDisableLinkPreviewsSync.applyMutations}
     * (single-mutation path within the batch):
     * <ol>
     *   <li>If the operation is not {@code SET}, returns {@code Unsupported}.</li>
     *   <li>Extracts {@code isPreviewsDisabled} from the
     *       {@code privacySettingDisableLinkPreviewsAction}. If the action or
     *       value is {@code null}, returns {@code Malformed}.</li>
     *   <li>Persists the value via
     *       {@code WAWebDisableLinkPreviewsAction.setDisableLinkPreviewsToUserPrefs}
     *       and returns {@code Success}.</li>
     * </ol>
     *
     * @implNote WAWebDisableLinkPreviewsSync.applyMutations (single-mutation semantics)
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebDisableLinkPreviewsSync.applyMutations: if (e.operation !== "set")
            return MutationApplicationResult.unsupported(); // WAWebDisableLinkPreviewsSync.applyMutations: {actionState: SyncActionState.Unsupported}
        }

        if (!(mutation.value().action().orElse(null) instanceof PrivacySettingDisableLinkPreviewsAction action)) { // WAWebDisableLinkPreviewsSync.applyMutations: (t = e.value.privacySettingDisableLinkPreviewsAction) == null
            return MutationApplicationResult.malformed(); // WAWebDisableLinkPreviewsSync.applyMutations: malformedActionValue(n.collectionName)
        }

        client.store().setDisableLinkPreviews(action.isPreviewsDisabled()); // ADAPTED: WAWebDisableLinkPreviewsAction.setDisableLinkPreviewsToUserPrefs(s) -> direct store call
        return MutationApplicationResult.success(); // WAWebDisableLinkPreviewsSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Builds a pending SET mutation for the disable link previews setting.
     *
     * <p>Per WhatsApp Web {@code WAWebDisableLinkPreviewsSync.getMutation}:
     * <ol>
     *   <li>Wraps the value in a {@code privacySettingDisableLinkPreviewsAction}
     *       object: {@code {isPreviewsDisabled: n}}</li>
     *   <li>Delegates to {@code WAWebSyncdActionUtils.buildPendingMutation} with
     *       collection={@code Regular}, indexArgs={@code []},
     *       operation={@code SET}, version={@code 8},
     *       action={@code "setting_disableLinkPreviews"}</li>
     * </ol>
     *
     * @implNote WAWebDisableLinkPreviewsSync.getMutation
     * @param timestamp          the mutation timestamp
     * @param isPreviewsDisabled whether link previews should be disabled
     * @return the pending mutation ready for sync upload
     */
    public SyncPendingMutation getMutation(Instant timestamp, boolean isPreviewsDisabled) {
        var action = new PrivacySettingDisableLinkPreviewsActionBuilder() // WAWebDisableLinkPreviewsSync.getMutation: {privacySettingDisableLinkPreviewsAction: {isPreviewsDisabled: n}}
                .isPreviewsDisabled(isPreviewsDisabled) // WAWebDisableLinkPreviewsSync.getMutation: isPreviewsDisabled: n
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: t
                .privacySettingDisableLinkPreviewsAction(action) // WAWebDisableLinkPreviewsSync.getMutation: value: {privacySettingDisableLinkPreviewsAction: {...}}
                .build();
        var index = JSON.toJSONString(List.of(actionName())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = []
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index, // WAWebSyncdActionUtils.buildPendingMutation: index
                value, // WAWebSyncdActionUtils.buildPendingMutation: binarySyncAction
                SyncdOperation.SET, // WAWebDisableLinkPreviewsSync.getMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp, // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                version() // WAWebSyncdActionUtils.buildPendingMutation: version: this.getVersion()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
