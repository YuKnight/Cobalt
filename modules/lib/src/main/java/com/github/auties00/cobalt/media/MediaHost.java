package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.media.MediaPath;
import com.github.auties00.cobalt.model.media.MediaProvider;

import java.util.*;

/**
 * Single CDN host entry returned by WhatsApp's {@code media_conn} query.
 *
 * <p>Every host advertises a hostname, the IP addresses the server
 * exposes for it, the media types it accepts for downloads and uploads,
 * and the deterministic download buckets it owns. Primary hosts may
 * additionally carry a nested fallback hostname with its own IP list,
 * which the retry loop rotates to when the primary endpoint fails.
 *
 * <p>The sealed type splits these two roles:
 * <ul>
 *   <li>{@link Primary}: hosts of type {@code "primary"}, eligible for
 *       bucket-based selection and possibly carrying a nested
 *       fallback.</li>
 *   <li>{@link Fallback}: hosts of type {@code "fallback"}, used as
 *       alternate endpoints after the primary path is exhausted.</li>
 * </ul>
 *
 * <p>Both variants normalise the requested media type (collapsing PTV
 * and product variants) before matching it against their accepted-type
 * sets, mirroring the helper functions WhatsApp Web uses for the same
 * checks.
 */
@WhatsAppWebModule(moduleName = "WAWebMediaHost")
@WhatsAppWebModule(moduleName = "WAWebMediaHostsRouteSelection")
@WhatsAppWebModule(moduleName = "WAWebMediaHostsMaybeSwitchHost")
@WhatsAppWebModule(moduleName = "WABase64Modulo")
public sealed interface MediaHost {

    /**
     * The remaining-bytes threshold below which a mid-transfer host
     * switch is never attempted on a long-running document upload or
     * download.
     *
     * <p>When fewer than 50 MiB remain the cost of restarting on a new
     * host outweighs any topology benefit, so {@link #maybeSwitchHost}
     * keeps the current host. The constant is kept here for parity even
     * though Cobalt does not yet re-query {@code media_conn} during a
     * transfer.
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHostsMaybeSwitchHost", exports = "THRESHOLD",
            adaptation = WhatsAppAdaptation.DIRECT)
    int SWITCH_HOST_THRESHOLD = 52428800;

    /**
     * The outcome of a {@link #maybeSwitchHost} decision: whether the
     * current host should be replaced and, if so, which host should
     * take its place.
     *
     * <p>When {@link #changed} is {@code false} the {@link #host} field
     * echoes the current host so callers can assign the result without
     * a null check.
     *
     * @param changed whether the host should be switched
     * @param host    the host to use after the decision, never {@code null}
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHostsMaybeSwitchHost")
    record SwitchHostResult(boolean changed, MediaHost host) {
    }

    /**
     * Decides whether a long-running document upload or download
     * currently using {@code current} should switch to a different host
     * because a periodic {@code media_conn} re-query returned a new
     * route list.
     *
     * <p>The rules:
     * <ol>
     *   <li>When fewer than {@link #SWITCH_HOST_THRESHOLD} bytes
     *       remain, keep the current host.</li>
     *   <li>When the current host is a {@link Primary} and the new
     *       route's selected host differs from it, switch.</li>
     *   <li>When the current host is a {@link Fallback} and the
     *       previous route's selected host differs from the new one,
     *       switch.</li>
     *   <li>When the current host is a {@link Fallback} and either the
     *       previous fallback or the previous selected host's nested
     *       fallback was replaced, switch to the new selected host.</li>
     *   <li>Otherwise keep the current host.</li>
     * </ol>
     *
     * @param current        the host currently in use
     * @param previousRoute  the route in force before the most recent
     *                       re-query
     * @param newRoute       the route returned by the most recent
     *                       re-query
     * @param bytesRemaining the number of bytes left to transfer
     * @return the switch decision; {@code host} echoes {@code current}
     *         when {@code changed} is {@code false}
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHostsMaybeSwitchHost", exports = "maybeSwitchHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    static SwitchHostResult maybeSwitchHost(
            MediaHost current,
            RouteSelectionResult previousRoute,
            RouteSelectionResult newRoute,
            long bytesRemaining
    ) {
        var previousFallback = previousRoute.fallbackHost().orElse(null);
        var previousSelected = previousRoute.selectedHost().orElse(null);
        var newFallback = newRoute.fallbackHost().orElse(null);
        var newSelected = newRoute.selectedHost().orElse(null);

        if (bytesRemaining < SWITCH_HOST_THRESHOLD) {
            return new SwitchHostResult(false, current);
        }

        if (current instanceof Primary && !equalsByHostname(current, newSelected)) {
            return new SwitchHostResult(true, newSelected);
        }

        if (current instanceof Fallback && !equalsByHostname(previousSelected, newSelected)) {
            return new SwitchHostResult(true, newSelected);
        }

        if (current instanceof Fallback
                && (isNestedFallbackChanged(current, previousFallback, newFallback)
                        || isNestedFallbackChanged(current, nestedFallbackHost(previousSelected), nestedFallbackHost(newSelected)))) {
            return new SwitchHostResult(true, newSelected);
        }

        return new SwitchHostResult(false, current);
    }

    /**
     * Compares two hosts by hostname only, matching WhatsApp Web's
     * {@code MediaHost.prototype.equals} semantics.
     *
     * <p>The bundle collapses host equality to a hostname compare so
     * that re-parsed {@code media_conn} responses with cosmetically
     * different IP orderings or rule encodings still register as the
     * same host.
     *
     * @param a the first host, may be {@code null}
     * @param b the second host, may be {@code null}
     * @return {@code true} when both hosts are non-{@code null} and
     *         share a hostname, otherwise {@code false}
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean equalsByHostname(MediaHost a, MediaHost b) {
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.hostname(), b.hostname());
    }

    /**
     * Tests whether {@code current} still occupies a slot whose host
     * has been replaced by the server.
     *
     * <p>The current host must equal the previous slot host, both slot
     * hosts must be present, and the previous and new slot hosts must
     * differ. Only when all four conditions hold does the slot change
     * warrant a host switch.
     *
     * @param current          the host currently in use
     * @param previousSlotHost the host that occupied the slot before
     *                         the re-query
     * @param newSlotHost      the host that occupies the slot after the
     *                         re-query
     * @return {@code true} if the slot's host changed under the current
     *         host
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHostsMaybeSwitchHost", exports = "maybeSwitchHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isNestedFallbackChanged(MediaHost current, MediaHost previousSlotHost, MediaHost newSlotHost) {
        return equalsByHostname(current, previousSlotHost)
                && newSlotHost != null
                && previousSlotHost != null
                && !equalsByHostname(previousSlotHost, newSlotHost);
    }

    /**
     * Looks up the nested fallback host of a primary host.
     *
     * <p>Cobalt only retains the nested fallback hostname string on
     * {@link Primary}, not a {@code MediaHost} reference, so this
     * helper always returns {@code null}. That collapses the
     * corresponding branch of {@link #maybeSwitchHost} to a no-op while
     * leaving the outer slot-change branch fully functional, which
     * covers the common case.
     *
     * @param primary the primary host to inspect, or {@code null}
     * @return always {@code null} in Cobalt
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHostsMaybeSwitchHost", exports = "maybeSwitchHost",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static MediaHost nestedFallbackHost(MediaHost primary) {
        return null;
    }

    /**
     * Identifies the two operations that may be performed against a
     * CDN host.
     *
     * <p>{@link #UPLOAD} matches against the host's upload media-type
     * set; {@link #DOWNLOAD} matches against the download set and
     * additionally participates in the deterministic bucket-based host
     * selection.
     */
    @WhatsAppWebModule(moduleName = "WAWebMmsOperationsConst")
    @WhatsAppWebModule(moduleName = "WAWebMediaHostsRouteSelection")
    enum Operation {
        /**
         * The upload operation. Route selection picks the first host
         * whose upload media-type set contains the requested type.
         */
        @WhatsAppWebExport(moduleName = "WAWebMmsOperationsConst", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @WhatsAppWebExport(moduleName = "WAWebMediaHostsRouteSelection", exports = "OPERATIONS",
                adaptation = WhatsAppAdaptation.DIRECT)
        UPLOAD,

        /**
         * The download operation. Route selection applies bucket-based
         * matching when vcache aggregation is enabled, then falls back
         * to a linear scan against the download media-type set.
         */
        @WhatsAppWebExport(moduleName = "WAWebMmsOperationsConst", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @WhatsAppWebExport(moduleName = "WAWebMediaHostsRouteSelection", exports = "OPERATIONS",
                adaptation = WhatsAppAdaptation.DIRECT)
        DOWNLOAD
    }

    /**
     * The outcome of a route selection pass.
     *
     * <p>Carries the best-matching host for the requested operation,
     * the first fallback-class host from the connection's host list,
     * and the deterministic bucket the selected host was matched
     * against. {@link #selectedBucket} is present only when the
     * selected host came from the bucket map (operation
     * {@link Operation#DOWNLOAD} with a hit on the explicit bucket
     * entry or the bucket-zero default); for upload operations or for
     * download selections that fell through to the linear scan it is
     * empty.
     *
     * @param selectedHost   the selected host, or empty when none matched
     * @param fallbackHost   the fallback host, or empty when none exists
     * @param selectedBucket the deterministic bucket of the selected
     *                       host, or empty when no bucket applies
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHostsRouteSelection")
    record RouteSelectionResult(
            Optional<MediaHost> selectedHost,
            Optional<MediaHost> fallbackHost,
            Optional<Integer> selectedBucket
    ) {
    }

    /**
     * Picks the best CDN host for the given operation and media type
     * from a parsed host list.
     *
     * <p>For downloads the algorithm tries deterministic bucket-based
     * routing first when vcache aggregation is enabled:
     * <ol>
     *   <li>When {@code encFileHash} is {@code null} the bucket
     *       defaults to zero.</li>
     *   <li>Otherwise the bucket is computed as
     *       {@code base64Modulo(encFileHash, maxBuckets) + 100}.</li>
     *   <li>The host whose {@code downloadBuckets} list claims the
     *       computed bucket is preferred; when no host claims it the
     *       zero-bucket host is tried.</li>
     * </ol>
     * If bucket selection finds nothing, or if the operation is
     * {@link Operation#UPLOAD}, the method falls back to a linear scan
     * for the first host that supports the requested media type. The
     * fallback host is always the first host in the list whose type is
     * {@link Fallback}, regardless of the operation.
     *
     * @param operation                the operation to perform
     * @param mediaType                the media path to match
     * @param hosts                    the list of available hosts
     * @param encFileHash              the base64-encoded encrypted
     *                                 file hash, or {@code null}
     * @param maxBuckets               the maximum number of buckets,
     *                                 or {@code null} when not available
     * @param vcacheAggregationEnabled whether the
     *                                 {@code mms_vcache_aggregation_enabled}
     *                                 AB prop is enabled
     * @return the route selection result
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
        if (hosts.isEmpty()) {
            return new RouteSelectionResult(Optional.empty(), Optional.empty(), Optional.empty());
        }

        MediaHost selected = null;
        Integer selectedBucket = null;
        if (operation == Operation.DOWNLOAD) {
            Integer bucket;
            if (encFileHash == null) {
                bucket = 0;
            } else if (vcacheAggregationEnabled && maxBuckets != null) {
                bucket = base64Modulo(encFileHash, maxBuckets) + 100;
            } else {
                bucket = null;
            }

            var bucketMap = buildBucketMap(hosts);
            var bucketHost = bucket != null ? bucketMap.get(bucket) : null;
            var defaultHost = bucketMap.get(0);

            if (bucketHost != null && supportsDownloadMediaType(bucketHost, mediaType)) {
                selected = bucketHost;
            } else if (defaultHost != null && supportsDownloadMediaType(defaultHost, mediaType)) {
                selected = defaultHost;
            }

            // The bucket attaches to the selected host only when the
            // bucket branch produced one; the linear-scan fall-through
            // below leaves selectedBucket null on purpose.
            if (selected != null) {
                selectedBucket = bucket;
            }
        }

        MediaHost fallback = null;
        for (var host : hosts) {
            if (host instanceof Fallback) {
                fallback = host;
                break;
            }
        }

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
                Optional.ofNullable(fallback),
                Optional.ofNullable(selectedBucket)
        );
    }

    /**
     * Returns whether the host can serve a download for the specified
     * media type after collapsing the type through the download
     * normalisation map.
     *
     * @param host      the host to check
     * @param mediaType the media type to check
     * @return {@code true} if the normalised type is in the host's
     *         download set
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean supportsDownloadMediaType(MediaHost host, MediaPath mediaType) {
        return host.download().contains(normalizeDownloadMediaType(mediaType));
    }

    /**
     * Returns whether the host can accept an upload for the specified
     * media type after collapsing the type through the upload
     * normalisation map.
     *
     * @param host      the host to check
     * @param mediaType the media type to check
     * @return {@code true} if the normalised type is in the host's
     *         upload set
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean supportsUploadMediaType(MediaHost host, MediaPath mediaType) {
        return host.upload().contains(normalizeUploadMediaType(mediaType));
    }

    /**
     * Builds a bucket-to-host map from the host list.
     *
     * <p>If multiple hosts claim the same bucket the last writer wins,
     * mirroring the plain object-assignment semantics of the JS source.
     *
     * @param hosts the list of hosts to index
     * @return a map from bucket number to host
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHostsRouteSelection", exports = "routeSelection",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Map<Integer, MediaHost> buildBucketMap(List<? extends MediaHost> hosts) {
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
     * <p>Decodes the input, then folds each byte as two 4-bit nibbles
     * via {@code remainder = ((remainder << 4) + nibble) % divisor}.
     * The output is the deterministic bucket identifier for download
     * route selection.
     *
     * @param base64  the base64-encoded string
     * @param divisor the modulo divisor (typically {@code maxBuckets})
     * @return the modulo result
     */
    @WhatsAppWebExport(moduleName = "WABase64Modulo", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static int base64Modulo(String base64, int divisor) {
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
     * @return the hostname
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    String hostname();

    /**
     * Returns the optional {@code class} attribute advertised by the
     * server for this host.
     *
     * <p>The {@code class} attribute groups hosts by deployment tier
     * (for example {@code mms} versus {@code mmg}) and is propagated
     * unchanged to downstream analytics and request tagging.
     *
     * @return an {@link Optional} holding the host class, or empty if
     *         the attribute is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    Optional<String> hostClass();

    /**
     * Returns the IP addresses the server advertises for this host.
     *
     * @return an unmodifiable list of IP address strings
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    List<String> ips();

    /**
     * Returns the set of media types this host accepts for downloads.
     *
     * <p>Defaults to the full set of routable server media types when
     * the {@code media_conn} response omits the explicit rules.
     *
     * @return an unmodifiable set of supported download media paths
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    Set<MediaPath> download();

    /**
     * Returns the set of media types this host accepts for uploads.
     *
     * <p>Defaults to the full set of routable server media types when
     * the {@code media_conn} response omits the explicit rules.
     *
     * @return an unmodifiable set of supported upload media paths
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    Set<MediaPath> upload();

    /**
     * Returns the deterministic download bucket identifiers owned by
     * this host.
     *
     * <p>Buckets participate in download host selection: the file hash
     * is reduced modulo {@code maxBuckets} to pick the bucket, then
     * the owning host is looked up in the bucket map.
     *
     * @return an unmodifiable list of bucket numbers
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    List<Integer> downloadBuckets();

    /**
     * Returns the nested fallback hostname declared by this host, if
     * any.
     *
     * <p>Only {@link Primary} hosts can advertise a nested fallback;
     * {@link Fallback} hosts always return an empty optional. The
     * nested fallback is tried by the retry loop when the selected
     * host itself fails.
     *
     * @return an {@link Optional} holding the fallback hostname
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    Optional<String> fallbackHostname();

    /**
     * Returns whether this host accepts a download of the media
     * produced by the given provider.
     *
     * <p>The provider's media path is normalised (PTV variants
     * collapse onto video, product onto image) before consulting the
     * host's accepted-type set.
     *
     * @param provider the media provider whose type to check
     * @return {@code true} if this host supports the download
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    boolean canDownload(MediaProvider provider);

    /**
     * Returns whether this host accepts an upload of the media
     * produced by the given provider.
     *
     * <p>The provider's media path is normalised (PTV collapses onto
     * video, product-catalog-image onto product) before consulting the
     * host's accepted-type set.
     *
     * @param provider the media provider whose type to check
     * @return {@code true} if this host supports the upload
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    boolean canUpload(MediaProvider provider);

    /**
     * Normalises a media path for the download accepted-type check.
     *
     * <p>{@link MediaPath#PTV} and {@link MediaPath#NEWSLETTER_PTV}
     * collapse to {@link MediaPath#VIDEO}, {@link MediaPath#PRODUCT}
     * collapses to {@link MediaPath#IMAGE}, and every other type is
     * returned unchanged.
     *
     * @param path the media path to normalise
     * @return the normalised media path
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MediaPath normalizeDownloadMediaType(MediaPath path) {
        return switch (path) {
            case PTV, NEWSLETTER_PTV -> MediaPath.VIDEO;
            case PRODUCT -> MediaPath.IMAGE;
            default -> path;
        };
    }

    /**
     * Normalises a media path for the upload accepted-type check.
     *
     * <p>{@link MediaPath#PTV} collapses to {@link MediaPath#VIDEO},
     * {@link MediaPath#PRODUCT_CATALOG_IMAGE} collapses to
     * {@link MediaPath#PRODUCT}, and every other type is returned
     * unchanged.
     *
     * @param path the media path to normalise
     * @return the normalised media path
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MediaPath normalizeUploadMediaType(MediaPath path) {
        return switch (path) {
            case PTV -> MediaPath.VIDEO;
            case PRODUCT_CATALOG_IMAGE -> MediaPath.PRODUCT;
            default -> path;
        };
    }

    /**
     * A primary CDN host.
     *
     * <p>Primary hosts are the preferred endpoints picked by route
     * selection. They may advertise a nested fallback hostname with
     * its own IP list, which the retry loop rotates to before falling
     * back to a fallback-class host.
     *
     * @param hostname         the hostname of this host
     * @param hostClass        the optional {@code class} attribute
     *                         advertised by the server
     * @param ips              the IP addresses advertised by the server
     * @param fallbackHostname the nested fallback hostname, or empty
     * @param fallbackClass    the optional {@code fallback_class}
     *                         attribute advertised alongside the
     *                         nested fallback
     * @param fallbackIps      the nested fallback IP addresses
     * @param downloadBuckets  the deterministic download buckets owned
     *                         by this host
     * @param download         the supported download media types
     * @param upload           the supported upload media types
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHost")
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "HOST_TYPE",
            adaptation = WhatsAppAdaptation.ADAPTED)
    record Primary(
            String hostname,
            Optional<String> hostClass,
            List<String> ips,
            Optional<String> fallbackHostname,
            Optional<String> fallbackClass,
            List<String> fallbackIps,
            List<Integer> downloadBuckets,
            Set<MediaPath> download,
            Set<MediaPath> upload
    ) implements MediaHost {

        /**
         * Returns whether this primary host accepts a download of the
         * media produced by the given provider.
         *
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalised type is in the
         *         download set
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public boolean canDownload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            return download.contains(normalizeDownloadMediaType(provider.mediaPath()));
        }

        /**
         * Returns whether this primary host accepts an upload of the
         * media produced by the given provider.
         *
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalised type is in the upload
         *         set
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public boolean canUpload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            return upload.contains(normalizeUploadMediaType(provider.mediaPath()));
        }
    }

    /**
     * A fallback-class CDN host.
     *
     * <p>Fallback hosts are used as alternate endpoints when the
     * primary host has exhausted its retry budget. They never carry a
     * nested fallback of their own.
     *
     * @param hostname        the hostname of this host
     * @param hostClass       the optional {@code class} attribute
     *                        advertised by the server
     * @param ips             the IP addresses advertised by the server
     * @param downloadBuckets the deterministic download buckets owned
     *                        by this host
     * @param download        the supported download media types
     * @param upload          the supported upload media types
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHost")
    @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "HOST_TYPE",
            adaptation = WhatsAppAdaptation.ADAPTED)
    record Fallback(
            String hostname,
            Optional<String> hostClass,
            List<String> ips,
            List<Integer> downloadBuckets,
            Set<MediaPath> download,
            Set<MediaPath> upload
    ) implements MediaHost {

        /**
         * Returns whether this fallback host accepts a download of the
         * media produced by the given provider.
         *
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalised type is in the
         *         download set
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public boolean canDownload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            return download.contains(normalizeDownloadMediaType(provider.mediaPath()));
        }

        /**
         * Returns whether this fallback host accepts an upload of the
         * media produced by the given provider.
         *
         * @param provider the media provider whose type to check
         * @return {@code true} if the normalised type is in the upload
         *         set
         */
        @WhatsAppWebExport(moduleName = "WAWebMediaHost", exports = "MediaHost",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public boolean canUpload(MediaProvider provider) {
            Objects.requireNonNull(provider, "provider cannot be null");
            return upload.contains(normalizeUploadMediaType(provider.mediaPath()));
        }

        /**
         * Always returns an empty optional because fallback-class
         * hosts never advertise a nested fallback hostname of their
         * own.
         *
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
