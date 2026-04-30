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
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterDirectorySearchResultsJob")
public final class FetchNewsletterDirectorySearchResultsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterDirectorySearchResults} compiled query.
     */
    public static final String QUERY_ID = "9699865846759651";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterDirectorySearchResults
     * operation.
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
     *                            from {@code WAWebNewsletterGatingUtils.isNewsletterStatusReceiverEnabled()}     */
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterDirectorySearchResultsJob", exports = "mexFetchNewsletterDirectorySearchResults",
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

            writer.writeName("search_text");
            writer.writeColon();
            writer.writeString(searchText);

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

            if (limit != null) {
                writer.writeName("limit");
                writer.writeColon();
                writer.writeInt64(limit);
            }
            if (cursorToken != null) {
                writer.writeName("start_cursor");
                writer.writeColon();
                writer.writeString(cursorToken);
            }

            writer.endObject();

            writer.writeName("fetch_status_metadata");
            writer.writeColon();
            writer.writeBool(fetchStatusMetadata);

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
