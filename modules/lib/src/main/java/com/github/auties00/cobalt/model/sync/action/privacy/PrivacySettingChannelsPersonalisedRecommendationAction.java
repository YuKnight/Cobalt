package com.github.auties00.cobalt.model.sync.action.privacy;

import com.github.auties00.cobalt.model.sync.SyncActionEmptyArgs;
import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * A sync action that records whether the user has opted out of personalised
 * channel recommendations.
 *
 * <p>WhatsApp Web persists this preference under the
 * {@code setting_channels_personalised_recommendation_optout} action so that
 * the user's choice on one device propagates to every linked device. The action
 * carries a single boolean indicating whether the user has actively opted out.
 *
 * @implNote WAWebProtobufSyncAction.pb PrivacySettingChannelsPersonalisedRecommendationAction
 */
@ProtobufMessage(name = "SyncActionValue.PrivacySettingChannelsPersonalisedRecommendationAction")
public final class PrivacySettingChannelsPersonalisedRecommendationAction implements SyncAction<SyncActionEmptyArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "setting_channels_personalised_recommendation_optout";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 1;

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
     * Whether the user has opted out of personalised channel recommendations.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivacySettingChannelsPersonalisedRecommendationAction.isUserOptedOut
     */
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean isUserOptedOut;


    /**
     * Constructs a new {@code PrivacySettingChannelsPersonalisedRecommendationAction}
     * carrying the supplied opt-out flag.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivacySettingChannelsPersonalisedRecommendationAction
     * @param isUserOptedOut the opt-out flag to persist, or {@code null} if unset
     */
    PrivacySettingChannelsPersonalisedRecommendationAction(Boolean isUserOptedOut) {
        this.isUserOptedOut = isUserOptedOut;
    }

    /**
     * Returns whether the user has opted out of personalised channel
     * recommendations.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivacySettingChannelsPersonalisedRecommendationAction.isUserOptedOut
     * @return {@code true} if the user has opted out, {@code false} otherwise (including when unset)
     */
    public boolean isUserOptedOut() {
        return isUserOptedOut != null && isUserOptedOut;
    }

    /**
     * Sets whether the user has opted out of personalised channel
     * recommendations.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivacySettingChannelsPersonalisedRecommendationAction.isUserOptedOut
     * @param isUserOptedOut the new opt-out flag, or {@code null} to clear it
     */
    public void setUserOptedOut(Boolean isUserOptedOut) {
        this.isUserOptedOut = isUserOptedOut;
    }
}
