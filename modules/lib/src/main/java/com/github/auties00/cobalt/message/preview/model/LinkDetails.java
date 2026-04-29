package com.github.auties00.cobalt.message.preview.model;

import com.github.auties00.cobalt.model.message.text.ExtendedTextMessage;

/**
 * Page-level descriptors of a link preview: title, description,
 * preview type, and the do-not-play hint applied to video tiles.
 *
 * @param title           the preview title
 * @param description     the preview description
 * @param previewType     the preview-type enum
 * @param doNotPlayInline the do-not-play-inline hint
 */
public record LinkDetails(String title, String description,
                          ExtendedTextMessage.PreviewType previewType,
                          Boolean doNotPlayInline) {
}
