package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.TimeFormatAction;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles time format actions.
 *
 * <p>This handler processes mutations that change the time format preference (12/24 hour).
 */
public final class TimeFormatHandler implements WebAppStateActionHandler {
    public static final TimeFormatHandler INSTANCE = new TimeFormatHandler();

    private TimeFormatHandler() {

    }

    @Override
    public String actionName() {
        return TimeFormatAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return TimeFormatAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return TimeFormatAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.MD_SYNCD_24_HOUR_TIME_FORMAT_SYNC_ENABLED)) {
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof TimeFormatAction action)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setTwentyFourHourFormat(action.isTwentyFourHourFormatEnabled());
        return MutationApplicationResult.success();
    }
}
