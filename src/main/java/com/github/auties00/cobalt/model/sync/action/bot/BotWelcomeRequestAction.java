package com.github.auties00.cobalt.model.sync.action.bot;

import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

@ProtobufMessage(name = "SyncActionValue.BotWelcomeRequestAction")
public final class BotWelcomeRequestAction implements SyncAction<BotWelcomeRequestActionArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "bot_welcome_request";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 2;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_LOW;

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
    Boolean isSent;


    BotWelcomeRequestAction(Boolean isSent) {
        this.isSent = isSent;
    }

    /**
     * Returns whether the welcome message has been sent to the bot, coalescing
     * an absent value to {@code false}.
     *
     * @implNote {@code WAWebBotWelcomeRequestSyncAction.apply} treats both an
     *           absent value and an explicit {@code false} as indicating that
     *           the welcome message still needs to be sent; callers that must
     *           distinguish the two cases should use {@link #rawIsSent()}.
     * @return {@code true} if the welcome message has been sent, otherwise
     *         {@code false}
     */
    public boolean isSent() {
        return isSent != null && isSent;
    }

    /**
     * Returns the raw nullable {@code isSent} flag as provided by the remote
     * sync action, preserving the distinction between an absent field and an
     * explicitly set value.
     *
     * @implNote WA Web treats absent isSent as malformed; this accessor exposes
     *           the raw nullable value for handlers that need to distinguish
     *           absent from explicit false.
     * @return an {@link Optional} containing the raw {@link Boolean} value, or
     *         an empty {@code Optional} if the field was not present on the
     *         wire
     */
    public Optional<Boolean> rawIsSent() {
        return Optional.ofNullable(isSent);
    }

    /**
     * Sets the {@code isSent} flag, which indicates whether the welcome message
     * has been delivered to the bot.
     *
     * @implNote {@code WAWebBotWelcomeRequestSyncAction.apply} sets this flag
     *           when the welcome message is successfully dispatched.
     * @param isSent the new flag value, or {@code null} to clear the field
     */
    public void setSent(Boolean isSent) {
        this.isSent = isSent;
    }


}
