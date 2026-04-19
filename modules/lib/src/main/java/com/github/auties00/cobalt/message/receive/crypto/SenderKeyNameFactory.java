package com.github.auties00.cobalt.message.receive.crypto;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.libsignal.SignalProtocolAddress;
import com.github.auties00.libsignal.groups.SignalSenderKeyName;


/**
 * Creates {@link SignalSenderKeyName} identifiers from group and sender JIDs
 * for use with the Signal group cipher.
 *
 * <p>Every group in WhatsApp has a sender key for each of its members: a
 * per-sender symmetric key shared among group participants for the Signal
 * sender-key (SKMSG) encryption scheme. The sender key is stored and looked
 * up via a {@code SignalSenderKeyName} which combines the group JID with
 * the sender's device address. This factory produces that identifier in a
 * consistent format used by both the send path (sender key distribution
 * and group message encryption) and the receive path (group message
 * decryption and processing incoming distribution messages).
 *
 * @implNote WAWebSignalCommonUtils.createSignalLikeSenderKeyName: constructs
 * a sender key name as {@code groupJid + "::" + createSignalAddress(senderJid)}.
 * In Cobalt, the {@code SignalSenderKeyName} record encapsulates the
 * group name and the Signal protocol address.
 */
@WhatsAppWebModule(moduleName = "WAWebSignalCommonUtils")
public final class SenderKeyNameFactory {

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     * @implNote Java-specific utility class pattern; no WA Web counterpart.
     */
    private SenderKeyNameFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns a {@link SignalSenderKeyName} that uniquely identifies the
     * given sender's key within the given group.
     *
     * <p>The returned name is the key under which the sender's group
     * session state is stored and looked up. It combines the group
     * JID with a Signal protocol address derived from the sender's
     * device JID ({@code user} + {@code device}).
     *
     * @param groupJid  the group or broadcast JID that hosts the sender key
     * @param senderJid the sender's device-level JID
     * @return the sender key name for use with the Signal group cipher
     *
     * @implNote WAWebSignalCommonUtils.createSignalLikeSenderKeyName: builds
     * {@code groupJid + "::" + createSignalAddress(senderJid, deviceId)}
     * as the string representation. Cobalt constructs a
     * {@code SignalSenderKeyName} record from the group JID string and a
     * {@code SignalProtocolAddress} derived from {@code senderJid.user()}
     * and {@code senderJid.device()}.
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalCommonUtils", exports = "createSignalLikeSenderKeyName",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static SignalSenderKeyName create(Jid groupJid, Jid senderJid) {
        // WAWebSignalCommonUtils.createSignalLikeSenderKeyName
        // Builds the Signal protocol address from the sender's user and device components
        var senderAddress = new SignalProtocolAddress(senderJid.user(), senderJid.device());

        // WAWebSignalCommonUtils.createSignalLikeSenderKeyName
        // Combines the group JID string with the sender address to form the sender key name
        return new SignalSenderKeyName(groupJid.toString(), senderAddress);
    }

}
