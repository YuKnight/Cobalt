package com.github.auties00.cobalt.node.usync.protocol;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.usync.UsyncProtocol;
import com.github.auties00.cobalt.node.usync.UsyncProtocolResult;
import com.github.auties00.cobalt.node.usync.UsyncUser;
import com.github.auties00.cobalt.node.usync.result.BusinessResult;

import java.util.List;
import java.util.Optional;

/**
 * USync {@code business} protocol descriptor. Wraps a {@code <business>} query
 * carrying an inner {@code <verified_name/>} child that asks the relay for the
 * peer's verified-name signed certificate.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncBusiness")
public final class UsyncBusinessProtocol implements UsyncProtocol {
    /**
     * Wire literal for the protocol tag name.
     */
    public static final String NAME = "business";

    /**
     * Constructs a default business-protocol descriptor.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncBusiness",
            exports = "USyncBusinessProtocol", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncBusinessProtocol() {
    }

    /**
     * Returns the wire literal for this protocol's tag name.
     *
     * @return the tag name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncBusiness",
            exports = "USyncBusinessProtocol.getName", adaptation = WhatsAppAdaptation.DIRECT)
    public String name() {
        return NAME;
    }

    /**
     * Builds the {@code <business>} query element wrapping a single empty
     * {@code <verified_name/>} child.
     *
     * @return the query-element node
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncBusiness",
            exports = "USyncBusinessProtocol.getQueryElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildQueryElement() {
        return new NodeBuilder()
                .description(NAME)
                .content(List.of(new NodeBuilder().description("verified_name").build()))
                .build();
    }

    /**
     * Returns no per-user element because the business protocol carries no
     * per-user payload on the request side.
     *
     * @param user the user the {@code <user>} entry refers to
     * @return always {@link Optional#empty()}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncBusiness",
            exports = "USyncBusinessProtocol.getUserElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Node> buildUserElement(UsyncUser user) {
        return Optional.empty();
    }

    /**
     * Parses the {@code <business>} child of a {@code <user>} response into a
     * {@link BusinessResult} or a per-protocol error.
     *
     * @param child the protocol-tagged response node
     * @return the parsed result
     * @throws IllegalStateException if the node tag is not {@link #NAME}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncBusiness",
            exports = "businessParser", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncProtocolResult parseUserResult(Node child) {
        if (!child.hasDescription(NAME)) {
            throw new IllegalStateException("expected <" + NAME + ">, got <" + child.description() + ">");
        }
        var error = UsyncContactProtocol.parseError(child);
        if (error.isPresent()) {
            return error.get();
        }
        return new BusinessResult(child.getChild("verified_name").orElse(null));
    }
}
