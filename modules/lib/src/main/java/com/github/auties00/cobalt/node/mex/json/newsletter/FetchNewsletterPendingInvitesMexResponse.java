package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Response variant for {@link FetchNewsletterPendingInvitesMexRequest} carrying the parsed server reply.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterPendingInvitesJob")
public final class FetchNewsletterPendingInvitesMexResponse implements MexOperation.Response.Json {
    private final List<PendingAdminInvites> pendingAdminInvites;
    private final String id;

    private FetchNewsletterPendingInvitesMexResponse(List<PendingAdminInvites> pendingAdminInvites, String id) {
        this.pendingAdminInvites = pendingAdminInvites;
        this.id = id;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<FetchNewsletterPendingInvitesMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewsletterPendingInvitesMexResponse::of);
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
     * Parses a {@link FetchNewsletterPendingInvitesMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterPendingInvitesMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_newsletter_admin");
        if (root == null) {
            return Optional.empty();
        }

        var pendingAdminInvites = PendingAdminInvites.ofArray(root.getJSONArray("pending_admin_invites"));
        var id = root.getString("id");

        return Optional.of(new FetchNewsletterPendingInvitesMexResponse(pendingAdminInvites, id));
    }
}
