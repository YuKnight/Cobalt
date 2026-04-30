package com.github.auties00.cobalt.node.mex.json.user;

import com.alibaba.fastjson2.JSON;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Parsed response for the username-availability check. Mirrors the {@code XWA2UsernameCheckResponse} GraphQL type with
 * a status token and a list of suggested alternative usernames.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUsernameAvailability")
public final class UsernameAvailabilityMexResponse implements MexOperation.Response.Json {
    /**
     * The status token the relay returns when the candidate username is available for registration. WA Web compares
     * {@code result === "SUCCESS"} to project the boolean availability flag.
     */
    public static final String RESULT_SUCCESS = "SUCCESS";

    /**
     * The raw status token reported by the relay.
     */
    private final String result;

    /**
     * The list of alternative usernames suggested when the candidate is unavailable.
     */
    private final List<String> suggestedUsernames;

    /**
     * Constructs a new response with the given fields.
     *
     * @param result the status token reported by the relay
     * @param suggestedUsernames the list of suggested alternative usernames
     */
    private UsernameAvailabilityMexResponse(String result, List<String> suggestedUsernames) {
        this.result = result;
        this.suggestedUsernames = suggestedUsernames;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailability", exports = "mexCheckUsernameAvailabilityQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<UsernameAvailabilityMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(UsernameAvailabilityMexResponse::of);
    }

    /**
     * Returns the raw status token reported by the relay. Known values include {@link #RESULT_SUCCESS} along with
     * implementation-defined error tokens surfaced when the candidate is rejected.
     *
     * @return an {@link Optional} containing the status token, or empty if absent
     */
    public Optional<String> result() {
        return Optional.ofNullable(result);
    }

    /**
     * Returns the list of alternative usernames suggested by the relay. The list is unmodifiable and never
     * {@code null}. An empty list is returned when the relay does not include any suggestions.
     *
     * @return the unmodifiable list of suggested usernames
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailability", exports = "mexCheckUsernameAvailabilityQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public List<String> suggestedUsernames() {
        return suggestedUsernames;
    }

    /**
     * Returns whether the queried username is available for registration. Mirrors WA Web's
     * {@code isUsernameAvailable: result === "SUCCESS"} projection.
     *
     * @return {@code true} if {@link #result()} equals {@link #RESULT_SUCCESS}, {@code false} otherwise
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailability", exports = "mexCheckUsernameAvailabilityQueryJob",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isUsernameAvailable() {
        return RESULT_SUCCESS.equals(result);
    }

    /**
     * Parses the response from the raw JSON payload bytes.
     *
     * @param json the raw JSON bytes from the {@code <result>} child
     * @return an {@link Optional} containing the parsed response, or empty if the envelope is missing
     */
    private static Optional<UsernameAvailabilityMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_username_check");
        if (root == null) {
            return Optional.empty();
        }

        var result = root.getString("result");

        // Spread the suggestions array into the response list, matching WA Web's u.push.apply(u, c.suggestions).
        var suggestionsArray = root.getJSONArray("suggestions");
        List<String> suggestedUsernames;
        if (suggestionsArray == null) {
            suggestedUsernames = List.of();
        } else {
            var collected = new ArrayList<String>(suggestionsArray.size());
            for (var i = 0; i < suggestionsArray.size(); i++) {
                var entry = suggestionsArray.getString(i);
                if (entry != null) {
                    collected.add(entry);
                }
            }
            suggestedUsernames = Collections.unmodifiableList(collected);
        }

        return Optional.of(new UsernameAvailabilityMexResponse(result, suggestedUsernames));
    }
}
