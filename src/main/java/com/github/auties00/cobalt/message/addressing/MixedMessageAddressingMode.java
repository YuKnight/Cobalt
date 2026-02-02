package com.github.auties00.cobalt.message.addressing;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;
import java.util.Optional;

/**
 * Mixed addressing mode during LID migration.
 * Includes both phone number and LID for backward compatibility.
 */
public final class MixedMessageAddressingMode implements MessageAddressingMode {
    private final Jid phoneJid;
    private final Jid lidJid;
    private final boolean preferLid;
    private final String username;

    /**
     * @param phoneJid  the phone number JID
     * @param lidJid    the LID JID
     * @param preferLid whether to prefer LID for routing
     * @param username  optional username if recipient has username enabled
     */
    public MixedMessageAddressingMode(Jid phoneJid, Jid lidJid, boolean preferLid, String username) {
        this.phoneJid = phoneJid;
        this.lidJid = lidJid;
        this.preferLid = preferLid;
        this.username = username;
    }

    public MixedMessageAddressingMode(Jid phoneJid, Jid lidJid, boolean preferLid) {
        this(phoneJid, lidJid, preferLid, null);
    }

    @Override
    public String protocolValue() {
        return preferLid ? LID_VALUE : PHONE_NUMBER_VALUE;
    }

    public Optional<String> username() {
        return Optional.ofNullable(username);
    }

    public Jid phoneJid() {
        return phoneJid;
    }

    public Jid lidJid() {
        return lidJid;
    }

    public boolean preferLid() {
        return preferLid;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MixedMessageAddressingMode that && preferLid == that.preferLid && Objects.equals(phoneJid, that.phoneJid) && Objects.equals(lidJid, that.lidJid) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneJid, lidJid, preferLid, username);
    }

    @Override
    public String toString() {
        return "MixedAddressingMode[" +
               "phoneJid=" + phoneJid + ", " +
               "lidJid=" + lidJid + ", " +
               "preferLid=" + preferLid + ", " +
               "username=" + username + ']';
    }
}
