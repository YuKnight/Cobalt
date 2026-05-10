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
 * Reports whether a group is flagged as "internal" by the WhatsApp relay.
 *
 * <p>Internal groups are the Meta-side staff or testing groups whose
 * lifecycle differs from regular consumer groups; the flag is exposed under
 * the {@code XWA2*Properties.internal} scalar across all four group
 * variants ({@code XWA2GroupRegularGroup}, {@code XWA2CommunityGroup},
 * {@code XWA2CommunityDefaultSubGroup}, {@code XWA2CommunitySubGroup}).
 * The query collapses the four inline fragments into a single boolean by
 * walking the response shape that the relay actually populates.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchGroupIsInternalJob")
public final class FetchGroupIsInternalMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the compiled {@code mexFetchGroupIsInternal} query.
     */
    public static final String QUERY_ID = "34119218944390847";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this query, mirroring the {@code params.name} value of
     * the compiled {@code mexFetchGroupIsInternal} operation.
     */
    public static final String OPERATION_NAME = "mexFetchGroupIsInternal";
    /**
     * The target group identifier bound to the {@code id} GraphQL variable.
     */
    private final String groupId;

    /**
     * Constructs a request that asks the relay whether the given group is
     * flagged as internal.
     * @param groupId the group identifier emitted as the {@code id} variable
     */
    public FetchGroupIsInternalMexRequest(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the compiled GraphQL query identifier.
     *
     * @return the constant {@link #QUERY_ID}; never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name.
     *
     * @return the constant {@link #OPERATION_NAME}; never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the WhatsApp relay.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchGroupIsInternalJob", exports = "mexFetchGroupIsInternal",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (groupId != null) {
                writer.writeName("id");
                writer.writeColon();
                writer.writeString(groupId);
            }
            writer.endObject();
            writer.endObject();

            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
