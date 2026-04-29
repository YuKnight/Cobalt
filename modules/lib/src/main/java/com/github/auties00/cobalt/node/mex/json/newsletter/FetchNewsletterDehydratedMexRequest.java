package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Fetches a lightweight dehydrated representation of a newsletter.
 *
 * <p>The dehydrated form carries only the minimal identifiers and state needed to display the newsletter in a list without triggering a full metadata hydration. WA Web uses it on chat-list rendering paths and follow suggestions.
 *
 * @implNote WAWebMexFetchNewsletterDehydratedJob: adapts the {@code mexGetNewsletterDehydrated} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterDehydratedJob")
public final class FetchNewsletterDehydratedMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterDehydrated} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterDehydratedJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexGetNewsletterDehydrated} query.
     */
    public static final String QUERY_ID = "30328461880085868";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexGetNewsletterDehydrated
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterDehydratedJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexGetNewsletterDehydrated"}.
     */
    public static final String OPERATION_NAME = "mexGetNewsletterDehydrated";
    private final Jid key;
    private final String viewRole;
    private final boolean fetchWamoSub;

    /**
     * Constructs a request for the dehydrated representation of the given
     * newsletter key.
     *
     * @implNote WAWebMexFetchNewsletterDehydratedJob.mexGetNewsletterDehydrated: WA Web's
     * {@code function u(t, a, i)} accepts the key {@code t}, the
     * {@code view_role} {@code a} and an options object {@code i} carrying
     * {@code fetchWamoSub}. The {@code type} variable is derived from
     * {@code WAWebWid.isNewsletter(t) ? "JID" : "INVITE"}.
     * @param key          the newsletter Jid or invite identifier
     * @param viewRole     the GraphQL {@code view_role} variable
     * @param fetchWamoSub whether to request the optional
     *                     {@code wamo_sub} fragment selections
     */
    public FetchNewsletterDehydratedMexRequest(Jid key, String viewRole, boolean fetchWamoSub) {
        this.key = Objects.requireNonNull(key, "key cannot be null");
        this.viewRole = viewRole;
        this.fetchWamoSub = fetchWamoSub;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterDehydratedJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchNewsletterDehydratedJob: WA Web's
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
     * @implNote WAWebMexFetchNewsletterDehydratedJob.mexGetNewsletterDehydrated: WA Web constructs the
     * {@code variables} object inline as
     * {@code {input: {key: t, type: u, view_role: a}, fetch_wamo_sub: i.fetchWamoSub === true}}
     * and delegates to {@code WAWebMexClient.fetchQuery}. Cobalt writes
     * the JSON directly via {@code fastjson2.JSONWriter} and wraps it
     * through {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterDehydratedJob", exports = "mexGetNewsletterDehydrated",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterDehydratedJob.mexGetNewsletterDehydrated
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterDehydratedJob.mexGetNewsletterDehydrated
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchNewsletterDehydratedJob.mexGetNewsletterDehydrated
            // Builds the input object: {key: t, type: WAWebWid.isNewsletter(t) ? "JID" : "INVITE", view_role: a}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            writer.writeName("key");
            writer.writeColon();
            writer.writeString(key.toString());
            writer.writeName("type");
            writer.writeColon();
            // WAWebWid.isNewsletter(t) ? "JID" : "INVITE"
            writer.writeString(key.hasNewsletterServer() ? "JID" : "INVITE");
            writer.writeName("view_role");
            writer.writeColon();
            writer.writeString(viewRole);
            writer.endObject();

            // WAWebMexFetchNewsletterDehydratedJob.mexGetNewsletterDehydrated
            // fetch_wamo_sub: i.fetchWamoSub === true (always emitted, defaults to false)
            writer.writeName("fetch_wamo_sub");
            writer.writeColon();
            writer.writeBool(fetchWamoSub);

            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchNewsletterDehydratedJob.mexGetNewsletterDehydrated
            // Flushes the JSON buffer into a StringWriter and wraps it in the shared MEX IQ envelope
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
