package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
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
import java.util.Objects;
import java.util.Optional;

/**
 * Logs a batch of newsletter exposure events for attribution and ranking.
 *
 * <p>When a user browses newsletters the client records lightweight exposure events which are later flushed to the server via this mutation. The backend uses the exposure signal to improve directory ranking.
 *
 * @implNote WAWebMexLogNewsletterExposuresJob: adapts the {@code mexLogNewsletterExposures} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexLogNewsletterExposuresJob")
public sealed interface LogNewsletterExposuresMex extends MexJsonOperation permits LogNewsletterExposuresMex.Request, LogNewsletterExposuresMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code LogNewsletterExposures} compiled mutation.
     *
     * @implNote WAWebMexLogNewsletterExposuresJobMutation.graphql: corresponds to the compiled
     * document id registered for the {@code mexLogNewsletterExposures} mutation.
     */
    String QUERY_ID = "25260800823586918";

    /**
     * The request variant of {@link LogNewsletterExposuresMex} that serialises the
     * mutation variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: adapts the {@code variables}
     * object constructed inline in the JS implementation into a dedicated
     * Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexLogNewsletterExposuresJob")
    final class Request implements LogNewsletterExposuresMex {
        private final String input;

        public Request(String input) {
            this.input = input;
        }

        /**
         * Builds the IQ stanza that dispatches this operation to the
         * WhatsApp relay.
         *
         * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: WA Web constructs the
         * {@code variables} object inline and delegates to
         * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and wraps it through
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexLogNewsletterExposuresJob", exports = "mexLogNewsletterExposures",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope

            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
                // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery

                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
                // Emits the input variable when present

                if (input != null) {
                    writer.writeName("input");
                    writer.writeColon();
                    writer.writeString(input);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
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
     * The response variant of {@link LogNewsletterExposuresMex} that exposes the data
     * returned by the server after a successful mutation.
     *
     * @implNote WAWebMexLogNewsletterExposuresJob: adapts the JSON root returned by the GraphQL
     * mutation into a Java value object.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexLogNewsletterExposuresJob")
    final class Response implements LogNewsletterExposuresMex {

        private Response() {
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
         * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: WA Web relies on the
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
         * Parses a {@link Response} from the raw JSON bytes of the
         * {@code <result>} child.
         *
         * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: mirrors the implicit
         * unwrapping that WA Web performs on the GraphQL response,
         * extracting the {@code xwa2_newsletter_log_exposures} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
            // Parses the raw JSON payload, bailing out if fastjson2 returns null

            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
            // Descends into the standard GraphQL "data" envelope

            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
            // Probes for the xwa2_newsletter_log_exposures marker so callers can confirm the batch was accepted

            var root = data.get("xwa2_newsletter_log_exposures");
            if (root == null) {
                return Optional.empty();
            }

            return Optional.of(new Response());
        }
    }
}
