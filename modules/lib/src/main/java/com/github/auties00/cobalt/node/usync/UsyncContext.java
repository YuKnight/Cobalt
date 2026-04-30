package com.github.auties00.cobalt.node.usync;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Enumerates the {@code context} values accepted by the {@code <usync>} stanza.
 *
 * <p>The context is a free-form discriminator the relay logs and uses to triage
 * backoff. A USync that fails with {@link #INTERACTIVE} (the user just tapped
 * something) is retried sooner than one that fails with {@link #BACKGROUND}.
 * The {@link #MESSAGE} and {@link #VOIP} contexts also change backoff semantics
 * because a {@code devices} protocol failure under those contexts is exempt
 * from per-protocol backoff. The resulting stanza could no longer be encrypted
 * otherwise.
 *
 * <p>WhatsApp Web hardcodes a small set of contexts. Cobalt models them
 * explicitly so callers cannot accidentally typo a context that the relay
 * silently ignores.
 */
@WhatsAppWebModule(moduleName = "WAWebUsync")
@WhatsAppWebModule(moduleName = "WAWebUsyncBackoff")
public enum UsyncContext {
    /**
     * Interactive context. The user is waiting on the result of this query and
     * backoff is skipped entirely.
     *
     * @implNote {@code WAWebUsyncBackoff.waitForBackoff} returns a resolved
     *     promise without consulting the per-protocol backoff map when the
     *     context equals {@code "interactive"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsync",
            exports = "USyncQuery", adaptation = WhatsAppAdaptation.DIRECT)
    INTERACTIVE("interactive"),

    /**
     * Outbound-message context. Used when the query is needed to encrypt an
     * outgoing message. The {@code devices} protocol is exempt from backoff
     * because failing here would block the send.
     *
     * @implNote The combination of {@code context="message"} and the
     *     {@code devices} protocol bypasses the backoff timer in
     *     {@code WAWebUsyncBackoff.waitForBackoff}.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncBackoff",
            exports = "waitForBackoff", adaptation = WhatsAppAdaptation.DIRECT)
    MESSAGE("message"),

    /**
     * VoIP signalling context. Shares the same backoff exemption as
     * {@link #MESSAGE} for the {@code devices} protocol.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncBackoff",
            exports = "waitForBackoff", adaptation = WhatsAppAdaptation.DIRECT)
    VOIP("voip"),

    /**
     * Background context. The query was issued by an idle task and the client
     * is willing to wait through any per-protocol backoff.
     *
     * @implNote Emitted by background syncs such as
     *     {@code WAWebSyncContactsJob} during periodic refreshes.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsync",
            exports = "USyncQuery", adaptation = WhatsAppAdaptation.DIRECT)
    BACKGROUND("background"),

    /**
     * Notification context. Used for queries triggered by an inbound
     * notification stanza such as account-sync.
     *
     * @implNote Emitted by {@code WAWebHandleAccountSyncNotification} when a
     *     notification forces a re-fetch.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsync",
            exports = "USyncQuery", adaptation = WhatsAppAdaptation.DIRECT)
    NOTIFICATION("notification");

    /**
     * Holds the literal value emitted on the wire for the {@code context}
     * attribute.
     */
    private final String wireValue;

    /**
     * Creates a new {@code UsyncContext} bound to the given wire string.
     *
     * @param wireValue the literal value the relay expects
     */
    UsyncContext(String wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the literal string emitted on the {@code context} attribute of
     * the {@code <usync>} stanza.
     *
     * @return the wire value
     */
    public String wireValue() {
        return wireValue;
    }
}
