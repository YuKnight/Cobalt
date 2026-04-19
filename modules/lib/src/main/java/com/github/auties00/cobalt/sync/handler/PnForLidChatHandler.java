package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.PnForLidChatAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles the {@code pn_for_lid_chat} app state sync action, which carries a
 * phone number JID to be associated with an existing LID JID for a chat.
 *
 * <p>Per WhatsApp Web {@code WAWebPnForLidChatSync}, the action's index is
 * shaped as {@code ["pnForLidChat", lidJid]} and the value payload contains a
 * {@code pnForLidChatAction} with a single {@code pnJid} field. For every
 * {@code SET} mutation, the handler extracts the LID (from the index) and the
 * phone number JID (from the action value) and registers a bidirectional
 * mapping between the two.
 *
 * <p>Unlike WhatsApp Web, which accumulates all valid pairs in a batch and
 * calls {@code WAWebDBCreateLidPnMappings.createLidPnMappings} once with
 * {@code flushImmediately: true, learningSource: "other"}, Cobalt registers
 * each mapping directly on the {@code WhatsAppStore} as the mutations arrive.
 * The end result is semantically equivalent because the store already persists
 * bidirectional {@code phoneJid <-> lidJid} entries.
 *
 * @implNote WAWebPnForLidChatSync
 */
@WhatsAppWebModule(moduleName = "WAWebPnForLidChatSync")
public final class PnForLidChatHandler implements WebAppStateActionHandler {
    /**
     * Canonical stateless instance of this handler.
     *
     * <p>Per WhatsApp Web {@code WAWebPnForLidChatSync}, the default export is a
     * single {@code new d(...)} instance registered with the sync action
     * registry; Cobalt exposes the same singleton via this constant.
     *
     * @implNote WAWebPnForLidChatSync: {@code m=new d;l.default=m}
     */
    @WhatsAppWebExport(moduleName = "WAWebPnForLidChatSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final PnForLidChatHandler INSTANCE = new PnForLidChatHandler();

    /**
     * Constructs the singleton handler instance.
     *
     * <p>Kept {@code private} so that all callers go through {@link #INSTANCE}.
     *
     * @implNote WAWebPnForLidChatSync: the default export is a single
     *           {@code new d} instance; Cobalt mirrors this by exposing only
     *           {@link #INSTANCE} and disallowing external construction.
     */
    @WhatsAppWebExport(moduleName = "WAWebPnForLidChatSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private PnForLidChatHandler() {

    }

    /**
     * Returns the action name routed to this handler.
     *
     * @implNote WAWebPnForLidChatSync.getAction:
     *           {@code WASyncdConst.Actions.PnForLidChat} ({@code "pnForLidChat"})
     * @return the action identifier {@code "pnForLidChat"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPnForLidChatSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return PnForLidChatAction.ACTION_NAME; // WAWebPnForLidChatSync.getAction -> WASyncdConst.Actions.PnForLidChat
    }

    /**
     * Returns the sync collection this handler operates on.
     *
     * @implNote WAWebPnForLidChatSync: {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     * @return {@link SyncPatchType#REGULAR}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPnForLidChatSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return PnForLidChatAction.COLLECTION_NAME; // WAWebPnForLidChatSync: collectionName = WASyncdConst.CollectionName.Regular
    }

    /**
     * Returns the mutation format version implemented by this handler.
     *
     * @implNote WAWebPnForLidChatSync.getVersion: {@code return 8}
     * @return {@code 8}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPnForLidChatSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return PnForLidChatAction.ACTION_VERSION; // WAWebPnForLidChatSync.getVersion: return 8
    }

    /**
     * Applies a single {@code pnForLidChat} mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} only when the action state is {@link SyncActionState#SUCCESS},
     * mirroring WhatsApp Web's per-mutation success flag.
     *
     * @implNote WAWebPnForLidChatSync.applyMutations — per-mutation success branch
     *           returning {@code {actionState: SyncActionState.Success}}
     * @param client   the WhatsApp client that owns the mutation source
     * @param mutation the decrypted, trusted mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false}
     *         for unsupported or malformed mutations
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPnForLidChatSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebPnForLidChatSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Applies a single {@code pnForLidChat} mutation and returns the detailed
     * {@link MutationApplicationResult}.
     *
     * <p>Order of validation, matching {@code WAWebPnForLidChatSync.applyMutations}:
     * <ol>
     *   <li>If the {@code pnh_pn_for_lid_chat_sync} A/B prop is not {@code true},
     *       the mutation is reported as {@link MutationApplicationResult#unsupported() unsupported}.</li>
     *   <li>If the mutation is not a {@link SyncdOperation#SET}, it is reported
     *       as unsupported.</li>
     *   <li>If the index array lacks a second element, or the second element is
     *       not a valid LID JID, it is reported via
     *       {@link #malformedActionIndex()} (mirroring WhatsApp Web's
     *       {@code isWidlike(indexParts[1])} guard and the subsequent
     *       {@code createUserLidOrThrow} which would throw if the string is not
     *       a LID).</li>
     *   <li>If the action payload is not a {@link PnForLidChatAction}, the
     *       mutation is reported via {@link #malformedActionValue()}.</li>
     *   <li>If the action's {@code pnJid} is absent, it is reported via
     *       {@link #malformedActionValue()}, mirroring WhatsApp Web's
     *       {@code isWidlike(pnForLidChatAction.pnJid)} guard.</li>
     * </ol>
     *
     * <p>When every check passes, the handler registers a bidirectional
     * {@code phoneJid <-> lidJid} mapping on the {@code WhatsAppStore}. This
     * corresponds to WhatsApp Web's
     * {@code createLidPnMappings({mappings, flushImmediately: true, learningSource: "other"})}
     * call issued at the end of {@code applyMutations}; the frontend-only
     * {@code bulkUpdatePhoneNumberJids} call and the identity-change side-effect
     * are intentionally not replicated, as Cobalt's store is the sole source of
     * truth.
     *
     * @implNote WAWebPnForLidChatSync.applyMutations
     * @param client   the WhatsApp client whose store receives the mapping update
     * @param mutation the decrypted, trusted mutation to apply
     * @return the detailed {@link MutationApplicationResult}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPnForLidChatSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // WAWebPnForLidChatSync.applyMutations: if (getABPropConfigValue("pnh_pn_for_lid_chat_sync") !== true) return all Unsupported
        if (!client.abPropsService().getBool(ABProp.PNH_PN_FOR_LID_CHAT_SYNC)) {
            return MutationApplicationResult.unsupported(); // WAWebPnForLidChatSync: {actionState: SyncActionState.Unsupported}
        }

        // WAWebPnForLidChatSync: if (e.operation !== "set") return {actionState: SyncActionState.Unsupported}
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported(); // WAWebPnForLidChatSync: a++, {actionState: SyncActionState.Unsupported}
        }

        // WAWebPnForLidChatSync: var s = e.indexParts[1]
        var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: Cobalt parses the raw JSON index string; WA Web's WAWebSyncdMutationParser already exposes indexParts
        var lidJidString = indexArray != null && indexArray.size() > 1 ? indexArray.getString(1) : null; // ADAPTED: out-of-bounds access in WA Web returns undefined, which fails isWidlike
        // WAWebPnForLidChatSync: if (!isWidlike(s)) return i++, n.malformedActionIndex()
        if (lidJidString == null || lidJidString.isEmpty()) {
            return malformedActionIndex(); // WAWebPnForLidChatSync: i++, return n.malformedActionIndex()
        }

        // WAWebPnForLidChatSync: var u = (t = e.value.pnForLidChatAction) == null ? void 0 : t.pnJid
        if (!(mutation.value().action().orElse(null) instanceof PnForLidChatAction action)) {
            return malformedActionValue(); // WAWebPnForLidChatSync: l++, return malformedActionValue(n.collectionName)
        }

        var pnJid = action.pnJid().orElse(null);
        // WAWebPnForLidChatSync: if (u == null || !isWidlike(u)) return l++, malformedActionValue(n.collectionName)
        if (pnJid == null) {
            return malformedActionValue(); // WAWebPnForLidChatSync: l++, return malformedActionValue(n.collectionName)
        }

        // WAWebPnForLidChatSync: var d = createUserLidOrThrow(s)
        // ADAPTED: WA Web splits the LID check into isWidlike (above) then createUserLidOrThrow
        // which throws if the string is not a @lid JID. Cobalt collapses both into hasLidServer()
        // and reports a non-LID widlike string as malformedActionIndex rather than propagating
        // an exception, preserving the underlying MALFORMED semantic.
        var lidJid = Jid.of(lidJidString);
        if (!lidJid.hasLidServer()) {
            return malformedActionIndex(); // WAWebPnForLidChatSync: createUserLidOrThrow would throw — modelled as malformedActionIndex
        }

        // WAWebPnForLidChatSync: var c = createUserWidOrThrow(u); r.push({lid: d, pn: c})
        // WAWebPnForLidChatSync: yield createLidPnMappings({mappings: r, flushImmediately: true, learningSource: "other"})
        // ADAPTED: WA Web accumulates pairs and flushes once at the end of the batch;
        // Cobalt writes each pair directly to the store, which is the sole source of truth.
        client.store().registerLidMapping(pnJid, lidJid);
        return MutationApplicationResult.success(); // WAWebPnForLidChatSync: {actionState: SyncActionState.Success}
    }
}
