package com.github.auties00.cobalt.model.sync.action.bot;

import com.github.auties00.cobalt.model.sync.SyncActionEmptyArgs;
import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;

import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;

/**
 * A sync action that propagates the user's preference for Meta AI ("Maiba")
 * features across linked devices.
 *
 * <p>The action carries a single enum value capturing whether Meta AI features
 * are enabled, enabled with learning, or fully disabled. WhatsApp Web stores
 * this preference in the {@code REGULAR_HIGH} sync collection alongside the
 * other Maiba-related toggles.
 *
 * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction
 */
@ProtobufMessage(name = "SyncActionValue.MaibaAIFeaturesControlAction")
public final class MaibaAIFeaturesControlAction implements SyncAction<SyncActionEmptyArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "maiba_ai_features_control";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 1;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     *
     * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction collection mapping
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_HIGH;

    /**
     * {@inheritDoc}
     */
    @Override
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int actionVersion() {
        return ACTION_VERSION;
    }


    /**
     * The current Meta AI feature status persisted by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction.aiFeatureStatus
     */
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    MaibaAIFeatureStatus aiFeatureStatus;


    /**
     * Constructs a new {@code MaibaAIFeaturesControlAction} carrying the
     * supplied feature status.
     *
     * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction
     * @param aiFeatureStatus the Meta AI feature status to persist, or {@code null} if unset
     */
    MaibaAIFeaturesControlAction(MaibaAIFeatureStatus aiFeatureStatus) {
        this.aiFeatureStatus = aiFeatureStatus;
    }

    /**
     * Returns the current Meta AI feature status carried by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction.aiFeatureStatus
     * @return the feature status, or {@link Optional#empty()} if unset
     */
    public Optional<MaibaAIFeatureStatus> aiFeatureStatus() {
        return Optional.ofNullable(aiFeatureStatus);
    }

    /**
     * Sets the Meta AI feature status carried by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction.aiFeatureStatus
     * @param aiFeatureStatus the new feature status, or {@code null} to clear it
     */
    public void setAiFeatureStatus(MaibaAIFeatureStatus aiFeatureStatus) {
        this.aiFeatureStatus = aiFeatureStatus;
    }

    /**
     * The user's Meta AI ("Maiba") feature preference.
     *
     * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction.MaibaAIFeatureStatus
     */
    @ProtobufEnum(name = "SyncActionValue.MaibaAIFeaturesControlAction.MaibaAIFeatureStatus")
    public enum MaibaAIFeatureStatus {
        /**
         * Meta AI features are enabled but the user has not granted the
         * additional consent required to participate in model learning.
         *
         * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction.MaibaAIFeatureStatus.ENABLED
         */
        ENABLED(0),
        /**
         * Meta AI features are enabled and the user has consented to having
         * their interactions contribute to Meta AI learning.
         *
         * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction.MaibaAIFeatureStatus.ENABLED_HAS_LEARNING
         */
        ENABLED_HAS_LEARNING(1),
        /**
         * Meta AI features are disabled for this user.
         *
         * @implNote WAWebProtobufSyncAction.pb MaibaAIFeaturesControlAction.MaibaAIFeatureStatus.DISABLED
         */
        DISABLED(2);

        MaibaAIFeatureStatus(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        final int index;

        public int index() {
            return this.index;
        }
    }
}
