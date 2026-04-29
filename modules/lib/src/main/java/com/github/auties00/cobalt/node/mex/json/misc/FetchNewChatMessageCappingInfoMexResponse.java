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
public final class FetchNewChatMessageCappingInfoMexResponse implements MexOperation.Response.Json {
    private final String totalQuota;
    private final String usedQuota;
    private final String cycleStartTimestamp;
    private final String cycleEndTimestamp;
    private final String serverSentTimestamp;
    private final String oteStatus;
    private final String mvStatus;
    private final String cappingStatus;

    private FetchNewChatMessageCappingInfoMexResponse(String totalQuota, String usedQuota, String cycleStartTimestamp, String cycleEndTimestamp, String serverSentTimestamp, String oteStatus, String mvStatus, String cappingStatus) {
        this.totalQuota = totalQuota;
        this.usedQuota = usedQuota;
        this.cycleStartTimestamp = cycleStartTimestamp;
        this.cycleEndTimestamp = cycleEndTimestamp;
        this.serverSentTimestamp = serverSentTimestamp;
        this.oteStatus = oteStatus;
        this.mvStatus = mvStatus;
        this.cappingStatus = cappingStatus;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexFetchNewChatMessageCappingInfoJobQuery.graphql:
     * reads the quota counters and per-policy status flags from the
     * response payload.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewChatMessageCappingInfoJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchNewChatMessageCappingInfoMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewChatMessageCappingInfoMexResponse::of);
    }

    /**
     * Returns the {@code total_quota} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> totalQuota() {
        return Optional.ofNullable(totalQuota);
    }

    /**
     * Returns the {@code used_quota} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> usedQuota() {
        return Optional.ofNullable(usedQuota);
    }

    /**
     * Returns the {@code cycle_start_timestamp} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> cycleStartTimestamp() {
        return Optional.ofNullable(cycleStartTimestamp);
    }

    /**
     * Returns the {@code cycle_end_timestamp} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> cycleEndTimestamp() {
        return Optional.ofNullable(cycleEndTimestamp);
    }

    /**
     * Returns the {@code server_sent_timestamp} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> serverSentTimestamp() {
        return Optional.ofNullable(serverSentTimestamp);
    }

    /**
     * Returns the {@code ote_status} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> oteStatus() {
        return Optional.ofNullable(oteStatus);
    }

    /**
     * Returns the {@code mv_status} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> mvStatus() {
        return Optional.ofNullable(mvStatus);
    }

    /**
     * Returns the {@code capping_status} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> cappingStatus() {
        return Optional.ofNullable(cappingStatus);
    }

    private static Optional<FetchNewChatMessageCappingInfoMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_message_capping_info");
        if (root == null) {
            return Optional.empty();
        }

        var totalQuota = root.getString("total_quota");
        var usedQuota = root.getString("used_quota");
        var cycleStartTimestamp = root.getString("cycle_start_timestamp");
        var cycleEndTimestamp = root.getString("cycle_end_timestamp");
        var serverSentTimestamp = root.getString("server_sent_timestamp");
        var oteStatus = root.getString("ote_status");
        var mvStatus = root.getString("mv_status");
        var cappingStatus = root.getString("capping_status");

        return Optional.of(new FetchNewChatMessageCappingInfoMexResponse(totalQuota, usedQuota, cycleStartTimestamp, cycleEndTimestamp, serverSentTimestamp, oteStatus, mvStatus, cappingStatus));
    }
}
