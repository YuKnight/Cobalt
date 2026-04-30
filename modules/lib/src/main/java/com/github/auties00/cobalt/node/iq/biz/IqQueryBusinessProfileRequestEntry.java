package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.model.jid.Jid;
import java.util.Objects;
import java.util.Optional;

/**
 * Typed {@code (businessJid, tag)} entry carried inside an
 * {@link IqQueryBusinessProfileRequest}. Each entry produces one
 * {@code <profile jid tag/>} child of the outbound payload; supplying the
 * tag lets the relay omit the full profile body when its cached version
 * matches.
 */
public final class IqQueryBusinessProfileRequestEntry {
    /**
     * The business JID being queried.
     */
    private final Jid businessJid;

    /**
     * The optional version tag — when supplied, the relay omits the
     * full profile body if the tag matches the cached version.
     */
    private final Integer tag;

    /**
     * Constructs an entry.
     *
     * @param businessJid the business JID; never {@code null}
     * @param tag         the optional version tag; may be {@code null}
     * @throws NullPointerException if {@code businessJid} is {@code null}
     */
    public IqQueryBusinessProfileRequestEntry(Jid businessJid, Integer tag) {
        this.businessJid = Objects.requireNonNull(businessJid, "businessJid cannot be null");
        this.tag = tag;
    }

    /**
     * Returns the business JID.
     *
     * @return the business JID; never {@code null}
     */
    public Jid businessJid() {
        return businessJid;
    }

    /**
     * Returns the optional version tag.
     *
     * @return an {@link Optional} carrying the tag
     */
    public Optional<Integer> tag() {
        return Optional.ofNullable(tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqQueryBusinessProfileRequestEntry) obj;
        return Objects.equals(this.businessJid, that.businessJid)
                && Objects.equals(this.tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessJid, tag);
    }

    @Override
    public String toString() {
        return "IqQueryBusinessProfileRequestEntry[businessJid=" + businessJid
                + ", tag=" + tag + ']';
    }
}
