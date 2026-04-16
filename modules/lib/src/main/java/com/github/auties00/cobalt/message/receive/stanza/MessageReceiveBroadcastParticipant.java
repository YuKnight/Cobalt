package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes one recipient of a broadcast message, parsed from a
 * {@code <to>} entry within the {@code <participants>} child of the
 * message stanza.
 *
 * <p>When a broadcast or peer-broadcast message is delivered, the server
 * attaches the broadcast contact list (BCL) as a {@code <participants>}
 * child. Each entry carries the recipient JID along with the optional
 * LID/PN mapping attributes that are used for the ongoing phone-number-to-LID
 * migration, plus the per-recipient ephemeral setting. Cobalt preserves
 * these mappings so the caller can resolve each recipient's actual address
 * under the phone-less addressing model.
 *
 * @implNote WAWebHandleMsgParser function y(): parses each {@code <to>}
 * child within {@code <participants>} to extract jid, eph_setting,
 * peer_recipient_lid, peer_recipient_pn, peer_recipient_username,
 * and recipient_latest_lid.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveBroadcastParticipant {
    /**
     * The recipient's JID from the {@code jid} attribute.
     *
     * @implNote WAWebHandleMsgParser function y(): {@code jid}.
     */
    private final Jid jid;

    /**
     * The ephemeral setting for this recipient from the
     * {@code eph_setting} attribute.
     *
     * @implNote WAWebHandleMsgParser function y(): {@code ephSetting}.
     */
    private final String ephSetting;

    /**
     * The recipient's LID from the {@code peer_recipient_lid} attribute.
     *
     * @implNote WAWebHandleMsgParser function y(): {@code peerRecipientLid}.
     */
    private final Jid peerRecipientLid;

    /**
     * The recipient's phone-number JID from the {@code peer_recipient_pn}
     * attribute, used for LID/PN migration mapping.
     *
     * @implNote WAWebHandleMsgParser function y(): {@code peerRecipientPn}.
     */
    private final Jid peerRecipientPn;

    /**
     * The recipient's username from the {@code peer_recipient_username}
     * attribute.
     *
     * @implNote WAWebHandleMsgParser function y(): {@code peerRecipientUsername}.
     */
    private final String peerRecipientUsername;

    /**
     * The most recent LID known for this recipient from the
     * {@code recipient_latest_lid} attribute.
     *
     * @implNote WAWebHandleMsgParser function y(): {@code recipientLatestLid}.
     */
    private final Jid recipientLatestLid;

    /**
     * Constructs a new broadcast participant record with all parsed
     * fields.
     *
     * @param jid                   the recipient JID, never {@code null}
     * @param ephSetting            the ephemeral setting, or {@code null}
     * @param peerRecipientLid      the recipient LID, or {@code null}
     * @param peerRecipientPn       the recipient phone-number JID, or {@code null}
     * @param peerRecipientUsername the recipient username, or {@code null}
     * @param recipientLatestLid    the latest known LID, or {@code null}
     *
     * @throws NullPointerException if {@code jid} is {@code null}
     *
     * @implNote WAWebHandleMsgParser function y(): builds the broadcast
     * participant entry from the {@code <to>} child attributes.
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
     * @implNote WAWebHandleMsgParser function y(): {@code jid}.
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Returns the ephemeral setting for this recipient, when present.
     *
     * @return an {@link Optional} wrapping the ephemeral setting
     * @implNote WAWebHandleMsgParser function y(): {@code ephSetting}.
     */
    public Optional<String> ephSetting() {
        return Optional.ofNullable(ephSetting);
    }

    /**
     * Returns the recipient's LID, when present.
     *
     * @return an {@link Optional} wrapping the recipient LID
     * @implNote WAWebHandleMsgParser function y(): {@code peerRecipientLid}.
     */
    public Optional<Jid> peerRecipientLid() {
        return Optional.ofNullable(peerRecipientLid);
    }

    /**
     * Returns the recipient's phone-number JID, when present.
     *
     * @return an {@link Optional} wrapping the recipient phone-number JID
     * @implNote WAWebHandleMsgParser function y(): {@code peerRecipientPn}.
     */
    public Optional<Jid> peerRecipientPn() {
        return Optional.ofNullable(peerRecipientPn);
    }

    /**
     * Returns the recipient's username, when present.
     *
     * @return an {@link Optional} wrapping the recipient username
     * @implNote WAWebHandleMsgParser function y(): {@code peerRecipientUsername}.
     */
    public Optional<String> peerRecipientUsername() {
        return Optional.ofNullable(peerRecipientUsername);
    }

    /**
     * Returns the most recent LID known for this recipient, when present.
     *
     * @return an {@link Optional} wrapping the latest LID
     * @implNote WAWebHandleMsgParser function y(): {@code recipientLatestLid}.
     */
    public Optional<Jid> recipientLatestLid() {
        return Optional.ofNullable(recipientLatestLid);
    }
}
