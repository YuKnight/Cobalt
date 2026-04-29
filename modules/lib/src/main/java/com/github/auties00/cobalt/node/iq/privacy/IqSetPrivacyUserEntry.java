package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.jid.Jid;
import java.util.Objects;
import java.util.Optional;

/**
 * One {@code <user/>} entry in the outbound user list.
 *
 * @implNote {@code WAWebSetPrivacyJob.createLidUserNode} for the LID
 *           variant; {@code WAWebSetPrivacyJob.g} (function {@code g})
 *           for the PN variant.
 */
@WhatsAppWebModule(moduleName = "WAWebSetPrivacyJob")
public final class IqSetPrivacyUserEntry {
    /**
     * The action to apply to this user.
     */
    private final IqSetPrivacyUserAction action;

    /**
     * The user's JID — interpreted as PN or LID per the request's
     * {@link IqSetPrivacyAddressingMode}.
     */
    private final Jid jid;

    /**
     * The optional WhatsApp username discriminator — emitted only on
     * the LID variant when the contact carries a username.
     */
    private final String username;

    /**
     * The optional PN JID echo — emitted only on the LID variant when
     * the contact has no username and the relay should fall back to
     * the legacy PN identity.
     */
    private final Jid pnJid;

    /**
     * Constructs a user entry.
     *
     * @param action   the action; never {@code null}
     * @param jid      the JID; never {@code null}
     * @param username the optional username; may be {@code null}
     * @param pnJid    the optional PN echo; may be {@code null}
     * @throws NullPointerException if {@code action} or {@code jid} is
     *                              {@code null}
     */
    public IqSetPrivacyUserEntry(IqSetPrivacyUserAction action, Jid jid, String username, Jid pnJid) {
        this.action = Objects.requireNonNull(action, "action cannot be null");
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        this.username = username;
        this.pnJid = pnJid;
    }

    /**
     * Returns the action to apply to this user.
     *
     * @return the action; never {@code null}
     */
    public IqSetPrivacyUserAction action() {
        return action;
    }

    /**
     * Returns the user's JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Returns the optional WhatsApp username.
     *
     * @return an {@link Optional} carrying the username, or empty when
     *         omitted
     */
    public Optional<String> username() {
        return Optional.ofNullable(username);
    }

    /**
     * Returns the optional PN JID echo.
     *
     * @return an {@link Optional} carrying the PN JID, or empty when
     *         omitted
     */
    public Optional<Jid> pnJid() {
        return Optional.ofNullable(pnJid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetPrivacyUserEntry) obj;
        return this.action == that.action
                && Objects.equals(this.jid, that.jid)
                && Objects.equals(this.username, that.username)
                && Objects.equals(this.pnJid, that.pnJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, jid, username, pnJid);
    }

    @Override
    public String toString() {
        return "IqSetPrivacyUserEntry[action=" + action + ", jid=" + jid
                + ", username=" + username + ", pnJid=" + pnJid + ']';
    }
}
