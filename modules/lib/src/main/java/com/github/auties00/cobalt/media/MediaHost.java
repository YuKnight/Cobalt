package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.model.media.MediaPath;
import com.github.auties00.cobalt.model.media.MediaProvider;

import java.util.*;

/**
 * Represents a media CDN host entry returned by the WhatsApp media connection
 * query. Each host stores its hostname, the list of IP addresses advertised
 * by the server, and the sets of media types that the host supports for
 * download and upload operations. A primary host may additionally carry a
 * fallback hostname, fallback IPs, and download bucket assignments for
 * deterministic routing.
 *
 * <p>This is a sealed interface with two permitted implementations:
 * <ul>
 *   <li>{@link Primary} -- a host with type {@code "primary"}, which may
 *       carry a nested fallback hostname derived from the server-supplied
 *       {@code fallback} sub-object.</li>
 *   <li>{@link Fallback} -- a host with type {@code "fallback"}, which
 *       never carries a nested fallback.</li>
 * </ul>
 *
 * <p>Both variants apply media-type normalization before checking the
 * supported-type sets, matching the behavior of
 * {@code WAWebMediaHost.supportsDownloadMediaType} (via helper {@code d})
 * and {@code WAWebMediaHost.supportsUploadMediaType} (via helper {@code m}).
 *
 * @implNote WAWebMediaHost.MediaHost, WAWebMediaHost.HOST_TYPE
 */
public sealed interface MediaHost {

    /**
     * Enumerates the two media operations that can be performed against a
     * CDN host: uploading new media and downloading existing media.
     *
     * <p>These constants are used by {@link #routeSelection} to determine
     * which host-type-support check to apply when searching for a suitable
     * host.
     *
     * @implNote WAWebMediaHostsRouteSelection.OPERATIONS
     */
    enum Operation {
        /**
         * The upload operation, corresponding to host upload-type checks.
         *
         * @implNote WAWebMediaHostsRouteSelection.OPERATIONS.UPLOAD
         */
        UPLOAD,

        /**
         * The download operation, corresponding to host download-type checks
         * and bucket-based host selection.
         *
         * @implNote WAWebMediaHostsRouteSelection.OPERATIONS.DOWNLOAD
         */
        DOWNLOAD
    }

    /**
     * Holds the result of a route selection: an optional selected host
     * (the best match for the requested operation and media type) and
     * an optional fallback host (the first host whose type is
     * {@link Fallback}).
     *
     * @implNote WAWebMediaHostsRouteSelection.routeSelection return value
     * @param selectedHost the selected host, or empty if none matched
     * @param fallbackHost the fallback host, or empty if none exists
     */
    record RouteSelectionResult(
            Optional<MediaHost> selectedHost,
            Optional<MediaHost> fallbackHost
    ) {
    }

    /**
     * Selects the best media host for the given operation and media type from
     * the provided host list. For download operations, the method first
     * attempts bucket-based deterministic routing when the vcache aggregation
     * AB prop is enabled:
     * <ol>
     *   <li>If {@code encFileHash} is {@code null}, bucket 0 is selected.</li>
     *   <li>Otherwise, the bucket is computed as
     *       {@code base64Modulo(encFileHash, maxBuckets) + 100}.</li>
     *   <li>The host whose {@code downloadBuckets} contains the computed
     *       bucket is preferred; if none, the host at bucket 0 is tried.</li>
     * </ol>
     * If bucket-based selection does not yield a result (or the operation is
     * upload), the method falls back to a linear scan for the first host
     * that supports the requested media type.
     *
     * <p>The fallback host is always the first host in the list whose type is
     * {@link Fallback}, regardless of the operation.
     *
     * @implNote WAWebMediaHostsRouteSelection.routeSelection
     * @param operation                  the operation to perform
     * @param mediaType                  the media path to match
     * @param hosts                      the list of available hosts
     * @param encFileHash                the base64-encoded encrypted file
     *                                   hash, or {@code null} if unavailable
     * @param maxBuckets                 the maximum number of buckets, or
     *                                   {@code null} if not available
     * @param vcacheAggregationEnabled   whether the
     *                                   {@code mms_vcache_aggregation_enabled}
     *                                   AB prop is enabled
     * @return the route selection result containing the selected and fallback
     *         hosts
     */
    static RouteSelectionResult routeSelection(
            Operation operation,
            MediaPath mediaType,
            List<? extends MediaHost> hosts,
            String encFileHash,
            Integer maxBuckets,
            boolean vcacheAggregationEnabled
    ) {
        // WAWebMediaHostsRouteSelection.routeSelection
        if (hosts.isEmpty()) {
            return new RouteSelectionResult(Optional.empty(), Optional.empty());
        }

        MediaHost selected = null;
        if (operation == Operation.DOWNLOAD) {
            // WAWebMediaHostsRouteSelection.routeSelection — bucket computation
            Integer bucket;
            if (encFileHash == null) {
                bucket = 0;
            } else if (vcacheAggregationEnabled && maxBuckets != null) {
                bucket = base64Modulo(encFileHash, maxBuckets) + 100;
            } else {
                bucket = null;
            }

            // WAWebMediaHostsRouteSelection.u — build bucket-to-host map
            var bucketMap = buildBucketMap(hosts);
            var bucketHost = bucket != null ? bucketMap.get(bucket) : null;
            var defaultHost = bucketMap.get(0);

            if (bucketHost != null && supportsDownloadMediaType(bucketHost, mediaType)) {
                selected = bucketHost;
            } else if (defaultHost != null && supportsDownloadMediaType(defaultHost, mediaType)) {
                selected = defaultHost;
            }
        }

        // WAWebMediaHostsRouteSelection.routeSelection — find fallback host
        MediaHost fallback = null;
        for (var host : hosts) {
            if (host instanceof Fallback) {
                fallback = host;
                break;
            }
        }

        // WAWebMediaHostsRouteSelection.routeSelection — linear scan fallback
        if (selected == null) {
            for (var host : hosts) {
                if (operation == Operation.UPLOAD
                        ? supportsUploadMediaType(host, mediaType)
                        : supportsDownloadMediaType(host, mediaType)) {
                    selected = host;
                    break;
                }
            }
        }

        return new RouteSelectionResult(
                Optional.ofNullable(selected),
                Optional.ofNullable(fallback)
        );
    }

    /**
     * Checks whether the given host supports downloading the specified media
     * type, after applying download-type normalization.
     *
     * @implNote WAWebMediaHost.MediaHost.supportsDownloadMediaType
     * @param host      the host to check
     * @param mediaType the media type to check
     * @return {@code true} if the normalized type is in the host's download set
     */
    private static boolean supportsDownloadMediaType(MediaHost host, MediaPath mediaType) {
        return host.download().contains(normalizeDownloadMediaType(mediaType));
    }

    /**
     * Checks whether the given host supports uploading the specified media
     * type, after applying upload-type normalization.
     *
     * @implNote WAWebMediaHost.MediaHost.supportsUploadMediaType
     * @param host      the host to check
     * @param mediaType the media type to check
     * @return {@code true} if the normalized type is in the host's upload set
     */
    private static boolean supportsUploadMediaType(MediaHost host, MediaPath mediaType) {
        return host.upload().contains(normalizeUploadMediaType(mediaType));
    }

    /**
     * Builds a map from download bucket number to the host that owns that
     * bucket. Each host's {@code downloadBuckets} list is iterated and each
     * bucket number is mapped to the host. If multiple hosts claim the same
     * bucket, the last one wins.
     *
     * @implNote WAWebMediaHostsRouteSelection local function {@code u}
     * @param hosts the list of hosts to index
     * @return a map from bucket number to host
     */
    private static Map<Integer, MediaHost> buildBucketMap(List<? extends MediaHost> hosts) {
        // WAWebMediaHostsRouteSelection.u
        var map = new HashMap<Integer, MediaHost>();
        for (var host : hosts) {
            for (var bucket : host.downloadBuckets()) {
                map.put(bucket, host);
            }
        }
        return map;
    }

    /**
     * Computes a base64-decoded modulo value for deterministic bucket
     * assignment. The base64-encoded input is decoded, and each byte is
     * processed as two 4-bit nibbles. The running remainder is accumulated
     * via {@code ((remainder << 4) + nibble) % divisor}.
     *
     * @implNote WABase64Modulo (default export)
     * @param base64 the base64-encoded string
     * @param divisor the modulo divisor (maxBuckets)
     * @return the modulo result
     */
    private static int base64Modulo(String base64, int divisor) {
        // WABase64Modulo
        var decoded = Base64.getDecoder().decode(base64);
        var remainder = 0;
        for (var b : decoded) {
            var unsigned = b & 0xFF;
            var high = unsigned >> 4;
            var low = unsigned & 0x0F;
            remainder = ((remainder << 4) + high) % divisor;
            remainder = ((remainder << 4) + low) % divisor;
        }
        return remainder;
    }

    /**
     * Returns the hostname of this media CDN host.
     *
     * @implNote WAWebMediaHost.MediaHost constructor -- {@code this.hostname = t.hostname}
     * @return the hostname, never {@code null}
     */
    String hostname();

    /**
     * Returns the list of IP addresses advertised by the server for this
     * host.
     *
     * @implNote WAWebMediaHost.MediaHost constructor -- {@code this.ips = t.ips || []}
     * @return an unmodifiable list of IP address strings, never {@code null}
     */
    List<String> ips();

    /**
     * Returns the set of media types that this host supports for download
     * operations. When the server response does not specify any download
     * types in its rules, this defaults to all known media type values.
     *
     * @implNote WAWebMediaHost.MediaHost constructor -- {@code this.$1} via
     *           helper {@code c} (parseRules), defaulting to
     *           {@code MEDIA_TYPE_VALUES}
     * @return an unmodifiable set of supported download media paths
     */
    Set<MediaPath> download();

    /**
     * Returns the set of media types that this host supports for upload
     * operations. When the server response does not specify any upload
     * types in its rules, this defaults to all known media type values.
     *
     * @implNote WAWebMediaHost.MediaHost constructor -- {@code this.$2} via
     *           helper {@code c} (parseRules), defaulting to
     *           {@code MEDIA_TYPE_VALUES}
     * @return an unmodifiable set of supported upload media paths
     */
    Set<MediaPath> upload();

    /**
     * Returns the download bucket assignments for this host. Buckets are
     * integer identifiers used by the route selection algorithm for
     * deterministic host selection based on the encrypted file hash.
     *
     * @implNote WAWebMediaHost.MediaHost constructor --
     *           {@code this.downloadBuckets = r} from {@code c(t.rules)}
     * @return an unmodifiable list of bucket numbers, never {@code null}
     */
    List<Integer> downloadBuckets();

    /**
     * Returns the fallback hostname for this host, if present. Only
     * {@link Primary} hosts may carry a fallback hostname; {@link Fallback}
     * hosts always return empty.
     *
     * @implNote WAWebMediaHost.MediaHost constructor --
     *           {@code this.fallback = t.fallback != null ? new MediaHost(...) : null}
     * @return an {@link Optional} containing the fallback hostname, or empty
     */
    Optional<String> fallbackHostname();

    /**
     * Checks whether this host supports downloading the media type of the
     * given provider. Before checking the supported set, the media type is
     * normalized: {@link MediaPath#PTV} and {@link MediaPath#NEWSLETTER_PTV}
     * are mapped to {@link MediaPath#VIDEO}, and {@link MediaPath#PRODUCT}
     * is mapped to {@link MediaPath#IMAGE}.
     *
     * @implNote WAWebMediaHost.MediaHost.supportsDownloadMediaType,
     *           WAWebMediaHost.d (normalizeDownloadMediaType)
     * @param provider the media provider whose type to check
     * @return {@code true} if this host supports downloading that media type
     */
    boolean canDownload(MediaProvider provider);

    /**
     * Checks whether this host supports uploading the media type of the
     * given provider. Before checking the supported set, the media type is
     * normalized: {@link MediaPath#PTV} is mapped to
     * {@link MediaPath#VIDEO}, and {@link MediaPath#PRODUCT_CATALOG_IMAGE}
     * is mapped to {@link MediaPath#PRODUCT}.
     *
     * @implNote WAWebMediaHost.MediaHost.supportsUploadMediaType,
     *           WAWebMediaHost.m (normalizeUploadMediaType)
     * @param provider the media provider whose type to check
     * @return {@code true} if this host supports uploading that media type
     */
    boolean canUpload(MediaProvider provider);

    /**
     * Normalizes a media path for download-type checking. This mirrors the
     * WA Web helper function {@code d} in {@code WAWebMediaHost}:
     * <ul>
     *   <li>{@link MediaPath#PTV} and {@link MediaPath#NEWSLETTER_PTV} are
     *       mapped to {@link MediaPath#VIDEO}</li>
     *   <li>{@link MediaPath#PRODUCT} is mapped to
     *       {@link MediaPath#IMAGE}</li>
     *   <li>All other types are returned unchanged.</li>
     * </ul>
     *
     * @implNote WAWebMediaHost.d
     * @param path the media path to normalize
     * @return the normalized media path for download checking
     */
    private static MediaPath normalizeDownloadMediaType(MediaPath path) {
        // WAWebMediaHost.d
        return switch (path) {
            case PTV, NEWSLETTER_PTV -> MediaPath.VIDEO;
            case PRODUCT -> MediaPath.IMAGE;
            default -> path;
        };
    }

    /**
     * Normalizes a media path for upload-type checking. This mirrors the
     * WA Web helper function {@code m} in {@code WAWebMediaHost}:
     * <ul>
     *   <li>{@link MediaPath#PTV} is mapped to
     *       {@link MediaPath#VIDEO}</li>
     *   <li>{@link MediaPath#PRODUCT_CATALOG_IMAGE} is mapped to
     *       {@link MediaPath#PRODUCT}</li>
     *   <li>All other types are returned unchanged.</li>
     * </ul>
     *
     * @implNote WAWebMediaHost.m
     * @param path the media path to normalize
     * @return the normalized media path for upload checking
     */
    private static MediaPath normalizeUploadMediaType(MediaPath path) {
        // WAWebMediaHost.m
        return switch (path) {
            case PTV -> MediaPath.VIDEO;
            case PRODUCT_CATALOG_IMAGE -> MediaPath.PRODUCT;
            default -> path;
        };
    }

    /**
     * A primary media CDN host. Primary hosts have type {@code "primary"}
     * and may carry a nested fallback hostname, fallback IPs, and download
     * bucket assignments for deterministic routing.
     *
     * @implNote WAWebMediaHost.MediaHost (type = HOST_TYPE.PRIMARY),
     *           WAWebMediaHost.HOST_TYPE.PRIMARY
     * @param hostname         the hostname of this host
     * @param ips              the list of IP addresses advertised by the server
     * @param fallbackHostname the fallback hostname, or empty if no fallback
     * @param fallbackIps      the list of fallback IP addresses
     * @param downloadBuckets  the download bucket assignments for this host
     * @param download         the set of supported download media types
     * @param upload           the set of supported upload media types
     */
    record Primary(
            String hostname,
            List<String> ips,
            Optional<String> fallbackHostname,
            List<String> fallbackIps,
            List<Integer> downloadBuckets,
            Set<MediaPath> download,
            Set<MediaPath> upload
    ) implements MediaHost {

        /**
         * Checks whether this primary host supports downloading the media
         * type of the given provider, after applying download-type
         * normalization.
         *
         * @implNote WAWebMediaHost.MediaHost.supportsDownloadMediaType
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalized type is in the download set
         */
        @Override
        public boolean canDownload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            // WAWebMediaHost.supportsDownloadMediaType -> this.$1.has(d(t))
            return download.contains(normalizeDownloadMediaType(provider.mediaPath()));
        }

        /**
         * Checks whether this primary host supports uploading the media type
         * of the given provider, after applying upload-type normalization.
         *
         * @implNote WAWebMediaHost.MediaHost.supportsUploadMediaType
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalized type is in the upload set
         */
        @Override
        public boolean canUpload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            // WAWebMediaHost.supportsUploadMediaType -> this.$2.has(m(t))
            return upload.contains(normalizeUploadMediaType(provider.mediaPath()));
        }
    }

    /**
     * A fallback media CDN host. Fallback hosts have type
     * {@code "fallback"} and never carry a nested fallback.
     *
     * @implNote WAWebMediaHost.MediaHost (type = HOST_TYPE.FALLBACK),
     *           WAWebMediaHost.HOST_TYPE.FALLBACK
     * @param hostname         the hostname of this host
     * @param ips              the list of IP addresses advertised by the server
     * @param downloadBuckets  the download bucket assignments for this host
     * @param download         the set of supported download media types
     * @param upload           the set of supported upload media types
     */
    record Fallback(
            String hostname,
            List<String> ips,
            List<Integer> downloadBuckets,
            Set<MediaPath> download,
            Set<MediaPath> upload
    ) implements MediaHost {

        /**
         * Checks whether this fallback host supports downloading the media
         * type of the given provider, after applying download-type
         * normalization.
         *
         * @implNote WAWebMediaHost.MediaHost.supportsDownloadMediaType
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalized type is in the download set
         */
        @Override
        public boolean canDownload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            // WAWebMediaHost.supportsDownloadMediaType -> this.$1.has(d(t))
            return download.contains(normalizeDownloadMediaType(provider.mediaPath()));
        }

        /**
         * Checks whether this fallback host supports uploading the media
         * type of the given provider, after applying upload-type
         * normalization.
         *
         * @implNote WAWebMediaHost.MediaHost.supportsUploadMediaType
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalized type is in the upload set
         */
        @Override
        public boolean canUpload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            // WAWebMediaHost.supportsUploadMediaType -> this.$2.has(m(t))
            return upload.contains(normalizeUploadMediaType(provider.mediaPath()));
        }

        /**
         * Returns an empty optional, as fallback hosts never carry a nested
         * fallback hostname.
         *
         * @implNote WAWebMediaHost.MediaHost constructor -- fallback hosts
         *           are constructed with {@code fallback: void 0}
         * @return an empty {@link Optional}
         */
        @Override
        public Optional<String> fallbackHostname() {
            return Optional.empty();
        }
    }
}
