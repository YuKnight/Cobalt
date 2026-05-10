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
 * The outbound stanza variant. Wraps the {@code <features/>} payload
 * in the canonical
 * {@code <iq xmlns="w:biz" type="get" to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizMarketingMessageGetBusinessEligibilityRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizMarketingMessageHackBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizMarketingMessageBaseIQGetRequestMixin")
public final class SmaxGetBusinessEligibilityRequest implements SmaxOperation.Request {
    /**
     * The optional {@code meta_verified} attribute toggle on the
     * {@code <features/>} child.
     */
    private final String featuresMetaVerified;

    /**
     * The optional {@code marketing_messages} attribute toggle.
     */
    private final String featuresMarketingMessages;

    /**
     * The optional {@code genai} attribute toggle.
     */
    private final String featuresGenai;

    /**
     * The optional {@code from} attribute echoed onto the outbound IQ
     * via the {@code HackBaseIQGetRequestMixin}. The active user JID
     * is the only legal value; {@code null} omits the attribute, which
     * is the default behavior because the upstream RPC
     * ({@code WASmaxBizMarketingMessageGetBusinessEligibilityRPC.sendGetBusinessEligibilityRPC})
     * never propagates an {@code iqFrom} into
     * {@code makeGetBusinessEligibilityRequest}.
     */
    private final Jid fromUserJid;

    /**
     * Constructs a request with all three feature toggles unset and no
     * {@code from} echo.
     */
    public SmaxGetBusinessEligibilityRequest() {
        this(null, null, null, null);
    }

    /**
     * Constructs a request with the three optional feature toggles and
     * no {@code from} echo.
     *
     * @param featuresMetaVerified      the optional Meta-Verified
     *                                  toggle attribute; may be
     *                                  {@code null}
     * @param featuresMarketingMessages the optional
     *                                  marketing-messages toggle
     *                                  attribute; may be
     *                                  {@code null}
     * @param featuresGenai             the optional GenAI toggle
     *                                  attribute; may be
     *                                  {@code null}
     */
    public SmaxGetBusinessEligibilityRequest(String featuresMetaVerified,
                   String featuresMarketingMessages,
                   String featuresGenai) {
        this(featuresMetaVerified, featuresMarketingMessages, featuresGenai, null);
    }

    /**
     * Constructs a request with the three optional feature toggles and
     * an optional {@code from} echo.
     *
     * @param featuresMetaVerified      the optional Meta-Verified
     *                                  toggle attribute; may be
     *                                  {@code null}
     * @param featuresMarketingMessages the optional
     *                                  marketing-messages toggle
     *                                  attribute; may be
     *                                  {@code null}
     * @param featuresGenai             the optional GenAI toggle
     *                                  attribute; may be
     *                                  {@code null}
     * @param fromUserJid               the optional user JID to echo
     *                                  onto the {@code from}
     *                                  attribute; may be {@code null}
     */
    public SmaxGetBusinessEligibilityRequest(String featuresMetaVerified,
                   String featuresMarketingMessages,
                   String featuresGenai,
                   Jid fromUserJid) {
        this.featuresMetaVerified = featuresMetaVerified;
        this.featuresMarketingMessages = featuresMarketingMessages;
        this.featuresGenai = featuresGenai;
        this.fromUserJid = fromUserJid;
    }

    /**
     * Returns the optional Meta-Verified toggle.
     *
     * @return an {@link Optional} carrying the toggle, or empty when
     *         the attribute was omitted
     */
    public Optional<String> featuresMetaVerified() {
        return Optional.ofNullable(featuresMetaVerified);
    }

    /**
     * Returns the optional marketing-messages toggle.
     *
     * @return an {@link Optional} carrying the toggle, or empty when
     *         the attribute was omitted
     */
    public Optional<String> featuresMarketingMessages() {
        return Optional.ofNullable(featuresMarketingMessages);
    }

    /**
     * Returns the optional GenAI toggle.
     *
     * @return an {@link Optional} carrying the toggle, or empty when
     *         the attribute was omitted
     */
    public Optional<String> featuresGenai() {
        return Optional.ofNullable(featuresGenai);
    }

    /**
     * Returns the optional {@code from} echo.
     *
     * @return an {@link Optional} carrying the user JID, or empty when
     *         no echo was supplied
     */
    public Optional<Jid> fromUserJid() {
        return Optional.ofNullable(fromUserJid);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <features/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizMarketingMessageGetBusinessEligibilityRequest",
            exports = "makeGetBusinessEligibilityRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizMarketingMessageHackBaseIQGetRequestMixin",
            exports = "mergeHackBaseIQGetRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizMarketingMessageBaseIQGetRequestMixin",
            exports = "mergeBaseIQGetRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var featuresBuilder = new NodeBuilder()
                .description("features");
        if (featuresMetaVerified != null) {
            featuresBuilder.attribute("meta_verified", featuresMetaVerified);
        }
        if (featuresMarketingMessages != null) {
            featuresBuilder.attribute("marketing_messages", featuresMarketingMessages);
        }
        if (featuresGenai != null) {
            featuresBuilder.attribute("genai", featuresGenai);
        }
        var featuresNode = featuresBuilder.build();
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz") // WASmaxOutBizMarketingMessageGetBusinessEligibilityRequest.makeGetBusinessEligibilityRequest: smax("iq", {xmlns: "w:biz", smax_id: INT(139)})
                .attribute("to", JidServer.user()) // WASmaxOutBizMarketingMessageHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: to: WAWap.S_WHATSAPP_NET
                .attribute("type", "get"); // WASmaxOutBizMarketingMessageBaseIQGetRequestMixin.mergeBaseIQGetRequestMixin: type: "get" (id=generateId() delegated to WhatsAppClient.sendNode)
        if (fromUserJid != null) {
            builder.attribute("from", fromUserJid); // WASmaxOutBizMarketingMessageHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: from: OPTIONAL(USER_JID, t)
        }
        return builder.content(featuresNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGetBusinessEligibilityRequest) obj;
        return Objects.equals(this.featuresMetaVerified, that.featuresMetaVerified)
                && Objects.equals(this.featuresMarketingMessages, that.featuresMarketingMessages)
                && Objects.equals(this.featuresGenai, that.featuresGenai)
                && Objects.equals(this.fromUserJid, that.fromUserJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featuresMetaVerified, featuresMarketingMessages, featuresGenai, fromUserJid);
    }

    @Override
    public String toString() {
        return "SmaxGetBusinessEligibilityRequest[featuresMetaVerified=" + featuresMetaVerified
                + ", featuresMarketingMessages=" + featuresMarketingMessages
                + ", featuresGenai=" + featuresGenai
                + ", fromUserJid=" + fromUserJid + ']';
    }
}
