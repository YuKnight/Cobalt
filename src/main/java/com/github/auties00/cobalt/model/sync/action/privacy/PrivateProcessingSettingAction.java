package com.github.auties00.cobalt.model.sync.action.privacy;

import com.github.auties00.cobalt.model.sync.SyncActionEmptyArgs;
import com.github.auties00.cobalt.model.sync.SyncAction;

import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;

/**
 * A sync action that records the user's preference for the "private
 * processing" feature, which controls whether sensitive on-device computation
 * may run for the linked account.
 *
 * <p>The action carries a single enum value capturing whether private
 * processing is undefined, enabled, or disabled. WhatsApp Web propagates this
 * preference across linked devices via the {@code private_processing_setting}
 * sync action.
 *
 * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction
 */
@ProtobufMessage(name = "SyncActionValue.PrivateProcessingSettingAction")
public final class PrivateProcessingSettingAction implements SyncAction<SyncActionEmptyArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "private_processing_setting";

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
     * The current private processing status persisted by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction.privateProcessingStatus
     */
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    PrivateProcessingStatus privateProcessingStatus;


    /**
     * Constructs a new {@code PrivateProcessingSettingAction} carrying the
     * supplied private processing status.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction
     * @param privateProcessingStatus the private processing status to persist, or {@code null} if unset
     */
    PrivateProcessingSettingAction(PrivateProcessingStatus privateProcessingStatus) {
        this.privateProcessingStatus = privateProcessingStatus;
    }

    /**
     * Returns the current private processing status carried by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction.privateProcessingStatus
     * @return the private processing status, or {@link Optional#empty()} if unset
     */
    public Optional<PrivateProcessingStatus> privateProcessingStatus() {
        return Optional.ofNullable(privateProcessingStatus);
    }

    /**
     * Sets the private processing status carried by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction.privateProcessingStatus
     * @param privateProcessingStatus the new private processing status, or {@code null} to clear it
     */
    public void setPrivateProcessingStatus(PrivateProcessingStatus privateProcessingStatus) {
        this.privateProcessingStatus = privateProcessingStatus;
    }

    /**
     * The user's private processing preference.
     *
     * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction.PrivateProcessingStatus
     */
    @ProtobufEnum(name = "SyncActionValue.PrivateProcessingSettingAction.PrivateProcessingStatus")
    public enum PrivateProcessingStatus {
        /**
         * The user has not yet expressed a preference for the private
         * processing feature.
         *
         * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction.PrivateProcessingStatus.UNDEFINED
         */
        UNDEFINED(0),
        /**
         * The user has enabled the private processing feature.
         *
         * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction.PrivateProcessingStatus.ENABLED
         */
        ENABLED(1),
        /**
         * The user has disabled the private processing feature.
         *
         * @implNote WAWebProtobufSyncAction.pb PrivateProcessingSettingAction.PrivateProcessingStatus.DISABLED
         */
        DISABLED(2);

        PrivateProcessingStatus(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        final int index;

        public int index() {
            return this.index;
        }
    }
}
