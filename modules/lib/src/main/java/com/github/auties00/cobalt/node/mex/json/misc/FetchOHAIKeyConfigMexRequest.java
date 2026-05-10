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
 */
@WhatsAppWebModule(moduleName = "WAWebFetchOHAIKeyConfigJob")
public final class FetchOHAIKeyConfigMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the compiled {@code WAWebFetchOHAIKeyConfigJobQuery} artifact.
     */
    @WhatsAppWebExport(moduleName = "WAWebFetchOHAIKeyConfigJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "29366514836329275";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this query, mirroring the {@code params.name} value of
     * the compiled {@code WAWebFetchOHAIKeyConfigJobQuery} artifact.
     */
    public static final String OPERATION_NAME = "WAWebFetchOHAIKeyConfigJobQuery";

    /**
     * Constructs a {@link FetchOHAIKeyConfigMexRequest}.
     */
    public FetchOHAIKeyConfigMexRequest() {
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
     * @return a {@link NodeBuilder} carrying the IQ envelope and an empty
     *         serialised GraphQL variables payload
     */
    @WhatsAppWebExport(moduleName = "WAWebFetchOHAIKeyConfigJob", exports = "mexFetchOHAIKeyConfig",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
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
