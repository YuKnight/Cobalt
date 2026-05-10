package com.github.auties00.cobalt.wam.type;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import com.github.auties00.cobalt.wam.annotation.WamEnum;
import com.github.auties00.cobalt.wam.annotation.WamEnumConstant;

/**
 * Whether the local user originated the call ({@link #CALLER}) or
 * was the callee ({@link #CALLEE}). Reported in the
 * {@code callSide} (index 1) field of WAM event 462 (Call /
 * fieldstats-ready).
 */
@WhatsAppWebModule(moduleName = "WAWebWamEnumCallSide")
@WamEnum
public enum CallSide {
    /**
     * The local user placed the call.
     */
    @WamEnumConstant(1) CALLER,

    /**
     * The local user accepted (or missed) an inbound call.
     */
    @WamEnumConstant(2) CALLEE
}
