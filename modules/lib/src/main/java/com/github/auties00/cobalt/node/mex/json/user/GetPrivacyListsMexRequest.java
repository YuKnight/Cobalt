package com.github.auties00.cobalt.node.mex.json.user;

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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;

/**
 * Fetches the contents of a privacy contact list for a user, identified by
 * the privacy list category, type and content hash.
 *
 * <p>Privacy lists are the server-side allow/deny rosters that gate
 * features such as last-seen visibility, profile photo visibility, status
 * visibility and call eligibility. The query is keyed by the requesting
 * user's Jid plus the
 * {@code (dhash, category, type)} triple that identifies the specific list
 * variant: {@code dhash} is the digest of the locally cached list used to
 * support delta refreshes, {@code category} encodes the privacy domain and
 * {@code type} encodes the allow/deny polarity.
 *
 * @implNote WAWebMexGetPrivacyList: adapts the {@code fetchPrivacyList}
 * GraphQL query, which in WA Web is invoked via
 * {@code WAWebMexClient.fetchQuery} and returns the raw GraphQL response
 * verbatim to its caller. Cobalt mirrors that opaque-pass-through shape:
 * the {@link GetPrivacyListsMexResponse} exposes the {@code <result>} child as a
 * {@link Node} so callers can drive their own projection. Annotated as
 * {@link WhatsAppAdaptation#ADAPTED} because the JS function returns the
 * raw GraphQL data while Cobalt returns the IQ child node instead.
 */
@WhatsAppWebModule(moduleName = "WAWebMexGetPrivacyList")
public final class GetPrivacyListsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code GetPrivacyLists} compiled query.
     *
     * @implNote WAWebMexGetPrivacyListsQuery.graphql: corresponds to the
     * compiled document id registered for the {@code fetchPrivacyList}
     * query.
     */
    public static final String QUERY_ID = "26806428515612550";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled fetchPrivacyList
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexGetPrivacyList: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "fetchPrivacyList"}.
     */
    public static final String OPERATION_NAME = "fetchPrivacyList";
    private final Jid jid;
    private final String dhash;
    private final String category;
    private final String type;

    /**
     * Constructs a request for the contents of a privacy contact list.
     *
     * @implNote WAWebMexGetPrivacyList.fetchPrivacyList: WA Web's
     * {@code function*(e)} accepts a wrapper object with
     * {@code {jid, dhash, category, type}} and constructs
     * {@code {input: {query_input: [{jid: e.jid, privacy_contact_list_type: {dhash: e.dhash, category: e.category, type: e.type}}]}}}
     * as the GraphQL variables payload. Cobalt accepts the four
     * scalars directly and leaves {@code category} / {@code type}
     * typed as plain {@link String} since they hold WA enum strings
     * defined elsewhere in the JS source.
     * @param jid      the requesting user's Jid; must not be {@code null}
     * @param dhash    the digest of the locally cached list used for delta refreshes
     * @param category the privacy list domain (for example {@code "ALL"})
     * @param type     the allow/deny polarity for the list
     */
    public GetPrivacyListsMexRequest(Jid jid, String dhash, String category, String type) {
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        this.dhash = dhash;
        this.category = category;
        this.type = type;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexGetPrivacyList: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexGetPrivacyList: WA Web's
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
     * @implNote WAWebMexGetPrivacyList.fetchPrivacyList: WA Web
     * constructs the {@code variables} object inline as
     * {@code {input: {query_input: [{jid: e.jid, privacy_contact_list_type: {dhash: e.dhash, category: e.category, type: e.type}}]}}}
     * and delegates to {@code WAWebMexClient.fetchQuery}. Cobalt
     * writes the JSON directly via {@code fastjson2.JSONWriter} and
     * wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacyList", exports = "fetchPrivacyList",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexGetPrivacyList.fetchPrivacyList
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexGetPrivacyList.fetchPrivacyList
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexGetPrivacyList.fetchPrivacyList
            // input: {query_input: [...]}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();

            // WAWebMexGetPrivacyList.fetchPrivacyList
            // query_input: single-element array carrying the jid and privacy_contact_list_type triple
            writer.writeName("query_input");
            writer.writeColon();
            writer.startArray();
            writer.startObject();
            writer.writeName("jid");
            writer.writeColon();
            writer.writeString(jid.toString());

            writer.writeName("privacy_contact_list_type");
            writer.writeColon();
            writer.startObject();
            if (dhash != null) {
                writer.writeName("dhash");
                writer.writeColon();
                writer.writeString(dhash);
            }
            if (category != null) {
                writer.writeName("category");
                writer.writeColon();
                writer.writeString(category);
            }
            if (type != null) {
                writer.writeName("type");
                writer.writeColon();
                writer.writeString(type);
            }
            writer.endObject();
            writer.endObject();
            writer.endArray();

            writer.endObject();
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexGetPrivacyList.fetchPrivacyList
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
