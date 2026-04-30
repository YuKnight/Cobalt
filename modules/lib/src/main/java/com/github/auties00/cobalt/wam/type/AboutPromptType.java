package com.github.auties00.cobalt.wam.type;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import com.github.auties00.cobalt.wam.annotation.WamEnum;
import com.github.auties00.cobalt.wam.annotation.WamEnumConstant;

@WhatsAppWebModule(moduleName = "WAWebWamEnumAboutPromptType")
@WamEnum
public enum AboutPromptType {
    @WamEnumConstant(1) SHARE_A_THOUGHT,
    @WamEnumConstant(2) MONDAY_MOTIVATION,
    @WamEnumConstant(3) WHATS_HAPPENING,
    @WamEnumConstant(4) CURRENT_MOOD,
    @WamEnumConstant(5) DONT_FORGET_ABOUT,
    @WamEnumConstant(6) SHARE_THOUGHT_ABOUT,
    @WamEnumConstant(7) SHARE_UP_TO,
    @WamEnumConstant(8) THOUGHTS_MOOD_PLANS,
    @WamEnumConstant(9) NEW_DAY_NEW,
    @WamEnumConstant(10) TIME_FOR_NEW,
    @WamEnumConstant(11) TODAY_THINKING,
    @WamEnumConstant(12) MIDWEEK_MOOD,
    @WamEnumConstant(13) FRIDAY_PLANS,
    @WamEnumConstant(14) UNWINDING,
    @WamEnumConstant(15) BREWING_MORNING,
    @WamEnumConstant(16) IM_FEELING,
    @WamEnumConstant(17) DROP_THOUGHT,
    @WamEnumConstant(18) RIGHT_NOW,
    @WamEnumConstant(19) WHATS_GOING_ON,
    @WamEnumConstant(20) EMOJIS_TODAY,
    @WamEnumConstant(21) EXPRESS_EMOJIS,
    @WamEnumConstant(22) SONG_REPEAT,
    @WamEnumConstant(23) PLAYLIST,
    @WamEnumConstant(24) TODAY_FEELS_LIKE,
    @WamEnumConstant(25) EASTER_HAPPY,
    @WamEnumConstant(26) EASTER_PLANS,
    @WamEnumConstant(27) EASTER_FEELS,
    @WamEnumConstant(28) EASTER_TIME_FOR
}
