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
 * Transfers ownership of a newsletter to another user.
 *
 * <p>Only the current owner of a newsletter may initiate an ownership change.
 * The target user must already be registered on WhatsApp. After the mutation
 * completes successfully, the target user receives full owner privileges and
 * the original owner is demoted to an admin role.
 *
 * <p>The request carries the newsletter identifier together with the
 * recipient user identifier, and the response returns the newsletter id on
 * success.
 *
 * @implNote WAWebMexChangeNewsletterOwnerJob: adapts the
 * {@code mexChangeNewsletterOwner} GraphQL mutation, which in WA Web is
 * invoked via {@code WAWebMexClient.fetchQuery} with a
 * {@code {newsletter_id, user_id}} variables object and unwraps the
 * {@code xwa2_newsletter_change_owner} root of the response.
 */
@WhatsAppWebModule(moduleName = "WAWebMexChangeNewsletterOwnerJob")
public final class ChangeNewsletterOwnerMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code ChangeNewsletterOwner} compiled mutation.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJobMutation.graphql: corresponds
     * to the compiled document id registered for the
     * {@code mexChangeNewsletterOwner} mutation.
     */
    public static final String QUERY_ID = "9546742745432473";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexChangeNewsletterOwner
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexChangeNewsletterOwner"}.
     */
    public static final String OPERATION_NAME = "mexChangeNewsletterOwner";
    /**
     * The identifier of the newsletter whose ownership is being
     * transferred.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
     * mirrors the {@code newsletter_id} variable in the WA Web call.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String newsletterId;

    /**
     * The identifier of the user who will become the new owner.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
     * mirrors the {@code user_id} variable which WA Web derives from the
     * target wid via {@code WAWebLidMigrationUtils}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String userId;

    /**
     * Creates a request that transfers ownership of the given newsletter
     * to the given user.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
     * maps the {@code (newsletterId, userId)} positional parameters of
     * the JS function.
     * @param newsletterId the identifier of the newsletter whose owner is
     *                     being changed
     * @param userId the identifier of the user who will become the new
     *               owner
     */
    @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
            adaptation = WhatsAppAdaptation.DIRECT)
    public ChangeNewsletterOwnerMexRequest(String newsletterId, String userId) {
        this.newsletterId = newsletterId;
        this.userId = userId;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexChangeNewsletterOwnerJob: WA Web's
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
     * Builds the IQ stanza that dispatches this ownership-transfer
     * mutation to the WhatsApp relay.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
     * WA Web constructs the {@code variables} object inline and delegates
     * to {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON
     * directly via {@code fastjson2.JSONWriter} and delegates stanza
     * wrapping to {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
            // Begins the outer envelope and the nested "variables" object
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
            // Emits the newsletter_id variable when present
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }

            // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
            // Emits the user_id variable when present, skipping it otherwise
            if (userId != null) {
                writer.writeName("user_id");
                writer.writeColon();
                writer.writeString(userId);
            }
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
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
