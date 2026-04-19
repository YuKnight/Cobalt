package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.message.MessageContainerBuilder;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.model.message.system.ProtocolMessageBuilder;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestMessageBuilder;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.WaffleAccountLinkStateAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles waffle account link state sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebWaffleAccountLinkStateSync}, this handler
 * processes the {@code "waffle_account_link_state"} sync action in the
 * {@code RegularHigh} collection at version {@code 1}. The handler is gated by
 * the {@code web_waffle} AB prop ({@code WAWebAccountLinkingGatingUtils.accountLinkingEnabled}):
 * when disabled, all mutations are acknowledged as {@code UNSUPPORTED} without
 * inspection.
 *
 * <p>Only {@code SET} operations are supported. On {@code SET}, the handler
 * validates that {@code waffleAccountLinkStateAction.linkState} is non-{@code null}
 * and, when processing a batch, applies only the latest mutation by
 * {@code value.timestamp} to the local store. After persisting an
 * {@code Active} link state, the handler triggers a primary-device WAFFLE
 * linking nonce fetch via a peer data operation request.
 *
 * <p>Index format: {@code ["waffle_account_link_state"]}
 *
 * @implNote WAWebWaffleAccountLinkStateSync.default — module-level singleton
 *           {@code d = new c(); l.default = d} where {@code c} extends
 *           {@code WAWebSyncdAction.AccountSyncdActionBase}
 */
@WhatsAppWebModule(moduleName = "WAWebWaffleAccountLinkStateSync")
public final class WaffleAccountLinkStateHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code WaffleAccountLinkStateHandler}.
     *
     * @implNote WAWebWaffleAccountLinkStateSync.default — WA Web exports a single
     *           pre-instantiated handler ({@code d = new c; l.default = d})
     */
    @WhatsAppWebExport(moduleName = "WAWebWaffleAccountLinkStateSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final WaffleAccountLinkStateHandler INSTANCE = new WaffleAccountLinkStateHandler();

    /**
     * Constructs the singleton instance.
     *
     * @implNote WAWebWaffleAccountLinkStateSync — WA Web instantiates the handler
     *           once via {@code new c()}; the constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularHigh}
     */
    @WhatsAppWebExport(moduleName = "WAWebWaffleAccountLinkStateSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private WaffleAccountLinkStateHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebWaffleAccountLinkStateSync.getAction — returns
     *           {@code WASyncdConst.Actions.WaffleAccountLinkState} which equals
     *           {@code "waffle_account_link_state"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebWaffleAccountLinkStateSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return WaffleAccountLinkStateAction.ACTION_NAME; // WAWebWaffleAccountLinkStateSync.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebWaffleAccountLinkStateSync — sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularHigh}
     *           in the constructor
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebWaffleAccountLinkStateSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return WaffleAccountLinkStateAction.COLLECTION_NAME; // WAWebWaffleAccountLinkStateSync constructor: this.collectionName = RegularHigh
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebWaffleAccountLinkStateSync.getVersion — returns the literal {@code 1}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebWaffleAccountLinkStateSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return WaffleAccountLinkStateAction.ACTION_VERSION; // WAWebWaffleAccountLinkStateSync.getVersion: return 1
    }

    /**
     * {@inheritDoc}
     *
     * <p>WhatsApp Web exposes only a batch entry point ({@code applyMutations});
     * Cobalt's interface contract additionally requires a single-mutation path,
     * which is implemented here by delegating to {@link #applyMutationResult}.
     *
     * @implNote ADAPTED: WAWebWaffleAccountLinkStateSync.applyMutations — WA Web
     *           only processes mutations as a batch; the single-mutation entry
     *           point is a Cobalt interface adaptation
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebWaffleAccountLinkStateSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: single-path adapter for batch-only WA Web entry
    }

    /**
     * {@inheritDoc}
     *
     * <p>Per WhatsApp Web {@code WAWebWaffleAccountLinkStateSync.applyMutations}:
     * <ol>
     *   <li>If {@code WAWebAccountLinkingGatingUtils.accountLinkingEnabled()}
     *       returns {@code false}, every mutation is acknowledged as
     *       {@code UNSUPPORTED} and no store work is performed.</li>
     *   <li>Otherwise, each mutation is mapped to a per-mutation
     *       {@link MutationApplicationResult}: non-{@code SET} mutations are
     *       acknowledged as {@code UNSUPPORTED}; mutations whose decoded value
     *       is not a {@link WaffleAccountLinkStateAction} or whose
     *       {@code linkState} is {@code null} are acknowledged as
     *       {@code MALFORMED}.</li>
     *   <li>While mapping, the mutation with the highest {@code value.timestamp}
     *       among the valid {@code SET} mutations is tracked.</li>
     *   <li>After mapping, the latest valid mutation's link state and
     *       timestamp are persisted via
     *       {@code createOrUpdateAccountLinkingState}, and if the persisted
     *       link state is {@code Active} the handler triggers
     *       {@code requestNonceFromPrimary} to fetch a fresh waffle linking
     *       nonce from the primary device.</li>
     * </ol>
     *
     * <p>WA Web also emits {@code WALogger.WARN} entries with the unsupported
     * and malformed mutation counts; these telemetry warnings are intentionally
     * omitted in Cobalt and the return semantics are preserved exactly.
     *
     * @implNote WAWebWaffleAccountLinkStateSync.applyMutations
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebWaffleAccountLinkStateSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        var accountLinkingEnabled = client.abPropsService().getBool(ABProp.WEB_WAFFLE); // WAWebWaffleAccountLinkStateSync.applyMutations: WAWebAccountLinkingGatingUtils.accountLinkingEnabled() -> getABPropConfigValue("web_waffle")
        DecryptedMutation.Trusted latest = null; // WAWebWaffleAccountLinkStateSync.applyMutations: var a
        var results = new ArrayList<MutationApplicationResult>(mutations.size()); // WAWebWaffleAccountLinkStateSync.applyMutations: var u = t.map(...)
        for (var mutation : mutations) { // WAWebWaffleAccountLinkStateSync.applyMutations: t.map(function(e) {...})
            if (!accountLinkingEnabled) { // WAWebWaffleAccountLinkStateSync.applyMutations: accountLinkingEnabled() ? (...) : {actionState: Unsupported}
                results.add(MutationApplicationResult.unsupported()); // WAWebWaffleAccountLinkStateSync.applyMutations: {actionState: Unsupported}
                continue;
            }

            if (mutation.operation() != SyncdOperation.SET) { // WAWebWaffleAccountLinkStateSync.applyMutations: e.operation !== "set"
                results.add(MutationApplicationResult.unsupported()); // WAWebWaffleAccountLinkStateSync.applyMutations: i++, {actionState: Unsupported}
                continue;
            }

            // WAWebWaffleAccountLinkStateSync.applyMutations: ((t = e.value.waffleAccountLinkStateAction) == null ? void 0 : t.linkState) == null
            if (!(mutation.value().action().orElse(null) instanceof WaffleAccountLinkStateAction action)
                    || action.linkState().isEmpty()) {
                results.add(malformedActionValue()); // WAWebWaffleAccountLinkStateSync.applyMutations: l++, WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
                continue;
            }

            if (latest == null || mutation.timestamp().compareTo(latest.timestamp()) > 0) { // WAWebWaffleAccountLinkStateSync.applyMutations: a == null || e.timestamp > a.timestamp
                latest = mutation; // WAWebWaffleAccountLinkStateSync.applyMutations: a = e
            }
            results.add(MutationApplicationResult.success()); // WAWebWaffleAccountLinkStateSync.applyMutations: {actionState: Success}
        }
        // WAWebWaffleAccountLinkStateSync.applyMutations: WALogger.WARN("waffleaccountlinkstate sync: i operations not supported") and WARN("waffleaccountlinkstate sync: l malformed mutations"), skipped, telemetry/logging
        if (latest != null) { // WAWebWaffleAccountLinkStateSync.applyMutations: if (a != null)
            // WAWebWaffleAccountLinkStateSync.applyMutations: var m = mapToAccountLinkState(NULL_THROWS(a.value.waffleAccountLinkStateAction.linkState))
            var action = (WaffleAccountLinkStateAction) latest.value().action().orElseThrow();
            var linkState = action.linkState().orElseThrow();
            // WAWebWaffleAccountLinkStateSync.applyMutations: var p = Number(NULL_THROWS(a.value.timestamp)), a.value.timestamp == mutation.timestamp() in Cobalt
            // WAWebWaffleAccountLinkStateSync.applyMutations: yield this.storeLinkState(m, p) -> u.createOrUpdateAccountLinkingState({accountLinkKey, linkState, linkTimestamp})
            client.store().setWaffleAccountLinkState(linkState); // ADAPTED: WAWebAccountLinkingDBOperations_DO_NOT_USE_DIRECTLY.createOrUpdateAccountLinkingState — Cobalt flattens the account-linking record into store fields
            client.store().setWaffleAccountLinkStateTimestamp(latest.timestamp()); // ADAPTED: createOrUpdateAccountLinkingState linkTimestamp field
            // WAWebWaffleAccountLinkStateSync.applyMutations: m === AccountLinkState.Active && (yield requestNonceFromPrimary())
            if (linkState == WaffleAccountLinkStateAction.AccountLinkState.ACTIVE) {
                requestNonceFromPrimary(client); // WAWebAccountLinkingNonceFetchAPI.requestNonceFromPrimary
            }
        }

        return results; // WAWebWaffleAccountLinkStateSync.applyMutations: return u
    }

    /**
     * {@inheritDoc}
     *
     * <p>Single-mutation adapter that mirrors the WhatsApp Web batch logic for
     * a list of size one. The {@code web_waffle} AB prop is checked first;
     * when disabled, the result is {@code UNSUPPORTED}. A non-{@code SET}
     * mutation yields {@code UNSUPPORTED}; a mutation whose decoded value is
     * not a {@link WaffleAccountLinkStateAction} or whose {@code linkState} is
     * {@code null} yields {@code MALFORMED}; otherwise the link state and
     * timestamp are persisted to the store and {@code SUCCESS} is returned.
     * If the persisted link state is {@code Active}, the handler also triggers
     * {@code requestNonceFromPrimary}.
     *
     * @implNote ADAPTED: WAWebWaffleAccountLinkStateSync.applyMutations — WA Web
     *           only defines a batch entry point; this single-mutation path
     *           applies the same per-mutation logic with no batch-level latest
     *           selection
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebWaffleAccountLinkStateSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.WEB_WAFFLE)) { // WAWebWaffleAccountLinkStateSync.applyMutations: WAWebAccountLinkingGatingUtils.accountLinkingEnabled() == false branch
            return MutationApplicationResult.unsupported(); // WAWebWaffleAccountLinkStateSync.applyMutations: {actionState: Unsupported}
        }

        if (mutation.operation() != SyncdOperation.SET) { // WAWebWaffleAccountLinkStateSync.applyMutations: e.operation !== "set"
            return MutationApplicationResult.unsupported(); // WAWebWaffleAccountLinkStateSync.applyMutations: {actionState: Unsupported}
        }

        // WAWebWaffleAccountLinkStateSync.applyMutations: (e.value.waffleAccountLinkStateAction?.linkState) == null
        if (!(mutation.value().action().orElse(null) instanceof WaffleAccountLinkStateAction action)
                || action.linkState().isEmpty()) {
            return malformedActionValue(); // WAWebWaffleAccountLinkStateSync.applyMutations: WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
        }

        var linkState = action.linkState().orElseThrow(); // WAWebWaffleAccountLinkStateSync.applyMutations: NULL_THROWS(a.value.waffleAccountLinkStateAction.linkState)
        // WAWebWaffleAccountLinkStateSync.applyMutations: yield this.storeLinkState(m, p) -> u.createOrUpdateAccountLinkingState({accountLinkKey, linkState, linkTimestamp})
        client.store().setWaffleAccountLinkState(linkState); // ADAPTED: WAWebAccountLinkingDBOperations_DO_NOT_USE_DIRECTLY.createOrUpdateAccountLinkingState — Cobalt flattens the account-linking record into store fields
        client.store().setWaffleAccountLinkStateTimestamp(mutation.timestamp()); // ADAPTED: createOrUpdateAccountLinkingState linkTimestamp field
        // WAWebWaffleAccountLinkStateSync.applyMutations: m === AccountLinkState.Active && (yield requestNonceFromPrimary())
        if (linkState == WaffleAccountLinkStateAction.AccountLinkState.ACTIVE) {
            requestNonceFromPrimary(client); // WAWebAccountLinkingNonceFetchAPI.requestNonceFromPrimary
        }
        return MutationApplicationResult.success(); // WAWebWaffleAccountLinkStateSync.applyMutations: {actionState: Success}
    }

    /**
     * Sends a {@code WAFFLE_LINKING_NONCE_FETCH} peer data operation request to
     * the primary device.
     *
     * <p>Per WhatsApp Web {@code WAWebAccountLinkingNonceFetchAPI.requestNonceFromPrimary}:
     * delegates to {@code WAWebSendNonMessageDataRequest.sendPeerDataOperationRequest}
     * with the {@code WAFFLE_LINKING_NONCE_FETCH} request type and an empty
     * payload. The send pipeline constructs a single peer protocol message of
     * subtype {@code "peer_data_operation_request_message"} addressed to the
     * primary device (own user, device {@code 0}) and dispatches it via the
     * non-message data request flow.
     *
     * <p>WA Web memoizes the in-flight promise so that concurrent calls share
     * the same request and the cache is cleared in {@code finally}; that
     * micro-optimization is intentionally omitted here because sync mutations
     * are processed sequentially in Cobalt.
     *
     * @param client the WhatsApp client used to dispatch the peer message
     * @implNote WAWebAccountLinkingNonceFetchAPI.requestNonceFromPrimary,
     *           WAWebSendNonMessageDataRequest.sendPeerDataOperationRequest
     */
    @WhatsAppWebExport(moduleName = "WAWebAccountLinkingNonceFetchAPI", exports = "requestNonceFromPrimary", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSendNonMessageDataRequest", exports = "sendPeerDataOperationRequest", adaptation = WhatsAppAdaptation.ADAPTED)
    private void requestNonceFromPrimary(WhatsAppClient client) {
        var me = client.store().jid().orElse(null); // ADAPTED: WAWebSendNonMessageDataRequest.D: getMePnUserOrThrow_DO_NOT_USE() / getMeDevicePnOrThrow_DO_NOT_USE()
        if (me == null) {
            return; // ADAPTED: defensive guard against missing own JID; WA Web throws via getMePnUserOrThrow_DO_NOT_USE
        }

        var request = new PeerDataOperationRequestMessageBuilder() // WAWebSendNonMessageDataRequest.L: var n = {peerDataOperationRequestType: e, requestUrlPreview: [], requestStickerReupload: [], placeholderMessageResendRequest: []}
                .peerDataOperationRequestType(PeerDataOperationRequestType.WAFFLE_LINKING_NONCE_FETCH) // WAWebSendNonMessageDataRequest.L: case Message$PeerDataOperationRequestType.WAFFLE_LINKING_NONCE_FETCH: break (no extra fields)
                .build();
        var protocol = new ProtocolMessageBuilder() // ADAPTED: WAWebSendNonMessageDataRequest wraps in protocol msg via send pipeline
                .type(ProtocolMessage.Type.PEER_DATA_OPERATION_REQUEST_MESSAGE) // WAWebSendNonMessageDataRequest.k: type: "protocol", subtype: "peer_data_operation_request_message"
                .peerDataOperationRequestMessage(request) // WAWebSendNonMessageDataRequest.k: peerDataOperationRequestMessage: e
                .build();
        var container = new MessageContainerBuilder() // ADAPTED: WAWebSendNonMessageDataRequest wraps in message container via send pipeline
                .protocolMessage(protocol)
                .build();
        client.sendMessage(me.withDevice(0), container); // WAWebSendNonMessageDataRequest.k non-fanout path: createDeviceWidFromUserAndDevice(getMeDevicePnOrThrow().user, getMeDevicePnOrThrow().server, 0); WAWebSendAppStateSyncMsgJob.encryptAndSendKeyMsg
    }
}
