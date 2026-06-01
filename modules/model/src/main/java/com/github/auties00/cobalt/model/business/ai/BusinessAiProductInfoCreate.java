package com.github.auties00.cobalt.model.business.ai;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Input model for registering a product the WhatsApp Business AI agent can
 * describe to customers.
 *
 * <p>A new AI product info entry carries the product's display name, an
 * optional description, and a structured price expressed as a sub-object
 * JSON literal so the AI agent can reproduce tiered or range pricing rather
 * than just a single scalar. Product imagery comes from two parallel
 * sources: local file paths uploaded fresh through the WhatsApp Business
 * media pipeline, and references to images already uploaded that the
 * merchant is reusing for the new product so the bot does not re-upload
 * them. The thumbnail width and height bound the rendered product
 * thumbnails; the WhatsApp Business app sends both as decimal pixel strings
 * so they are kept as {@link String}s here too.
 */
@ProtobufMessage(name = "BusinessAiProductInfoCreate")
public final class BusinessAiProductInfoCreate {
    /**
     * Product display name.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String name;

    /**
     * Pre-encoded JSON object literal carrying the structured product
     * price (tiered, range, or scalar). Unset emits a JSON {@code null}.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String priceJson;

    /**
     * Free-form product description. Unset emits a JSON {@code null}.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String description;

    /**
     * Local file paths fed to the WhatsApp Business media pipeline for
     * fresh upload. Defaults to {@link List#of()} when unset; only
     * non-empty lists are written to the wire.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final List<String> localImageFilePaths;

    /**
     * Pre-encoded JSON array of references to images already uploaded for
     * this merchant that the new product reuses. Unset omits the
     * references from the request.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    final String existingImageReferencesJson;

    /**
     * Requested thumbnail width as a decimal pixel string. Unset omits
     * the width from the request.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    final String thumbnailWidthPx;

    /**
     * Requested thumbnail height as a decimal pixel string. Unset omits
     * the height from the request.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.STRING)
    final String thumbnailHeightPx;

    /**
     * Constructs a new {@code BusinessAiProductInfoCreate}.
     *
     * @param name                        the product display name; required
     * @param priceJson                   the pre-encoded structured price, or {@code null}
     * @param description                 the product description, or {@code null}
     * @param localImageFilePaths         the local image paths to upload; never
     *                                    {@code null}, defaults to {@link List#of()}
     * @param existingImageReferencesJson the pre-encoded JSON array of
     *                                    existing image references, or {@code null}
     * @param thumbnailWidthPx            the thumbnail width pixel string, or {@code null}
     * @param thumbnailHeightPx           the thumbnail height pixel string, or {@code null}
     * @throws NullPointerException if {@code name} or
     *                              {@code localImageFilePaths} is {@code null}
     */
    public BusinessAiProductInfoCreate(String name, String priceJson, String description,
                                       List<String> localImageFilePaths,
                                       String existingImageReferencesJson,
                                       String thumbnailWidthPx, String thumbnailHeightPx) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.priceJson = priceJson;
        this.description = description;
        this.localImageFilePaths = Objects.requireNonNull(localImageFilePaths,
                "localImageFilePaths cannot be null");
        this.existingImageReferencesJson = existingImageReferencesJson;
        this.thumbnailWidthPx = thumbnailWidthPx;
        this.thumbnailHeightPx = thumbnailHeightPx;
    }

    /**
     * Returns the product display name.
     *
     * @return the product name, never {@code null}
     */
    public String name() {
        return name;
    }

    /**
     * Returns the pre-encoded structured price JSON.
     *
     * @return an {@link Optional} carrying the price JSON, or empty when unset
     */
    public Optional<String> priceJson() {
        return Optional.ofNullable(priceJson);
    }

    /**
     * Returns the product description.
     *
     * @return an {@link Optional} carrying the description, or empty when unset
     */
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns the local image file paths to upload.
     *
     * @return the image paths, never {@code null}
     */
    public List<String> localImageFilePaths() {
        return localImageFilePaths;
    }

    /**
     * Returns the pre-encoded existing image references JSON.
     *
     * @return an {@link Optional} carrying the references JSON, or empty when unset
     */
    public Optional<String> existingImageReferencesJson() {
        return Optional.ofNullable(existingImageReferencesJson);
    }

    /**
     * Returns the requested thumbnail width pixel string.
     *
     * @return an {@link Optional} carrying the width string, or empty when unset
     */
    public Optional<String> thumbnailWidthPx() {
        return Optional.ofNullable(thumbnailWidthPx);
    }

    /**
     * Returns the requested thumbnail height pixel string.
     *
     * @return an {@link Optional} carrying the height string, or empty when unset
     */
    public Optional<String> thumbnailHeightPx() {
        return Optional.ofNullable(thumbnailHeightPx);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BusinessAiProductInfoCreate) obj;
        return Objects.equals(name, that.name)
                && Objects.equals(priceJson, that.priceJson)
                && Objects.equals(description, that.description)
                && Objects.equals(localImageFilePaths, that.localImageFilePaths)
                && Objects.equals(existingImageReferencesJson, that.existingImageReferencesJson)
                && Objects.equals(thumbnailWidthPx, that.thumbnailWidthPx)
                && Objects.equals(thumbnailHeightPx, that.thumbnailHeightPx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, priceJson, description, localImageFilePaths,
                existingImageReferencesJson, thumbnailWidthPx, thumbnailHeightPx);
    }

    @Override
    public String toString() {
        return "BusinessAiProductInfoCreate[" +
                "name=" + name + ", " +
                "priceJson=" + priceJson + ", " +
                "description=" + description + ", " +
                "localImageFilePaths=" + localImageFilePaths + ", " +
                "existingImageReferencesJson=" + existingImageReferencesJson + ", " +
                "thumbnailWidthPx=" + thumbnailWidthPx + ", " +
                "thumbnailHeightPx=" + thumbnailHeightPx + ']';
    }
}
