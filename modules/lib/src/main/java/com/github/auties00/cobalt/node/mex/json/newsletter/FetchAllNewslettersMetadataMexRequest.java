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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fetches metadata for every newsletter followed by the authenticated user.
 *
 * <p>WA Web uses this query during login and periodic syncs to hydrate the local newsletter list. The response provides a collection of newsletter entries each with thread metadata, viewer role, and state.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchAllNewslettersMetadataJob")
public final class FetchAllNewslettersMetadataMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchAllNewslettersMetadata} compiled query.
     */
    public static final String QUERY_ID = "25399611239711790";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchAllNewsletters
     * operation.
     */
    public static final String OPERATION_NAME = "mexFetchAllNewsletters";
    private final Boolean fetchWamoSub;
    private final Boolean fetchStatusMetadata;

    /**
     * Constructs a request that selects only the {@code fetch_wamo_sub}
     * gating flag.
     * @param fetchWamoSub the value of the {@code fetch_wamo_sub} GraphQL
     *                     variable, or {@code null} to omit the field
     */
    public FetchAllNewslettersMetadataMexRequest(Boolean fetchWamoSub) {
        this(fetchWamoSub, null);
    }

    /**
     * Constructs a request with both GraphQL gating variables.
     *
     * @param fetchWamoSub        the value of the {@code fetch_wamo_sub}
     *                            variable, or {@code null} to omit
     * @param fetchStatusMetadata the value of the
     *                            {@code fetch_status_metadata} variable,
     *                            or {@code null} to omit
     */
    public FetchAllNewslettersMetadataMexRequest(Boolean fetchWamoSub, Boolean fetchStatusMetadata) {
        this.fetchWamoSub = fetchWamoSub;
        this.fetchStatusMetadata = fetchStatusMetadata;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @return the constant {@link #QUERY_ID}, never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name projected from
     * {@link #OPERATION_NAME}.
     *
     * @return the constant {@link #OPERATION_NAME}, never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the
     * WhatsApp relay.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAllNewslettersMetadataJob", exports = "mexFetchAllNewsletters",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (fetchWamoSub != null) {
                writer.writeName("fetch_wamo_sub");
                writer.writeColon();
                writer.writeBool(fetchWamoSub);
            }
            if (fetchStatusMetadata != null) {
                writer.writeName("fetch_status_metadata");
                writer.writeColon();
                writer.writeBool(fetchStatusMetadata);
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
