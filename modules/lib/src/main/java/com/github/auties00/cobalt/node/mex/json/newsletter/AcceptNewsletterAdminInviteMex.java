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
 * Accepts a pending newsletter admin invite for the authenticated user.
 *
 * <p>When a newsletter owner sends an admin invitation to another user, the
 * recipient must acknowledge the invite via this MEX mutation before becoming
 * a newsletter administrator. Accepting the invite upgrades the user's role
 * on the newsletter and removes the pending entry from the invitation list.
 *
 * <p>The request carries the target newsletter identifier and the response
 * returns the same identifier on success so callers can correlate the reply
 * with the originating request.
 *
 * @implNote WAWebMexAcceptNewsletterAdminInviteJob: adapts the
 * {@code acceptNewsletterAdminInvite} GraphQL mutation, which WA Web sends
 * through {@code WAWebMexClient.fetchQuery} with a single {@code newsletter_id}
 * variable and unwraps the {@code xwa2_newsletter_admin_invite_accept} root
 * of the response. Cobalt models the request and response as sibling variants
 * of a sealed interface rather than a free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob")
public sealed interface AcceptNewsletterAdminInviteMex extends MexJsonOperation permits AcceptNewsletterAdminInviteMex.Request, AcceptNewsletterAdminInviteMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code AcceptNewsletterAdminInvite} compiled mutation.
     *
     * <p>This identifier is embedded as the {@code query_id} attribute of the
     * outbound {@code <query>} stanza so the server can route the payload to
     * the right GraphQL operation without transmitting the full query text.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJobMutation.graphql:
     * corresponds to the compiled document id registered for the
     * {@code acceptNewsletterAdminInvite} mutation.
     */
    String QUERY_ID = "9580828702035549";

    /**
     * The request variant of {@link AcceptNewsletterAdminInviteMex} that
     * serialises the mutation variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
     * adapts the {@code variables} object constructed inline inside
     * {@code fetchQuery} into an explicit Java record-like class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob")
    final class Request implements AcceptNewsletterAdminInviteMex {
        /**
         * The identifier of the newsletter whose admin invite is being accepted.
         *
         * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
         * mirrors the {@code newsletter_id} variable passed to
         * {@code fetchQuery(l, {newsletter_id: e})}.
         */
        @WhatsAppWebExport(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob", exports = "acceptNewsletterAdminInvite",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final String newsletterId;

        /**
         * Creates a request targeting the given newsletter.
         *
         * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
         * mirrors the single positional parameter of the JS function which
         * receives the newsletter id and bundles it into the
         * {@code variables} object.
         * @param newsletterId the identifier of the newsletter whose admin
         *                     invite should be accepted
         */
        @WhatsAppWebExport(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob", exports = "acceptNewsletterAdminInvite",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Request(String newsletterId) {
            this.newsletterId = newsletterId;
        }

        /**
         * Builds the IQ stanza that dispatches this mutation to the WhatsApp
         * relay.
         *
         * <p>The returned {@link NodeBuilder} has not yet been built, so
         * callers may add routing attributes such as the IQ id before sending.
         * The {@code newsletter_id} field is only emitted when it is
         * non-{@code null} so that the server-side schema never receives an
         * explicit null variable.
         *
         * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
         * WA Web constructs the {@code variables} object inline and delegates
         * to {@code WAWebMexClient.fetchQuery} which performs the JSON
         * serialisation and stanza wrapping. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and delegates stanza wrapping to
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables, ready to be mutated and built
         */
        @WhatsAppWebExport(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob", exports = "acceptNewsletterAdminInvite",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope

            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
                // Begins the outer envelope and the nested "variables" object required by WAWebMexClient.fetchQuery

                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();

                // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
                // Emits the newsletter_id variable when present, skipping it otherwise so the server schema defaults apply

                if (newsletterId != null) {
                    writer.writeName("newsletter_id");
                    writer.writeColon();
                    writer.writeString(newsletterId);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
                // Flushes the JSON into a StringWriter and wraps it in the shared MEX IQ envelope

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
     * The response variant of {@link AcceptNewsletterAdminInviteMex} that
     * exposes the identifier echoed back by the server once the invite is
     * accepted.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob: adapts the return
     * value of the GraphQL mutation, which is the object keyed under
     * {@code xwa2_newsletter_admin_invite_accept} in the JSON response body.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob")
    final class Response implements AcceptNewsletterAdminInviteMex {
        /**
         * The identifier of the newsletter whose admin invite was accepted, as
         * echoed by the server.
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
         * <p>The IQ result contains a {@code <result>} child whose binary
         * content carries the JSON reply. The reply is expected to contain a
         * {@code data.xwa2_newsletter_admin_invite_accept} object with a
         * single {@code id} field.
         *
         * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
         * WA Web relies on the GraphQL client to unwrap the response. Cobalt
         * performs the unwrapping manually because the stanza is returned by
         * the IQ framework rather than a JS promise.
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
         * Returns the identifier of the newsletter whose invite was accepted.
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
         * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
         * mirrors the implicit unwrapping that WA Web performs on the GraphQL
         * response, extracting the {@code xwa2_newsletter_admin_invite_accept}
         * root before returning.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or empty
         *         if the envelope lacks a {@code data} or result root
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
            // Parses the raw JSON payload into a fastjson2 JSONObject and returns empty if the parser yields null

            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
            // Descends into the standard GraphQL "data" envelope, returning empty when the server reported only errors

            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
            // Extracts the mutation-specific root object, which the server keys by xwa2_newsletter_admin_invite_accept

            var root = data.getJSONObject("xwa2_newsletter_admin_invite_accept");
            if (root == null) {
                return Optional.empty();
            }

            var id = root.getString("id");

            return Optional.of(new Response(id));
        }
    }
}
