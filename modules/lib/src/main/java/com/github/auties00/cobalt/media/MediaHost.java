package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.media.MediaPath;
import com.github.auties00.cobalt.model.media.MediaProvider;

import java.util.*;

/**
 * Represents a CDN host entry returned by the WhatsApp {@code media_conn}
 * query.
 *
 * <p>Each host carries a hostname, the IP addresses advertised by the
 * server, the sets of media types it accepts for download and upload
 * operations, and the list of download buckets it owns. Primary hosts
 * may additionally carry a nested fallback hostname and a list of fallback
 * IP addresses which the retry loop rotates to when the primary fails.
 *
 * <p>The sealed interface has two permitted implementations:
 * <ul>
 *   <li>{@link Primary}: a host with type {@code "primary"} that may
 *       advertise a nested fallback hostname.</li>
 *   <li>{@link Fallback}: a host with type {@code "fallback"} that never
 *       advertises a nested fallback.</li>
 * </ul>
 *
 * <p>Both variants normalise the media type before checking their
 * download/upload supported sets, mirroring WA Web's helper
 * {@code d(t)} for downloads and {@code m(t)} for uploads which collapse
 * PTV/newsletter-PTV to video and product/product-catalog-image down to
 * their base types.
 *
 * @implNote WAWebMediaHost: {@code MediaHost} class and {@code HOST_TYPE}
 * enum. WAWebMediaHostsRouteSelection: {@code routeSelection} and
 * {@code OPERATIONS}. WABase64Modulo: deterministic bucket selection.
 */
@WhatsAppWebModule(moduleName = "WAWebMediaHost")
@WhatsAppWebModule(moduleName = "WAWebMediaHostsRouteSelection")
@WhatsAppWebModule(moduleName = "WABase64Modulo")
public sealed interface MediaHost {

    /**
     * Enumerates the two media operations that can be performed against a
     * CDN host.
     *
     * <p>Used by {@link #routeSelection} to pick the correct supported-type
     * check: {@link #UPLOAD} matches against the host's upload media type
     * set, while {@link #DOWNLOAD} matches against the download set and
     * additionally participates in the bucket-based host selection.
     *
     * @implNote WAWebMediaHostsRouteSelection.OPERATIONS.
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHostsRouteSelection")
    enum Operation {
        /**
         * The upload operation; route selection chooses the first host
         * whose upload media type set contains the requested media type.
         *
         * @implNote WAWebMediaHostsRouteSelection.OPERATIONS: {@code UPLOAD}
         * constant.
         */
        UPLOAD,

        /**
         * The download operation; route selection applies bucket-based
         * host matching (when vcache aggregation is enabled) and then
         * falls back to the first host whose download media type set
         * contains the requested media type.
         *
         * @implNote WAWebMediaHostsRouteSelection.OPERATIONS: {@code DOWNLOAD}
         * constant.
         */
        DOWNLOAD
    }

    /**
     * The outcome of a route selection pass: the best-matching host for
     * the requested operation and media type, plus the first
     * fallback-class host from the connection's host list.
     *
     * <p>Consumed by {@link MediaConnection#upload(MediaProvider, java.io.InputStream)}
     * and {@link MediaConnection#download(MediaProvider)} which feed the
     * pair into the {@code selectHost} rotation strategy.
     *
     * @implNote WAWebMediaHostsRouteSelection.routeSelection: the return
     * value of the function, which exposes {@code selectedHost} and
     * {@code fallback} on the route object.
     * @param selectedHost the selected host, or empty if none matched
     * @param fallbackHost the fallback host, or empty if none exists
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHostsRouteSelection")
    record RouteSelectionResult(
            Optional<MediaHost> selectedHost,
            Optional<MediaHost> fallbackHost
    ) {
    }

    /**
     * Picks the best CDN host for the given operation and media type from
     * a parsed host list.
     *
     * <p>For download operations the method tries bucket-based routing
     * first when vcache aggregation is enabled:
     * <ol>
     *   <li>When {@code encFileHash} is {@code null} the bucket defaults
     *       to {@code 0}.</li>
     *   <li>Otherwise the bucket is computed as
     *       {@code base64Modulo(encFileHash, maxBuckets) + 100}.</li>
     *   <li>The host whose {@code downloadBuckets} list claims the
     *       computed bucket is preferred; when no host claims the bucket
     *       the {@code 0}-bucket host is tried.</li>
     * </ol>
     * When bucket-based selection finds nothing (or for upload
     * operations) the method falls back to a linear scan for the first
     * host that supports the requested media type.
     *
     * <p>Regardless of the operation, the fallback host is always the
     * first host in the list whose type is {@link Fallback}.
     *
     * @implNote WAWebMediaHostsRouteSelection.routeSelection combined with
     * its internal bucket map helper ({@code u}) and the primary/fallback
     * selection loop.
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
    @WhatsAppWebExport(moduleName = "WAWebMediaHostsRouteSelection", exports = "routeSelection",
            adaptation = WhatsAppAdaptation.DIRECT)
    static RouteSelectionResult routeSelection(
            Operation operation,
            MediaPath mediaType,
            List<? extends MediaHost> hosts,
            String encFileHash,
            Integer maxBuckets,
            boolean vcacheAggregationEnabled
    ) {
        // WAWebMediaHostsRouteSelection.routeSelection
        // Empty host list yields an empty RouteSelectionResult, matching
        // the null branch in the WA Web source

        if (hosts.isEmpty()) {
            return new RouteSelectionResult(Optional.empty(), Optional.empty());
        }

        MediaHost selected = null;
        if (operation == Operation.DOWNLOAD) {
            // WAWebMediaHostsRouteSelection.routeSelection
            // Computes the bucket for deterministic host selection:
            // null encFileHash uses bucket 0; otherwise vcache aggregation
            // combined with maxBuckets yields base64Modulo(n, i) + 100

            Integer bucket;
            if (encFileHash == null) {
                bucket = 0;
            } else if (vcacheAggregationEnabled && maxBuckets != null) {
                bucket = base64Modulo(encFileHash, maxBuckets) + 100;
            } else {
                bucket = null;
            }

            // WAWebMediaHostsRouteSelection.routeSelection
            // Builds the bucket -> host map once and then consults it for
            // the computed bucket; the bucket-0 host is used as a default

            var bucketMap = buildBucketMap(hosts);
            var bucketHost = bucket != null ? bucketMap.get(bucket) : null;
            var defaultHost = bucketMap.get(0);

            if (bucketHost != null && supportsDownloadMediaType(bucketHost, mediaType)) {
                selected = bucketHost;
            } else if (defaultHost != null && supportsDownloadMediaType(defaultHost, mediaType)) {
                selected = defaultHost;
            }
        }

        // WAWebMediaHostsRouteSelection.routeSelection
        // Finds the first fallback-class host independently of the
        // selected host choice

        MediaHost fallback = null;
        for (var host : hosts) {
            if (host instanceof Fallback) {
                fallback = host;
                break;
            }
        }

        // WAWebMediaHostsRouteSelection.routeSelection
        // Falls back to a linear scan when bucket routing did not find a
        // supporting host (or the operation is upload)

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
     * Returns whether the given host can serve a download for the
     * specified media type, after collapsing the media type through the
     * WA Web download-type normalisation map.
     *
     * @implNote WAWebMediaHost.MediaHost.supportsDownloadMediaType.
     * @param host      the host to check
     * @param mediaType the media type to check
     * @return {@code true} if the normalized type is in the host's download set
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean supportsDownloadMediaType(MediaHost host, MediaPath mediaType) {
        return host.download().contains(normalizeDownloadMediaType(mediaType));
    }

    /**
     * Returns whether the given host can accept an upload for the
     * specified media type, after collapsing the media type through the
     * WA Web upload-type normalisation map.
     *
     * @implNote WAWebMediaHost.MediaHost.supportsUploadMediaType.
     * @param host      the host to check
     * @param mediaType the media type to check
     * @return {@code true} if the normalized type is in the host's upload set
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean supportsUploadMediaType(MediaHost host, MediaPath mediaType) {
        return host.upload().contains(normalizeUploadMediaType(mediaType));
    }

    /**
     * Builds a map from download bucket number to the host that claims
     * that bucket.
     *
     * <p>Iterates every host's {@code downloadBuckets} list; if multiple
     * hosts claim the same bucket the last writer wins, mirroring the
     * plain assignment semantics of the JS object in the source module.
     *
     * @implNote WAWebMediaHostsRouteSelection local function {@code u}.
     * @param hosts the list of hosts to index
     * @return a map from bucket number to host
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHostsRouteSelection", exports = "routeSelection",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Map<Integer, MediaHost> buildBucketMap(List<? extends MediaHost> hosts) {
        // WAWebMediaHostsRouteSelection.routeSelection
        // Iterates every host's bucket list and maps each bucket number to
        // the owning host; later assignments overwrite earlier ones

        var map = new HashMap<Integer, MediaHost>();
        for (var host : hosts) {
            for (var bucket : host.downloadBuckets()) {
                map.put(bucket, host);
            }
        }
        return map;
    }

    /**
     * Computes the modulo of a base64-encoded string treated as a
     * big-endian byte stream.
     *
     * <p>Decodes the input, then processes each byte as two 4-bit nibbles,
     * accumulating the remainder via
     * {@code ((remainder << 4) + nibble) % divisor}. Used as the
     * deterministic bucket-selection hash for download route selection.
     *
     * @implNote WABase64Modulo.default.
     * @param base64 the base64-encoded string
     * @param divisor the modulo divisor (maxBuckets)
     * @return the modulo result
     */
    @WhatsAppWebExport(moduleName = "WABase64Modulo", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static int base64Modulo(String base64, int divisor) {
        // WABase64Modulo.default
        // Decodes the base64 string into raw bytes and processes each byte
        // as two 4-bit nibbles to mirror the JS BigInt-free implementation

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
     * Returns the hostname of this CDN host.
     *
     * @implNote WAWebMediaHost.MediaHost constructor: {@code this.hostname = t.hostname}.
     * @return the hostname, never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    String hostname();

    /**
     * Returns the list of IP addresses the server advertises for this
     * host.
     *
     * @implNote WAWebMediaHost.MediaHost constructor: {@code this.ips = t.ips || []}.
     * @return an unmodifiable list of IP address strings, never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    List<String> ips();

    /**
     * Returns the set of media types this host accepts for downloads.
     *
     * <p>When the server response omits the download rules the set
     * defaults to all known server media types (minus the handful of
     * non-routable entries filtered by {@code compactMap}).
     *
     * @implNote WAWebMediaHost.MediaHost constructor: {@code this.$1}
     * produced by the {@code c(parseRules)} helper, defaulting to
     * {@code MEDIA_TYPE_VALUES}.
     * @return an unmodifiable set of supported download media paths
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    Set<MediaPath> download();

    /**
     * Returns the set of media types this host accepts for uploads.
     *
     * <p>When the server response omits the upload rules the set
     * defaults to all known server media types (minus the handful of
     * non-routable entries filtered by {@code compactMap}).
     *
     * @implNote WAWebMediaHost.MediaHost constructor: {@code this.$2}
     * produced by the {@code c(parseRules)} helper, defaulting to
     * {@code MEDIA_TYPE_VALUES}.
     * @return an unmodifiable set of supported upload media paths
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    Set<MediaPath> upload();

    /**
     * Returns the download bucket identifiers owned by this host.
     *
     * <p>Buckets participate in the deterministic download host
     * selection: the file hash is reduced modulo {@code maxBuckets} to
     * pick the bucket, then the owning host is looked up.
     *
     * @implNote WAWebMediaHost.MediaHost constructor:
     * {@code this.downloadBuckets = r} from the {@code c(t.rules)} helper.
     * @return an unmodifiable list of bucket numbers, never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    List<Integer> downloadBuckets();

    /**
     * Returns the nested fallback hostname declared by this host, if any.
     *
     * <p>Only {@link Primary} hosts can advertise a nested fallback;
     * {@link Fallback} hosts always return an empty optional. The nested
     * fallback is rotated in by {@code selectHost} when the selected host
     * itself fails.
     *
     * @implNote WAWebMediaHost.MediaHost constructor:
     * {@code this.fallback = t.fallback != null ? new MediaHost(...) : null}.
     * @return an {@link Optional} containing the fallback hostname, or empty
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    Optional<String> fallbackHostname();

    /**
     * Returns whether this host accepts a download of the media produced
     * by the given provider.
     *
     * <p>The provider's media path is first normalised to collapse PTV
     * variants onto video and product onto image before the host's
     * supported-type set is consulted.
     *
     * @implNote WAWebMediaHost.MediaHost.supportsDownloadMediaType combined
     * with {@code WAWebMediaHost.d} for download-type normalisation.
     * @param provider the media provider whose type to check
     * @return {@code true} if this host supports downloading that media type
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    boolean canDownload(MediaProvider provider);

    /**
     * Returns whether this host accepts an upload of the media produced
     * by the given provider.
     *
     * <p>The provider's media path is first normalised to collapse PTV
     * onto video and product-catalog-image onto product before the
     * host's supported-type set is consulted.
     *
     * @implNote WAWebMediaHost.MediaHost.supportsUploadMediaType combined
     * with {@code WAWebMediaHost.m} for upload-type normalisation.
     * @param provider the media provider whose type to check
     * @return {@code true} if this host supports uploading that media type
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    boolean canUpload(MediaProvider provider);

    /**
     * Normalises a media path before consulting a host's download support
     * set. Mirrors the WA Web helper {@code d(t)}:
     * <ul>
     *   <li>{@link MediaPath#PTV} and {@link MediaPath#NEWSLETTER_PTV}
     *       collapse to {@link MediaPath#VIDEO}.</li>
     *   <li>{@link MediaPath#PRODUCT} collapses to
     *       {@link MediaPath#IMAGE}.</li>
     *   <li>Every other type is returned unchanged.</li>
     * </ul>
     *
     * @implNote WAWebMediaHost local helper {@code d}.
     * @param path the media path to normalize
     * @return the normalized media path for download checking
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MediaPath normalizeDownloadMediaType(MediaPath path) {
        // WAWebMediaHost.MediaHost
        // Collapses PTV / newsletter-PTV to VIDEO and PRODUCT to IMAGE
        // before consulting the download-type set

        return switch (path) {
            case PTV, NEWSLETTER_PTV -> MediaPath.VIDEO;
            case PRODUCT -> MediaPath.IMAGE;
            default -> path;
        };
    }

    /**
     * Normalises a media path before consulting a host's upload support
     * set. Mirrors the WA Web helper {@code m(t)}:
     * <ul>
     *   <li>{@link MediaPath#PTV} collapses to
     *       {@link MediaPath#VIDEO}.</li>
     *   <li>{@link MediaPath#PRODUCT_CATALOG_IMAGE} collapses to
     *       {@link MediaPath#PRODUCT}.</li>
     *   <li>Every other type is returned unchanged.</li>
     * </ul>
     *
     * @implNote WAWebMediaHost local helper {@code m}.
     * @param path the media path to normalize
     * @return the normalized media path for upload checking
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MediaPath normalizeUploadMediaType(MediaPath path) {
        // WAWebMediaHost.MediaHost
        // Collapses PTV to VIDEO and PRODUCT_CATALOG_IMAGE to PRODUCT
        // before consulting the upload-type set

        return switch (path) {
            case PTV -> MediaPath.VIDEO;
            case PRODUCT_CATALOG_IMAGE -> MediaPath.PRODUCT;
            default -> path;
        };
    }

    /**
     * A primary CDN host.
     *
     * <p>Primary hosts have type {@code "primary"} and may advertise a
     * nested fallback hostname together with its own IP list. The route
     * selection algorithm prefers primary hosts over fallback-class hosts
     * and consults the download bucket assignments to pick one
     * deterministically.
     *
     * @implNote WAWebMediaHost.MediaHost for a {@code HOST_TYPE.PRIMARY}
     * instance.
     * @param hostname         the hostname of this host
     * @param ips              the list of IP addresses advertised by the server
     * @param fallbackHostname the fallback hostname, or empty if no fallback
     * @param fallbackIps      the list of fallback IP addresses
     * @param downloadBuckets  the download bucket assignments for this host
     * @param download         the set of supported download media types
     * @param upload           the set of supported upload media types
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHost")
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
         * Returns whether this primary host accepts a download of the
         * media produced by the given provider, after applying the
         * download-type normalisation.
         *
         * @implNote WAWebMediaHost.MediaHost.supportsDownloadMediaType.
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalized type is in the download set
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public boolean canDownload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            // WAWebMediaHost.MediaHost
            // supportsDownloadMediaType(t) = this.$1.has(d(t))

            return download.contains(normalizeDownloadMediaType(provider.mediaPath()));
        }

        /**
         * Returns whether this primary host accepts an upload of the
         * media produced by the given provider, after applying the
         * upload-type normalisation.
         *
         * @implNote WAWebMediaHost.MediaHost.supportsUploadMediaType.
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalized type is in the upload set
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public boolean canUpload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            // WAWebMediaHost.MediaHost
            // supportsUploadMediaType(t) = this.$2.has(m(t))

            return upload.contains(normalizeUploadMediaType(provider.mediaPath()));
        }
    }

    /**
     * A fallback-class CDN host.
     *
     * <p>Fallback hosts have type {@code "fallback"} and never advertise
     * a nested fallback of their own. They are used by the retry loop as
     * an alternate endpoint after the primary host has exhausted its
     * attempts.
     *
     * @implNote WAWebMediaHost.MediaHost for a {@code HOST_TYPE.FALLBACK}
     * instance.
     * @param hostname         the hostname of this host
     * @param ips              the list of IP addresses advertised by the server
     * @param downloadBuckets  the download bucket assignments for this host
     * @param download         the set of supported download media types
     * @param upload           the set of supported upload media types
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHost")
    record Fallback(
            String hostname,
            List<String> ips,
            List<Integer> downloadBuckets,
            Set<MediaPath> download,
            Set<MediaPath> upload
    ) implements MediaHost {

        /**
         * Returns whether this fallback host accepts a download of the
         * media produced by the given provider, after applying the
         * download-type normalisation.
         *
         * @implNote WAWebMediaHost.MediaHost.supportsDownloadMediaType.
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalized type is in the download set
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public boolean canDownload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            // WAWebMediaHost.MediaHost
            // supportsDownloadMediaType(t) = this.$1.has(d(t))

            return download.contains(normalizeDownloadMediaType(provider.mediaPath()));
        }

        /**
         * Returns whether this fallback host accepts an upload of the
         * media produced by the given provider, after applying the
         * upload-type normalisation.
         *
         * @implNote WAWebMediaHost.MediaHost.supportsUploadMediaType.
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalized type is in the upload set
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public boolean canUpload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            // WAWebMediaHost.MediaHost
            // supportsUploadMediaType(t) = this.$2.has(m(t))

            return upload.contains(normalizeUploadMediaType(provider.mediaPath()));
        }

        /**
         * Always returns an empty optional because fallback-class hosts
         * never advertise a nested fallback hostname of their own.
         *
         * @implNote WAWebMediaHost.MediaHost constructor: fallback hosts
         * are built with {@code fallback: void 0}.
         * @return an empty {@link Optional}
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public Optional<String> fallbackHostname() {
            return Optional.empty();
        }
    }
}
