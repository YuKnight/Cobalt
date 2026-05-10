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
 * Transfers ownership of a newsletter to another user.
 *
 * <p>Only the current owner of a newsletter may initiate an ownership change.
 * The target user must already be registered on WhatsApp. After the mutation
 * completes successfully the target user receives full owner privileges and
 * the original owner is demoted to an admin role.
 */
@WhatsAppWebModule(moduleName = "WAWebMexChangeNewsletterOwnerJob")
public final class ChangeNewsletterOwnerMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code ChangeNewsletterOwner} compiled mutation.
     */
    public static final String QUERY_ID = "9546742745432473";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this mutation.
     */
    public static final String OPERATION_NAME = "mexChangeNewsletterOwner";

    /**
     * The identifier of the newsletter whose ownership is being transferred.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String newsletterId;

    /**
     * The identifier of the user who will become the new owner.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String userId;

    /**
     * Creates a request that transfers ownership of the given newsletter to
     * the given user.
     *
     * @param newsletterId the identifier of the newsletter whose owner is
     *                     being changed
     * @param userId       the identifier of the user who will become the new
     *                     owner
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
     * Builds the IQ stanza that dispatches this ownership-transfer mutation
     * to the WhatsApp relay.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexChangeNewsletterOwnerJob", exports = "mexChangeNewsletterOwner",
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
