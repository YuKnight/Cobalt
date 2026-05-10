package com.github.auties00.cobalt.message.preview;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadata.PaymentLinkHeader;
import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadata.PaymentLinkHeader.PaymentLinkHeaderType;
import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadata.PaymentLinkProvider;
import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadataBuilder;
import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadataPaymentLinkHeaderBuilder;
import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadataPaymentLinkProviderBuilder;
import com.github.auties00.cobalt.model.message.text.ExtendedTextMessage;
import com.github.auties00.cobalt.message.preview.model.LinkDetails;
import com.github.auties00.cobalt.message.preview.model.LinkThumbnail;
import com.github.auties00.cobalt.message.preview.model.PaymentLinkDetails;

/**
 * Assembles {@link ExtendedTextMessage} preview fields from the
 * resolved title/description/thumbnail and any HQ-thumbnail upload
 * parameters.
 *
 * <p>Mutates the supplied message in place because the calling
 * pipeline already holds it; reconstructing a fresh
 * {@link ExtendedTextMessage} would force the caller to discard the
 * existing text, font, mentions, and other unrelated fields.
 */
@WhatsAppWebModule(moduleName = "WAWebLinkPreviewUtils")
final class LinkPreviewUtils {
    /**
     * Hidden constructor for the utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private LinkPreviewUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Attaches a fully-resolved preview to {@code message}.
     *
     * @param message        the outgoing message to enrich
     * @param details        the preview details (title, description,
     *                       preview type, do-not-play flag)
     * @param thumbnail      the low-resolution JPEG thumbnail and the
     *                       optional HQ-thumbnail upload parameters
     * @param paymentDetails the payment-link descriptors; when non-null
     *                       and carrying a PSP, populates
     *                       {@link ExtendedTextMessage#paymentLinkMetadata()}
     *                       with a {@link PaymentLinkProvider} carrying
     *                       the PSP and a {@link PaymentLinkHeader} of
     *                       type
     *                       {@link PaymentLinkHeaderType#LINK_PREVIEW}
     */
    @WhatsAppWebExport(moduleName = "WAWebLinkPreviewUtils", exports = "genLinkPreview",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static void attach(ExtendedTextMessage message,
                       LinkDetails details,
                       LinkThumbnail thumbnail,
                       PaymentLinkDetails paymentDetails) {
        if (message == null) {
            return;
        }
        if (details != null) {
            if (details.title() != null) {
                message.setTitle(details.title());
            }
            if (details.description() != null) {
                message.setDescription(details.description());
            }
            if (details.previewType() != null) {
                message.setPreviewType(details.previewType());
            }
            if (details.doNotPlayInline() != null) {
                message.setDoNotPlayInline(details.doNotPlayInline());
            }
        }
        if (thumbnail != null) {
            if (thumbnail.jpegThumbnail() != null) {
                message.setJpegThumbnail(thumbnail.jpegThumbnail());
            }
            if (thumbnail.thumbnailDirectPath() != null) {
                message.setThumbnailDirectPath(thumbnail.thumbnailDirectPath());
            }
            if (thumbnail.thumbnailSha256() != null) {
                message.setThumbnailSha256(thumbnail.thumbnailSha256());
            }
            if (thumbnail.thumbnailEncSha256() != null) {
                message.setThumbnailEncSha256(thumbnail.thumbnailEncSha256());
            }
            if (thumbnail.mediaKey() != null) {
                message.setMediaKey(thumbnail.mediaKey());
            }
            if (thumbnail.mediaKeyTimestamp() != null) {
                message.setMediaKeyTimestamp(thumbnail.mediaKeyTimestamp());
            }
            if (thumbnail.thumbnailWidth() != null) {
                message.setThumbnailWidth(thumbnail.thumbnailWidth());
            }
            if (thumbnail.thumbnailHeight() != null) {
                message.setThumbnailHeight(thumbnail.thumbnailHeight());
            }
        }
        if (paymentDetails != null && paymentDetails.psp() != null) {
            // The URL driven branch sets headerType to LINK_PREVIEW and provider.paramsJson
            // to the resolved PSP label so receivers can reach the matching payment service.
            // Button text is server localised and only attached by the business composer,
            // never by URL detection.
            var provider = new PaymentLinkMetadataPaymentLinkProviderBuilder()
                    .paramsJson(paymentDetails.psp())
                    .build();
            var header = new PaymentLinkMetadataPaymentLinkHeaderBuilder()
                    .headerType(PaymentLinkHeaderType.LINK_PREVIEW)
                    .build();
            var metadata = new PaymentLinkMetadataBuilder()
                    .header(header)
                    .provider(provider)
                    .build();
            message.setPaymentLinkMetadata(metadata);
        }
    }

    /**
     * Builds the {@link LinkDetails} record that
     * {@code genMinimalLinkPreview} produces when the rich pipeline
     * does not return a result.
     *
     * @param matchedText the URL substring from the body
     * @param domain      the URL's domain
     * @param previewType the resolved preview type (typically
     *                    {@link ExtendedTextMessage.PreviewType#NONE})
     * @return the minimal preview details
     */
    @WhatsAppWebExport(moduleName = "WAWebGenMinimalLinkPreviewChatAction", exports = "genMinimalLinkPreview",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static LinkDetails minimalDetails(String matchedText, String domain,
                                      ExtendedTextMessage.PreviewType previewType) {
        return new LinkDetails(domain, matchedText, previewType, true);
    }
}
