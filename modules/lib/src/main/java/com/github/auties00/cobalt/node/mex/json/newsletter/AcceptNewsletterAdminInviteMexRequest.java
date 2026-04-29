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
 * Accepts a pending newsletter admin invite for the authenticated user.
 *
 * <p>When a newsletter owner sends an admin invitation to another user, the
 * recipient must acknowledge the invite via this MEX mutation before becoming
 * a newsletter administrator. Accepting the invite upgrades the user's role
 * on the newsletter and removes the pending entry from the invitation list.
 *
 * <p>The request carries the target newsletter identifier and the response
 * returns the same identifier on success so callers can correlate the reply
 * with the originating request.
 *
 * @implNote WAWebMexAcceptNewsletterAdminInviteJob: adapts the
 * {@code acceptNewsletterAdminInvite} GraphQL mutation, which WA Web sends
 * through {@code WAWebMexClient.fetchQuery} with a single {@code newsletter_id}
 * variable and unwraps the {@code xwa2_newsletter_admin_invite_accept} root
 * of the response. Cobalt models the request and response as sibling variants
 * of a sealed interface rather than a free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob")
public final class AcceptNewsletterAdminInviteMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code AcceptNewsletterAdminInvite} compiled mutation.
     *
     * <p>This identifier is embedded as the {@code query_id} attribute of the
     * outbound {@code <query>} stanza so the server can route the payload to
     * the right GraphQL operation without transmitting the full query text.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJobMutation.graphql:
     * corresponds to the compiled document id registered for the
     * {@code acceptNewsletterAdminInvite} mutation.
     */
    public static final String QUERY_ID = "9580828702035549";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled acceptNewsletterAdminInvite
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "acceptNewsletterAdminInvite"}.
     */
    public static final String OPERATION_NAME = "acceptNewsletterAdminInvite";
    /**
     * The identifier of the newsletter whose admin invite is being accepted.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
     * mirrors the {@code newsletter_id} variable passed to
     * {@code fetchQuery(l, {newsletter_id: e})}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob", exports = "acceptNewsletterAdminInvite",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String newsletterId;

    /**
     * Creates a request targeting the given newsletter.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
     * mirrors the single positional parameter of the JS function which
     * receives the newsletter id and bundles it into the
     * {@code variables} object.
     * @param newsletterId the identifier of the newsletter whose admin
     *                     invite should be accepted
     */
    @WhatsAppWebExport(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob", exports = "acceptNewsletterAdminInvite",
            adaptation = WhatsAppAdaptation.DIRECT)
    public AcceptNewsletterAdminInviteMexRequest(String newsletterId) {
        this.newsletterId = newsletterId;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob: WA Web's
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
     * Builds the IQ stanza that dispatches this mutation to the WhatsApp
     * relay.
     *
     * <p>The returned {@link NodeBuilder} has not yet been built, so
     * callers may add routing attributes such as the IQ id before sending.
     * The {@code newsletter_id} field is only emitted when it is
     * non-{@code null} so that the server-side schema never receives an
     * explicit null variable.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
     * WA Web constructs the {@code variables} object inline and delegates
     * to {@code WAWebMexClient.fetchQuery} which performs the JSON
     * serialisation and stanza wrapping. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and delegates stanza wrapping to
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables, ready to be mutated and built
     */
    @WhatsAppWebExport(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob", exports = "acceptNewsletterAdminInvite",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
            // Begins the outer envelope and the nested "variables" object required by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
            // Emits the newsletter_id variable when present, skipping it otherwise so the server schema defaults apply
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
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
