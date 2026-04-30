package com.github.auties00.cobalt.node.mex.json.newsletter;

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
 * Accepts a pending newsletter admin invite for the authenticated user.
 *
 * <p>When a newsletter owner sends an admin invitation to another user, the
 * recipient must acknowledge the invite via this MEX mutation before becoming
 * a newsletter administrator. Accepting the invite upgrades the user's role on
 * the newsletter and removes the pending entry from the invitation list.
 */
@WhatsAppWebModule(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob")
public final class AcceptNewsletterAdminInviteMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code AcceptNewsletterAdminInvite} compiled mutation.
     */
    public static final String QUERY_ID = "9580828702035549";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this mutation.
     */
    public static final String OPERATION_NAME = "acceptNewsletterAdminInvite";

    /**
     * The identifier of the newsletter whose admin invite is being accepted.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob", exports = "acceptNewsletterAdminInvite",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String newsletterId;

    /**
     * Creates a request targeting the given newsletter.
     *
     * @param newsletterId the identifier of the newsletter whose admin invite
     *                     should be accepted
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
     * @return the constant {@link #QUERY_ID}, never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name projected from
     * {@link #OPERATION_NAME}.
     *
     * @return the constant {@link #OPERATION_NAME}, never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this mutation to the WhatsApp
     * relay.
     *
     * <p>The {@code newsletter_id} field is only emitted when it is
     * non-{@code null} so the server-side schema never receives an explicit
     * null variable.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables, ready to be mutated and built
     */
    @WhatsAppWebExport(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob", exports = "acceptNewsletterAdminInvite",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }
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
