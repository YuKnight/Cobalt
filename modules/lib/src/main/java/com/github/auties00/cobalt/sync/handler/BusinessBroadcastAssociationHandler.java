package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.BroadcastListParticipantAction;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastAssociationAction;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastListAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles business broadcast association sync actions ({@code "broadcast_jid"}).
 *
 * <p>Each mutation associates (SET) or disassociates (REMOVE / {@code deleted=true})
 * a single recipient JID with a business broadcast list. The recipient is stored
 * inside the parent {@link BusinessBroadcastListAction} via its
 * {@link BroadcastListParticipantAction} list. When SET, the handler appends a
 * participant entry, taking care to populate {@code lidJid} for LID/HostedLID
 * recipients and {@code pnJid} (with the resolved LID via
 * {@code WhatsAppStore.getLidByPhoneNumber}) for phone-number recipients.
 * When REMOVE (or when the action carries {@code deleted=true}), the handler
 * strips any participant whose {@code lidJid} or {@code pnJid} matches the
 * recipient.
 *
 * <p>Index format: {@code ["broadcast_jid", broadcastListId, recipientJid]}
 *
 * <p><b>NO_WA_BASIS:</b> The {@code "broadcast_jid"} action is registered as
 * {@code WASyncdConst.Actions.BroadcastJid} and the
 * {@code SyncActionValue.BusinessBroadcastAssociationAction} protobuf is defined
 * in {@code WAWebProtobufSyncAction.pb} (with a single {@code deleted: bool}
 * field), but the current WA Web snapshot does <em>not</em> ship a corresponding
 * sync handler module. The action is also absent from
 * {@code WAWebCollectionHandlerActions.ActionHandlers}, so WA Web would not
 * dispatch any incoming mutation with this action. The Cobalt handler is a
 * forward-looking implementation: it follows the conventions of the closely
 * related {@code WAWebBroadcastListSync} (which manages the parent broadcast
 * list and its participants) and the canonical Cobalt index format
 * {@code [actionName, ...indexArgs]}, but every behavioural step here is
 * Cobalt-inferred until WA Web ships the matching {@code WAWebBroadcastJidSync}
 * module.
 *
 * @implNote NO_WA_BASIS: no WA Web sync handler exists for
 *           {@code WASyncdConst.Actions.BroadcastJid}; only the action constant
 *           and the {@code SyncActionValue.BusinessBroadcastAssociationAction}
 *           protobuf shape are present in
 *           {@code WAWebProtobufSyncAction.pb}. Closest WA Web reference for
 *           the surrounding model is {@code WAWebBroadcastListSync.applyMutations}
 *           which mutates the broadcast list participants in bulk.
 */
@WhatsAppWebModule(moduleName = "WAWebBroadcastListSync")
public final class BusinessBroadcastAssociationHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code BusinessBroadcastAssociationHandler}.
     *
     * @implNote NO_WA_BASIS: WA Web has no sync handler module for
     *           {@code "broadcast_jid"}; the singleton mirrors the
     *           {@code l.default = new u()} pattern used by every other Cobalt
     *           sync handler.
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final BusinessBroadcastAssociationHandler INSTANCE = new BusinessBroadcastAssociationHandler();

    /**
     * Private constructor to enforce the singleton pattern.
     *
     * @implNote NO_WA_BASIS: no WA Web counterpart constructor; mirrors the
     *           Cobalt handler convention.
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private BusinessBroadcastAssociationHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: returns the canonical
     *           {@code WASyncdConst.Actions.BroadcastJid} value
     *           ({@code "broadcast_jid"}) declared in {@code WASyncdConst}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getAction", adaptation = WhatsAppAdaptation.ADAPTED)
    public String actionName() {
        return BusinessBroadcastAssociationAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web does not declare a collection for this
     *           action since no handler module exists; Cobalt assigns
     *           {@code REGULAR} to match the related
     *           {@code WAWebBroadcastListSync} which uses
     *           {@code WASyncdConst.CollectionName.Regular}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPatchType collectionName() {
        return BusinessBroadcastAssociationAction.COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web has no version constant for this action;
     *           Cobalt defaults to {@code 1} matching every other unmigrated
     *           sync action handler.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getVersion", adaptation = WhatsAppAdaptation.ADAPTED)
    public int version() {
        return BusinessBroadcastAssociationAction.ACTION_VERSION;
    }

    /**
     * Applies a business broadcast association mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and reports {@code true} only when the resolved
     * {@link MutationApplicationResult#actionState()} is
     * {@link SyncActionState#SUCCESS}.
     *
     * @implNote ADAPTED: WAWebBroadcastListSync.applyMutations — WA Web returns
     *           {@code WASyncdConst.SyncActionState} per mutation; Cobalt's
     *           {@link WebAppStateActionHandler} contract uses a boolean return.
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a business broadcast association mutation and returns a detailed result.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only SET mutations
     *       are accepted; the {@code deleted} flag inside the action payload
     *       distinguishes additions from removals.</li>
     *   <li>Parse {@code mutation.index()} as a JSON array; require at least
     *       three elements. {@code index[1]} is the broadcast list id and
     *       {@code index[2]} is the recipient JID string. Return
     *       {@link MutationApplicationResult#malformed()} when the array is
     *       too short or either field is blank.</li>
     *   <li>Resolve the action payload to a
     *       {@link BusinessBroadcastAssociationAction}; return
     *       {@link MutationApplicationResult#malformed()} when missing or of
     *       the wrong type.</li>
     *   <li>Look up the parent {@link BusinessBroadcastListAction} in
     *       {@code client.store().businessBroadcastLists()}; if absent return
     *       {@link MutationApplicationResult#orphan(String, String)} with the
     *       missing list id and {@code "BroadcastList"} model type.</li>
     *   <li>Remove any existing participant whose {@code lidJid} or {@code pnJid}
     *       equals the recipient.</li>
     *   <li>If the action is <em>not</em> deleted, append a fresh
     *       {@link BroadcastListParticipantAction}: when the recipient JID has a
     *       LID or HostedLID server, populate {@code lidJid}; otherwise populate
     *       {@code pnJid} and resolve {@code lidJid} via
     *       {@code WhatsAppStore.getLidByPhoneNumber}, falling back to the
     *       phone-number JID itself.</li>
     *   <li>Persist the mutated participant list back into the broadcast list
     *       and write the parent map to the store.</li>
     * </ol>
     *
     * @implNote NO_WA_BASIS: no WA Web sync handler implements
     *           {@code "broadcast_jid"}. The closest reference is
     *           {@code WAWebBroadcastListSync.applyMutations} which mutates
     *           {@code BusinessBroadcastListAction.participants} in bulk via
     *           {@code WAWebBroadcastListStorageUtils.updateBroadcastListStorage}.
     *           Cobalt's per-recipient SET/delete shape is inferred from the
     *           protobuf ({@code deleted: bool}) and the
     *           {@code SyncActionValue.BroadcastListParticipant} layout.
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: WAWebBroadcastListSync.applyMutations: var t = e.indexParts (pre-parsed in WA Web)
        if (indexArray.size() < 3) {
            return MutationApplicationResult.malformed();
        }

        var listId = indexArray.getString(1);
        var recipientJidString = indexArray.getString(2);
        if (listId == null || listId.isBlank() || recipientJidString == null || recipientJidString.isBlank()) { // ADAPTED: WAWebBroadcastListSync.applyMutations: if (!n) return r.malformedActionIndex()
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof BusinessBroadcastAssociationAction action)) { // ADAPTED: WAWebBroadcastListSync.applyMutations: if (!u) return malformedActionValue(...)
            return MutationApplicationResult.malformed();
        }

        // ADAPTED: WAWebBroadcastListSync resolves the parent broadcast list via
        // ConcurrentHashMap on WhatsAppStore.
        var lists = new HashMap<>(client.store().businessBroadcastLists());
        var broadcastList = lists.get(listId);
        if (broadcastList == null) {
            return MutationApplicationResult.orphan(listId, "BroadcastList");
        }

        var recipientJid = Jid.of(recipientJidString);
        var participants = new ArrayList<>(broadcastList.participants()); // ADAPTED: WAWebBroadcastListSync uses BroadcastListParticipantAction[]; Cobalt copies to a mutable list before mutating
        participants.removeIf(participant ->
                recipientJid.equals(participant.lidJid())
                        || participant.pnJid().filter(recipientJid::equals).isPresent()
        );
        if (!action.deleted()) {
            var participant = new BroadcastListParticipantAction();
            if (recipientJid.hasLidServer() || recipientJid.hasHostedLidServer()) {
                participant.setLidJid(recipientJid);
            } else {
                participant.setPnJid(recipientJid);
                participant.setLidJid(client.store().getLidByPhoneNumber(recipientJid).orElse(recipientJid)); // ADAPTED: WhatsAppStore.getLidByPhoneNumber resolves the LID/PN map maintained by Cobalt
            }
            participants.add(participant);
        }

        broadcastList.setParticipants(participants); // ADAPTED: WAWebBroadcastListSync writes participants via WAWebBroadcastListStorageUtils.updateBroadcastListStorage
        lists.put(listId, broadcastList);
        client.store().setBusinessBroadcastLists(lists);
        return MutationApplicationResult.success();
    }
}
