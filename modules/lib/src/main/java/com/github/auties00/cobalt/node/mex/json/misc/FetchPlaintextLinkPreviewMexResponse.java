package com.github.auties00.cobalt.node.mex.json.misc;

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
import java.util.Optional;

/**
 * The parsed response for this MEX query.
 */
public final class FetchPlaintextLinkPreviewMexResponse implements MexOperation.Response.Json {
    private final String description;
    private final String directPath;
    private final String hash;
    private final String previewType;
    private final String thumbData;
    private final String title;
    private final String height;
    private final String width;

    private FetchPlaintextLinkPreviewMexResponse(String description, String directPath, String hash, String previewType, String thumbData, String title, String height, String width) {
        this.description = description;
        this.directPath = directPath;
        this.hash = hash;
        this.previewType = previewType;
        this.thumbData = thumbData;
        this.title = title;
        this.height = height;
        this.width = width;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexFetchPlaintextLinkPreviewJobQuery.graphql: reads
     * the preview metadata and the encrypted media handle from
     * {@code data.xwa2_newsletter_link_preview}.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchPlaintextLinkPreviewJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchPlaintextLinkPreviewMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchPlaintextLinkPreviewMexResponse::of);
    }

    /**
     * Returns the {@code description} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns the {@code direct_path} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> directPath() {
        return Optional.ofNullable(directPath);
    }

    /**
     * Returns the {@code hash} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> hash() {
        return Optional.ofNullable(hash);
    }

    /**
     * Returns the {@code preview_type} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> previewType() {
        return Optional.ofNullable(previewType);
    }

    /**
     * Returns the {@code thumb_data} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> thumbData() {
        return Optional.ofNullable(thumbData);
    }

    /**
     * Returns the {@code title} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> title() {
        return Optional.ofNullable(title);
    }

    /**
     * Returns the {@code height} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> height() {
        return Optional.ofNullable(height);
    }

    /**
     * Returns the {@code width} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> width() {
        return Optional.ofNullable(width);
    }

    private static Optional<FetchPlaintextLinkPreviewMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_newsletter_link_preview");
        if (root == null) {
            return Optional.empty();
        }

        var description = root.getString("description");
        var directPath = root.getString("direct_path");
        var hash = root.getString("hash");
        var previewType = root.getString("preview_type");
        var thumbData = root.getString("thumb_data");
        var title = root.getString("title");
        var height = root.getString("height");
        var width = root.getString("width");

        return Optional.of(new FetchPlaintextLinkPreviewMexResponse(description, directPath, hash, previewType, thumbData, title, height, width));
    }
}
