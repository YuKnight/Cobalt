package com.github.auties00.cobalt.node.smax.util;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;

import java.util.Objects;

/**
 * Shared envelope-validation helper for the legacy
 * {@code <iq id type="error">} reply produced by domains that pre-date the
 * standard echoed-{@code from} contract.
 *
 * <p>Unlike {@link SmaxIqErrorResponseMixin#validate(Node, Node)}, the
 * deprecated envelope check makes the {@code from} attribute optional and,
 * when present, accepts either a domain JID
 * ({@code s.whatsapp.net}, {@code g.us}, or {@code call}) or a user JID
 * (phone, LID, interop, msgr, or bot). The relay does not have to echo
 * {@code request.to}.
 *
 * <p>The shape is the direct counterpart of WA Web's
 * {@code WASmaxIn*DeprecatedIQErrorResponseOptionalFromMixin.parseDeprecatedIQErrorResponseOptionalFromMixin}
 * family. Two byte-identical clones currently live in WA Web, one per
 * domain that still uses the legacy contract:
 * {@code WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin}
 * (the BizCtwaNativeAd domain, consumed by
 * {@code WASmaxInBizCtwaNativeAdUploadAdMediaResponseError}) and
 * {@code WASmaxInPrivacyDeprecatedIQErrorResponseOptionalFromMixin}
 * (the Privacy domain, consumed by
 * {@code WASmaxInPrivacyGetContactBlacklistResponseError}). Both share the
 * identical validation pipeline, so Cobalt collapses them into this single
 * helper.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin")
@WhatsAppWebModule(moduleName = "WASmaxInPrivacyDeprecatedIQErrorResponseOptionalFromMixin")
public final class SmaxDeprecatedIqErrorResponseOptionalFromMixin {

    /**
     * Private constructor. The class is a static-only utility.
     */
    private SmaxDeprecatedIqErrorResponseOptionalFromMixin() {
        throw new AssertionError("SmaxDeprecatedIqErrorResponseOptionalFromMixin cannot be instantiated");
    }

    /**
     * Validates that the supplied reply is a well-formed legacy
     * {@code <iq type="error">} echoing the request's {@code id} attribute,
     * with an optional {@code from} attribute that, when present, must be
     * either a domain JID or a user JID.
     *
     * <p>Returns {@code true} only when every check passes. The reply has
     * the {@code iq} tag, carries {@code type="error"}, echoes the
     * request's {@code id} verbatim, and either omits the {@code from}
     * attribute entirely or carries one that parses as a domain or user
     * JID.
     *
     * @param reply   the inbound stanza received from the relay; never
     *                {@code null}
     * @param request the outbound stanza emitted by the caller. Used to
     *                cross-check the echoed {@code id} attribute. Never
     *                {@code null}
     * @return {@code true} when {@code reply} is a legacy error envelope
     *         echoing {@code request}'s {@code id}; {@code false} otherwise
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin",
            exports = "parseDeprecatedIQErrorResponseOptionalFromMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInPrivacyDeprecatedIQErrorResponseOptionalFromMixin",
            exports = "parseDeprecatedIQErrorResponseOptionalFromMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static boolean validate(Node reply, Node request) {
        Objects.requireNonNull(reply, "reply cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        // WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin: assertTag(reply, "iq")
        if (!reply.hasDescription("iq")) {
            return false;
        }
        // WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin: attrStringFromReference(request, ["id"])
        var requestId = request.getAttributeAsString("id").orElse(null);
        if (requestId == null) {
            return false;
        }
        // WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin: literal(attrString, reply, "id", request.id)
        if (!reply.hasAttribute("id", requestId)) {
            return false;
        }
        // WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin: optional(attrJidEnum, reply, "from", DOMAINJID_USERJID)
        var from = reply.getAttributeAsString("from").orElse(null);
        if (from != null && !isDomainOrUserJid(from)) {
            return false;
        }
        // WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin: literal(attrString, reply, "type", "error")
        return reply.hasAttribute("type", "error");
    }

    /**
     * Returns whether the supplied attribute value parses as a domain JID
     * ({@code s.whatsapp.net}, {@code g.us}, or {@code call}) or as a user
     * JID (phone, LID, interop, msgr, or bot).
     *
     * @param value the raw attribute string; never {@code null}
     * @return {@code true} when {@code value} is a domain or user JID
     */
    private static boolean isDomainOrUserJid(String value) {
        Jid jid;
        try {
            jid = Jid.of(value);
        } catch (RuntimeException e) {
            return false;
        }
        var server = jid.server();
        // validateDomainJid: s.whatsapp.net | g.us | call (server-only JID).
        if (!jid.hasUser()
                && (server.equals(JidServer.user())
                || server.equals(JidServer.groupOrCommunity())
                || server.equals(JidServer.call()))) {
            return true;
        }
        // validateUserJid: any phone/LID/interop/msgr/bot user JID (must carry a user component).
        return jid.hasUser();
    }
}
