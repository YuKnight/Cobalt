package com.github.auties00.cobalt.model.sync.action.media;

import com.github.auties00.cobalt.model.media.MediaPath;
import com.github.auties00.cobalt.model.media.MediaProvider;
import com.github.auties00.cobalt.model.mixin.InstantSecondsMixin;
import com.github.auties00.cobalt.model.preference.Sticker;
import com.github.auties00.cobalt.model.preference.StickerBuilder;
import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

@ProtobufMessage(name = "SyncActionValue.StickerAction")
public final class StickerAction implements SyncAction<StickerActionArgs>, MediaProvider {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "favoriteSticker";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 7;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_LOW;

    /**
     * {@inheritDoc}
     */
    @Override
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int actionVersion() {
        return ACTION_VERSION;
    }


    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    String mediaUrl;

    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    byte[] mediaEncryptedSha256;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    byte[] mediaKey;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    String mimetype;

    @ProtobufProperty(index = 5, type = ProtobufType.UINT32)
    Integer height;

    @ProtobufProperty(index = 6, type = ProtobufType.UINT32)
    Integer width;

    @ProtobufProperty(index = 7, type = ProtobufType.STRING)
    String mediaDirectPath;

    @ProtobufProperty(index = 8, type = ProtobufType.UINT64)
    Long mediaSize;

    @ProtobufProperty(index = 9, type = ProtobufType.BOOL)
    Boolean isFavorite;

    @ProtobufProperty(index = 10, type = ProtobufType.UINT32)
    Integer deviceIdHint;

    @ProtobufProperty(index = 11, type = ProtobufType.BOOL)
    Boolean isLottie;

    @ProtobufProperty(index = 12, type = ProtobufType.STRING)
    String imageHash;

    @ProtobufProperty(index = 13, type = ProtobufType.BOOL)
    Boolean isAvatarSticker;

    @ProtobufProperty(index = 14, type = ProtobufType.INT64, mixins = InstantSecondsMixin.class)
    Instant mediaKeyTimestamp;


    StickerAction(String mediaUrl, byte[] mediaEncryptedSha256, byte[] mediaKey, String mimetype, Integer height, Integer width, String mediaDirectPath, Long mediaSize, Boolean isFavorite, Integer deviceIdHint, Boolean isLottie, String imageHash, Boolean isAvatarSticker, Instant mediaKeyTimestamp) {
        this.mediaUrl = mediaUrl;
        this.mediaEncryptedSha256 = mediaEncryptedSha256;
        this.mediaKey = mediaKey;
        this.mimetype = mimetype;
        this.height = height;
        this.width = width;
        this.mediaDirectPath = mediaDirectPath;
        this.mediaSize = mediaSize;
        this.isFavorite = isFavorite;
        this.deviceIdHint = deviceIdHint;
        this.isLottie = isLottie;
        this.imageHash = imageHash;
        this.isAvatarSticker = isAvatarSticker;
        this.mediaKeyTimestamp = mediaKeyTimestamp;
    }

    public Optional<String> url() {
        return Optional.ofNullable(mediaUrl);
    }

    @Override
    public Optional<String> mediaUrl() {
        return Optional.ofNullable(mediaUrl);
    }

    public Optional<byte[]> fileEncSha256() {
        return Optional.ofNullable(mediaEncryptedSha256);
    }

    @Override
    public Optional<byte[]> mediaEncryptedSha256() {
        return Optional.ofNullable(mediaEncryptedSha256);
    }

    public Optional<byte[]> mediaKey() {
        return Optional.ofNullable(mediaKey);
    }

    public Optional<String> mimetype() {
        return Optional.ofNullable(mimetype);
    }

    public OptionalInt height() {
        return height == null ? OptionalInt.empty() : OptionalInt.of(height);
    }

    public OptionalInt width() {
        return width == null ? OptionalInt.empty() : OptionalInt.of(width);
    }

    public Optional<String> directPath() {
        return Optional.ofNullable(mediaDirectPath);
    }

    @Override
    public Optional<String> mediaDirectPath() {
        return Optional.ofNullable(mediaDirectPath);
    }

    public OptionalLong fileLength() {
        return mediaSize == null ? OptionalLong.empty() : OptionalLong.of(mediaSize);
    }

    @Override
    public OptionalLong mediaSize() {
        return mediaSize == null ? OptionalLong.empty() : OptionalLong.of(mediaSize);
    }

    public boolean isFavorite() {
        return isFavorite != null && isFavorite;
    }

    public OptionalInt deviceIdHint() {
        return deviceIdHint == null ? OptionalInt.empty() : OptionalInt.of(deviceIdHint);
    }

    public boolean isLottie() {
        return isLottie != null && isLottie;
    }

    public Optional<String> imageHash() {
        return Optional.ofNullable(imageHash);
    }

    public boolean isAvatarSticker() {
        return isAvatarSticker != null && isAvatarSticker;
    }

    /**
     * Returns the {@link Instant} at which the sticker's media key was
     * generated.
     *
     * @implNote {@code WAWebStickersFavoriteSyncAction.apply} threads the media
     *           key timestamp through the rehydrated sticker so the CDN can
     *           verify key freshness on subsequent downloads.
     * @return an {@link Optional} containing the media key timestamp, or empty
     *         if the field is not set
     */
    public Optional<Instant> mediaKeyTimestamp() {
        return Optional.ofNullable(mediaKeyTimestamp);
    }

    @Override
    public Optional<byte[]> mediaSha256() {
        return Optional.empty();
    }

    @Override
    public MediaPath mediaPath() {
        return MediaPath.STICKER;
    }

    @Override
    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    @Override
    public void setMediaEncryptedSha256(byte[] mediaEncryptedSha256) {
        this.mediaEncryptedSha256 = mediaEncryptedSha256;
    }

    @Override
    public void setMediaKey(byte[] mediaKey) {
        this.mediaKey = mediaKey;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    @Override
    public void setMediaDirectPath(String mediaDirectPath) {
        this.mediaDirectPath = mediaDirectPath;
    }

    @Override
    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
    }

    public void setFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setDeviceIdHint(Integer deviceIdHint) {
        this.deviceIdHint = deviceIdHint;
    }

    public void setLottie(Boolean isLottie) {
        this.isLottie = isLottie;
    }

    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }

    public void setAvatarSticker(Boolean isAvatarSticker) {
        this.isAvatarSticker = isAvatarSticker;
    }

    @Override
    public void setMediaSha256(byte[] bytes) {
    }

    /**
     * Stores the epoch-second timestamp at which this sticker's media key was
     * generated.
     *
     * @implNote {@code WAWebStickersFavoriteSyncAction} records the CDN media
     *           key timestamp on the sticker action so downstream consumers
     *           can verify key freshness when the sticker is later
     *           rehydrated; mirrors the {@code MediaProvider} contract shared
     *           with other media-bearing types.
     * @param timestamp the media key timestamp, or {@code null} to clear the
     *                  field
     */
    @Override
    public void setMediaKeyTimestamp(Instant timestamp) {
        this.mediaKeyTimestamp = timestamp;
    }

    /**
     * Converts this sticker action into a {@link Sticker} preference entry,
     * copying over the media descriptors and feature flags.
     *
     * @implNote {@code WAWebStickersFavoriteSyncAction.apply} rehydrates a
     *           {@code Sticker} model from the sync payload; this method
     *           mirrors that conversion. TODO: propagate
     *           {@link #mediaKeyTimestamp} once {@code Sticker} exposes a
     *           dedicated {@code mediaKeyTimestamp} field - the current
     *           {@code Sticker.timestamp} field represents a generic sticker
     *           timestamp, not the media key timestamp, so this conversion
     *           intentionally drops the media key timestamp until the
     *           {@code Sticker} model is extended.
     * @return a {@link Sticker} populated with the fields that currently have
     *         a matching {@code Sticker} representation
     */
    public Sticker toSticker() {
        // TODO: propagate mediaKeyTimestamp once Sticker exposes a dedicated
        //       mediaKeyTimestamp field. Sticker.timestamp is a generic
        //       sticker timestamp, not a media key timestamp.
        return new StickerBuilder()
                .mediaUrl(mediaUrl)
                .mediaEncryptedSha256(mediaEncryptedSha256)
                .mediaKey(mediaKey)
                .mimetype(mimetype)
                .height(height)
                .width(width)
                .mediaDirectPath(mediaDirectPath)
                .mediaSize(mediaSize)
                .favorite(isFavorite())
                .deviceIdHint(deviceIdHint)
                .isAvatar(isAvatarSticker())
                .build();
    }
}
