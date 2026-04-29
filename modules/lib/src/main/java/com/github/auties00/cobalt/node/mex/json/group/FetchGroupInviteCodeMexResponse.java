package com.github.auties00.cobalt.node.mex.json.group;

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
public final class FetchGroupInviteCodeMexResponse implements MexOperation.Response.Json {
    private final String inviteCode;
    private final String id;

    private FetchGroupInviteCodeMexResponse(String inviteCode, String id) {
        this.inviteCode = inviteCode;
        this.id = id;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexFetchGroupInviteCodeJobQuery.graphql: reads
     * {@code data.xwa2_group_query_by_id.invite_code} and the group
     * {@code id}.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchGroupInviteCodeJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchGroupInviteCodeMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchGroupInviteCodeMexResponse::of);
    }

    /**
     * Returns the {@code invite_code} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> inviteCode() {
        return Optional.ofNullable(inviteCode);
    }

    /**
     * Returns the {@code id} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    private static Optional<FetchGroupInviteCodeMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_group_query_by_id");
        if (root == null) {
            return Optional.empty();
        }

        var inviteCode = root.getString("invite_code");
        var id = root.getString("id");

        return Optional.of(new FetchGroupInviteCodeMexResponse(inviteCode, id));
    }
}
