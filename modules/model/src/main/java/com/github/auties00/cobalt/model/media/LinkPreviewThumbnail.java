package com.github.auties00.cobalt.model.media;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Mutable {@link MediaProvider} carrier for the high-quality link
 * preview thumbnail that is uploaded to the media CDN before being
 * referenced from an outgoing
 * {@code com.github.auties00.cobalt.model.message.text.ExtendedTextMessage}.
 *
 * <p>This type is not transmitted on the wire and has no protobuf
 * spec; it exists purely as a vessel for the upload pipeline to stamp
 * the CDN coordinates ({@code mediaDirectPath / mediaSha256 /
 * mediaEncSha256 / mediaKey / mediaKeyTimestamp / mediaSize}) onto
 * after encrypting the bytes. The link-preview orchestrator copies
 * the resulting fields onto the outgoing message's
 * {@code thumbnailDirectPath / thumbnailSha256 / thumbnailEncSha256 /
 * mediaKey / mediaKeyTimestamp} slots.
 *
 * <p>The mutability is intentional: the upload pipeline calls every
 * {@code set...} method to populate the carrier, then the caller
 * reads them back via the {@link MediaProvider} accessors. No JSON
 * serialisation, no protobuf serialisation, no equality.
 */
public final class LinkPreviewThumbnail implements MediaProvider {
    /**
     * The CDN URL at which the encrypted thumbnail can be downloaded
     * after upload.
     */
    private String mediaUrl;

    /**
     * The CDN direct path at which the encrypted thumbnail can be
     * fetched after upload.
     */
    private String mediaDirectPath;

    /**
     * The symmetric key used to encrypt the thumbnail before upload.
     */
    private byte[] mediaKey;

    /**
     * The timestamp at which {@link #mediaKey} was generated.
     */
    private Instant mediaKeyTimestamp;

    /**
     * The SHA-256 of the encrypted ciphertext uploaded to the CDN.
     */
    private byte[] mediaEncryptedSha256;

    /**
     * The SHA-256 of the original plaintext bytes.
     */
    private byte[] mediaSha256;

    /**
     * The size in bytes of the original plaintext payload.
     */
    private long mediaSize;

    /**
     * Creates an empty carrier ready to be populated by the upload
     * pipeline.
     */
    public LinkPreviewThumbnail() {

    }

    /**
     * Returns the CDN URL at which the encrypted thumbnail can be
     * downloaded.
     *
     * @return an {@link Optional} containing the URL, or empty if the
     *         upload has not completed yet
     */
    @Override
    public Optional<String> mediaUrl() {
        return Optional.ofNullable(mediaUrl);
    }

    /**
     * Sets the CDN URL of the encrypted thumbnail.
     *
     * @param mediaUrl the URL, or {@code null} to clear
     */
    @Override
    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    /**
     * Returns the CDN direct path of the encrypted thumbnail.
     *
     * @return an {@link Optional} containing the direct path, or empty
     */
    @Override
    public Optional<String> mediaDirectPath() {
        return Optional.ofNullable(mediaDirectPath);
    }

    /**
     * Sets the CDN direct path of the encrypted thumbnail.
     *
     * @param mediaDirectPath the direct path, or {@code null} to clear
     */
    @Override
    public void setMediaDirectPath(String mediaDirectPath) {
        this.mediaDirectPath = mediaDirectPath;
    }

    /**
     * Returns the symmetric key used to encrypt the thumbnail.
     *
     * @return an {@link Optional} containing the media key, or empty
     */
    @Override
    public Optional<byte[]> mediaKey() {
        return Optional.ofNullable(mediaKey);
    }

    /**
     * Sets the symmetric key used to encrypt the thumbnail.
     *
     * @param bytes the media key, or {@code null} to clear
     */
    @Override
    public void setMediaKey(byte[] bytes) {
        this.mediaKey = bytes;
    }

    /**
     * Sets the moment the {@link #mediaKey} was generated.
     *
     * @param timestamp the media-key timestamp, or {@code null} to
     *                  clear
     */
    @Override
    public void setMediaKeyTimestamp(Instant timestamp) {
        this.mediaKeyTimestamp = timestamp;
    }

    /**
     * Returns the moment the {@link #mediaKey} was generated.
     *
     * @return an {@link Optional} containing the media-key timestamp,
     *         or empty
     */
    public Optional<Instant> mediaKeyTimestamp() {
        return Optional.ofNullable(mediaKeyTimestamp);
    }

    /**
     * Returns the SHA-256 of the encrypted ciphertext.
     *
     * @return an {@link Optional} containing the SHA-256, or empty
     */
    @Override
    public Optional<byte[]> mediaEncryptedSha256() {
        return Optional.ofNullable(mediaEncryptedSha256);
    }

    /**
     * Sets the SHA-256 of the encrypted ciphertext.
     *
     * @param mediaEncryptedSha256 the SHA-256 bytes, or {@code null}
     *                             to clear
     */
    @Override
    public void setMediaEncryptedSha256(byte[] mediaEncryptedSha256) {
        this.mediaEncryptedSha256 = mediaEncryptedSha256;
    }

    /**
     * Returns the SHA-256 of the original plaintext bytes.
     *
     * @return an {@link Optional} containing the SHA-256, or empty
     */
    @Override
    public Optional<byte[]> mediaSha256() {
        return Optional.ofNullable(mediaSha256);
    }

    /**
     * Sets the SHA-256 of the original plaintext bytes.
     *
     * @param mediaSha256 the SHA-256 bytes, or {@code null} to clear
     */
    @Override
    public void setMediaSha256(byte[] mediaSha256) {
        this.mediaSha256 = mediaSha256;
    }

    /**
     * Returns the size in bytes of the original plaintext payload.
     *
     * @return an {@link OptionalLong} containing the size, or empty
     *         when the upload has not completed yet
     */
    @Override
    public OptionalLong mediaSize() {
        return mediaSize > 0 ? OptionalLong.of(mediaSize) : OptionalLong.empty();
    }

    /**
     * Sets the size in bytes of the original plaintext payload.
     *
     * @param mediaSize the size in bytes
     */
    @Override
    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
    }

    /**
     * Returns the media-path category used to route the upload to the
     * link-thumbnail CDN bucket.
     *
     * @return {@link MediaPath#THUMBNAIL_LINK}
     */
    @Override
    public MediaPath mediaPath() {
        return MediaPath.THUMBNAIL_LINK;
    }

    /**
     * Returns whether two {@code LinkPreviewThumbnail} carriers refer
     * to the same uploaded blob.
     *
     * @param o the object to compare against
     * @return {@code true} when {@code o} is another
     *         {@code LinkPreviewThumbnail} with matching fields
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof LinkPreviewThumbnail that
                && mediaSize == that.mediaSize
                && Objects.equals(mediaUrl, that.mediaUrl)
                && Objects.equals(mediaDirectPath, that.mediaDirectPath)
                && Arrays.equals(mediaKey, that.mediaKey)
                && Objects.equals(mediaKeyTimestamp, that.mediaKeyTimestamp)
                && Arrays.equals(mediaEncryptedSha256, that.mediaEncryptedSha256)
                && Arrays.equals(mediaSha256, that.mediaSha256);
    }

    /**
     * Returns the hash code of this carrier.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(mediaUrl, mediaDirectPath, Arrays.hashCode(mediaKey),
                mediaKeyTimestamp, Arrays.hashCode(mediaEncryptedSha256),
                Arrays.hashCode(mediaSha256), mediaSize);
    }
}
