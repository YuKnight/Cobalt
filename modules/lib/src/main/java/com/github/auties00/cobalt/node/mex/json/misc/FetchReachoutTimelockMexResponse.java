package com.github.auties00.cobalt.node.mex.json.misc;

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
import java.util.Optional;

/**
 * The parsed response for this MEX query.
 */
public final class FetchReachoutTimelockMexResponse implements MexOperation.Response.Json {
    private final Boolean isActive;
    private final String timeEnforcementEnds;
    private final String enforcementType;

    private FetchReachoutTimelockMexResponse(Boolean isActive, String timeEnforcementEnds, String enforcementType) {
        this.isActive = isActive;
        this.timeEnforcementEnds = timeEnforcementEnds;
        this.enforcementType = enforcementType;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexFetchReachoutTimelockJobQuery.graphql: reads the
     * {@code is_active}, {@code time_enforcement_ends} and
     * {@code enforcement_type} fields from the response payload.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchReachoutTimelockJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchReachoutTimelockMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchReachoutTimelockMexResponse::of);
    }

    /**
     * Returns the {@code is_active} field.
     *
     * @return {@code true} if the value is present and true, {@code false} otherwise
     */
    public boolean isActive() {
        return isActive != null && isActive;
    }

    /**
     * Returns the {@code time_enforcement_ends} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> timeEnforcementEnds() {
        return Optional.ofNullable(timeEnforcementEnds);
    }

    /**
     * Returns the {@code enforcement_type} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> enforcementType() {
        return Optional.ofNullable(enforcementType);
    }

    private static Optional<FetchReachoutTimelockMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_fetch_account_reachout_timelock");
        if (root == null) {
            return Optional.empty();
        }

        var isActive = root.getBoolean("is_active");
        var timeEnforcementEnds = root.getString("time_enforcement_ends");
        var enforcementType = root.getString("enforcement_type");

        return Optional.of(new FetchReachoutTimelockMexResponse(isActive, timeEnforcementEnds, enforcementType));
    }
}
