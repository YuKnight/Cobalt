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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fetches a paginated list of followers for a given newsletter.
 *
 * <p>Each follower entry includes the member identifier, role, follow time and optional admin profile metadata. Admins use this query to display the follower roster and manage roles.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterFollowersJob")
public final class FetchNewsletterFollowersMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterFollowers} compiled query.
     */
    public static final String QUERY_ID = "25895136756785869";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterFollowers
     * operation.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterFollowers";
    private final String newsletterId;
    private final Integer count;

    /**
     * Constructs a request for the given newsletter and follower page size.
     *
     * @param newsletterId the newsletter JID, written into the
     *                     {@code input.newsletter_id} variable when
     *                     non-{@code null}
     * @param count        the requested follower page size, written into
     *                     {@code input.count} when non-{@code null}; the
     *                     caller is responsible for clamping this to
     *                     {@code WAWebNewsletterGatingUtils.getMaxSubscriberNumber()}
     */
    public FetchNewsletterFollowersMexRequest(String newsletterId, Integer count) {
        this.newsletterId = newsletterId;
        this.count = count;
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
     * Builds the IQ stanza that dispatches this operation to the
     * WhatsApp relay.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterFollowersJob", exports = "mexFetchNewsletterFollowers",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // object mirrors the JS object literal shape. Each scalar is omitted when null, leaving
            // the GraphQL schema defaults to apply server-side.
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }
            if (count != null) {
                writer.writeName("count");
                writer.writeColon();
                writer.writeInt32(count);
            }
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
