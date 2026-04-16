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
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Creates a newsletter admin invitation for the target user.
 *
 * <p>The owner of a newsletter issues this mutation to invite another user to become a newsletter administrator. The mutation records the invite on the server so the target user can later accept it via AcceptNewsletterAdminInviteMex.
 *
 * @implNote WAWebMexCreateNewsletterAdminInviteJob: adapts the {@code createNewsletterAdminInvite} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateNewsletterAdminInviteJob")
public sealed interface CreateNewsletterAdminInviteMex extends MexJsonOperation permits CreateNewsletterAdminInviteMex.Request, CreateNewsletterAdminInviteMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code CreateNewsletterAdminInvite} compiled mutation.
     *
     * @implNote WAWebMexCreateNewsletterAdminInviteJobMutation.graphql: corresponds to the compiled
     * document id registered for the {@code createNewsletterAdminInvite} mutation.
     */
    String QUERY_ID = "9387141988078609";

    /**
     * The request variant of {@link CreateNewsletterAdminInviteMex} that serialises the
     * mutation variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite: adapts the {@code variables}
     * object constructed inline in the JS implementation into a dedicated
     * Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexCreateNewsletterAdminInviteJob")
    final class Request implements CreateNewsletterAdminInviteMex {
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
         * @implNote WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite: WA Web constructs the
         * {@code variables} object inline and delegates to
         * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and wraps it through
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexCreateNewsletterAdminInviteJob", exports = "createNewsletterAdminInvite",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope

            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
                // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery

                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
                // Emits the newsletter_id variable when present

                if (newsletterId != null) {
                    writer.writeName("newsletter_id");
                    writer.writeColon();
                    writer.writeString(newsletterId);
                }

                // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
                // Emits the user_id variable when present

                if (userId != null) {
                    writer.writeName("user_id");
                    writer.writeColon();
                    writer.writeString(userId);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
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
     * The response variant of {@link CreateNewsletterAdminInviteMex} that exposes the data
     * returned by the server after a successful mutation.
     *
     * @implNote WAWebMexCreateNewsletterAdminInviteJob: adapts the JSON root returned by the GraphQL
     * mutation into a Java value object.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexCreateNewsletterAdminInviteJob")
    final class Response implements CreateNewsletterAdminInviteMex {
        private final Long inviteExpirationTime;
        private final String id;

        private Response(Long inviteExpirationTime, String id) {
            this.inviteExpirationTime = inviteExpirationTime;
            this.id = id;
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
         * @implNote WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite: WA Web relies on the
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
         * Returns the {@code invite_expiration_time} field.
         *
         * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
         */
        public Optional<Instant> inviteExpirationTime() {
            return Optional.ofNullable(inviteExpirationTime).map(Instant::ofEpochSecond);
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
         * @implNote WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite: mirrors the implicit
         * unwrapping that WA Web performs on the GraphQL response,
         * extracting the {@code xwa2_newsletter_admin_invite_create} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
            // Parses the raw JSON payload, bailing out if fastjson2 returns null

            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
            // Descends into the standard GraphQL "data" envelope

            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
            // Extracts the operation-specific root keyed by xwa2_newsletter_admin_invite_create

            var root = data.getJSONObject("xwa2_newsletter_admin_invite_create");
            if (root == null) {
                return Optional.empty();
            }

            var inviteExpirationTime = root.getLong("invite_expiration_time");
            var id = root.getString("id");

            return Optional.of(new Response(inviteExpirationTime, id));
        }
    }
}
