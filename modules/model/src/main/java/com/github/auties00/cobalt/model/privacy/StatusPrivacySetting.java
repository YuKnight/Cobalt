package com.github.auties00.cobalt.model.privacy;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Describes the user's Status privacy configuration as exposed by the
 * WhatsApp server over the {@code xmlns="status"} IQ stanza.
 *
 * <p>The DTO pairs a {@link StatusPrivacyMode} with the optional JID list
 * applied in allow/deny mode. The list is empty for
 * {@link StatusPrivacyMode#CONTACTS}.
 *
 * @param mode the selected distribution mode; never {@code null}
 * @param jids the JIDs applied by {@link StatusPrivacyMode#WHITELIST} or
 *             {@link StatusPrivacyMode#CONTACTS_EXCEPT}; never {@code null},
 *             unmodifiable, possibly empty
 *
 * @implNote WAWebUserPrefsStatus.getStatusPrivacySetting: returns
 * {@code {setting, list}} from user preferences. Cobalt exposes the same
 * pairing as an immutable record.
 */
public record StatusPrivacySetting(StatusPrivacyMode mode, List<Jid> jids) {
    /**
     * Canonical constructor applying defensive copies and null validation.
     *
     * @param mode the distribution mode
     * @param jids the paired JID list, possibly empty
     */
    public StatusPrivacySetting {
        Objects.requireNonNull(mode, "mode cannot be null");
        jids = jids == null ? List.of() : Collections.unmodifiableList(List.copyOf(jids));
    }
}
