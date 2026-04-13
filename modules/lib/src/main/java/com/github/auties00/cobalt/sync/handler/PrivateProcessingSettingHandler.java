package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivateProcessingSettingAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles {@link PrivateProcessingSettingAction} sync mutations
 * ({@code "private_processing_setting"}).
 *
 * <p>Each mutation carries a single
 * {@link PrivateProcessingSettingAction.PrivateProcessingStatus} value
 * (one of {@code UNDEFINED}, {@code ENABLED}, {@code DISABLED}) which is
 * persisted on the local {@code WhatsAppStore} via
 * {@code setPrivateProcessingStatus}. Only {@code SET} operations are accepted;
 * any other operation maps to
 * {@link MutationApplicationResult#unsupported()} and a missing or unparseable
 * value maps to {@link MutationApplicationResult#malformed()}.
 *
 * <p><b>NO_WA_BASIS:</b> The {@code SyncActionValue.PrivateProcessingSettingAction}
 * protobuf is defined in {@code WAWebProtobufSyncAction.pb} as field index
 * {@code 74} with a single {@code privateProcessingStatus} enum, but the
 * current WA Web snapshot does <em>not</em> ship a corresponding sync handler
 * module (no {@code WAWebPrivateProcessingSettingSync}). The action is also
 * absent from {@code WAWebCollectionHandlerActions.ActionHandlers}, the
 * registry consumed by {@code WAWebSyncdGetActionHandler.setActionHandlers},
 * so WA Web would never dispatch any incoming mutation with this action.
 * The literal {@code "private_processing_setting"} only appears in the
 * protobuf spec module and nowhere else in the WA Web source.
 *
 * <p>The Cobalt handler is a forward-looking implementation: it follows the
 * Cobalt sync handler conventions used by every other registered handler
 * (singleton, {@code applyMutationResult} producing a typed
 * {@link MutationApplicationResult}, eager store update on
 * {@code SET}). Every behavioural step here is Cobalt-inferred until WA Web
 * ships the matching {@code WAWebPrivateProcessingSettingSync} module.
 *
 * @implNote NO_WA_BASIS: no WA Web sync handler exists for
 *           {@code "private_processing_setting"}. Only the protobuf shape
 *           {@code SyncActionValue.PrivateProcessingSettingAction} (field
 *           index {@code 74}, single {@code privateProcessingStatus} enum
 *           from {@code WAWebProtobufSyncAction.pb}) is present in the WA Web
 *           snapshot. {@code WAWebCollectionHandlerActions.ActionHandlers}
 *           does not include a private processing setting handler.
 */
public final class PrivateProcessingSettingHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code PrivateProcessingSettingHandler}.
     *
     * @implNote NO_WA_BASIS: WA Web has no sync handler module for
     *           {@code "private_processing_setting"}; the singleton mirrors
     *           the {@code l.default = new u()} pattern used by every other
     *           Cobalt sync handler.
     */
    public static final PrivateProcessingSettingHandler INSTANCE = new PrivateProcessingSettingHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     *
     * @implNote NO_WA_BASIS: no WA Web counterpart constructor; mirrors the
     *           Cobalt handler convention of a private no-arg constructor with
     *           a public {@code INSTANCE} field.
     */
    private PrivateProcessingSettingHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: returns the canonical
     *           {@code "private_processing_setting"} action name declared on
     *           {@link PrivateProcessingSettingAction#ACTION_NAME}. This name
     *           matches the protobuf field name
     *           {@code privateProcessingSettingAction} in
     *           {@code WAWebProtobufSyncAction.pb} but no WA Web
     *           {@code WASyncdConst.Actions} entry references it.
     * @return the canonical {@code "private_processing_setting"} string
     */
    @Override
    public String actionName() {
        return PrivateProcessingSettingAction.ACTION_NAME; // NO_WA_BASIS: WAWebProtobufSyncAction.pb only declares the protobuf field privateProcessingSettingAction at index 74
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
     *           preference handlers (e.g.
     *           {@code MaibaAIFeaturesControlHandler}) that operate on a
     *           single global flag stored on the user account.
     * @return {@link SyncPatchType#REGULAR_HIGH}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR_HIGH; // NO_WA_BASIS: no WA Web sync handler declares a collection for "private_processing_setting"; REGULAR_HIGH matches sibling preference-style handlers
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web has no version constant for this action;
     *           Cobalt defaults to
     *           {@link PrivateProcessingSettingAction#ACTION_VERSION}
     *           ({@code 1}) matching every other unmigrated sync action
     *           handler.
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return PrivateProcessingSettingAction.ACTION_VERSION; // NO_WA_BASIS: WA Web has no PrivateProcessingSetting version constant; defaults to 1
    }

    /**
     * Applies a private processing setting mutation.
     *
     * <p>Boolean adapter on top of
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}:
     * returns {@code true} only when the underlying result is
     * {@link SyncActionState#SUCCESS}. {@code MALFORMED} and
     * {@code UNSUPPORTED} both map to {@code false}, mirroring the convention
     * used by every other Cobalt sync handler.
     *
     * @implNote ADAPTED: NO_WA_BASIS — there is no WA Web
     *           {@code WAWebPrivateProcessingSettingSync.applyMutations} to
     *           map to. The boolean collapse mirrors the
     *           {@code SUCCESS == true, everything-else == false} pattern
     *           used by all other Cobalt sync handlers.
     * @param client   the {@link WhatsAppClient} instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was successfully applied,
     *         {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: NO_WA_BASIS — boolean collapse over the typed result
    }

    /**
     * Applies a private processing setting mutation and returns the detailed
     * outcome.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only {@code SET}
     *       mutations are accepted; the action carries a single mandatory
     *       {@code privateProcessingStatus} enum and there is no semantic for
     *       {@code REMOVE}.</li>
     *   <li>Resolve the mutation value to a
     *       {@link PrivateProcessingSettingAction}; if the value is missing
     *       or of the wrong type, or if {@code privateProcessingStatus} is
     *       empty, return {@link MutationApplicationResult#malformed()}.</li>
     *   <li>Persist the resolved
     *       {@link PrivateProcessingSettingAction.PrivateProcessingStatus}
     *       on the store via {@code WhatsAppStore.setPrivateProcessingStatus}
     *       and return {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors {@code privateProcessingStatus()} and
     * {@code setPrivateProcessingStatus(...)} already exist on
     * {@code WhatsAppStore} / {@code AbstractWhatsAppStore}; this handler is
     * the sole writer.
     *
     * @implNote NO_WA_BASIS: no WA Web sync handler implements
     *           {@code "private_processing_setting"}. The shape of this
     *           method — only-{@code SET}, single typed enum payload, single
     *           store setter — is inferred from the protobuf
     *           {@code SyncActionValue.PrivateProcessingSettingAction}
     *           ({@code privateProcessingStatus: enum}) and from sibling
     *           preference-style handlers (e.g.
     *           {@code MaibaAIFeaturesControlHandler}) which follow the same
     *           {@code single enum -> single store setter} pattern.
     * @param client   the {@link WhatsAppClient} instance linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // NO_WA_BASIS: only SET makes sense for a single-enum preference action
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof PrivateProcessingSettingAction action) // NO_WA_BASIS: payload type guard
                || action.privateProcessingStatus().isEmpty()) { // NO_WA_BASIS: privateProcessingStatus is the only field on the protobuf and is required for any meaningful update
            return MutationApplicationResult.malformed();
        }

        client.store().setPrivateProcessingStatus(action.privateProcessingStatus().get()); // NO_WA_BASIS: persist the enum on the flattened Cobalt store
        return MutationApplicationResult.success();
    }
}
