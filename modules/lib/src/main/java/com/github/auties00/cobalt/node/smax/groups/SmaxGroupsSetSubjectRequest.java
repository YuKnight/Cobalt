package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsSetSubjectRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsSetSubjectChangeSubjectMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
public final class SmaxGroupsSetSubjectRequest implements SmaxOperation.Request {
    /**
     * The group JID whose subject is being changed.
     */
    private final Jid groupJid;

    /**
     * The new subject text (UTF-8 string, server-bounded length).
     */
    private final String subject;

    /**
     * Constructs a request.
     *
     * @param groupJid the group JID; never {@code null}
     * @param subject  the new subject text; never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxGroupsSetSubjectRequest(Jid groupJid, String subject) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.subject = Objects.requireNonNull(subject, "subject cannot be null");
    }

    /**
     * Returns the group JID.
     *
     * @return the group JID
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the new subject text.
     *
     * @return the subject
     */
    public String subject() {
        return subject;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         {@code <subject/>} payload
     *
     * @implNote {@code WASmaxOutGroupsSetSubjectRequest.makeSetSubjectRequest}
     *           composes
     *           {@code WASmaxOutGroupsSetSubjectChangeSubjectMixin}
     *           ({@code <subject>BODY</subject>}) over
     *           {@code WASmaxOutGroupsBaseSetGroupMixin}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsSetSubjectRequest",
            exports = "makeSetSubjectRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutGroupsSetSubjectChangeSubjectMixin: smax("subject", null, BODY)
        var subjectNode = new NodeBuilder()
                .description("subject")
                .content(subject.getBytes(StandardCharsets.UTF_8))
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(subjectNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsSetSubjectRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid) && Objects.equals(this.subject, that.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, subject);
    }

    @Override
    public String toString() {
        return "SmaxGroupsSetSubjectRequest[groupJid=" + groupJid + ", subject=" + subject + ']';
    }
}
