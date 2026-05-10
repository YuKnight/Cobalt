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
 * {@code <notification type="hosted"><onboarding_status/></notification>}
 * stanza.
 */
@WhatsAppWebModule(moduleName = "WASmaxInCoexistenceOnboardingStatusNotificationRequest")
@WhatsAppWebModule(moduleName = "WASmaxInCoexistenceServerNotificationMixin")
@WhatsAppWebModule(moduleName = "WASmaxInCoexistenceProductSurfaceMixin")
@WhatsAppWebModule(moduleName = "WASmaxInCoexistenceEnums")
public final class SmaxCoexistenceOnboardingStatusNotificationResponse implements SmaxOperation.Response {
    /**
     * The notification id.
     */
    private final String notificationId;

    /**
     * The notification {@code from} JID (always the WA server domain).
     */
    private final Jid notificationFrom;

    /**
     * The {@code status} attribute on {@code <onboarding_status/>} .
     * one of {@code "completed"}, {@code "failed"}.
     */
    private final String onboardingStatusStatus;

    /**
     * The {@code product_surface} attribute on
     * {@code <onboarding_status/>} . one of {@code "ai_from_meta"},
     * {@code "automation"}, {@code "business_platform"}.
     */
    private final String onboardingStatusProductSurface;

    /**
     * The provider-info projection.
     */
    private final SmaxCoexistenceOffboardingNotificationProviderInfo onboardingStatusProviderInfo;

    /**
     * Constructs a new {@code SmaxCoexistenceOnboardingStatusNotificationResponse} projection.
     *
     * @param notificationId                 the notification id. Never {@code null}
     * @param notificationFrom               the from JID. Never {@code null}
     * @param onboardingStatusStatus         the status enum literal. Never {@code null}
     * @param onboardingStatusProductSurface the product-surface enum literal. Never {@code null}
     * @param onboardingStatusProviderInfo   the provider-info projection. Never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxCoexistenceOnboardingStatusNotificationResponse(String notificationId,
                    Jid notificationFrom,
                    String onboardingStatusStatus,
                    String onboardingStatusProductSurface,
                    SmaxCoexistenceOffboardingNotificationProviderInfo onboardingStatusProviderInfo) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.onboardingStatusStatus = Objects.requireNonNull(onboardingStatusStatus, "onboardingStatusStatus cannot be null");
        this.onboardingStatusProductSurface = Objects.requireNonNull(onboardingStatusProductSurface, "onboardingStatusProductSurface cannot be null");
        this.onboardingStatusProviderInfo = Objects.requireNonNull(onboardingStatusProviderInfo, "onboardingStatusProviderInfo cannot be null");
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
     * Returns the status enum literal.
     *
     * @return the literal. Never {@code null}
     */
    public String onboardingStatusStatus() {
        return onboardingStatusStatus;
    }

    /**
     * Returns the product-surface enum literal.
     *
     * @return the literal. Never {@code null}
     */
    public String onboardingStatusProductSurface() {
        return onboardingStatusProductSurface;
    }

    /**
     * Returns the provider-info projection.
     *
     * @return the projection. Never {@code null}
     */
    public SmaxCoexistenceOffboardingNotificationProviderInfo onboardingStatusProviderInfo() {
        return onboardingStatusProviderInfo;
    }

    /**
     * Tries to parse a {@link SmaxCoexistenceOnboardingStatusNotificationResponse} projection.
     *
     * @param node the inbound notification stanza. Never {@code null}
     * @return an {@link Optional} carrying the projection
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxCoexistenceOnboardingStatusNotificationRPC",
            exports = "receiveOnboardingStatusNotificationRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInCoexistenceOnboardingStatusNotificationRequest",
            exports = "parseOnboardingStatusNotificationRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxCoexistenceOnboardingStatusNotificationResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        // WASmaxParseUtils.assertTag(e, "notification")
        if (!node.hasDescription("notification")) {
            return Optional.empty();
        }
        // WASmaxParseUtils.flattenedChildWithTag(e, "onboarding_status")
        var onboardingStatus = node.getChild("onboarding_status").orElse(null);
        if (onboardingStatus == null) {
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
        // WASmaxParseUtils.attrStringEnum(onboardingStatus, "status",
        //   WASmaxInCoexistenceEnums.ENUM_COMPLETED_FAILED)
        var status = onboardingStatus.getAttributeAsString("status").orElse(null);
        if (status == null || (!"completed".equals(status) && !"failed".equals(status))) {
            return Optional.empty();
        }
        // WASmaxInCoexistenceProductSurfaceMixin.parseProductSurfaceMixin(onboardingStatus):
        // inlined as WASmaxParseUtils.attrStringEnum(onboardingStatus, "product_surface",
        //   WASmaxInCoexistenceEnums.ENUM_AIFROMMETA_AUTOMATION_BUSINESSPLATFORM)
        var productSurface = onboardingStatus.getAttributeAsString("product_surface").orElse(null);
        if (productSurface == null
                || (!"ai_from_meta".equals(productSurface)
                && !"automation".equals(productSurface)
                && !"business_platform".equals(productSurface))) {
            return Optional.empty();
        }
        // WASmaxInCoexistenceProviderInfoMixin.parseProviderInfoMixin(onboardingStatus)
        var providerInfo = SmaxCoexistenceOffboardingNotificationProviderInfo.of(onboardingStatus).orElse(null);
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
        return Optional.of(new SmaxCoexistenceOnboardingStatusNotificationResponse(id, from, status, productSurface, providerInfo));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxCoexistenceOnboardingStatusNotificationResponse) obj;
        return Objects.equals(this.notificationId, that.notificationId)
                && Objects.equals(this.notificationFrom, that.notificationFrom)
                && Objects.equals(this.onboardingStatusStatus, that.onboardingStatusStatus)
                && Objects.equals(this.onboardingStatusProductSurface, that.onboardingStatusProductSurface)
                && Objects.equals(this.onboardingStatusProviderInfo, that.onboardingStatusProviderInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, notificationFrom, onboardingStatusStatus,
                onboardingStatusProductSurface, onboardingStatusProviderInfo);
    }

    @Override
    public String toString() {
        return "SmaxCoexistenceOnboardingStatusNotificationResponse[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", onboardingStatusStatus=" + onboardingStatusStatus
                + ", onboardingStatusProductSurface=" + onboardingStatusProductSurface
                + ", onboardingStatusProviderInfo=" + onboardingStatusProviderInfo + ']';
    }
}
