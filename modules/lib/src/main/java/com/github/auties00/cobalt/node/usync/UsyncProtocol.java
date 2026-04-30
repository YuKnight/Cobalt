package com.github.auties00.cobalt.node.usync;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.usync.protocol.UsyncBotProfileProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncBusinessProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncContactProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncDeviceProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncDisappearingModeProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncFeatureProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncLidProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncPictureProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncStatusProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncTextStatusProtocol;
import com.github.auties00.cobalt.node.usync.protocol.UsyncUsernameProtocol;

import java.util.Optional;

/**
 * Sealed interface implemented by every USync protocol descriptor.
 *
 * <p>A USync stanza is composed of one or more protocol elements that describe
 * what the client wants to learn about each user, plus one {@code <user>} entry
 * per peer with optional protocol-specific child elements. Each protocol
 * declares three things. Its wire {@link #name()} is the literal tag name on
 * the {@code <query>} child. The {@link #buildQueryElement()} method emits the
 * {@code <query>} child, often empty and sometimes carrying the protocol's
 * request shape. The {@link #buildUserElement(UsyncUser)} method emits an
 * optional child inside each {@code <user>} entry, returning
 * {@link Optional#empty()} when the protocol has no per-user payload.
 *
 * <p>Protocols also expose {@link #parseUserResult(Node)} that consumes a
 * {@code <user>}-child node and returns the protocol-specific result. The
 * shared result type is {@link UsyncProtocolResult}.
 *
 * <p>The eleven permitted implementations correspond one-to-one with the
 * eleven {@code WAWebUsync*Protocol} JS classes.
 *
 * @implNote The JS module {@code WAWebUsync} enumerates the protocols in the
 *     constants object {@code c}. Cobalt keeps the same closed enumeration by
 *     sealing this interface. The
 *     {@link com.github.auties00.cobalt.node.usync.protocol} sub-package holds
 *     the implementations and {@link com.github.auties00.cobalt.node.usync.result}
 *     holds the result types so this file stays a thin contract surface.
 */
@WhatsAppWebModule(moduleName = "WAWebUsync")
public sealed interface UsyncProtocol permits
        UsyncContactProtocol,
        UsyncDeviceProtocol,
        UsyncFeatureProtocol,
        UsyncBusinessProtocol,
        UsyncPictureProtocol,
        UsyncStatusProtocol,
        UsyncDisappearingModeProtocol,
        UsyncLidProtocol,
        UsyncBotProfileProtocol,
        UsyncUsernameProtocol,
        UsyncTextStatusProtocol {

    /**
     * Returns the literal protocol name as it appears on the wire. Examples
     * include {@code "contact"}, {@code "devices"}, and {@code "feature"}.
     *
     * @return the protocol's tag name
     */
    String name();

    /**
     * Builds the protocol's child of the {@code <query>} element.
     *
     * <p>Most protocols emit an empty element such as {@code <picture/>}. The
     * device protocol carries a {@code version="2"} attribute, the contact
     * protocol optionally carries {@code addressing_mode="lid"}, and the
     * feature protocol carries one empty child per requested feature key.
     *
     * @return the query-element node
     */
    Node buildQueryElement();

    /**
     * Builds the optional per-user child inside a {@code <user>} entry.
     *
     * <p>Many protocols return {@link Optional#empty()} because they have no
     * per-user payload. The protocol's mere presence in the {@code <query>}
     * element is enough.
     *
     * @param user the user the {@code <user>} entry refers to
     * @return the protocol-specific child element, or empty
     * @implNote The JS counterpart {@code WAWebUsync*Protocol.getUserElement}
     *     returns either a {@code WAWap.wap(...)} expression or {@code null}.
     *     {@link Optional#empty()} mirrors {@code null}.
     */
    Optional<Node> buildUserElement(UsyncUser user);

    /**
     * Parses the protocol's child of a {@code <user>} response into a Java
     * result.
     *
     * <p>If the relay returned a per-protocol error, the implementation
     * returns a {@link com.github.auties00.cobalt.node.usync.result.UsyncProtocolError}.
     * Otherwise it returns the protocol-specific success variant declared by
     * the corresponding permit of {@link UsyncProtocolResult}.
     *
     * @param userChild the child node tagged with this protocol's
     *                  {@link #name()}, located inside a {@code <user>} result
     *                  entry
     * @return the parsed result, never {@code null}
     */
    UsyncProtocolResult parseUserResult(Node userChild);
}
