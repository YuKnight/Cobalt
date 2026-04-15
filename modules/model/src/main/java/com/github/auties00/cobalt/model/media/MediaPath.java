package com.github.auties00.cobalt.model.media;

import it.auties.protobuf.annotation.ProtobufEnum;

import java.util.*;

/**
 * A media attachment type that maps to a specific CDN upload/download path
 * and encryption key derivation label.
 *
 * <p>Each constant defines a tuple of: the server media type identifier as
 * used by the WhatsApp protocol (for example {@code "ppic"} or
 * {@code "product-catalog-image"}), the CDN path segment used to construct
 * the upload or download URL (for example {@code "mms/image"} or
 * {@code "pps/photo"}), the HKDF info string used when deriving the
 * encryption key for that media type (for example
 * {@code "WhatsApp Image Keys"}), and a flag indicating whether the
 * downloaded content is compressed and must be inflated before use.
 *
 * <p>The {@link #NONE} constant represents the absence of a media
 * attachment. All other constants describe a known attachment type. The
 * {@link #known()} method returns the set of all defined attachment types
 * excluding {@code NONE}, and {@link #ofId(String)} provides a lookup by
 * the server media type identifier.
 *
 * <p>Newsletter media types use a separate CDN namespace prefixed with
 * {@code "newsletter/"} and do not require a key derivation label because
 * newsletter media is not end-to-end encrypted.
 *
 * @implNote WAServerMediaType.SERVER_MEDIA, WAWebMmsMediaTypes.MEDIA_TYPES,
 *           WAWebMmsClientFormatHashUrl
 */
@ProtobufEnum
public enum MediaPath {
    /**
     * The singleton instance representing the absence of a media attachment.
     * The id, path, and key name are all {@code null}.
     *
     * @implNote No WA Web equivalent; Cobalt-specific sentinel value
     */
    NONE(null, null, null, false),

    /**
     * The singleton instance for audio attachments, using the CDN path
     * {@code "mms/audio"} and the key label {@code "WhatsApp Audio Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "audio",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.AUDIO
     */
    AUDIO("audio", "mms/audio", "WhatsApp Audio Keys", false),

    /**
     * The singleton instance for document attachments, using the CDN path
     * {@code "mms/document"} and the key label
     * {@code "WhatsApp Document Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "document",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.DOCUMENT
     */
    DOCUMENT("document", "mms/document", "WhatsApp Document Keys", false),

    /**
     * The singleton instance for GIF attachments, using the CDN path
     * {@code "mms/gif"} and the key label {@code "WhatsApp Video Keys"}.
     * GIF media is stored as video on the server.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "gif",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.GIF
     */
    GIF("gif", "mms/gif", "WhatsApp Video Keys", false),

    /**
     * The singleton instance for image attachments, using the CDN path
     * {@code "mms/image"} and the key label {@code "WhatsApp Image Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "image",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.IMAGE
     */
    IMAGE("image", "mms/image", "WhatsApp Image Keys", false),

    /**
     * The singleton instance for profile picture media, using the CDN path
     * {@code "pps/photo"}. Profile pictures do not use a key derivation
     * label.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "ppic",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.PPIC,
     *           WAWebMmsClientFormatHashUrl ppic -> "/pps/photo"
     */
    PROFILE_PICTURE("ppic", "pps/photo", null, false),

    /**
     * The singleton instance for product images, using the CDN path
     * {@code "mms/image"} and the key label {@code "WhatsApp Image Keys"}.
     * Product images share the same encryption scheme as regular images.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.PRODUCT,
     *           WAWebMmsClientFormatHashUrl product -> "/mms/image"
     */
    PRODUCT("product", "mms/image", "WhatsApp Image Keys", false),

    /**
     * The singleton instance for push-to-talk voice message attachments,
     * using the CDN path {@code "mms/ptt"} and the key label
     * {@code "WhatsApp Audio Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "ptt",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.PTT
     */
    VOICE("ptt", "mms/ptt", "WhatsApp Audio Keys", false),

    /**
     * The singleton instance for sticker attachments, using the CDN path
     * {@code "mms/sticker"} and the key label
     * {@code "WhatsApp Image Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "sticker",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.STICKER
     */
    STICKER("sticker", "mms/sticker", "WhatsApp Image Keys", false),

    /**
     * The singleton instance for document thumbnail media, using the CDN
     * path {@code "mms/thumbnail-document"} and the key label
     * {@code "WhatsApp Document Thumbnail Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "thumbnail-document",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.THUMBNAIL_DOCUMENT
     */
    THUMBNAIL_DOCUMENT("thumbnail-document", "mms/thumbnail-document", "WhatsApp Document Thumbnail Keys", false),

    /**
     * The singleton instance for link preview thumbnail media, using the
     * CDN path {@code "mms/thumbnail-link"} and the key label
     * {@code "WhatsApp Link Thumbnail Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "thumbnail-link",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.THUMBNAIL_LINK
     */
    THUMBNAIL_LINK("thumbnail-link", "mms/thumbnail-link", "WhatsApp Link Thumbnail Keys", false),

    /**
     * The singleton instance for image thumbnail media, using the CDN path
     * {@code "mms/thumbnail-image"} and the key label
     * {@code "WhatsApp Image Thumbnail Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "thumbnail-image",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.THUMBNAIL_IMAGE
     */
    THUMBNAIL_IMAGE("thumbnail-image", "mms/thumbnail-image", "WhatsApp Image Thumbnail Keys", false),

    /**
     * The singleton instance for video thumbnail media, using the CDN path
     * {@code "mms/thumbnail-video"} and the key label
     * {@code "WhatsApp Video Thumbnail Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "thumbnail-video",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.THUMBNAIL_VIDEO
     */
    THUMBNAIL_VIDEO("thumbnail-video", "mms/thumbnail-video", "WhatsApp Video Thumbnail Keys", false),

    /**
     * The singleton instance for GIF thumbnail media. GIF thumbnails are
     * valid server media types but are filtered out of media connection
     * routing. The CDN path is {@code null} because GIF thumbnails do not
     * support hash-based URL construction.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "thumbnail-gif"
     */
    THUMBNAIL_GIF("thumbnail-gif", null, null, false),

    /**
     * The singleton instance for video attachments, using the CDN path
     * {@code "mms/video"} and the key label {@code "WhatsApp Video Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "video",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.VIDEO
     */
    VIDEO("video", "mms/video", "WhatsApp Video Keys", false),

    /**
     * The singleton instance for push-to-video (personal video message)
     * attachments, using the key label {@code "WhatsApp Video Keys"}.
     * PTV is not present in the hash-based URL map
     * ({@code WAWebMmsClientFormatHashUrl}); media host resolution remaps
     * PTV to {@link #VIDEO} for CDN routing purposes, so PTV media is
     * uploaded and downloaded via direct-path URLs only.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.PTV,
     *           WAWebCryptoMediaTypeInfo -> "WhatsApp Video Keys",
     *           WAWebMediaHost.d remaps PTV to VIDEO for CDN routing
     */
    PTV("ptv", null, "WhatsApp Video Keys", false),

    /**
     * The singleton instance for application state synchronization blobs,
     * using the CDN path {@code "mms/md-app-state"} and the key label
     * {@code "WhatsApp App State Keys"}. This media type is inflatable,
     * meaning the downloaded content must be decompressed.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "md-app-state",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.MD_APP_STATE
     */
    APP_STATE("md-app-state", "mms/md-app-state", "WhatsApp App State Keys", true),

    /**
     * The singleton instance for message history synchronization blobs,
     * using the CDN path {@code "mms/md-msg-hist"} and the key label
     * {@code "WhatsApp History Keys"}. This media type is inflatable,
     * meaning the downloaded content must be decompressed.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "md-msg-hist",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.HISTORY_SYNC
     */
    HISTORY_SYNC("md-msg-hist", "mms/md-msg-hist", "WhatsApp History Keys", true),

    /**
     * The singleton instance for product catalog images, using the CDN
     * path {@code "product/image"}. Catalog images do not use a key
     * derivation label.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.PRODUCT_CATALOG_IMAGE,
     *           WAWebMmsClientFormatHashUrl "product-catalog-image" -> "/product/image"
     */
    PRODUCT_CATALOG_IMAGE("product-catalog-image", "product/image", null, false),

    /**
     * The singleton instance for payment background images, using the key
     * label {@code "WhatsApp Payment Background Keys"}. This type is a
     * valid server media type but is not present in the hash URL map, so
     * the CDN path is {@code null} and direct-path-based downloads are
     * used instead.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "payment-bg-image",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.PAYMENT_BG_IMAGE,
     *           WAWebCryptoMediaTypeInfo
     */
    PAYMENT_BG_IMAGE("payment-bg-image", null, "WhatsApp Payment Background Keys", false),

    /**
     * The singleton instance for business cover photos, using the CDN path
     * {@code "pps/biz-cover-photo"}. Cover photos do not use a key
     * derivation label.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "biz-cover-photo",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.BIZ_COVER_PHOTO,
     *           WAWebMmsClientFormatHashUrl "biz-cover-photo" -> "/pps/biz-cover-photo"
     */
    BUSINESS_COVER_PHOTO("biz-cover-photo", "pps/biz-cover-photo", null, false),

    /**
     * The singleton instance for native advertisement images, using the
     * CDN path {@code "mms/ads-image"} and the key label
     * {@code "ads-image"}.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.NATIVE_AD_IMAGE,
     *           WAWebMmsClientFormatHashUrl "ads-image" -> "/mms/ads-image"
     */
    NATIVE_AD_IMAGE("ads-image", "mms/ads-image", "ads-image", false),

    /**
     * The singleton instance for native advertisement videos, using the
     * CDN path {@code "mms/ads-video"} and the key label
     * {@code "ads-video"}.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.NATIVE_AD_VIDEO,
     *           WAWebMmsClientFormatHashUrl "ads-video" -> "/mms/ads-video"
     */
    NATIVE_AD_VIDEO("ads-video", "mms/ads-video", "ads-video", false),

    /**
     * The singleton instance for sticker pack bundles, using the CDN path
     * {@code "mms/sticker-pack"} and the key label
     * {@code "WhatsApp Sticker Pack Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "sticker-pack",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.STICKER_PACK
     */
    STICKER_PACK("sticker-pack", "mms/sticker-pack", "WhatsApp Sticker Pack Keys", false),

    /**
     * The singleton instance for sticker pack thumbnail media, using the
     * CDN path {@code "mms/thumbnail-sticker-pack"} and the key label
     * {@code "WhatsApp Sticker Pack Thumbnail Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "thumbnail-sticker-pack",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.THUMBNAIL_STICKER_PACK
     */
    THUMBNAIL_STICKER_PACK("thumbnail-sticker-pack", "mms/thumbnail-sticker-pack", "WhatsApp Sticker Pack Thumbnail Keys", false),

    /**
     * The singleton instance for music artwork images, using the CDN path
     * {@code "mms/music-artwork"} and the key label
     * {@code "WhatsApp Music Artwork Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "music-artwork",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.MUSIC_ARTWORK
     */
    MUSIC_ARTWORK("music-artwork", "mms/music-artwork", "WhatsApp Music Artwork Keys", false),

    /**
     * The singleton instance for group history synchronization blobs,
     * using the CDN path {@code "mms/group-history"} and the key label
     * {@code "Group History"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "group-history",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.GROUP_HISTORY
     */
    GROUP_HISTORY("group-history", "mms/group-history", "Group History", false),

    /**
     * The singleton instance for KYC (Know Your Customer) identity
     * verification media. This type is a valid server media type but is
     * filtered out of media connection routing and has no hash URL entry.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "kyc-id"
     */
    KYC_ID("kyc-id", null, null, false),

    /**
     * The singleton instance for template media. This type is a valid
     * server media type but does not support key derivation.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "template",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.TEMPLATE
     */
    TEMPLATE("template", null, null, false),

    /**
     * The singleton instance for Novi (Meta Pay) video media. This type is
     * a valid server media type but is filtered out of media connection
     * routing.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "novi-video"
     */
    NOVI_VIDEO("novi-video", null, null, false),

    /**
     * The singleton instance for Novi (Meta Pay) image media. This type is
     * a valid server media type but is filtered out of media connection
     * routing.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "novi-image"
     */
    NOVI_IMAGE("novi-image", null, null, false),

    /**
     * The singleton instance for cross-messenger attachment (XMA) images.
     * This type is a valid server media type but is filtered out of media
     * connection routing. The key label is
     * {@code "WhatsApp Image Keys"}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "xma-image",
     *           WAMediaHkdfInfo "xma-image" -> "WhatsApp Image Keys"
     */
    XMA_IMAGE("xma-image", null, "WhatsApp Image Keys", false),

    /**
     * The singleton instance for preview media. This type is a valid
     * server media type but does not support key derivation.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "preview",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.PREVIEW
     */
    PREVIEW("preview", null, null, false),

    /**
     * The singleton instance for native flow interactive media. This type
     * is defined in {@code WAWebMmsMediaTypes} but is not present in the
     * server media type list.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.NATIVE_FLOW
     */
    NATIVE_FLOW("native_flow", null, null, false),

    /**
     * The singleton instance for newsletter audio media, using the CDN
     * path {@code "newsletter/newsletter-audio"}. Newsletter media is not
     * end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-audio",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_AUDIO
     */
    NEWSLETTER_AUDIO("newsletter-audio", "newsletter/newsletter-audio", null, false),

    /**
     * The singleton instance for newsletter image media, using the CDN
     * path {@code "newsletter/newsletter-image"}. Newsletter media is not
     * end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-image",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_IMAGE
     */
    NEWSLETTER_IMAGE("newsletter-image", "newsletter/newsletter-image", null, false),

    /**
     * The singleton instance for newsletter document media, using the CDN
     * path {@code "newsletter/newsletter-document"}. Newsletter media is
     * not end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-document",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_DOCUMENT
     */
    NEWSLETTER_DOCUMENT("newsletter-document", "newsletter/newsletter-document", null, false),

    /**
     * The singleton instance for newsletter GIF media, using the CDN path
     * {@code "newsletter/newsletter-gif"}. Newsletter media is not
     * end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-gif",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_GIF
     */
    NEWSLETTER_GIF("newsletter-gif", "newsletter/newsletter-gif", null, false),

    /**
     * The singleton instance for newsletter voice message media, using
     * the CDN path {@code "newsletter/newsletter-ptt"}. Newsletter media
     * is not end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-ptt",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_PTT
     */
    NEWSLETTER_VOICE("newsletter-ptt", "newsletter/newsletter-ptt", null, false),

    /**
     * The singleton instance for newsletter push-to-video media, using
     * the CDN path {@code "newsletter/newsletter-ptv"}. Newsletter media
     * is not end-to-end encrypted.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_PTV,
     *           WAWebMmsClientFormatHashUrl
     */
    NEWSLETTER_PTV("newsletter-ptv", "newsletter/newsletter-ptv", null, false),

    /**
     * The singleton instance for newsletter sticker media, using the CDN
     * path {@code "newsletter/newsletter-sticker"}. Newsletter media is
     * not end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-sticker",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_STICKER
     */
    NEWSLETTER_STICKER("newsletter-sticker", "newsletter/newsletter-sticker", null, false),

    /**
     * The singleton instance for newsletter sticker pack media, using the
     * CDN path {@code "newsletter/newsletter-sticker-pack"}. Newsletter
     * media is not end-to-end encrypted.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_STICKER_PACK,
     *           WAWebMmsClientFormatHashUrl
     */
    NEWSLETTER_STICKER_PACK("newsletter-sticker-pack", "newsletter/newsletter-sticker-pack", null, false),

    /**
     * The singleton instance for newsletter link preview thumbnails, using
     * the CDN path {@code "newsletter/newsletter-thumbnail-link"}.
     * Newsletter media is not end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-thumbnail-link",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_THUMBNAIL_LINK
     */
    NEWSLETTER_THUMBNAIL_LINK("newsletter-thumbnail-link", "newsletter/newsletter-thumbnail-link", null, false),

    /**
     * The singleton instance for newsletter video media, using the CDN
     * path {@code "newsletter/newsletter-video"}. Newsletter media is not
     * end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-video",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_VIDEO
     */
    NEWSLETTER_VIDEO("newsletter-video", "newsletter/newsletter-video", null, false),

    /**
     * The singleton instance for newsletter music artwork media, using
     * the CDN path {@code "mms/newsletter-music-artwork"}. Newsletter
     * media is not end-to-end encrypted.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA "newsletter-music-artwork",
     *           WAWebMmsMediaTypes.MEDIA_TYPES.NEWSLETTER_MUSIC_ARTWORK
     */
    NEWSLETTER_MUSIC_ARTWORK("newsletter-music-artwork", "mms/newsletter-music-artwork", null, false),

    /**
     * The singleton instance for Waffle image media, using the CDN path
     * {@code "mms/waffle-image"}. Waffle media is not end-to-end
     * encrypted.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.WAFFLE_IMAGE,
     *           WAWebMmsClientFormatHashUrl
     */
    WAFFLE_IMAGE("waffle-image", "mms/waffle-image", null, false),

    /**
     * The singleton instance for Waffle video media, using the CDN path
     * {@code "mms/waffle-video"}. Waffle media is not end-to-end
     * encrypted.
     *
     * @implNote WAWebMmsMediaTypes.MEDIA_TYPES.WAFFLE_VIDEO,
     *           WAWebMmsClientFormatHashUrl
     */
    WAFFLE_VIDEO("waffle-video", "mms/waffle-video", null, false);

    /**
     * The server media type identifier as used by the WhatsApp protocol
     * (for example {@code "ppic"} or {@code "product-catalog-image"}).
     * This is the value that appears in the {@code SERVER_MEDIA} array and
     * is accepted by {@code castToServerMediaType} in WA Web.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA,
     *           WAWebMmsMediaTypes.MEDIA_TYPES
     */
    private final String id;

    /**
     * The CDN path segment used to construct media upload and download URLs
     * via hash-based URL construction, or {@code null} if the media type
     * does not support hash-based URLs.
     *
     * @implNote WAWebMmsClientFormatHashUrl
     */
    private final String path;

    /**
     * The HKDF info string used when deriving the encryption key for this
     * media type, or {@code null} if the media is not end-to-end encrypted.
     *
     * @implNote WAWebCryptoMediaTypeInfo.getMediaTypeInfo,
     *           WAMediaHkdfInfo.getMediaHkdfInfo
     */
    private final String keyName;

    /**
     * Whether the downloaded content is compressed and must be inflated
     * (decompressed) before processing.
     */
    private final boolean inflatable;

    /**
     * The HKDF info string used when deriving encryption keys for preview
     * thumbnail media. Unlike per-type HKDF info strings returned by
     * {@code WAMediaHkdfInfo.getMediaHkdfInfo}, this is a single fixed
     * string shared across all preview media types.
     *
     * @implNote WAMediaHkdfInfo.getPreviewMediaHkdfInfo
     */
    public static final String PREVIEW_HKDF_INFO = "Messenger Preview Keys";

    /**
     * The precomputed set of all known media path constants, excluding
     * {@link #NONE}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA
     */
    private static final Set<MediaPath> KNOWN;

    /**
     * A lookup map from server media type identifier to the corresponding
     * {@code MediaPath} constant.
     *
     * @implNote WAServerMediaType.castToServerMediaType
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
            // WAServerMediaType.castToServerMediaType
            byId.put(value.id, value);
        }
        BY_ID = Collections.unmodifiableMap(byId);
    }

    /**
     * Returns the set of all known media path constants, excluding
     * {@link #NONE}.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA
     * @return an unmodifiable set of known media paths
     */
    public static Set<MediaPath> known() {
        return KNOWN;
    }

    /**
     * Returns the {@code MediaPath} constant corresponding to the given
     * server media type identifier.
     *
     * <p>The identifier is the server media type string as used by the
     * WhatsApp protocol. For example, passing {@code "ppic"} returns
     * {@link #PROFILE_PICTURE}, and passing {@code "product-catalog-image"}
     * returns {@link #PRODUCT_CATALOG_IMAGE}.
     *
     * @implNote WAServerMediaType.castToServerMediaType
     * @param id the server media type identifier to look up
     * @return an {@link Optional} containing the matching media path, or
     *         empty if no constant matches the given identifier
     */
    public static Optional<MediaPath> ofId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    /**
     * Constructs a new {@code MediaPath} with the given server media type
     * identifier, CDN path, key derivation label, and inflation flag.
     *
     * @implNote WAServerMediaType, WAWebMmsClientFormatHashUrl,
     *           WAWebCryptoMediaTypeInfo
     * @param id         the server media type identifier, or {@code null}
     *                   for {@link #NONE}
     * @param path       the CDN path segment, or {@code null} if hash-based
     *                   URL construction is not supported
     * @param keyName    the HKDF info string, or {@code null} if not
     *                   encrypted
     * @param inflatable whether the content must be decompressed after
     *                   download
     */
    MediaPath(String id, String path, String keyName, boolean inflatable) {
        this.id = id;
        this.path = path;
        this.keyName = keyName;
        this.inflatable = inflatable;
    }

    /**
     * Returns the server media type identifier as used by the WhatsApp
     * protocol.
     *
     * @implNote WAServerMediaType.SERVER_MEDIA,
     *           WAWebMmsMediaTypes.MEDIA_TYPES
     * @return an {@link Optional} containing the identifier, or empty for
     *         {@link #NONE}
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Returns the CDN path segment used to construct media upload and
     * download URLs via hash-based URL construction.
     *
     * @implNote WAWebMmsClientFormatHashUrl
     * @return an {@link Optional} containing the path, or empty if the
     *         media type does not support hash-based URLs or for
     *         {@link #NONE}
     */
    public Optional<String> path() {
        return Optional.ofNullable(path);
    }

    /**
     * Returns the HKDF info string used when deriving the encryption key
     * for this media type.
     *
     * @implNote WAWebCryptoMediaTypeInfo.getMediaTypeInfo,
     *           WAMediaHkdfInfo.getMediaHkdfInfo
     * @return an {@link Optional} containing the key name, or empty if the
     *         media type is not end-to-end encrypted
     */
    public Optional<String> keyName() {
        return Optional.ofNullable(keyName);
    }

    /**
     * Returns whether the downloaded content for this media type is
     * compressed and must be inflated before processing.
     *
     * @return {@code true} if the content must be decompressed,
     *         {@code false} otherwise
     */
    public boolean inflatable() {
        return this.inflatable;
    }
}
