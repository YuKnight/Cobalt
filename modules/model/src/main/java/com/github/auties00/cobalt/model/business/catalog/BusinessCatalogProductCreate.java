package com.github.auties00.cobalt.model.business.catalog;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidProvider;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Input model for adding one product to a WhatsApp Business catalog.
 *
 * <p>When a merchant adds a product to their catalog, three pieces of
 * information accompany the request: the business account that owns the
 * catalog, the thumbnail dimensions the server should render the product
 * imagery at, and the product fields themselves (name, media, price,
 * compliance details). This model bundles them into one argument so the
 * catalog add call takes a single parameter rather than a long scalar list.
 *
 * <p>The product fields are supplied as a pre-encoded JSON object literal
 * because the field set varies per product and is not yet modelled as
 * fixed typed fields. The thumbnail width and height are independently
 * optional; leaving either unset lets the server pick a default size.
 */
@ProtobufMessage(name = "BusinessCatalogProductCreate")
public final class BusinessCatalogProductCreate {
    /**
     * Business account that owns the catalog the product is added to.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final Jid businessJid;

    /**
     * Requested thumbnail width, in pixels. Unset lets the server pick a
     * default size.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.INT32)
    final Integer thumbnailWidth;

    /**
     * Requested thumbnail height, in pixels. Unset lets the server pick a
     * default size.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.INT32)
    final Integer thumbnailHeight;

    /**
     * Pre-encoded JSON object literal carrying the new product's fields
     * (name, media, price, compliance details, and so on). Unset omits
     * the product body from the request.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String productInfoJson;

    /**
     * Constructs a new {@code BusinessCatalogProductCreate}.
     *
     * @param businessJid     the business account that owns the catalog; required
     * @param thumbnailWidth  the requested thumbnail width, or {@code null}
     * @param thumbnailHeight the requested thumbnail height, or {@code null}
     * @param productInfoJson the pre-encoded product fields, or {@code null}
     * @throws NullPointerException if {@code businessJid} is {@code null}
     */
    public BusinessCatalogProductCreate(Jid businessJid, Integer thumbnailWidth, Integer thumbnailHeight,
                                        String productInfoJson) {
        this.businessJid = Objects.requireNonNull(businessJid, "businessJid cannot be null");
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
        this.productInfoJson = productInfoJson;
    }

    /**
     * Convenience constructor that accepts any {@link JidProvider} and
     * resolves it to a {@link Jid}.
     *
     * @param businessJid     the business account that owns the catalog; required
     * @param thumbnailWidth  the requested thumbnail width, or {@code null}
     * @param thumbnailHeight the requested thumbnail height, or {@code null}
     * @param productInfoJson the pre-encoded product fields, or {@code null}
     * @throws NullPointerException if {@code businessJid} is {@code null}
     */
    public BusinessCatalogProductCreate(JidProvider businessJid, Integer thumbnailWidth,
                                        Integer thumbnailHeight, String productInfoJson) {
        this(Objects.requireNonNull(businessJid, "businessJid cannot be null").toJid(),
                thumbnailWidth, thumbnailHeight, productInfoJson);
    }

    /**
     * Returns the business account that owns the catalog.
     *
     * @return the business JID, never {@code null}
     */
    public Jid businessJid() {
        return businessJid;
    }

    /**
     * Returns the requested thumbnail width in pixels.
     *
     * @return an {@code OptionalInt} carrying the width, or empty when unset
     */
    public OptionalInt thumbnailWidth() {
        return thumbnailWidth == null ? OptionalInt.empty() : OptionalInt.of(thumbnailWidth);
    }

    /**
     * Returns the requested thumbnail height in pixels.
     *
     * @return an {@code OptionalInt} carrying the height, or empty when unset
     */
    public OptionalInt thumbnailHeight() {
        return thumbnailHeight == null ? OptionalInt.empty() : OptionalInt.of(thumbnailHeight);
    }

    /**
     * Returns the pre-encoded JSON product fields.
     *
     * @return an {@link Optional} carrying the JSON body, or empty when unset
     */
    public Optional<String> productInfoJson() {
        return Optional.ofNullable(productInfoJson);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BusinessCatalogProductCreate) obj;
        return Objects.equals(businessJid, that.businessJid)
                && Objects.equals(thumbnailWidth, that.thumbnailWidth)
                && Objects.equals(thumbnailHeight, that.thumbnailHeight)
                && Objects.equals(productInfoJson, that.productInfoJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessJid, thumbnailWidth, thumbnailHeight, productInfoJson);
    }

    @Override
    public String toString() {
        return "BusinessCatalogProductCreate[" +
                "businessJid=" + businessJid + ", " +
                "thumbnailWidth=" + thumbnailWidth + ", " +
                "thumbnailHeight=" + thumbnailHeight + ", " +
                "productInfoJson=" + productInfoJson + ']';
    }
}
