package com.github.auties00.cobalt.message.preview.model;

/**
 * Combined preview details and inline thumbnail returned by the
 * per-source resolvers in {@code com.github.auties00.cobalt.preview.source}.
 *
 * @param details   the preview text details
 * @param thumbnail the inline thumbnail, or {@code null} when no
 *                  thumbnail is available
 */
public record ResolvedPreview(LinkDetails details, LinkThumbnail thumbnail) {
}
