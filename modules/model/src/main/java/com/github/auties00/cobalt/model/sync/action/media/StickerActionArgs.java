package com.github.auties00.cobalt.model.sync.action.media;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link StickerAction}.
 *
 * <p>The sync index produced is {@code ["favoriteSticker", stickerFileHash]}.
 * The file hash uniquely identifies the sticker in the favorite-stickers collection.
 *
 * @param stickerFileHash the file hash of the sticker being favorited or unfavorited
 */
public record StickerActionArgs(String stickerFileHash) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the sticker file hash
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{stickerFileHash};
    }
}
