package com.github.auties00.cobalt.calls2.common;

import java.util.Optional;

/**
 * Enumerates the target-settings selector under which the wa-voip engine stores and
 * activates a raw voip-param set.
 *
 * <p>The engine can hold several parsed voip-param sets at once, one per media context,
 * and selects exactly one of them as the in-use set for the live call. This enum is the
 * three-value key that distinguishes those sets: {@link #NONE} is the call-wide default
 * bundle that every call starts from, {@link #AUDIO} is the audio-only-call overlay, and
 * {@link #VIDEO} is the video-call overlay. The engine swaps the active set by this key
 * when the call's media mode changes, so the same manager can carry a default set plus
 * the audio and video specialisations and promote whichever one matches the current
 * call.
 *
 * <p>The numeric codes are {@code 0} for {@link #NONE}, {@code 1} for {@link #AUDIO}, and
 * {@code 2} for {@link #VIDEO}; they are contiguous and match the order the engine uses
 * when indexing its target-settings name table.
 *
 * @implNote This implementation ports the three-entry target-settings table read by
 * {@code wa_call_voip_settings_type_to_cstr} (the array indexed at data segment offset
 * {@code 0x125994}) and used as the key by {@code update_voip_params_in_use(target_settings)}
 * in {@code voip_param_internal.cc} of the wa-voip WASM module {@code ff-tScznZ8P}
 * (re/calls2-spec/SPEC.md sec 9.3; re/calls2-spec/parts/rev-common.json wireProtocol entry
 * {@code VoipSettingsType}).
 */
public enum VoipSettingsType {
    /**
     * The call-wide default bundle keyed under code {@code 0}.
     *
     * <p>This is the mandatory baseline set every call is filled from before any
     * audio-specific or video-specific overlay is selected.
     */
    NONE(0, "none"),

    /**
     * The audio-call overlay keyed under code {@code 1}.
     */
    AUDIO(1, "audio"),

    /**
     * The video-call overlay keyed under code {@code 2}.
     */
    VIDEO(2, "video");

    /**
     * The integer target-settings code the engine indexes this set by.
     */
    private final int code;

    /**
     * The lowercase token the engine prints for this set.
     */
    private final String token;

    /**
     * Constructs a target-settings constant bound to its engine code and printable token.
     *
     * @param code  the integer target-settings code the engine indexes by
     * @param token the lowercase token the engine prints for this set
     */
    VoipSettingsType(int code, String token) {
        this.code = code;
        this.token = token;
    }

    /**
     * Returns the integer target-settings code the engine indexes this set by.
     *
     * @return the engine target-settings code
     */
    public int code() {
        return code;
    }

    /**
     * Returns the lowercase token the engine prints for this set.
     *
     * <p>The token matches the string {@code wa_call_voip_settings_type_to_cstr} emits:
     * {@code "none"}, {@code "audio"}, or {@code "video"}.
     *
     * @return the printable token for this set
     */
    public String token() {
        return token;
    }

    /**
     * Returns the target-settings set whose {@linkplain #code() code} equals the given
     * value.
     *
     * @param code the engine target-settings code to resolve
     * @return the matching set, or {@link Optional#empty()} if no set matches
     */
    public static Optional<VoipSettingsType> ofCode(int code) {
        for (var type : values()) {
            if (type.code == code) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
