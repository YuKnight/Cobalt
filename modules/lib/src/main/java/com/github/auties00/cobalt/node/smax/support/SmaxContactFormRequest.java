package com.github.auties00.cobalt.node.smax.support;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSupportContactFormRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutSupportHackBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSupportBaseIQSetRequestMixin")
public final class SmaxContactFormRequest implements SmaxOperation.Request {
    /**
     * The optional sender JID. When set, routed verbatim into
     * the IQ's {@code from} attribute.
     */
    private final Jid iqFrom;

    /**
     * The free-form support description (the textarea content);
     * never {@code null}.
     */
    private final String descriptionElementValue;

    /**
     * The optional pre-filled topic title (the dropdown label).
     */
    private final String topicElementValue;

    /**
     * The optional canonical topic id (the dropdown value).
     */
    private final String topicIdElementValue;

    /**
     * The optional debug-information JSON blob.
     */
    private final String debugInformationJsonElementValue;

    /**
     * The optional uploaded-crashlog handle.
     */
    private final String uploadedLogsIdElementValue;

    /**
     * The optional {@code context_flow} marker passed via the
     * {@code <additional_attributes/>} child.
     */
    private final String additionalAttributesContextFlow;

    /**
     * Constructs a new contact-form request.
     *
     * @param iqFrom                          the optional sender
     *                                        JID; may be
     *                                        {@code null}
     * @param descriptionElementValue         the free-form
     *                                        description; never
     *                                        {@code null}
     * @param topicElementValue               the optional topic
     *                                        title; may be
     *                                        {@code null}
     * @param topicIdElementValue             the optional topic
     *                                        id; may be
     *                                        {@code null}
     * @param debugInformationJsonElementValue the optional debug
     *                                        JSON; may be
     *                                        {@code null}
     * @param uploadedLogsIdElementValue      the optional crashlog
     *                                        handle; may be
     *                                        {@code null}
     * @param additionalAttributesContextFlow the optional
     *                                        context-flow marker;
     *                                        may be {@code null}
     * @throws NullPointerException if {@code descriptionElementValue}
     *                              is {@code null}
     */
    public SmaxContactFormRequest(Jid iqFrom,
                   String descriptionElementValue,
                   String topicElementValue,
                   String topicIdElementValue,
                   String debugInformationJsonElementValue,
                   String uploadedLogsIdElementValue,
                   String additionalAttributesContextFlow) {
        this.iqFrom = iqFrom;
        this.descriptionElementValue = Objects.requireNonNull(descriptionElementValue,
                "descriptionElementValue cannot be null");
        this.topicElementValue = topicElementValue;
        this.topicIdElementValue = topicIdElementValue;
        this.debugInformationJsonElementValue = debugInformationJsonElementValue;
        this.uploadedLogsIdElementValue = uploadedLogsIdElementValue;
        this.additionalAttributesContextFlow = additionalAttributesContextFlow;
    }

    /**
     * Returns the optional sender JID.
     *
     * @return an {@link Optional} carrying the JID
     */
    public Optional<Jid> iqFrom() {
        return Optional.ofNullable(iqFrom);
    }

    /**
     * Returns the free-form support description.
     *
     * @return the description; never {@code null}
     */
    public String descriptionElementValue() {
        return descriptionElementValue;
    }

    /**
     * Returns the optional topic title.
     *
     * @return an {@link Optional} carrying the title
     */
    public Optional<String> topicElementValue() {
        return Optional.ofNullable(topicElementValue);
    }

    /**
     * Returns the optional topic id.
     *
     * @return an {@link Optional} carrying the id
     */
    public Optional<String> topicIdElementValue() {
        return Optional.ofNullable(topicIdElementValue);
    }

    /**
     * Returns the optional debug-information JSON blob.
     *
     * @return an {@link Optional} carrying the JSON
     */
    public Optional<String> debugInformationJsonElementValue() {
        return Optional.ofNullable(debugInformationJsonElementValue);
    }

    /**
     * Returns the optional uploaded-crashlog handle.
     *
     * @return an {@link Optional} carrying the handle
     */
    public Optional<String> uploadedLogsIdElementValue() {
        return Optional.ofNullable(uploadedLogsIdElementValue);
    }

    /**
     * Returns the optional context-flow marker.
     *
     * @return an {@link Optional} carrying the marker
     */
    public Optional<String> additionalAttributesContextFlow() {
        return Optional.ofNullable(additionalAttributesContextFlow);
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         payload
     *
     * @implNote {@code WASmaxOutSupportContactFormRequest.makeContactFormRequest}
     *           composes
     *           {@code WASmaxOutSupportHackBaseIQSetRequestMixin}
     *           ({@code from? to=S_WHATSAPP_NET}) and
     *           {@code WASmaxOutSupportBaseIQSetRequestMixin}
     *           ({@code id=generateId() type="set"}) over a
     *           payload of one
     *           {@code <description/>} mandatory child plus four
     *           OPTIONAL_CHILD entries
     *           ({@code <topic/>}, {@code <topic_id/>},
     *           {@code <debug_information_json/>},
     *           {@code <uploaded_logs_id/>}) and an
     *           {@code <additional_attributes context_flow=…/>}
     *           OPTIONAL_CHILD when present.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutSupportContactFormRequest",
            exports = "makeContactFormRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        children.add(new NodeBuilder()
                .description("description")
                .content(descriptionElementValue)
                .build());
        if (topicElementValue != null) {
            children.add(new NodeBuilder()
                    .description("topic")
                    .content(topicElementValue)
                    .build());
        }
        if (topicIdElementValue != null) {
            children.add(new NodeBuilder()
                    .description("topic_id")
                    .content(topicIdElementValue)
                    .build());
        }
        if (debugInformationJsonElementValue != null) {
            children.add(new NodeBuilder()
                    .description("debug_information_json")
                    .content(debugInformationJsonElementValue)
                    .build());
        }
        if (uploadedLogsIdElementValue != null) {
            children.add(new NodeBuilder()
                    .description("uploaded_logs_id")
                    .content(uploadedLogsIdElementValue)
                    .build());
        }
        if (additionalAttributesContextFlow != null) {
            children.add(new NodeBuilder()
                    .description("additional_attributes")
                    .attribute("context_flow", additionalAttributesContextFlow)
                    .build());
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("smax_id", 3)
                .attribute("from", iqFrom)
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .content(children);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxContactFormRequest) obj;
        return Objects.equals(this.iqFrom, that.iqFrom)
                && Objects.equals(this.descriptionElementValue, that.descriptionElementValue)
                && Objects.equals(this.topicElementValue, that.topicElementValue)
                && Objects.equals(this.topicIdElementValue, that.topicIdElementValue)
                && Objects.equals(this.debugInformationJsonElementValue, that.debugInformationJsonElementValue)
                && Objects.equals(this.uploadedLogsIdElementValue, that.uploadedLogsIdElementValue)
                && Objects.equals(this.additionalAttributesContextFlow, that.additionalAttributesContextFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iqFrom, descriptionElementValue, topicElementValue, topicIdElementValue,
                debugInformationJsonElementValue, uploadedLogsIdElementValue,
                additionalAttributesContextFlow);
    }

    @Override
    public String toString() {
        return "SmaxContactFormRequest[iqFrom=" + iqFrom
                + ", descriptionElementValue=" + descriptionElementValue
                + ", topicElementValue=" + topicElementValue
                + ", topicIdElementValue=" + topicIdElementValue
                + ", debugInformationJsonElementValue=" + debugInformationJsonElementValue
                + ", uploadedLogsIdElementValue=" + uploadedLogsIdElementValue
                + ", additionalAttributesContextFlow=" + additionalAttributesContextFlow + ']';
    }
}
