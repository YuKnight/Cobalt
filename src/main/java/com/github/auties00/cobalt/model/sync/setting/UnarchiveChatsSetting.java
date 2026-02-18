package com.github.auties00.cobalt.model.sync.setting;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.UnarchiveChatsSetting")
public final class UnarchiveChatsSetting implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean unarchiveChats;


    UnarchiveChatsSetting(Boolean unarchiveChats) {
        this.unarchiveChats = unarchiveChats;
    }

    public boolean unarchiveChats() {
        return unarchiveChats != null && unarchiveChats;
    }

    public UnarchiveChatsSetting setUnarchiveChats(Boolean unarchiveChats) {
        this.unarchiveChats = unarchiveChats;
        return this;
    }
}
