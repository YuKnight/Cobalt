package com.github.auties00.cobalt.node.usync.protocol;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.usync.UsyncAddressingMode;
import com.github.auties00.cobalt.node.usync.UsyncProtocol;
import com.github.auties00.cobalt.node.usync.UsyncProtocolResult;
import com.github.auties00.cobalt.node.usync.result.UsyncProtocolError;
import com.github.auties00.cobalt.node.usync.UsyncUser;
import com.github.auties00.cobalt.node.usync.result.ContactResult;

import java.time.Duration;
import java.util.Optional;

/**
 * USync {@code contact} protocol descriptor. Asks the relay whether each peer
 * is a registered WhatsApp user and optionally resolves usernames or LIDs.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncContact")
public final class UsyncContactProtocol implements UsyncProtocol {
    /**
     * Wire literal for the protocol tag name.
     */
    public static final String NAME = "contact";

    /**
     * Holds the addressing mode the request applies to. {@code null} means
     * the default phone-number addressing.
     */
    private final UsyncAddressingMode addressingMode;

    /**
     * Creates a contact-protocol descriptor for the given addressing mode.
     *
     * @param addressingMode the addressing mode, or {@code null} for the
     *                       default phone-number addressing
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncContact",
            exports = "USyncContactProtocol", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncContactProtocol(UsyncAddressingMode addressingMode) {
        this.addressingMode = addressingMode;
    }

    /**
     * Creates a contact-protocol descriptor with {@link UsyncAddressingMode#PN}
     * addressing.
     */
    public UsyncContactProtocol() {
        this(UsyncAddressingMode.PN);
    }

    /**
     * Returns the wire literal for this protocol's tag name.
     *
     * @return the tag name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncContact",
            exports = "USyncContactProtocol.getName", adaptation = WhatsAppAdaptation.DIRECT)
    public String name() {
        return NAME;
    }

    /**
     * Builds the {@code <contact>} query element. Emits the
     * {@code addressing_mode} attribute only when the LID mode is selected,
     * mirroring the JS frozen-object {@code DROP_ATTR} default.
     *
     * @return the query-element node
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncContact",
            exports = "USyncContactProtocol.getQueryElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildQueryElement() {
        var builder = new NodeBuilder().description(NAME);
        if (addressingMode == UsyncAddressingMode.LID) {
            builder.attribute("addressing_mode", UsyncAddressingMode.LID.wireValue());
        }
        return builder.build();
    }

    /**
     * Builds the per-user {@code <contact>} element. Picks the addressing
     * shape (phone-number content, username attributes, or contact-type
     * attribute) based on which slots the user carries.
     *
     * @param user the user the {@code <user>} entry refers to
     * @return the per-user element, or empty when the user carries none of
     *     the supported slots
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncContact",
            exports = "USyncContactProtocol.getUserElement", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Node> buildUserElement(UsyncUser user) {
        if (user.phoneNumber().isPresent()) {
            return Optional.of(new NodeBuilder()
                    .description(NAME)
                    .content(user.phoneNumber().get().getBytes())
                    .build());
        }
        if (user.username().isPresent()) {
            var builder = new NodeBuilder().description(NAME)
                    .attribute("username", user.username().get());
            user.pin().ifPresent(p -> builder.attribute("pin", p));
            user.lid().ifPresent(l -> builder.attribute("lid", l.toString()));
            return Optional.of(builder.build());
        }
        if (user.contactType().isPresent()) {
            return Optional.of(new NodeBuilder()
                    .description(NAME)
                    .attribute("type", user.contactType().get())
                    .build());
        }
        return Optional.empty();
    }

    /**
     * Parses the {@code <contact>} child of a {@code <user>} response into a
     * {@link ContactResult} or a per-protocol error.
     *
     * @param child the protocol-tagged response node
     * @return the parsed result
     * @throws IllegalStateException if the node tag is not {@link #NAME} or
     *     the required {@code type} attribute is missing
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsyncContact",
            exports = "contactParser", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncProtocolResult parseUserResult(Node child) {
        if (!child.hasDescription(NAME)) {
            throw new IllegalStateException("expected <" + NAME + ">, got <" + child.description() + ">");
        }
        var error = parseError(child);
        if (error.isPresent()) {
            return error.get();
        }
        var type = child.getAttributeAsString("type")
                .orElseThrow(() -> new IllegalStateException("[usync] contact node has missing type attribute"));
        var username = child.getAttributeAsString("username").orElse(null);
        var content = child.toContentString().orElse(null);
        return new ContactResult(type, username, content);
    }

    /**
     * Probes the optional {@code <error/>} child of a USync protocol response.
     * Reused by every protocol parser to share the per-protocol error decode.
     *
     * @param child the protocol-tagged response node
     * @return the parsed error, or empty when the response is a success
     */
    public static Optional<UsyncProtocolError> parseError(Node child) {
        return child.getChild("error").map(err -> new UsyncProtocolError(
                err.getRequiredAttributeAsInt("code"),
                err.getAttributeAsString("text", ""),
                err.getAttributeAsLong("error_backoff").stream().boxed()
                        .map(Duration::ofSeconds).findFirst().orElse(null)));
    }
}
