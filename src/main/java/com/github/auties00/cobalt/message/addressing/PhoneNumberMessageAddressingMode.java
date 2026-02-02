package com.github.auties00.cobalt.message.addressing;

import com.github.auties00.cobalt.model.jid.Jid;

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
}
