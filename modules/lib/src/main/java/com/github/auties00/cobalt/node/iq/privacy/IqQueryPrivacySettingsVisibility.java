package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.Optional;

/**
 * Closed set of visibility values that a privacy category can take.
 *
 * @implNote Union of {@code WAWebPrivacySettings.VISIBILITY},
 *           {@code ONLINE_VISIBILITY}, {@code ALL_NONE}, {@code CALL_ADD},
 *           {@code DEFENSE_MODE_STATE}. The {@code error} marker is not
 *           modelled — when the relay emits {@code value="error"} for
 *           a category, the Cobalt parser skips that entry mirroring
 *           WA Web's logging behaviour.
 */
@WhatsAppWebModule(moduleName = "WAWebPrivacySettings")
public enum IqQueryPrivacySettingsVisibility {
    /**
     * The {@code all} value — visible to / available from everyone.
     */
    ALL("all"),

    /**
     * The {@code contacts} value — restricted to address-book contacts.
     */
    CONTACTS("contacts"),

    /**
     * The {@code contact_blacklist} value — visible to contacts except
     * the named blacklist set.
     */
    CONTACT_BLACKLIST("contact_blacklist"),

    /**
     * The {@code none} value — hidden from everyone.
     */
    NONE("none"),

    /**
     * The {@code match_last_seen} value — only valid for the
     * {@link IqQueryPrivacySettingsCategoryName#ONLINE} category;
     * mirrors the {@code last} setting.
     */
    MATCH_LAST_SEEN("match_last_seen"),

    /**
     * The {@code known} value — only valid for the
     * {@link IqQueryPrivacySettingsCategoryName#CALL_ADD} category;
     * restricts callers to previously-known peers.
     */
    KNOWN("known"),

    /**
     * The {@code off} value — only valid for the
     * {@link IqQueryPrivacySettingsCategoryName#DEFENSE_MODE} category;
     * defence mode disabled.
     */
    OFF("off"),

    /**
     * The {@code on_standard} value — only valid for the
     * {@link IqQueryPrivacySettingsCategoryName#DEFENSE_MODE} category;
     * defence mode enabled in standard tier.
     */
    ON_STANDARD("on_standard");

    /**
     * The wire string carried in the {@code value} attribute.
     */
    private final String wire;

    /**
     * Constructs a visibility constant.
     *
     * @param wire the wire string; never {@code null}
     */
    IqQueryPrivacySettingsVisibility(String wire) {
        this.wire = wire;
    }

    /**
     * Returns the wire string for this visibility.
     *
     * @return the wire string; never {@code null}
     */
    public String wire() {
        return wire;
    }

    /**
     * Resolves the {@link IqQueryPrivacySettingsVisibility} matching
     * the supplied wire string.
     *
     * @param wire the wire string; may be {@code null}
     * @return an {@link Optional} carrying the resolved enum, or empty
     *         when no constant matches (notably the {@code error}
     *         marker emitted by the relay for unsupported values)
     */
    public static Optional<IqQueryPrivacySettingsVisibility> fromWire(String wire) {
        if (wire == null) {
            return Optional.empty();
        }
        for (var value : values()) {
            if (value.wire.equals(wire)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
