package com.github.auties00.cobalt.message.receive.addressing;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;
import java.util.Optional;

/**
 * LID addressing mode.
 * Privacy-enhanced mode using opaque identifiers.
 */
public final class LidMessageAddressingMode implements MessageAddressingMode {
    private final Jid lidJid;
    private final Jid recipientPn;
    private final Jid peerPn;
    private final String username;

    public LidMessageAddressingMode(Jid lidJid, Jid recipientPn, Jid peerPn, String username) {
        this.lidJid = lidJid;
        this.recipientPn = recipientPn;
        this.peerPn = peerPn;
        this.username = username;
    }

    public LidMessageAddressingMode(Jid lidJid) {
        this(lidJid, null, null, null);
    }

    @Override
    public String protocolValue() {
        return LID_VALUE;
    }

    public Optional<Jid> recipientPn() {
        return Optional.ofNullable(recipientPn);
    }

    public Optional<Jid> peerPn() {
        return Optional.ofNullable(peerPn);
    }

    public Optional<String> username() {
        return Optional.ofNullable(username);
    }

    public Jid lidJid() {
        return lidJid;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LidMessageAddressingMode that && Objects.equals(lidJid, that.lidJid) && Objects.equals(recipientPn, that.recipientPn) && Objects.equals(peerPn, that.peerPn) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lidJid, recipientPn, peerPn, username);
    }

    @Override
    public String toString() {
        return "LidAddressingMode[" +
               "lidJid=" + lidJid + ", " +
               "recipientPn=" + recipientPn + ", " +
               "peerPn=" + peerPn + ", " +
               "username=" + username + ']';
    }
}
