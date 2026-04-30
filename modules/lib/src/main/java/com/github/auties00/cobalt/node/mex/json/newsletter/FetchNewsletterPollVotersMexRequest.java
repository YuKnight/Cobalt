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
 * Fetches the list of users who voted on a newsletter poll option.
 *
 * <p>Newsletter polls track voters per option; this query returns the paginated list of voter identifiers for a specific option so admins can inspect poll participation.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterPollVotersJob")
public final class FetchNewsletterPollVotersMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterPollVoters} compiled query.
     */
    public static final String QUERY_ID = "9407762219322536";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled fetchNewsletterPollVoters
     * operation.
     */
    public static final String OPERATION_NAME = "fetchNewsletterPollVoters";
    private final String newsletterId;
    private final long limit;
    private final long serverId;
    private final String voteHash;

    /**
     * Constructs a new request for the supplied poll voter list query.
     *
     * @param newsletterId the newsletter JID whose poll voters are being requested
     * @param limit        the maximum number of voter edges to return
     * @param serverId     the server-assigned identifier of the poll message
     * @param voteHash     the base64-encoded option hash, or {@code null} for all options
     */
    public FetchNewsletterPollVotersMexRequest(String newsletterId, long limit, long serverId, String voteHash) {
        this.newsletterId = newsletterId;
        this.limit = limit;
        this.serverId = serverId;
        this.voteHash = voteHash;
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterPollVotersJob", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            writer.writeName("limit");
            writer.writeColon();
            writer.writeInt64(limit);
            writer.writeName("server_id");
            writer.writeColon();
            writer.writeString(Long.toString(serverId, 10));
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }
            if (voteHash != null) {
                writer.writeName("vote_hash");
                writer.writeColon();
                writer.writeString(voteHash);
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
