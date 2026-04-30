package com.github.auties00.cobalt.node.mex.json.user;

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
 * Parsed response for the get-username query. Carries the username record bound to the authenticated account.
 */
@WhatsAppWebModule(moduleName = "WAWebMexGetUsernameJob")
public final class GetUsernameMexResponse implements MexOperation.Response.Json {
    /**
     * The username record returned by the relay.
     */
    private final UsernameInfo usernameInfo;

    /**
     * Constructs a new response with the given username record.
     *
     * @param usernameInfo the username record returned by the relay
     */
    private GetUsernameMexResponse(UsernameInfo usernameInfo) {
        this.usernameInfo = usernameInfo;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetUsernameJob", exports = "mexGetUsernameQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<GetUsernameMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(GetUsernameMexResponse::of);
    }

    /**
     * Returns the username record returned by the relay.
     *
     * @return an {@link Optional} containing the record, or empty if absent
     */
    public Optional<UsernameInfo> usernameInfo() {
        return Optional.ofNullable(usernameInfo);
    }

    /**
     * Username record returned by the relay. Carries the assigned username, its registration state and the recovery
     * PIN hash.
     */
    public static final class UsernameInfo {
        /**
         * The username currently bound to the account.
         */
        private final String username;

        /**
         * The registration state of the username (for example {@code pending} or {@code active}).
         */
        private final String state;

        /**
         * The recovery PIN hash associated with the username.
         */
        private final String pin;

        /**
         * Constructs a new username record.
         *
         * @param username the username currently bound to the account
         * @param state the registration state of the username
         * @param pin the recovery PIN hash
         */
        private UsernameInfo(String username, String state, String pin) {
            this.username = username;
            this.state = state;
            this.pin = pin;
        }

        /**
         * Returns the username currently bound to the account.
         *
         * @return an {@link Optional} containing the username, or empty if absent
         */
        public Optional<String> username() {
            return Optional.ofNullable(username);
        }

        /**
         * Returns the registration state of the username.
         *
         * @return an {@link Optional} containing the state, or empty if absent
         */
        public Optional<String> state() {
            return Optional.ofNullable(state);
        }

        /**
         * Returns the recovery PIN hash.
         *
         * @return an {@link Optional} containing the PIN hash, or empty if absent
         */
        public Optional<String> pin() {
            return Optional.ofNullable(pin);
        }

        /**
         * Parses a username record from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
         */
        static Optional<UsernameInfo> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var username = obj.getString("username");
            var state = obj.getString("state");
            var pin = obj.getString("pin");
            return Optional.of(new UsernameInfo(username, state, pin));
        }

        /**
         * Parses a list of username records from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed records, empty if {@code arr} is {@code null}
         */
        static List<UsernameInfo> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<UsernameInfo>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Parses the response from the raw JSON payload bytes.
     *
     * @param json the raw JSON bytes from the {@code <result>} child
     * @return an {@link Optional} containing the parsed response, or empty if the envelope is missing
     */
    private static Optional<GetUsernameMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_username_get");
        if (root == null) {
            return Optional.empty();
        }

        var usernameInfo = UsernameInfo.of(root.getJSONObject("username_info")).orElse(null);

        return Optional.of(new GetUsernameMexResponse(usernameInfo));
    }
}
