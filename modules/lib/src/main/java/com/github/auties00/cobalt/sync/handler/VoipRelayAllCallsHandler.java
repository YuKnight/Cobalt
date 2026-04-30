package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivacySettingRelayAllCalls;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivacySettingRelayAllCallsBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.List;

/**
 * Handles VoIP relay-all-calls setting sync actions.
 *
 * <p>This handler processes mutations that control whether all VoIP calls
 * are relayed through WhatsApp servers (hiding the user's IP address from
 * the call peer). It maps to the singleton instance exported as
 * {@code default} from the WA Web module, which extends
 * {@code AccountSyncdActionBase} with collection {@code Regular},
 * version {@code 1}, and action {@code "setting_relayAllCalls"}.
 *
 * <p>Index format: {@code ["setting_relayAllCalls"]}
 *
 * @implNote WAWebVoipRelayAllCallsSettingSync.default (singleton instance of the
 *           VoipRelayAllCallsSettingSync class extending AccountSyncdActionBase)
 */
@WhatsAppWebModule(moduleName = "WAWebVoipRelayAllCallsSettingSync")
public final class VoipRelayAllCallsHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code VoipRelayAllCallsHandler}.
     *
     * @implNote WAWebVoipRelayAllCallsSettingSync.default — {@code var m = new d; l.default = m}
     */
    @WhatsAppWebExport(moduleName = "WAWebVoipRelayAllCallsSettingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final VoipRelayAllCallsHandler INSTANCE = new VoipRelayAllCallsHandler();

    /**
     * Creates a new {@code VoipRelayAllCallsHandler}.
     *
     * @implNote WAWebVoipRelayAllCallsSettingSync — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     */
    @WhatsAppWebExport(moduleName = "WAWebVoipRelayAllCallsSettingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private VoipRelayAllCallsHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebVoipRelayAllCallsSettingSync.getAction — returns
     *           {@code WASyncdConst.Actions.VoipRelayAllCalls} which is
     *           {@code "setting_relayAllCalls"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebVoipRelayAllCallsSettingSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return PrivacySettingRelayAllCalls.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebVoipRelayAllCallsSettingSync — constructor field
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     *           which is {@code "regular"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebVoipRelayAllCallsSettingSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return PrivacySettingRelayAllCalls.COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebVoipRelayAllCallsSettingSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return PrivacySettingRelayAllCalls.ACTION_VERSION;
    }

    /**
     * Applies a single VoIP relay-all-calls mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebVoipRelayAllCallsSettingSync.applyMutations — WA Web returns
     *           {@code SyncActionState} values directly; Cobalt wraps in
     *           {@link MutationApplicationResult} for type safety
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebVoipRelayAllCallsSettingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a single VoIP relay-all-calls mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebVoipRelayAllCallsSettingSync.applyMutations}
     * (single-mutation path within the {@code Promise.all(r.map(...))} batch):
     * <ol>
     *   <li>If the operation is not {@code SET}, increments the unsupported counter
     *       and returns {@code Unsupported}.</li>
     *   <li>Extracts {@code privacySettingRelayAllCalls} from the value. If absent,
     *       increments the malformed counter and returns
     *       {@code malformedActionValue(collectionName)}.</li>
     *   <li>Reads {@code isEnabled}. WA Web counts {@code null} values separately
     *       and skips the backend persist call, but still returns {@code Success}.
     *       In Cobalt, the existing nullable boolean accessor coalesces {@code null}
     *       to {@code false} per the project's nullable-boolean-accessor convention.</li>
     *   <li>Otherwise, persists the value via
     *       {@code WAWebBackendApi.frontendSendAndReceive("setRelayAllCallsToUserPrefs", {disallowAllP2p: s})}
     *       and returns {@code Success}.</li>
     * </ol>
     *
     * <p>Per the Cobalt error model, the WA Web {@code try/catch} that converts
     * any thrown error into {@code {actionState: Failed}} is intentionally not
     * replicated; thrown {@code WhatsAppException} subtypes propagate to the
     * pluggable error handler instead.
     *
     * @implNote WAWebVoipRelayAllCallsSettingSync.applyMutations (single-mutation semantics)
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebVoipRelayAllCallsSettingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof PrivacySettingRelayAllCalls action)) {
            return MutationApplicationResult.malformed();
        }

        // MISMATCH (NULL-SKIP): WA Web tests {@code s == null} where {@code s = r.isEnabled}: when null, it increments
        // {@code i++} and SKIPS the {@code setRelayAllCallsToUserPrefs} call entirely (still returning Success).
        // Cobalt's {@link PrivacySettingRelayAllCalls#isEnabled()} coalesces null -> false, so a mutation with
        // {@code privacySettingRelayAllCalls: {isEnabled: null}} is silently written as "relay disabled" to the store
        // instead of being a no-op. Per the project-wide "nullable boolean coalesces to false" convention
        // (see {@code feedback_nullable_bool_accessors.md}), this divergence is accepted.
        client.store().setRelayAllCalls(action.isEnabled()); // ADAPTED: WAWebBackendApi.frontendSendAndReceive("setRelayAllCallsToUserPrefs", {disallowAllP2p: s}) -> direct store call
        return MutationApplicationResult.success();
    }

    /**
     * Builds a pending SET mutation for the VoIP relay-all-calls setting.
     *
     * <p>Per WhatsApp Web {@code WAWebVoipRelayAllCallsSettingSync.getMutation}:
     * <ol>
     *   <li>Wraps the value in a {@code privacySettingRelayAllCalls} object:
     *       {@code {isEnabled: n}}</li>
     *   <li>Delegates to {@code WAWebSyncdActionUtils.buildPendingMutation} with
     *       collection={@code Regular}, indexArgs={@code []},
     *       operation={@code SET}, version={@code 1},
     *       action={@code "setting_relayAllCalls"}</li>
     * </ol>
     *
     * @implNote WAWebVoipRelayAllCallsSettingSync.getMutation
     * @param timestamp the mutation timestamp
     * @param isEnabled whether VoIP relay-all-calls should be enabled
     * @return the pending mutation ready for sync upload
     */
    @WhatsAppWebExport(moduleName = "WAWebVoipRelayAllCallsSettingSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPendingMutation getMutation(Instant timestamp, boolean isEnabled) {
        var action = new PrivacySettingRelayAllCallsBuilder()
                .isEnabled(isEnabled)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .privacySettingRelayAllCalls(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName()));
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0);
    }
}
