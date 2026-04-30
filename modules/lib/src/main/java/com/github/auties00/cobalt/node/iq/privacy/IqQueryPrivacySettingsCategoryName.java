package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.Optional;

/**
 * Closed set of privacy category names recognised by the relay.
 *
 * @implNote {@code WAWebPrivacySettings} ships category-specific value
 *           enums. The category-name keys themselves are not exported as
 *           a single enum but are spread across the {@code privacyParser}
 *           switch. Cobalt re-projects the keys as this enum.
 */
@WhatsAppWebModule(moduleName = "WAWebPrivacySettings")
public enum IqQueryPrivacySettingsCategoryName {
    /**
     * The {@code last} category. Last-seen visibility.
     */
    LAST_SEEN("last"),

    /**
     * The {@code online} category. Online presence visibility.
     */
    ONLINE("online"),

    /**
     * The {@code profile} category. Profile-picture visibility.
     */
    PROFILE_PICTURE("profile"),

    /**
     * The {@code status} category. About-text visibility.
     */
    ABOUT("status"),

    /**
     * The {@code readreceipts} category. Read-receipt toggle.
     */
    READ_RECEIPTS("readreceipts"),

    /**
     * The {@code groupadd} category. Who can add the user to groups.
     */
    GROUP_ADD("groupadd"),

    /**
     * The {@code calladd} category. Who can call the user.
     */
    CALL_ADD("calladd"),

    /**
     * The {@code messages} category. Message-reception filter.
     */
    MESSAGES("messages"),

    /**
     * The {@code defense} category. Defence-mode state.
     */
    DEFENSE_MODE("defense");

    /**
     * The wire string emitted in the {@code name} attribute.
     */
    private final String wire;

    /**
     * Constructs a category constant.
     *
     * @param wire the wire string. Never {@code null}
     */
    IqQueryPrivacySettingsCategoryName(String wire) {
        this.wire = wire;
    }

    /**
     * Returns the wire string for this category.
     *
     * @return the wire string. Never {@code null}
     */
    public String wire() {
        return wire;
    }

    /**
     * Resolves the {@link IqQueryPrivacySettingsCategoryName} matching the
     * supplied wire string.
     *
     * @param wire the wire string. May be {@code null}
     * @return an {@link Optional} carrying the resolved enum, or empty
     *         when no constant matches
     */
    public static Optional<IqQueryPrivacySettingsCategoryName> fromWire(String wire) {
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
