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
import java.util.OptionalLong;

/**
 * Searches the newsletter directory for channels matching the given query.
 *
 * <p>This query powers the newsletter directory search experience, returning paginated channels whose names, handles or descriptions match the supplied text query.
 *
 * @implNote WAWebMexFetchNewsletterDirectorySearchResultsJob: adapts the {@code mexFetchNewsletterDirectorySearchResults} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterDirectorySearchResultsJob")
public final class FetchNewsletterDirectorySearchResultsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterDirectorySearchResults} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterDirectorySearchResultsJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterDirectorySearchResults} query.
     */
    public static final String QUERY_ID = "9699865846759651";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterDirectorySearchResults
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterDirectorySearchResultsJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchNewsletterDirectorySearchResults"}.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterDirectorySearchResults";
    private final String searchText;
    private final List<String> categories;
    private final Long limit;
    private final String cursorToken;
    private final boolean fetchStatusMetadata;

    /**
     * Constructs a new request for the newsletter directory search query.
     *
     * @param searchText          the free-text search query; corresponds to {@code e.searchText} in the JS source
     * @param categories          the categories filter as upper-case on-wire values
     *                            (e.g. {@code "BUSINESS"}); WA Web obtains these via
     *                            {@code WAWebNewsletterDirectoryCategoryUtils.getCategoryValueFromEnum}
     * @param limit               the page size, may be {@code null}
     * @param cursorToken         the start cursor for pagination, may be {@code null}
     * @param fetchStatusMetadata whether to include {@code status_metadata} in the response, set
     *                            from {@code WAWebNewsletterGatingUtils.isNewsletterStatusReceiverEnabled()}
     * @implNote WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults:
     * mirrors the inline destructure {@code var t=e.categories,n=e.cursorToken,r=e.limit,a=e.searchText}.
     */
    public FetchNewsletterDirectorySearchResultsMexRequest(String searchText,
                   List<String> categories,
                   Long limit,
                   String cursorToken,
                   boolean fetchStatusMetadata) {
        this.searchText = searchText;
        this.categories = categories;
        this.limit = limit;
        this.cursorToken = cursorToken;
        this.fetchStatusMetadata = fetchStatusMetadata;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterDirectorySearchResultsJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchNewsletterDirectorySearchResultsJob: WA Web's
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
     * @implNote WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults: WA Web constructs the
     * {@code variables} object inline and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterDirectorySearchResultsJob", exports = "mexFetchNewsletterDirectorySearchResults",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
            // var i={input:{search_text:a,categories:t.map(...),limit:r,start_cursor:n}, fetch_status_metadata:...}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
            // input.search_text = a
            writer.writeName("search_text");
            writer.writeColon();
            writer.writeString(searchText);

            // WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
            // input.categories = t.map(WAWebNewsletterDirectoryCategoryUtils.getCategoryValueFromEnum)
            writer.writeName("categories");
            writer.writeColon();
            writer.startArray();
            if (categories != null) {
                for (var i = 0; i < categories.size(); i++) {
                    if (i > 0) {
                        writer.writeComma();
                    }
                    writer.writeString(categories.get(i));
                }
            }
            writer.endArray();

            // WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
            // input.limit = r
            if (limit != null) {
                writer.writeName("limit");
                writer.writeColon();
                writer.writeInt64(limit);
            }
            // WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
            // input.start_cursor = n
            if (cursorToken != null) {
                writer.writeName("start_cursor");
                writer.writeColon();
                writer.writeString(cursorToken);
            }

            writer.endObject();

            // WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
            // fetch_status_metadata: o("WAWebNewsletterGatingUtils").isNewsletterStatusReceiverEnabled()
            writer.writeName("fetch_status_metadata");
            writer.writeColon();
            writer.writeBool(fetchStatusMetadata);

            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchNewsletterDirectorySearchResultsJob.mexFetchNewsletterDirectorySearchResults
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
