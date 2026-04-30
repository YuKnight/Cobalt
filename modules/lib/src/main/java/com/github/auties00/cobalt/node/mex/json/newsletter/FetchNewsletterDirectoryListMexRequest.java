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
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterDirectoryListJob")
public final class FetchNewsletterDirectoryListMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterDirectoryList} compiled query.
     *     */
    public static final String QUERY_ID = "26125047313831973";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterDirectoryList
     * operation.
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
     *                            {@code WAWebNewsletterGatingUtils.isNewsletterStatusReceiverEnabled()}     */
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterDirectoryListJob", exports = "mexFetchNewsletterDirectoryList",
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

            if (view != null) {
                writer.writeName("view");
                writer.writeColon();
                writer.writeString(view.value());
            }

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
