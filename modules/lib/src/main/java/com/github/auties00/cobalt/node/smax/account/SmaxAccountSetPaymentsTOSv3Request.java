package com.github.auties00.cobalt.node.smax.account;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.List;
import java.util.Objects;

/**
 * The outbound stanza variant. Wraps the disjunctive
 * {@link SmaxAccountSetPaymentsTOSv3ConsumerVariant} children inside an
 * {@code <iq xmlns="urn:xmpp:whatsapp:account" type="set"
 * to="s.whatsapp.net"><accept_pay version="3"
 * tos_version=INT(t)>...</accept_pay></iq>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutAccountSetPaymentsTOSv3Request")
@WhatsAppWebModule(moduleName = "WASmaxOutAccountSetIQMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutAccountBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutAccountSetPaymentsTOSv3BRConsumerOrSetPaymentsTOSv3UPIConsumerPaymentsTOSv3MixinGroup")
public final class SmaxAccountSetPaymentsTOSv3Request implements SmaxOperation.Request {
    /**
     * The integer ToS version being accepted (routed into the
     * {@code tos_version} attribute on {@code <accept_pay/>}).
     */
    private final int acceptPayTosVersion;

    /**
     * The consumer-variant payload selecting between BR and UPI.
     */
    private final SmaxAccountSetPaymentsTOSv3ConsumerVariant variant;

    /**
     * Constructs a new request.
     *
     * @param acceptPayTosVersion the ToS version
     * @param variant             the consumer-variant payload;
     *                            never {@code null}
     * @throws NullPointerException if {@code variant} is
     *                              {@code null}
     */
    public SmaxAccountSetPaymentsTOSv3Request(int acceptPayTosVersion, SmaxAccountSetPaymentsTOSv3ConsumerVariant variant) {
        this.acceptPayTosVersion = acceptPayTosVersion;
        this.variant = Objects.requireNonNull(variant, "variant cannot be null");
    }

    /**
     * Returns the ToS version being accepted.
     *
     * @return the version
     */
    public int acceptPayTosVersion() {
        return acceptPayTosVersion;
    }

    /**
     * Returns the consumer-variant payload.
     *
     * @return the variant; never {@code null}
     */
    public SmaxAccountSetPaymentsTOSv3ConsumerVariant variant() {
        return variant;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <accept_pay/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutAccountSetPaymentsTOSv3Request",
            exports = "makeSetPaymentsTOSv3Request",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutAccountSetIQMixin",
            exports = "mergeSetIQMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutAccountBaseIQSetRequestMixin",
            exports = "mergeBaseIQSetRequestMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutAccountSetPaymentsTOSv3BRConsumerOrSetPaymentsTOSv3UPIConsumerPaymentsTOSv3MixinGroup",
            exports = "mergeSetPaymentsTOSv3BRConsumerOrSetPaymentsTOSv3UPIConsumerPaymentsTOSv3MixinGroup",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutAccountSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin",
            exports = "makeSetPaymentsTOSv3BRConsumerPaymentsTOSv3AdditionalNotice",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutAccountSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin",
            exports = "mergeSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutAccountSetPaymentsTOSv3UPIConsumerPaymentsTOSv3Mixin",
            exports = "makeSetPaymentsTOSv3UPIConsumerPaymentsTOSv3AdditionalNotice",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutAccountSetPaymentsTOSv3UPIConsumerPaymentsTOSv3Mixin",
            exports = "mergeSetPaymentsTOSv3UPIConsumerPaymentsTOSv3Mixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        String service;
        List<String> notices;
        switch (variant) {
            case SmaxAccountSetPaymentsTOSv3ConsumerVariant.BrConsumer brConsumer -> {
                service = "FBPAY"; // WASmaxOutAccountSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin: smax("accept_pay", {service: "FBPAY"}, ...)
                notices = brConsumer.additionalNotices();
            }
            case SmaxAccountSetPaymentsTOSv3ConsumerVariant.UpiConsumer upiConsumer -> {
                service = "UPI"; // WASmaxOutAccountSetPaymentsTOSv3UPIConsumerPaymentsTOSv3Mixin: smax("accept_pay", {service: "UPI"}, ...)
                notices = upiConsumer.additionalNotices();
            }
        }
        // WASmaxOutAccountSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin.makeSetPaymentsTOSv3BRConsumerPaymentsTOSv3AdditionalNotice:
        // smax("additional_notice", {notice: WAWap.CUSTOM_STRING(t)}); REPEATED_CHILD enforces 1..10 (validated in BrConsumer/UpiConsumer constructor).
        var noticeNodes = new Node[notices.size()];
        for (var i = 0; i < notices.size(); i++) {
            noticeNodes[i] = new NodeBuilder()
                    .description("additional_notice")
                    .attribute("notice", notices.get(i))
                    .build();
        }
        // WASmaxOutAccountSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin (and UPI sibling): smax("accept_pay", {service}, REPEATED_CHILD(...))
        // mergeSetPaymentsTOSv3{BR|UPI}ConsumerPaymentsTOSv3Mixin: ADAPTED — instead of a separate mergeStanzas step, the accept_pay is composed directly as the IQ's content.
        var acceptPay = new NodeBuilder()
                .description("accept_pay")
                .attribute("version", "3")
                .attribute("tos_version", acceptPayTosVersion)
                .attribute("service", service)
                .content(noticeNodes)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "urn:xmpp:whatsapp:account") // WASmaxOutAccountSetIQMixin.mergeSetIQMixin: stamps xmlns="urn:xmpp:whatsapp:account"
                .attribute("to", JidServer.user()) // WASmaxOutAccountSetIQMixin.mergeSetIQMixin: stamps to=WAWap.S_WHATSAPP_NET ("s.whatsapp.net")
                .attribute("type", "set") // WASmaxOutAccountBaseIQSetRequestMixin.mergeBaseIQSetRequestMixin: stamps type="set" via WASmaxMixins.mergeStanzas; id is added by the central IQ dispatch pipeline (WAWap.generateId())
                .content(acceptPay);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxAccountSetPaymentsTOSv3Request) obj;
        return this.acceptPayTosVersion == that.acceptPayTosVersion
                && Objects.equals(this.variant, that.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acceptPayTosVersion, variant);
    }

    @Override
    public String toString() {
        return "SmaxAccountSetPaymentsTOSv3Request[acceptPayTosVersion=" + acceptPayTosVersion
                + ", variant=" + variant + ']';
    }
}
