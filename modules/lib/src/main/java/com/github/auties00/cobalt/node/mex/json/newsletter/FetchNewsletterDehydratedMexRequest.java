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
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterDehydratedJob")
public final class FetchNewsletterDehydratedMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterDehydrated} compiled query.
     */
    public static final String QUERY_ID = "30328461880085868";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexGetNewsletterDehydrated
     * operation.
     */
    public static final String OPERATION_NAME = "mexGetNewsletterDehydrated";
    private final Jid key;
    private final String viewRole;
    private final boolean fetchWamoSub;

    /**
     * Constructs a request for the dehydrated representation of the given
     * newsletter key.
     *
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterDehydratedJob", exports = "mexGetNewsletterDehydrated",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

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

            writer.writeName("fetch_wamo_sub");
            writer.writeColon();
            writer.writeBool(fetchWamoSub);

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
