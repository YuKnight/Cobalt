package com.github.auties00.cobalt.message.preview.model;

import java.time.Instant;

/**
 * Thumbnail descriptors for a link preview: a low-resolution JPEG plus
 * the optional HQ-thumbnail upload outputs (direct path, hashes, media
 * key, dimensions).
 *
 * @param jpegThumbnail       the low-resolution JPEG bytes
 * @param thumbnailDirectPath the CDN direct path
 * @param thumbnailSha256     the plaintext SHA-256 of the HQ
 *                            thumbnail
 * @param thumbnailEncSha256  the encrypted SHA-256 of the HQ
 *                            thumbnail
 * @param mediaKey            the media key used to decrypt the HQ
 *                            thumbnail
 * @param mediaKeyTimestamp   when the media key was generated
 * @param thumbnailWidth      the HQ-thumbnail width
 * @param thumbnailHeight     the HQ-thumbnail height
 */
public record LinkThumbnail(byte[] jpegThumbnail, String thumbnailDirectPath,
                            byte[] thumbnailSha256, byte[] thumbnailEncSha256,
                            byte[] mediaKey, Instant mediaKeyTimestamp,
                            Integer thumbnailWidth, Integer thumbnailHeight) {
}
