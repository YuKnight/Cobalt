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
 * Transfers ownership of a newsletter to another user.
 *
 * <p>Only the current owner of a newsletter may initiate an ownership change.
 * The target user must already be registered on WhatsApp. After the mutation
 * completes successfully, the target user receives full owner privileges and
 * the original owner is demoted to an admin role.
 *
 * <p>The request carries the newsletter identifier together with the
 * recipient user identifier, and the response returns the newsletter id on
 * success.
 *
 * @implNote WAWebMexChangeNewsletterOwnerJob: adapts the
 * {@code mexChangeNewsletterOwner} GraphQL mutation, which in WA Web is
 * invoked via {@code WAWebMexClient.fetchQuery} with a
 * {@code {newsletter_id, user_id}} variables object and unwraps the
 * {@code xwa2_newsletter_change_owner} root of the response.
 */
@WhatsAppWebModule(moduleName = "WAWebMexChangeNewsletterOwnerJob")
public sealed interface ChangeNewsletterOwnerMex extends MexJsonOperation permits ChangeNewsletterOwnerMex.Request, ChangeNewsletterOwnerMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code ChangeNewsletterOwner} compiled mutation.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJobMutation.graphql: corresponds
     * to the compiled document id registered for the
     * {@code mexChangeNewsletterOwner} mutation.
     */
    String QUERY_ID = "9546742745432473";

    /**
     * The request variant of {@link ChangeNewsletterOwnerMex} that serialises
     * the mutation variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
     * adapts the {@code variables} object constructed inline in the JS
     * implementation into a dedicated Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexChangeNewsletterOwnerJob")
    final class Request implements ChangeNewsletterOwnerMex {
        /**
         * The identifier of the newsletter whose ownership is being
         * transferred.
         *
         * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
         * mirrors the {@code newsletter_id} variable in the WA Web call.
         */
        @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final String newsletterId;

        /**
         * The identifier of the user who will become the new owner.
         *
         * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
         * mirrors the {@code user_id} variable which WA Web derives from the
         * target wid via {@code WAWebLidMigrationUtils}.
         */
        @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final String userId;

        /**
         * Creates a request that transfers ownership of the given newsletter
         * to the given user.
         *
         * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
         * maps the {@code (newsletterId, userId)} positional parameters of
         * the JS function.
         * @param newsletterId the identifier of the newsletter whose owner is
         *                     being changed
         * @param userId the identifier of the user who will become the new
         *               owner
         */
        @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Request(String newsletterId, String userId) {
            this.newsletterId = newsletterId;
            this.userId = userId;
        }

        /**
         * Builds the IQ stanza that dispatches this ownership-transfer
         * mutation to the WhatsApp relay.
         *
         * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
         * WA Web constructs the {@code variables} object inline and delegates
         * to {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON
         * directly via {@code fastjson2.JSONWriter} and delegates stanza
         * wrapping to {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope

            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
                // Begins the outer envelope and the nested "variables" object

                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();

                // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
                // Emits the newsletter_id variable when present

                if (newsletterId != null) {
                    writer.writeName("newsletter_id");
                    writer.writeColon();
                    writer.writeString(newsletterId);
                }

                // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
                // Emits the user_id variable when present, skipping it otherwise

                if (userId != null) {
                    writer.writeName("user_id");
                    writer.writeColon();
                    writer.writeString(userId);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
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
     * The response variant of {@link ChangeNewsletterOwnerMex} exposing the
     * identifier echoed back by the server once the ownership change is
     * applied.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob: adapts the
     * {@code xwa2_newsletter_change_owner} root of the JSON response.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexChangeNewsletterOwnerJob")
    final class Response implements ChangeNewsletterOwnerMex {
        /**
         * The identifier of the newsletter whose owner was changed, as echoed
         * by the server.
         */
        private final String id;

        /**
         * Creates a response carrying the newsletter identifier returned by
         * the server.
         *
         * @param id the newsletter identifier echoed by the server
         */
        private Response(String id) {
            this.id = id;
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
         * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
         * WA Web relies on the GraphQL client to unwrap the response. Cobalt
         * performs the unwrapping manually from the IQ {@code <result>} child.
         * @param node the IQ response node received from the relay
         * @return an {@link Optional} containing the parsed response, or empty
         *         if the node does not contain a well-formed result payload
         */
        public static Optional<Response> of(Node node) {
            return node.getChild("result")
                    .flatMap(Node::toContentBytes)
                    .flatMap(Response::of);
        }

        /**
         * Returns the identifier of the newsletter whose owner was changed.
         *
         * @return an {@link Optional} containing the identifier, or empty if
         *         the server did not echo it back
         */
        public Optional<String> id() {
            return Optional.ofNullable(id);
        }

        /**
         * Parses a {@link Response} from the raw JSON bytes of the
         * {@code <result>} child.
         *
         * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
         * mirrors the implicit unwrapping that WA Web performs on the GraphQL
         * response, extracting the {@code xwa2_newsletter_change_owner} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or empty
         *         if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
            // Parses the raw JSON payload, bailing out if fastjson2 returns null

            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
            // Descends into the standard GraphQL "data" envelope

            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
            // Extracts the mutation-specific root keyed by xwa2_newsletter_change_owner

            var root = data.getJSONObject("xwa2_newsletter_change_owner");
            if (root == null) {
                return Optional.empty();
            }

            var id = root.getString("id");

            return Optional.of(new Response(id));
        }
    }
}
