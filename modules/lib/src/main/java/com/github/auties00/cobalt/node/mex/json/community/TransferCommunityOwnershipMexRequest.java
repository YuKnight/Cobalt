package com.github.auties00.cobalt.node.mex.json.community;

import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.mex.MexOperation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 * Transfers ownership of a community from the current owner to another admin,
 * updating the server-side role mapping for the group.
 *
 * <p>This mutation is issued from the "transfer ownership" action in the
 * community settings. The response echoes the affected group id and the
 * resulting LID migration state (addressing mode) so that the client can
 * update its local view of the community before replaying cached actions.
 */
@WhatsAppWebModule(moduleName = "WAWebMexTransferCommunityOwnershipJob")
public final class TransferCommunityOwnershipMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled
     * {@code WAWebMexTransferCommunityOwnershipJobMutation} GraphQL mutation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexTransferCommunityOwnershipJobMutation.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "29643783178598899";

    /**
     * The GraphQL operation name fed into {@code MexPerfTracker.setOperationName}
     * when this mutation is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexTransferCommunityOwnershipJob", exports = "mexTransferCommunityOwnershipJob",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexTransferCommunityOwnershipJob";

    private final String input;

    /**
     * Constructs a new request carrying the serialised input payload with
     * the community id and the new owner's id.
     *
     * @param input the serialised input variable, may be {@code null} to
     *              omit
     */
    public TransferCommunityOwnershipMexRequest(String input) {
        this.input = input;
    }

    /**
     * Returns the compiled GraphQL query identifier.
     *
     * @return the constant {@link #QUERY_ID}, never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name.
     *
     * @return the constant {@link #OPERATION_NAME}, never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Serialises the GraphQL variables and wraps them in a {@code w:mex} IQ
     * stanza.
     *
     * @return the IQ {@link NodeBuilder} ready to be built and dispatched
     */
    @WhatsAppWebExport(moduleName = "WAWebMexTransferCommunityOwnershipJob", exports = "mexTransferCommunityOwnershipJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (input != null) {
                writer.writeName("input");
                writer.writeColon();
                writer.writeString(input);
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
