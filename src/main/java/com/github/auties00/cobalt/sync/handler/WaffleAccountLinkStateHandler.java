package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.message.MessageContainerBuilder;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.model.message.system.ProtocolMessageBuilder;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestMessageBuilder;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.WaffleAccountLinkStateAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles waffle account link state actions.
 *
 * <p>Index format: ["waffle_account_link_state"]
 */
public final class WaffleAccountLinkStateHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code WaffleAccountLinkStateHandler}.
     */
    public static final WaffleAccountLinkStateHandler INSTANCE = new WaffleAccountLinkStateHandler();

    private WaffleAccountLinkStateHandler() {

    }

    @Override
    public String actionName() {
        return WaffleAccountLinkStateAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return WaffleAccountLinkStateAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return WaffleAccountLinkStateAction.ACTION_VERSION;
    }

    /**
     * Applies a waffle account link state mutation.
     *
     * <p>Per WhatsApp Web (WAWebWaffleAccountLinkStateSync), only SET is supported;
     * non-SET operations are acknowledged as unsupported. On SET, the web client
     * validates that {@code linkState} is non-null, then stores the link state
     * and requests a waffle linking nonce fetch.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was acknowledged, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        DecryptedMutation.Trusted latest = null;
        var results = new ArrayList<MutationApplicationResult>(mutations.size());
        for (var mutation : mutations) {
            if (mutation.operation() != SyncdOperation.SET) {
                results.add(MutationApplicationResult.unsupported());
                continue;
            }

            if (!(mutation.value().action().orElse(null) instanceof WaffleAccountLinkStateAction action)
                    || action.linkState().isEmpty()) {
                results.add(MutationApplicationResult.malformed());
                continue;
            }

            if (latest == null || mutation.timestamp().compareTo(latest.timestamp()) > 0) {
                latest = mutation;
            }
            results.add(MutationApplicationResult.success());
        }

        if (latest != null) {
            applyMutation(client, latest);
        }
        return results;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof WaffleAccountLinkStateAction action)) {
            return MutationApplicationResult.malformed();
        }

        if (action.linkState().isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        client.store().setWaffleAccountLinkState(action.linkState().get());
        client.store().setWaffleAccountLinkStateTimestamp(mutation.timestamp());
        requestNonceFetch(client);
        return MutationApplicationResult.success();
    }

    private void requestNonceFetch(WhatsAppClient client) {
        var me = client.store().jid().orElse(null);
        if (me == null) {
            return;
        }

        var request = new PeerDataOperationRequestMessageBuilder()
                .peerDataOperationRequestType(PeerDataOperationRequestType.WAFFLE_LINKING_NONCE_FETCH)
                .build();
        var protocol = new ProtocolMessageBuilder()
                .type(ProtocolMessage.Type.PEER_DATA_OPERATION_REQUEST_MESSAGE)
                .peerDataOperationRequestMessage(request)
                .build();
        var container = new MessageContainerBuilder()
                .protocolMessage(protocol)
                .build();
        client.sendMessage(me.withDevice(0), container);
    }
}
