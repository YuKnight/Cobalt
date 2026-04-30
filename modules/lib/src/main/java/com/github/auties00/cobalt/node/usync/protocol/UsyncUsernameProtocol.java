package com.github.auties00.cobalt.node.usync.protocol;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.usync.UsyncProtocol;
import com.github.auties00.cobalt.node.usync.UsyncProtocolResult;
import com.github.auties00.cobalt.node.usync.UsyncUser;
import com.github.auties00.cobalt.node.usync.result.UsernameResult;

import java.util.Optional;

/**
 * USync {@code username} protocol descriptor. Asks the relay for each peer's
 * claimed username.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncUsername")
public final class UsyncUsernameProtocol implements UsyncProtocol {
    /**
     * Wire literal for the protocol tag name.
     */
    public static final String NAME = "username";

    /**
     * Constructs a default username-protocol descriptor.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUsername",
            exports = "USyncUsernameProtocol", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncUsernameProtocol() {
    }

    /**
     * Returns the wire literal for this protocol's tag name.
     *
     * @return the tag name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncUsername",
            exports = "USyncUsernameProtocol.getName", adaptation = WhatsAppAdaptation.DIRECT)
    public String name() {
        return NAME;
    }

    /**
     * Builds an empty {@code <username/>} query element.
     *
     * @return the query-element node
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncUsername",
            exports = "USyncUsernameProtocol.getQueryElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildQueryElement() {
        return new NodeBuilder().description(NAME).build();
    }

    /**
     * Returns no per-user element because the username protocol carries no
     * per-user payload on the request side.
     *
     * @param user the user the {@code <user>} entry refers to
     * @return always {@link Optional#empty()}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncUsername",
            exports = "USyncUsernameProtocol.getUserElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Node> buildUserElement(UsyncUser user) {
        return Optional.empty();
    }

    /**
     * Parses the {@code <username>} child of a {@code <user>} response into a
     * {@link UsernameResult} or a per-protocol error.
     *
     * @param child the protocol-tagged response node
     * @return the parsed result
     * @throws IllegalStateException if the node tag is not {@link #NAME}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncUsername",
            exports = "usernameParser", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncProtocolResult parseUserResult(Node child) {
        if (!child.hasDescription(NAME)) {
            throw new IllegalStateException("expected <" + NAME + ">, got <" + child.description() + ">");
        }
        var error = UsyncContactProtocol.parseError(child);
        if (error.isPresent()) {
            return error.get();
        }
        return new UsernameResult(child.toContentString().orElse(null));
    }
}
