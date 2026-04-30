package com.github.auties00.cobalt.node.usync.protocol;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.usync.UsyncProtocol;
import com.github.auties00.cobalt.node.usync.UsyncProtocolResult;
import com.github.auties00.cobalt.node.usync.UsyncUser;
import com.github.auties00.cobalt.node.usync.result.TextStatusResult;

import java.time.Duration;
import java.util.Optional;

/**
 * USync {@code text_status} protocol descriptor. Asks the relay for each
 * peer's modern text-status payload (text, emoji, ephemeral lifetime, last
 * update timestamp).
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncTextStatus")
public final class UsyncTextStatusProtocol implements UsyncProtocol {
    /**
     * Wire literal for the protocol tag name.
     */
    public static final String NAME = "text_status";

    /**
     * Constructs a default text-status-protocol descriptor.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncTextStatus",
            exports = "USyncTextStatusProtocol", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncTextStatusProtocol() {
    }

    /**
     * Returns the wire literal for this protocol's tag name.
     *
     * @return the tag name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncTextStatus",
            exports = "USyncTextStatusProtocol.getName", adaptation = WhatsAppAdaptation.DIRECT)
    public String name() {
        return NAME;
    }

    /**
     * Builds an empty {@code <text_status/>} query element.
     *
     * @return the query-element node
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncTextStatus",
            exports = "USyncTextStatusProtocol.getQueryElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildQueryElement() {
        return new NodeBuilder().description(NAME).build();
    }

    /**
     * Returns no per-user element because the text-status protocol carries
     * no per-user payload on the request side.
     *
     * @param user the user the {@code <user>} entry refers to
     * @return always {@link Optional#empty()}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncTextStatus",
            exports = "USyncTextStatusProtocol.getUserElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Node> buildUserElement(UsyncUser user) {
        return Optional.empty();
    }

    /**
     * Parses the {@code <text_status>} child of a {@code <user>} response
     * into a {@link TextStatusResult} or a per-protocol error.
     *
     * @param child the protocol-tagged response node
     * @return the parsed result
     * @throws IllegalStateException if the node tag is not {@link #NAME}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncTextStatus",
            exports = "textStatusParser", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncProtocolResult parseUserResult(Node child) {
        if (!child.hasDescription(NAME)) {
            throw new IllegalStateException("expected <" + NAME + ">, got <" + child.description() + ">");
        }
        var error = UsyncContactProtocol.parseError(child);
        if (error.isPresent()) {
            return error.get();
        }
        var text = child.getAttributeAsString("text").orElse(null);
        var emoji = child.getChild("emoji")
                .flatMap(e -> e.getAttributeAsString("content")).orElse(null);
        var ephemeralDuration = child.getAttributeAsLong("ephemeral_duration_sec").stream().boxed()
                .map(Duration::ofSeconds).findFirst().orElse(null);
        var lastUpdateTime = child.getAttributeAsString("last_update_time").orElse(null);
        return new TextStatusResult(text, emoji, ephemeralDuration, lastUpdateTime);
    }
}
