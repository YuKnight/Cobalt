package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.bot.MaibaAIFeaturesControlAction;
import com.github.auties00.cobalt.model.sync.action.bot.MaibaAIFeaturesControlActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Handles {@link MaibaAIFeaturesControlAction} sync mutations
 * ({@code "maiba_ai_features_control"}).
 *
 * <p>Each mutation carries a single
 * {@link MaibaAIFeaturesControlAction.MaibaAIFeatureStatus} value
 * (one of {@code ENABLED}, {@code ENABLED_HAS_LEARNING}, {@code DISABLED})
 * which is persisted on the local {@code WhatsAppStore} via
 * {@code setAiBusinessAgentStatus}. Only {@code SET} operations are accepted;
 * any other operation maps to
 * {@link MutationApplicationResult#unsupported()} and a missing or unparseable
 * value maps to {@link MutationApplicationResult#malformed()}.
 *
 * <p><b>NO_WA_BASIS:</b> The {@code SyncActionValue.MaibaAIFeaturesControlAction}
 * protobuf is defined in {@code WAWebProtobufSyncAction.pb} as field index
 * {@code 68} with a single {@code aiFeatureStatus} enum, but the current
 * WA Web snapshot does <em>not</em> ship a corresponding sync handler module
 * (no {@code WAWebMaibaAiFeaturesControlSync}). The action is also absent from
 * {@code WAWebCollectionHandlerActions.ActionHandlers}, the registry consumed
 * by {@code WAWebSyncdGetActionHandler.setActionHandlers}, so WA Web would
 * never dispatch any incoming mutation with this action. The closest WA Web
 * code paths that touch the {@code Maiba} surface are
 * {@code WAWebBizAiBridgeApi}, {@code WAWebHandleCloudApiThreadControlNotification}
 * and {@code WAWebChatModel} (per-chat {@code capiThreadControl} state) — none
 * of which consume {@code SyncActionValue.MaibaAIFeaturesControlAction}.
 *
 * <p>The Cobalt handler is a forward-looking implementation: it follows the
 * Cobalt sync handler conventions used by every other registered handler
 * (singleton, {@code applyMutation} producing a typed
 * {@link MutationApplicationResult}, eager store update on
 * {@code SET}). Every behavioural step here is Cobalt-inferred until WA Web
 * ships the matching {@code WAWebMaibaAiFeaturesControlSync} module.
 */
public final class MaibaAIFeaturesControlHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MaibaAIFeaturesControlHandler}.
     */
    public static final MaibaAIFeaturesControlHandler INSTANCE = new MaibaAIFeaturesControlHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     */
    private MaibaAIFeaturesControlHandler() {

    }

    /**
     * {@inheritDoc}
     * @return the canonical {@code "maiba_ai_features_control"} string
     */
    @Override
    public String actionName() {
        return MaibaAIFeaturesControlAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#REGULAR_HIGH} as an inferred default.
     * @return {@link SyncPatchType#REGULAR_HIGH}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR_HIGH; // NO_WA_BASIS: no WA Web sync handler declares a collection for "maiba_ai_features_control"; REGULAR_HIGH matches sibling preference-style handlers
    }

    /**
     * {@inheritDoc}
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return MaibaAIFeaturesControlAction.ACTION_VERSION;
    }

    /**
     * Applies a Maiba AI features control mutation and returns the detailed
     * outcome.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only {@code SET}
     *       mutations are accepted; the action carries a single mandatory
     *       {@code aiFeatureStatus} enum and there is no semantic for
     *       {@code REMOVE}.</li>
     *   <li>Resolve the mutation value to a
     *       {@link MaibaAIFeaturesControlAction}; if the value is missing or
     *       of the wrong type, or if {@code aiFeatureStatus} is empty, return
     *       {@link MutationApplicationResult#malformed()}.</li>
     *   <li>Persist the resolved
     *       {@link MaibaAIFeaturesControlAction.MaibaAIFeatureStatus} on the
     *       store via {@code WhatsAppStore.setAiBusinessAgentStatus} and
     *       return {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors {@code aiBusinessAgentStatus()} and
     * {@code setAiBusinessAgentStatus(...)} already exist on
     * {@code WhatsAppStore} / {@code AbstractWhatsAppStore}; this handler is
     * the sole writer.
     * @param client   the {@link WhatsAppClient} instance linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof MaibaAIFeaturesControlAction action)
                || action.aiFeatureStatus().isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        client.store().setAiBusinessAgentStatus(action.aiFeatureStatus().get());
        return MutationApplicationResult.success();
    }

    /**
     * Builds a pending {@code maiba_ai_features_control} mutation carrying the
     * given AI feature status.
     *
     * <p>NO_WA_BASIS: WA Web has no outgoing helper for this action; the shape
     * follows {@code WAWebSyncdActionUtils.buildPendingMutation} as used by
     * every other sibling {@code AccountSyncdActionBase} subclass. Cobalt
     * surfaces the helper so the public
     * {@code WhatsAppClient.changeAIFeaturesEnabled} setter can build a single
     * mutation without hand-rolling the protobuf wrapping.
     * @param timestamp the mutation timestamp
     * @param status    the new {@link MaibaAIFeaturesControlAction.MaibaAIFeatureStatus}
     * @return a pending mutation carrying the {@code maiba_ai_features_control}
     *         action
     * @throws NullPointerException if {@code timestamp} or {@code status} is {@code null}
     */
    public SyncPendingMutation getMaibaAiFeatureStatusMutation(Instant timestamp, MaibaAIFeaturesControlAction.MaibaAIFeatureStatus status) {
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        var action = new MaibaAIFeaturesControlActionBuilder()
                .aiFeatureStatus(status)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .maibaAiFeaturesControlAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName()));
        var pending = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(pending, 0);
    }
}
