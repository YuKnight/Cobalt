package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The response variant of {@link FetchNewsletterAdminCapabilitiesMexResponse} that exposes the data
 * returned by the server after a successful query.
 *
 * @implNote WAWebMexFetchNewsletterAdminCapabilitiesJob: adapts the JSON root returned by the GraphQL
 * query into a Java value object.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterAdminCapabilitiesJob")
public final class FetchNewsletterAdminCapabilitiesMexResponse implements MexOperation.Response.Json {
    private final List<String> capabilities;

    private FetchNewsletterAdminCapabilitiesMexResponse(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexFetchNewsletterAdminCapabilitiesJob.mexFetchNewsletterAdminCapabilities: WA Web relies on the
     * GraphQL client to unwrap the response. Cobalt performs the
     * unwrapping manually from the IQ {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<FetchNewsletterAdminCapabilitiesMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewsletterAdminCapabilitiesMexResponse::of);
    }

    /**
     * Returns the raw newsletter capability values granted to the
     * authenticated admin.
     *
     * @implNote WAWebMexFetchNewsletterAdminCapabilitiesJob.mexFetchNewsletterAdminCapabilities: WA Web reads
     * {@code r.xwa2_newsletter_admin?.capabilities} and maps each entry
     * via {@code WAWebNewsletterModelUtils.getNewsletterCapabilityFromValue}
     * before wrapping the result in a {@code Set}. Cobalt returns the
     * raw string values; the enum mapping lives in the newsletter model
     * utilities.
     * @return an unmodifiable {@link List} of capability identifiers; never
     *         {@code null} but possibly empty
     */
    public List<String> capabilities() {
        return capabilities;
    }

    /**
     * Parses a {@link FetchNewsletterAdminCapabilitiesMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexFetchNewsletterAdminCapabilitiesJob.mexFetchNewsletterAdminCapabilities: mirrors the implicit
     * unwrapping that WA Web performs on the GraphQL response,
     * extracting the {@code xwa2_newsletter_admin} root.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterAdminCapabilitiesMexResponse> of(byte[] json) {
        // WAWebMexFetchNewsletterAdminCapabilitiesJob.mexFetchNewsletterAdminCapabilities
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterAdminCapabilitiesJob.mexFetchNewsletterAdminCapabilities
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterAdminCapabilitiesJob.mexFetchNewsletterAdminCapabilities
        // Extracts the operation-specific root keyed by xwa2_newsletter_admin (r.xwa2_newsletter_admin)
        var root = data.getJSONObject("xwa2_newsletter_admin");
        if (root == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterAdminCapabilitiesJob.mexFetchNewsletterAdminCapabilities
        // var a = r.xwa2_newsletter_admin?.capabilities; var i = a==null ? [] : a.map(...)
        var capabilitiesArray = root.getJSONArray("capabilities");
        var capabilities = new ArrayList<String>();
        if (capabilitiesArray != null) {
            for (var i = 0; i < capabilitiesArray.size(); i++) {
                var value = capabilitiesArray.getString(i);
                if (value != null) {
                    capabilities.add(value);
                }
            }
        }

        return Optional.of(new FetchNewsletterAdminCapabilitiesMexResponse(List.copyOf(capabilities)));
    }
}
