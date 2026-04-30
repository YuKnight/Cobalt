package com.github.auties00.cobalt.node.usync.result;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * Success result of {@code WAWebUsyncPicture.pictureParser}.
 *
 * <p>Carries the profile-picture id and can be passed to the media service to
 * download the actual JPEG payload.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncPicture")
public final class PictureResult implements UsyncProtocolResponse {
    /**
     * Holds the profile-picture id.
     */
    private final int id;

    /**
     * Creates a new picture result.
     *
     * @param id the profile-picture id
     */
    public PictureResult(int id) {
        this.id = id;
    }

    /**
     * Returns the profile-picture id.
     *
     * @return the id
     */
    public int id() {
        return id;
    }
}
