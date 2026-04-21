package com.github.auties00.cobalt.wam.type;

import com.github.auties00.cobalt.wam.annotation.WamEnum;
import com.github.auties00.cobalt.wam.annotation.WamEnumConstant;

/**
 * Enumerates the opus-related action codes reported by WAM events that
 * track the "advanced chat privacy" (Opus) feature pipeline.
 *
 * <p>Mirrors {@code WAWebWamEnumOpusAction.OPUS_ACTION} as emitted by
 * {@code WAWebLimitSharingSettingUpdateWamEvent} and related loggers.
 */
@WamEnum
public enum OpusAction {
    /**
     * The opus pipeline is unavailable or disabled for the current event.
     */
    @WamEnumConstant(0) OPUS_NOT_WORKING,
    /**
     * An opus processing job ran successfully for the chat.
     */
    @WamEnumConstant(1) OPUS_JOB_RUN,
    /**
     * An opus fallback path ran after the primary job declined to run.
     */
    @WamEnumConstant(2) OPUS_FALLBACK_RUN,
    /**
     * The user clicked an opus-related system message in the chat.
     */
    @WamEnumConstant(3) OPUS_MESSAGE_CLICKED,
    /**
     * An opus processing job failed to complete.
     */
    @WamEnumConstant(4) OPUS_JOB_FAILED,
    /**
     * An opus fallback path failed to complete.
     */
    @WamEnumConstant(5) OPUS_FALLBACK_FAILED,
    /**
     * An opus lifecycle hook failed.
     */
    @WamEnumConstant(6) OPUS_HOOK_FAILED
}
