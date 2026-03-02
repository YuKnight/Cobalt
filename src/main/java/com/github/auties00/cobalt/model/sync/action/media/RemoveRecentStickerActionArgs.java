package com.github.auties00.cobalt.model.sync.action.media;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link RemoveRecentStickerAction}.
 *
 * <p>The sync index produced is {@code ["removeRecentSticker", stickerFileHash]}.
 * The file hash uniquely identifies the sticker in the recent-stickers collection.
 *
 * @param stickerFileHash the file hash of the sticker to remove from the recent list
 */
public record RemoveRecentStickerActionArgs(String stickerFileHash) implements SyncActionArgs {
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
