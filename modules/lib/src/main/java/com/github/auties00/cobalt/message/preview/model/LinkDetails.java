package com.github.auties00.cobalt.message.preview.model;

import com.github.auties00.cobalt.model.message.text.ExtendedTextMessage;

/**
 * Page-level descriptors of a link preview rendered inside a chat
 * message. Carries the title and description shown on the preview
 * card, the kind of preview the recipient should render (plain link,
 * video, payment), and the do-not-play hint that suppresses inline
 * playback on video tiles.
 *
 * @param title           the preview title shown above the card
 * @param description     the preview description shown below the
 *                        title
 * @param previewType     the kind of preview to render
 * @param doNotPlayInline whether the recipient should suppress
 *                        inline playback for video previews
 */
public record LinkDetails(String title, String description,
                          ExtendedTextMessage.PreviewType previewType,
                          Boolean doNotPlayInline) {
}
