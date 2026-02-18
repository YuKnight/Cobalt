package com.github.auties00.cobalt.model.sync.action.contact;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.LabelAssociationAction")
public final class LabelAssociationAction implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean labeled;


    LabelAssociationAction(Boolean labeled) {
        this.labeled = labeled;
    }

    public boolean labeled() {
        return labeled != null && labeled;
    }

    public LabelAssociationAction setLabeled(Boolean labeled) {
        this.labeled = labeled;
        return this;
    }
}
