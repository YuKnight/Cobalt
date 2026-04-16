package com.github.auties00.cobalt.model.sync.action.device;

import com.github.auties00.cobalt.model.sync.SyncActionEmptyArgs;
import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Represents a sync action that records whether the user has opted into the
 * WhatsApp Web beta program.
 *
 * <p>Opting in unlocks early access features on linked web sessions and is
 * replicated to every companion device so the user's preference is consistent
 * across all surfaces. The mutation is singleton, so the sync index is composed
 * solely of {@link #ACTION_NAME} with no trailing arguments.
 */
@ProtobufMessage(name = "SyncActionValue.ExternalWebBetaAction")
public final class ExternalWebBetaAction implements SyncAction<SyncActionEmptyArgs> {
    /**
     * Canonical action name used as the sole component of the singleton mutation
     * index for this action type.
     */
    public static final String ACTION_NAME = "external_web_beta";

    /**
     * Schema version advertised by this action, used by sync handlers to gate
     * deserialisation and handling of newer payload shapes.
     */
    public static final int ACTION_VERSION = 3;

    /**
     * Collection this action belongs to, used by the sync protocol to route the
     * mutation into the correct replication stream.
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR;

    /**
     * Returns the canonical action name for every {@code ExternalWebBetaAction}.
     *
     * @return the constant {@link #ACTION_NAME}
     */
    @Override
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * Returns the schema version for every {@code ExternalWebBetaAction}.
     *
     * @return the constant {@link #ACTION_VERSION}
     */
    @Override
    public int actionVersion() {
        return ACTION_VERSION;
    }


    /**
     * Flag that records whether the user has opted into the WhatsApp Web beta
     * program.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean isOptIn;


    /**
     * Constructs a new {@code ExternalWebBetaAction} from raw protobuf field
     * values.
     *
     * @param isOptIn whether the user opted into the beta program, possibly
     *                {@code null}
     */
    ExternalWebBetaAction(Boolean isOptIn) {
        this.isOptIn = isOptIn;
    }

    /**
     * Returns whether the user has opted into the WhatsApp Web beta program.
     *
     * @return {@code true} if opted in, {@code false} otherwise (including when
     *         the field was unset on the wire)
     */
    public boolean isOptIn() {
        return isOptIn != null && isOptIn;
    }

    /**
     * Sets the opt in flag for the WhatsApp Web beta program.
     *
     * @param isOptIn {@code true} to opt in, {@code false} to opt out, or
     *                {@code null} to clear
     */
    public void setOptIn(Boolean isOptIn) {
        this.isOptIn = isOptIn;
    }
}
