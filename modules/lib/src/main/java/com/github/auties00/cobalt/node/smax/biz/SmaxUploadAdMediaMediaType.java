package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Locale;
import java.util.Optional;

/**
 * Literal-tuple validator for the documented {@code type} attribute on
 * the CTWA (click-to-WhatsApp) native-ad upload-media stanzas. Carries
 * the high-level media-kind classifier on both the outbound
 * {@code <media id type/>} / {@code <media_list id type/>} children
 * (built by {@code WASmaxOutBizCtwaNativeAdUploadAdMediaRequest}) and
 * the inbound echoes (parsed by
 * {@code WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess}).
 *
 * <p>The wire literals are lowercase {@code "image"} and {@code "video"};
 * Java's enum constants use the uppercase {@link #IMAGE} / {@link #VIDEO}
 * names, mirrored to/from the wire form by {@link #wire()} and
 * {@link #of(String)}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBizCtwaNativeAdEnums",
        exports = "ENUM_IMAGE_VIDEO",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxUploadAdMediaMediaType {
    /**
     * Still-image media. Wire literal {@code "image"}; serialised onto
     * the {@code type} attribute by JPEG/PNG/WebP attachments.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaNativeAdEnums",
            exports = "ENUM_IMAGE_VIDEO",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    IMAGE,
    /**
     * Video media. Wire literal {@code "video"}; serialised onto the
     * {@code type} attribute by MP4 attachments.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaNativeAdEnums",
            exports = "ENUM_IMAGE_VIDEO",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    VIDEO;

    /**
     * Returns the wire-form attribute string for this enum value. The
     * wire form is the lowercase literal expected by both the outbound
     * builder and the inbound parser.
     *
     * @return the lowercase enum name; never {@code null}
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaNativeAdEnums",
            exports = "ENUM_IMAGE_VIDEO",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public String wire() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Tries to parse a {@code type} attribute string back into a
     * {@link SmaxUploadAdMediaMediaType} enum value. Mirrors the WA Web
     * {@code attrStringEnum} lookup, which is a case-sensitive
     * dictionary match against the lowercase literals.
     *
     * @param value the wire-form attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum constant,
     *         or empty when the value is {@code null} or does not match
     *         any documented literal
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaNativeAdEnums",
            exports = "ENUM_IMAGE_VIDEO",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxUploadAdMediaMediaType> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        // WASmaxInBizCtwaNativeAdEnums.ENUM_IMAGE_VIDEO: {image:"image",video:"video"}
        // WASmaxParseUtils.attrStringEnum: case-sensitive dict lookup n[r.value]
        return switch (value) {
            case "image" -> Optional.of(IMAGE);
            case "video" -> Optional.of(VIDEO);
            default -> Optional.empty();
        };
    }
}
