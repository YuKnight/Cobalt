package com.github.auties00.cobalt.wam.type;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import com.github.auties00.cobalt.wam.annotation.WamEnum;
import com.github.auties00.cobalt.wam.annotation.WamEnumConstant;

@WhatsAppWebModule(moduleName = "WAWebWamEnumHatchActionType")
@WamEnum
public enum HatchActionType {
    @WamEnumConstant(1) REQUEST_WELCOME_MSG_SENT,
    @WamEnumConstant(2) TAP_UNLINK_BUTTON
}
