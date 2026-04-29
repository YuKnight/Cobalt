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
 * The parsed response for this MEX query.
 */
public final class GetUsernameMexResponse implements MexOperation.Response.Json {
    private final UsernameInfo usernameInfo;

    private GetUsernameMexResponse(UsernameInfo usernameInfo) {
        this.usernameInfo = usernameInfo;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexGetUsernameJob.mexGetUsernameQueryJob: reads the
     * {@code username_info} record with {@code username}, {@code state}
     * and {@code pin}.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetUsernameJob", exports = "mexGetUsernameQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<GetUsernameMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(GetUsernameMexResponse::of);
    }

    /**
     * Returns the {@code username_info} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<UsernameInfo> usernameInfo() {
        return Optional.ofNullable(usernameInfo);
    }

    /**
     * A parsed {@code UsernameInfo} object.
     */
    public static final class UsernameInfo {
        private final String username;
        private final String state;
        private final String pin;

        private UsernameInfo(String username, String state, String pin) {
            this.username = username;
            this.state = state;
            this.pin = pin;
        }

        /**
         * Returns the {@code username} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> username() {
            return Optional.ofNullable(username);
        }

        /**
         * Returns the {@code state} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> state() {
            return Optional.ofNullable(state);
        }

        /**
         * Returns the {@code pin} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> pin() {
            return Optional.ofNullable(pin);
        }

        /**
         * Parses a {@code UsernameInfo} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
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
         * Parses a list of {@code UsernameInfo} from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
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
