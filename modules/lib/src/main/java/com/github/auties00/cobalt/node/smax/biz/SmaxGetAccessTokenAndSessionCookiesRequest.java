package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the verification-code payload
 * in the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaAdAccountGetAccessTokenAndSessionCookiesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaAdAccountHackBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaAdAccountBaseIQGetRequestMixin")
public final class SmaxGetAccessTokenAndSessionCookiesRequest implements SmaxOperation.Request {
    /**
     * The user-supplied verification code (the email-recovery code
     * the user typed into the UI). Embedded as the text content of
     * the {@code <code/>} child of the {@code <parameters/>} payload.
     */
    private final String code;

    /**
     * The optional {@code from} attribute echoed onto the outbound IQ
     * via the {@code HackBaseIQGetRequestMixin}; may be {@code null}.
     */
    private final Jid fromUserJid;

    /**
     * Constructs a request for the given verification code with no
     * {@code from} echo.
     *
     * @param code the verification code; never {@code null}
     * @throws NullPointerException if {@code code} is {@code null}
     */
    public SmaxGetAccessTokenAndSessionCookiesRequest(String code) {
        this(code, null);
    }

    /**
     * Constructs a request for the given verification code,
     * optionally echoing the supplied user JID onto the {@code from}
     * attribute.
     *
     * @param code        the verification code; never {@code null}
     * @param fromUserJid the optional user JID to echo onto the
     *                    {@code from} attribute; may be {@code null}
     * @throws NullPointerException if {@code code} is {@code null}
     */
    public SmaxGetAccessTokenAndSessionCookiesRequest(String code, Jid fromUserJid) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.fromUserJid = fromUserJid;
    }

    /**
     * Returns the verification code.
     *
     * @return the code; never {@code null}
     */
    public String code() {
        return code;
    }

    /**
     * Returns the optional {@code from} echo.
     *
     * @return an {@link Optional} carrying the user JID, or empty
     *         when no echo was supplied
     */
    public Optional<Jid> fromUserJid() {
        return Optional.ofNullable(fromUserJid);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <parameters/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaAdAccountGetAccessTokenAndSessionCookiesRequest",
            exports = "makeGetAccessTokenAndSessionCookiesRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaAdAccountHackBaseIQGetRequestMixin",
            exports = "mergeHackBaseIQGetRequestMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaAdAccountBaseIQGetRequestMixin",
            exports = "mergeBaseIQGetRequestMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var codeNode = new NodeBuilder()
                .description("code")
                .content(code)
                .build();
        var parametersNode = new NodeBuilder()
                .description("parameters")
                .content(codeNode)
                .build();
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("to", JidServer.user()) // WASmaxOutBizCtwaAdAccountHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: to: WAWap.S_WHATSAPP_NET
                .attribute("type", "get") // WASmaxOutBizCtwaAdAccountBaseIQGetRequestMixin.mergeBaseIQGetRequestMixin: type: "get" (id=generateId() delegated to WhatsAppClient.sendNode)
                .content(parametersNode);
        if (fromUserJid != null) {
            builder.attribute("from", fromUserJid); // WASmaxOutBizCtwaAdAccountHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: from: OPTIONAL(USER_JID, t)
        }
        return builder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGetAccessTokenAndSessionCookiesRequest) obj;
        return Objects.equals(this.code, that.code)
                && Objects.equals(this.fromUserJid, that.fromUserJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, fromUserJid);
    }

    @Override
    public String toString() {
        return "SmaxGetAccessTokenAndSessionCookiesRequest[code=" + code
                + ", fromUserJid=" + fromUserJid + ']';
    }
}
