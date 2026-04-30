package com.github.auties00.cobalt.wam.event;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import com.github.auties00.cobalt.wam.annotation.WamEvent;
import com.github.auties00.cobalt.wam.model.WamEventSpec;
import com.github.auties00.cobalt.wam.annotation.WamProperty;
import com.github.auties00.cobalt.wam.model.WamType;

import java.util.Optional;
import java.util.OptionalInt;

@WhatsAppWebModule(moduleName = "WAWebAboutConsumptionDailyWamEvent")
@WamEvent(id = 6816)
public interface AboutConsumptionDailyEvent extends WamEventSpec {
    @WamProperty(index = 1, type = WamType.INTEGER)
    OptionalInt aboutChatBubbleTapCount();

    @WamProperty(index = 2, type = WamType.INTEGER)
    OptionalInt aboutChatConsumptionCount();

    @WamProperty(index = 3, type = WamType.STRING)
    Optional<String> aboutLocale();

    @WamProperty(index = 4, type = WamType.INTEGER)
    OptionalInt aboutMessageSendCount();
}
