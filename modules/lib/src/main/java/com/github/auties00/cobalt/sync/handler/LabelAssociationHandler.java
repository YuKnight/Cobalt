package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.preference.Label;
import com.github.auties00.cobalt.model.preference.LabelBuilder;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelAssociationAction;
import com.github.auties00.cobalt.model.sync.action.contact.LabelAssociationActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.List;

/**
 * Handles label JID association sync actions.
 *
 * <p>This handler processes mutations that associate chat/contact JIDs with
 * labels (the "label_jid" action). It corresponds to the WhatsApp Web module
 * {@code WAWebLabelJidSync} which extends {@code ChatOrContactSyncdActionBase}
 * with {@code chatJidIndex = 2} and collection {@code Regular}.
 *
 * <p>Index format: {@code ["label_jid", "labelId", "chatJid"]}
 *
 * <p>On a {@code SET} operation with {@code labeled = true}, the {@code chatJid}
 * is added to the {@code labelId}'s association set. With {@code labeled = false}
 * (or absent), the association is removed. Non-{@code SET} operations are
 * reported as unsupported. Per WhatsApp Web, the {@code chatJid} is validated
 * as a WID, resolved to a chat via {@code resolveChatForMutationIndex}, and
 * (when LID 1:1 migration is active) a LID target is transparently mapped to
 * the user's phone number JID.
 *
 * @implNote WAWebLabelJidSync.default, module exports a singleton {@code new c()}
 *           where {@code c} is the handler class extending
 *           {@code ChatOrContactSyncdActionBase}
 */
@WhatsAppWebModule(moduleName = "WAWebLabelJidSync")
public final class LabelAssociationHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code LabelAssociationHandler}.
     *
     * @implNote WAWebLabelJidSync.default, the module exports a singleton
     *           {@code d = new c()} where {@code c} is the handler class
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final LabelAssociationHandler INSTANCE = new LabelAssociationHandler();

    /**
     * Index of the chat JID within the mutation {@code indexParts} array.
     *
     * <p>Per WhatsApp Web, {@code WAWebLabelJidSync} sets {@code chatJidIndex = 2}
     * in its constructor. This is used by the base class
     * ({@code ChatOrContactSyncdActionBase}) for cross-index conflict detection
     * and chat JID extraction.
     *
     * @implNote WAWebLabelJidSync constructor, {@code e.chatJidIndex = 2}
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    private static final int CHAT_JID_INDEX = 2;

    /**
     * Creates a new {@code LabelAssociationHandler}.
     *
     * @implNote WAWebLabelJidSync.default, constructor sets
     *           {@code chatJidIndex = 2} and
     *           {@code collectionName = WASyncdConst.CollectionName.Regular}
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private LabelAssociationHandler() {

    }

    /**
     * Returns the action name for label JID sync.
     *
     * @implNote WAWebLabelJidSync.getAction, returns
     *           {@code WASyncdConst.Actions.LabelJid} which is {@code "label_jid"}
     * @return the action name string
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return LabelAssociationAction.ACTION_NAME;
    }

    /**
     * Returns the sync collection for this handler.
     *
     * @implNote WAWebLabelJidSync constructor, sets
     *           {@code collectionName = WASyncdConst.CollectionName.Regular}
     * @return the {@link SyncPatchType#REGULAR} collection
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return LabelAssociationAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for label JID sync.
     *
     * @implNote WAWebLabelJidSync.getVersion, returns
     *           {@code WASyncdConst.LABEL_ASSOCIATION_SYNC_VERSION} which is {@code 3}
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return LabelAssociationAction.ACTION_VERSION;
    }

    /**
     * Applies a single label JID mutation.
     *
     * @implNote WAWebLabelJidSync.applyMutations, delegates to
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     *           and checks for success
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a single label JID mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebLabelJidSync.applyMutations}, for each
     * mutation:
     * <ol>
     *   <li>If {@code operation !== "set"}, returns {@code Unsupported}</li>
     *   <li>Extracts {@code labelId = indexParts[1]} and {@code chatJid = indexParts[2]};
     *       if either is empty, returns {@code malformedActionIndex}</li>
     *   <li>Reads {@code labeled = value.labelAssociationAction.labeled}. Per Cobalt
     *       project convention, a {@code null} value is coalesced to {@code false}
     *       (WA Web returns {@code malformedActionValue} in that case)</li>
     *   <li>Validates the {@code chatJid} string is a WID; if not, returns
     *       {@code malformedActionIndex}</li>
     *   <li>Resolves the chat via {@code resolveChatForMutationIndex}. If the
     *       chat is not found and LID 1:1 migration is active and the target is
     *       a LID, attempts to resolve the LID to a phone number JID</li>
     *   <li>If {@code labeled} is {@code true}, adds the {@code chatJid} to the
     *       label's association set (creating the label stub if absent). If
     *       {@code false}, removes the association</li>
     * </ol>
     *
     * <p>After the batch is processed, WA Web calls
     * {@code WAWebDBLabelAssociationDatabaseApi.removeLabelAssociations(removals)} and
     * {@code addOrEditLabelAssociations(additions)} to persist the changes, then fires
     * {@code frontendFireAndForget("applyLabelAssociationChanges", ...)}. In Cobalt,
     * the association is stored directly inside the {@link Label} object, so there
     * is no separate database update — assignments are updated inline.
     *
     * <p>Per WhatsApp Web, when the label is a predefined "Detected Outcome"
     * label ({@code DO_NEW_ORDER} or {@code DO_LEAD}) and detected outcome
     * onboarding is enabled, a business automation notification system message
     * is generated. In Cobalt, that UI/notification feature is skipped.
     *
     * <p>WA Web also emits WAM label-sync telemetry events via
     * {@code WAWebWamLabelSyncTrackingReporter}. In Cobalt, WAM telemetry is
     * intentionally omitted.
     *
     * @implNote WAWebLabelJidSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        try {
            if (mutation.operation() != SyncdOperation.SET) {
                return MutationApplicationResult.unsupported();
            }

            var indexArray = JSON.parseArray(mutation.index());
            var labelId = indexArray.getString(1);
            var targetJidString = indexArray.getString(CHAT_JID_INDEX);
            if (labelId == null || labelId.isEmpty() || targetJidString == null || targetJidString.isEmpty()) {
                return malformedActionIndex();
            }

            if (!(mutation.value().action().orElse(null) instanceof LabelAssociationAction action)) {
                return malformedActionValue();
            }

            // ADAPTED: WAWebLabelJidSync.applyMutations, var g = (t = s.labelAssociationAction) == null ? void 0 : t.labeled; if (g == null) { p++; ... return malformedActionValue(a.collectionName) }
            // Cobalt's LabelAssociationAction.labeled() coalesces null Boolean to false per project convention,
            // so the null-vs-false distinction is intentionally lost here; a null labeled is treated as "remove".
            var labeled = action.labeled();

            Jid targetJid;
            try {
                targetJid = Jid.of(targetJidString);
            } catch (Exception jidError) {
                return malformedActionIndex();
            }
            if (targetJid == null) {
                return malformedActionIndex();
            }

            // ADAPTED: WAWebLabelJidSync.applyMutations, var v = yield o("WAWebSyncdGetChat").resolveChatForMutationIndex(b); if (v.success === true) b = createWid(v.chat.id); else if (isLidMigrated() && b.isLid()) { var S = getPhoneNumber(b); S != null && (b = S) }
            // Cobalt collapses the multi-strategy chat lookup into findChatByJid + LID-to-PN mapping fallback.
            var resolvedTargetJid = targetJid;
            var resolvedChat = client.store().findChatByJid(targetJid);
            if (resolvedChat.isPresent()) {
                resolvedTargetJid = resolvedChat.get().toJid();
            } else if (targetJid.hasLidServer()) {
                var phoneJid = client.store().getPhoneNumberByLid(targetJid);
                if (phoneJid.isPresent()) {
                    resolvedTargetJid = phoneJid.get();
                }
            }

            // ADAPTED: WAWebLabelJidSync.applyMutations, WA Web stores label associations in a separate LabelAssociation table; Cobalt stores them inside the Label object's assignments set.
            // If the label does not exist locally, we create a stub to hold the association (WA Web's addOrEditLabelAssociations would add the record even if the label row is absent).
            var label = client.store()
                    .findLabel(labelId) // ADAPTED: WAWebLabelJidSync.applyMutations, getLabelTable().get(u) is used in WA Web only to read predefinedId for biz notifications; Cobalt reuses it to hold assignments
                    .orElseGet(() -> {
                        var newLabel = new LabelBuilder()
                                .id(labelId)
                                .name("")
                                .color(0)
                                .build();
                        client.store().addLabel(newLabel);
                        return newLabel;
                    });

            if (labeled) {
                label.addAssignment(resolvedTargetJid);
            } else {
                label.removeAssignment(resolvedTargetJid);
            }

            return MutationApplicationResult.success();
        } catch (Exception e) {
            return MutationApplicationResult.failed();
        }
    }

    /**
     * Builds a pending SET mutation for associating (or disassociating) a
     * label with a chat/contact JID.
     *
     * <p>Per WhatsApp Web {@code WAWebLabelJidSync.createLabelAssociationMutations}
     * the association is emitted as a {@link LabelAssociationAction} whose
     * {@code labeled} flag maps {@code true} to "add" and {@code false} to
     * "remove". The mutation index is
     * {@code ["label_jid", labelId, targetJid]} where {@code targetJid} is
     * the canonical chat/contact index JID (callers should pre-resolve LID
     * 1x1 migration when applicable).
     *
     * <p>WAM telemetry ({@code WAWebWamLabelSyncTrackingReporter}) is
     * intentionally omitted in Cobalt.
     *
     * @implNote WAWebLabelJidSync.createLabelAssociationMutations
     * @param labelId   the label identifier
     * @param targetJid the chat or contact JID the label is being applied to
     * @param labeled   {@code true} to add the association, {@code false} to remove
     * @param timestamp the mutation timestamp
     * @return the pending mutation for the label association
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelJidSync", exports = "createLabelAssociationMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation createLabelAssociationMutation(
            String labelId,
            Jid targetJid,
            boolean labeled,
            Instant timestamp
    ) {
        var action = new LabelAssociationActionBuilder()
                .labeled(labeled)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .labelAssociationAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName(), labelId, targetJid.toString()));
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // ADAPTED: WA Web returns the raw pending mutation; Cobalt wraps it in SyncPendingMutation
    }
}
