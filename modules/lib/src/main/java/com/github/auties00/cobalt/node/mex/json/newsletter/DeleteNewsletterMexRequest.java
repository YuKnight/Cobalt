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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Permanently deletes a newsletter owned by the authenticated user.
 *
 * <p>Deleting a newsletter is an irreversible action that removes the
 * channel, its messages and its subscriber list from the WhatsApp servers.
 * Only the current owner may issue the deletion; all followers stop
 * receiving updates as soon as the mutation succeeds.
 *
 * <p>The request carries the newsletter identifier and the response echoes
 * the identifier together with the transitional {@code state} object that
 * indicates the newsletter has moved into a terminal deleted state.
 *
 * @implNote WAWebMexDeleteNewsletterJob: adapts the
 * {@code mexDeleteNewsletter} GraphQL mutation, which in WA Web is invoked
 * via {@code WAWebMexClient.fetchQuery} with a single {@code newsletter_id}
 * variable and unwraps the {@code xwa2_newsletter_delete_v2} root of the
 * response.
 */
@WhatsAppWebModule(moduleName = "WAWebMexDeleteNewsletterJob")
public final class DeleteNewsletterMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code DeleteNewsletter} compiled mutation.
     *
     * @implNote WAWebMexDeleteNewsletterJobMutation.graphql: corresponds to
     * the compiled document id registered for the
     * {@code mexDeleteNewsletter} mutation.
     */
    public static final String QUERY_ID = "30062808666639665";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexDeleteNewsletter
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexDeleteNewsletterJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexDeleteNewsletter"}.
     */
    public static final String OPERATION_NAME = "mexDeleteNewsletter";
    /**
     * The identifier of the newsletter being deleted.
     *
     * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: mirrors
     * the {@code newsletter_id} variable passed to {@code fetchQuery}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexDeleteNewsletterJob", exports = "mexDeleteNewsletter",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String newsletterId;

    /**
     * Creates a request targeting the given newsletter for deletion.
     *
     * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: mirrors
     * the single positional parameter of the JS function.
     * @param newsletterId the identifier of the newsletter to delete
     */
    @WhatsAppWebExport(moduleName = "WAWebMexDeleteNewsletterJob", exports = "mexDeleteNewsletter",
            adaptation = WhatsAppAdaptation.DIRECT)
    public DeleteNewsletterMexRequest(String newsletterId) {
        this.newsletterId = newsletterId;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexDeleteNewsletterJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexDeleteNewsletterJob: WA Web's
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
     * Builds the IQ stanza that dispatches this deletion mutation to the
     * WhatsApp relay.
     *
     * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: WA Web
     * constructs the {@code variables} object inline and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexDeleteNewsletterJob", exports = "mexDeleteNewsletter",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
            // Begins the outer envelope and the nested "variables" object
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
            // Emits the newsletter_id variable when present, skipping it otherwise
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
            // Flushes the JSON into a StringWriter and wraps it in the shared MEX IQ envelope
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
