package com.github.auties00.cobalt.media;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.exception.WhatsAppMediaException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
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
 *
 * <p>A media connection is the handshake credential needed to upload and
 * download encrypted media (images, videos, audio, documents, stickers)
 * through WhatsApp's CDN. It carries the authentication token, the list of
 * candidate CDN hosts with their supported media types and download buckets,
 * the time-to-live values for the credentials, and the retry budgets to
 * apply when a host is slow or refuses a request.
 *
 * <p>The client fetches a {@code MediaConnection} by sending the IQ stanza
 * returned by {@link #queryNode()}, parses the response via {@link #of(Node)},
 * and then uses {@link #upload(MediaProvider, InputStream)} and
 * {@link #download(MediaProvider)} to move media through the CDN. Callers
 * should consult {@link #isExpired()} and {@link #needsRefresh()} to decide
 * when the connection must be re-queried.
 *
 * @implNote WAMediaConnParser: mediaConnParser. WAWebQueryMediaConnsJob:
 * mapParsedMediaConn, queryMediaConn. WAWebMediaHosts: singleton lifecycle
 * (TTL, refresh, expiry). WAWebMmsClient: upload/download retry orchestration.
 * WAWebMmsClientSelectHost: per-attempt host rotation. WAWebMmsClientMmsUpload
 * and WAWebMmsClientMmsDownload: per-request CDN interaction. This class
 * fuses the roles of all of these modules into a single aggregate since
 * Cobalt stores the parsed media connection in {@code WhatsAppStore} and
 * uses synchronous virtual-thread I/O in place of WA Web's async promise
 * pipelines.
 */
@WhatsAppWebModule(moduleName = "WAMediaConnParser")
@WhatsAppWebModule(moduleName = "WAWebQueryMediaConnsJob")
@WhatsAppWebModule(moduleName = "WAWebMediaHosts")
@WhatsAppWebModule(moduleName = "WAWebMmsClient")
@WhatsAppWebModule(moduleName = "WAWebMmsClientSelectHost")
@WhatsAppWebModule(moduleName = "WAWebMmsClientMmsUpload")
@WhatsAppWebModule(moduleName = "WAWebMmsClientMmsDownload")
@WhatsAppWebModule(moduleName = "WAWebMmsClientFormatUploadUrl")
@WhatsAppWebModule(moduleName = "WAWebMmsClientFormatDownloadUrl")
@WhatsAppWebModule(moduleName = "WAWebMmsClientFormatHashUrl")
@WhatsAppWebModule(moduleName = "WAWebMmsClientIsErrorRetryable")
@WhatsAppWebModule(moduleName = "WAWebMmsCdnUrlValidationUtils")
@WhatsAppWebModule(moduleName = "WABase64UrlSafe")
@WhatsAppWebModule(moduleName = "WAWebWamMediaMetricUtils")
public final class MediaConnection {
    /**
     * The authentication token presented to the CDN on every upload and
     * download request. Without a valid token the CDN refuses to service
     * the request with HTTP 401.
     *
     * @implNote WAMediaConnParser.mediaConnParser: {@code authToken: r.attrString("auth")}.
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String auth;

    /**
     * The routes time-to-live in seconds. After this interval the CDN host
     * list returned by the server may no longer be current and the caller
     * should re-query a fresh {@code MediaConnection}.
     *
     * @implNote WAMediaConnParser.mediaConnParser: {@code routesExpiryTs: r.attrFutureTime("ttl")}.
     * WAWebQueryMediaConnsJob.queryMediaConn: {@code ttl: routesExpiryTs - unixTime()}.
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "queryMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final int ttl;

    /**
     * The authentication token time-to-live in seconds. Once expired the
     * stored {@link #auth} token is no longer accepted by the CDN and the
     * caller must refresh the connection before issuing new requests.
     *
     * @implNote WAMediaConnParser.mediaConnParser: {@code authTokenExpiryTs: r.attrFutureTime("auth_ttl")}.
     * WAWebQueryMediaConnsJob.queryMediaConn: {@code authTTL: authTokenExpiryTs - unixTime()}.
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "queryMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final int authTtl;

    /**
     * The maximum number of deterministic download buckets advertised by
     * the server. This value governs the bucket assignment arithmetic used
     * when vcache aggregation is enabled during download route selection.
     *
     * @implNote WAMediaConnParser.mediaConnParser: {@code maxBuckets: r.attrInt("max_buckets")}.
     * WAWebQueryMediaConnsJob.mapParsedMediaConn: {@code maxBuckets: e.maxBuckets}.
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "mapParsedMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final int maxBuckets;

    /**
     * The maximum number of retry attempts the UI should offer when a user
     * manually re-triggers a failed media download. The server clamps the
     * value to {@code [0, 4]} and defaults to {@code 3}.
     *
     * @implNote WAMediaConnParser.mediaConnParser:
     * {@code maxManualRetry: r.maybeAttrInt("max_manual_retry", 0, 4) ?? 3}.
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final int maxManualRetry;

    /**
     * The maximum number of retry attempts the client should perform when
     * transparently re-downloading a piece of media after a failure. The
     * server clamps the value to {@code [0, 4]} and defaults to {@code 3}.
     *
     * @implNote WAMediaConnParser.mediaConnParser:
     * {@code maxAutoDownloadRetry: r.maybeAttrInt("max_auto_download_retry", 0, 4) ?? 3}.
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final int maxAutoDownloadRetry;

    /**
     * The epoch-second timestamp at which this media connection was parsed.
     * Combined with {@link #ttl} and {@link #authTtl} to compute the
     * absolute expiry times used by {@link #isExpired()} and
     * {@link #needsRefresh()}.
     *
     * @implNote ADAPTED: WAWebQueryMediaConnsJob.queryMediaConn derives
     * {@code routesExpiryTs} and {@code authTokenExpiryTs} inline via
     * {@code futureUnixTime(seconds)}; Cobalt stores the creation
     * timestamp and the raw TTL separately so that the expiry checks stay
     * side-effect free.
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "queryMediaConn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private final long timestamp;

    /**
     * The ordered list of CDN host candidates for media uploads and
     * downloads. Each entry advertises a hostname, a set of supported media
     * types, and (for primary hosts) a nested fallback hostname.
     *
     * @implNote WAMediaConnParser.mediaConnParser: {@code hosts: c(r)} via
     * {@code e.mapChildrenWithTag("host", u)}. WAWebQueryMediaConnsJob.mapParsedMediaConn:
     * {@code hosts: e.hosts.map(...)}.
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "mapParsedMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final SequencedCollection<? extends MediaHost> hosts;

    /**
     * Constructs a new media connection with the specified parsed fields.
     *
     * @implNote WAMediaConnParser.mediaConnParser: aggregates all {@code r.attr*}
     * reads. WAWebQueryMediaConnsJob.queryMediaConn: aggregates the IQ response
     * into the returned {@code mediaConn} object with {@code ttl},
     * {@code authTTL}, {@code queryStartTime}, and host list.
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
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "queryMediaConn",
            adaptation = WhatsAppAdaptation.ADAPTED)
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
     * Builds the IQ stanza that asks the WhatsApp server for a fresh media
     * connection. The returned builder produces a stanza of the form:
     * <pre>{@code
     * <iq to="s.whatsapp.net" xmlns="w:m" type="set">
     *   <media_conn/>
     * </iq>
     * }</pre>
     *
     * <p>The caller sends this stanza through the usual client IQ pipeline
     * and then hands the reply to {@link #of(Node)} to obtain the parsed
     * connection credentials.
     *
     * @implNote WAWebQueryMediaConnsJob.queryMediaConn: IQ stanza
     * construction via {@code wap("iq", {to: S_WHATSAPP_NET, xmlns: "w:m",
     * type: "set", id: generateId()}, wap("media_conn", null))}.
     * @return a node builder representing the media connection query IQ
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "queryMediaConn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static NodeBuilder queryNode() {
        // WAWebQueryMediaConnsJob.queryMediaConn
        // Constructs the w:m IQ "set" stanza with an empty media_conn child

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
     * Parses the {@code media_conn} IQ reply into a usable media connection.
     *
     * <p>Combines the role of three WA Web helpers: the raw attribute
     * decoding of {@code WAMediaConnParser}, the host/field projection of
     * {@code WAWebQueryMediaConnsJob.mapParsedMediaConn}, and the TTL
     * normalisation performed inside {@code queryMediaConn} (which
     * subtracts the current time from the future expiry timestamps).
     *
     * @implNote WAMediaConnParser.mediaConnParser: raw attribute parsing.
     * WAWebQueryMediaConnsJob.mapParsedMediaConn: host mapping and field
     * projection. WAWebQueryMediaConnsJob.queryMediaConn: TTL normalisation
     * via {@code routesExpiryTs - unixTime()}.
     * @param response the IQ response node containing a {@code media_conn}
     *                 child
     * @return the parsed media connection
     * @throws IllegalArgumentException if the response does not contain a
     *         valid {@code media_conn} child node
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob",
            exports = {"mapParsedMediaConn", "queryMediaConn"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static MediaConnection of(Node response) {
        // WAMediaConnParser.mediaConnParser
        // Unwraps the required media_conn child before reading its attributes

        var mediaConn = response.getRequiredChild("media_conn");

        // WAMediaConnParser.mediaConnParser
        // Reads the mandatory CDN authentication token string

        var auth = mediaConn.getRequiredAttributeAsString("auth");

        // WAMediaConnParser.mediaConnParser
        // Reads the raw routes TTL; WA Web stores it as a future unix time and
        // later subtracts unixTime() to recover the seconds-to-live value

        var ttl = mediaConn.getAttributeAsInt("ttl", 0);

        // WAMediaConnParser.mediaConnParser
        // Reads the raw auth token TTL; WA Web uses the same future-time trick
        // before subtracting unixTime() in queryMediaConn

        var authTtl = mediaConn.getAttributeAsInt("auth_ttl", 0);

        // WAMediaConnParser.mediaConnParser
        // Reads the maximum bucket count for deterministic download routing

        var maxBuckets = mediaConn.getAttributeAsInt("max_buckets", 0);

        // WAMediaConnParser.mediaConnParser
        // Reads the clamped max_manual_retry attribute falling back to 3

        var maxManualRetry = clampOptionalInt(
                mediaConn.getAttributeAsInt("max_manual_retry", (Integer) null),
                0, 4, 3
        );

        // WAMediaConnParser.mediaConnParser
        // Reads the clamped max_auto_download_retry attribute falling back to 3

        var maxAutoDownloadRetry = clampOptionalInt(
                mediaConn.getAttributeAsInt("max_auto_download_retry", (Integer) null),
                0, 4, 3
        );

        // WAMediaConnParser.mediaConnParser
        // Maps each host child node into a MediaHost record via parseHost

        var hosts = mediaConn.streamChildren("host")
                .map(MediaConnection::parseHost)
                .toList();

        // WAWebQueryMediaConnsJob.queryMediaConn
        // Records the parse time so the expiry checks can compute the
        // absolute deadlines from the TTL seconds

        var timestamp = Instant.now().getEpochSecond();

        return new MediaConnection(auth, ttl, authTtl, maxBuckets, maxManualRetry, maxAutoDownloadRetry, timestamp, hosts);
    }

    /**
     * Parses a single {@code host} child node into a {@link MediaHost}
     * record.
     *
     * <p>The host is classified as {@link MediaHost.Fallback} when its
     * {@code type} attribute equals {@code "fallback"}, otherwise as
     * {@link MediaHost.Primary}. Primary hosts may carry a nested
     * fallback hostname derived from the {@code fallback_hostname},
     * {@code fallback_ip4}, and {@code fallback_ip6} attributes.
     *
     * @implNote WAMediaConnParser local function {@code u} (host parser)
     * combined with WAWebQueryMediaConnsJob.mapParsedMediaConn host mapping
     * and type classification.
     * @param hostNode the host child node
     * @return the parsed media host
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "mapParsedMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MediaHost parseHost(Node hostNode) {
        // WAMediaConnParser.mediaConnParser
        // Reads the mandatory hostname attribute of this host entry

        var hostname = hostNode.getRequiredAttributeAsString("hostname");

        // WAMediaConnParser.mediaConnParser
        // Collects the optional IPv4 and IPv6 advertised by the server

        var ips = new ArrayList<String>();
        hostNode.getAttributeAsString("ip4").ifPresent(ips::add);
        hostNode.getAttributeAsString("ip6").ifPresent(ips::add);

        // WAMediaConnParser.mediaConnParser
        // Parses the download and upload media-type whitelists advertised
        // by the host, defaulting to the full SERVER_MEDIA set when absent

        var downloadTypes = parseMediaTypes(hostNode, "download");
        var uploadTypes = parseMediaTypes(hostNode, "upload");

        // WAMediaConnParser.mediaConnParser
        // Parses the download_buckets list whose children are integer
        // bucket identifiers tagged as their numeric value

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

        // WAMediaConnParser.mediaConnParser
        // Determines whether this host is a fallback-class host; primary is
        // the default when the type attribute is missing

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
            // WAMediaConnParser.mediaConnParser
            // Extracts the optional nested fallback hostname and IPs for a
            // primary host so that selectHost can rotate to them on failure

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
     * Parses the media type whitelist declared under a host's
     * {@code download} or {@code upload} child node.
     *
     * <p>When the child is present each inner node's tag is mapped to a
     * {@link MediaPath} constant via {@link MediaPath#ofId(String)}.
     * When the child is absent the whitelist defaults to every known
     * {@code SERVER_MEDIA} type, then a small set of types that never
     * appear in CDN routing ({@code kyc-id}, the novi placeholders,
     * {@code thumbnail-gif}, and {@code xma-image}) is filtered out,
     * mirroring WA Web's {@code compactMap(types, castToServerMediaType)}.
     *
     * @implNote WAMediaConnParser local function {@code d}: media type list
     * parser that defaults to {@code SERVER_MEDIA} when the child is absent.
     * WAWebQueryMediaConnsJob.mapParsedMediaConn inner function {@code t}:
     * applies {@code compactMap(types, _)} which filters out {@code null}
     * returns from the media type validator.
     * @param hostNode    the host node
     * @param description the child node description ({@code "download"} or
     *                    {@code "upload"})
     * @return the set of supported media paths
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "mapParsedMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Set<MediaPath> parseMediaTypes(Node hostNode, String description) {
        // WAMediaConnParser.mediaConnParser
        // Iterates the tagged children of the download/upload sub-node and
        // converts their tags into MediaPath constants

        return hostNode.getChild(description)
                .map(typesNode -> {
                    var result = new LinkedHashSet<MediaPath>();
                    typesNode.streamChildren()
                            .map(Node::description)
                            .forEach(tag -> MediaPath.ofId(tag).ifPresent(result::add));

                    // WAWebQueryMediaConnsJob.mapParsedMediaConn
                    // Mirrors the compactMap(types, castToServerMediaType)
                    // filter which drops "kyc-id", "novi-image", "novi-video",
                    // "thumbnail-gif", and "xma-image" from host routing

                    result.remove(MediaPath.KYC_ID);
                    result.remove(MediaPath.NOVI_IMAGE);
                    result.remove(MediaPath.NOVI_VIDEO);
                    result.remove(MediaPath.THUMBNAIL_GIF);
                    result.remove(MediaPath.XMA_IMAGE);
                    return Collections.unmodifiableSet(result);
                })
                .orElseGet(() -> {
                    // WAMediaConnParser.mediaConnParser
                    // Defaults to the full SERVER_MEDIA set, minus the
                    // non-routable types that compactMap would drop

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
     * default value when the input is {@code null} or falls outside the
     * range.
     *
     * <p>Mirrors WA Web's {@code r.maybeAttrInt(name, min, max) ?? default}
     * idiom which returns {@code null} when the attribute is absent or out
     * of range and then defaults via the nullish coalescing operator.
     *
     * @implNote WAMediaConnParser.mediaConnParser:
     * {@code r.maybeAttrInt("max_manual_retry", 0, 4) ?? 3}.
     * @param value        the parsed integer value, or {@code null}
     * @param min          the minimum allowed value (inclusive)
     * @param max          the maximum allowed value (inclusive)
     * @param defaultValue the default value when input is {@code null} or
     *                     out of range
     * @return the clamped value, or the default
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static int clampOptionalInt(Integer value, int min, int max, int defaultValue) {
        if (value == null || value < min || value > max) {
            return defaultValue;
        }
        return value;
    }

    /**
     * The maximum number of retry attempts for media upload and download
     * operations.
     *
     * <p>Matches the module-level {@code e = 4} constant in
     * {@code WAWebMmsClientSelectHost} and the {@code retries: 3} entry of
     * {@code WAWebMmsClientMmsBackoffOptions} (3 retries add up to 4 total
     * attempts indexed from {@code 0} through {@code 3}).
     *
     * @implNote WAWebMmsClientSelectHost: module-level {@code var e = 4}.
     * WAWebMmsClientMmsBackoffOptions: {@code retries: 3}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientSelectHost", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int MAX_ATTEMPT_COUNT = 4;

    /**
     * Picks the CDN hostname that should be used for the current retry
     * attempt of a media upload or download.
     *
     * <p>Implements the exact host rotation strategy from
     * {@code WAWebMmsClientSelectHost}:
     * <ol>
     *   <li>If the previous attempt made progress on a host, reuse that
     *       host because the connection was functional.</li>
     *   <li>On the first two attempts ({@code attemptCount <= 1}), stick
     *       with the initially selected host.</li>
     *   <li>On the last attempt, try the fallback-class host if one
     *       exists.</li>
     *   <li>If the last host used is the selected host and the selected
     *       host has a nested fallback hostname, try that nested
     *       fallback.</li>
     *   <li>Otherwise prefer the fallback host when available, falling
     *       back to the originally selected host.</li>
     * </ol>
     *
     * @implNote WAWebMmsClientSelectHost.default.
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
    @WhatsAppWebExport(moduleName = "WAWebMmsClientSelectHost", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    static String selectHost(
            MediaHost selectedHost,
            MediaHost fallbackHost,
            String lastHostUsed,
            int attemptCount,
            boolean lastFetchMadeProgress
    ) {
        // WAWebMmsClientSelectHost.default
        // Resolves the candidate hostnames from the primary and fallback hosts

        var selectedHostname = selectedHost != null ? selectedHost.hostname() : null;
        var fallbackHostname = fallbackHost != null ? fallbackHost.hostname() : null;

        // WAWebMmsClientSelectHost.default
        // If the previous attempt made progress on some host, keep using it
        // because the connection is clearly working

        if (lastFetchMadeProgress && lastHostUsed != null) {
            return lastHostUsed;
        }

        // WAWebMmsClientSelectHost.default
        // On the first two attempts (n <= 1), use the initial selected host

        if (attemptCount <= 1) {
            return selectedHostname;
        }

        // WAWebMmsClientSelectHost.default
        // On the last attempt, try the fallback-class host if one is available

        if (attemptCount == MAX_ATTEMPT_COUNT - 1 && fallbackHostname != null) {
            return fallbackHostname;
        }

        // WAWebMmsClientSelectHost.default
        // If the previous attempt went to the selected host and that host
        // advertised a nested fallback hostname, rotate to the nested fallback

        if (lastHostUsed != null
                && lastHostUsed.equals(selectedHostname)
                && selectedHost != null
                && selectedHost.fallbackHostname().isPresent()) {
            return selectedHost.fallbackHostname().get();
        }

        // WAWebMmsClientSelectHost.default
        // Otherwise prefer the fallback host, defaulting to the selected one

        return fallbackHostname != null ? fallbackHostname : selectedHostname;
    }

    /**
     * Uploads a media payload to the WhatsApp CDN on behalf of the given
     * provider.
     *
     * <p>Resolves the initial CDN host through {@link MediaHost#routeSelection}
     * and rotates across candidate hosts with {@link #selectHost} on each
     * retry, mirroring the exponential-retry strategy of
     * {@code WAWebMmsClient.upload}. On success the provider's media
     * metadata (plaintext and encrypted SHA-256 hashes, media key, direct
     * path, URL, byte size, and key timestamp) is written back through the
     * provider setters so that the caller can build the outgoing message
     * protobuf; if the provider is an {@link ExternalBlobReference}, the
     * server-returned handle is also stored.
     *
     * @implNote WAWebMmsClient.upload: retry loop and selectHost rotation.
     * WAWebMmsClientMmsUpload.default: per-request HTTP POST. Upload
     * metadata writeback corresponds to the fields of the
     * {@code buildExternalBlobReference} payload produced by
     * {@code WAWebSyncdMMSUpload} for app-state sync.
     * @param provider    the media provider describing the media type and
     *                    receiving the upload metadata
     * @param inputStream the input stream containing the media content
     * @return {@code true} if the upload succeeded
     * @throws WhatsAppMediaException.Upload if no host could service the
     *         upload, a non-retryable HTTP error occurred (413, 415, 507),
     *         or an I/O error occurred
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClient", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
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

                // WAWebMmsClient.upload
                // Delegates to routeSelection to pick the initial primary and
                // fallback hosts for the upload media type

                var hostList = hosts instanceof List<? extends MediaHost> list ? list : List.copyOf(hosts);
                var route = MediaHost.routeSelection(
                        Operation.UPLOAD,
                        provider.mediaPath(),
                        hostList,
                        fileEncSha256 != null ? Base64.getEncoder().encodeToString(fileEncSha256) : null,
                        maxBuckets > 0 ? maxBuckets : null,
                        false
                );

                // WAWebMmsClient.upload
                // Initialises the retry-loop state: h (last host used) and
                // _ (last fetch made progress) both start unset

                String lastHostUsed = null;
                var lastFetchMadeProgress = false;
                for (var attemptCount = 0; attemptCount < MAX_ATTEMPT_COUNT; attemptCount++) {
                    // WAWebMmsClientSelectHost.default
                    // Chooses the hostname to contact for this attempt based on
                    // route selection and the outcome of the previous attempt

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
                    lastHostUsed = hostname;

                    try {
                        // WAWebMmsClientMmsUpload.default
                        // Sends the HTTP POST with the encrypted payload and
                        // returns the validated JSON response

                        var uploadResult = tryUpload(client, hostname, path.get(), provider.mediaPath(), fileEncSha256, fileSha256, tempFile);

                        // WAWebMmsClientMmsUpload.default
                        // Pulls directPath, url, and handle out of the JSON
                        // response after the standard validator has approved it

                        var directPath = uploadResult.getString("direct_path");
                        var url = uploadResult.getString("url");
                        var handle = uploadResult.getString("handle");

                        // WAWebMmsClient.upload
                        // Stores the upload outcome on the provider so the
                        // caller can build the message protobuf

                        provider.setMediaSha256(fileSha256);
                        provider.setMediaEncryptedSha256(fileEncSha256);
                        provider.setMediaKey(mediaKey);
                        provider.setMediaSize(fileLength);
                        provider.setMediaDirectPath(directPath);
                        provider.setMediaUrl(url);
                        provider.setMediaKeyTimestamp(timestamp);
                        if (provider instanceof ExternalBlobReference externalBlobReference) {
                            // WAWebSyncdMMSUpload.buildExternalBlobReference
                            // Carries the server-assigned handle on the
                            // external blob reference used for app-state sync

                            externalBlobReference.setHandle(handle);
                        }

                        return true;
                    } catch (WhatsAppMediaException.Upload uploadException) {
                        // WAWebMmsClientIsErrorRetryable.isErrorRetryable
                        // Stops retrying on non-retryable errors (413, 415,
                        // 507, and most 4xx); 401, 408, and 5xx are retryable

                        if (!isRetryable(uploadException)) {
                            throw uploadException;
                        }

                        // WAWebMmsClient.upload
                        // Resets lastFetchMadeProgress so selectHost rotates
                        // to a different host on the next attempt

                        lastFetchMadeProgress = false;
                    }
                }

                throw new WhatsAppMediaException.Upload("Cannot upload media: no hosts available");
            } finally {
                // Deletes the temp file used to buffer the encrypted payload
                // before POSTing it; this is a Java resource adaptation with
                // no direct WA Web counterpart

                Files.deleteIfExists(tempFile);
            }
        } catch (WhatsAppMediaException.Upload uploadException) {
            throw uploadException;
        } catch (IOException exception) {
            throw new WhatsAppMediaException.Upload("Cannot upload media", exception);
        }
    }

    /**
     * Performs one HTTP POST upload of an encrypted media payload against a
     * single CDN host.
     *
     * <p>The target URL follows the hash-URL pattern of
     * {@code WAWebMmsClientFormatHashUrl.default}: the encrypted file hash
     * is URL-safe base64 encoded (padding preserved) and appended to the
     * media path segment, while the authentication token, a token duplicate,
     * a random {@code media_id} value, and an optional
     * {@code server_transcode} flag are appended as query parameters by
     * {@code WAWebMmsClientFormatUploadUrl.default}.
     *
     * <p>On a non-200 response this method raises a
     * {@link WhatsAppMediaException.Upload} with the HTTP status code
     * preserved, mirroring the error mapping performed inside
     * {@code WAWebMmsClientMmsUpload.default}:
     * <ul>
     *   <li>401: {@code MMSUnauthorizedError}</li>
     *   <li>413: {@code MediaTooLargeError}</li>
     *   <li>415: {@code MediaInvalidError} (hash mismatch)</li>
     *   <li>507: {@code MMSThrottleError}</li>
     *   <li>other: {@code HttpStatusCodeError}</li>
     * </ul>
     *
     * <p>The response JSON must contain non-null, non-empty
     * {@code direct_path} and {@code url} fields, matching the standard
     * {@code u()} response validator in the WA Web module.
     *
     * @implNote WAWebMmsClientMmsUpload.default: HTTP POST and status code
     * mapping. WAWebMmsClientFormatUploadUrl.default: query parameter
     * assembly. WAWebMmsClientFormatHashUrl.default: URL path construction.
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
    @WhatsAppWebExport(moduleName = "WAWebMmsClientMmsUpload", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebMmsClientFormatUploadUrl", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebMmsClientFormatHashUrl", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private JSONObject tryUpload(HttpClient client, String hostname, String path, MediaPath mediaPath, byte[] fileEncSha256, byte[] fileSha256, Path body) throws WhatsAppMediaException.Upload {
        try {
            // WAWebMmsClientFormatHashUrl.default
            // Encodes the encrypted hash (or the plaintext hash when
            // unavailable) as URL-safe base64 preserving padding characters

            var token = Base64.getUrlEncoder()
                    .encodeToString(Objects.requireNonNullElse(fileEncSha256, fileSha256));

            // WAWebMmsClientFormatHashUrl.default
            // Builds the path portion "https://<host>/<path>/<hash-token>"

            var basePath = "https://" + hostname + "/" + path + "/" + token;

            // WAWebMmsClientFormatUploadUrl.default
            // Assembles the upload query parameters in the same order as
            // WA Web: auth, token, media_id, optional server_transcode

            var queryParams = new LinkedHashMap<String, String>();
            queryParams.put("auth", this.auth);
            queryParams.put("token", token);

            // WAWebWamMediaMetricUtils.generateMediaEventId
            // Random media_id used to correlate metrics across retries

            queryParams.put("media_id", String.valueOf(generateMediaId()));

            // WAWebMmsClientFormatUploadUrl.default
            // Requests server-side transcoding for newsletter video uploads
            // to match the ChannelVideoServerTranscodeGating behaviour

            if (mediaPath == MediaPath.NEWSLETTER_VIDEO) {
                queryParams.put("server_transcode", "1");
            }

            // WAWebMmsClientFormatHashUrl.default
            // Serializes the query parameters exactly as URLSearchParams does,
            // filtering out any null values before encoding

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

            // WAWebMmsClientMmsUpload.default
            // Maps each non-OK HTTP status code to the matching
            // WAWebMmsClientErrors class from the upstream module

            if (response.statusCode() != 200) {
                var statusCode = response.statusCode();
                var message = switch (statusCode) {
                    case 401 -> "mmsUpload: unauthorized";
                    case 413 -> "mmsUpload: media too large";
                    case 415 -> "mmsUpload: hash mismatch";
                    case 507 -> "mmsUpload: throttled";
                    default -> "mmsUpload: HTTP " + statusCode;
                };
                throw new WhatsAppMediaException.Upload(statusCode, message);
            }

            // WAWebMmsClientMmsUpload.default
            // Parses the JSON body and runs the standard validator which
            // rejects responses missing direct_path or url

            var jsonObject = JSON.parseObject(response.body());
            var directPath = jsonObject != null ? jsonObject.getString("direct_path") : null;
            var url = jsonObject != null ? jsonObject.getString("url") : null;
            if (directPath == null || directPath.isEmpty()) {
                throw new WhatsAppMediaException.Upload("mmsUpload: missing direct_path");
            }
            if (url == null || url.isEmpty()) {
                throw new WhatsAppMediaException.Upload("mmsUpload: missing url");
            }
            return jsonObject;
        } catch (WhatsAppMediaException.Upload upload) {
            throw upload;
        } catch (IOException | InterruptedException exception) {
            // ADAPTED: WAWebMmsClientMmsUpload.default
            // WA Web treats TypeError (network failure) as retryable; Cobalt
            // wraps any I/O failure in Upload without a status code so that
            // isRetryable can classify it as retryable via httpStatusCode().isEmpty()

            throw new WhatsAppMediaException.Upload("mmsUpload: network error", exception);
        }
    }

    /**
     * Produces a random media event identifier for telemetry and request
     * correlation.
     *
     * <p>The returned value is a positive long in the range {@code [1,
     * Number.MAX_SAFE_INTEGER]}, matching the range used by WA Web's
     * metric utility so that server-side analytics can de-duplicate events
     * across retries.
     *
     * @implNote WAWebWamMediaMetricUtils.generateMediaEventId:
     * {@code 1 + Math.floor(Number.MAX_SAFE_INTEGER * Math.random())}.
     * @return a random positive long value suitable for use as a media ID
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMediaMetricUtils", exports = "generateMediaEventId",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static long generateMediaId() {
        // WAWebWamMediaMetricUtils.generateMediaEventId
        // Generates 1 + floor(MAX_SAFE_INTEGER * random()) where
        // MAX_SAFE_INTEGER is 9007199254740991

        return 1 + (long) Math.floor(9007199254740991.0 * Math.random());
    }

    /**
     * Serializes a map of query parameters into a URL query string.
     *
     * <p>Mirrors WA Web's {@code new URLSearchParams(params).toString()}
     * idiom: {@code null} entries are skipped and every remaining key/value
     * pair is percent-encoded using {@code application/x-www-form-urlencoded}
     * rules.
     *
     * @implNote WAWebMmsClientFormatHashUrl.default:
     * {@code new URLSearchParams(filteredQuery).toString()}.
     * @param params the query parameter map
     * @return the encoded query string with leading {@code ?}, or an empty
     *         string if no parameters remain after filtering
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientFormatHashUrl", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static String encodeQueryString(Map<String, String> params) {
        // WAWebMmsClientFormatHashUrl.default
        // Iterates the parameter map mimicking URLSearchParams toString():
        // skips null values and percent-encodes each key and value

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
     * Tests whether a media upload error is worth retrying with a different
     * host.
     *
     * <p>Mirrors {@code WAWebMmsClientIsErrorRetryable}:
     * <ul>
     *   <li>Missing status code (network error): retryable</li>
     *   <li>401 ({@code MMSUnauthorizedError}): retryable</li>
     *   <li>408 (request timeout): retryable</li>
     *   <li>507 ({@code MMSThrottleError}): not retryable</li>
     *   <li>Other 5xx: retryable</li>
     *   <li>All other status codes (413, 415, and the rest): not retryable</li>
     * </ul>
     *
     * @implNote WAWebMmsClientIsErrorRetryable.isRetriableStatusCode,
     * WAWebMmsClientIsErrorRetryable.isErrorRetryable.
     * @param exception the upload exception to test
     * @return {@code true} if the error is retryable, {@code false} otherwise
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientIsErrorRetryable",
            exports = {"isRetriableStatusCode", "isErrorRetryable"},
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isRetryable(WhatsAppMediaException.Upload exception) {
        // WAWebMmsClientIsErrorRetryable.isErrorRetryable
        // Missing HTTP status means network-level failure (fetch TypeError)
        // and is treated as retryable

        var optStatus = exception.httpStatusCode();
        if (optStatus.isEmpty()) {
            return true;
        }
        var status = optStatus.getAsInt();

        // WAWebMmsClientIsErrorRetryable.isRetriableStatusCode
        // 408 is an explicit retryable case, 507 (throttle) is always fatal,
        // any other 5xx is retryable

        if (status == 408) {
            return true;
        }
        if (status == 507) {
            return false;
        }
        if (status >= 500) {
            return true;
        }

        // WAWebMmsClientIsErrorRetryable.isErrorRetryable
        // 401 (MMSUnauthorizedError) is retryable so that selectHost can
        // rotate to a fresh host for re-auth

        return status == 401;
    }

    /**
     * Downloads a media payload from the WhatsApp CDN for the given
     * provider.
     *
     * <p>First tries the provider's cached static media URL (if any), then
     * resolves a fresh host through {@link MediaHost#routeSelection} and
     * rotates across candidate hosts with {@link #selectHost} on each
     * retry, mirroring {@code WAWebMmsClient.download}. Non-retryable
     * errors (404, 410, 507, and the rest of 4xx) propagate immediately;
     * retryable errors (401, 408, 5xx, network failures) advance the loop
     * to the next attempt.
     *
     * @implNote WAWebMmsClient.download: retry loop and selectHost rotation.
     * WAWebMmsClientMmsDownload.mms4Download: per-request HTTP GET.
     * WAWebMmsClientFormatDownloadUrl.default: URL construction.
     * @param provider the media provider describing the media type and
     *                 carrying the direct path and optional URL
     * @return an {@link InputStream} containing the downloaded media content
     * @throws WhatsAppMediaException.Download if no host could service the
     *         download, the direct path is missing, or a non-retryable HTTP
     *         error occurred (404, 410, 507, etc.)
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClient", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public InputStream download(MediaProvider provider) throws WhatsAppMediaException {
        Objects.requireNonNull(provider, "provider cannot be null");

        // WAWebMmsClient.download
        // Attempts the cached static URL first so that previously-issued
        // CDN URLs are reused when still fresh

        var defaultUploadUrl = provider.mediaUrl();
        if (defaultUploadUrl.isPresent()) {
            try {
                return tryDownload(provider, defaultUploadUrl.get());
            } catch (WhatsAppMediaException.Download downloadException) {
                // WAWebMmsClient.download
                // Non-retryable static-URL failures propagate; retryable ones
                // fall through to the host-based download path below

                if (!isDownloadRetryable(downloadException)) {
                    throw downloadException;
                }
            }
        }

        // WAWebMmsClientMmsDownload.mms4Download
        // Resolves the directPath and encrypted hash used to construct the
        // CDN URL from the provider metadata

        var defaultDirectPath = provider.mediaDirectPath()
                .orElse(null);
        var encFileHash = provider.mediaEncryptedSha256()
                .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                .orElse(null);
        var mediaType = provider.mediaPath();

        // WAWebMmsClientMmsDownload.mms4Download
        // Rejects the download when neither directPath nor encFilehash is
        // available, since there is no valid URL to construct

        if ((defaultDirectPath == null || defaultDirectPath.isEmpty())
                && (encFileHash == null || encFileHash.isEmpty())) {
            throw new WhatsAppMediaException.Download(
                    "No staticUrl, directPath, or encFilehash available for download");
        }

        // WAWebMmsClient.download
        // Delegates to routeSelection to find the primary and fallback
        // hosts that support downloading this media type

        var hostList = hosts instanceof List<? extends MediaHost> list ? list : List.copyOf(hosts);
        var route = MediaHost.routeSelection(
                Operation.DOWNLOAD,
                mediaType,
                hostList,
                encFileHash,
                maxBuckets > 0 ? maxBuckets : null,
                false
        );

        // WAWebMediaHostsRouteSelection.routeSelection
        // Computes the download bucket that WA Web would attach to the
        // selected host via setSelectedBucket(p); fallback hosts never
        // carry a bucket so only the selected host's bucket is stored

        var selectedBucket = computeSelectedBucket(encFileHash);
        var selectedHostname = route.selectedHost()
                .map(MediaHost::hostname)
                .orElse(null);

        // WAWebMmsClient.download
        // Initialises the retry loop state: b (last host used) and h
        // (last fetch made progress) both start unset

        String lastHostUsed = null;
        var lastFetchMadeProgress = false;
        for (var attemptCount = 0; attemptCount < MAX_ATTEMPT_COUNT; attemptCount++) {
            // WAWebMmsClientSelectHost.default
            // Picks the host to contact for this attempt based on the
            // route result and the outcome of the previous attempt

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
            lastHostUsed = hostname;

            // WAWebMmsClientSelectHost
            // Bucket is only attached when the chosen hostname matches the
            // original selected host; fallback hosts never carry a bucket

            var hostBucket = hostname.equals(selectedHostname) ? selectedBucket : null;

            // WAWebMmsClientFormatDownloadUrl.default
            // Builds the full download URL from the host, directPath or
            // encFilehash, media type, and bucket

            var downloadUrl = formatDownloadUrl(
                    hostname, defaultDirectPath, encFileHash, mediaType, hostBucket
            );

            try {
                // WAWebMmsClientMmsDownload.mms4Download
                // Issues the GET request and returns the streaming response

                return tryDownload(provider, downloadUrl);
            } catch (WhatsAppMediaException.Download downloadException) {
                // WAWebMmsClientIsErrorRetryable.isErrorRetryable
                // Same retry rules as upload: 401/408/5xx and network errors
                // are retryable, 404/410/413/415/507 are fatal

                if (!isDownloadRetryable(downloadException)) {
                    throw downloadException;
                }

                // WAWebMmsClient.download
                // Clears lastFetchMadeProgress so selectHost rotates to a
                // different host on the next attempt

                lastFetchMadeProgress = false;
            }
        }

        throw new WhatsAppMediaException.Download("Cannot download media: no hosts available");
    }

    /**
     * Chooses between the direct-path and hash-based URL formats and
     * returns the final CDN download URL.
     *
     * <p>When a direct path is available, the URL is built as
     * {@code https://{hostname}{directPath}?hash=...&_nc_cat=...&mode=...&mms-type=...&__wa-mms=}
     * via {@link #buildDirectPathUrl}. When only the encrypted file hash is
     * available, the URL is built as
     * {@code https://{hostname}/{path}/{urlSafeBase64(encFileHash)}?mode=...&__wa-mms=}
     * via {@link #formatHashUrl}.
     *
     * @implNote WAWebMmsClientFormatDownloadUrl.default.
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
    @WhatsAppWebExport(moduleName = "WAWebMmsClientFormatDownloadUrl", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private String formatDownloadUrl(
            String hostname,
            String directPath,
            String encFileHash,
            MediaPath mediaType,
            Integer downloadBucket
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientFormatDownloadUrl.default
        // Projects the server media type id so it can be attached as the
        // mms-type query parameter

        var mediaTypeId = mediaType.id().orElse(null);
        if (directPath != null && !directPath.isEmpty()) {
            // WAWebMmsClientFormatDownloadUrl.default
            // Direct-path branch: query carries mode, mms-type, and the
            // __wa-mms marker (WAWebSharedConstants)

            var query = new LinkedHashMap<String, String>();
            query.put("mode", "auto");
            query.put("mms-type", mediaTypeId);
            query.put("__wa-mms", "");
            return buildDirectPathUrl(hostname, directPath, encFileHash, downloadBucket, query);
        }

        // WAWebMmsClientFormatDownloadUrl.default
        // Hash-URL fallback requires at least the encrypted file hash

        if (encFileHash == null || encFileHash.isEmpty()) {
            throw new WhatsAppMediaException.Download(
                    "No direct path or encFilehash available for download, abort");
        }

        // WAWebMmsClientFormatDownloadUrl.default
        // Hash-URL query: only mode and the __wa-mms marker are attached

        var query = new LinkedHashMap<String, String>();
        query.put("mode", "auto");
        query.put("__wa-mms", "");
        return formatHashUrl(hostname, mediaType, encFileHash, query);
    }

    /**
     * Builds a direct-path download URL from the CDN hostname, direct path,
     * and query parameters.
     *
     * <p>The URL appends these query parameters in order:
     * <ul>
     *   <li>{@code hash}: URL-safe base64 encoding of the encrypted file
     *       hash when available</li>
     *   <li>{@code _nc_cat}: the download bucket number when available</li>
     *   <li>Every non-null entry from {@code query}</li>
     * </ul>
     *
     * <p>A hostname security check rejects direct paths whose embedded URL
     * resolves to a different host, preventing a malicious server from
     * redirecting the client to an arbitrary endpoint.
     *
     * @implNote WAWebMmsClientFormatDownloadUrl local function {@code c}.
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
    @WhatsAppWebExport(moduleName = "WAWebMmsClientFormatDownloadUrl", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String buildDirectPathUrl(
            String hostname,
            String directPath,
            String encFileHash,
            Integer downloadBucket,
            Map<String, String> query
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientFormatDownloadUrl.default
        // Parses the directPath as a URL relative to "https://<hostname>" so
        // that any embedded query string is preserved

        var baseUri = URI.create("https://" + hostname).resolve(directPath);

        // WAWebMmsClientFormatDownloadUrl.default
        // Rejects the URL if the resolved host does not match the expected
        // hostname, guarding against a malicious directPath redirect

        if (!baseUri.getHost().equals(hostname)) {
            throw new WhatsAppMediaException.Download("malicious directPath");
        }

        // ADAPTED: WAWebMmsClientFormatDownloadUrl.default
        // WA Web uses URLSearchParams to parse the existing query; Cobalt
        // splits the raw query manually because java.net.URI does not
        // expose URLSearchParams semantics

        var params = new LinkedHashMap<String, String>();
        var existingQuery = baseUri.getRawQuery();
        if (existingQuery != null && !existingQuery.isEmpty()) {
            for (var param : existingQuery.split("&")) {
                var eqIndex = param.indexOf('=');
                if (eqIndex >= 0) {
                    params.put(param.substring(0, eqIndex), param.substring(eqIndex + 1));
                } else {
                    params.put(param, "");
                }
            }
        }

        // WAWebMmsClientFormatDownloadUrl.default
        // Attaches the URL-safe base64 encrypted hash as the "hash" query
        // parameter when present

        if (encFileHash != null && !encFileHash.isEmpty()) {
            params.put("hash", urlSafeBase64(encFileHash));
        }

        // WAWebMmsClientFormatDownloadUrl.default
        // Attaches the download bucket as the "_nc_cat" query parameter

        if (downloadBucket != null) {
            params.put("_nc_cat", downloadBucket.toString());
        }

        // WAWebMmsClientFormatDownloadUrl.default
        // Copies the caller-supplied extra query parameters, skipping nulls

        for (var entry : query.entrySet()) {
            if (entry.getValue() != null) {
                params.put(entry.getKey(), entry.getValue());
            }
        }

        // WAWebMmsClientFormatDownloadUrl.default
        // Reassembles the URL as scheme + host + path + encoded query

        return "https://" + baseUri.getHost() + baseUri.getRawPath()
                + encodeQueryString(params);
    }

    /**
     * Builds a hash-based download URL from the CDN hostname, media type
     * path segment, encrypted file hash, and query parameters.
     *
     * <p>The resulting URL has the form
     * {@code https://{hostname}/{path}/{urlSafeBase64(encFileHash)}?{query}}
     * where {@code path} is read from {@link MediaPath#path()} and
     * {@code encFileHash} is URL-safe base64 encoded.
     *
     * @implNote WAWebMmsClientFormatHashUrl.default.
     * @param hostname    the CDN hostname
     * @param mediaType   the media path type (must have a non-null path)
     * @param encFileHash the base64-encoded encrypted file hash
     * @param query       the additional query parameters to include
     * @return the fully constructed hash-based download URL
     * @throws WhatsAppMediaException.Download if the media type has no path
     *         for hash URL construction
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientFormatHashUrl", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String formatHashUrl(
            String hostname,
            MediaPath mediaType,
            String encFileHash,
            Map<String, String> query
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientFormatHashUrl.default
        // Resolves the path segment for the media type; WA Web uses
        // nullthrows(u[mediaType]) here

        var pathSegment = mediaType.path()
                .orElseThrow(() -> new WhatsAppMediaException.Download(
                        "No hash URL path for media type: " + mediaType));

        // WAWebMmsClientFormatHashUrl.default
        // Builds "https://<host>/<path>/<urlSafeBase64(hash)>" as the base
        // URL without the query component

        var basePath = "https://" + hostname + "/" + pathSegment + "/" + urlSafeBase64(encFileHash);

        // WAWebMmsClientFormatHashUrl.default
        // Strips null values before appending the query string, matching
        // URLSearchParams behaviour

        var filteredQuery = new LinkedHashMap<String, String>();
        for (var entry : query.entrySet()) {
            if (entry.getValue() != null) {
                filteredQuery.put(entry.getKey(), entry.getValue());
            }
        }

        return basePath + encodeQueryString(filteredQuery);
    }

    /**
     * Converts a standard base64 string to the URL-safe variant used by
     * WhatsApp CDN URLs.
     *
     * <p>Replaces {@code /} with {@code _} and {@code +} with {@code -}
     * while preserving the {@code =} padding characters, matching the
     * behaviour of the WA Web utility function.
     *
     * @implNote WABase64UrlSafe.urlSafeBase64.
     * @param base64 the standard base64 string to convert
     * @return the URL-safe base64 string
     */
    @WhatsAppWebExport(moduleName = "WABase64UrlSafe", exports = "urlSafeBase64",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String urlSafeBase64(String base64) {
        // WABase64UrlSafe.urlSafeBase64
        // Replaces the non-URL-safe characters while keeping = padding

        return base64.replace('/', '_').replace('+', '-');
    }

    /**
     * Computes the bucket number that should be attached to the selected
     * host for a download request.
     *
     * <p>When {@code encFileHash} is {@code null}, the bucket defaults to
     * {@code 0}. When vcache aggregation is enabled (currently never
     * enabled in Cobalt) and {@link #maxBuckets} is positive, the bucket is
     * computed as {@code base64Modulo(encFileHash, maxBuckets) + 100}.
     * Otherwise no bucket-based routing applies and {@code null} is
     * returned.
     *
     * @implNote WAWebMediaHostsRouteSelection.routeSelection: bucket
     * computation that precedes {@code setSelectedBucket(p)}.
     * @param encFileHash the base64-encoded encrypted file hash, or
     *                    {@code null}
     * @return the selected bucket number, or {@code null} if no bucket applies
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHostsRouteSelection", exports = "routeSelection",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Integer computeSelectedBucket(String encFileHash) {
        // WAWebMediaHostsRouteSelection.routeSelection
        // When there is no encFilehash the bucket is 0 unconditionally,
        // matching "n == null ? p = 0" in the WA Web source

        if (encFileHash == null) {
            return 0;
        }

        // WAWebMediaHostsRouteSelection.routeSelection
        // The bucketed branch (m && i != null) requires the vcache
        // aggregation AB prop which Cobalt does not currently honour, so
        // the method returns null to signal "no bucket"

        return null;
    }

    /**
     * Performs one HTTP GET download against a fully-formed CDN URL.
     *
     * <p>Validates the HTTP status code through
     * {@link #validateMmsResponse(int, String)} and wraps the body in a
     * {@link MediaDownloadInputStream} so that the caller sees decrypted,
     * integrity-checked bytes. The returned stream owns the underlying
     * {@link HttpClient} and closes it when consumed or closed by the
     * caller.
     *
     * @implNote WAWebMmsClientMmsDownload.mms4Download combined with
     * WAWebMmsClientMmsDownload.validateMmsResponse. WA Web's
     * {@code extendedFetch(url, ...)} call is mapped to a direct
     * {@link HttpClient#send} invocation.
     * @param provider    the media provider for decryption metadata
     * @param downloadUrl the full URL to download from
     * @return an {@link InputStream} containing the downloaded media content
     * @throws WhatsAppMediaException.Download if the server returns a
     *         non-200 status code, the content length is missing, or a
     *         network error occurs
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientMmsDownload", exports = "mms4Download",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public InputStream tryDownload(MediaProvider provider, String downloadUrl) throws WhatsAppMediaException.Download {
        var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // WAWebMmsClientMmsDownload.validateMmsResponse
            // Maps non-200 responses to the appropriate Download exception
            // subtype before propagating to the retry loop

            if (response.statusCode() != 200) {
                client.close();
                validateMmsResponse(response.statusCode(), downloadUrl);
            }

            // WAWebMmsClientMmsDownload.mms4Download
            // Content-Length must be present so the download stream knows
            // how many ciphertext bytes to consume before the HMAC trailer

            var payloadLength = response.headers()
                    .firstValueAsLong("Content-Length")
                    .orElseThrow(() -> {
                        client.close();
                        return new WhatsAppMediaException.Download("Unknown content length");
                    });

            // WAWebMmsClientMmsDownload.mms4Download
            // Wraps the response body in a decrypting, verifying stream

            var rawInputStream = response.body();
            return new MediaDownloadInputStream(client, rawInputStream, payloadLength, provider);
        } catch (WhatsAppMediaException.Download downloadException) {
            throw downloadException;
        } catch (IOException | InterruptedException exception) {
            // ADAPTED: WAWebMmsClientMmsDownload.mms4Download
            // WA Web maps fetch TypeError to HttpNetworkError; Cobalt wraps
            // I/O and interruption errors in Download without a status code
            // so that isDownloadRetryable can classify them as retryable

            client.close();
            throw new WhatsAppMediaException.Download("mmsDownload: network error", exception);
        }
    }

    /**
     * Translates a non-OK HTTP status code from a media download into the
     * appropriate {@link WhatsAppMediaException.Download} exception.
     *
     * <p>Mirrors the WA Web error mapping:
     * <ul>
     *   <li>401: {@link WhatsAppMediaException#HTTP_UNAUTHORIZED}</li>
     *   <li>403: inspects the URL's {@code oe} parameter; when the
     *       signature has expired the error is reclassified as
     *       {@link WhatsAppMediaException#HTTP_NOT_FOUND}, otherwise
     *       {@link WhatsAppMediaException#HTTP_FORBIDDEN} is used</li>
     *   <li>404 and 410: {@link WhatsAppMediaException#HTTP_NOT_FOUND}</li>
     *   <li>507: {@link WhatsAppMediaException#HTTP_THROTTLE}</li>
     *   <li>other: the raw status code</li>
     * </ul>
     *
     * <p>WA Web additionally reads the response body on 403 to check for
     * "URL signature expired"; Cobalt already consumed the stream by the
     * time this method is called, so only the URL {@code oe} date check is
     * performed.
     *
     * @implNote WAWebMmsClientMmsDownload.validateMmsResponse combined with
     * WAWebMmsCdnUrlValidationUtils.parseCdnUrlParams for the {@code oe}
     * expiry lookup.
     * @param statusCode  the HTTP status code from the CDN response
     * @param url         the download URL, used for expiry date parsing
     * @throws WhatsAppMediaException.Download always, since this method is only
     *         called for non-OK responses
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientMmsDownload", exports = "validateMmsResponse",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebMmsCdnUrlValidationUtils", exports = "parseCdnUrlParams",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static void validateMmsResponse(int statusCode, String url) throws WhatsAppMediaException.Download {
        // WAWebMmsClientMmsDownload.validateMmsResponse
        // 401 maps to MMSUnauthorizedError in the upstream error taxonomy

        if (statusCode == 401) {
            throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_UNAUTHORIZED,
                    "mmsDownload: unauthorized");
        }

        if (statusCode == 403) {
            // WAWebMmsCdnUrlValidationUtils.parseCdnUrlParams
            // Reads the "oe" query parameter which encodes the URL signature
            // expiry as a hexadecimal unix timestamp; if the URL is expired
            // the error is reclassified as MediaNotFoundError

            try {
                var uri = URI.create(url);
                var query = uri.getRawQuery();
                if (query != null) {
                    for (var param : query.split("&")) {
                        var eqIndex = param.indexOf('=');
                        if (eqIndex >= 0 && param.substring(0, eqIndex).equals("oe")) {
                            var hexValue = param.substring(eqIndex + 1);
                            var expirationEpoch = Long.parseLong(hexValue, 16);

                            // WAWebMmsClientMmsDownload.validateMmsResponse
                            // Expired signature becomes MediaNotFoundError

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
                // ADAPTED: WAWebMmsCdnUrlValidationUtils.parseCdnUrlParams
                // On malformed URL or non-hex oe values WA Web returns null
                // from parseCdnUrlParams; Cobalt falls through to
                // MMSForbiddenError

            }

            // WAWebMmsClientMmsDownload.validateMmsResponse
            // Default 403 classification is MMSForbiddenError

            throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_FORBIDDEN,
                    "mmsDownload: forbidden");
        }

        // WAWebMmsClientMmsDownload.validateMmsResponse
        // 404 and 410 both map to MediaNotFoundError

        if (statusCode == 404 || statusCode == 410) {
            throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_NOT_FOUND,
                    "mmsDownload: media not found");
        }

        // WAWebMmsClientMmsDownload.validateMmsResponse
        // 507 maps to MMSThrottleError which is explicitly non-retryable

        if (statusCode == 507) {
            throw new WhatsAppMediaException.Download(WhatsAppMediaException.HTTP_THROTTLE,
                    "mmsDownload: throttled");
        }

        // WAWebMmsClientMmsDownload.validateMmsResponse
        // Anything else becomes a generic HttpStatusCodeError carrying the
        // original status code

        throw new WhatsAppMediaException.Download(statusCode,
                "mmsDownload: HTTP " + statusCode);
    }

    /**
     * Tests whether a media download error is worth retrying with a
     * different host. Uses the same rule set as
     * {@link #isRetryable(WhatsAppMediaException.Upload)} applied to
     * download-class exceptions:
     * <ul>
     *   <li>Missing status code (network error): retryable</li>
     *   <li>401 ({@code MMSUnauthorizedError}): retryable</li>
     *   <li>408 (request timeout): retryable</li>
     *   <li>507 ({@code MMSThrottleError}): not retryable</li>
     *   <li>Other 5xx: retryable</li>
     *   <li>All other status codes (404, 410, 403, and the rest): not retryable</li>
     * </ul>
     *
     * @implNote WAWebMmsClientIsErrorRetryable.isRetriableStatusCode,
     * WAWebMmsClientIsErrorRetryable.isErrorRetryable.
     * @param exception the download exception to test
     * @return {@code true} if the error is retryable, {@code false} otherwise
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientIsErrorRetryable",
            exports = {"isRetriableStatusCode", "isErrorRetryable"},
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isDownloadRetryable(WhatsAppMediaException.Download exception) {
        // WAWebMmsClientIsErrorRetryable.isErrorRetryable
        // Network-level failures have no status code and are retryable

        var optStatus = exception.httpStatusCode();
        if (optStatus.isEmpty()) {
            return true;
        }
        var status = optStatus.getAsInt();

        // WAWebMmsClientIsErrorRetryable.isRetriableStatusCode
        // 408 retryable, 507 fatal, other 5xx retryable

        if (status == 408) {
            return true;
        }
        if (status == 507) {
            return false;
        }
        if (status >= 500) {
            return true;
        }

        // WAWebMmsClientIsErrorRetryable.isErrorRetryable
        // 401 (MMSUnauthorizedError) is always retryable

        return status == 401;
    }

    /**
     * Probes the WhatsApp CDN to verify that a media file exists and is
     * still available for download.
     *
     * <p>Sends an HTTP HEAD request using the download URL format. A 200
     * response indicates the media is available; any other status triggers
     * the standard error mapping from
     * {@link #validateMmsResponse(int, String)}.
     *
     * @implNote WAWebMmsClientMmsDownload.mmsCheckExistence, which issues
     * a HEAD request and runs it through {@code validateMmsResponse}.
     * @param hostname    the CDN hostname
     * @param mediaType   the media path type
     * @param directPath  the CDN direct path, or {@code null}
     * @param encFileHash the base64-encoded encrypted file hash, or {@code null}
     * @throws WhatsAppMediaException.Download if the media does not exist
     *         (non-OK HTTP status) or a network error occurs
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientMmsDownload", exports = "mmsCheckExistence",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void checkExistence(
            String hostname,
            MediaPath mediaType,
            String directPath,
            String encFileHash
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientMmsDownload.mmsCheckExistence
        // Delegates to the shared HEAD helper; success is signalled by the
        // absence of a thrown exception

        sendHeadRequest(hostname, mediaType, directPath, encFileHash, "mmsCheckExistence");
    }

    /**
     * Retrieves the size in bytes of the encrypted media payload stored on
     * the WhatsApp CDN.
     *
     * <p>Sends an HTTP HEAD request and reads the {@code Content-Length}
     * response header. Callers use this to pre-allocate buffers or to
     * compute bandwidth estimates before invoking a full download.
     *
     * @implNote WAWebMmsClientMmsDownload.mmsGetEncryptedMediaSize.
     * @param hostname    the CDN hostname
     * @param mediaType   the media path type
     * @param directPath  the CDN direct path, or {@code null}
     * @param encFileHash the base64-encoded encrypted file hash, or {@code null}
     * @return the encrypted media file size in bytes
     * @throws WhatsAppMediaException.Download if the Content-Length header is
     *         missing, the server returns a non-OK status, or a network error
     *         occurs
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientMmsDownload", exports = "mmsGetEncryptedMediaSize",
            adaptation = WhatsAppAdaptation.DIRECT)
    public long getEncryptedMediaSize(
            String hostname,
            MediaPath mediaType,
            String directPath,
            String encFileHash
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientMmsDownload.mmsGetEncryptedMediaSize
        // Shares the HEAD helper with mmsCheckExistence and parses the
        // Content-Length header from the response

        var response = sendHeadRequest(hostname, mediaType, directPath, encFileHash, "mmsGetEncryptedMediaSize");

        // WAWebMmsClientMmsDownload.mmsGetEncryptedMediaSize
        // Missing Content-Length maps to UnableToGetContentLengthError in
        // WA Web's WAWebMiscErrors

        var contentLength = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1L);
        if (contentLength < 0) {
            throw new WhatsAppMediaException.Download("Unable to get content length");
        }

        return contentLength;
    }

    /**
     * Issues a single HTTP HEAD request to the CDN and returns the
     * validated response.
     *
     * <p>Shared helper for {@link #checkExistence} and
     * {@link #getEncryptedMediaSize}. Constructs the URL via
     * {@link #formatDownloadUrl} with mode {@code "auto"} and validates the
     * status code via {@link #validateMmsResponse(int, String)}.
     *
     * @implNote WAWebMmsClientMmsDownload: shared HEAD helper used by
     * {@code mmsCheckExistence} and {@code mmsGetEncryptedMediaSize}.
     * @param hostname     the CDN hostname
     * @param mediaType    the media path type
     * @param directPath   the CDN direct path, or {@code null}
     * @param encFileHash  the base64-encoded encrypted file hash, or {@code null}
     * @param functionName the caller function name for error context
     * @return the HTTP response from the HEAD request
     * @throws WhatsAppMediaException.Download if no path is available, the
     *         server returns a non-OK status, or a network error occurs
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientMmsDownload",
            exports = {"mmsCheckExistence", "mmsGetEncryptedMediaSize"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    private HttpResponse<?> sendHeadRequest(
            String hostname,
            MediaPath mediaType,
            String directPath,
            String encFileHash,
            String functionName
    ) throws WhatsAppMediaException.Download {
        // WAWebMmsClientMmsDownload.mmsCheckExistence
        // Builds the download URL with mode "auto" so the CDN returns the
        // HEAD metadata for the target media

        var url = formatDownloadUrl(hostname, directPath, encFileHash, mediaType, null);

        try (var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            // WAWebMmsClientMmsDownload.mmsCheckExistence
            // Non-200 responses flow through the standard Download error
            // mapping before returning

            if (response.statusCode() != 200) {
                validateMmsResponse(response.statusCode(), url);
            }

            return response;
        } catch (WhatsAppMediaException.Download downloadException) {
            throw downloadException;
        } catch (IOException | InterruptedException exception) {
            // ADAPTED: WAWebMmsClientMmsDownload.mmsCheckExistence
            // WA Web propagates fetch errors as HttpNetworkError; Cobalt
            // wraps I/O and interruption errors in Download with the caller
            // name prefix for easier diagnosis

            throw new WhatsAppMediaException.Download(functionName + ": network error", exception);
        }
    }

    /**
     * Returns the authentication token presented to the CDN on every
     * upload and download request.
     *
     * @implNote WAMediaConnParser.mediaConnParser:
     * {@code authToken: r.attrString("auth")}.
     * @return the authentication token, never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public String auth() {
        return auth;
    }

    /**
     * Returns the routes time-to-live in seconds, indicating how long the
     * host list can be trusted before a fresh media connection should be
     * requested.
     *
     * @implNote WAMediaConnParser.mediaConnParser: raw ttl attribute.
     * WAWebQueryMediaConnsJob.queryMediaConn: {@code ttl: routesExpiryTs - unixTime()}.
     * @return the TTL in seconds
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "queryMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    public int ttl() {
        return ttl;
    }

    /**
     * Returns the authentication token time-to-live in seconds, after
     * which the CDN rejects requests made with the stored token.
     *
     * @implNote WAMediaConnParser.mediaConnParser: raw auth_ttl attribute.
     * WAWebQueryMediaConnsJob.queryMediaConn: {@code authTTL: authTokenExpiryTs - unixTime()}.
     * @return the auth TTL in seconds
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "queryMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    public int authTtl() {
        return authTtl;
    }

    /**
     * Returns the maximum number of deterministic download buckets used
     * when vcache aggregation is enabled.
     *
     * @implNote WAMediaConnParser.mediaConnParser: {@code maxBuckets: r.attrInt("max_buckets")}.
     * @return the maximum bucket count
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public int maxBuckets() {
        return maxBuckets;
    }

    /**
     * Returns the server-advertised budget for manual media download
     * retries, defaulting to {@code 3} when absent.
     *
     * @implNote WAMediaConnParser.mediaConnParser:
     * {@code maxManualRetry: r.maybeAttrInt("max_manual_retry", 0, 4) ?? 3}.
     * @return the maximum manual retry count
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public int maxManualRetry() {
        return maxManualRetry;
    }

    /**
     * Returns the server-advertised budget for automatic media download
     * retries, defaulting to {@code 3} when absent.
     *
     * @implNote WAMediaConnParser.mediaConnParser:
     * {@code maxAutoDownloadRetry: r.maybeAttrInt("max_auto_download_retry", 0, 4) ?? 3}.
     * @return the maximum auto download retry count
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public int maxAutoDownloadRetry() {
        return maxAutoDownloadRetry;
    }

    /**
     * Returns the epoch-second timestamp at which this media connection
     * was parsed.
     *
     * @implNote ADAPTED: WAWebQueryMediaConnsJob.queryMediaConn stores the
     * future expiry timestamps directly; Cobalt stores the parse time and
     * the TTL separately so that expiry checks can be recomputed on
     * demand.
     * @return the creation timestamp in epoch seconds
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "queryMediaConn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public long timestamp() {
        return timestamp;
    }

    /**
     * Returns the list of CDN host entries available for uploads and
     * downloads.
     *
     * @implNote WAMediaConnParser.mediaConnParser: {@code hosts: c(r)}.
     * WAWebQueryMediaConnsJob.mapParsedMediaConn: host mapping.
     * @return an unmodifiable collection of hosts, never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAMediaConnParser", exports = "mediaConnParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob", exports = "mapParsedMediaConn",
            adaptation = WhatsAppAdaptation.DIRECT)
    public SequencedCollection<? extends MediaHost> hosts() {
        return hosts;
    }

    /**
     * Checks whether this media connection's authentication token has
     * expired.
     *
     * <p>A connection is considered expired when the current clock is at
     * or past {@code timestamp + authTtl}. Callers that see
     * {@code true} must request a fresh media connection before issuing
     * new CDN requests.
     *
     * <p>In WA Web {@code _isExpiredOrMissing()} also returns {@code true}
     * when the cached data is {@code null}; Cobalt handles the
     * {@code null} case through the store's await semantics and therefore
     * only checks the TTL here.
     *
     * @implNote WAWebMediaHosts._isExpiredOrMissing: the auth-token
     * expiration clause {@code new Date >= e.authExpirationTime} where
     * {@code authExpirationTime = queryStartTime + authTTL}.
     * @return {@code true} if the auth token has expired
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHosts", exports = "mediaHosts",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean isExpired() {
        // WAWebMediaHosts.mediaHosts
        // Mirrors _isExpiredOrMissing: now >= queryStartTime + authTTL

        return Instant.now().getEpochSecond() >= timestamp + authTtl;
    }

    /**
     * Checks whether this media connection should be proactively
     * refreshed.
     *
     * <p>A refresh is needed when either the routes TTL has elapsed or
     * {@code 80%} of the authentication TTL has elapsed, whichever
     * happens first. This matches WA Web's proactive strategy to avoid
     * serving requests with stale or nearly-expired credentials.
     *
     * @implNote WAWebMediaHosts._needsRefresh: combines the
     * {@code hostsRefreshTime} check ({@code queryStartTime + ttl}) with
     * the 80% auth-TTL check ({@code queryStartTime + floor(authTtl * 0.8)}).
     * @return {@code true} if the media connection should be refreshed
     */
    @WhatsAppWebExport(moduleName = "WAWebMediaHosts", exports = "mediaHosts",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean needsRefresh() {
        // WAWebMediaHosts.mediaHosts
        // Captures the current time once for both branches of the refresh
        // check to avoid observing the clock twice

        var now = Instant.now().getEpochSecond();

        // WAWebMediaHosts.mediaHosts
        // Refresh when the routes TTL has elapsed
        // (now >= hostsRefreshTime)

        if (now >= timestamp + ttl) {
            return true;
        }

        // WAWebMediaHosts.mediaHosts
        // Refresh when 80% of the auth TTL has elapsed, defined as
        // floor(authTtl * 0.8) seconds after the query start time

        var authRefreshThreshold = (long) Math.floor(authTtl * 0.8);
        return now >= timestamp + authRefreshThreshold;
    }

    /**
     * Returns a debug-friendly string representation of every field of
     * this media connection.
     *
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
