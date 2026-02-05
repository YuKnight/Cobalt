package com.github.auties00.cobalt.message.addressing;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.libsignal.SignalProtocolAddress;
import com.github.auties00.libsignal.groups.SignalSenderKeyName;

import java.util.Objects;
import java.util.Optional;

/**
 * Phone Number addressing mode.
 * Traditional mode using phone numbers as identifiers.
 */
public final class PhoneNumberMessageAddressingMode implements MessageAddressingMode {
    private final Jid phoneJid;
    private final Jid peerLid;
    private final String username;

    /**
     * @param phoneJid the phone number JID
     * @param peerLid  optional LID for migration (peer_recipient_lid)
     * @param username optional username if recipient has username enabled
     */
    public PhoneNumberMessageAddressingMode(Jid phoneJid, Jid peerLid, String username) {
        this.phoneJid = phoneJid;
        this.peerLid = peerLid;
        this.username = username;
    }

    public PhoneNumberMessageAddressingMode(Jid phoneJid) {
        this(phoneJid, null, null);
    }

    @Override
    public String protocolValue() {
        return PHONE_NUMBER_VALUE;
    }

    public Optional<Jid> peerLid() {
        return Optional.ofNullable(peerLid);
    }

    public Optional<String> username() {
        return Optional.ofNullable(username);
    }

    public Jid phoneJid() {
        return phoneJid;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PhoneNumberMessageAddressingMode that && Objects.equals(phoneJid, that.phoneJid) && Objects.equals(peerLid, that.peerLid) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneJid, peerLid, username);
    }

    @Override
    public String toString() {
        return "PhoneNumber[" +
               "phoneJid=" + phoneJid + ", " +
               "peerLid=" + peerLid + ", " +
               "username=" + username + ']';
    }

    /**
     * Factory for creating SignalSenderKeyName objects from JIDs.
     * <p>
     * Used by both message sending (for encryption and sender key distribution)
     * and message receiving (for decryption and processing received sender keys).
     */
    public static final class SenderKeyNameFactory {

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
}
