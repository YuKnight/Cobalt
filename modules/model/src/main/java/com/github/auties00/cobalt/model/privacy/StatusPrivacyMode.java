package com.github.auties00.cobalt.model.privacy;

/**
 * Enumerates the distribution modes that can be configured for the user's
 * WhatsApp Status updates.
 *
 * <p>Each constant mirrors one of the settings exposed by
 * {@code WAWebStatusPrivacySettingSync} and maps onto a combination of
 * {@link PrivacySettingType#STATUS} and {@link PrivacySettingValue}.
 *
 * @implNote WAWebStatusPrivacySettingSync.applyMutations: switches on
 * {@code StatusDistributionMode} to select the target privacy value and the
 * paired allow/deny list. Cobalt exposes the high-level caller-facing view
 * via this enum, while the lower-level protobuf enum
 * {@code StatusPrivacyAction.StatusDistributionMode} is used on the wire.
 */
public enum StatusPrivacyMode {
    /**
     * Shares status updates with every contact in the address book.
     *
     * <p>Maps to {@link PrivacySettingValue#CONTACTS} with an empty jid list.
     *
     * @implNote WAWebStatusPrivacySettingSync.applyMutations:
     * {@code StatusDistributionMode.CONTACTS -> StatusPrivacySettingType.Contact}.
     */
    CONTACTS,

    /**
     * Shares status updates with every contact except for an explicit
     * blocklist of JIDs.
     *
     * <p>Maps to {@link PrivacySettingValue#CONTACTS_EXCEPT} with the blocklist
     * carried in {@link PrivacySettingEntry#excluded()}.
     *
     * @implNote WAWebStatusPrivacySettingSync.applyMutations:
     * {@code StatusDistributionMode.DENY_LIST -> StatusPrivacySettingType.DenyList}.
     */
    CONTACTS_EXCEPT,

    /**
     * Shares status updates with an explicit allowlist of JIDs and hides the
     * status from everyone else.
     *
     * <p>Maps to {@link PrivacySettingValue#CONTACTS_ONLY} with the allowlist
     * carried in {@link PrivacySettingEntry#excluded()}.
     *
     * @implNote WAWebStatusPrivacySettingSync.applyMutations:
     * {@code StatusDistributionMode.ALLOW_LIST -> StatusPrivacySettingType.AllowList}.
     */
    WHITELIST
}
