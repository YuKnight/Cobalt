package com.github.auties00.cobalt.model.sync.setting;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.UserStatusMuteAction")
public final class UserStatusMuteAction implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean muted;


    UserStatusMuteAction(Boolean muted) {
        this.muted = muted;
    }

    public boolean muted() {
        return muted != null && muted;
    }

    public UserStatusMuteAction setMuted(Boolean muted) {
        this.muted = muted;
        return this;
    }
}
