package com.github.auties00.cobalt.model.sync.setting;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.TimeFormatAction")
public final class TimeFormatAction implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean isTwentyFourHourFormatEnabled;


    TimeFormatAction(Boolean isTwentyFourHourFormatEnabled) {
        this.isTwentyFourHourFormatEnabled = isTwentyFourHourFormatEnabled;
    }

    public boolean isTwentyFourHourFormatEnabled() {
        return isTwentyFourHourFormatEnabled != null && isTwentyFourHourFormatEnabled;
    }

    public TimeFormatAction setTwentyFourHourFormatEnabled(Boolean isTwentyFourHourFormatEnabled) {
        this.isTwentyFourHourFormatEnabled = isTwentyFourHourFormatEnabled;
        return this;
    }
}
