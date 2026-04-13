package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.WamoUserIdentifierAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles {@link WamoUserIdentifierAction} sync mutations
 * ({@code "generated_wui"}).
 *
 * <p>Each mutation carries a single {@code identifier} string (an opaque,
 * server-generated WAMO user identifier token, where {@code wui} stands for
 * "WAMO user identifier") which is persisted on the local
 * {@code WhatsAppStore} via {@code setWamoUserIdentifier}. Only {@code SET}
 * operations are accepted; any other operation maps to
 * {@link MutationApplicationResult#unsupported()} and a missing, wrong-typed,
 * empty or blank value maps to
 * {@link MutationApplicationResult#malformed()}.
 *
 * <p><b>NO_WA_BASIS:</b> The {@code SyncActionValue.WamoUserIdentifierAction}
 * protobuf is defined in {@code WAWebProtobufSyncAction.pb} (exported as
 * {@code SyncActionValue$WamoUserIdentifierActionSpec}) with one optional
 * field ({@code identifier: string} at index {@code 1}), and the
 * {@code WAWebProtobufSyncAction.pb} collection-name resolver explicitly maps
 * {@code WAMO_USER_IDENTIFIER_ACTION} (numeric id {@code 52}) to the
 * {@code CRITICAL_BLOCK} collection
 * ({@code e===c.WAMO_USER_IDENTIFIER_ACTION?u.CRITICAL_BLOCK}). However, the
 * current WA Web snapshot does <em>not</em> ship a corresponding sync handler
 * module (no {@code WAWebWamoUserIdentifierSync}). The action is also absent
 * from {@code WASyncdConst.Actions}, the action-name enum consumed by
 * {@code WAWebSyncdGetActionHandler}, and from
 * {@code WAWebCollectionHandlerActions.ActionHandlers}, the registry consumed
 * by {@code WAWebSyncdGetActionHandler.setActionHandlers}. Consequently WA Web
 * would never dispatch any incoming mutation with this action via
 * {@code WAWebSyncdGetActionHandler.getActionHandler("generated_wui")} (the
 * lookup would return {@code undefined} and the mutation would be skipped).
 * The {@code "generated_wui"} string literal does appear once outside the
 * protobuf module: {@code WAWebSyncdAntiTampering} hardcodes it inside an
 * inclusion list {@code A} consumed by the {@code te(collectionName, action)}
 * helper, which only causes the action to be tracked while logging
 * snapshot/patch MAC inconsistencies — it does <em>not</em> apply the action
 * to any local state.
 *
 * <p>The other "WAMO" surface in WA Web (modules such as
 * {@code WAWebWamoNewsletterGatingUtils}, {@code WAWebWamoPDFNGatingUtils},
 * {@code WAWebNewsletterWamoSubMessageType},
 * {@code WAWebNewsletterWamoSubUtils}, {@code WASmaxInNewslettersWAMOSubMixin})
 * deals exclusively with paid newsletter subscription gating, terms of
 * service, and message parsing — none of those modules consume
 * {@code SyncActionValue.WamoUserIdentifierAction}.
 *
 * <p>The Cobalt handler is a forward-looking implementation: it follows the
 * Cobalt sync handler conventions used by every other registered handler
 * (singleton, {@code applyMutationResult} producing a typed
 * {@link MutationApplicationResult}, eager store update on {@code SET}). The
 * shape of the handler — only-{@code SET}, single non-blank string payload,
 * single store setter — is inferred directly from the protobuf shape (one
 * optional {@code identifier: string} field) and from sibling
 * identifier-style handlers (e.g. {@code MusicUserIdHandler},
 * {@code NewsletterSavedInterestsHandler}) which follow the same
 * {@code single-string -> single store setter} pattern. Every behavioural step
 * here is Cobalt-inferred until WA Web ships the matching
 * {@code WAWebWamoUserIdentifierSync} module.
 *
 * @implNote NO_WA_BASIS: no WA Web sync handler exists for
 *           {@code "generated_wui"}. Only the protobuf shape
 *           {@code SyncActionValue.WamoUserIdentifierAction} (field index
 *           {@code 52}, single {@code identifier: string} at index {@code 1},
 *           from {@code WAWebProtobufSyncAction.pb}) and the inline collection
 *           mapping ({@code WAMO_USER_IDENTIFIER_ACTION -> CRITICAL_BLOCK})
 *           are present in the WA Web snapshot.
 *           {@code WAWebCollectionHandlerActions.ActionHandlers} does not
 *           include a wamo user identifier handler, and the only non-protobuf
 *           reference to {@code "generated_wui"} is the anti-tampering
 *           tracking inclusion list in {@code WAWebSyncdAntiTampering}.
 */
public final class WamoUserIdentifierHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code WamoUserIdentifierHandler}.
     *
     * @implNote NO_WA_BASIS: WA Web has no sync handler module for
     *           {@code "generated_wui"}; the singleton mirrors the
     *           {@code l.default = new u()} pattern used by every other Cobalt
     *           sync handler.
     */
    public static final WamoUserIdentifierHandler INSTANCE = new WamoUserIdentifierHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     *
     * @implNote NO_WA_BASIS: no WA Web counterpart constructor; mirrors the
     *           Cobalt handler convention of a private no-arg constructor with
     *           a public {@code INSTANCE} field.
     */
    private WamoUserIdentifierHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: returns the canonical {@code "generated_wui"}
     *           action name declared on
     *           {@link WamoUserIdentifierAction#ACTION_NAME}. This name matches
     *           the protobuf field name {@code wamoUserIdentifierAction} in
     *           {@code WAWebProtobufSyncAction.pb} (numeric id {@code 52}) but
     *           no WA Web {@code WASyncdConst.Actions} entry references it from
     *           a runtime handler.
     * @return the canonical {@code "generated_wui"} string
     */
    @Override
    public String actionName() {
        return WamoUserIdentifierAction.ACTION_NAME; // NO_WA_BASIS: WAWebProtobufSyncAction.pb only declares the protobuf field wamoUserIdentifierAction at index 52
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#CRITICAL_BLOCK} as inferred from the WA
     * Web protobuf-side collection router.
     *
     * @implNote NO_WA_BASIS: WA Web does not declare a sync handler for this
     *           action, but the inline collection router in
     *           {@code WAWebProtobufSyncAction.pb}
     *           ({@code e===c.WAMO_USER_IDENTIFIER_ACTION?u.CRITICAL_BLOCK})
     *           explicitly maps the action id {@code 52} to the
     *           {@code CRITICAL_BLOCK} collection. Cobalt mirrors that mapping
     *           by returning {@link SyncPatchType#CRITICAL_BLOCK}.
     * @return {@link SyncPatchType#CRITICAL_BLOCK}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.CRITICAL_BLOCK; // NO_WA_BASIS: matches the WAWebProtobufSyncAction.pb inline collection mapping WAMO_USER_IDENTIFIER_ACTION -> CRITICAL_BLOCK
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web has no version constant for this action;
     *           Cobalt defaults to
     *           {@link WamoUserIdentifierAction#ACTION_VERSION} ({@code 1})
     *           matching every other unmigrated sync action handler.
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return WamoUserIdentifierAction.ACTION_VERSION; // NO_WA_BASIS: WA Web has no wamo user identifier version constant; defaults to 1
    }

    /**
     * Applies a wamo user identifier mutation.
     *
     * <p>Boolean adapter on top of
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}:
     * returns {@code true} only when the underlying result is
     * {@link SyncActionState#SUCCESS}. {@code MALFORMED} and
     * {@code UNSUPPORTED} both map to {@code false}, mirroring the convention
     * used by every other Cobalt sync handler.
     *
     * @implNote ADAPTED: NO_WA_BASIS — there is no WA Web
     *           {@code WAWebWamoUserIdentifierSync.applyMutations} to map to.
     *           The boolean collapse mirrors the
     *           {@code SUCCESS == true, everything-else == false} pattern used
     *           by all other Cobalt sync handlers.
     * @param client   the {@link WhatsAppClient} instance linked to the
     *                 mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was successfully applied,
     *         {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: NO_WA_BASIS — boolean collapse over the typed result
    }

    /**
     * Applies a wamo user identifier mutation and returns the detailed
     * outcome.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only {@code SET}
     *       mutations are accepted; the action carries a single string token
     *       and there is no semantic for {@code REMOVE}.</li>
     *   <li>Resolve the mutation value to a {@link WamoUserIdentifierAction};
     *       if the value is missing or of the wrong type, return
     *       {@link MutationApplicationResult#malformed()}.</li>
     *   <li>Reject mutations whose {@code identifier} is empty or blank by
     *       returning {@link MutationApplicationResult#malformed()}: an empty
     *       or whitespace-only WAMO identifier carries no meaningful update.</li>
     *   <li>Persist the resolved identifier on the store via
     *       {@code WhatsAppStore.setWamoUserIdentifier} and return
     *       {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors {@code wamoUserIdentifier()} and
     * {@code setWamoUserIdentifier(...)} already exist on
     * {@code WhatsAppStore} / {@code AbstractWhatsAppStore}; this handler is
     * the sole writer.
     *
     * @implNote NO_WA_BASIS: no WA Web sync handler implements
     *           {@code "generated_wui"}. The shape of this method —
     *           only-{@code SET}, single non-blank string payload, single
     *           store setter — is inferred from the protobuf
     *           {@code SyncActionValue.WamoUserIdentifierAction}
     *           ({@code identifier: string} at index {@code 1}) and from
     *           sibling identifier-style handlers (e.g.
     *           {@code MusicUserIdHandler},
     *           {@code NewsletterSavedInterestsHandler}) which follow the same
     *           {@code single-string -> single store setter} pattern.
     * @param client   the {@link WhatsAppClient} instance linked to the
     *                 mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // NO_WA_BASIS: only SET makes sense for a single-string identifier-style action
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof WamoUserIdentifierAction action) // NO_WA_BASIS: payload type guard
                || action.identifier().isEmpty() // NO_WA_BASIS: identifier is the only field on the protobuf and is required for any meaningful update
                || action.identifier().get().isBlank()) { // NO_WA_BASIS: blank/whitespace-only identifier carries no meaningful update
            return MutationApplicationResult.malformed();
        }

        client.store().setWamoUserIdentifier(action.identifier().get()); // NO_WA_BASIS: persist the identifier on the flattened Cobalt store
        return MutationApplicationResult.success();
    }
}
