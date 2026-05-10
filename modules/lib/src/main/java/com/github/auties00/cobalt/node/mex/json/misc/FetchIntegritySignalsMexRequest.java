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
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchIntegritySignals")
public final class FetchIntegritySignalsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the compiled {@code fetchIntegritySignals} query.
     */
    public static final String QUERY_ID = "26438847999065394";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this query, mirroring the {@code params.name} value of
     * the compiled {@code fetchIntegritySignals} operation.
     */
    public static final String OPERATION_NAME = "fetchIntegritySignals";
    /**
     * The user the integrity signals are being fetched for, bound to the
     * {@code query_input[0].jid} GraphQL variable.
     */
    private final Jid userJid;

    /**
     * Constructs a request that asks the relay for integrity signals on the
     * given user.
     * @param userJid the user the integrity signals are being fetched for,
     *                must not be {@code null}
     * @throws NullPointerException if {@code userJid} is {@code null}
     */
    public FetchIntegritySignalsMexRequest(Jid userJid) {
        this.userJid = Objects.requireNonNull(userJid, "userJid cannot be null");
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchIntegritySignals", exports = "fetchIntegritySignals",
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
            writer.writeName("use_case");
            writer.writeColon();
            writer.writeString("CHAT_FMX");
            writer.endObject();
            writer.endObject();
            writer.endArray();

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

            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
