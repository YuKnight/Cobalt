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
import java.util.OptionalLong;

/**
 * Fetches full metadata for a single newsletter by id or invite key.
 *
 * <p>This is the primary query used to hydrate a newsletter's metadata on-demand. Depending on the input key type, the server returns metadata for an already-joined newsletter (JID) or a newsletter discovered through an invite link.
 *
 * @implNote WAWebMexFetchNewsletterJob: adapts the {@code mexGetNewsletter} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterJob")
public final class FetchNewsletterMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletter} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexGetNewsletter} query.
     */
    public static final String QUERY_ID = "35452404184358876";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexGetNewsletter
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexGetNewsletter"}.
     */
    public static final String OPERATION_NAME = "mexGetNewsletter";
    private final Boolean fetchCreationTime;
    private final Boolean fetchFullImage;
    private final Boolean fetchStatusMetadata;
    private final Boolean fetchViewerMetadata;
    private final Boolean fetchWamoSub;
    private final Input input;

    /**
     * Constructs a request without the {@code fetch_status_metadata} flag.
     *
     * @param fetchCreationTime   the {@code fetch_creation_time} flag, may be {@code null}
     * @param fetchFullImage      the {@code fetch_full_image} flag, may be {@code null}
     * @param fetchViewerMetadata the {@code fetch_viewer_metadata} flag, may be {@code null}
     * @param fetchWamoSub        the {@code fetch_wamo_sub} flag, may be {@code null}
     * @param input               the structured {@code input} GraphQL variable
     */
    public FetchNewsletterMexRequest(Boolean fetchCreationTime, Boolean fetchFullImage, Boolean fetchViewerMetadata, Boolean fetchWamoSub, Input input) {
        this(fetchCreationTime, fetchFullImage, null, fetchViewerMetadata, fetchWamoSub, input);
    }

    /**
     * Constructs a request with the full set of GraphQL variables.
     *
     * @param fetchCreationTime    the {@code fetch_creation_time} flag, may be {@code null}
     * @param fetchFullImage       the {@code fetch_full_image} flag, may be {@code null}
     * @param fetchStatusMetadata  the {@code fetch_status_metadata} flag, may be {@code null}
     * @param fetchViewerMetadata  the {@code fetch_viewer_metadata} flag, may be {@code null}
     * @param fetchWamoSub         the {@code fetch_wamo_sub} flag, may be {@code null}
     * @param input                the structured {@code input} GraphQL variable
     */
    public FetchNewsletterMexRequest(Boolean fetchCreationTime, Boolean fetchFullImage, Boolean fetchStatusMetadata, Boolean fetchViewerMetadata, Boolean fetchWamoSub, Input input) {
        this.fetchCreationTime = fetchCreationTime;
        this.fetchFullImage = fetchFullImage;
        this.fetchStatusMetadata = fetchStatusMetadata;
        this.fetchViewerMetadata = fetchViewerMetadata;
        this.fetchWamoSub = fetchWamoSub;
        this.input = input;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterJob: WA Web reads the {@code params.id}
     *           field of the compiled artifact and forwards it to
     *           {@code MexPerfTracker.setQueryId}; Cobalt projects
     *           the same scalar through this accessor.
     * @return the constant {@link #QUERY_ID}; never
     *         {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name projected from
     * {@link #OPERATION_NAME}.
     *
     * @implNote WAWebMexFetchNewsletterJob: WA Web's
     *           {@code WAWebMexNativeClient.fetchQuery} reads
     *           {@code params.name} from the compiled GraphQL
     *           artifact and forwards it to
     *           {@code MexPerfTracker.setOperationName}; Cobalt
     *           projects the same scalar through this accessor.
     * @return the constant {@link #OPERATION_NAME};
     *         never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the
     * WhatsApp relay.
     *
     * @implNote WAWebMexFetchNewsletterJob.mexGetNewsletter: WA Web constructs the
     * {@code variables} object inline and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterJob", exports = "mexGetNewsletter",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterJob.mexGetNewsletter
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterJob.mexGetNewsletter
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexFetchNewsletterJob.mexGetNewsletter: d.input = {key: t, type: u, view_role: a}
            // Emits the input variable as a nested object when present
            if (input != null) {
                writer.writeName("input");
                writer.writeColon();
                writer.startObject();
                if (input.key() != null) {
                    writer.writeName("key");
                    writer.writeColon();
                    writer.writeString(input.key());
                }
                if (input.type() != null) {
                    writer.writeName("type");
                    writer.writeColon();
                    writer.writeString(input.type());
                }
                if (input.viewRole() != null) {
                    writer.writeName("view_role");
                    writer.writeColon();
                    writer.writeString(input.viewRole());
                }
                writer.endObject();
            }
            // WAWebMexFetchNewsletterJob.mexGetNewsletter
            // Emits the fetch_viewer_metadata boolean variable when present
            if (fetchViewerMetadata != null) {
                writer.writeName("fetch_viewer_metadata");
                writer.writeColon();
                writer.writeBool(fetchViewerMetadata);
            }
            // WAWebMexFetchNewsletterJob.mexGetNewsletter: c = u !== "INVITE"
            // Emits the fetch_full_image boolean variable when present
            if (fetchFullImage != null) {
                writer.writeName("fetch_full_image");
                writer.writeColon();
                writer.writeBool(fetchFullImage);
            }
            // WAWebMexFetchNewsletterJob.mexGetNewsletter
            // Emits the fetch_creation_time boolean variable when present
            if (fetchCreationTime != null) {
                writer.writeName("fetch_creation_time");
                writer.writeColon();
                writer.writeBool(fetchCreationTime);
            }
            // WAWebMexFetchNewsletterJob.mexGetNewsletter
            // Emits the fetch_wamo_sub boolean variable when present
            if (fetchWamoSub != null) {
                writer.writeName("fetch_wamo_sub");
                writer.writeColon();
                writer.writeBool(fetchWamoSub);
            }
            // WAWebMexFetchNewsletterJob.mexGetNewsletter
            // Emits the fetch_status_metadata boolean variable when present
            if (fetchStatusMetadata != null) {
                writer.writeName("fetch_status_metadata");
                writer.writeColon();
                writer.writeBool(fetchStatusMetadata);
            }
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchNewsletterJob.mexGetNewsletter
            // Flushes the JSON buffer into a StringWriter and wraps it in the shared MEX IQ envelope
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /**
     * The structured {@code input} GraphQL variable consumed by the
     * {@code mexGetNewsletter} query.
     *
     * @implNote WAWebMexFetchNewsletterJob.mexGetNewsletter: corresponds to
     * the inline JS object {@code {input: {key: t, type: u, view_role: a}}},
     * where {@code u} is computed by WAWebWid.isNewsletter as
     * {@code "JID"} for newsletter JIDs and {@code "INVITE"} for invite
     * keys.
     */
    public static final class Input {
        private final String key;
        private final String type;
        private final String viewRole;

        /**
         * Constructs a new {@link Input}.
         *
         * @param key      the newsletter JID or invite key
         * @param type     the lookup discriminator, either {@code "JID"} or {@code "INVITE"}
         * @param viewRole the optional viewer role enum name, may be {@code null}
         */
        public Input(String key, String type, String viewRole) {
            this.key = key;
            this.type = type;
            this.viewRole = viewRole;
        }

        /**
         * Returns the {@code key} field.
         *
         * @return the newsletter JID or invite key, or {@code null} if absent
         */
        public String key() {
            return key;
        }

        /**
         * Returns the {@code type} field.
         *
         * @return the lookup discriminator, or {@code null} if absent
         */
        public String type() {
            return type;
        }

        /**
         * Returns the {@code view_role} field.
         *
         * @return the viewer role enum name, or {@code null} if absent
         */
        public String viewRole() {
            return viewRole;
        }
    }
}
