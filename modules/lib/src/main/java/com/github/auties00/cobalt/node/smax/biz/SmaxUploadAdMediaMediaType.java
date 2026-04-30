package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Documented {@code type} enum carried by both the request and the
 * response. The high-level media-kind classifier.
 *
 * @implNote Mirrors {@code WASmaxInBizCtwaNativeAdEnums.ENUM_IMAGE_VIDEO}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdEnums")
public enum SmaxUploadAdMediaMediaType {
    /**
     * Still-image media. JPEG/PNG/WebP attachments.
     */
    IMAGE,
    /**
     * Video media. MP4 attachments.
     */
    VIDEO;

    /**
     * Returns the wire-form attribute string for this enum value.
     *
     * @return the upper-case enum name
     */
    public String wire() {
        return name();
    }

    /**
     * Tries to parse a {@code type} attribute string back into a
     * {@link SmaxUploadAdMediaMediaType} enum value.
     *
     * @param value the wire-form attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum constant,
     *         or empty when the value does not match the documented
     *         literals
     */
    public static Optional<SmaxUploadAdMediaMediaType> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxUploadAdMediaMediaType.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
