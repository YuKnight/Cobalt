package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes one recipient of a broadcast message, parsed from a {@code <to>} entry
 * within the {@code <participants>} child of the message stanza.
 *
 * <p>The server attaches the broadcast contact list as a {@code <participants>} child
 * on broadcast and peer-broadcast deliveries. Each entry carries the recipient JID
 * along with the LID/PN mapping attributes used during the phone-number-to-LID
 * migration plus the per-recipient ephemeral setting.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveBroadcastParticipant {
    /**
     * Recipient's JID from the {@code jid} attribute.
     */
    private final Jid jid;

    /**
     * Per-recipient ephemeral setting from the {@code eph_setting} attribute.
     */
    private final String ephSetting;

    /**
     * Recipient's LID from the {@code peer_recipient_lid} attribute.
     */
    private final Jid peerRecipientLid;

    /**
     * Recipient's phone-number JID from the {@code peer_recipient_pn} attribute, used
     * for LID/PN migration mapping.
     */
    private final Jid peerRecipientPn;

    /**
     * Recipient's username from the {@code peer_recipient_username} attribute.
     */
    private final String peerRecipientUsername;

    /**
     * Most recent LID known for this recipient from the {@code recipient_latest_lid}
     * attribute.
     */
    private final Jid recipientLatestLid;

    /**
     * Constructs a new broadcast participant record.
     *
     * @param jid                   the recipient JID, never {@code null}
     * @param ephSetting            the ephemeral setting, or {@code null}
     * @param peerRecipientLid      the recipient LID, or {@code null}
     * @param peerRecipientPn       the recipient phone-number JID, or {@code null}
     * @param peerRecipientUsername the recipient username, or {@code null}
     * @param recipientLatestLid    the latest known LID, or {@code null}
     * @throws NullPointerException if {@code jid} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public MessageReceiveBroadcastParticipant(
            Jid jid,
            String ephSetting,
            Jid peerRecipientLid,
            Jid peerRecipientPn,
            String peerRecipientUsername,
            Jid recipientLatestLid
    ) {
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        this.ephSetting = ephSetting;
        this.peerRecipientLid = peerRecipientLid;
        this.peerRecipientPn = peerRecipientPn;
        this.peerRecipientUsername = peerRecipientUsername;
        this.recipientLatestLid = recipientLatestLid;
    }

    /**
     * Returns the recipient's JID.
     *
     * @return the recipient JID, never {@code null}
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Returns the per-recipient ephemeral setting, when present.
     *
     * @return an {@link Optional} wrapping the ephemeral setting
     */
    public Optional<String> ephSetting() {
        return Optional.ofNullable(ephSetting);
    }

    /**
     * Returns the recipient's LID, when present.
     *
     * @return an {@link Optional} wrapping the recipient LID
     */
    public Optional<Jid> peerRecipientLid() {
        return Optional.ofNullable(peerRecipientLid);
    }

    /**
     * Returns the recipient's phone-number JID, when present.
     *
     * @return an {@link Optional} wrapping the recipient phone-number JID
     */
    public Optional<Jid> peerRecipientPn() {
        return Optional.ofNullable(peerRecipientPn);
    }

    /**
     * Returns the recipient's username, when present.
     *
     * @return an {@link Optional} wrapping the recipient username
     */
    public Optional<String> peerRecipientUsername() {
        return Optional.ofNullable(peerRecipientUsername);
    }

    /**
     * Returns the most recent LID known for this recipient, when present.
     *
     * @return an {@link Optional} wrapping the latest LID
     */
    public Optional<Jid> recipientLatestLid() {
        return Optional.ofNullable(recipientLatestLid);
    }
}
