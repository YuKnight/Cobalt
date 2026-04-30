package com.github.auties00.cobalt.message.preview.model;

/**
 * Combined preview text and inline thumbnail returned by the
 * per-source resolvers in
 * {@code com.github.auties00.cobalt.message.preview.source} (group
 * invites, business catalogs and products, newsletter MEX). The
 * link-preview pipeline merges both halves onto the outgoing
 * extended-text message.
 *
 * @param details   the preview text details
 * @param thumbnail the inline thumbnail, or {@code null} when no
 *                  thumbnail is available
 */
public record ResolvedPreview(LinkDetails details, LinkThumbnail thumbnail) {
}
