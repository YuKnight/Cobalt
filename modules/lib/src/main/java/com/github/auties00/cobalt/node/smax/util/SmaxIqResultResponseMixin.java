package com.github.auties00.cobalt.node.smax.util;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;

import java.util.Objects;

/**
 * Shared envelope-validation helper for the standard
 * {@code <iq from id type="result">} reply produced by every domain's SMAX
 * {@code Success}-shape response variant.
 *
 * <p>WhatsApp Web's {@code WASmaxIn*IQResultResponseMixin} family ships one
 * copy of this exact validation per (domain, RPC) pair. Every module looks
 * the same: assert the {@code iq} tag, echo-check the {@code id} attribute
 * against the request reference, echo-check the {@code from}/{@code to}
 * attribute, and assert {@code type="result"}. Cobalt deduplicates the
 * sixty-plus near-identical copies into this single helper that every
 * groups-domain {@code Response.Success} variant invokes through
 * {@link #validate(Node, Node)}.
 *
 * <p>The helper is intentionally lenient about the {@code from} attribute:
 * the WA Web parser delegates to {@code attrStringFromReference} which
 * tolerates a missing {@code to} on the request when the relay echoes back
 * the implicit {@code g.us} server, so callers that build a request without
 * an explicit {@code to} (the sub-set of groups RPCs addressed at
 * {@code G_US}) can still use the helper without supplying a synthetic
 * reference.
 */
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenHackBaseIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountHackBaseIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdHackBaseIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingHackBaseIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageHackBaseIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBugReportingHackBaseIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizSettingsIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBotIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBrPaymentIQResultResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBugReportingIQResultResponseMixin")
public final class SmaxIqResultResponseMixin {

    /**
     * Private constructor. The class is a static-only utility.
     */
    private SmaxIqResultResponseMixin() {
        throw new AssertionError("SmaxIqResultResponseMixin cannot be instantiated");
    }

    /**
     * Validates that the supplied reply is a well-formed
     * {@code <iq type="result">} echoing the request's {@code id} and
     * {@code to} attributes.
     *
     * <p>Returns {@code true} only when every check passes. The reply has
     * the {@code iq} tag, carries {@code type="result"}, and echoes the
     * request's {@code id} verbatim. The {@code from} echo check is applied
     * only when the request actually carries a {@code to} attribute (some
     * SMAX requests omit it because the relay defaults to the implicit
     * {@code g.us} server).
     *
     * @param reply   the inbound stanza received from the relay. Never
     *                {@code null}
     * @param request the outbound stanza emitted by the caller. Used to
     *                cross-check the echoed {@code id} and {@code from}
     *                attributes. Never {@code null}
     * @return {@code true} when {@code reply} is a result envelope echoing
     *         {@code request}'s identifiers; {@code false} otherwise
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenHackBaseIQResultResponseMixin",
            exports = "parseHackBaseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountHackBaseIQResultResponseMixin",
            exports = "parseHackBaseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdHackBaseIQResultResponseMixin",
            exports = "parseHackBaseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingHackBaseIQResultResponseMixin",
            exports = "parseHackBaseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageHackBaseIQResultResponseMixin",
            exports = "parseHackBaseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBugReportingHackBaseIQResultResponseMixin",
            exports = "parseHackBaseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBotIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBrPaymentIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBugReportingIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public static boolean validate(Node reply, Node request) {
        Objects.requireNonNull(reply, "reply cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        if (!reply.hasDescription("iq")) {
            return false;
        }
        if (!reply.hasAttribute("type", "result")) {
            return false;
        }
        var requestId = request.getAttributeAsString("id").orElse(null);
        if (requestId == null) {
            return false;
        }
        if (!reply.hasAttribute("id", requestId)) {
            return false;
        }
        var requestTo = request.getAttributeAsString("to").orElse(null);
        if (requestTo != null && !reply.hasAttribute("from", requestTo)) {
            return false;
        }
        return true;
    }
}
