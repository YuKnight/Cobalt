package com.github.auties00.cobalt.model.sync.action.device;

import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncActionEmptyArgs;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Collections;
import java.util.List;

/**
 * Top-level container action carrying the authoritative snapshot of the
 * authenticated business account's paid subscriptions and feature flags.
 *
 * <p>Mutations of this type are singletons (the index is
 * {@code ["subscriptions_sync_v2"]}). On {@code SET} the receiver replaces
 * its locally cached subscription state and feature flag table with the
 * lists carried in this message; {@code REMOVE} simply clears that local
 * state.
 *
 * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action
 */
@ProtobufMessage(name = "SyncActionValue.SubscriptionsSyncV2Action")
public final class SubscriptionsSyncV2Action implements SyncAction<SyncActionEmptyArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     *
     * @implNote WASyncdConst.Actions.SubscriptionsSyncV2 — string constant
     *           {@code "subscriptions_sync_v2"}
     */
    public static final String ACTION_NAME = "subscriptions_sync_v2";

    /**
     * Canonical WhatsApp Web action version for this action type.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.getVersion — returns {@code 1}
     */
    public static final int ACTION_VERSION = 1;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync — constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.Regular}
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR;

    /**
     * The repeated list of {@link SubscriptionInfo} entries describing
     * every paid subscription known to the server for this account.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action.subscriptions
     */
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    List<SubscriptionInfo> subscriptions;

    /**
     * The repeated list of {@link PaidFeature} entries describing every
     * paid business feature flag known to the server for this account.
     *
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action.paidFeature
     */
    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    List<PaidFeature> paidFeatures;

    /**
     * Constructs a new {@code SubscriptionsSyncV2Action} from raw protobuf
     * field values.
     *
     * @param subscriptions the subscriptions list
     * @param paidFeatures  the paid features list
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action
     */
    SubscriptionsSyncV2Action(List<SubscriptionInfo> subscriptions, List<PaidFeature> paidFeatures) {
        this.subscriptions = subscriptions;
        this.paidFeatures = paidFeatures;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.getAction — returns
     *           {@code WASyncdConst.Actions.SubscriptionsSyncV2}, the
     *           constant {@code "subscriptions_sync_v2"}
     */
    @Override
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.getVersion — returns {@code 1}
     */
    @Override
    public int actionVersion() {
        return ACTION_VERSION;
    }

    /**
     * Returns the unmodifiable list of {@link SubscriptionInfo} entries
     * carried by this action.
     *
     * <p>The list is never {@code null}; an empty list is returned when no
     * subscription entries were present on the wire.
     *
     * @return the subscriptions list
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action.subscriptions
     */
    public List<SubscriptionInfo> subscriptions() {
        return subscriptions == null ? List.of() : Collections.unmodifiableList(subscriptions);
    }

    /**
     * Returns the unmodifiable list of {@link PaidFeature} entries carried
     * by this action.
     *
     * <p>The list is never {@code null}; an empty list is returned when no
     * paid feature entries were present on the wire.
     *
     * @return the paid features list
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action.paidFeature
     */
    public List<PaidFeature> paidFeatures() {
        return paidFeatures == null ? List.of() : Collections.unmodifiableList(paidFeatures);
    }

    /**
     * Sets the subscriptions list.
     *
     * @param subscriptions the new subscriptions list
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action.subscriptions
     */
    public SubscriptionsSyncV2Action setSubscriptions(List<SubscriptionInfo> subscriptions) {
        this.subscriptions = subscriptions;
        return this;
    }

    /**
     * Sets the paid features list.
     *
     * @param paidFeatures the new paid features list
     * @return this instance for method chaining
     * @implNote WAWebProtobufsServerSync.SyncActionValue$SubscriptionsSyncV2Action.paidFeature
     */
    public SubscriptionsSyncV2Action setPaidFeatures(List<PaidFeature> paidFeatures) {
        this.paidFeatures = paidFeatures;
        return this;
    }
}
