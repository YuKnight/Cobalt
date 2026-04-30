package com.github.auties00.cobalt.node.usync.protocol;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.usync.UsyncProtocol;
import com.github.auties00.cobalt.node.usync.UsyncProtocolResult;
import com.github.auties00.cobalt.node.usync.UsyncUser;
import com.github.auties00.cobalt.node.usync.result.DisappearingModeResult;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * USync {@code disappearing_mode} protocol descriptor. Asks the relay for the
 * peer's current disappearing-message timer.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncDisappearingMode")
public final class UsyncDisappearingModeProtocol implements UsyncProtocol {
    /**
     * Wire literal for the protocol tag name.
     */
    public static final String NAME = "disappearing_mode";

    /**
     * Constructs a default disappearing-mode-protocol descriptor.
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncDisappearingMode",
            exports = "USyncDisappearingModeProtocol", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncDisappearingModeProtocol() {
    }

    /**
     * Returns the wire literal for this protocol's tag name.
     *
     * @return the tag name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncDisappearingMode",
            exports = "USyncDisappearingModeProtocol.getName", adaptation = WhatsAppAdaptation.DIRECT)
    public String name() {
        return NAME;
    }

    /**
     * Builds an empty {@code <disappearing_mode/>} query element.
     *
     * @return the query-element node
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncDisappearingMode",
            exports = "USyncDisappearingModeProtocol.getQueryElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildQueryElement() {
        return new NodeBuilder().description(NAME).build();
    }

    /**
     * Returns no per-user element because the disappearing-mode protocol
     * carries no per-user payload on the request side.
     *
     * @param user the user the {@code <user>} entry refers to
     * @return always {@link Optional#empty()}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncDisappearingMode",
            exports = "USyncDisappearingModeProtocol.getUserElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Node> buildUserElement(UsyncUser user) {
        return Optional.empty();
    }

    /**
     * Parses the {@code <disappearing_mode>} child of a {@code <user>}
     * response into a {@link DisappearingModeResult} or a per-protocol error.
     *
     * @param child the protocol-tagged response node
     * @return the parsed result
     * @throws IllegalStateException if the node tag is not {@link #NAME}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncDisappearingMode",
            exports = "disappearingModeParser", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncProtocolResult parseUserResult(Node child) {
        if (!child.hasDescription(NAME)) {
            throw new IllegalStateException("expected <" + NAME + ">, got <" + child.description() + ">");
        }
        var error = UsyncContactProtocol.parseError(child);
        if (error.isPresent()) {
            return error.get();
        }
        var duration = Duration.ofSeconds(child.getAttributeAsLong("duration", 0L));
        var timestamp = Instant.ofEpochSecond(child.getRequiredAttributeAsLong("t"));
        var ephemeralityDisabled = "true".equals(child.getAttributeAsString("ephemerality_disabled", ""));
        return new DisappearingModeResult(duration, timestamp, ephemeralityDisabled);
    }
}
