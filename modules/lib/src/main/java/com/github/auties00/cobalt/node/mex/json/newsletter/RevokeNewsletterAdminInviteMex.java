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
 * Revokes a previously issued newsletter admin invitation.
 *
 * <p>Newsletter owners can cancel pending admin invites before the target accepts them. Once revoked the invite disappears from the target user's pending invites list.
 *
 * @implNote WAWebMexRevokeNewsletterAdminInviteJob: adapts the {@code revokeNewsletterAdminInvite} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexRevokeNewsletterAdminInviteJob")
public sealed interface RevokeNewsletterAdminInviteMex extends MexJsonOperation permits RevokeNewsletterAdminInviteMex.Request, RevokeNewsletterAdminInviteMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code RevokeNewsletterAdminInvite} compiled mutation.
     *
     * @implNote WAWebMexRevokeNewsletterAdminInviteJobMutation.graphql: corresponds to the compiled
     * document id registered for the {@code revokeNewsletterAdminInvite} mutation.
     */
    String QUERY_ID = "9656078347839416";

    /**
     * The request variant of {@link RevokeNewsletterAdminInviteMex} that serialises the
     * mutation variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite: adapts the {@code variables}
     * object constructed inline in the JS implementation into a dedicated
     * Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexRevokeNewsletterAdminInviteJob")
    final class Request implements RevokeNewsletterAdminInviteMex {
        private final String newsletterId;
        private final String userId;

        public Request(String newsletterId, String userId) {
            this.newsletterId = newsletterId;
            this.userId = userId;
        }

        /**
         * Builds the IQ stanza that dispatches this operation to the
         * WhatsApp relay.
         *
         * @implNote WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite: WA Web constructs the
         * {@code variables} object inline and delegates to
         * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and wraps it through
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexRevokeNewsletterAdminInviteJob", exports = "revokeNewsletterAdminInvite",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope

            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite
                // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery

                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                // WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite
                // Emits the newsletter_id variable when present

                if (newsletterId != null) {
                    writer.writeName("newsletter_id");
                    writer.writeColon();
                    writer.writeString(newsletterId);
                }

                // WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite
                // Emits the user_id variable when present

                if (userId != null) {
                    writer.writeName("user_id");
                    writer.writeColon();
                    writer.writeString(userId);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite
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
     * The response variant of {@link RevokeNewsletterAdminInviteMex} that exposes the data
     * returned by the server after a successful mutation.
     *
     * @implNote WAWebMexRevokeNewsletterAdminInviteJob: adapts the JSON root returned by the GraphQL
     * mutation into a Java value object.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexRevokeNewsletterAdminInviteJob")
    final class Response implements RevokeNewsletterAdminInviteMex {
        private final String id;

        private Response(String id) {
            this.id = id;
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
         * @implNote WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite: WA Web relies on the
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
         * @implNote WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite: mirrors the implicit
         * unwrapping that WA Web performs on the GraphQL response,
         * extracting the {@code xwa2_newsletter_admin_invite_revoke} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite
            // Parses the raw JSON payload, bailing out if fastjson2 returns null

            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite
            // Descends into the standard GraphQL "data" envelope

            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexRevokeNewsletterAdminInviteJob.revokeNewsletterAdminInvite
            // Extracts the operation-specific root keyed by xwa2_newsletter_admin_invite_revoke

            var root = data.getJSONObject("xwa2_newsletter_admin_invite_revoke");
            if (root == null) {
                return Optional.empty();
            }

            var id = root.getString("id");

            return Optional.of(new Response(id));
        }
    }
}
