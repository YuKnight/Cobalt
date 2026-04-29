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
 *
 * @implNote WAWebMexFetchNewsletterPollVotersJob: adapts the {@code default} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterPollVotersJob")
public final class FetchNewsletterPollVotersMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterPollVoters} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterPollVotersJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code default} query.
     */
    public static final String QUERY_ID = "9407762219322536";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled fetchNewsletterPollVoters
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterPollVotersJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "fetchNewsletterPollVoters"}.
     */
    public static final String OPERATION_NAME = "fetchNewsletterPollVoters";
    private final String newsletterId;
    private final long limit;
    private final long serverId;
    private final String voteHash;

    /**
     * Constructs a new request for the supplied poll voter list query.
     *
     * @implNote WAWebMexFetchNewsletterPollVotersJob.default: the four
     * positional arguments correspond to the {@code newsletterId},
     * {@code limit}, {@code serverId} and {@code voteHash} variables
     * embedded under {@code variables.input}.
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
     * @implNote WAWebMexFetchNewsletterPollVotersJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchNewsletterPollVotersJob: WA Web's
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
     * @implNote WAWebMexFetchNewsletterPollVotersJob.default: WA Web constructs the
     * {@code {input: {limit, server_id, newsletter_id, vote_hash}}}
     * variables envelope inline and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}. The
     * {@code server_id} is rendered as a base-10 string to mirror the JS
     * {@code a.toString(10)} call.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterPollVotersJob", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterPollVotersJob.default
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterPollVotersJob.default
            // Begins the outer envelope and the nested "variables" then "input" objects consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            // WAWebMexFetchNewsletterPollVotersJob.default
            // limit:t -- numeric page size
            writer.writeName("limit");
            writer.writeColon();
            writer.writeInt64(limit);
            // WAWebMexFetchNewsletterPollVotersJob.default
            // server_id:a.toString(10) -- server-assigned poll id rendered as decimal string
            writer.writeName("server_id");
            writer.writeColon();
            writer.writeString(Long.toString(serverId, 10));
            // WAWebMexFetchNewsletterPollVotersJob.default
            // newsletter_id:n -- newsletter JID, written when present
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }
            // WAWebMexFetchNewsletterPollVotersJob.default
            // vote_hash:i -- optional base64 option hash; null selects all options
            if (voteHash != null) {
                writer.writeName("vote_hash");
                writer.writeColon();
                writer.writeString(voteHash);
            }
            writer.endObject();
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchNewsletterPollVotersJob.default
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
