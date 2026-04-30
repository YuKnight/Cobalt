package com.github.auties00.cobalt.node.usync.protocol;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.usync.UsyncProtocol;
import com.github.auties00.cobalt.node.usync.UsyncProtocolResult;
import com.github.auties00.cobalt.node.usync.UsyncUser;
import com.github.auties00.cobalt.node.usync.result.FeatureResult;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * USync {@code feature} protocol descriptor. Carries one empty child per
 * requested feature key inside the {@code <feature>} query element so the
 * relay reports which features the peer supports.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncFeature")
public final class UsyncFeatureProtocol implements UsyncProtocol {
    /**
     * Wire literal for the protocol tag name.
     */
    public static final String NAME = "feature";

    /**
     * Holds the features the relay is asked to report on. Never empty.
     */
    private final List<FeatureQuery> queries;

    /**
     * Creates a new feature-protocol descriptor for the given queries.
     *
     * @param queries the features to request; must not be empty
     * @throws IllegalArgumentException if {@code queries} is empty
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncFeature",
            exports = "USyncFeaturesProtocol", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncFeatureProtocol(List<FeatureQuery> queries) {
        Objects.requireNonNull(queries, "queries cannot be null");
        if (queries.isEmpty()) {
            throw new IllegalArgumentException("must specify at least one query");
        }
        this.queries = List.copyOf(queries);
    }

    /**
     * Returns the wire literal for this protocol's tag name.
     *
     * @return the tag name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncFeature",
            exports = "USyncFeaturesProtocol.getName", adaptation = WhatsAppAdaptation.DIRECT)
    public String name() {
        return NAME;
    }

    /**
     * Builds the {@code <feature>} query element with one empty child per
     * requested feature key.
     *
     * @return the query-element node
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncFeature",
            exports = "USyncFeaturesProtocol.getQueryElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildQueryElement() {
        var children = queries.stream()
                .map(q -> new NodeBuilder().description(q.wireValue()).build())
                .toList();
        return new NodeBuilder().description(NAME).content(children).build();
    }

    /**
     * Returns no per-user element because the feature protocol carries no
     * per-user payload on the request side.
     *
     * @param user the user the {@code <user>} entry refers to
     * @return always {@link Optional#empty()}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncFeature",
            exports = "USyncFeaturesProtocol.getUserElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Node> buildUserElement(UsyncUser user) {
        return Optional.empty();
    }

    /**
     * Parses the {@code <feature>} child of a {@code <user>} response into a
     * {@link FeatureResult} or a per-protocol error.
     *
     * @param child the protocol-tagged response node
     * @return the parsed result
     * @throws IllegalStateException if the node tag is not {@link #NAME}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncFeature",
            exports = "featureParser", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncProtocolResult parseUserResult(Node child) {
        if (!child.hasDescription(NAME)) {
            throw new IllegalStateException("expected <" + NAME + ">, got <" + child.description() + ">");
        }
        var error = UsyncContactProtocol.parseError(child);
        if (error.isPresent()) {
            return error.get();
        }
        var values = new HashMap<FeatureQuery, String>();
        for (var key : FeatureQuery.values()) {
            child.getChild(key.wireValue()).ifPresent(c ->
                    c.getAttributeAsString("value").ifPresent(v -> values.put(key, v)));
        }
        return new FeatureResult(values);
    }

    /**
     * Enumerates every feature key the relay currently understands inside the
     * {@code <feature>} query.
     *
     * @implNote The JS counterpart is module-level constant {@code s} in
     *     {@code WAWebUsyncFeature}.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsyncFeature")
    public enum FeatureQuery {
        /**
         * Document-message support.
         */
        DOCUMENT("document"),
        /**
         * Generic encryption support.
         */
        ENCRYPT("encrypt"),
        /**
         * Encrypted block-list support.
         */
        ENCRYPT_BLIST("encrypt_blist"),
        /**
         * Encrypted contact-card support.
         */
        ENCRYPT_CONTACT("encrypt_contact"),
        /**
         * Encrypted-group v2 support.
         */
        ENCRYPT_GROUP_GEN2("encrypt_group_gen2"),
        /**
         * Encrypted image support.
         */
        ENCRYPT_IMAGE("encrypt_image"),
        /**
         * Encrypted location-share support.
         */
        ENCRYPT_LOCATION("encrypt_location"),
        /**
         * Encrypted URL support.
         */
        ENCRYPT_URL("encrypt_url"),
        /**
         * Encryption v2 support.
         */
        ENCRYPT_V2("encrypt_v2"),
        /**
         * VoIP signalling support.
         */
        VOIP("voip"),
        /**
         * Multi-agent (CRM) support.
         */
        MULTI_AGENT("multi_agent");

        /**
         * Holds the literal tag name on the wire.
         */
        private final String wireValue;

        /**
         * Creates a new feature key bound to the given wire string.
         *
         * @param wireValue the literal tag name on the wire
         */
        FeatureQuery(String wireValue) {
            this.wireValue = wireValue;
        }

        /**
         * Returns the literal tag name on the wire.
         *
         * @return the wire value
         */
        public String wireValue() {
            return wireValue;
        }
    }
}
