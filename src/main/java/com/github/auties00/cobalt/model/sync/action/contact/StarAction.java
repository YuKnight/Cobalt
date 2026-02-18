package com.github.auties00.cobalt.model.sync.action.contact;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.StarAction")
public final class StarAction implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean starred;


    StarAction(Boolean starred) {
        this.starred = starred;
    }

    public boolean starred() {
        return starred != null && starred;
    }

    public StarAction setStarred(Boolean starred) {
        this.starred = starred;
        return this;
    }
}
