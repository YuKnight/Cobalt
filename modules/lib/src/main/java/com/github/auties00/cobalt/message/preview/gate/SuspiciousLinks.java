package com.github.auties00.cobalt.message.preview.gate;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.message.preview.linkify.Linkify;

import java.util.List;

/**
 * Caller-side wrapper around {@link Idn#isSuspicious} that resolves
 * the recipient and sender country codes from JIDs before invoking
 * the homograph-attack detector.
 *
 * <p>Mirrors {@code WAWebSuspiciousLinks.findSuspiciousCharacters},
 * which itself is a thin adapter over {@code WAIdn.findSuspiciousCharacters}
 * that pulls the country codes out of the chat JID and the local
 * user's JID and forwards them to the heuristic.
 */
@WhatsAppWebModule(moduleName = "WASuspiciousLinks")
public final class SuspiciousLinks {
    /**
     * Country-code sentinel used when the recipient is a LID, group,
     * or newsletter — i.e. when no phone country code can be
     * extracted.
     */
    private static final String LID_COUNTRY_CODE_SENTINEL = "ZZ";

    /**
     * Hidden constructor for the utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private SuspiciousLinks() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns whether the URL detected at {@code match} should be
     * dropped before any preview is generated, given the local
     * client's identity and the chat being sent to.
     *
     * @param client  the WhatsApp client whose self-JID is consulted
     *                to derive the local country code
     * @param chatJid the recipient chat JID; phone users contribute
     *                their country code, LID/group/newsletter chats
     *                use {@link #LID_COUNTRY_CODE_SENTINEL}
     * @param match   the URL match
     * @return {@code true} when the host is suspicious
     */
    @WhatsAppWebExport(moduleName = "WASuspiciousLinks", exports = "findSuspiciousCharacters",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static boolean isSuspicious(WhatsAppClient client, Jid chatJid, Linkify.Match match) {
        if (match == null || match.domain() == null) {
            return false;
        }
        var host = match.domain().toLowerCase();
        var recipientCC = countryCodeFor(chatJid);
        var selfCC = countryCodeFor(client == null ? null : client.store().jid().orElse(null));
        // Web does not propagate the recipient locale into the suspicious character
        // heuristic, so the recipient language list is always passed empty.
        return Idn.isSuspicious(host, recipientCC, selfCC, List.of());
    }

    /**
     * Returns the phone country code associated with {@code jid}, or
     * the LID sentinel when no phone country code can be extracted.
     *
     * @param jid the JID
     * @return the country-code prefix, or
     *         {@link #LID_COUNTRY_CODE_SENTINEL} for non-phone JIDs
     */
    private static String countryCodeFor(Jid jid) {
        if (jid == null || !jid.hasUserServer()) {
            return LID_COUNTRY_CODE_SENTINEL;
        }
        var user = jid.user();
        if (user == null || user.isEmpty()) {
            return LID_COUNTRY_CODE_SENTINEL;
        }
        return Idn.countryCodeOf(user);
    }
}
