package com.github.auties00.cobalt.model.sync.setting;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.ExternalWebBetaAction")
public final class ExternalWebBetaAction implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean isOptIn;


    ExternalWebBetaAction(Boolean isOptIn) {
        this.isOptIn = isOptIn;
    }

    public boolean isOptIn() {
        return isOptIn != null && isOptIn;
    }

    public ExternalWebBetaAction setOptIn(Boolean isOptIn) {
        this.isOptIn = isOptIn;
        return this;
    }
}
