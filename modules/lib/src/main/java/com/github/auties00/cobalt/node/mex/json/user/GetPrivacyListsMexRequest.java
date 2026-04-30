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
 * Fetches the contents of a privacy contact list for a user, identified by the privacy list category, type and content
 * hash.
 *
 * <p>Privacy lists are the server-side allow/deny rosters that gate features such as last-seen visibility, profile
 * photo visibility, status visibility and call eligibility. The query is keyed by the requesting user's JID plus the
 * {@code (dhash, category, type)} triple that identifies the specific list variant. The {@code dhash} is the digest of
 * the locally cached list used to support delta refreshes, {@code category} encodes the privacy domain and
 * {@code type} encodes the allow/deny polarity.
 */
@WhatsAppWebModule(moduleName = "WAWebMexGetPrivacyList")
public final class GetPrivacyListsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier of the compiled privacy-list query.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacyListsQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "26806428515612550";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacyListsQuery.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "fetchPrivacyList";

    /**
     * The requesting user's JID.
     */
    private final Jid jid;

    /**
     * The digest of the locally cached list, used to drive delta refreshes against the server-side state.
     */
    private final String dhash;

    /**
     * The privacy list domain (for example {@code "ALL"}).
     */
    private final String category;

    /**
     * The allow/deny polarity for the list.
     */
    private final String type;

    /**
     * Constructs a request for the contents of a privacy contact list.
     *
     * @apiNote {@code category} and {@code type} hold WA enum strings declared elsewhere in the JS source and are
     *          forwarded verbatim to the relay.
     * @param jid the requesting user's JID, must not be {@code null}
     * @param dhash the digest of the locally cached list used for delta refreshes
     * @param category the privacy list domain
     * @param type the allow/deny polarity for the list
     */
    public GetPrivacyListsMexRequest(Jid jid, String dhash, String category, String type) {
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        this.dhash = dhash;
        this.category = category;
        this.type = type;
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
     * Builds the IQ stanza that dispatches this operation to the WhatsApp relay.
     *
     * @implNote The {@code variables} payload is shaped as
     *           {@code {input: {query_input: [{jid, privacy_contact_list_type: {dhash, category, type}}]}}}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacyList", exports = "fetchPrivacyList",
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

            // Single-element array carrying the JID plus the privacy_contact_list_type triple.
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

            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
