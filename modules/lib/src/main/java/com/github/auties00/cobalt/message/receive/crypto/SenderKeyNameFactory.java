package com.github.auties00.cobalt.message.receive.crypto;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.libsignal.SignalProtocolAddress;
import com.github.auties00.libsignal.groups.SignalSenderKeyName;

/**
 * Builds {@link SignalSenderKeyName} identifiers from a group JID and a sender JID for
 * use with the Signal group cipher.
 *
 * <p>Each group member has its own per-sender symmetric key keyed by the combination of
 * the group JID and the sender's device address. Both the send path (sender key
 * distribution and group encryption) and the receive path (group decryption and
 * processing distribution messages) build the identifier the same way.
 */
@WhatsAppWebModule(moduleName = "WAWebSignalCommonUtils")
public final class SenderKeyNameFactory {

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private SenderKeyNameFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns a {@link SignalSenderKeyName} that identifies the given sender's key within
     * the given group.
     *
     * <p>Combines the group JID with a Signal protocol address derived from the sender's
     * {@code user} and {@code device} components.
     *
     * @param groupJid  the group or broadcast JID hosting the sender key
     * @param senderJid the sender's device-level JID
     * @return the sender key name for the Signal group cipher
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalCommonUtils", exports = "createSignalLikeSenderKeyName",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static SignalSenderKeyName create(Jid groupJid, Jid senderJid) {
        var senderAddress = new SignalProtocolAddress(senderJid.user(), senderJid.device());
        return new SignalSenderKeyName(groupJid.toString(), senderAddress);
    }
}
