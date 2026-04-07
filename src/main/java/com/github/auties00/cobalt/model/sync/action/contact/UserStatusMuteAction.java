package com.github.auties00.cobalt.model.sync.action.contact;

import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

@ProtobufMessage(name = "SyncActionValue.UserStatusMuteAction")
public final class UserStatusMuteAction implements SyncAction<UserStatusMuteActionArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "userStatusMute";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 7;

    /**
     * Canonical WhatsApp Web collection name for this action type.
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


    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean muted;


    UserStatusMuteAction(Boolean muted) {
        this.muted = muted;
    }

    /**
     * Returns whether the user status is muted, coalescing an absent value to
     * {@code false}.
     *
     * @implNote {@code WAWebUserStatusMuteSyncAction.apply} treats both an
     *           absent value and an explicit {@code false} as "not muted";
     *           callers that must distinguish the two cases should use
     *           {@link #rawMuted()}.
     * @return {@code true} if the user status is muted, otherwise {@code false}
     */
    public boolean muted() {
        return muted != null && muted;
    }

    /**
     * Returns the raw nullable {@code muted} flag as provided by the remote
     * sync action, preserving the distinction between an absent field and an
     * explicitly set value.
     *
     * @implNote WA Web treats absent muted as malformed; this accessor exposes
     *           the raw nullable value for handlers that need to distinguish
     *           absent from explicit false.
     * @return an {@link Optional} containing the raw {@link Boolean} value, or
     *         an empty {@code Optional} if the field was not present on the
     *         wire
     */
    public Optional<Boolean> rawMuted() {
        return Optional.ofNullable(muted);
    }

    /**
     * Sets the {@code muted} flag, which indicates whether the user status is
     * muted.
     *
     * @implNote {@code WAWebUserStatusMuteSyncAction.apply} mirrors updates to
     *           this flag from the primary device.
     * @param muted the new flag value, or {@code null} to clear the field
     */
    public void setMuted(Boolean muted) {
        this.muted = muted;
    }


}
