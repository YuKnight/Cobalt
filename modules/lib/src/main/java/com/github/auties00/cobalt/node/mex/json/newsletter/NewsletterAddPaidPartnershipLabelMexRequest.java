package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
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
import java.util.Optional;

/**
 * Attaches a paid-partnership disclosure label to a newsletter message.
 *
 * <p>Monetised creators must disclose paid partnerships on sponsored messages. This mutation attaches the required legal label to a specific newsletter message so the server can render the disclosure badge.
 *
 * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob: adapts the {@code mexNewsletterAddPaidPartnershipLabelJob} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexNewsletterAddPaidPartnershipLabelJob")
public final class NewsletterAddPaidPartnershipLabelMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code NewsletterAddPaidPartnershipLabel} compiled mutation.
     *
     * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJobMutation.graphql: corresponds to the compiled
     * document id registered for the {@code mexNewsletterAddPaidPartnershipLabelJob} mutation.
     */
    public static final String QUERY_ID = "25690501173969818";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexNewsletterAddPaidPartnershipLabelJob
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexNewsletterAddPaidPartnershipLabelJob"}.
     */
    public static final String OPERATION_NAME = "mexNewsletterAddPaidPartnershipLabelJob";
    private final String newsletterId;
    private final String serverId;

    public NewsletterAddPaidPartnershipLabelMexRequest(String newsletterId, String serverId) {
        this.newsletterId = newsletterId;
        this.serverId = serverId;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob: WA Web's
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
     * @implNote WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob: WA Web constructs the
     * {@code variables} object inline and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexNewsletterAddPaidPartnershipLabelJob", exports = "mexNewsletterAddPaidPartnershipLabelJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
            // Emits the newsletter_id variable when present
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }

            // WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
            // Emits the server_id variable when present
            if (serverId != null) {
                writer.writeName("server_id");
                writer.writeColon();
                writer.writeString(serverId);
            }
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexNewsletterAddPaidPartnershipLabelJob.mexNewsletterAddPaidPartnershipLabelJob
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
