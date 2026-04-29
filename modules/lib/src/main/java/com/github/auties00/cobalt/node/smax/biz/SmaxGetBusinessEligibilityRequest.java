package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * The outbound stanza variant — wraps the {@code <features/>} payload
 * in the canonical {@code <iq xmlns="w:biz" type="get">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizMarketingMessageGetBusinessEligibilityRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizMarketingMessageHackBaseIQGetRequestMixin")
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
     * Constructs a request with all three feature toggles unset.
     */
    public SmaxGetBusinessEligibilityRequest() {
        this(null, null, null);
    }

    /**
     * Constructs a request.
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
        this.featuresMetaVerified = featuresMetaVerified;
        this.featuresMarketingMessages = featuresMarketingMessages;
        this.featuresGenai = featuresGenai;
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
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <features/>} payload
     *
     * @implNote {@code WASmaxOutBizMarketingMessageGetBusinessEligibilityRequest.makeGetBusinessEligibilityRequest}
     *           composes
     *           {@code WASmaxOutBizMarketingMessageHackBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over a
     *           bare {@code <iq xmlns="w:biz" smax_id=139>} root
     *           that carries a single {@code <features/>} child with
     *           up to three optional attributes.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizMarketingMessageGetBusinessEligibilityRequest",
            exports = "makeGetBusinessEligibilityRequest", adaptation = WhatsAppAdaptation.DIRECT)
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
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz")
                .attribute("type", "get")
                .content(featuresNode);
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
                && Objects.equals(this.featuresGenai, that.featuresGenai);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featuresMetaVerified, featuresMarketingMessages, featuresGenai);
    }

    @Override
    public String toString() {
        return "SmaxGetBusinessEligibilityRequest[featuresMetaVerified=" + featuresMetaVerified
                + ", featuresMarketingMessages=" + featuresMarketingMessages
                + ", featuresGenai=" + featuresGenai + ']';
    }
}
