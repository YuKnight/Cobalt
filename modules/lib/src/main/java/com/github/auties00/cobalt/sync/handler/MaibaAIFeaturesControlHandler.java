package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.bot.MaibaAIFeaturesControlAction;
import com.github.auties00.cobalt.model.sync.action.bot.MaibaAIFeaturesControlActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

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
 * {@code setMaibaAiFeatureStatus}. Only {@code SET} operations are accepted;
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
 * (singleton, {@code applyMutationResult} producing a typed
 * {@link MutationApplicationResult}, eager store update on
 * {@code SET}). Every behavioural step here is Cobalt-inferred until WA Web
 * ships the matching {@code WAWebMaibaAiFeaturesControlSync} module.
 *
 * @implNote NO_WA_BASIS: no WA Web sync handler exists for
 *           {@code "maiba_ai_features_control"}. Only the protobuf shape
 *           {@code SyncActionValue.MaibaAIFeaturesControlAction} (field index
 *           {@code 68}, single {@code aiFeatureStatus} enum from
 *           {@code WAWebProtobufSyncAction.pb}) is present in the WA Web
 *           snapshot. {@code WAWebCollectionHandlerActions.ActionHandlers}
 *           does not include a Maiba handler.
 */
public final class MaibaAIFeaturesControlHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MaibaAIFeaturesControlHandler}.
     *
     * @implNote NO_WA_BASIS: WA Web has no sync handler module for
     *           {@code "maiba_ai_features_control"}; the singleton mirrors the
     *           {@code l.default = new u()} pattern used by every other Cobalt
     *           sync handler.
     */
    public static final MaibaAIFeaturesControlHandler INSTANCE = new MaibaAIFeaturesControlHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     *
     * @implNote NO_WA_BASIS: no WA Web counterpart constructor; mirrors the
     *           Cobalt handler convention of a private no-arg constructor with
     *           a public {@code INSTANCE} field.
     */
    private MaibaAIFeaturesControlHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: returns the canonical
     *           {@code "maiba_ai_features_control"} action name declared on
     *           {@link MaibaAIFeaturesControlAction#ACTION_NAME}. This name
     *           matches the protobuf field name {@code maibaAiFeaturesControlAction}
     *           in {@code WAWebProtobufSyncAction.pb} but no WA Web
     *           {@code WASyncdConst.Actions} entry references it.
     * @return the canonical {@code "maiba_ai_features_control"} string
     */
    @Override
    public String actionName() {
        return MaibaAIFeaturesControlAction.ACTION_NAME; // NO_WA_BASIS: WAWebProtobufSyncAction.pb only declares the protobuf field maibaAiFeaturesControlAction at index 68
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#REGULAR_HIGH} as an inferred default.
     *
     * @implNote NO_WA_BASIS: WA Web does not declare a collection for this
     *           action since no handler module exists. Cobalt assigns
     *           {@link SyncPatchType#REGULAR_HIGH} to keep the value
     *           consistent with other "settings"-shaped boolean/enum
     *           preference handlers (e.g. {@code PrivateProcessingSettingHandler})
     *           that operate on a single global flag stored on the user
     *           account. The model class
     *           {@link MaibaAIFeaturesControlAction} (a context file) does
     *           not yet expose a {@code COLLECTION_NAME} constant, so the
     *           value is inlined here.
     * @return {@link SyncPatchType#REGULAR_HIGH}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR_HIGH; // NO_WA_BASIS: no WA Web sync handler declares a collection for "maiba_ai_features_control"; REGULAR_HIGH matches sibling preference-style handlers
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web has no version constant for this action;
     *           Cobalt defaults to {@link MaibaAIFeaturesControlAction#ACTION_VERSION}
     *           ({@code 1}) matching every other unmigrated sync action handler.
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return MaibaAIFeaturesControlAction.ACTION_VERSION; // NO_WA_BASIS: WA Web has no Maiba version constant; defaults to 1
    }

    /**
     * Applies a Maiba AI features control mutation.
     *
     * <p>Boolean adapter on top of
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}:
     * returns {@code true} only when the underlying result is
     * {@link SyncActionState#SUCCESS}. {@code MALFORMED} and {@code UNSUPPORTED}
     * both map to {@code false}, mirroring the convention used by every other
     * Cobalt sync handler.
     *
     * @implNote ADAPTED: NO_WA_BASIS — there is no WA Web
     *           {@code WAWebMaibaAiFeaturesControlSync.applyMutations} to map
     *           to. The boolean collapse mirrors the
     *           {@code SUCCESS == true, everything-else == false} pattern
     *           used by all other Cobalt sync handlers.
     * @param client   the {@link WhatsAppClient} instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was successfully applied,
     *         {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: NO_WA_BASIS — boolean collapse over the typed result
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
     *       store via {@code WhatsAppStore.setMaibaAiFeatureStatus} and
     *       return {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors {@code maibaAiFeatureStatus()} and
     * {@code setMaibaAiFeatureStatus(...)} already exist on
     * {@code WhatsAppStore} / {@code AbstractWhatsAppStore}; this handler is
     * the sole writer.
     *
     * @implNote NO_WA_BASIS: no WA Web sync handler implements
     *           {@code "maiba_ai_features_control"}. The shape of this method
     *           — only-{@code SET}, single typed enum payload, single store
     *           setter — is inferred from the protobuf
     *           {@code SyncActionValue.MaibaAIFeaturesControlAction}
     *           ({@code aiFeatureStatus: enum}) and from sibling
     *           preference-style handlers (e.g.
     *           {@code PrivateProcessingSettingHandler}) which follow the
     *           same {@code single enum -> single store setter} pattern.
     * @param client   the {@link WhatsAppClient} instance linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // NO_WA_BASIS: only SET makes sense for a single-enum preference action
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof MaibaAIFeaturesControlAction action) // NO_WA_BASIS: payload type guard
                || action.aiFeatureStatus().isEmpty()) { // NO_WA_BASIS: aiFeatureStatus is the only field on the protobuf and is required for any meaningful update
            return MutationApplicationResult.malformed();
        }

        client.store().setMaibaAiFeatureStatus(action.aiFeatureStatus().get()); // NO_WA_BASIS: persist the enum on the flattened Cobalt store
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
     *
     * @implNote NO_WA_BASIS — shaped after
     *           {@code WAWebDisableLinkPreviewsSync.getMutation} (same
     *           {@code collection / indexArgs=[] / value / version / operation=SET
     *           / timestamp / action} payload).
     * @param timestamp the mutation timestamp
     * @param status    the new {@link MaibaAIFeaturesControlAction.MaibaAIFeatureStatus}
     * @return a pending mutation carrying the {@code maiba_ai_features_control}
     *         action
     * @throws NullPointerException if {@code timestamp} or {@code status} is {@code null}
     */
    public SyncPendingMutation getMaibaAiFeatureStatusMutation(Instant timestamp, MaibaAIFeaturesControlAction.MaibaAIFeatureStatus status) {
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        var action = new MaibaAIFeaturesControlActionBuilder() // NO_WA_BASIS: wrap the enum into the protobuf payload
                .aiFeatureStatus(status)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .maibaAiFeaturesControlAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName())); // NO_WA_BASIS: JSON.stringify([action]) with empty indexArgs
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
