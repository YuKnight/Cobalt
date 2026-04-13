package com.github.auties00.cobalt.model.sync.action.privacy;

import com.github.auties00.cobalt.model.sync.SyncActionEmptyArgs;
import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

@ProtobufMessage(name = "SyncActionValue.PrivacySettingDisableLinkPreviewsAction")
public final class PrivacySettingDisableLinkPreviewsAction implements SyncAction<SyncActionEmptyArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "setting_disableLinkPreviews";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 8;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR;

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


    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean isPreviewsDisabled;


    PrivacySettingDisableLinkPreviewsAction(Boolean isPreviewsDisabled) {
        this.isPreviewsDisabled = isPreviewsDisabled;
    }

    /**
     * Returns whether link previews are disabled, coalescing an absent value
     * to {@code false}.
     *
     * @implNote {@code WAWebPrivacySettingDisableLinkPreviewsSyncAction.apply}
     *           treats both an absent value and an explicit {@code false} as
     *           "previews enabled"; callers that must distinguish the two
     *           cases should use {@link #rawIsPreviewsDisabled()}.
     * @return {@code true} if link previews are disabled, otherwise
     *         {@code false}
     */
    public boolean isPreviewsDisabled() {
        return isPreviewsDisabled != null && isPreviewsDisabled;
    }

    /**
     * Returns the raw nullable {@code isPreviewsDisabled} flag as provided by
     * the remote sync action, preserving the distinction between an absent
     * field and an explicitly set value.
     *
     * @implNote WA Web treats absent isPreviewsDisabled as malformed; this
     *           accessor exposes the raw nullable value for handlers that need
     *           to distinguish absent from explicit false.
     * @return an {@link Optional} containing the raw {@link Boolean} value, or
     *         an empty {@code Optional} if the field was not present on the
     *         wire
     */
    public Optional<Boolean> rawIsPreviewsDisabled() {
        return Optional.ofNullable(isPreviewsDisabled);
    }

    /**
     * Sets the {@code isPreviewsDisabled} flag, which indicates whether link
     * previews should be suppressed for this account.
     *
     * @implNote {@code WAWebPrivacySettingDisableLinkPreviewsSyncAction.apply}
     *           mirrors updates to this preference from the primary device.
     * @param isPreviewsDisabled the new flag value, or {@code null} to clear
     *                           the field
     */
    public void setPreviewsDisabled(Boolean isPreviewsDisabled) {
        this.isPreviewsDisabled = isPreviewsDisabled;
    }
}
