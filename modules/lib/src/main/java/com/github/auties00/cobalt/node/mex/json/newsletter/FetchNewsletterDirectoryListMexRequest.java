package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.newsletter.NewsletterDirectoryListView;
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
 * Fetches the full newsletter directory list filtered by country or category.
 *
 * <p>This query powers the explore tab of the newsletter directory, returning paginated channels that match the supplied filter arguments such as country or category.
 *
 * @implNote WAWebMexFetchNewsletterDirectoryListJob: adapts the {@code mexFetchNewsletterDirectoryList} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterDirectoryListJob")
public final class FetchNewsletterDirectoryListMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterDirectoryList} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterDirectoryListJobQuery.graphql: corresponds to the compiled
     * document id {@code params.id} registered for the
     * {@code WAWebMexFetchNewsletterDirectoryListJobQuery} compiled query.
     */
    public static final String QUERY_ID = "26125047313831973";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterDirectoryList
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterDirectoryListJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchNewsletterDirectoryList"}.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterDirectoryList";
    private final NewsletterDirectoryListView view;
    private final List<String> countryCodes;
    private final List<String> categories;
    private final Long limit;
    private final String cursorToken;
    private final boolean fetchStatusMetadata;

    /**
     * Constructs a new request for the newsletter directory list query.
     *
     * @param view                the directory list view (filter pill) to query;
     *                            translated to the uppercase enum string by
     *                            {@code u(i)} in the JS source
     * @param countryCodes        the country codes to filter by, may be {@code null}
     * @param categories          the categories to filter by as upper-case
     *                            on-wire values (e.g. {@code "BUSINESS"}); WA Web
     *                            obtains these via
     *                            {@code WAWebNewsletterDirectoryCategoryUtils.getCategoryValueFromEnum}
     * @param limit               the page size, may be {@code null}
     * @param cursorToken         the start cursor for pagination, may be {@code null}
     * @param fetchStatusMetadata whether to include
     *                            {@code status_metadata} in the response, set
     *                            from
     *                            {@code WAWebNewsletterGatingUtils.isNewsletterStatusReceiverEnabled()}
     * @implNote WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList:
     * mirrors the inline destructure {@code var t=e.categories,n=e.countryCodes,r=e.cursorToken,a=e.limit,i=e.view}.
     */
    public FetchNewsletterDirectoryListMexRequest(NewsletterDirectoryListView view,
                   List<String> countryCodes,
                   List<String> categories,
                   Long limit,
                   String cursorToken,
                   boolean fetchStatusMetadata) {
        this.view = view;
        this.countryCodes = countryCodes;
        this.categories = categories;
        this.limit = limit;
        this.cursorToken = cursorToken;
        this.fetchStatusMetadata = fetchStatusMetadata;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterDirectoryListJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchNewsletterDirectoryListJob: WA Web's
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
     * @implNote WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList: WA Web constructs the
     * {@code variables} object inline and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterDirectoryListJob", exports = "mexFetchNewsletterDirectoryList",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
            // var l={input:{view:u(i),filters:{country_codes:n,categories:t.map(...)},limit:a,start_cursor:r}, ...}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
            // input.view = u(i) -> uppercase enum string
            if (view != null) {
                writer.writeName("view");
                writer.writeColon();
                writer.writeString(view.value());
            }

            // WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
            // input.filters = {country_codes:n, categories:t.map(getCategoryValueFromEnum)}
            writer.writeName("filters");
            writer.writeColon();
            writer.startObject();
            writer.writeName("country_codes");
            writer.writeColon();
            writeStringArray(writer, countryCodes);
            writer.writeName("categories");
            writer.writeColon();
            writeStringArray(writer, categories);
            writer.endObject();

            // WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
            // input.limit = a
            if (limit != null) {
                writer.writeName("limit");
                writer.writeColon();
                writer.writeInt64(limit);
            }
            // WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
            // input.start_cursor = r
            if (cursorToken != null) {
                writer.writeName("start_cursor");
                writer.writeColon();
                writer.writeString(cursorToken);
            }

            writer.endObject();

            // WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
            // fetch_status_metadata: o("WAWebNewsletterGatingUtils").isNewsletterStatusReceiverEnabled()
            writer.writeName("fetch_status_metadata");
            writer.writeColon();
            writer.writeBool(fetchStatusMetadata);

            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchNewsletterDirectoryListJob.mexFetchNewsletterDirectoryList
            // Flushes the JSON buffer into a StringWriter and wraps it in the shared MEX IQ envelope
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /**
     * Writes a string array, emitting an empty array when the input is
     * {@code null} so that the on-wire shape always contains the
     * {@code country_codes} and {@code categories} keys (matching the
     * JS object literal which never omits them).
     *
     * @param writer the JSON writer to emit into
     * @param values the string values to serialise, may be {@code null}
     */
    private static void writeStringArray(JSONWriter writer, List<String> values) {
        writer.startArray();
        if (values != null) {
            for (var i = 0; i < values.size(); i++) {
                if (i > 0) {
                    writer.writeComma();
                }
                writer.writeString(values.get(i));
            }
        }
        writer.endArray();
    }
}
