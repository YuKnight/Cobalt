package com.github.auties00.cobalt.message.addon;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Use-case label that WhatsApp mixes into the HKDF info parameter when
 * deriving a message-secret-derived encryption or MAC key.
 *
 * <p>Eight variants drive dual-encrypted addons (poll votes, encrypted
 * reactions, encrypted comments, event responses, event edits, poll edits,
 * poll add-options, message edits). Each binds its HKDF derivation to a
 * distinct label so keys derived for different use cases never collide even
 * when the parent message, sender, and stanza id are identical. The ninth
 * variant, {@link #REPORT_TOKEN}, is used by the reporting-token flow to
 * derive the HMAC key that authenticates abuse reports rather than for
 * AES-GCM encryption.
 *
 * <p>Each value records whether the use case applies additional authenticated
 * data in AES-GCM. Only poll votes and event responses set that flag.
 */
@WhatsAppWebModule(moduleName = "WAUseCaseSecret")
@WhatsAppWebModule(moduleName = "WAWebAddonEncryption")
public enum MessageAddonType {
    /**
     * Poll vote addon, dual-encrypted under a key derived with this label.
     *
     * <p>Uses AAD ({@code stanzaId + "\0" + voterJid}) so a malicious server
     * cannot reattribute a vote to a different user.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    POLL_VOTE("Poll Vote", true),

    /**
     * Encrypted reaction addon, used for reactions posted inside CAG
     * (community / announcement group) threads where the default
     * non-encrypted reaction wire format would leak emoji content to the
     * server.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    ENC_REACTION("Enc Reaction", false),

    /**
     * Encrypted comment addon, used when a comment is attached to a message
     * in a CAG thread.
     *
     * <p>The inner {@code MessageSpec} payload is dual-encrypted so the
     * server can route the comment without reading its body.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    ENC_COMMENT("Enc Comment", false),

    /**
     * Reporting token label.
     *
     * <p>Unlike the other variants, this label does not drive AES-GCM
     * encryption of an addon payload. It is mixed into the HKDF info when
     * deriving the 32-byte HMAC key used to compute franking tags that the
     * server verifies on abuse reports.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    REPORT_TOKEN("Report Token", false),

    /**
     * Event response addon (RSVP).
     *
     * <p>Uses AAD ({@code stanzaId + "\0" + responderJid}) so a malicious
     * server cannot lift an encrypted RSVP emitted by one user and replay it
     * as if it came from a different user.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    EVENT_RESPONSE("Event Response", true),

    /**
     * Event edit addon, emitted when an event organiser updates the details
     * of a previously scheduled event.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    EVENT_EDIT("Event Edit", false),

    /**
     * Poll edit addon, emitted when a poll creator updates the poll question,
     * options, or end-time.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    POLL_EDIT("Poll Edit", false),

    /**
     * Poll add-option addon, emitted when a poll participant adds a new
     * option to a poll that allows open-ended contributions.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    POLL_ADD_OPTION("Poll Add Option", false),

    /**
     * Message edit addon, emitted when a user edits a previously sent
     * message.
     *
     * <p>The edited payload is dual-encrypted so the server cannot read the
     * edit even though it still needs to route the stanza.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    MESSAGE_EDIT("Message Edit", false);

    /**
     * Label mixed into the HKDF info parameter for this use case.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String value;

    /**
     * Whether this use case authenticates the stanza id and addon sender JID
     * as AAD during AES-GCM encryption and decryption.
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryption", exports = {"encryptAddOn", "decryptAddOn"},
            adaptation = WhatsAppAdaptation.DIRECT)
    private final boolean usesAad;

    /**
     * Constructs an addon type bound to the given HKDF label and AAD flag.
     *
     * @param value   the HKDF info string associated with this use case
     * @param usesAad whether this use case authenticates stanza id and addon
     *                sender as AES-GCM AAD
     */
    MessageAddonType(String value, boolean usesAad) {
        this.value = value;
        this.usesAad = usesAad;
    }

    /**
     * Returns the label mixed into the HKDF info parameter for this use case.
     *
     * @return the HKDF info string
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "UseCaseSecretModificationType",
            adaptation = WhatsAppAdaptation.DIRECT)
    public String value() {
        return value;
    }

    /**
     * Returns whether this use case binds the stanza id and addon sender as
     * AES-GCM AAD.
     *
     * <p>Only poll votes and event responses set the flag to {@code true}.
     * These addons require per-sender binding because the server otherwise
     * sees enough structural metadata to attempt cross-user rebinding of the
     * ciphertext.
     *
     * @return {@code true} if AAD should be applied during encrypt and decrypt
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryption", exports = {"encryptAddOn", "decryptAddOn"},
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean usesAad() {
        return usesAad;
    }
}
