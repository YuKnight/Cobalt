package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.json.MexJsonOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Checks whether a given domain is previewable inside newsletter messages.
 *
 * <p>The WhatsApp backend maintains a list of allowed domains whose link previews may be rendered inside newsletter messages. This query validates a URL before publishing.
 *
 * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob: adapts the {@code mexFetchNewsletterIsDomainPreviewable} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterIsDomainPreviewableJob")
public sealed interface FetchNewsletterIsDomainPreviewableMex extends MexJsonOperation permits FetchNewsletterIsDomainPreviewableMex.Request, FetchNewsletterIsDomainPreviewableMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterIsDomainPreviewable} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterIsDomainPreviewable} query.
     */
    String QUERY_ID = "9849510985088294";

    /**
     * The request variant of {@link FetchNewsletterIsDomainPreviewableMex} that serialises the
     * query variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable: adapts the {@code variables}
     * object constructed inline in the JS implementation into a dedicated
     * Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterIsDomainPreviewableJob")
    final class Request implements FetchNewsletterIsDomainPreviewableMex {
        private final String urlDomains;

        public Request(String urlDomains) {
            this.urlDomains = urlDomains;
        }

        /**
         * Builds the IQ stanza that dispatches this operation to the
         * WhatsApp relay.
         *
         * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable: WA Web constructs the
         * {@code variables} object inline and delegates to
         * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and wraps it through
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterIsDomainPreviewableJob", exports = "mexFetchNewsletterIsDomainPreviewable",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
                // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
                // Emits the url_domains variable when present
                if (urlDomains != null) {
                    writer.writeName("url_domains");
                    writer.writeColon();
                    writer.writeString(urlDomains);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
                // Flushes the JSON buffer into a StringWriter and wraps it in the shared MEX IQ envelope
                try (var output = new StringWriter()) {
                    writer.flushTo(output);
                    return MexJsonOperation.createMexNode(QUERY_ID, output.toString());
                }
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }

    /**
     * The response variant of {@link FetchNewsletterIsDomainPreviewableMex} that exposes the data
     * returned by the server after a successful query.
     *
     * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob: adapts the JSON root returned by the GraphQL
     * query into a Java value object.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterIsDomainPreviewableJob")
    final class Response implements FetchNewsletterIsDomainPreviewableMex {
        private final List<UrlPreviews> urlPreviews;

        private Response(List<UrlPreviews> urlPreviews) {
            this.urlPreviews = urlPreviews;
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
         * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable: WA Web relies on the
         * GraphQL client to unwrap the response. Cobalt performs the
         * unwrapping manually from the IQ {@code <result>} child.
         * @param node the IQ response node received from the relay
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the node is missing a result payload
         */
        public static Optional<Response> of(Node node) {
            return node.getChild("result")
                    .flatMap(Node::toContentBytes)
                    .flatMap(Response::of);
        }

        /**
         * Returns the {@code url_previews} field.
         *
         * @return the list of values, empty if absent
         */
        public List<UrlPreviews> urlPreviews() {
            return urlPreviews;
        }

        /**
         * A parsed {@code UrlPreviews} object.
         */
        public static final class UrlPreviews {
            private final String urlDomain;
            private final Boolean isPreviewable;

            private UrlPreviews(String urlDomain, Boolean isPreviewable) {
                this.urlDomain = urlDomain;
                this.isPreviewable = isPreviewable;
            }

            /**
             * Returns the {@code url_domain} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> urlDomain() {
                return Optional.ofNullable(urlDomain);
            }

            /**
             * Returns the {@code is_previewable} field.
             *
             * @return {@code true} if the value is present and true, {@code false} otherwise
             */
            public boolean isPreviewable() {
                return isPreviewable != null && isPreviewable;
            }

            /**
             * Parses a {@code UrlPreviews} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<UrlPreviews> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var urlDomain = obj.getString("url_domain");
                var isPreviewable = obj.getBoolean("is_previewable");
                return Optional.of(new UrlPreviews(urlDomain, isPreviewable));
            }

            /**
             * Parses a list of {@code UrlPreviews} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<UrlPreviews> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<UrlPreviews>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * Parses a {@link Response} from the raw JSON bytes of the
         * {@code <result>} child.
         *
         * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable: mirrors the implicit
         * unwrapping that WA Web performs on the GraphQL response,
         * extracting the {@code xwa2_newsletter_message_integrity} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
            // Parses the raw JSON payload, bailing out if fastjson2 returns null
            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
            // Descends into the standard GraphQL "data" envelope
            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
            // Extracts the operation-specific root keyed by xwa2_newsletter_message_integrity
            var root = data.getJSONObject("xwa2_newsletter_message_integrity");
            if (root == null) {
                return Optional.empty();
            }

            var urlPreviews = UrlPreviews.ofArray(root.getJSONArray("url_previews"));

            return Optional.of(new Response(urlPreviews));
        }
    }
}
