package com.github.auties00.cobalt.wam.event;

import com.github.auties00.cobalt.wam.annotation.WamEvent;
import com.github.auties00.cobalt.wam.annotation.WamProperty;
import com.github.auties00.cobalt.wam.annotation.WamChannel;
import com.github.auties00.cobalt.wam.annotation.WamType;
import com.github.auties00.cobalt.wam.type.DeleteSuspendedGroupBtn;

import java.util.Optional;

@WamEvent(id = 4342, channel = WamChannel.PRIVATE, privateStatsId = 0)
public interface SuspendedGroupDeleteEvent extends WamEventSpec {
    @WamProperty(index = 1, type = WamType.ENUM)
    Optional<DeleteSuspendedGroupBtn> deleteBtnSource();
}
