package com.github.auties00.cobalt.model.sync.setting;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.DetectedOutcomesStatusAction")
public final class DetectedOutcomesStatusAction implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean isEnabled;


    DetectedOutcomesStatusAction(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled != null && isEnabled;
    }

    public DetectedOutcomesStatusAction setEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }
}
