package com.github.auties00.cobalt.model.media;

import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Represents an external blob reference used to point to application state
 * synchronization data that has been uploaded to the media CDN rather than
 * sent inline within a {@code SyncdPatch}.
 *
 * <p>When the number of mutations or the serialized patch protobuf size
 * exceeds the configured inline thresholds, the mutations are serialized
 * as a {@code SyncdMutations} protobuf, encrypted and uploaded via the
 * media connection, and the resulting CDN metadata is captured in an
 * instance of this class.  The {@code SyncdPatch} then carries this
 * reference in its {@code externalMutations} field instead of inline
 * {@code mutations}.
 *
 * <p>The protobuf field names in WhatsApp Web are {@code mediaKey},
 * {@code directPath}, {@code handle}, {@code fileSizeBytes},
 * {@code fileSha256}, and {@code fileEncSha256}.  Cobalt uses generic
 * {@link MediaProvider}-compatible names because this class implements
 * that interface for unified upload/download handling.
 *
 * @implNote WAWebProtobufsServerSync.pb ExternalBlobReference,
 *           WAWebSyncdMMSUpload.buildExternalBlobReference
 */
@ProtobufMessage(name = "ExternalBlobReference")
public final class ExternalBlobReference implements MediaProvider {
    /**
     * The symmetric encryption key used to encrypt the blob before upload.
     *
     * <p>Populated by the media upload pipeline after encryption.  In WhatsApp
     * Web, this is the base64-decoded {@code mediaKey} field returned by
     * {@code uploadSyncExternalPatch}.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — mediaKey: i,
     *           WAWebSyncdNetCallbacksApi.uploadSyncExternalPatch — decodeB64(mediaKey)
     */
    @ProtobufProperty(index = 1, type = ProtobufType.BYTES)
    byte[] mediaKey;

    /**
     * The CDN direct path from which the encrypted blob can be fetched.
     *
     * <p>Populated from the upload response JSON {@code direct_path} field.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — directPath: n,
     *           WAWebSyncdNetCallbacksApi.uploadSyncExternalPatch — directPath
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    String mediaDirectPath;

    /**
     * The opaque server handle identifying the uploaded blob.
     *
     * <p>Populated from the upload response JSON {@code handle} field.
     * WhatsApp Web validates that this is non-null after upload and throws
     * if it is missing.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — handle: a,
     *           WAWebSyncdNetCallbacksApi.uploadSyncExternalPatch — if (handle == null) throw err(...)
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    String handle;

    /**
     * The size of the original (plaintext) blob in bytes.
     *
     * <p>In WhatsApp Web, this is set to {@code t.byteLength} where {@code t}
     * is the original blob passed to {@code buildExternalBlobReference}.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileSizeBytes: t.byteLength
     */
    @ProtobufProperty(index = 4, type = ProtobufType.UINT64)
    Long mediaSize;

    /**
     * The SHA-256 digest of the original (plaintext) blob.
     *
     * <p>In WhatsApp Web, this is computed as
     * {@code decodeB64(calculateFilehash(t))} where {@code calculateFilehash}
     * is {@code sha256Base64}.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileSha256: decodeB64(calculateFilehash(t)),
     *           WAMediaCalculateFilehash.calculateFilehash — sha256Base64(e)
     */
    @ProtobufProperty(index = 5, type = ProtobufType.BYTES)
    byte[] mediaSha256;

    /**
     * The SHA-256 digest of the encrypted blob.
     *
     * <p>In WhatsApp Web, this is the base64-decoded {@code encFilehash}
     * field returned by {@code uploadSyncExternalPatch}.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileEncSha256: r,
     *           WAWebSyncdNetCallbacksApi.uploadSyncExternalPatch — decodeB64(encFilehash)
     */
    @ProtobufProperty(index = 6, type = ProtobufType.BYTES)
    byte[] mediaEncryptedSha256;

    /**
     * Constructs a new external blob reference with the specified CDN
     * metadata fields.
     *
     * <p>This constructor is package-private and is invoked by the generated
     * {@code ExternalBlobReferenceBuilder}.  Fields are populated by the
     * media upload pipeline in {@code MediaConnection.upload}, which maps
     * to the combined behavior of
     * {@code WAWebSyncdNetCallbacksApi.uploadSyncExternalPatch} (upload and
     * decode response) and {@code WAWebSyncdMMSUpload.buildExternalBlobReference}
     * (construct the protobuf object from the upload result).
     *
     * @implNote ADAPTED: WAWebSyncdMMSUpload.buildExternalBlobReference —
     *           fields populated by MediaConnection.upload instead of explicit
     *           property assignment from upload result
     * @param mediaKey              the symmetric encryption key
     * @param mediaDirectPath       the CDN direct path
     * @param handle                the opaque server handle
     * @param mediaSize             the plaintext blob size in bytes
     * @param mediaSha256           the SHA-256 of the plaintext blob
     * @param mediaEncryptedSha256  the SHA-256 of the encrypted blob
     */
    ExternalBlobReference(byte[] mediaKey, String mediaDirectPath, String handle, Long mediaSize, byte[] mediaSha256, byte[] mediaEncryptedSha256) {
        this.mediaKey = mediaKey;
        this.mediaDirectPath = mediaDirectPath;
        this.handle = handle;
        this.mediaSize = mediaSize;
        this.mediaSha256 = mediaSha256;
        this.mediaEncryptedSha256 = mediaEncryptedSha256;
    }

    /**
     * Returns the symmetric encryption key used to encrypt the blob.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — mediaKey: i
     * @return an {@link Optional} containing the media key bytes, or empty
     *         if not set
     */
    @Override
    public Optional<byte[]> mediaKey() {
        return Optional.ofNullable(mediaKey);
    }

    /**
     * Returns the CDN direct path for the uploaded blob.
     *
     * <p>This is a convenience alias for {@link #mediaDirectPath()} using the
     * WhatsApp Web protobuf field name.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — directPath: n
     * @return an {@link Optional} containing the direct path, or empty if
     *         not set
     */
    public Optional<String> directPath() {
        return Optional.ofNullable(mediaDirectPath);
    }

    /**
     * Returns the CDN direct path for the uploaded blob.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — directPath: n
     * @return an {@link Optional} containing the direct path, or empty if
     *         not set
     */
    @Override
    public Optional<String> mediaDirectPath() {
        return Optional.ofNullable(mediaDirectPath);
    }

    /**
     * Returns the opaque server handle identifying the uploaded blob.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — handle: a,
     *           WAWebSyncdNetCallbacksApi.uploadSyncExternalPatch — handle
     * @return an {@link Optional} containing the handle, or empty if not set
     */
    public Optional<String> handle() {
        return Optional.ofNullable(handle);
    }

    /**
     * Returns the size of the plaintext blob in bytes.
     *
     * <p>This is a convenience alias for {@link #mediaSize()} using the
     * WhatsApp Web protobuf field name.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileSizeBytes: t.byteLength
     * @return an {@link OptionalLong} containing the size, or empty if not set
     */
    public OptionalLong fileSizeBytes() {
        return mediaSize == null ? OptionalLong.empty() : OptionalLong.of(mediaSize);
    }

    /**
     * Returns the size of the plaintext blob in bytes.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileSizeBytes: t.byteLength
     * @return an {@link OptionalLong} containing the size, or empty if not set
     */
    @Override
    public OptionalLong mediaSize() {
        return mediaSize == null ? OptionalLong.empty() : OptionalLong.of(mediaSize);
    }

    /**
     * Returns the SHA-256 digest of the plaintext blob.
     *
     * <p>This is a convenience alias for {@link #mediaSha256()} using the
     * WhatsApp Web protobuf field name.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileSha256: decodeB64(calculateFilehash(t))
     * @return an {@link Optional} containing the hash bytes, or empty if
     *         not set
     */
    public Optional<byte[]> fileSha256() {
        return Optional.ofNullable(mediaSha256);
    }

    /**
     * Returns the SHA-256 digest of the plaintext blob.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileSha256: decodeB64(calculateFilehash(t))
     * @return an {@link Optional} containing the hash bytes, or empty if
     *         not set
     */
    @Override
    public Optional<byte[]> mediaSha256() {
        return Optional.ofNullable(mediaSha256);
    }

    /**
     * Returns the SHA-256 digest of the encrypted blob.
     *
     * <p>This is a convenience alias for {@link #mediaEncryptedSha256()} using
     * the WhatsApp Web protobuf field name.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileEncSha256: r
     * @return an {@link Optional} containing the hash bytes, or empty if
     *         not set
     */
    public Optional<byte[]> fileEncSha256() {
        return Optional.ofNullable(mediaEncryptedSha256);
    }

    /**
     * Returns the SHA-256 digest of the encrypted blob.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileEncSha256: r
     * @return an {@link Optional} containing the hash bytes, or empty if
     *         not set
     */
    @Override
    public Optional<byte[]> mediaEncryptedSha256() {
        return Optional.ofNullable(mediaEncryptedSha256);
    }

    /**
     * Returns the CDN media URL.
     *
     * <p>External blob references do not carry a media URL; the blob is
     * always fetched via {@link #mediaDirectPath()}.  This method always
     * returns an empty {@link Optional}.
     *
     * @implNote NO_WA_BASIS — ExternalBlobReference has no mediaUrl field;
     *           required by MediaProvider interface
     * @return an empty {@link Optional}, always
     */
    @Override
    public Optional<String> mediaUrl() {
        return Optional.empty();
    }

    /**
     * Returns the media path identifying this provider as application state
     * media.
     *
     * <p>External blob references are always uploaded with type
     * {@code "md-app-state"}, corresponding to {@link MediaPath#APP_STATE}.
     *
     * @implNote WAWebSyncdNetCallbacksApi.uploadSyncExternalPatch — type: "md-app-state"
     * @return {@link MediaPath#APP_STATE}, always
     */
    @Override
    public MediaPath mediaPath() {
        return MediaPath.APP_STATE;
    }

    /**
     * Sets the symmetric encryption key.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — mediaKey: i
     * @param mediaKey the media key bytes
     */
    @Override
    public void setMediaKey(byte[] mediaKey) {
        this.mediaKey = mediaKey;
    }

    /**
     * Sets the CDN direct path.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — directPath: n
     * @param mediaDirectPath the direct path string
     */
    @Override
    public void setMediaDirectPath(String mediaDirectPath) {
        this.mediaDirectPath = mediaDirectPath;
    }

    /**
     * Sets the opaque server handle.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — handle: a
     * @param handle the server handle string
     */
    public void setHandle(String handle) {
        this.handle = handle;
    }

    /**
     * Sets the plaintext blob size in bytes.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileSizeBytes: t.byteLength
     * @param mediaSize the blob size in bytes
     */
    @Override
    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
    }

    /**
     * Sets the SHA-256 digest of the plaintext blob.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileSha256: decodeB64(calculateFilehash(t))
     * @param mediaSha256 the plaintext SHA-256 hash bytes
     */
    @Override
    public void setMediaSha256(byte[] mediaSha256) {
        this.mediaSha256 = mediaSha256;
    }

    /**
     * Sets the SHA-256 digest of the encrypted blob.
     *
     * @implNote WAWebSyncdMMSUpload.buildExternalBlobReference — fileEncSha256: r
     * @param mediaEncryptedSha256 the encrypted SHA-256 hash bytes
     */
    @Override
    public void setMediaEncryptedSha256(byte[] mediaEncryptedSha256) {
        this.mediaEncryptedSha256 = mediaEncryptedSha256;
    }

    /**
     * No-op implementation for the media URL setter.
     *
     * <p>External blob references do not carry a media URL, so this method
     * has no effect.
     *
     * @implNote NO_WA_BASIS — required by MediaProvider interface;
     *           ExternalBlobReference has no url field
     * @param mediaUrl ignored
     */
    @Override
    public void setMediaUrl(String mediaUrl) {
    }

    /**
     * No-op implementation for the media key timestamp setter.
     *
     * <p>External blob references do not carry a key timestamp, so this
     * method has no effect.
     *
     * @implNote NO_WA_BASIS — required by MediaProvider interface;
     *           ExternalBlobReference has no timestamp field
     * @param timestamp ignored
     */
    @Override
    public void setMediaKeyTimestamp(Instant timestamp) {
    }
}
