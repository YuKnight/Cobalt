package com.github.auties00.cobalt.node.smax.account;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the
 * {@code <custom_payment_method/>} child inside an
 * {@code <iq xmlns="w:pay" type="set" to="s.whatsapp.net">} envelope
 * with an {@code <account action="create-custom-payment-method"
 * device_id country="BR">} wrapper.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBrPaymentCreateCustomPaymentMethodRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBrPaymentSetIQMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBrPaymentCustomPaymentMethodMetaDataInfoMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBrPaymentCustomPaymentMethodMetaDataMixin")
public final class SmaxBrPaymentCreateCustomPaymentMethodRequest implements SmaxOperation.Request {
    /**
     * The opaque device-id string carried as
     * {@code <account device_id=...>}.
     */
    private final String accountDeviceId;

    /**
     * The custom-payment-method type — one of
     * {@code "PAYONDELIVERY"} or {@code "PIXKEY"}.
     */
    private final String customPaymentMethodType;

    /**
     * The optional {@code update} attribute on
     * {@code <custom_payment_method/>}.
     */
    private final String customPaymentMethodUpdate;

    /**
     * The optional {@code flow} attribute on
     * {@code <custom_payment_method/>} — one of {@code "P2M"} or
     * {@code "P2P"}.
     */
    private final String customPaymentMethodFlow;

    /**
     * The 1..5 metadata key-value pairs to attach as
     * {@code <metadata_info><metadata key= value=/>...</metadata_info>}
     * children, preserving insertion order.
     */
    private final Map<String, String> metadata;

    /**
     * Constructs a new request.
     *
     * @param accountDeviceId           the device-id; never
     *                                  {@code null}
     * @param customPaymentMethodType   the method type; never
     *                                  {@code null}
     * @param customPaymentMethodUpdate the optional update marker;
     *                                  may be {@code null}
     * @param customPaymentMethodFlow   the optional flow marker;
     *                                  may be {@code null}
     * @param metadata                  the 1..5 metadata pairs;
     *                                  never {@code null}, never
     *                                  empty
     * @throws NullPointerException     if any non-nullable argument
     *                                  is {@code null}
     * @throws IllegalArgumentException if the metadata map is empty
     *                                  or has more than 5 entries
     */
    public SmaxBrPaymentCreateCustomPaymentMethodRequest(String accountDeviceId,
                   String customPaymentMethodType,
                   String customPaymentMethodUpdate,
                   String customPaymentMethodFlow,
                   Map<String, String> metadata) {
        this.accountDeviceId = Objects.requireNonNull(accountDeviceId, "accountDeviceId cannot be null");
        this.customPaymentMethodType = Objects.requireNonNull(customPaymentMethodType, "customPaymentMethodType cannot be null");
        this.customPaymentMethodUpdate = customPaymentMethodUpdate;
        this.customPaymentMethodFlow = customPaymentMethodFlow;
        Objects.requireNonNull(metadata, "metadata cannot be null");
        if (metadata.isEmpty() || metadata.size() > 5) {
            throw new IllegalArgumentException("metadata must contain 1..5 entries");
        }
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    /**
     * Returns the account device-id.
     *
     * @return the id; never {@code null}
     */
    public String accountDeviceId() {
        return accountDeviceId;
    }

    /**
     * Returns the custom-payment-method type.
     *
     * @return the type; never {@code null}
     */
    public String customPaymentMethodType() {
        return customPaymentMethodType;
    }

    /**
     * Returns the optional {@code update} marker.
     *
     * @return an {@link Optional} carrying the marker, or empty
     *         when omitted
     */
    public Optional<String> customPaymentMethodUpdate() {
        return Optional.ofNullable(customPaymentMethodUpdate);
    }

    /**
     * Returns the optional {@code flow} marker.
     *
     * @return an {@link Optional} carrying the marker, or empty
     *         when omitted
     */
    public Optional<String> customPaymentMethodFlow() {
        return Optional.ofNullable(customPaymentMethodFlow);
    }

    /**
     * Returns the metadata pairs.
     *
     * @return an unmodifiable map; never {@code null}
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <account>} → {@code <custom_payment_method>}
     *         payload
     *
     * @implNote {@code WASmaxOutBrPaymentCreateCustomPaymentMethodRequest.makeCreateCustomPaymentMethodRequest}
     *           composes
     *           {@code WASmaxOutBrPaymentSetIQMixin}
     *           ({@code xmlns="w:pay"}, {@code type="set"},
     *           {@code to=S_WHATSAPP_NET}, {@code id=generateId()})
     *           over an {@code <account
     *           action="create-custom-payment-method"
     *           device_id=CUSTOM_STRING(t) country="BR">} wrapper
     *           around a
     *           {@code <custom_payment_method type
     *           update?  flow?>} child plus a
     *           {@code WASmaxOutBrPaymentCustomPaymentMethodMetaDataInfoMixin}
     *           overlay.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBrPaymentCreateCustomPaymentMethodRequest",
            exports = "makeCreateCustomPaymentMethodRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // metadata children
        var metadataChildren = new Node[metadata.size()];
        var i = 0;
        for (var entry : metadata.entrySet()) {
            metadataChildren[i++] = new NodeBuilder()
                    .description("metadata")
                    .attribute("key", entry.getKey())
                    .attribute("value", entry.getValue())
                    .build();
        }
        var metadataInfo = new NodeBuilder()
                .description("metadata_info")
                .content(metadataChildren)
                .build();
        // custom_payment_method child
        var cpmBuilder = new NodeBuilder()
                .description("custom_payment_method")
                .attribute("type", customPaymentMethodType);
        if (customPaymentMethodUpdate != null) {
            cpmBuilder.attribute("update", customPaymentMethodUpdate);
        }
        if (customPaymentMethodFlow != null) {
            cpmBuilder.attribute("flow", customPaymentMethodFlow);
        }
        cpmBuilder.content(metadataInfo);
        var customPaymentMethod = cpmBuilder.build();
        // <account ...>
        var account = new NodeBuilder()
                .description("account")
                .attribute("action", "create-custom-payment-method")
                .attribute("device_id", accountDeviceId)
                .attribute("country", "BR")
                .content(customPaymentMethod)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:pay")
                .attribute("type", "set")
                .attribute("to", JidServer.user())
                .content(account);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBrPaymentCreateCustomPaymentMethodRequest) obj;
        return Objects.equals(this.accountDeviceId, that.accountDeviceId)
                && Objects.equals(this.customPaymentMethodType, that.customPaymentMethodType)
                && Objects.equals(this.customPaymentMethodUpdate, that.customPaymentMethodUpdate)
                && Objects.equals(this.customPaymentMethodFlow, that.customPaymentMethodFlow)
                && Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountDeviceId, customPaymentMethodType, customPaymentMethodUpdate,
                customPaymentMethodFlow, metadata);
    }

    @Override
    public String toString() {
        return "SmaxBrPaymentCreateCustomPaymentMethodRequest[accountDeviceId=" + accountDeviceId
                + ", customPaymentMethodType=" + customPaymentMethodType
                + ", customPaymentMethodUpdate=" + customPaymentMethodUpdate
                + ", customPaymentMethodFlow=" + customPaymentMethodFlow
                + ", metadata=" + metadata + ']';
    }
}
