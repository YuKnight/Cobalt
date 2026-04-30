package com.github.auties00.cobalt.wam.event;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import com.github.auties00.cobalt.wam.annotation.WamEvent;
import com.github.auties00.cobalt.wam.model.WamEventSpec;
import com.github.auties00.cobalt.wam.annotation.WamProperty;
import com.github.auties00.cobalt.wam.model.WamChannel;
import com.github.auties00.cobalt.wam.model.WamType;
import com.github.auties00.cobalt.wam.type.MmSignalType;

import java.util.Optional;
import java.util.OptionalInt;

@WhatsAppWebModule(moduleName = "WAWebMmSignalRealtimeWebWamEvent")
@WamEvent(id = 7860, channel = WamChannel.PRIVATE, privateStatsId = 0)
public interface MmSignalRealtimeWebEvent extends WamEventSpec {
    @WamProperty(index = 1, type = WamType.INTEGER)
    OptionalInt mmCarouselCardIndex();

    @WamProperty(index = 2, type = WamType.INTEGER)
    OptionalInt mmCtaButtonIndex();

    @WamProperty(index = 3, type = WamType.STRING)
    Optional<String> mmSignalData();

    @WamProperty(index = 4, type = WamType.ENUM)
    Optional<MmSignalType> mmSignalType();
}
