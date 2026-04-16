package com.github.auties00.cobalt.model.media;

import it.auties.protobuf.annotation.ProtobufEnum;

import java.util.*;

/**
 * A classification of every media type that WhatsApp can upload to or download
 * from its CDN.
 *
 * <p>Each constant bundles together the information the media pipeline needs to
 * handle a particular kind of attachment:
 * <ul>
 *   <li>the server-side identifier used on the wire (for example {@code "ppic"}
 *       for a profile picture or {@code "product-catalog-image"} for a catalog
 *       entry),</li>
 *   <li>the CDN path segment used when constructing hash-based URLs (for
 *       example {@code "mms/image"}),</li>
 *   <li>the HKDF info string used when deriving the per-type encryption key
 *       (for example {@code "WhatsApp Image Keys"}),</li>
 *   <li>a flag indicating whether the downloaded payload is compressed and
 *       must be inflated before use.</li>
 * </ul>
 *
 * <p>The special {@link #NONE} constant represents the absence of a media
 * attachment and has no identifier, path, or key label. Newsletter media types
 * use a dedicated CDN namespace prefixed with {@code "newsletter/"} and are
 * not end-to-end encrypted, so they have no HKDF key label. Some types such as
 * {@link #PTV} or {@link #PAYMENT_BG_IMAGE} have no hash-based CDN path and
 * are handled exclusively through direct-path downloads.
 *
 * <p>Use {@link #ofId(String)} to resolve a server identifier received from
 * the wire into the corresponding constant, and {@link #known()} to enumerate
 * every concrete media type.
 */
@ProtobufEnum
public enum MediaPath {
    /**
     * Sentinel value indicating that no media is attached.
     *
     * <p>All field accessors return empty for this constant.
     */
    NONE(null, null, null, false),

    /**
     * Audio attachments uploaded as regular audio files.
     */
    AUDIO("audio", "mms/audio", "WhatsApp Audio Keys", false),

    /**
     * Generic file attachments sent as documents.
     */
    DOCUMENT("document", "mms/document", "WhatsApp Document Keys", false),

    /**
     * Animated GIF attachments. GIF media is internally stored and encrypted
     * as video on the CDN.
     */
    GIF("gif", "mms/gif", "WhatsApp Video Keys", false),

    /**
     * Photo and image attachments.
     */
    IMAGE("image", "mms/image", "WhatsApp Image Keys", false),

    /**
     * Profile picture uploads. Profile pictures are stored publicly and do
     * not use HKDF-derived encryption keys.
     */
    PROFILE_PICTURE("ppic", "pps/photo", null, false),

    /**
     * Product images used in business catalogs. Product images share the
     * image encryption scheme.
     */
    PRODUCT("product", "mms/image", "WhatsApp Image Keys", false),

    /**
     * Push-to-talk voice messages.
     */
    VOICE("ptt", "mms/ptt", "WhatsApp Audio Keys", false),

    /**
     * Sticker attachments.
     */
    STICKER("sticker", "mms/sticker", "WhatsApp Image Keys", false),

    /**
     * Thumbnail previews for document attachments.
     */
    THUMBNAIL_DOCUMENT("thumbnail-document", "mms/thumbnail-document", "WhatsApp Document Thumbnail Keys", false),

    /**
     * Thumbnail previews for link previews.
     */
    THUMBNAIL_LINK("thumbnail-link", "mms/thumbnail-link", "WhatsApp Link Thumbnail Keys", false),

    /**
     * Thumbnail previews for image attachments.
     */
    THUMBNAIL_IMAGE("thumbnail-image", "mms/thumbnail-image", "WhatsApp Image Thumbnail Keys", false),

    /**
     * Thumbnail previews for video attachments.
     */
    THUMBNAIL_VIDEO("thumbnail-video", "mms/thumbnail-video", "WhatsApp Video Thumbnail Keys", false),

    /**
     * Thumbnail previews for GIF attachments. Declared by the server but not
     * routed through the hash-based CDN paths.
     */
    THUMBNAIL_GIF("thumbnail-gif", null, null, false),

    /**
     * Video attachments.
     */
    VIDEO("video", "mms/video", "WhatsApp Video Keys", false),

    /**
     * Push-to-video personal video messages. PTV media shares the video
     * encryption scheme but is always downloaded through the direct path.
     */
    PTV("ptv", null, "WhatsApp Video Keys", false),

    /**
     * Encrypted application state synchronization blobs. The payload is
     * compressed and must be inflated after decryption.
     */
    APP_STATE("md-app-state", "mms/md-app-state", "WhatsApp App State Keys", true),

    /**
     * Encrypted message history synchronization blobs delivered during
     * history sync. The payload is compressed and must be inflated after
     * decryption.
     */
    HISTORY_SYNC("md-msg-hist", "mms/md-msg-hist", "WhatsApp History Keys", true),

    /**
     * Images used in product catalog listings. Catalog images are public
     * and therefore not encrypted.
     */
    PRODUCT_CATALOG_IMAGE("product-catalog-image", "product/image", null, false),

    /**
     * Background images used by payment features. Valid on the server but
     * only fetched through the direct path.
     */
    PAYMENT_BG_IMAGE("payment-bg-image", null, "WhatsApp Payment Background Keys", false),

    /**
     * Business account cover photos.
     */
    BUSINESS_COVER_PHOTO("biz-cover-photo", "pps/biz-cover-photo", null, false),

    /**
     * Images used in native advertisement formats.
     */
    NATIVE_AD_IMAGE("ads-image", "mms/ads-image", "ads-image", false),

    /**
     * Videos used in native advertisement formats.
     */
    NATIVE_AD_VIDEO("ads-video", "mms/ads-video", "ads-video", false),

    /**
     * Bundled sticker pack archives.
     */
    STICKER_PACK("sticker-pack", "mms/sticker-pack", "WhatsApp Sticker Pack Keys", false),

    /**
     * Thumbnail previews for sticker packs.
     */
    THUMBNAIL_STICKER_PACK("thumbnail-sticker-pack", "mms/thumbnail-sticker-pack", "WhatsApp Sticker Pack Thumbnail Keys", false),

    /**
     * Artwork images attached to music tracks shared in chats.
     */
    MUSIC_ARTWORK("music-artwork", "mms/music-artwork", "WhatsApp Music Artwork Keys", false),

    /**
     * Group history synchronization blobs used when joining a group with
     * history sharing enabled.
     */
    GROUP_HISTORY("group-history", "mms/group-history", "Group History", false),

    /**
     * Identity verification (KYC) document uploads. Valid on the server but
     * not exposed through the standard media pipeline.
     */
    KYC_ID("kyc-id", null, null, false),

    /**
     * Interactive message template assets. Exposed on the server without a
     * dedicated CDN path or encryption label.
     */
    TEMPLATE("template", null, null, false),

    /**
     * Novi (Meta Pay) video uploads. Not routed through the standard media
     * pipeline.
     */
    NOVI_VIDEO("novi-video", null, null, false),

    /**
     * Novi (Meta Pay) image uploads. Not routed through the standard media
     * pipeline.
     */
    NOVI_IMAGE("novi-image", null, null, false),

    /**
     * Cross-messenger attachment (XMA) images used in shared Meta experiences.
     */
    XMA_IMAGE("xma-image", null, "WhatsApp Image Keys", false),

    /**
     * Generic preview media.
     */
    PREVIEW("preview", null, null, false),

    /**
     * Native flow interactive assets.
     */
    NATIVE_FLOW("native_flow", null, null, false),

    /**
     * Audio media posted in a newsletter channel. Newsletter content is
     * publicly readable and therefore not encrypted.
     */
    NEWSLETTER_AUDIO("newsletter-audio", "newsletter/newsletter-audio", null, false),

    /**
     * Images posted in a newsletter channel.
     */
    NEWSLETTER_IMAGE("newsletter-image", "newsletter/newsletter-image", null, false),

    /**
     * Documents posted in a newsletter channel.
     */
    NEWSLETTER_DOCUMENT("newsletter-document", "newsletter/newsletter-document", null, false),

    /**
     * GIF media posted in a newsletter channel.
     */
    NEWSLETTER_GIF("newsletter-gif", "newsletter/newsletter-gif", null, false),

    /**
     * Voice messages posted in a newsletter channel.
     */
    NEWSLETTER_VOICE("newsletter-ptt", "newsletter/newsletter-ptt", null, false),

    /**
     * Push-to-video messages posted in a newsletter channel.
     */
    NEWSLETTER_PTV("newsletter-ptv", "newsletter/newsletter-ptv", null, false),

    /**
     * Stickers posted in a newsletter channel.
     */
    NEWSLETTER_STICKER("newsletter-sticker", "newsletter/newsletter-sticker", null, false),

    /**
     * Sticker packs posted in a newsletter channel.
     */
    NEWSLETTER_STICKER_PACK("newsletter-sticker-pack", "newsletter/newsletter-sticker-pack", null, false),

    /**
     * Link preview thumbnails posted in a newsletter channel.
     */
    NEWSLETTER_THUMBNAIL_LINK("newsletter-thumbnail-link", "newsletter/newsletter-thumbnail-link", null, false),

    /**
     * Videos posted in a newsletter channel.
     */
    NEWSLETTER_VIDEO("newsletter-video", "newsletter/newsletter-video", null, false),

    /**
     * Music track artwork posted in a newsletter channel.
     */
    NEWSLETTER_MUSIC_ARTWORK("newsletter-music-artwork", "mms/newsletter-music-artwork", null, false),

    /**
     * Images exchanged through Waffle (Meta account linking) integrations.
     */
    WAFFLE_IMAGE("waffle-image", "mms/waffle-image", null, false),

    /**
     * Videos exchanged through Waffle (Meta account linking) integrations.
     */
    WAFFLE_VIDEO("waffle-video", "mms/waffle-video", null, false);

    /**
     * The server-side identifier used when referring to this media type on the
     * wire, or {@code null} for {@link #NONE}.
     */
    private final String id;

    /**
     * The CDN path segment used when building hash-based URLs for this media
     * type, or {@code null} if the media type is only fetched through the
     * direct path.
     */
    private final String path;

    /**
     * The HKDF info string used when deriving the per-type encryption key, or
     * {@code null} if the media type is not end-to-end encrypted.
     */
    private final String keyName;

    /**
     * Whether the downloaded payload must be decompressed before processing.
     */
    private final boolean inflatable;

    /**
     * The HKDF info string used when deriving encryption keys for preview
     * media. Unlike per-type info strings, this single value is shared across
     * every preview variant.
     */
    public static final String PREVIEW_HKDF_INFO = "Messenger Preview Keys";

    /**
     * The set of all concrete media types, excluding {@link #NONE}.
     */
    private static final Set<MediaPath> KNOWN;

    /**
     * Lookup map from server-side identifier to the corresponding media path.
     */
    private static final Map<String, MediaPath> BY_ID;

    static {
        var known = new HashSet<MediaPath>();
        for (var value : values()) {
            if (value != NONE) {
                known.add(value);
            }
        }
        KNOWN = Collections.unmodifiableSet(known);

        Map<String, MediaPath> byId = HashMap.newHashMap(known.size());
        for (var value : known) {
            byId.put(value.id, value);
        }
        BY_ID = Collections.unmodifiableMap(byId);
    }

    /**
     * Returns the set of all concrete media types, excluding {@link #NONE}.
     *
     * @return an unmodifiable set of every known media path
     */
    public static Set<MediaPath> known() {
        return KNOWN;
    }

    /**
     * Returns the media path whose server-side identifier matches the given
     * value.
     *
     * <p>For example, passing {@code "ppic"} returns {@link #PROFILE_PICTURE}
     * and passing {@code "product-catalog-image"} returns
     * {@link #PRODUCT_CATALOG_IMAGE}.
     *
     * @param id the server-side identifier
     * @return an {@link Optional} containing the matching media path, or
     *         empty if no constant has this identifier
     */
    public static Optional<MediaPath> ofId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    /**
     * Constructs a new media path constant.
     *
     * @param id         the server-side identifier, or {@code null} for {@link #NONE}
     * @param path       the CDN path segment, or {@code null} if hash-based URLs are unsupported
     * @param keyName    the HKDF info string, or {@code null} if not encrypted
     * @param inflatable whether the payload must be decompressed after download
     */
    MediaPath(String id, String path, String keyName, boolean inflatable) {
        this.id = id;
        this.path = path;
        this.keyName = keyName;
        this.inflatable = inflatable;
    }

    /**
     * Returns the server-side identifier for this media type.
     *
     * @return an {@link Optional} containing the identifier, or empty for
     *         {@link #NONE}
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Returns the CDN path segment for this media type.
     *
     * @return an {@link Optional} containing the path, or empty if hash-based
     *         URL construction is unsupported for this media type
     */
    public Optional<String> path() {
        return Optional.ofNullable(path);
    }

    /**
     * Returns the HKDF info string used when deriving the per-type encryption
     * key for this media type.
     *
     * @return an {@link Optional} containing the HKDF info string, or empty if
     *         the media type is not end-to-end encrypted
     */
    public Optional<String> keyName() {
        return Optional.ofNullable(keyName);
    }

    /**
     * Returns whether the downloaded payload for this media type is compressed
     * and must be decompressed before processing.
     *
     * @return {@code true} if the payload must be inflated, {@code false} otherwise
     */
    public boolean inflatable() {
        return this.inflatable;
    }
}
