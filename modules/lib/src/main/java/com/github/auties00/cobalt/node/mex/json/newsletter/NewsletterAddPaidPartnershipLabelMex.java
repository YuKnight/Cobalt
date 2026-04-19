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
 * Attaches a paid-partnership disclosure label to a newsletter message.
 *
 * <p>Monetised creators must disclose paid partnerships on sponsored messages. This mutation attaches the required legal label to a specific newsletter message so the server can render the disclosure badge.
 *
 * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob: adapts the {@code mexNewsletterAddPaidPartnershipLabelJob} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexNewsletterAddPaidPartnershipLabelJob")
public sealed interface NewsletterAddPaidPartnershipLabelMex extends MexJsonOperation permits NewsletterAddPaidPartnershipLabelMex.Request, NewsletterAddPaidPartnershipLabelMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code NewsletterAddPaidPartnershipLabel} compiled mutation.
     *
     * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJobMutation.graphql: corresponds to the compiled
     * document id registered for the {@code mexNewsletterAddPaidPartnershipLabelJob} mutation.
     */
    String QUERY_ID = "25690501173969818";

    /**
     * The request variant of {@link NewsletterAddPaidPartnershipLabelMex} that serialises the
     * mutation variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob: adapts the {@code variables}
     * object constructed inline in the JS implementation into a dedicated
     * Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexNewsletterAddPaidPartnershipLabelJob")
    final class Request implements NewsletterAddPaidPartnershipLabelMex {
        private final String newsletterId;
        private final String serverId;

        public Request(String newsletterId, String serverId) {
            this.newsletterId = newsletterId;
            this.serverId = serverId;
        }

        /**
         * Builds the IQ stanza that dispatches this operation to the
         * WhatsApp relay.
         *
         * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob: WA Web constructs the
         * {@code variables} object inline and delegates to
         * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and wraps it through
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexNewsletterAddPaidPartnershipLabelJob", exports = "mexNewsletterAddPaidPartnershipLabelJob",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
                // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
                // Emits the newsletter_id variable when present
                if (newsletterId != null) {
                    writer.writeName("newsletter_id");
                    writer.writeColon();
                    writer.writeString(newsletterId);
                }

                // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
                // Emits the server_id variable when present
                if (serverId != null) {
                    writer.writeName("server_id");
                    writer.writeColon();
                    writer.writeString(serverId);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
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
     * The response variant of {@link NewsletterAddPaidPartnershipLabelMex} that exposes the data
     * returned by the server after a successful mutation.
     *
     * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob: adapts the JSON root returned by the GraphQL
     * mutation into a Java value object.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexNewsletterAddPaidPartnershipLabelJob")
    final class Response implements NewsletterAddPaidPartnershipLabelMex {
        private final String id;

        private Response(String id) {
            this.id = id;
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
         * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob: WA Web relies on the
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
         * Returns the {@code id} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> id() {
            return Optional.ofNullable(id);
        }

        /**
         * Parses a {@link Response} from the raw JSON bytes of the
         * {@code <result>} child.
         *
         * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob: mirrors the implicit
         * unwrapping that WA Web performs on the GraphQL response,
         * extracting the {@code xwa2_newsletter_label_paid_partnership} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
            // Parses the raw JSON payload, bailing out if fastjson2 returns null
            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
            // Descends into the standard GraphQL "data" envelope
            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
            // Extracts the operation-specific root keyed by xwa2_newsletter_label_paid_partnership
            var root = data.getJSONObject("xwa2_newsletter_label_paid_partnership");
            if (root == null) {
                return Optional.empty();
            }

            var id = root.getString("id");

            return Optional.of(new Response(id));
        }
    }
}
