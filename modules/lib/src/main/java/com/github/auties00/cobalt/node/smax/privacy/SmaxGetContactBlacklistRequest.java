package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the
 * {@code <privacy><list value="contact_blacklist" name=NAME/></privacy>}
 * payload in the canonical {@code <iq xmlns="privacy" type="get">}
 * envelope, optionally promoting the {@code <privacy/>} envelope to LID
 * addressing.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPrivacyGetContactBlacklistRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutPrivacyGetIQMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutPrivacyBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutPrivacyGetContactBlacklistGetContactBlacklistLIDOrGetContactBlacklistPNMixinGroup")
@WhatsAppWebModule(moduleName = "WASmaxOutPrivacyGetContactBlacklistGetContactBlacklistLIDMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutPrivacyGetContactBlacklistGetContactBlacklistPNMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutPrivacyCategoryNamesForContactBlacklistMixin")
public final class SmaxGetContactBlacklistRequest implements SmaxOperation.Request {
    /**
     * The privacy category name whose contact-blacklist should be
     * fetched (e.g. {@code "last"}, {@code "profile"},
     * {@code "status"}). Routed verbatim into the {@code <list/>}
     * element's {@code name} attribute.
     */
    private final String categoryName;

    /**
     * The wire addressing mode. Selects the LID-promoted or legacy-PN
     * variant of the {@code <privacy/>} envelope.
     */
    private final SmaxGetContactBlacklistAddressingMode addressingMode;

    /**
     * Constructs a request.
     *
     * @param categoryName   the privacy category name; never
     *                       {@code null}
     * @param addressingMode the addressing mode; never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxGetContactBlacklistRequest(String categoryName, SmaxGetContactBlacklistAddressingMode addressingMode) {
        this.categoryName = Objects.requireNonNull(categoryName, "categoryName cannot be null");
        this.addressingMode = Objects.requireNonNull(addressingMode, "addressingMode cannot be null");
    }

    /**
     * Returns the privacy category name.
     *
     * @return the category name; never {@code null}
     */
    public String categoryName() {
        return categoryName;
    }

    /**
     * Returns the wire addressing mode.
     *
     * @return the addressing mode; never {@code null}
     */
    public SmaxGetContactBlacklistAddressingMode addressingMode() {
        return addressingMode;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPrivacyGetContactBlacklistRequest",
            exports = "makeGetContactBlacklistRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // merged onto: smax("list", {value: "contact_blacklist"})
        var listNode = new NodeBuilder()
                .description("list")
                .attribute("value", "contact_blacklist")
                .attribute("name", categoryName)
                .build();
        var privacyBuilder = new NodeBuilder()
                .description("privacy");
        if (addressingMode == SmaxGetContactBlacklistAddressingMode.LID) {
            privacyBuilder.attribute("addressing_mode", "lid");
        }
        var privacyNode = privacyBuilder
                .content(listNode)
                .build();
        // smax("iq", {to: S_WHATSAPP_NET, xmlns: "privacy", id: generateId(), type: "get"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "privacy")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(privacyNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGetContactBlacklistRequest) obj;
        return Objects.equals(this.categoryName, that.categoryName)
                && this.addressingMode == that.addressingMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, addressingMode);
    }

    @Override
    public String toString() {
        return "SmaxGetContactBlacklistRequest[categoryName=" + categoryName
                + ", addressingMode=" + addressingMode + ']';
    }
}
