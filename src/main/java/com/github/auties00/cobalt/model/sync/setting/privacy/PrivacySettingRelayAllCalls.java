package com.github.auties00.cobalt.model.sync.setting.privacy;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.PrivacySettingRelayAllCalls")
public final class PrivacySettingRelayAllCalls implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean isEnabled;


    PrivacySettingRelayAllCalls(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled != null && isEnabled;
    }

    public PrivacySettingRelayAllCalls setEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }
}
