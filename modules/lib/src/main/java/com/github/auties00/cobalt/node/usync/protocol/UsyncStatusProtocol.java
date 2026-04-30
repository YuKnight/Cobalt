package com.github.auties00.cobalt.node.usync.protocol;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.usync.UsyncProtocol;
import com.github.auties00.cobalt.node.usync.UsyncProtocolResult;
import com.github.auties00.cobalt.node.usync.UsyncUser;
import com.github.auties00.cobalt.node.usync.result.StatusResult;

import java.util.Optional;

/**
 * USync {@code status} protocol descriptor. Asks the relay for each peer's
 * legacy status string. Distinguishes "no status set" from "status hidden by
 * peer privacy" via a {@code code="401"} marker on the response.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncStatus")
public final class UsyncStatusProtocol implements UsyncProtocol {
    /**
     * Wire literal for the protocol tag name.
     */
    public static final String NAME = "status";

    /**
     * Constructs a default status-protocol descriptor.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncStatus",
            exports = "USyncStatusProtocol", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncStatusProtocol() {
    }

    /**
     * Returns the wire literal for this protocol's tag name.
     *
     * @return the tag name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncStatus",
            exports = "USyncStatusProtocol.getName", adaptation = WhatsAppAdaptation.DIRECT)
    public String name() {
        return NAME;
    }

    /**
     * Builds an empty {@code <status/>} query element.
     *
     * @return the query-element node
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncStatus",
            exports = "USyncStatusProtocol.getQueryElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildQueryElement() {
        return new NodeBuilder().description(NAME).build();
    }

    /**
     * Builds a per-user {@code <tctoken>} child carrying the trusted-contact
     * token attached to the user entry. Returns empty when the user carries
     * no token.
     *
     * @param user the user the {@code <user>} entry refers to
     * @return the per-user element, or empty
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncStatus",
            exports = "USyncStatusProtocol.getUserElement", adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<Node> buildUserElement(UsyncUser user) {
        return user.trustedContactToken().map(token -> new NodeBuilder()
                .description("tctoken")
                .content(token)
                .build());
    }

    /**
     * Parses the {@code <status>} child of a {@code <user>} response into a
     * {@link StatusResult} or a per-protocol error. Distinguishes three
     * states: live status text, privacy-blocked (preserved as the empty
     * string when the relay returns {@code code="401"}), and no status set
     * (preserved as {@code null}).
     *
     * @param child the protocol-tagged response node
     * @return the parsed result
     * @throws IllegalStateException if the node tag is not {@link #NAME}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncStatus",
            exports = "statusParser", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncProtocolResult parseUserResult(Node child) {
        if (!child.hasDescription(NAME)) {
            throw new IllegalStateException("expected <" + NAME + ">, got <" + child.description() + ">");
        }
        var error = UsyncContactProtocol.parseError(child);
        if (error.isPresent()) {
            return error.get();
        }
        if (child.hasContent()) {
            var text = child.toContentString().orElse("");
            return new StatusResult(text.isEmpty() ? null : text);
        }
        if (child.getAttributeAsInt("code", -1) == 401) {
            return new StatusResult("");
        }
        return new StatusResult(null);
    }
}
