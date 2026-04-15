package com.github.auties00.cobalt.media;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.exception.WhatsAppMediaException;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.media.ExternalBlobReference;
import com.github.auties00.cobalt.model.media.MediaPath;
import com.github.auties00.cobalt.model.media.MediaProvider;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import com.github.auties00.cobalt.media.MediaHost.Operation;
import com.github.auties00.cobalt.media.MediaHost.RouteSelectionResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/**
 * Represents a parsed media connection response from the WhatsApp server.
 * A media connection contains the authentication token, TTL values, host
 * entries, and retry limits needed to upload and download media via the
 * WhatsApp CDN.
 *
 * <p>This class corresponds to the result of the {@code WAMediaConnParser}
 * parser applied to the {@code media_conn} IQ response node, combined with
 * the mapping performed by {@code WAWebQueryMediaConnsJob.mapParsedMediaConn}.
 *
 * @implNote WAMediaConnParser.mediaConnParser,
 *           WAWebQueryMediaConnsJob.mapParsedMediaConn,
 *           WAWebQueryMediaConnsJob.queryMediaConn,
 *           WAWebMediaHosts (singleton lifecycle: TTL, refresh, expiry)
 */
public final class MediaConnection {
    /**
     * The authentication token extracted from the {@code auth} attribute of
     * the {@code media_conn} node. Used to authenticate CDN upload requests.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code authToken: r.attrString("auth")}
     */
    private final String auth;

    /**
     * The routes time-to-live in seconds, extracted from the {@code ttl}
     * attribute of the {@code media_conn} node. In WA Web, this is computed
     * as {@code routesExpiryTs - unixTime()} where {@code routesExpiryTs}
     * is {@code futureUnixTime(ttl_seconds)}.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code routesExpiryTs: r.attrFutureTime("ttl")},
     *           WAWebQueryMediaConnsJob.queryMediaConn -- {@code ttl: routesExpiryTs - unixTime()}
     */
    private final int ttl;

    /**
     * The authentication token time-to-live in seconds, extracted from the
     * {@code auth_ttl} attribute of the {@code media_conn} node. In WA Web,
     * this is computed as {@code authTokenExpiryTs - unixTime()} where
     * {@code authTokenExpiryTs} is {@code futureUnixTime(auth_ttl_seconds)}.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code authTokenExpiryTs: r.attrFutureTime("auth_ttl")},
     *           WAWebQueryMediaConnsJob.queryMediaConn -- {@code authTTL: authTokenExpiryTs - unixTime()}
     */
    private final int authTtl;

    /**
     * The maximum number of CDN buckets, extracted from the
     * {@code max_buckets} attribute of the {@code media_conn} node. Used
     * by the route selection algorithm for deterministic bucket assignment.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code maxBuckets: r.attrInt("max_buckets")},
     *           WAWebQueryMediaConnsJob.mapParsedMediaConn -- {@code maxBuckets: e.maxBuckets}
     */
    private final int maxBuckets;

    /**
     * The maximum number of manual retry attempts for media downloads.
     * Extracted from the {@code max_manual_retry} attribute of the
     * {@code media_conn} node, clamped to the range [0, 4], defaulting
     * to 3 if the attribute is absent.
     *
     * @implNote WAMediaConnParser.mediaConnParser --
     *           {@code maxManualRetry: r.maybeAttrInt("max_manual_retry", 0, 4) ?? 3}
     */
    private final int maxManualRetry;

    /**
     * The maximum number of automatic retry attempts for media downloads.
     * Extracted from the {@code max_auto_download_retry} attribute of the
     * {@code media_conn} node, clamped to the range [0, 4], defaulting
     * to 3 if the attribute is absent.
     *
     * @implNote WAMediaConnParser.mediaConnParser --
     *           {@code maxAutoDownloadRetry: r.maybeAttrInt("max_auto_download_retry", 0, 4) ?? 3}
     */
    private final int maxAutoDownloadRetry;

    /**
     * The epoch-second timestamp at which this media connection was created.
     * Used together with {@link #ttl} and {@link #authTtl} to compute
     * absolute expiry times for the routes and authentication token.
     *
     * @implNote ADAPTED: WAWebQueryMediaConnsJob.queryMediaConn --
     *           WA Web computes absolute expiry inline via
     *           {@code futureUnixTime(seconds)}; Cobalt stores the creation
     *           timestamp and the raw TTL separately
     */
    private final long timestamp;

    /**
     * The list of CDN host entries extracted from the {@code host} child
     * nodes of the {@code media_conn} node. Each host entry describes a
     * CDN endpoint with its hostname, IP addresses, supported media types,
     * download bucket assignments, and optional fallback hostname.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code hosts: c(r)} via
     *           {@code e.mapChildrenWithTag("host", u)},
     *           WAWebQueryMediaConnsJob.mapParsedMediaConn -- {@code hosts: e.hosts.map(...)}
     */
    private final SequencedCollection<? extends MediaHost> hosts;

    /**
     * Constructs a new media connection with the specified parsed fields.
     *
     * @implNote WAMediaConnParser.mediaConnParser,
     *           WAWebQueryMediaConnsJob.queryMediaConn
     * @param auth                 the authentication token from the
     *                             {@code auth} attribute
     * @param ttl                  the routes TTL in seconds from the
     *                             {@code ttl} attribute
     * @param authTtl              the auth token TTL in seconds from the
     *                             {@code auth_ttl} attribute
     * @param maxBuckets           the maximum bucket count from the
     *                             {@code max_buckets} attribute
     * @param maxManualRetry       the maximum manual retry count, defaulting
     *                             to 3
     * @param maxAutoDownloadRetry the maximum auto download retry count,
     *                             defaulting to 3
     * @param timestamp            the epoch-second creation timestamp
     * @param hosts                the list of CDN host entries
     */
    public MediaConnection(String auth, int ttl, int authTtl, int maxBuckets, int maxManualRetry, int maxAutoDownloadRetry, long timestamp, SequencedCollection<? extends MediaHost> hosts) {
        this.auth = auth;
        this.ttl = ttl;
        this.authTtl = authTtl;
        this.maxBuckets = maxBuckets;
        this.maxManualRetry = maxManualRetry;
        this.maxAutoDownloadRetry = maxAutoDownloadRetry;
        this.timestamp = timestamp;
        this.hosts = hosts;
    }

    /**
     * Constructs a {@link NodeBuilder} representing the IQ stanza used to
     * query the WhatsApp server for a media connection. The stanza has the
     * form:
     * <pre>{@code
     * <iq to="s.whatsapp.net" xmlns="w:m" type="set">
     *   <media_conn/>
     * </iq>
     * }</pre>
     *
     * <p>The caller must send this stanza via
     * {@code WhatsAppClient.sendNode(NodeBuilder)} and pass the response to
     * {@link #of(Node)} to obtain the parsed media connection.
     *
     * @implNote WAWebQueryMediaConnsJob.queryMediaConn -- IQ stanza
     *           construction: {@code wap("iq", {to: S_WHATSAPP_NET,
     *           xmlns: "w:m", type: "set", id: generateId()},
     *           wap("media_conn", null))}
     * @return a node builder representing the media connection query IQ
     */
    public static NodeBuilder queryNode() {
        // WAWebQueryMediaConnsJob.queryMediaConn
        var mediaConnChild = new NodeBuilder()
                .description("media_conn");
        return new NodeBuilder()
                .description("iq")
                .attribute("to", JidServer.user())
                .attribute("xmlns", "w:m")
                .attribute("type", "set")
                .content(mediaConnChild.build());
    }

    /**
     * Parses a {@code media_conn} IQ response node into a
     * {@code MediaConnection} instance. This method combines the behaviors
     * of {@code WAMediaConnParser.mediaConnParser} (raw attribute parsing),
     * {@code WAWebQueryMediaConnsJob.mapParsedMediaConn} (host mapping and
     * field selection), and the TTL computation from
     * {@code WAWebQueryMediaConnsJob.queryMediaConn}
     * ({@code routesExpiryTs - unixTime()}).
     *
     * @implNote WAMediaConnParser.mediaConnParser,
     *           WAWebQueryMediaConnsJob.mapParsedMediaConn,
     *           WAWebQueryMediaConnsJob.queryMediaConn
     * @param response the IQ response node containing a {@code media_conn}
     *                 child
     * @return the parsed media connection
     * @throws IllegalArgumentException if the response does not contain a
     *         valid {@code media_conn} child node
     */
    public static MediaConnection of(Node response) {
        // WAMediaConnParser.mediaConnParser -- var r = e.child("media_conn")
        var mediaConn = response.getRequiredChild("media_conn");

        // WAMediaConnParser.mediaConnParser -- authToken: r.attrString("auth")
        var auth = mediaConn.getRequiredAttributeAsString("auth");

        // WAMediaConnParser.mediaConnParser -- routesExpiryTs: r.attrFutureTime("ttl")
        // WAWebQueryMediaConnsJob.queryMediaConn -- ttl: routesExpiryTs - unixTime()
        // Since attrFutureTime("ttl") = unixTime() + attrInt("ttl"), the subtraction
        // yields approximately the raw attribute value.
        var ttl = mediaConn.getAttributeAsInt("ttl", 0);

        // WAMediaConnParser.mediaConnParser -- authTokenExpiryTs: r.attrFutureTime("auth_ttl")
        // WAWebQueryMediaConnsJob.queryMediaConn -- authTTL: authTokenExpiryTs - unixTime()
        var authTtl = mediaConn.getAttributeAsInt("auth_ttl", 0);

        // WAMediaConnParser.mediaConnParser -- maxBuckets: r.attrInt("max_buckets")
        var maxBuckets = mediaConn.getAttributeAsInt("max_buckets", 0);

        // WAMediaConnParser.mediaConnParser --
        // maxManualRetry: (t = r.maybeAttrInt("max_manual_retry", 0, 4)) != null ? t : 3
        var maxManualRetry = clampOptionalInt(
                mediaConn.getAttributeAsInt("max_manual_retry", (Integer) null),
                0, 4, 3
        );

        // WAMediaConnParser.mediaConnParser --
        // maxAutoDownloadRetry: (n = r.maybeAttrInt("max_auto_download_retry", 0, 4)) != null ? n : 3
        var maxAutoDownloadRetry = clampOptionalInt(
                mediaConn.getAttributeAsInt("max_auto_download_retry", (Integer) null),
                0, 4, 3
        );

        // WAMediaConnParser.mediaConnParser -- hosts: c(r) via e.mapChildrenWithTag("host", u)
        // WAWebQueryMediaConnsJob.mapParsedMediaConn -- hosts: e.hosts.map(...)
        var hosts = mediaConn.streamChildren("host")
                .map(MediaConnection::parseHost)
                .toList();

        // WAWebQueryMediaConnsJob.queryMediaConn -- queryStartTime: new Date()
        var timestamp = Instant.now().getEpochSecond();

        return new MediaConnection(auth, ttl, authTtl, maxBuckets, maxManualRetry, maxAutoDownloadRetry, timestamp, hosts);
    }

    /**
     * Parses a single {@code host} child node from the {@code media_conn}
     * response into a {@link MediaHost} instance. The host is classified as
     * {@link MediaHost.Fallback} when its {@code type} attribute equals
     * {@code "fallback"}, otherwise as {@link MediaHost.Primary}.
     *
     * @implNote WAMediaConnParser local function {@code u} (host parser),
     *           WAWebQueryMediaConnsJob.mapParsedMediaConn -- host mapping
     *           via {@code f(e)} and type/fallback classification
     * @param hostNode the host child node
     * @return the parsed media host
     */
    private static MediaHost parseHost(Node hostNode) {
        // WAMediaConnParser.u -- domain: e.attrString("hostname")
        var hostname = hostNode.getRequiredAttributeAsString("hostname");

        // WAMediaConnParser.u -- ip4, ip6
        // WAWebQueryMediaConnsJob.mapParsedMediaConn.f -- ips: [{ip4, ip6}]
        var ips = new ArrayList<String>();
        hostNode.getAttributeAsString("ip4").ifPresent(ips::add);
        hostNode.getAttributeAsString("ip6").ifPresent(ips::add);

        // WAMediaConnParser.u -- downloadable: d(e, "download"), uploadable: d(e, "upload")
        // WAWebQueryMediaConnsJob.mapParsedMediaConn inner function t -- compactMap(t.downloadable, _), compactMap(t.uploadable, _)
        var downloadTypes = parseMediaTypes(hostNode, "download");
        var uploadTypes = parseMediaTypes(hostNode, "upload");

        // WAMediaConnParser.u -- downloadBuckets: e.maybeChild("download_buckets")?.mapChildren(e => parseInt(e.tag(), 10)) ?? []
        var downloadBuckets = hostNode.getChild("download_buckets")
                .map(bucketsNode -> bucketsNode.streamChildren()
                        .map(Node::description)
                        .map(tag -> {
                            try {
                                return Integer.parseInt(tag);
                            } catch (NumberFormatException _) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList())
                .orElse(List.of());

        // WAMediaConnParser.u -- isFallback: e.maybeAttrString("type") === "fallback"
        // WAWebQueryMediaConnsJob.mapParsedMediaConn -- type: e.isFallback ? "fallback" : "primary"
        var isFallback = hostNode.getAttributeAsString("type")
                .map("fallback"::equals)
                .orElse(false);

        if (isFallback) {
            return new MediaHost.Fallback(
                    hostname,
                    List.copyOf(ips),
                    downloadBuckets,
                    downloadTypes,
                    uploadTypes
            );
        } else {
            // WAMediaConnParser.s -- fallback: e.hasAttr("fallback_hostname") ? {...} : undefined
            // WAWebQueryMediaConnsJob.mapParsedMediaConn -- fallback: e.fallback != null ? f(e.fallback) : void 0
            var fallbackHostname = hostNode.getAttributeAsString("fallback_hostname");
            var fallbackIps = new ArrayList<String>();
            hostNode.getAttributeAsString("fallback_ip4").ifPresent(fallbackIps::add);
            hostNode.getAttributeAsString("fallback_ip6").ifPresent(fallbackIps::add);

            return new MediaHost.Primary(
                    hostname,
                    List.copyOf(ips),
                    fallbackHostname,
                    List.copyOf(fallbackIps),
                    downloadBuckets,
                    downloadTypes,
                    uploadTypes
            );
        }
    }

    /**
     * Parses the media type list from a child node of a host node. If the
     * host node has a child with the given description, its children's tags
     * are mapped to {@link MediaPath} constants via
     * {@link MediaPath#ofId(String)}, filtering out types that return
     * {@code null} from the WA Web {@code castToServerMediaType} function.
     * If the child is absent, all known media types are returned (matching
     * the WA Web behavior of defaulting to {@code SERVER_MEDIA}).
     *
     * @implNote WAMediaConnParser local function {@code d} -- media type
     *           list parser; defaults to {@code SERVER_MEDIA} when absent.
     *           WAWebQueryMediaConnsJob.mapParsedMediaConn inner function
     *           {@code t} -- applies {@code compactMap(types, _)} which
     *           filters out {@code null} returns from the media type
     *           validator.
     * @param hostNode    the host node
     * @param description the child node description ({@code "download"} or
     *                    {@code "upload"})
     * @return the set of supported media paths
     */
    private static Set<MediaPath> parseMediaTypes(Node hostNode, String description) {
        // WAMediaConnParser.d -- e.hasChild(t) ? e.child(t).mapChildren(e => castToServerMediaType(e.tag())).filter(Boolean) : SERVER_MEDIA
        return hostNode.getChild(description)
                .map(typesNode -> {
                    var result = new LinkedHashSet<MediaPath>();
                    typesNode.streamChildren()
                            .map(Node::description)
                            .forEach(tag -> MediaPath.ofId(tag).ifPresent(result::add));
                    // WAWebQueryMediaConnsJob.mapParsedMediaConn inner function t --
                    // compactMap(t.downloadable, _) filters out null from _()
                    // The _ function returns null for "kyc-id", "novi-image", "novi-video",
                    // "thumbnail-gif", "xma-image"; since these are valid MediaPath entries
                    // but should not appear in host routing, we mirror the filter.
                    result.remove(MediaPath.KYC_ID);
                    result.remove(MediaPath.NOVI_IMAGE);
                    result.remove(MediaPath.NOVI_VIDEO);
                    result.remove(MediaPath.THUMBNAIL_GIF);
                    result.remove(MediaPath.XMA_IMAGE);
                    return Collections.unmodifiableSet(result);
                })
                .orElseGet(() -> {
                    // WAMediaConnParser.d -- defaults to SERVER_MEDIA (all known types)
                    // After compactMap with _, we must also remove the filtered types
                    var allTypes = new LinkedHashSet<>(MediaPath.known());
                    allTypes.remove(MediaPath.KYC_ID);
                    allTypes.remove(MediaPath.NOVI_IMAGE);
                    allTypes.remove(MediaPath.NOVI_VIDEO);
                    allTypes.remove(MediaPath.THUMBNAIL_GIF);
                    allTypes.remove(MediaPath.XMA_IMAGE);
                    return Collections.unmodifiableSet(allTypes);
                });
    }

    /**
     * Clamps an optional integer value to the given range, returning a
     * default value if the input is {@code null}. This mirrors the WA Web
     * helper {@code maybeAttrInt(name, min, max)} which returns {@code null}
     * when the attribute is absent or out of range, combined with the
     * nullish coalescing default.
     *
     * @implNote WAMediaConnParser.mediaConnParser --
     *           {@code r.maybeAttrInt("max_manual_retry", 0, 4) ?? 3}
     * @param value        the parsed integer value, or {@code null}
     * @param min          the minimum allowed value (inclusive)
     * @param max          the maximum allowed value (inclusive)
     * @param defaultValue the default value when input is {@code null} or
     *                     out of range
     * @return the clamped value, or the default
     */
    private static int clampOptionalInt(Integer value, int min, int max, int defaultValue) {
        if (value == null || value < min || value > max) {
            return defaultValue;
        }
        return value;
    }

    /**
     * The maximum number of retry attempts for media upload and download
     * operations. This constant matches the module-level {@code e = 4} in
     * {@code WAWebMmsClientSelectHost} and the {@code retries: 3} configuration
     * in {@code WAWebMmsClientMmsBackoffOptions} (3 retries = 4 total attempts,
     * indexed 0 through 3).
     *
     * @implNote WAWebMmsClientSelectHost -- {@code var e = 4},
     *           WAWebMmsClientMmsBackoffOptions -- {@code retries: 3}
     */
    private static final int MAX_ATTEMPT_COUNT = 4;

    /**
     * Selects the best host to use for the current retry attempt during a
     * media upload or download operation. This method implements the exact
     * host rotation strategy from {@code WAWebMmsClientSelectHost}:
     * <ol>
     *   <li>If the last fetch made progress and a host was previously used,
     *       reuse the same host (the connection was working).</li>
     *   <li>On the first or second attempt ({@code attemptCount <= 1}), use
     *       the initially selected host from route selection.</li>
     *   <li>On the last attempt ({@code attemptCount == MAX_ATTEMPT_COUNT - 1})
     *       with an available fallback host, use the fallback host.</li>
     *   <li>If the last host used is the same as the selected host and the
     *       selected host has a nested fallback hostname, use that fallback
     *       hostname.</li>
     *   <li>If a fallback host exists, use it.</li>
     *   <li>Otherwise, fall back to the selected host.</li>
     * </ol>
     *
     * @implNote WAWebMmsClientSelectHost.default
     * @param selectedHost          the host chosen by
     *                              {@link MediaHost#routeSelection}, or
     *                              {@code null} if route selection found no match
     * @param fallbackHost          the fallback-type host from route selection,
     *                              or {@code null} if none exists
     * @param lastHostUsed          the hostname used in the previous attempt,
     *                              or {@code null} on the first attempt
     * @param attemptCount          the zero-based attempt number
     * @param lastFetchMadeProgress {@code true} if the previous attempt
     *                              transferred at least some data
     * @return the hostname to use for this attempt, or {@code null} if no
     *         host is available
     */
    static String selectHost(
            MediaHost selectedHost,
            MediaHost fallbackHost,
            String lastHostUsed,
            int attemptCount,
            boolean lastFetchMadeProgress
    ) {
        // WAWebMmsClientSelectHost.default
        var selectedHostname = selectedHost != null ? selectedHost.hostname() : null;
        var fallbackHostname = fallbackHost != null ? fallbackHost.hostname() : null;

        // o && a ? a — if last fetch made progress and a host was used, reuse it
        if (lastFetchMadeProgress && lastHostUsed != null) {
            return lastHostUsed;
        }

        // n <= 1 ? i — first or second attempt, use selected host
        if (attemptCount <= 1) {
            return selectedHostname;
        }

        // n === e - 1 && r ? r — last attempt with fallback, use fallback
        if (attemptCount == MAX_ATTEMPT_COUNT - 1 && fallbackHostname != null) {
            return fallbackHostname;
        }

        // a != null && a.equals(i) && i.fallback != null ? i.fallback
        // In WA Web, equals compares hostnames; i.fallback is the nested fallback MediaHost
        if (lastHostUsed != null
                && lastHostUsed.equals(selectedHostname)
                && selectedHost != null
                && selectedHost.fallbackHostname().isPresent()) {
            return selectedHost.fallbackHostname().get();
        }

        // r != null ? r : i — use fallback if available, otherwise selected
        return fallbackHostname != null ? fallbackHostname : selectedHostname;
    }

    /**
     * Uploads media content to the WhatsApp CDN on behalf of the given
     * provider. Uses {@link MediaHost#routeSelection} to pick the initial
     * host and {@link #selectHost} to rotate hosts across retry attempts,
     * matching the retry strategy from {@code WAWebMmsClient.upload}.
     *
     * <p>On success, the provider's media metadata fields (SHA-256 hashes,
     * encryption key, direct path, URL, file size, key timestamp) are
     * populated from the upload response. If the provider is an
     * {@link ExternalBlobReference}, the server handle is also set.
     *
     * @implNote WAWebMmsClient.upload, WAWebMmsClientSelectHost.default,
     *           WAWebSyncdNetCallbacksApi.uploadSyncExternalPatch,
     *           WAWebSyncdMMSUpload.buildExternalBlobReference
     * @param provider    the media provider describing the media type and
     *                    receiving the upload metadata
     * @param inputStream the input stream containing the media content
     * @return {@code true} if the upload succeeded
     * @throws WhatsAppMediaException.Upload if no host could service the
     *         upload, a non-retryable HTTP error occurred (413, 415, 507),
     *         or an I/O error occurred
     */
    public boolean upload(MediaProvider provider, InputStream inputStream) throws WhatsAppMediaException {
        Objects.requireNonNull(provider, "provider cannot be null");
        Objects.requireNonNull(inputStream, "inputStream cannot be null");

        var path = provider.mediaPath()
                .path();
        if (path.isEmpty()) {
            return false;
        }

        try(var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            var uploadStream = MediaUploadInputStream.of(provider, inputStream);
            var tempFile = Files.createTempFile("upload", ".tmp");
            try {
                try (uploadStream; var outputStream = Files.newOutputStream(tempFile)) {
                    uploadStream.transferTo(outputStream);
                }
                var timestamp = Instant.now();
                var fileSha256 = uploadStream.fileSha256();
                var fileEncSha256 = uploadStream.fileEncSha256()
                        .orElse(null);
                var mediaKey = uploadStream.fileKey()
                        .orElse(null);
                var fileLength = uploadStream.fileLength();

                // WAWebMmsClient.upload — route selection via WAWebMediaHostsRouteSelection
                var hostList = hosts instanceof List<? extends MediaHost> list ? list : List.copyOf(hosts);
                var route = MediaHost.routeSelection(
                        Operation.UPLOAD,
                        provider.mediaPath(),
                        hostList,
                        fileEncSha256 != null ? Base64.getEncoder().encodeToString(fileEncSha256) : null,
                        maxBuckets > 0 ? maxBuckets : null,
                        false
                );

                // WAWebMmsClient.upload — retry loop with selectHost
                String lastHostUsed = null; // WAWebMmsClient.upload — var h = null
                var lastFetchMadeProgress = false; // WAWebMmsClient.upload — var _ = !1
                for (var attemptCount = 0; attemptCount < MAX_ATTEMPT_COUNT; attemptCount++) {
                    // WAWebMmsClientSelectHost.default — pick host for this attempt
                    var hostname = selectHost(
                            route.selectedHost().orElse(null),
                            route.fallbackHost().orElse(null),
                            lastHostUsed,
                            attemptCount,
                            lastFetchMadeProgress
                    );
                    if (hostname == null) {
                        continue;
                    }
                    lastHostUsed = hostname; // WAWebMmsClient.upload — h = I

                    try {
                        var uploadResult = tryUpload(client, hostname, path.get(), provider.mediaPath(), fileEncSha256, fileSha256, tempFile);
                        // WAWebMmsClientMmsUpload.default -- response validator u() already validated
                        var directPath = uploadResult.getString("direct_path"); // WAWebMmsClientMmsUpload.u -- directPath: e.direct_path
                        var url = uploadResult.getString("url"); // WAWebMmsClientMmsUpload.u -- url: e.url
                        var handle = uploadResult.getString("handle"); // WAWebMmsClientMmsUpload.u -- handle: e.handle

                        provider.setMediaSha256(fileSha256);
                        provider.setMediaEncryptedSha256(fileEncSha256);
                        provider.setMediaKey(mediaKey);
                        provider.setMediaSize(fileLength);
                        provider.setMediaDirectPath(directPath);
                        provider.setMediaUrl(url);
                        provider.setMediaKeyTimestamp(timestamp);
                        if (provider instanceof ExternalBlobReference externalBlobReference) { // WAWebSyncdMMSUpload.buildExternalBlobReference -- handle: a
                            externalBlobReference.setHandle(handle);
                        }

                        return true;
                    } catch (WhatsAppMediaException.Upload uploadException) {
                        // WAWebMmsClientIsErrorRetryable.isRetriableStatusCode / isErrorRetryable --
                        // 401 is retryable (MMSUnauthorizedError), 5xx (except 507) is retryable,
                        // 413, 415, 507 are NOT retryable, network errors (no status code) are retryable
                        if (!isRetryable(uploadException)) {
                            throw uploadException;
                        }
                        // WAWebMmsClient.upload — on failure, lastFetchMadeProgress reset to false
                        lastFetchMadeProgress = false;
                    }
                }

                throw new WhatsAppMediaException.Upload("Cannot upload media: no hosts available");
            } finally {
                // NO_WA_BASIS -- clean up temp file used for buffering the encrypted content
                Files.deleteIfExists(tempFile);
            }
        } catch (WhatsAppMediaException.Upload uploadException) {
            throw uploadException;
        } catch (IOException exception) {
            throw new WhatsAppMediaException.Upload("Cannot upload media", exception);
        }
    }

    /**
     * Attempts to upload encrypted media content to a single CDN host.
     * Constructs the upload URL from the hostname, path, and authentication
     * token, then sends an HTTP POST with the file content.
     *
     * <p>The URL is constructed following the pattern defined by
     * {@code WAWebMmsClientFormatHashUrl.default}: the encrypted file hash is
     * encoded as URL-safe base64 (with padding preserved) and placed in the
     * URL path segment, while the auth token, token duplicate, and media ID
     * are placed in the query string. The query parameters are assembled by
     * {@code WAWebMmsClientFormatUploadUrl.default}, which passes them into
     * the hash URL formatter.
     *
     * <p>The {@code media_id} parameter is a random integer generated per
     * upload attempt, matching the behavior of
     * {@code WAWebWamMediaMetricUtils.generateMediaEventId}. The
     * {@code server_transcode} parameter is set to {@code "1"} when the
     * media type is {@link MediaPath#NEWSLETTER_VIDEO} to request server-side
     * video transcoding for newsletter media.
     *
     * <p>On non-200 responses, this method throws a
     * {@link WhatsAppMediaException.Upload} with the HTTP status code
     * preserved, matching the error mapping in
     * {@code WAWebMmsClientMmsUpload.default}:
     * <ul>
     *   <li>401 -- {@code MMSUnauthorizedError}</li>
     *   <li>413 -- {@code MediaTooLargeError}</li>
     *   <li>415 -- {@code MediaInvalidError} (hash mismatch)</li>
     *   <li>507 -- {@code MMSThrottleError}</li>
     *   <li>other -- {@code HttpStatusCodeError}</li>
     * </ul>
     *
     * <p>The response JSON is validated to contain non-null, non-empty
     * {@code direct_path} and {@code url} fields, matching the standard
     * response validator {@code u()} in the WA Web module.
     *
     * @implNote WAWebMmsClientMmsUpload.default,
     *           WAWebMmsClientFormatUploadUrl.default,
     *           WAWebMmsClientFormatHashUrl.default
     * @param client        the HTTP client to use
     * @param hostname      the CDN hostname
     * @param path          the CDN path segment
     * @param mediaPath     the media path type for the upload
     * @param fileEncSha256 the encrypted file SHA-256, or {@code null}
     * @param fileSha256    the plaintext file SHA-256
     * @param body          the path to the temporary file containing the
     *                      encrypted content
     * @return the parsed response JSON containing at least {@code direct_path}
     *         and {@code url} fields
     * @throws WhatsAppMediaException.Upload if the server returns a non-200
     *         status code or the response is missing required fields
     */
    private JSONObject tryUpload(HttpClient client, String hostname, String path, MediaPath mediaPath, byte[] fileEncSha256, byte[] fileSha256, Path body) throws WhatsAppMediaException.Upload {
        try {
            // WAWebMmsClientFormatHashUrl.default -- urlSafeBase64(encFilehash)
            // WA Web's urlSafeBase64 replaces +/- and /->_ but preserves = padding
            var token = Base64.getUrlEncoder()
                    .encodeToString(Objects.requireNonNullElse(fileEncSha256, fileSha256));
            // WAWebMmsClientFormatHashUrl.default -- "https://" + hostname + pathSegment + "/" + urlSafeBase64(encFilehash)
            var basePath = "https://" + hostname + "/" + path + "/" + token;
            // WAWebMmsClientFormatUploadUrl.default -- query params assembled and filtered
            var queryParams = new LinkedHashMap<String, String>();
            // WAWebMmsClientFormatUploadUrl.default -- auth: a
            queryParams.put("auth", this.auth);
            // WAWebMmsClientFormatUploadUrl.default -- token: urlSafeBase64(token)
            queryParams.put("token", token);
            // WAWebMmsClientFormatUploadUrl.default -- media_id: mediaId.toString(10)
            // WAWebWamMediaMetricUtils.generateMediaEventId -- 1 + Math.floor(MAX_SAFE_INTEGER * Math.random())
            queryParams.put("media_id", String.valueOf(generateMediaId()));
            // WAWebMmsClientFormatUploadUrl.default -- server_transcode: type === "newsletter-video" && AB prop ? "1" : undefined
            if (mediaPath == MediaPath.NEWSLETTER_VIDEO) {
                // WAWebMmsClientFormatUploadUrl.default / WAWebChannelVideoServerTranscodeGating
                queryParams.put("server_transcode", "1");
            }
            // WAWebMmsClientFormatUploadUrl.default / WAWebMmsClientFormatHashUrl.default -- URLSearchParams filters nulls
            var queryString = encodeQueryString(queryParams);
            var uri = URI.create(basePath + queryString);
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofFile(body));
            var request = requestBuilder.header("Content-Type", "application/octet-stream")
                    .header("Accept", "application/json")
                    .headers("Origin", "https://web.whatsapp.com")
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            // WAWebMmsClientMmsUpload.default -- status code error mapping
            if (response.statusCode() != 200) {
                // WAWebMmsClientMmsUpload.default -- switch(e.status)
                var statusCode = response.statusCode();
                var message = switch (statusCode) {
                    case 401 -> "mmsUpload: unauthorized"; // WAWebMmsClientErrors.MMSUnauthorizedError
                    case 413 -> "mmsUpload: media too large"; // WAWebMmsClientErrors.MediaTooLargeError
                    case 415 -> "mmsUpload: hash mismatch"; // WAWebMmsClientErrors.MediaInvalidError
                    case 507 -> "mmsUpload: throttled"; // WAWebMmsClientErrors.MMSThrottleError
                    default -> "mmsUpload: HTTP " + statusCode; // WAWebHttpErrors.HttpStatusCodeError
                };
                throw new WhatsAppMediaException.Upload(statusCode, message);
            }

            // WAWebMmsClientMmsUpload.default -- e.json().then(b)
            var jsonObject = JSON.parseObject(response.body());
            // WAWebMmsClientMmsUpload.default -- response validator u(): validates direct_path and url
            var directPath = jsonObject != null ? jsonObject.getString("direct_path") : null;
            var url = jsonObject != null ? jsonObject.getString("url") : null;
            if (directPath == null || directPath.isEmpty()) {
                // WAWebMmsClientMmsUpload.u -- throw HttpInvalidResponseError("mmsUpload: missing direct_path")
                throw new WhatsAppMediaException.Upload("mmsUpload: missing direct_path");
            }
            if (url == null || url.isEmpty()) {
                // WAWebMmsClientMmsUpload.u -- throw HttpInvalidResponseError("mmsUpload: missing url")
                throw new WhatsAppMediaException.Upload("mmsUpload: missing url");
            }
            return jsonObject;
        } catch (WhatsAppMediaException.Upload upload) {
            throw upload;
        } catch (IOException | InterruptedException exception) {
            // ADAPTED: WAWebMmsClientMmsUpload.default -- network errors are retryable in WA Web;
            // Cobalt wraps them in WhatsAppMediaException.Upload without an HTTP status code
            throw new WhatsAppMediaException.Upload("mmsUpload: network error", exception);
        }
    }

    /**
     * Generates a random media event identifier for upload tracking purposes.
     * The generated value is a random integer between 1 and
     * {@code Number.MAX_SAFE_INTEGER} (9007199254740991), matching the
     * behavior of the WA Web metrics utility.
     *
     * @implNote WAWebWamMediaMetricUtils.generateMediaEventId --
     *           {@code 1 + Math.floor(Number.MAX_SAFE_INTEGER * Math.random())}
     * @return a random positive long value suitable for use as a media ID
     */
    private static long generateMediaId() {
        // WAWebWamMediaMetricUtils.generateMediaEventId
        // 1 + Math.floor(Number.MAX_SAFE_INTEGER * Math.random())
        // Number.MAX_SAFE_INTEGER = 9007199254740991
        return 1 + (long) Math.floor(9007199254740991.0 * Math.random());
    }

    /**
     * Encodes a map of query parameters into a URL query string, filtering
     * out {@code null} values. Each key and value is percent-encoded using
     * {@code application/x-www-form-urlencoded} encoding, matching the
     * behavior of {@code URLSearchParams.toString()} used by WA Web's
     * {@code WAWebMmsClientFormatHashUrl.default}.
     *
     * @implNote WAWebMmsClientFormatHashUrl.default --
     *           {@code new URLSearchParams(filteredQuery).toString()}
     * @param params the query parameter map
     * @return the encoded query string with leading {@code ?}, or an empty
     *         string if no parameters remain after filtering
     */
    private static String encodeQueryString(Map<String, String> params) {
        // WAWebMmsClientFormatHashUrl.default -- new URLSearchParams(c).toString()
        var sb = new StringBuilder();
        for (var entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.isEmpty() ? "" : "?" + sb;
    }

    /**
     * Determines whether a media upload exception represents a retryable error.
     * This mirrors the logic of {@code WAWebMmsClientIsErrorRetryable.isErrorRetryable}
     * and {@code WAWebMmsClientIsErrorRetryable.isRetriableStatusCode}:
     * <ul>
     *   <li>No HTTP status code (network error): retryable</li>
     *   <li>401 ({@code MMSUnauthorizedError}): retryable</li>
     *   <li>408 (request timeout): retryable</li>
     *   <li>507 ({@code MMSThrottleError}): <strong>not</strong> retryable</li>
     *   <li>Other 5xx: retryable</li>
     *   <li>All other status codes (413, 415, etc.): not retryable</li>
     * </ul>
     *
     * @implNote WAWebMmsClientIsErrorRetryable.isRetriableStatusCode,
     *           WAWebMmsClientIsErrorRetryable.isErrorRetryable
     * @param exception the upload exception to test
     * @return {@code true} if the error is retryable, {@code false} otherwise
     */
    private static boolean isRetryable(WhatsAppMediaException.Upload exception) {
        // WAWebMmsClientIsErrorRetryable.isRetriableStatusCode
        var optStatus = exception.httpStatusCode();
        if (optStatus.isEmpty()) {
            // WAWebMmsClientIsErrorRetryable.isErrorRetryable -- network errors are retryable
            return true;
        }
        var status = optStatus.getAsInt();
        // WAWebMmsClientIsErrorRetryable.isRetriableStatusCode -- e === 408 ? true : e === 507 ? false : e >= 500
        if (status == 408) {
            return true;
        }
        if (status == 507) {
            return false;
        }
        if (status >= 500) {
            return true;
        }
        // WAWebMmsClientIsErrorRetryable.isErrorRetryable -- instanceof MMSUnauthorizedError (401)
        return status == 401;
    }

    /**
     * Downloads media content from the WhatsApp CDN for the given provider.
     * First attempts to download from the provider's existing media URL, then
     * uses {@link MediaHost#routeSelection} to pick the initial host and
     * {@link #selectHost} to rotate hosts across retry attempts, matching
     * the retry strategy from {@code WAWebMmsClient.download}.
     *
     * <p>Non-retryable errors (e.g., 404, 410, 507) are thrown immediately.
     * Retryable errors (e.g., 401, 5xx except 507, network errors) cause the
     * loop to advance to the next attempt.
     *
     * @implNote WAWebMmsClient.download, WAWebMmsClientSelectHost.default,
     *           WAWebMmsClientMmsDownload.mms4Download
     * @param provider the media provider describing the media type and
     *                 carrying the direct path and optional URL
     * @return an {@link InputStream} containing the downloaded media content
     * @throws WhatsAppMediaException.Download if no host could service the
     *         download, the direct path is missing, or a non-retryable HTTP
     *         error occurred (404, 410, 507, etc.)
     */
    public InputStream download(MediaProvider provider) throws WhatsAppMediaException {
        Objects.requireNonNull(provider, "provider cannot be null");

        // WAWebMmsClient.download — try static URL first
        var defaultUploadUrl = provider.mediaUrl();
        if (defaultUploadUrl.isPresent()) {
            try {
                return tryDownload(provider, defaultUploadUrl.get()); // WAWebMmsClient.download
            } catch (WhatsAppMediaException.Download downloadException) {
                // WAWebMmsClient.download — static URL failure falls through to host-based download
                if (!isDownloadRetryable(downloadException)) {
                    throw downloadException;
                }
            }
        }

        // WAWebMmsClientMmsDownload.mms4Download — directPath and encFilehash
        var defaultDirectPath = provider.mediaDirectPath()
                .orElse(null);
        var encFileHash = provider.mediaEncryptedSha256()
                .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                .orElse(null);
        var mediaType = provider.mediaPath();

        // WAWebMmsClientMmsDownload.mms4Download — if neither directPath nor encFilehash, reject
        if ((defaultDirectPath == null || defaultDirectPath.isEmpty())
                && (encFileHash == null || encFileHash.isEmpty())) {
            throw new WhatsAppMediaException.Download(
                    "No staticUrl, directPath, or encFilehash available for download"); // WAWebMmsClientMmsDownload.mms4Download
        }

        // WAWebMmsClient.download — route selection via WAWebMediaHostsRouteSelection
        var hostList = hosts instanceof List<? extends MediaHost> list ? list : List.copyOf(hosts);
        var route = MediaHost.routeSelection(
                Operation.DOWNLOAD,
                mediaType,
                hostList,
                encFileHash,
                maxBuckets > 0 ? maxBuckets : null,
                false
        );

        // WAWebMediaHostsRouteSelection.routeSelection — compute selectedBucket
        // In WA Web, the bucket is stored on the selected host via setSelectedBucket(p)
        // and later accessed as k.selectedBucket in WAWebMmsClient.download.
        // Only the selected host carries the bucket; fallback hosts have no bucket.
        var selectedBucket = computeSelectedBucket(encFileHash); // WAWebMediaHostsRouteSelection.routeSelection
        var selectedHostname = route.selectedHost()
                .map(MediaHost::hostname)
                .orElse(null); // WAWebMmsClientSelectHost — selectedHost.hostname for bucket matching

        // WAWebMmsClient.download — retry loop with selectHost
        String lastHostUsed = null; // WAWebMmsClient.download — var b = null
        var lastFetchMadeProgress = false; // WAWebMmsClient.download — var h = !1
        for (var attemptCount = 0; attemptCount < MAX_ATTEMPT_COUNT; attemptCount++) {
            // WAWebMmsClientSelectHost.default — pick host for this attempt
            var hostname = selectHost(
                    route.selectedHost().orElse(null),
                    route.fallbackHost().orElse(null),
                    lastHostUsed,
                    attemptCount,
                    lastFetchMadeProgress
            );
            if (hostname == null) {
                continue;
            }
            lastHostUsed = hostname; // WAWebMmsClient.download — b = k

            // WAWebMmsClientSelectHost — selectedBucket is only set on the
            // selected host via routeSelection.setSelectedBucket(p); fallback
            // and nested-fallback hosts do not carry a bucket.
            var hostBucket = hostname.equals(selectedHostname) ? selectedBucket : null; // WAWebMmsClientSelectHost — k.selectedBucket

            // WAWebMmsClientFormatDownloadUrl.default — construct the download URL
            var downloadUrl = formatDownloadUrl(
                    hostname, defaultDirectPath, encFileHash, mediaType, hostBucket
            );

            try {
                return tryDownload(provider, downloadUrl); // WAWebMmsClientMmsDownload.mms4Download
            } catch (WhatsAppMediaException.Download downloadException) {
                // WAWebMmsClientIsErrorRetryable.isErrorRetryable / isRetriableStatusCode —
                // same retryable logic as upload: 401 retryable, 5xx (except 507) retryable,
                // network errors retryable; 404, 410, 413, 415, 507 are NOT retryable
                if (!isDownloadRetryable(downloadException)) {
                    throw downloadException;
                }
                // WAWebMmsClient.download — on failure, lastFetchMadeProgress reset to false
                lastFetchMadeProgress = false;
            }
        }

        throw new WhatsAppMediaException.Download("Cannot download media: no hosts available");
    }

    /**
     * Constructs the download URL for a media file, choosing between the
     * direct-path URL format and the hash-based URL format depending on the
     * availability of a direct path. This mirrors the top-level function
     * exported by {@code WAWebMmsClientFormatDownloadUrl}.
     *
     * <p>When a direct path is available, the URL is constructed as
     * {@code https://{hostname}{directPath}?hash=...&_nc_cat=...&mode=...&mms-type=...&__wa-mms=}
     * via {@link #buildDirectPathUrl}. When only the encrypted file hash is
     * available, the URL is constructed as
     * {@code https://{hostname}/{path}/{urlSafeBase64(encFileHash)}?mode=...&__wa-mms=}
     * via {@link #formatHashUrl}.
     *
     * @implNote WAWebMmsClientFormatDownloadUrl.default
     * @param hostname       the CDN hostname
     * @param directPath     the direct path, or {@code null} if unavailable
     * @param encFileHash    the base64-encoded encrypted file hash, or
     *                       {@code null} if unavailable
     * @param mediaType      the media path type
     * @param downloadBucket the selected download bucket, or {@code null} if
     *                       no bucket was selected
     * @return the fully constructed download URL
     * @throws WhatsAppMediaException.Download if neither direct path nor
     *         encrypted file hash is available
     */
    private String formatDownloadUrl(
            String hostname,
            String directPath,
            String encFileHash,
            MediaPath mediaType,
            Integer downloadBucket
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientFormatDownloadUrl.default (function u)
        var mediaTypeId = mediaType.id().orElse(null);
        if (directPath != null && !directPath.isEmpty()) {
            // WAWebMmsClientFormatDownloadUrl.default — direct path branch
            var query = new LinkedHashMap<String, String>();
            query.put("mode", "auto"); // WAWebMmsClientMmsDownload.mms4Download passes mode
            // WAWebSharedConstants.MMS_URL_MEDIA_TYPE_SEARCH_PARAM = "mms-type"
            query.put("mms-type", mediaTypeId);
            // WAWebSharedConstants.IS_MMS_URL_SEARCH_PARAM = "__wa-mms"
            query.put("__wa-mms", "");
            return buildDirectPathUrl(hostname, directPath, encFileHash, downloadBucket, query);
        }

        // WAWebMmsClientFormatDownloadUrl.default — hash URL fallback
        if (encFileHash == null || encFileHash.isEmpty()) {
            throw new WhatsAppMediaException.Download(
                    "No direct path or encFilehash available for download, abort"); // WAWebMmsClientFormatDownloadUrl.default
        }
        var query = new LinkedHashMap<String, String>();
        query.put("mode", "auto"); // WAWebMmsClientMmsDownload.mms4Download passes mode
        // WAWebSharedConstants.IS_MMS_URL_SEARCH_PARAM = "__wa-mms"
        query.put("__wa-mms", "");
        return formatHashUrl(hostname, mediaType, encFileHash, query);
    }

    /**
     * Constructs a direct-path download URL with query parameters matching the
     * inner function {@code c} of {@code WAWebMmsClientFormatDownloadUrl}. The
     * URL is built from the hostname and direct path, and the following query
     * parameters are appended:
     * <ul>
     *   <li>{@code hash} -- the URL-safe base64 encoding of the encrypted
     *       file hash, if non-null and non-empty</li>
     *   <li>{@code _nc_cat} -- the download bucket number, if non-null</li>
     *   <li>All entries from the provided query map with non-null values</li>
     * </ul>
     *
     * <p>A security check is performed to ensure the constructed URL's hostname
     * matches the expected hostname, preventing malicious direct paths from
     * redirecting to an unintended server.
     *
     * @implNote WAWebMmsClientFormatDownloadUrl local function c
     * @param hostname       the expected CDN hostname
     * @param directPath     the direct path segment
     * @param encFileHash    the base64-encoded encrypted file hash, or
     *                       {@code null}
     * @param downloadBucket the download bucket number, or {@code null}
     * @param query          the additional query parameters to include
     * @return the fully constructed direct-path download URL
     * @throws WhatsAppMediaException.Download if the direct path resolves to
     *         a different hostname than expected
     */
    private static String buildDirectPathUrl(
            String hostname,
            String directPath,
            String encFileHash,
            Integer downloadBucket,
            Map<String, String> query
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientFormatDownloadUrl.c — var c = new URL(t, "https://" + i)
        var baseUri = URI.create("https://" + hostname).resolve(directPath);

        // WAWebMmsClientFormatDownloadUrl.c — if (c.hostname !== i) throw "malicious directPath"
        if (!baseUri.getHost().equals(hostname)) {
            throw new WhatsAppMediaException.Download("malicious directPath"); // WAWebMmsClientFormatDownloadUrl.c
        }

        // WAWebMmsClientFormatDownloadUrl.c — var d = c.searchParams || new URLSearchParams(c.search)
        // Preserve any existing query params from directPath, then add new ones on top
        var params = new LinkedHashMap<String, String>();
        var existingQuery = baseUri.getRawQuery();
        if (existingQuery != null && !existingQuery.isEmpty()) {
            // ADAPTED: WAWebMmsClientFormatDownloadUrl.c — URLSearchParams parses existing params
            for (var param : existingQuery.split("&")) {
                var eqIndex = param.indexOf('=');
                if (eqIndex >= 0) {
                    params.put(param.substring(0, eqIndex), param.substring(eqIndex + 1));
                } else {
                    params.put(param, "");
                }
            }
        }

        // WAWebMmsClientFormatDownloadUrl.c — a != null && a !== "" && d.set("hash", urlSafeBase64(a))
        if (encFileHash != null && !encFileHash.isEmpty()) {
            params.put("hash", urlSafeBase64(encFileHash)); // WABase64UrlSafe.urlSafeBase64
        }

        // WAWebMmsClientFormatDownloadUrl.c — n != null && d.set("_nc_cat", n.toString())
        if (downloadBucket != null) {
            params.put("_nc_cat", downloadBucket.toString());
        }

        // WAWebMmsClientFormatDownloadUrl.c — Object.keys(l).forEach(...)
        for (var entry : query.entrySet()) {
            if (entry.getValue() != null) {
                params.put(entry.getKey(), entry.getValue());
            }
        }

        // WAWebMmsClientFormatDownloadUrl.c — "https://" + c.host + c.pathname + "?" + d.toString()
        return "https://" + baseUri.getHost() + baseUri.getRawPath()
                + encodeQueryString(params);
    }

    /**
     * Constructs a hash-based download URL following the format defined by
     * {@code WAWebMmsClientFormatHashUrl.default}. The URL has the form
     * {@code https://{hostname}/{path}/{urlSafeBase64(encFileHash)}?{query}}
     * where the path is obtained from the media type's
     * {@link MediaPath#path()} and the encrypted file hash is URL-safe base64
     * encoded.
     *
     * @implNote WAWebMmsClientFormatHashUrl.default
     * @param hostname    the CDN hostname
     * @param mediaType   the media path type (must have a non-null path)
     * @param encFileHash the base64-encoded encrypted file hash
     * @param query       the additional query parameters to include
     * @return the fully constructed hash-based download URL
     * @throws WhatsAppMediaException.Download if the media type has no path
     *         for hash URL construction
     */
    private static String formatHashUrl(
            String hostname,
            MediaPath mediaType,
            String encFileHash,
            Map<String, String> query
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientFormatHashUrl.default — var l = nullthrows(u[i])
        var pathSegment = mediaType.path()
                .orElseThrow(() -> new WhatsAppMediaException.Download(
                        "No hash URL path for media type: " + mediaType)); // WAWebMmsClientFormatHashUrl.default — nullthrows

        // WAWebMmsClientFormatHashUrl.default — new URL("https://" + n + l + "/" + urlSafeBase64(t))
        var basePath = "https://" + hostname + "/" + pathSegment + "/" + urlSafeBase64(encFileHash);

        // WAWebMmsClientFormatHashUrl.default — filter null values and build query string
        var filteredQuery = new LinkedHashMap<String, String>();
        for (var entry : query.entrySet()) {
            if (entry.getValue() != null) {
                filteredQuery.put(entry.getKey(), entry.getValue());
            }
        }

        // WAWebMmsClientFormatHashUrl.default — new URLSearchParams(c).toString()
        return basePath + encodeQueryString(filteredQuery);
    }

    /**
     * Converts a standard base64 string to a URL-safe base64 string by
     * replacing {@code /} with {@code _} and {@code +} with {@code -}. This
     * mirrors the WA Web utility function {@code WABase64UrlSafe.urlSafeBase64}
     * which preserves padding characters ({@code =}).
     *
     * @implNote WABase64UrlSafe.urlSafeBase64
     * @param base64 the standard base64 string to convert
     * @return the URL-safe base64 string
     */
    private static String urlSafeBase64(String base64) {
        // WABase64UrlSafe.urlSafeBase64 — e.replace(/\//g, "_").replace(/\+/g, "-")
        return base64.replace('/', '_').replace('+', '-');
    }

    /**
     * Computes the selected download bucket for the current media connection
     * parameters. This mirrors the bucket computation performed inside
     * {@code WAWebMediaHostsRouteSelection.routeSelection} before calling
     * {@code host.setSelectedBucket(p)}.
     *
     * <p>When {@code encFileHash} is {@code null}, the bucket defaults to
     * {@code 0}. When vcache aggregation is enabled (currently always
     * {@code false} in Cobalt) and {@link #maxBuckets} is positive, the bucket
     * is computed as {@code base64Modulo(encFileHash, maxBuckets) + 100}.
     * Otherwise the bucket is {@code null}, meaning no bucket-based routing.
     *
     * @implNote WAWebMediaHostsRouteSelection.routeSelection -- bucket
     *           computation before {@code setSelectedBucket(p)}
     * @param encFileHash the base64-encoded encrypted file hash, or
     *                    {@code null}
     * @return the selected bucket number, or {@code null} if no bucket applies
     */
    private Integer computeSelectedBucket(String encFileHash) {
        // WAWebMediaHostsRouteSelection.routeSelection — bucket computation
        // n == null ? p = 0 : m && i != null && (p = base64Modulo(n, i) + 100)
        if (encFileHash == null) {
            return 0;
        }
        // vcache aggregation is currently not enabled in Cobalt
        // When enabled: return base64Modulo(encFileHash, maxBuckets) + 100
        return null;
    }

    /**
     * Attempts to download media content from a single URL. Sends an HTTP
     * GET request, validates the response via the
     * {@link #validateMmsResponse(int, String)} logic, and wraps the
     * response in a {@link MediaDownloadInputStream} that handles decryption
     * and integrity verification.
     *
     * <p>This method corresponds to the inner download function {@code d}
     * in {@code WAWebMmsClientMmsDownload} which calls
     * {@code extendedFetch(url, ...)} followed by
     * {@code validateMmsResponse({response, ...})}.
     *
     * @implNote WAWebMmsClientMmsDownload.mms4Download (inner function d),
     *           WAWebMmsClientMmsDownload.validateMmsResponse
     * @param provider    the media provider for decryption metadata
     * @param downloadUrl the full URL to download from
     * @return an {@link InputStream} containing the downloaded media content
     * @throws WhatsAppMediaException.Download if the server returns a
     *         non-200 status code, the content length is missing, or a
     *         network error occurs
     */
    public InputStream tryDownload(MediaProvider provider, String downloadUrl) throws WhatsAppMediaException.Download {
        var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // WAWebMmsClientMmsDownload.validateMmsResponse — validate HTTP status
            if (response.statusCode() != 200) {
                client.close();
                validateMmsResponse(response.statusCode(), downloadUrl); // WAWebMmsClientMmsDownload.validateMmsResponse
            }

            var payloadLength = response.headers()
                    .firstValueAsLong("Content-Length")
                    .orElseThrow(() -> {
                        client.close();
                        return new WhatsAppMediaException.Download("Unknown content length");
                    });

            var rawInputStream = response.body();
            return new MediaDownloadInputStream(client, rawInputStream, payloadLength, provider);
        } catch (WhatsAppMediaException.Download downloadException) {
            throw downloadException;
        } catch (IOException | InterruptedException exception) {
            // ADAPTED: WAWebMmsClientMmsDownload.mms4Download — TypeError is mapped to HttpNetworkError;
            // Cobalt wraps I/O errors in WhatsAppMediaException.Download without an HTTP status code
            client.close();
            throw new WhatsAppMediaException.Download("mmsDownload: network error", exception);
        }
    }

    /**
     * Validates an HTTP response status code from a media download request,
     * throwing the appropriate exception for non-OK responses. This method
     * implements the exact error mapping from
     * {@code WAWebMmsClientMmsDownload.validateMmsResponse}:
     * <ul>
     *   <li>401 -- {@link WhatsAppMediaException.Download} with
     *       {@link WhatsAppMediaException#HTTP_UNAUTHORIZED}</li>
     *   <li>403 -- checks if the URL's {@code oe} parameter indicates an
     *       expired signature; if so, throws with
     *       {@link WhatsAppMediaException#HTTP_NOT_FOUND} (reclassified as
     *       {@code MediaNotFoundError}); otherwise throws with
     *       {@link WhatsAppMediaException#HTTP_FORBIDDEN}</li>
     *   <li>404, 410 -- {@link WhatsAppMediaException.Download} with
     *       {@link WhatsAppMediaException#HTTP_NOT_FOUND}</li>
     *   <li>507 -- {@link WhatsAppMediaException.Download} with
     *       {@link WhatsAppMediaException#HTTP_THROTTLE}</li>
     *   <li>other -- {@link WhatsAppMediaException.Download} with the raw
     *       status code</li>
     * </ul>
     *
     * <p>In WA Web, the 403 path also reads the response body to check for
     * "URL signature expired"; since Cobalt uses an {@code InputStream}-based
     * response (body already consumed), only the URL expiration date check
     * is performed here.
     *
     * @implNote WAWebMmsClientMmsDownload.validateMmsResponse
     * @param statusCode  the HTTP status code from the CDN response
     * @param url         the download URL, used for expiry date parsing
     * @throws WhatsAppMediaException.Download always, since this method is only
     *         called for non-OK responses
     */
    private static void validateMmsResponse(int statusCode, String url) throws WhatsAppMediaException.Download {
        // WAWebMmsClientMmsDownload.validateMmsResponse (function y / C)
        if (statusCode == 401) {
            // WAWebMmsClientErrors.MMSUnauthorizedError
            throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_UNAUTHORIZED,
                    "mmsDownload: unauthorized");
        }

        if (statusCode == 403) {
            // WAWebMmsClientMmsDownload.validateMmsResponse — 403 with URL expiry check
            // WAWebMmsCdnUrlValidationUtils.parseCdnUrlParams — extracts "oe" param as hex date
            // If the URL's expiration date has passed, reclassify as MediaNotFoundError
            try {
                var uri = URI.create(url);
                var query = uri.getRawQuery();
                if (query != null) {
                    for (var param : query.split("&")) {
                        var eqIndex = param.indexOf('=');
                        if (eqIndex >= 0 && param.substring(0, eqIndex).equals("oe")) {
                            // WAWebMmsCdnUrlValidationUtils.parseCdnUrlParams — convertHexToDate(oe)
                            var hexValue = param.substring(eqIndex + 1);
                            var expirationEpoch = Long.parseLong(hexValue, 16);
                            // WAWebMmsClientMmsDownload.validateMmsResponse —
                            // toDate(unixTime()) >= expirationDate ? MediaNotFoundError : MMSForbiddenError
                            if (Instant.now().getEpochSecond() >= expirationEpoch) {
                                throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_NOT_FOUND,
                                        "mmsDownload: media not found (URL expired)");
                            }
                            break;
                        }
                    }
                }
            } catch (WhatsAppMediaException.Download e) {
                throw e;
            } catch (Exception _) {
                // ADAPTED: malformed URL or non-hex oe param — fall through to MMSForbiddenError
            }
            // WAWebMmsClientErrors.MMSForbiddenError
            throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_FORBIDDEN,
                    "mmsDownload: forbidden");
        }

        if (statusCode == 404 || statusCode == 410) {
            // WAWebMmsClientErrors.MediaNotFoundError
            throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_NOT_FOUND,
                    "mmsDownload: media not found");
        }

        if (statusCode == 507) {
            // WAWebMmsClientErrors.MMSThrottleError
            throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_THROTTLE,
                    "mmsDownload: throttled");
        }

        // WAWebHttpErrors.HttpStatusCodeError — generic non-OK status
        throw new WhatsAppMediaException.Download(statusCode,
                "mmsDownload: HTTP " + statusCode);
    }

    /**
     * Determines whether a media download exception represents a retryable
     * error. This mirrors the same retry logic as
     * {@link #isRetryable(WhatsAppMediaException.Upload)} but applied to
     * download errors, using the same rules from
     * {@code WAWebMmsClientIsErrorRetryable}:
     * <ul>
     *   <li>No HTTP status code (network error): retryable</li>
     *   <li>401 ({@code MMSUnauthorizedError}): retryable</li>
     *   <li>408 (request timeout): retryable</li>
     *   <li>507 ({@code MMSThrottleError}): <strong>not</strong> retryable</li>
     *   <li>Other 5xx: retryable</li>
     *   <li>All other status codes (404, 410, 403, etc.): not retryable</li>
     * </ul>
     *
     * @implNote WAWebMmsClientIsErrorRetryable.isRetriableStatusCode,
     *           WAWebMmsClientIsErrorRetryable.isErrorRetryable
     * @param exception the download exception to test
     * @return {@code true} if the error is retryable, {@code false} otherwise
     */
    private static boolean isDownloadRetryable(WhatsAppMediaException.Download exception) {
        // WAWebMmsClientIsErrorRetryable.isRetriableStatusCode
        var optStatus = exception.httpStatusCode();
        if (optStatus.isEmpty()) {
            // WAWebMmsClientIsErrorRetryable.isErrorRetryable — network errors are retryable
            return true;
        }
        var status = optStatus.getAsInt();
        // WAWebMmsClientIsErrorRetryable.isRetriableStatusCode — e === 408 ? true : e === 507 ? false : e >= 500
        if (status == 408) {
            return true;
        }
        if (status == 507) {
            return false;
        }
        if (status >= 500) {
            return true;
        }
        // WAWebMmsClientIsErrorRetryable.isErrorRetryable — instanceof MMSUnauthorizedError (401)
        return status == 401;
    }

    /**
     * Checks whether the specified media file exists on the WhatsApp CDN by
     * sending an HTTP HEAD request. If the response status is OK (200), the
     * media is considered to exist. Any non-OK status results in an exception
     * being thrown via {@link #validateMmsResponse(int, String)}.
     *
     * <p>The URL is constructed via {@link #formatDownloadUrl} using mode
     * {@code "auto"}, matching the behavior of the shared HEAD request helper
     * in {@code WAWebMmsClientMmsDownload}.
     *
     * @implNote WAWebMmsClientMmsDownload.mmsCheckExistence
     * @param hostname   the CDN hostname
     * @param mediaType  the media path type
     * @param directPath the CDN direct path, or {@code null}
     * @param encFileHash the base64-encoded encrypted file hash, or {@code null}
     * @throws WhatsAppMediaException.Download if the media does not exist
     *         (non-OK HTTP status) or a network error occurs
     */
    public void checkExistence(
            String hostname,
            MediaPath mediaType,
            String directPath,
            String encFileHash
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientMmsDownload.mmsCheckExistence -> shared HEAD helper b/v
        sendHeadRequest(hostname, mediaType, directPath, encFileHash, "mmsCheckExistence");
    }

    /**
     * Retrieves the encrypted media file size from the WhatsApp CDN by
     * sending an HTTP HEAD request and reading the {@code Content-Length}
     * response header.
     *
     * <p>The URL is constructed via {@link #formatDownloadUrl} using mode
     * {@code "auto"}, matching the behavior of the shared HEAD request helper
     * in {@code WAWebMmsClientMmsDownload}.
     *
     * @implNote WAWebMmsClientMmsDownload.mmsGetEncryptedMediaSize
     * @param hostname    the CDN hostname
     * @param mediaType   the media path type
     * @param directPath  the CDN direct path, or {@code null}
     * @param encFileHash the base64-encoded encrypted file hash, or {@code null}
     * @return the encrypted media file size in bytes
     * @throws WhatsAppMediaException.Download if the Content-Length header is
     *         missing, the server returns a non-OK status, or a network error
     *         occurs
     */
    public long getEncryptedMediaSize(
            String hostname,
            MediaPath mediaType,
            String directPath,
            String encFileHash
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientMmsDownload.mmsGetEncryptedMediaSize -> shared HEAD helper b/v
        var response = sendHeadRequest(hostname, mediaType, directPath, encFileHash, "mmsGetEncryptedMediaSize");

        // WAWebMmsClientMmsDownload.mmsGetEncryptedMediaSize — headers.get("content-length")
        var contentLength = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1L);
        if (contentLength < 0) {
            // WAWebMiscErrors.UnableToGetContentLengthError
            throw new WhatsAppMediaException.Download("Unable to get content length");
        }

        // WAWebMmsClientMmsDownload.mmsGetEncryptedMediaSize — parseInt(contentLength, 10)
        return contentLength;
    }

    /**
     * Sends an HTTP HEAD request to the WhatsApp CDN for the specified media
     * file. This is the shared helper used by both
     * {@link #checkExistence} and {@link #getEncryptedMediaSize}, corresponding
     * to the inner function {@code b} / {@code v} in
     * {@code WAWebMmsClientMmsDownload}.
     *
     * <p>The URL is constructed using the direct path (preferred) or the
     * encrypted file hash (fallback), with mode set to {@code "auto"}. The
     * response status is validated via
     * {@link #validateMmsResponse(int, String)}.
     *
     * @implNote WAWebMmsClientMmsDownload (inner function b/v — shared HEAD helper)
     * @param hostname     the CDN hostname
     * @param mediaType    the media path type
     * @param directPath   the CDN direct path, or {@code null}
     * @param encFileHash  the base64-encoded encrypted file hash, or {@code null}
     * @param functionName the caller function name for error context
     * @return the HTTP response from the HEAD request
     * @throws WhatsAppMediaException.Download if no path is available, the
     *         server returns a non-OK status, or a network error occurs
     */
    private HttpResponse<?> sendHeadRequest(
            String hostname,
            MediaPath mediaType,
            String directPath,
            String encFileHash,
            String functionName
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientMmsDownload (inner function b/v)
        // Construct URL using formatDownloadUrl with mode "auto"
        var url = formatDownloadUrl(hostname, directPath, encFileHash, mediaType, null);

        try (var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            // WAWebMmsClientMmsDownload (inner function b/v) — validateMmsResponse
            if (response.statusCode() != 200) {
                validateMmsResponse(response.statusCode(), url);
            }

            return response;
        } catch (WhatsAppMediaException.Download downloadException) {
            throw downloadException;
        } catch (IOException | InterruptedException exception) {
            // ADAPTED: WAWebMmsClientMmsDownload — network errors mapped to Download exception
            throw new WhatsAppMediaException.Download(functionName + ": network error", exception);
        }
    }

    /**
     * Returns the authentication token for CDN requests.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code authToken: r.attrString("auth")}
     * @return the authentication token, never {@code null}
     */
    public String auth() {
        return auth;
    }

    /**
     * Returns the routes time-to-live in seconds.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code routesExpiryTs: r.attrFutureTime("ttl")},
     *           WAWebQueryMediaConnsJob.queryMediaConn -- {@code ttl: routesExpiryTs - unixTime()}
     * @return the TTL in seconds
     */
    public int ttl() {
        return ttl;
    }

    /**
     * Returns the authentication token time-to-live in seconds.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code authTokenExpiryTs: r.attrFutureTime("auth_ttl")},
     *           WAWebQueryMediaConnsJob.queryMediaConn -- {@code authTTL: authTokenExpiryTs - unixTime()}
     * @return the auth TTL in seconds
     */
    public int authTtl() {
        return authTtl;
    }

    /**
     * Returns the maximum number of CDN buckets for deterministic host
     * selection.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code maxBuckets: r.attrInt("max_buckets")}
     * @return the maximum bucket count
     */
    public int maxBuckets() {
        return maxBuckets;
    }

    /**
     * Returns the maximum number of manual retry attempts for media
     * downloads. Defaults to 3 when the server does not provide a value.
     *
     * @implNote WAMediaConnParser.mediaConnParser --
     *           {@code maxManualRetry: r.maybeAttrInt("max_manual_retry", 0, 4) ?? 3}
     * @return the maximum manual retry count
     */
    public int maxManualRetry() {
        return maxManualRetry;
    }

    /**
     * Returns the maximum number of automatic retry attempts for media
     * downloads. Defaults to 3 when the server does not provide a value.
     *
     * @implNote WAMediaConnParser.mediaConnParser --
     *           {@code maxAutoDownloadRetry: r.maybeAttrInt("max_auto_download_retry", 0, 4) ?? 3}
     * @return the maximum auto download retry count
     */
    public int maxAutoDownloadRetry() {
        return maxAutoDownloadRetry;
    }

    /**
     * Returns the epoch-second timestamp at which this media connection was
     * created.
     *
     * @implNote ADAPTED: WAWebQueryMediaConnsJob.queryMediaConn --
     *           WA Web uses {@code futureUnixTime} for absolute expiry;
     *           Cobalt stores creation time separately
     * @return the creation timestamp in epoch seconds
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Returns the list of CDN host entries available for media operations.
     *
     * @implNote WAMediaConnParser.mediaConnParser -- {@code hosts: c(r)},
     *           WAWebQueryMediaConnsJob.mapParsedMediaConn -- host mapping
     * @return an unmodifiable collection of hosts, never {@code null}
     */
    public SequencedCollection<? extends MediaHost> hosts() {
        return hosts;
    }

    /**
     * Checks whether this media connection's authentication token has
     * expired. A media connection is considered expired when the current
     * time is at or past the authentication token expiration time, which
     * is computed as {@code timestamp + authTtl}.
     *
     * <p>In WA Web, {@code _isExpiredOrMissing()} also returns {@code true}
     * when the data is {@code null}; in Cobalt, a {@code null} media
     * connection is handled by the store's {@code awaitMediaConnection}
     * blocking semantics, so this method only checks the TTL condition.
     *
     * @implNote WAWebMediaHosts._isExpiredOrMissing
     * @return {@code true} if the auth token has expired
     */
    public boolean isExpired() {
        // WAWebMediaHosts._isExpiredOrMissing -- new Date >= e.authExpirationTime
        // authExpirationTime = new Date(queryStartTime.getTime() + authTTL)
        // In Cobalt, timestamp is epoch seconds and authTtl is seconds
        return Instant.now().getEpochSecond() >= timestamp + authTtl;
    }

    /**
     * Checks whether this media connection should be refreshed. A media
     * connection needs refresh when either the routes TTL has elapsed or
     * when 80% of the authentication TTL has elapsed, whichever comes
     * first. This mirrors the proactive refresh strategy used by WA Web
     * to avoid serving requests with stale or nearly-expired credentials.
     *
     * <p>In WA Web, {@code _needsRefresh()} also returns {@code true}
     * when the data is {@code null}; in Cobalt, a {@code null} media
     * connection is handled by the store, so this method only checks the
     * time-based conditions.
     *
     * @implNote WAWebMediaHosts._needsRefresh
     * @return {@code true} if the media connection should be refreshed
     */
    public boolean needsRefresh() {
        // WAWebMediaHosts._needsRefresh
        var now = Instant.now().getEpochSecond();
        // new Date >= e.hostsRefreshTime
        // hostsRefreshTime = new Date(queryStartTime.getTime() + ttl)
        if (now >= timestamp + ttl) {
            return true;
        }
        // var r = Math.floor(t * .8), o = new Date(n.getTime() + r)
        // new Date >= o
        var authRefreshThreshold = (long) Math.floor(authTtl * 0.8);
        return now >= timestamp + authRefreshThreshold;
    }

    /**
     * Returns a string representation of this media connection, including
     * all fields.
     *
     * @implNote NO_WA_BASIS
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "MediaConnection[" +
               "auth=" + auth + ", " +
               "ttl=" + ttl + ", " +
               "authTtl=" + authTtl + ", " +
               "maxBuckets=" + maxBuckets + ", " +
               "maxManualRetry=" + maxManualRetry + ", " +
               "maxAutoDownloadRetry=" + maxAutoDownloadRetry + ", " +
               "timestamp=" + timestamp + ", " +
               "hosts=" + hosts + ']';
    }
}
