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
 * The parsed response for this MEX query.
 *
 * <p>The fields mirror the {@code XWA2UsernameCheckResponse} GraphQL type
 * selected by {@code WAWebMexUsernameAvailabilityQuery.graphql}: a
 * {@code result} status string and a list of suggested alternative
 * usernames.
 */
public final class UsernameAvailabilityMexResponse implements MexOperation.Response.Json {
    /**
     * The {@code result} token the relay returns when the candidate
     * username is available for registration.
     *
     * @implNote {@code WAWebMexUsernameAvailability.mexCheckUsernameAvailabilityQueryJob}:
     *           the JS helper compares
     *           {@code s.xwa2_username_check?.result === "SUCCESS"} to
     *           project the boolean {@code isUsernameAvailable} flag;
     *           Cobalt mirrors that literal verbatim through this constant.
     */
    public static final String RESULT_SUCCESS = "SUCCESS";

    private final String result;
    private final List<String> suggestedUsernames;

    private UsernameAvailabilityMexResponse(String result, List<String> suggestedUsernames) {
        this.result = result;
        this.suggestedUsernames = suggestedUsernames;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexUsernameAvailability.mexCheckUsernameAvailabilityQueryJob:
     * reads the {@code result} and {@code suggestions} fields from
     * {@code data.xwa2_username_check}.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailability", exports = "mexCheckUsernameAvailabilityQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<UsernameAvailabilityMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(UsernameAvailabilityMexResponse::of);
    }

    /**
     * Returns the raw {@code result} status token reported by the relay.
     *
     * <p>Known values include {@code "SUCCESS"} (the username is available
     * for registration) along with implementation-defined error tokens
     * surfaced by the WhatsApp backend when the candidate is rejected.
     *
     * @return an {@link Optional} containing the status token, or empty if
     *         absent
     */
    public Optional<String> result() {
        return Optional.ofNullable(result);
    }

    /**
     * Returns the list of alternative usernames suggested by the relay
     * when the candidate is not available.
     *
     * <p>The list is unmodifiable and never {@code null}; an empty list is
     * returned when the relay does not include any suggestions.
     *
     * @implNote WAWebMexUsernameAvailability.mexCheckUsernameAvailabilityQueryJob:
     * {@code u.push.apply(u, c.suggestions)} flattens the
     * {@code suggestions} array into the {@code suggestedUsernames}
     * field returned to callers.
     * @return the unmodifiable list of suggested usernames
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailability", exports = "mexCheckUsernameAvailabilityQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public List<String> suggestedUsernames() {
        return suggestedUsernames;
    }

    /**
     * Returns whether the queried username is available for registration.
     *
     * @implNote WAWebMexUsernameAvailability.mexCheckUsernameAvailabilityQueryJob:
     * mirrors {@code isUsernameAvailable: result === "SUCCESS"} from the
     * JS job's return shape.
     * @return {@code true} if {@link #result()} equals {@link #RESULT_SUCCESS}
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailability", exports = "mexCheckUsernameAvailabilityQueryJob",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isUsernameAvailable() {
        return RESULT_SUCCESS.equals(result);
    }

    private static Optional<UsernameAvailabilityMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexUsernameAvailability.mexCheckUsernameAvailabilityQueryJob
        // The fetchQuery wrapper unwraps the GraphQL `data` envelope before returning.
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexUsernameAvailability.mexCheckUsernameAvailabilityQueryJob: s.xwa2_username_check
        var root = data.getJSONObject("xwa2_username_check");
        if (root == null) {
            return Optional.empty();
        }

        // WAWebMexUsernameAvailability.mexCheckUsernameAvailabilityQueryJob: r.result
        var result = root.getString("result");

        // WAWebMexUsernameAvailability.mexCheckUsernameAvailabilityQueryJob:
        // u.push.apply(u, c.suggestions) -- spread `suggestions` array into the result list.
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
