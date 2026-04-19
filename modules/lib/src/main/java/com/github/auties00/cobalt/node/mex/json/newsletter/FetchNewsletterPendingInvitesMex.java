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
 * Fetches the list of pending newsletter admin invites awaiting the user's acceptance.
 *
 * <p>Users may be invited to administer multiple newsletters; this query returns all invitations that are still pending so the client can surface them in the notifications surface.
 *
 * @implNote WAWebMexFetchNewsletterPendingInvitesJob: adapts the {@code mexFetchNewsletterPendingInvites} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterPendingInvitesJob")
public sealed interface FetchNewsletterPendingInvitesMex extends MexJsonOperation permits FetchNewsletterPendingInvitesMex.Request, FetchNewsletterPendingInvitesMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterPendingInvites} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterPendingInvitesJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterPendingInvites} query.
     */
    String QUERY_ID = "9783111038412085";

    /**
     * The request variant of {@link FetchNewsletterPendingInvitesMex} that serialises the
     * query variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites: adapts the {@code variables}
     * object constructed inline in the JS implementation into a dedicated
     * Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterPendingInvitesJob")
    final class Request implements FetchNewsletterPendingInvitesMex {
        private final String newsletterId;

        public Request(String newsletterId) {
            this.newsletterId = newsletterId;
        }

        /**
         * Builds the IQ stanza that dispatches this operation to the
         * WhatsApp relay.
         *
         * @implNote WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites: WA Web constructs the
         * {@code variables} object inline and delegates to
         * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and wraps it through
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterPendingInvitesJob", exports = "mexFetchNewsletterPendingInvites",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites
                // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                // WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites
                // Emits the newsletter_id variable when present
                if (newsletterId != null) {
                    writer.writeName("newsletter_id");
                    writer.writeColon();
                    writer.writeString(newsletterId);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites
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
     * The response variant of {@link FetchNewsletterPendingInvitesMex} that exposes the data
     * returned by the server after a successful query.
     *
     * @implNote WAWebMexFetchNewsletterPendingInvitesJob: adapts the JSON root returned by the GraphQL
     * query into a Java value object.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterPendingInvitesJob")
    final class Response implements FetchNewsletterPendingInvitesMex {
        private final List<PendingAdminInvites> pendingAdminInvites;
        private final String id;

        private Response(List<PendingAdminInvites> pendingAdminInvites, String id) {
            this.pendingAdminInvites = pendingAdminInvites;
            this.id = id;
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
         * @implNote WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites: WA Web relies on the
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
         * Returns the {@code pending_admin_invites} field.
         *
         * @return the list of values, empty if absent
         */
        public List<PendingAdminInvites> pendingAdminInvites() {
            return pendingAdminInvites;
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
         * A parsed {@code PendingAdminInvites} object.
         */
        public static final class PendingAdminInvites {
            private final User user;

            private PendingAdminInvites(User user) {
                this.user = user;
            }

            /**
             * Returns the {@code user} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<User> user() {
                return Optional.ofNullable(user);
            }

            /**
             * A parsed {@code User} object.
             */
            public static final class User {
                private final String pn;
                private final String id;

                private User(String pn, String id) {
                    this.pn = pn;
                    this.id = id;
                }

                /**
                 * Returns the {@code pn} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> pn() {
                    return Optional.ofNullable(pn);
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
                 * Parses a {@code User} from the given JSON object.
                 *
                 * @param obj the JSON object to parse
                 * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
                 */
                static Optional<User> of(JSONObject obj) {
                    if (obj == null) {
                        return Optional.empty();
                    }

                    var pn = obj.getString("pn");
                    var id = obj.getString("id");
                    return Optional.of(new User(pn, id));
                }

                /**
                 * Parses a list of {@code User} from the given JSON array.
                 *
                 * @param arr the JSON array to parse
                 * @return the list of parsed results, empty if {@code arr} is {@code null}
                 */
                static List<User> ofArray(JSONArray arr) {
                    if (arr == null) {
                        return List.of();
                    }

                    var result = new ArrayList<User>(arr.size());
                    for (var i = 0; i < arr.size(); i++) {
                        of(arr.getJSONObject(i)).ifPresent(result::add);
                    }
                    return result;
                }
            }

            /**
             * Parses a {@code PendingAdminInvites} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<PendingAdminInvites> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var user = User.of(obj.getJSONObject("user")).orElse(null);
                return Optional.of(new PendingAdminInvites(user));
            }

            /**
             * Parses a list of {@code PendingAdminInvites} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<PendingAdminInvites> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<PendingAdminInvites>(arr.size());
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
         * @implNote WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites: mirrors the implicit
         * unwrapping that WA Web performs on the GraphQL response,
         * extracting the {@code xwa2_newsletter_admin} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites
            // Parses the raw JSON payload, bailing out if fastjson2 returns null
            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites
            // Descends into the standard GraphQL "data" envelope
            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexFetchNewsletterPendingInvitesJob.mexFetchNewsletterPendingInvites
            // Extracts the operation-specific root keyed by xwa2_newsletter_admin
            var root = data.getJSONObject("xwa2_newsletter_admin");
            if (root == null) {
                return Optional.empty();
            }

            var pendingAdminInvites = PendingAdminInvites.ofArray(root.getJSONArray("pending_admin_invites"));
            var id = root.getString("id");

            return Optional.of(new Response(pendingAdminInvites, id));
        }
    }
}
