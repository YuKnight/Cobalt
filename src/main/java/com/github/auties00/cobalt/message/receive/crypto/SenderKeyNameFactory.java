package com.github.auties00.cobalt.message.receive.crypto;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.libsignal.SignalProtocolAddress;
import com.github.auties00.libsignal.groups.SignalSenderKeyName;

import java.util.Objects;

/**
 * Factory for creating SignalSenderKeyName objects from JIDs.
 * <p>
 * Used by both message sending (for encryption and sender key distribution)
 * and message receiving (for decryption and processing received sender keys).
 */
public final class SenderKeyNameFactory {

    private SenderKeyNameFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates a SignalSenderKeyName from group and sender JIDs.
     * <p>
     * The sender key name uniquely identifies a sender's key within a group,
     * combining:
     * <ul>
     *   <li>Group JID - identifies the group</li>
     *   <li>Sender address - identifies the sender within the group</li>
     * </ul>
     *
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID
     * @return the SignalSenderKeyName for use with the Signal group cipher
     */
    public static SignalSenderKeyName create(Jid groupJid, Jid senderJid) {
        var senderAddress = new SignalProtocolAddress(senderJid.user(), senderJid.device());
        return new SignalSenderKeyName(groupJid.toString(), senderAddress);
    }

    /**
     * Creates a SignalSenderKeyName from group JID and Signal protocol address.
     *
     * @param groupJid      the group JID
     * @param senderAddress the sender's Signal protocol address
     * @return the SignalSenderKeyName for use with the Signal group cipher
     */
    public static SignalSenderKeyName create(Jid groupJid, SignalProtocolAddress senderAddress) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderAddress, "senderAddress cannot be null");

        return new SignalSenderKeyName(groupJid.toString(), senderAddress);
    }
}
