package com.github.auties00.cobalt.node.mex.json.misc;

import com.alibaba.fastjson2.JSON;
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
import java.util.Objects;
import java.util.Optional;

/**
 * Fetches integrity signals for a peer in the FMX (first-message-experience)
 * flow, returning whether the account is new and whether the start-chat
 * action is considered suspicious.
 *
 * <p>Integrity signals power the safety nudges shown when a user starts a
 * conversation with an unfamiliar contact. The query batches over a list
 * of users in the wire format but WA Web only ever passes a single Jid;
 * Cobalt mirrors that single-user shape and exposes the two scalar flags
 * from the resulting {@code XWA2IntegritySignals} fragment.
 *
 * @implNote WAWebMexFetchIntegritySignals: adapts the
 * {@code fetchIntegritySignals} GraphQL query, which in WA Web is invoked
 * via {@code WAWebMexClient.fetchQuery}. The JS side wraps the call in
 * defensive logging and try/catch boilerplate that emits {@code WALogger}
 * breadcrumbs and downgrades any failure to a {@code null} return; Cobalt
 * drops that telemetry sidetrack and surfaces the parsed scalars
 * directly, which is why this mex is annotated as
 * {@link WhatsAppAdaptation#ADAPTED} rather than {@code DIRECT}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchIntegritySignals")
public final class FetchIntegritySignalsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchIntegritySignals} compiled query.
     *
     * @implNote WAWebMexFetchIntegritySignalsQuery.graphql: corresponds to
     * the compiled document id registered for the
     * {@code fetchIntegritySignals} query.
     */
    public static final String QUERY_ID = "26438847999065394";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled fetchIntegritySignals
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchIntegritySignals: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "fetchIntegritySignals"}.
     */
    public static final String OPERATION_NAME = "fetchIntegritySignals";
    private final Jid userJid;

    /**
     * Constructs a request that asks the relay for integrity signals on
     * the given user.
     *
     * @implNote WAWebMexFetchIntegritySignals.fetchIntegritySignals: WA
     * Web's {@code function*(e)} accepts a single user wrapper and
     * builds
     * {@code {input: {query_input: [{jid: e.toJid(), integrity_signals: {use_case: "CHAT_FMX"}}], telemetry: {context: "INTERACTIVE"}}}}
     * as the GraphQL variables payload. The string literals
     * {@code "CHAT_FMX"} and {@code "INTERACTIVE"} are hard-coded
     * constants in the JS source; Cobalt emits them verbatim.
     * @param userJid the user the integrity signals are being fetched
     *                for; must not be {@code null}
     */
    public FetchIntegritySignalsMexRequest(Jid userJid) {
        this.userJid = Objects.requireNonNull(userJid, "userJid cannot be null");
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchIntegritySignals: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchIntegritySignals: WA Web's
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
     * @implNote WAWebMexFetchIntegritySignals.fetchIntegritySignals: WA
     * Web constructs the {@code variables} object inline as
     * {@code {input: {query_input: [{jid: e.toJid(), integrity_signals: {use_case: "CHAT_FMX"}}], telemetry: {context: "INTERACTIVE"}}}}
     * and delegates to {@code WAWebMexClient.fetchQuery}. The
     * single-element {@code query_input} array, the
     * {@code use_case: "CHAT_FMX"} literal and the
     * {@code telemetry.context: "INTERACTIVE"} literal are all
     * hard-coded constants in the JS source. Cobalt writes the JSON
     * directly via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchIntegritySignals", exports = "fetchIntegritySignals",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchIntegritySignals.fetchIntegritySignals
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchIntegritySignals.fetchIntegritySignals
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchIntegritySignals.fetchIntegritySignals
            // input: {query_input: [...], telemetry: {context: "INTERACTIVE"}}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchIntegritySignals.fetchIntegritySignals
            // query_input is a single-element array carrying the target jid and the use_case constant
            writer.writeName("query_input");
            writer.writeColon();
            writer.startArray();
            writer.startObject();
            writer.writeName("jid");
            writer.writeColon();
            writer.writeString(userJid.toString());
            writer.writeName("integrity_signals");
            writer.writeColon();
            writer.startObject();
            // WAWebMexFetchIntegritySignals.fetchIntegritySignals
            // use_case is the hard-coded "CHAT_FMX" literal in the JS source
            writer.writeName("use_case");
            writer.writeColon();
            writer.writeString("CHAT_FMX");
            writer.endObject();
            writer.endObject();
            writer.endArray();

            // WAWebMexFetchIntegritySignals.fetchIntegritySignals
            // telemetry.context is the hard-coded "INTERACTIVE" literal in the JS source
            writer.writeName("telemetry");
            writer.writeColon();
            writer.startObject();
            writer.writeName("context");
            writer.writeColon();
            writer.writeString("INTERACTIVE");
            writer.endObject();

            writer.endObject();
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchIntegritySignals.fetchIntegritySignals
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
