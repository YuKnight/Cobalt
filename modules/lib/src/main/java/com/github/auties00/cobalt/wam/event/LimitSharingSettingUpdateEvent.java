package com.github.auties00.cobalt.wam.event;

import com.github.auties00.cobalt.wam.annotation.WamEvent;
import com.github.auties00.cobalt.wam.annotation.WamProperty;
import com.github.auties00.cobalt.wam.model.WamEventSpec;
import com.github.auties00.cobalt.wam.model.WamType;
import com.github.auties00.cobalt.wam.type.OpusAction;
import com.github.auties00.cobalt.wam.type.ToggleUpdateAction;

import java.util.Optional;

/**
 * Event spec mirroring {@code WAWebLimitSharingSettingUpdateWamEvent} from
 * WhatsApp Web. Logged when the user toggles the "advanced chat privacy"
 * (a.k.a. limit-sharing) setting for a chat or group.
 *
 * <p>Definition in WA Web:
 * {@code LimitSharingSettingUpdate:[6390,{opusAction:[3,OPUS_ACTION],
 * threadId:[1,STRING],toggleUpdateAction:[2,TOGGLE_UPDATE_ACTION]},
 * [1,1,1],"regular"]}.
 */
@WamEvent(id = 6390)
public interface LimitSharingSettingUpdateEvent extends WamEventSpec {
    /**
     * Returns the thread identifier the toggle applied to, when set by the
     * caller. WA Web's {@code WAWebLimitSharingUIUtils.W(e)} call site
     * omits this property; it is reserved for other emitters.
     *
     * @return the chat or group thread id, or empty if not populated
     */
    @WamProperty(index = 1, type = WamType.STRING)
    Optional<String> threadId();

    /**
     * Returns the toggle direction chosen by the user.
     *
     * @return {@code TURN_ON} when the setting was enabled, {@code TURN_OFF}
     *         when it was disabled, or empty if unset
     */
    @WamProperty(index = 2, type = WamType.ENUM)
    Optional<ToggleUpdateAction> toggleUpdateAction();

    /**
     * Returns the opus-pipeline action associated with this setting update,
     * when set by the caller. WA Web's {@code WAWebLimitSharingUIUtils.W(e)}
     * call site omits this property; it is reserved for opus-related
     * emitters.
     *
     * @return the opus action, or empty if not populated
     */
    @WamProperty(index = 3, type = WamType.ENUM)
    Optional<OpusAction> opusAction();
}
