package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the feedback-update payload
 * in the canonical
 * {@code <iq xmlns="w:biz:msg_feedback" type="set" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizMsgUserFeedbackUpdatePreferenceRequest")
public final class SmaxUpdatePreferenceRequest implements SmaxOperation.Request {
    /**
     * The feedback action — opaque on the JS side; typical values
     * are {@code "block"} / {@code "unblock"} / {@code "report"}.
     * Routed verbatim into the {@code action} attribute of the
     * {@code <user_feedback>} child.
     */
    private final String action;

    /**
     * The contact JID this preference applies to. Routed verbatim
     * into the {@code jid} attribute of the {@code <user_feedback>}
     * child.
     */
    private final Jid jid;

    /**
     * The optional free-form feedback annotation; routed into the
     * {@code feedback} attribute when non-{@code null}.
     */
    private final String feedback;

    /**
     * Constructs a new request with no feedback annotation.
     *
     * @param action the feedback action; never {@code null}
     * @param jid    the target contact JID; never {@code null}
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    public SmaxUpdatePreferenceRequest(String action, Jid jid) {
        this(action, jid, null);
    }

    /**
     * Constructs a new request optionally carrying a free-form
     * feedback annotation.
     *
     * @param action   the feedback action; never {@code null}
     * @param jid      the target contact JID; never {@code null}
     * @param feedback the optional feedback annotation; may be
     *                 {@code null}
     * @throws NullPointerException if {@code action} or {@code jid}
     *                              is {@code null}
     */
    public SmaxUpdatePreferenceRequest(String action, Jid jid, String feedback) {
        this.action = Objects.requireNonNull(action, "action cannot be null");
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        this.feedback = feedback;
    }

    /**
     * Returns the feedback action.
     *
     * @return the action; never {@code null}
     */
    public String action() {
        return action;
    }

    /**
     * Returns the target contact JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Returns the optional feedback annotation.
     *
     * @return an {@link Optional} carrying the annotation, or empty
     *         when none was supplied
     */
    public Optional<String> feedback() {
        return Optional.ofNullable(feedback);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <user_feedback>} child
     *
     * @implNote {@code WASmaxOutBizMsgUserFeedbackUpdatePreferenceRequest.makeUpdatePreferenceRequest}
     *           composes
     *           {@code smax("iq", {xmlns: "w:biz:msg_feedback",
     *           to: S_WHATSAPP_NET, id: generateId(), type: "set"},
     *           smax("user_feedback", {action: CUSTOM_STRING(t),
     *           jid: USER_JID(t), feedback: OPTIONAL(CUSTOM_STRING, t)}))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizMsgUserFeedbackUpdatePreferenceRequest",
            exports = "makeUpdatePreferenceRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutBizMsgUserFeedbackUpdatePreferenceRequest:
        //   smax("user_feedback", {action: CUSTOM_STRING(t), jid: USER_JID(t),
        //                          feedback: OPTIONAL(CUSTOM_STRING, t)})
        var feedbackBuilder = new NodeBuilder()
                .description("user_feedback")
                .attribute("action", action)
                .attribute("jid", jid);
        if (feedback != null) {
            feedbackBuilder.attribute("feedback", feedback);
        }
        // smax("iq", {xmlns: "w:biz:msg_feedback", to: S_WHATSAPP_NET,
        //             id: generateId(), type: "set"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz:msg_feedback")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(feedbackBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxUpdatePreferenceRequest) obj;
        return Objects.equals(this.action, that.action)
                && Objects.equals(this.jid, that.jid)
                && Objects.equals(this.feedback, that.feedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, jid, feedback);
    }

    @Override
    public String toString() {
        return "SmaxUpdatePreferenceRequest[action=" + action
                + ", jid=" + jid
                + ", feedback=" + feedback + ']';
    }
}
