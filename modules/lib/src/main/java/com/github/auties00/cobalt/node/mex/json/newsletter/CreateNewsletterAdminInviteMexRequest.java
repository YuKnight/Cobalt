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
 * Creates a newsletter admin invitation for the target user.
 *
 * <p>The owner of a newsletter issues this mutation to invite another user to
 * become a newsletter administrator. The mutation records the invite on the
 * server so the target user can later accept it via
 * {@link AcceptNewsletterAdminInviteMexRequest}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateNewsletterAdminInviteJob")
public final class CreateNewsletterAdminInviteMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code CreateNewsletterAdminInvite} compiled mutation.
     */
    public static final String QUERY_ID = "9387141988078609";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this mutation.
     */
    public static final String OPERATION_NAME = "createNewsletterAdminInvite";

    /**
     * The identifier of the newsletter on which the admin invite is being
     * created.
     */
    private final String newsletterId;

    /**
     * The identifier of the user being invited as an admin.
     */
    private final String userId;

    /**
     * Creates a request that issues an admin invite for the given newsletter
     * and user.
     *
     * @param newsletterId the identifier of the newsletter
     * @param userId       the identifier of the invited user
     */
    public CreateNewsletterAdminInviteMexRequest(String newsletterId, String userId) {
        this.newsletterId = newsletterId;
        this.userId = userId;
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
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexCreateNewsletterAdminInviteJob", exports = "createNewsletterAdminInvite",
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

            if (userId != null) {
                writer.writeName("user_id");
                writer.writeColon();
                writer.writeString(userId);
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
