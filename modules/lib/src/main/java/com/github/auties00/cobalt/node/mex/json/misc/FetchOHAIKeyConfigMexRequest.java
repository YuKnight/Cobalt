package com.github.auties00.cobalt.node.mex.json.misc;

import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 * Fetches the current OHAI (Oblivious HTTP Authentication for Initiation)
 * key configuration list issued by the WhatsApp relay.
 *
 * <p>OHAI is the Hybrid Public Key Encryption (HPKE) key bundle used to
 * encapsulate ACS (Account Centre Service) requests sent by the OHAI
 * client. The relay rotates the key set periodically and clients are
 * expected to refetch the configuration when their cached value expires.
 *
 * <p>The compiled GraphQL artifact declares no variables ({@code argumentDefinitions: []})
 * and selects the {@code xwa2_ohai_configurations.ohai_configs} list, with
 * each entry carrying {@code aead_id}, {@code expiration_date},
 * {@code kdf_id}, {@code kem_id}, {@code key_id},
 * {@code last_updated_time} and {@code public_key}.
 *
 * @implNote WAWebFetchOHAIKeyConfigJob.mexFetchOHAIKeyConfig: WA Web's
 * {@code function*()} dispatches the compiled
 * {@code WAWebFetchOHAIKeyConfigJobQuery} via
 * {@code WAWebMexClient.fetchQuery} with an empty variables object and
 * reduces the resulting {@code ohai_configs} array to the entry with the
 * earliest {@code expiration_date}. Cobalt models the request as a
 * variable-less MEX query and exposes the full configuration list through
 * the matching {@link FetchOHAIKeyConfigMexResponse} so callers can pick
 * the active entry themselves.
 */
@WhatsAppWebModule(moduleName = "WAWebFetchOHAIKeyConfigJob")
public final class FetchOHAIKeyConfigMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchOHAIKeyConfig} compiled query.
     *
     * @implNote WAWebFetchOHAIKeyConfigJobQuery.graphql: corresponds to the
     * {@code params.id} field of the compiled GraphQL artifact.
     */
    @WhatsAppWebExport(moduleName = "WAWebFetchOHAIKeyConfigJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "29366514836329275";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled
     * {@code WAWebFetchOHAIKeyConfigJobQuery} artifact.
     *
     * <p>The constant is exposed through {@link #name()} so call sites can
     * reach the same telemetry tag WA Web emits without duplicating the
     * literal at every dispatch site.
     *
     * @implNote WAWebFetchOHAIKeyConfigJobQuery.graphql: WA Web invokes the
     * operation through {@code WAWebMexClient.fetchQuery} which forwards
     * to {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that scalar
     * verbatim as {@code "WAWebFetchOHAIKeyConfigJobQuery"}.
     */
    public static final String OPERATION_NAME = "WAWebFetchOHAIKeyConfigJobQuery";

    /**
     * Constructs a {@link FetchOHAIKeyConfigMexRequest}. The compiled
     * GraphQL artifact takes no variables.
     *
     * @implNote WAWebFetchOHAIKeyConfigJob.mexFetchOHAIKeyConfig: WA Web
     * dispatches the query with an empty variables object
     * ({@code fetchQuery(s, {})}). Cobalt mirrors the contract by
     * exposing a no-arg constructor.
     */
    public FetchOHAIKeyConfigMexRequest() {
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebFetchOHAIKeyConfigJobQuery.graphql: WA Web reads the
     *           {@code params.id} field of the compiled artifact and
     *           forwards it to {@code MexPerfTracker.setQueryId}; Cobalt
     *           projects the same scalar through this accessor.
     * @return the constant {@link #QUERY_ID}; never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name projected from
     * {@link #OPERATION_NAME}.
     *
     * @implNote WAWebFetchOHAIKeyConfigJobQuery.graphql: WA Web's
     *           {@code WAWebMexNativeClient.fetchQuery} reads
     *           {@code params.name} from the compiled GraphQL artifact
     *           and forwards it to {@code MexPerfTracker.setOperationName};
     *           Cobalt projects the same scalar through this accessor.
     * @return the constant {@link #OPERATION_NAME}; never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the
     * WhatsApp relay.
     *
     * @implNote WAWebFetchOHAIKeyConfigJob.mexFetchOHAIKeyConfig: WA Web
     * passes an empty object literal as the variables argument to
     * {@code WAWebMexClient.fetchQuery}. Cobalt serialises an empty
     * {@code variables} envelope through {@code fastjson2.JSONWriter}
     * and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         empty serialised GraphQL variables payload
     */
    @WhatsAppWebExport(moduleName = "WAWebFetchOHAIKeyConfigJob", exports = "mexFetchOHAIKeyConfig",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebFetchOHAIKeyConfigJob.mexFetchOHAIKeyConfig
        // Opens a UTF-8 JSON writer that will serialise the empty GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebFetchOHAIKeyConfigJob.mexFetchOHAIKeyConfig
            // Begins the outer envelope and the empty "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebFetchOHAIKeyConfigJob.mexFetchOHAIKeyConfig
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
