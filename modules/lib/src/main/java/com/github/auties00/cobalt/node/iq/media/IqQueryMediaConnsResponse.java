package com.github.auties00.cobalt.node.iq.media;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to an {@link IqQueryMediaConnsRequest}.
 *
 * @implNote {@code WAWebQueryMediaConnsJob.queryMediaConn} folds every
 *           non-success code into either {@code E507} with backoff or
 *           {@code ServerStatusCodeError}; Cobalt splits the failure
 *           into typed {@code ClientError} and {@code ServerError}
 *           variants.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryMediaConnsJob")
public sealed interface IqQueryMediaConnsResponse extends IqOperation.Response
        permits IqQueryMediaConnsResponse.Success, IqQueryMediaConnsResponse.ClientError, IqQueryMediaConnsResponse.ServerError {

    /**
     * Tries each {@link IqQueryMediaConnsResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob",
            exports = "queryMediaConn", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqQueryMediaConnsResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay returned the
     * current media-conn configuration.
     *
     * @implNote {@code WAMediaConnParser.mediaConnParser} reads the
     *           top-level {@code <media_conn auth auth_ttl ttl
     *           max_buckets max_manual_retry max_auto_download_retry/>}
     *           attributes and the {@code <host/>} grandchild list;
     *           Cobalt maps the wire shape onto {@link Host}
     *           tuples and surfaces the auth/TTL/bucket scalars
     *           directly on {@link Success}.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryMediaConnsJob")
    @WhatsAppWebModule(moduleName = "WAMediaConnParser")
    final class Success implements IqQueryMediaConnsResponse {
        /**
         * The relay-issued auth token used to authenticate uploads
         * and downloads with the media CDN. Routed to the
         * {@code Authorization} header verbatim.
         */
        private final String authToken;

        /**
         * The unix-second absolute expiry timestamp of
         * {@link #authToken}.
         */
        private final long authTokenExpiry;

        /**
         * The unix-second absolute expiry timestamp of the host
         * routes themselves.
         */
        private final long routesExpiry;

        /**
         * The maximum bucket cardinality the relay supports for
         * sharded downloads. Capped at four by the WA Web parser.
         */
        private final int maxBuckets;

        /**
         * The maximum manual-retry budget per upload. Capped at
         * four by the WA Web parser; defaults to three when the
         * relay omits the attribute.
         */
        private final int maxManualRetry;

        /**
         * The maximum auto-download-retry budget per download.
         * Capped at four by the WA Web parser; defaults to three
         * when the relay omits the attribute.
         */
        private final int maxAutoDownloadRetry;

        /**
         * The list of media-server host endpoints returned by the
         * relay.
         */
        private final List<Host> hosts;

        /**
         * Constructs a new successful reply.
         *
         * @param authToken           the auth token; never
         *                            {@code null}
         * @param authTokenExpiry     the auth-token absolute expiry
         *                            timestamp
         * @param routesExpiry        the host-routes absolute expiry
         *                            timestamp
         * @param maxBuckets          the bucket cardinality
         * @param maxManualRetry      the manual-retry budget
         * @param maxAutoDownloadRetry the auto-download-retry budget
         * @param hosts               the host endpoints; never
         *                            {@code null}
         * @throws NullPointerException if any reference argument is
         *                              {@code null}
         */
        public Success(String authToken, long authTokenExpiry, long routesExpiry,
                       int maxBuckets, int maxManualRetry, int maxAutoDownloadRetry,
                       List<Host> hosts) {
            this.authToken = Objects.requireNonNull(authToken, "authToken cannot be null");
            this.authTokenExpiry = authTokenExpiry;
            this.routesExpiry = routesExpiry;
            this.maxBuckets = maxBuckets;
            this.maxManualRetry = maxManualRetry;
            this.maxAutoDownloadRetry = maxAutoDownloadRetry;
            Objects.requireNonNull(hosts, "hosts cannot be null");
            this.hosts = List.copyOf(hosts);
        }

        /**
         * Returns the auth token.
         *
         * @return the token; never {@code null}
         */
        public String authToken() {
            return authToken;
        }

        /**
         * Returns the auth-token absolute expiry timestamp.
         *
         * @return the timestamp
         */
        public long authTokenExpiry() {
            return authTokenExpiry;
        }

        /**
         * Returns the host-routes absolute expiry timestamp.
         *
         * @return the timestamp
         */
        public long routesExpiry() {
            return routesExpiry;
        }

        /**
         * Returns the bucket cardinality.
         *
         * @return the cardinality
         */
        public int maxBuckets() {
            return maxBuckets;
        }

        /**
         * Returns the manual-retry budget.
         *
         * @return the budget
         */
        public int maxManualRetry() {
            return maxManualRetry;
        }

        /**
         * Returns the auto-download-retry budget.
         *
         * @return the budget
         */
        public int maxAutoDownloadRetry() {
            return maxAutoDownloadRetry;
        }

        /**
         * Returns the unmodifiable list of host endpoints.
         *
         * @return the hosts; never {@code null}
         */
        public List<Host> hosts() {
            return hosts;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WAMediaConnParser",
                exports = "mediaConnParser", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var mediaConnChild = node.getChild("media_conn").orElse(null);
            if (mediaConnChild == null) {
                return Optional.empty();
            }
            var auth = mediaConnChild.getAttributeAsString("auth").orElse(null);
            if (auth == null) {
                return Optional.empty();
            }
            var authTtl = mediaConnChild.getAttributeAsLong("auth_ttl", -1L);
            var ttl = mediaConnChild.getAttributeAsLong("ttl", -1L);
            if (authTtl < 0 || ttl < 0) {
                return Optional.empty();
            }
            var maxBuckets = mediaConnChild.getAttributeAsInt("max_buckets", 0);
            var maxManualRetry = mediaConnChild.getAttributeAsInt("max_manual_retry", 3);
            if (maxManualRetry < 0 || maxManualRetry > 4) {
                maxManualRetry = 3;
            }
            var maxAutoDownloadRetry = mediaConnChild.getAttributeAsInt("max_auto_download_retry", 3);
            if (maxAutoDownloadRetry < 0 || maxAutoDownloadRetry > 4) {
                maxAutoDownloadRetry = 3;
            }
            var hostNodes = mediaConnChild.getChildren("host");
            var hosts = new ArrayList<Host>(hostNodes.size());
            for (var hostNode : hostNodes) {
                hosts.add(Host.of(hostNode));
            }
            return Optional.of(new Success(auth, authTtl, ttl, maxBuckets,
                    maxManualRetry, maxAutoDownloadRetry, hosts));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return this.authTokenExpiry == that.authTokenExpiry
                    && this.routesExpiry == that.routesExpiry
                    && this.maxBuckets == that.maxBuckets
                    && this.maxManualRetry == that.maxManualRetry
                    && this.maxAutoDownloadRetry == that.maxAutoDownloadRetry
                    && Objects.equals(this.authToken, that.authToken)
                    && Objects.equals(this.hosts, that.hosts);
        }

        @Override
        public int hashCode() {
            return Objects.hash(authToken, authTokenExpiry, routesExpiry, maxBuckets,
                    maxManualRetry, maxAutoDownloadRetry, hosts);
        }

        @Override
        public String toString() {
            return "IqQueryMediaConnsResponse.Success[authToken=" + authToken
                    + ", authTokenExpiry=" + authTokenExpiry
                    + ", routesExpiry=" + routesExpiry
                    + ", maxBuckets=" + maxBuckets
                    + ", maxManualRetry=" + maxManualRetry
                    + ", maxAutoDownloadRetry=" + maxAutoDownloadRetry
                    + ", hosts=" + hosts + ']';
        }

        /**
         * One media-server host endpoint projected from a single
         * {@code <host hostname class ip4 ip6 type><download/><upload/><download_buckets/></host>}
         * subtree.
         *
         * @implNote {@code WAMediaConnParser.u()} reads the
         *           {@code <host/>} attributes and inner
         *           {@code <upload/>} / {@code <download/>} /
         *           {@code <download_buckets/>} subtrees;
         *           {@code WAWebQueryMediaConnsJob.f()} drops the
         *           wire-side {@code fallback_*} variants because
         *           Cobalt callers don't need the legacy
         *           secondary-host indirection.
         */
        @WhatsAppWebModule(moduleName = "WAMediaConnParser")
        @WhatsAppWebModule(moduleName = "WAWebQueryMediaConnsJob")
        public static final class Host {
            /**
             * The host's DNS hostname. Routed verbatim from the
             * {@code hostname} attribute.
             */
            private final String hostname;

            /**
             * The host's class label, when supplied. Routed
             * verbatim from the {@code class} attribute.
             */
            private final String hostClass;

            /**
             * The host's IPv4 address, when supplied. Routed
             * verbatim from the {@code ip4} attribute.
             */
            private final String ip4;

            /**
             * The host's IPv6 address, when supplied. Routed
             * verbatim from the {@code ip6} attribute.
             */
            private final String ip6;

            /**
             * Whether the host is the relay's documented fallback
             * route — {@code true} when the {@code type} attribute
             * is {@code "fallback"}.
             */
            private final boolean fallback;

            /**
             * The list of media-type tags supported by uploads to
             * this host. Routed from {@code <upload/>} grandchild
             * tags (e.g. {@code "image"}, {@code "video"},
             * {@code "ptt"}); empty list means "every documented
             * media type".
             */
            private final List<String> uploadable;

            /**
             * The list of media-type tags supported by downloads
             * from this host. Routed from {@code <download/>}
             * grandchild tags; empty list means "every documented
             * media type".
             */
            private final List<String> downloadable;

            /**
             * The list of bucket indices eligible for downloads
             * through this host. Routed from
             * {@code <download_buckets/>} grandchild tags.
             */
            private final List<Integer> downloadBuckets;

            /**
             * Constructs a new host endpoint.
             *
             * @param hostname        the DNS hostname; never
             *                        {@code null}
             * @param hostClass       the optional class label;
             *                        may be {@code null}
             * @param ip4             the optional IPv4 address;
             *                        may be {@code null}
             * @param ip6             the optional IPv6 address;
             *                        may be {@code null}
             * @param fallback        the fallback flag
             * @param uploadable      the upload media-type tags;
             *                        never {@code null}
             * @param downloadable    the download media-type tags;
             *                        never {@code null}
             * @param downloadBuckets the download bucket indices;
             *                        never {@code null}
             * @throws NullPointerException if any non-nullable
             *                              argument is {@code null}
             */
            public Host(String hostname, String hostClass, String ip4, String ip6,
                        boolean fallback, List<String> uploadable, List<String> downloadable,
                        List<Integer> downloadBuckets) {
                this.hostname = Objects.requireNonNull(hostname, "hostname cannot be null");
                this.hostClass = hostClass;
                this.ip4 = ip4;
                this.ip6 = ip6;
                this.fallback = fallback;
                Objects.requireNonNull(uploadable, "uploadable cannot be null");
                Objects.requireNonNull(downloadable, "downloadable cannot be null");
                Objects.requireNonNull(downloadBuckets, "downloadBuckets cannot be null");
                this.uploadable = List.copyOf(uploadable);
                this.downloadable = List.copyOf(downloadable);
                this.downloadBuckets = List.copyOf(downloadBuckets);
            }

            /**
             * Returns the DNS hostname.
             *
             * @return the hostname; never {@code null}
             */
            public String hostname() {
                return hostname;
            }

            /**
             * Returns the optional class label.
             *
             * @return an {@link Optional} carrying the label, or
             *         empty when absent
             */
            public Optional<String> hostClass() {
                return Optional.ofNullable(hostClass);
            }

            /**
             * Returns the optional IPv4 address.
             *
             * @return an {@link Optional} carrying the address, or
             *         empty when absent
             */
            public Optional<String> ip4() {
                return Optional.ofNullable(ip4);
            }

            /**
             * Returns the optional IPv6 address.
             *
             * @return an {@link Optional} carrying the address, or
             *         empty when absent
             */
            public Optional<String> ip6() {
                return Optional.ofNullable(ip6);
            }

            /**
             * Returns the fallback flag.
             *
             * @return {@code true} when this host is the
             *         documented fallback route
             */
            public boolean fallback() {
                return fallback;
            }

            /**
             * Returns the unmodifiable list of upload media-type
             * tags.
             *
             * @return the tags; never {@code null}
             */
            public List<String> uploadable() {
                return uploadable;
            }

            /**
             * Returns the unmodifiable list of download media-type
             * tags.
             *
             * @return the tags; never {@code null}
             */
            public List<String> downloadable() {
                return downloadable;
            }

            /**
             * Returns the unmodifiable list of download bucket
             * indices.
             *
             * @return the indices; never {@code null}
             */
            public List<Integer> downloadBuckets() {
                return downloadBuckets;
            }

            /**
             * Parses a host endpoint from the given
             * {@code <host/>} subtree.
             *
             * @param hostNode the {@code <host/>} subtree; never
             *                 {@code null}
             * @return the parsed host
             *
             * @implNote Mirrors {@code WAMediaConnParser.u} +
             *           {@code d} for the upload/download tag
             *           lists.
             */
            @WhatsAppWebExport(moduleName = "WAMediaConnParser",
                    exports = "mediaConnParser", adaptation = WhatsAppAdaptation.ADAPTED)
            public static Host of(Node hostNode) {
                Objects.requireNonNull(hostNode, "hostNode cannot be null");
                var hostname = hostNode.getAttributeAsString("hostname").orElse("");
                var hostClass = hostNode.getAttributeAsString("class").orElse(null);
                var ip4 = hostNode.getAttributeAsString("ip4").orElse(null);
                var ip6 = hostNode.getAttributeAsString("ip6").orElse(null);
                var fallback = "fallback".equals(hostNode.getAttributeAsString("type").orElse(null));
                var uploadable = collectMediaTypes(hostNode.getChild("upload").orElse(null));
                var downloadable = collectMediaTypes(hostNode.getChild("download").orElse(null));
                var bucketsNode = hostNode.getChild("download_buckets").orElse(null);
                var buckets = new ArrayList<Integer>();
                if (bucketsNode != null) {
                    for (var bucketChild : bucketsNode.children()) {
                        try {
                            buckets.add(Integer.parseInt(bucketChild.description()));
                        } catch (NumberFormatException _) {
                            // skip non-integer descriptions; mirrors WA Web's
                            // best-effort projection
                        }
                    }
                }
                return new Host(hostname, hostClass, ip4, ip6, fallback,
                        uploadable, downloadable, buckets);
            }

            /**
             * Collects the media-type tag descriptions from a
             * {@code <upload/>} or {@code <download/>} subtree.
             *
             * @param wrapper the wrapper node, may be {@code null}
             * @return the collected tags
             */
            private static List<String> collectMediaTypes(Node wrapper) {
                if (wrapper == null) {
                    return List.of();
                }
                var result = new ArrayList<String>();
                for (var child : wrapper.children()) {
                    result.add(child.description());
                }
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Host) obj;
                return this.fallback == that.fallback
                        && Objects.equals(this.hostname, that.hostname)
                        && Objects.equals(this.hostClass, that.hostClass)
                        && Objects.equals(this.ip4, that.ip4)
                        && Objects.equals(this.ip6, that.ip6)
                        && Objects.equals(this.uploadable, that.uploadable)
                        && Objects.equals(this.downloadable, that.downloadable)
                        && Objects.equals(this.downloadBuckets, that.downloadBuckets);
            }

            @Override
            public int hashCode() {
                return Objects.hash(hostname, hostClass, ip4, ip6, fallback,
                        uploadable, downloadable, downloadBuckets);
            }

            @Override
            public String toString() {
                return "IqQueryMediaConnsResponse.Success.Host[hostname=" + hostname
                        + ", hostClass=" + hostClass
                        + ", ip4=" + ip4
                        + ", ip6=" + ip6
                        + ", fallback=" + fallback
                        + ", uploadable=" + uploadable
                        + ", downloadable=" + downloadable
                        + ", downloadBuckets=" + downloadBuckets + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * media-conn query as malformed or unauthorised.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryMediaConnsJob")
    final class ClientError implements IqQueryMediaConnsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ClientError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob",
                exports = "queryMediaConn", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ClientError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientError) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryMediaConnsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure (codes {@code >= 500}) while
     * processing the query; in particular, code {@code 507}
     * "Insufficient Storage" maps to the WA Web {@code E507}
     * type that carries a server-supplied backoff hint.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryMediaConnsJob")
    final class ServerError implements IqQueryMediaConnsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ServerError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob",
                exports = "queryMediaConn", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ServerError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ServerError) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryMediaConnsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
