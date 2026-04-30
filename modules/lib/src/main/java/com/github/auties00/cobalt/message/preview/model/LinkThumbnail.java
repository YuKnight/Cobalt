package com.github.auties00.cobalt.message.preview.model;

import java.time.Instant;

/**
 * Thumbnail data attached to a link preview card. Always carries the
 * inline low-resolution JPEG that the recipient renders immediately,
 * and optionally the CDN coordinates of an HQ thumbnail uploaded to
 * WhatsApp's media servers so the recipient can fetch a sharper image
 * on demand.
 *
 * @param jpegThumbnail       the inline low-resolution JPEG bytes
 *                            rendered immediately by the recipient
 * @param thumbnailDirectPath the CDN direct path of the HQ thumbnail,
 *                            or {@code null} when no HQ upload exists
 * @param thumbnailSha256     the plaintext SHA-256 digest of the HQ
 *                            thumbnail bytes
 * @param thumbnailEncSha256  the SHA-256 digest of the encrypted HQ
 *                            thumbnail payload uploaded to the CDN
 * @param mediaKey            the media key used to decrypt the HQ
 *                            thumbnail download
 * @param mediaKeyTimestamp   when the media key was generated
 * @param thumbnailWidth      the HQ-thumbnail width in pixels
 * @param thumbnailHeight     the HQ-thumbnail height in pixels
 */
public record LinkThumbnail(byte[] jpegThumbnail, String thumbnailDirectPath,
                            byte[] thumbnailSha256, byte[] thumbnailEncSha256,
                            byte[] mediaKey, Instant mediaKeyTimestamp,
                            Integer thumbnailWidth, Integer thumbnailHeight) {
}
