package com.github.auties00.cobalt.node.smax.coexistence;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The inbound projection of the
 * {@code <notification type="hosted"><offboarding/></notification>}
 * stanza.
 */
@WhatsAppWebModule(moduleName = "WASmaxInCoexistenceOffboardingNotificationRequest")
@WhatsAppWebModule(moduleName = "WASmaxInCoexistenceServerNotificationMixin")
@WhatsAppWebModule(moduleName = "WASmaxInCoexistenceProductSurfaceMixin")
@WhatsAppWebModule(moduleName = "WASmaxInCoexistenceEnums")
public final class SmaxCoexistenceOffboardingNotificationResponse implements SmaxOperation.Response {
    /**
     * The notification id.
     */
    private final String notificationId;

    /**
     * The notification {@code from} JID (always the WA server domain).
     */
    private final Jid notificationFrom;

    /**
     * The {@code product_surface} attribute on
     * {@code <offboarding/>} . one of {@code "ai_from_meta"},
     * {@code "automation"}, {@code "business_platform"}.
     */
    private final String offboardingProductSurface;

    /**
     * The provider-info projection.
     */
    private final SmaxCoexistenceOffboardingNotificationProviderInfo offboardingProviderInfo;

    /**
     * Constructs a new {@code SmaxCoexistenceOffboardingNotificationResponse} projection.
     *
     * @param notificationId            the notification id. Never {@code null}
     * @param notificationFrom          the from JID. Never {@code null}
     * @param offboardingProductSurface the product-surface enum literal. Never {@code null}
     * @param offboardingProviderInfo   the provider-info projection. Never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxCoexistenceOffboardingNotificationResponse(String notificationId,
                    Jid notificationFrom,
                    String offboardingProductSurface,
                    SmaxCoexistenceOffboardingNotificationProviderInfo offboardingProviderInfo) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.offboardingProductSurface = Objects.requireNonNull(offboardingProductSurface, "offboardingProductSurface cannot be null");
        this.offboardingProviderInfo = Objects.requireNonNull(offboardingProviderInfo, "offboardingProviderInfo cannot be null");
    }

    /**
     * Returns the notification id.
     *
     * @return the id. Never {@code null}
     */
    public String notificationId() {
        return notificationId;
    }

    /**
     * Returns the notification from JID.
     *
     * @return the JID. Never {@code null}
     */
    public Jid notificationFrom() {
        return notificationFrom;
    }

    /**
     * Returns the product-surface enum literal.
     *
     * @return the literal. Never {@code null}
     */
    public String offboardingProductSurface() {
        return offboardingProductSurface;
    }

    /**
     * Returns the provider-info projection.
     *
     * @return the projection. Never {@code null}
     */
    public SmaxCoexistenceOffboardingNotificationProviderInfo offboardingProviderInfo() {
        return offboardingProviderInfo;
    }

    /**
     * Tries to parse a {@link SmaxCoexistenceOffboardingNotificationResponse} projection.
     *
     * @param node the inbound notification stanza. Never {@code null}
     * @return an {@link Optional} carrying the projection
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxCoexistenceOffboardingNotificationRPC",
            exports = "receiveOffboardingNotificationRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInCoexistenceOffboardingNotificationRequest",
            exports = "parseOffboardingNotificationRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxCoexistenceOffboardingNotificationResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        // WASmaxParseUtils.assertTag(e, "notification")
        if (!node.hasDescription("notification")) {
            return Optional.empty();
        }
        // WASmaxParseUtils.flattenedChildWithTag(e, "offboarding")
        var offboarding = node.getChild("offboarding").orElse(null);
        if (offboarding == null) {
            return Optional.empty();
        }
        // WASmaxParseJid.literalJid(WASmaxParseJid.attrDomainJid, e, "from", "s.whatsapp.net"):
        // attrDomainJid validates the JID is a server-only domain JID
        // (s.whatsapp.net | g.us | call); literalJid then asserts the
        // value equals "s.whatsapp.net".
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null || !from.isServerJid(JidServer.user())) {
            return Optional.empty();
        }
        // WASmaxParseUtils.literal(WASmaxParseUtils.attrString, e, "type", "hosted")
        if (!node.hasAttribute("type", "hosted")) {
            return Optional.empty();
        }
        // WASmaxInCoexistenceProductSurfaceMixin.parseProductSurfaceMixin(offboarding):
        // inlined as WASmaxParseUtils.attrStringEnum(offboarding, "product_surface",
        //   WASmaxInCoexistenceEnums.ENUM_AIFROMMETA_AUTOMATION_BUSINESSPLATFORM)
        var productSurface = offboarding.getAttributeAsString("product_surface").orElse(null);
        if (productSurface == null
                || (!"ai_from_meta".equals(productSurface)
                && !"automation".equals(productSurface)
                && !"business_platform".equals(productSurface))) {
            return Optional.empty();
        }
        // WASmaxInCoexistenceProviderInfoMixin.parseProviderInfoMixin(offboarding)
        var providerInfo = SmaxCoexistenceOffboardingNotificationProviderInfo.of(offboarding).orElse(null);
        if (providerInfo == null) {
            return Optional.empty();
        }
        // WASmaxInCoexistenceServerNotificationMixin.parseServerNotificationMixin(e):
        // ADAPTED: only the `id` projection is consumed here. `t` (server
        // timestamp) and the optional `offline` batch index ([0, 1024]) are
        // dropped — see class-level @implNote.
        var id = node.getAttributeAsString("id").orElse(null);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxCoexistenceOffboardingNotificationResponse(id, from, productSurface, providerInfo));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxCoexistenceOffboardingNotificationResponse) obj;
        return Objects.equals(this.notificationId, that.notificationId)
                && Objects.equals(this.notificationFrom, that.notificationFrom)
                && Objects.equals(this.offboardingProductSurface, that.offboardingProductSurface)
                && Objects.equals(this.offboardingProviderInfo, that.offboardingProviderInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, notificationFrom, offboardingProductSurface, offboardingProviderInfo);
    }

    @Override
    public String toString() {
        return "SmaxCoexistenceOffboardingNotificationResponse[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", offboardingProductSurface=" + offboardingProductSurface
                + ", offboardingProviderInfo=" + offboardingProviderInfo + ']';
    }
}
