package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.call.CallLog;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.call.CallLogAction;
import com.github.auties00.cobalt.model.sync.action.call.CallLogActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import com.alibaba.fastjson2.JSON;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * Handles call log sync actions.
 *
 * <p>This handler processes incoming mutations that create or remove call log
 * records. The action is identified by the {@code "call_log"} action name in
 * {@code SyncActionValue.callLogAction}. The mutation index format is
 * {@code ["call_log", peerJid, callId, fromMe]}.
 *
 * <p>Per WhatsApp Web, this handler extends {@code AccountSyncdActionBase} and
 * stores its mutations in the {@code Regular} collection at version {@code 1}.
 * When a {@code SET} mutation arrives, the handler extracts the
 * {@code callLogRecord} from the action value and delegates to
 * {@code WAWebVoipActionWriteCallLogSync.generateCallLogFromCallSyncRecord} to
 * write it as a VoIP call log message. In Cobalt, the record is stored directly
 * in the {@code callLogStates} map of the store.
 *
 * @implNote WAWebCallLogSync — singleton instance exported as {@code default}
 */
@WhatsAppWebModule(moduleName = "WAWebCallLogSync")
public final class CallLogHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code CallLogHandler}.
     *
     * <p>Per WhatsApp Web, {@code WAWebCallLogSync} exports a single instance
     * ({@code var f = new _(); l.default = f}).
     *
     * @implNote WAWebCallLogSync.default — module-level singleton
     */
    @WhatsAppWebExport(moduleName = "WAWebCallLogSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final CallLogHandler INSTANCE = new CallLogHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebCallLogSync — class constructor (inherits from
     *           {@code AccountSyncdActionBase}, sets
     *           {@code collectionName = WASyncdConst.CollectionName.Regular})
     */
    @WhatsAppWebExport(moduleName = "WAWebCallLogSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private CallLogHandler() {

    }

    /**
     * Returns the action name for call log actions.
     *
     * @implNote WAWebCallLogSync.getAction — returns
     *           {@code WASyncdConst.Actions.CallLog} ({@code "call_log"})
     * @return the action name {@code "call_log"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCallLogSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return CallLogAction.ACTION_NAME; // WAWebCallLogSync.getAction -> WASyncdConst.Actions.CallLog
    }

    /**
     * Returns the sync collection for call log actions.
     *
     * <p>Per WhatsApp Web, the call log handler's {@code collectionName} is set
     * to {@code WASyncdConst.CollectionName.Regular} in the constructor.
     *
     * @implNote WAWebCallLogSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.Regular}
     * @return {@link SyncPatchType#REGULAR}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCallLogSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return CallLogAction.COLLECTION_NAME; // WAWebCallLogSync.collectionName = WASyncdConst.CollectionName.Regular
    }

    /**
     * Returns the mutation format version for call log actions.
     *
     * @implNote WAWebCallLogSync.getVersion — returns {@code 1}
     * @return the version number {@code 1}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCallLogSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return CallLogAction.ACTION_VERSION; // WAWebCallLogSync.getVersion -> 1
    }

    /**
     * Applies a call log mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link SyncActionState#SUCCESS}.
     *
     * @implNote WAWebCallLogSync.applyMutations — per-mutation inner logic,
     *           success check on the returned {@code actionState}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCallLogSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebCallLogSync.applyMutations
    }

    /**
     * Applies a call log mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebCallLogSync.applyMutations}, for each
     * mutation:
     * <ol>
     *   <li>If {@code operation === "set"}:
     *     <ul>
     *       <li>Extracts the {@code callLogAction} and its {@code callLogRecord}</li>
     *       <li>If the record is missing, returns {@code malformedActionValue}</li>
     *       <li>Checks the pairing timestamp and whether the mutation timestamp
     *           is after the pairing time</li>
     *       <li>Computes {@code shouldHideInConversation} based on whether the
     *           mutation happened within one minute</li>
     *       <li>Calls {@code generateCallLogFromCallSyncRecord} to write the log</li>
     *       <li>Returns {@code Success}</li>
     *     </ul>
     *   </li>
     *   <li>If {@code operation === "remove"}: returns {@code Success}</li>
     *   <li>Otherwise: returns {@code Unsupported}</li>
     * </ol>
     *
     * <p>In Cobalt, the call log record is stored in the {@code callLogStates}
     * map keyed by {@code peerJid|callId|fromMe} instead of writing a VoIP call
     * log message to a chat. The pairing timestamp and time-window checks are
     * omitted because they control browser-specific UI behavior (whether to show
     * the call in the conversation view).
     *
     * @implNote ADAPTED: WAWebCallLogSync.applyMutations — Cobalt stores the
     *           {@code CallLog} record in the store instead of calling
     *           {@code WAWebVoipActionWriteCallLogSync.generateCallLogFromCallSyncRecord}.
     *           Pairing timestamp and time-window checks are omitted (browser UI concern).
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCallLogSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try { // WAWebCallLogSync.applyMutations: try/catch wrapping per-mutation logic
            if (mutation.operation() == SyncdOperation.SET) { // WAWebCallLogSync.applyMutations: if (e.operation === "set")
                if (!(mutation.value().action().orElse(null) instanceof CallLogAction action)) { // WAWebCallLogSync.applyMutations: var n = e.value, s = (t = n.callLogAction) != null ? t : {}
                    return malformedActionValue(); // WAWebCallLogSync.applyMutations: return malformedActionValue(r.collectionName)
                }

                var log = action.log().orElse(null); // WAWebCallLogSync.applyMutations: var u = s.callLogRecord
                if (log == null) { // WAWebCallLogSync.applyMutations: if (!u)
                    return malformedActionValue(); // WAWebCallLogSync.applyMutations: return a++, malformedActionValue(r.collectionName)
                }

                // ADAPTED: WA Web checks pairingTimestamp and happenedWithin(timestamp, MINUTE_SECONDS)
                // before calling generateCallLogFromCallSyncRecord. These checks control browser UI
                // behavior (shouldHideInConversation). In Cobalt, we store the log unconditionally.
                // ADAPTED: WA Web calls generateCallLogFromCallSyncRecord to write a VoIP call log
                // message to a chat. Cobalt stores the record in callLogStates keyed by index parts.
                var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: extract index parts for store key
                if (indexArray.size() < 4) { // ADAPTED: defensive index validation
                    return malformedActionValue(); // ADAPTED: treat malformed index same as malformed value for store keying
                }

                var peer = indexArray.getString(1); // ADAPTED: peerJid from index
                var callId = indexArray.getString(2); // ADAPTED: callId from index
                var fromMe = indexArray.getString(3); // ADAPTED: fromMe from index
                if (peer == null || callId == null || fromMe == null) { // ADAPTED: defensive null checks
                    return malformedActionValue(); // ADAPTED: treat missing parts as malformed
                }

                var key = peer + "|" + callId + "|" + fromMe; // ADAPTED: composite store key from index parts
                var states = new HashMap<>(client.store().callLogStates()); // ADAPTED: copy-modify-set pattern for unmodifiable store map
                states.put(key, log); // ADAPTED: WAWebVoipActionWriteCallLogSync.generateCallLogFromCallSyncRecord
                client.store().setCallLogStates(states); // ADAPTED: persist updated map

                return MutationApplicationResult.success(); // WAWebCallLogSync.applyMutations: {actionState: SyncActionState.Success}
            } else if (mutation.operation() == SyncdOperation.REMOVE) { // WAWebCallLogSync.applyMutations: else if (e.operation === "remove")
                // ADAPTED: WA Web simply returns Success for remove. Cobalt also removes from store.
                var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: extract index parts for store key
                if (indexArray.size() >= 4) { // ADAPTED: only remove if index is valid
                    var peer = indexArray.getString(1);
                    var callId = indexArray.getString(2);
                    var fromMe = indexArray.getString(3);
                    if (peer != null && callId != null && fromMe != null) {
                        var key = peer + "|" + callId + "|" + fromMe;
                        var states = new HashMap<>(client.store().callLogStates()); // ADAPTED: copy-modify-set pattern
                        states.remove(key); // ADAPTED: remove from store
                        client.store().setCallLogStates(states); // ADAPTED: persist updated map
                    }
                }
                return MutationApplicationResult.success(); // WAWebCallLogSync.applyMutations: {actionState: SyncActionState.Success}
            }

            return MutationApplicationResult.unsupported(); // WAWebCallLogSync.applyMutations: return l++, {actionState: SyncActionState.Unsupported}
        } catch (Exception e) { // WAWebCallLogSync.applyMutations: catch (e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebCallLogSync.applyMutations: {actionState: SyncActionState.Failed}
        }
    }

    /**
     * Builds a pending mutation for syncing an outgoing call log record.
     *
     * <p>Per WhatsApp Web {@code WAWebCallLogSync.getCallLogMutation}:
     * <ol>
     *   <li>Determines the caller JID: uses {@code callCreatorJid} from the
     *       record if present, otherwise falls back to the current user's
     *       device PN JID (when {@code fromMe} is {@code true}) or the
     *       {@code peerJid}</li>
     *   <li>Builds the mutation index as
     *       {@code [action, callerJid, callId, fromMe ? "1" : "0"]}</li>
     *   <li>Wraps the record in a {@code callLogAction} value</li>
     *   <li>Delegates to {@code WAWebSyncdActionUtils.buildPendingMutation}</li>
     * </ol>
     *
     * <p>In Cobalt, the caller must supply the pre-computed caller JID and the
     * {@code CallLog} record directly.
     *
     * @implNote WAWebCallLogSync.getCallLogMutation
     * @param timestamp the mutation timestamp
     * @param callerJid the JID to use as the first index key (the resolved
     *                  caller or peer JID)
     * @param callId    the unique call identifier
     * @param fromMe    whether the call was initiated by the current user
     * @param log       the call log record to sync
     * @return the pending mutation for the call log action
     */
    @WhatsAppWebExport(moduleName = "WAWebCallLogSync", exports = "getCallLogMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getCallLogMutation(
            Instant timestamp,
            Jid callerJid,
            String callId,
            boolean fromMe,
            CallLog log
    ) {
        var action = new CallLogActionBuilder() // WAWebCallLogSync.getCallLogMutation: {callLogAction: {callLogRecord: f}}
                .log(log) // WAWebCallLogSync.getCallLogMutation: callLogRecord: f
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: t
                .callLogAction(action) // WAWebCallLogSync.getCallLogMutation: {callLogAction: ...}
                .build();
        var fromMeStr = fromMe ? "1" : "0"; // WAWebCallLogSync.getCallLogMutation: var m = n.fromMe ? "1" : "0"
        var index = JSON.toJSONString(List.of(actionName(), callerJid.toString(), callId, fromMeStr)); // WAWebSyncdActionUtils.buildPendingMutation: index = buildIndex(action, [d, p, m])
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index,
                value,
                SyncdOperation.SET, // WAWebCallLogSync.getCallLogMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
