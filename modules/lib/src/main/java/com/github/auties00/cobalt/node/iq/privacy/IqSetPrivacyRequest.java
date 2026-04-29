package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WAWebSetPrivacyJob")
public final class IqSetPrivacyRequest implements IqOperation.Request {
    /**
     * The privacy category being set.
     */
    private final IqQueryPrivacySettingsCategoryName name;

    /**
     * The new visibility value for the category.
     */
    private final IqQueryPrivacySettingsVisibility value;

    /**
     * The optional user-list mutation list. When empty the request
     * omits user children entirely (bare-category shape). When
     * non-empty the wire shape depends on {@link #addressingMode}.
     */
    private final List<IqSetPrivacyUserEntry> users;

    /**
     * The wire addressing mode — selects between PN and LID
     * envelopes when {@link #users} is non-empty.
     */
    private final IqSetPrivacyAddressingMode addressingMode;

    /**
     * The optional category-list digest — emitted as
     * {@code dhash="…"} on the {@code <category/>} element when
     * {@link #users} is non-empty. {@code null} maps to the literal
     * {@code "none"} per WA Web's fallback.
     */
    private final String dhash;

    /**
     * Constructs a request.
     *
     * @param name           the category; never {@code null}
     * @param value          the new value; never {@code null}
     * @param users          the user-list mutation; never
     *                       {@code null} (use {@link List#of()} for
     *                       the bare-category shape)
     * @param addressingMode the addressing mode; never {@code null}
     * @param dhash          the optional category-list digest; may be
     *                       {@code null}
     * @throws NullPointerException if any non-{@code null}-allowed
     *                              argument is {@code null}
     */
    public IqSetPrivacyRequest(IqQueryPrivacySettingsCategoryName name,
                   IqQueryPrivacySettingsVisibility value,
                   List<IqSetPrivacyUserEntry> users,
                   IqSetPrivacyAddressingMode addressingMode,
                   String dhash) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(users, "users cannot be null");
        this.users = List.copyOf(users);
        this.addressingMode = Objects.requireNonNull(addressingMode, "addressingMode cannot be null");
        this.dhash = dhash;
    }

    /**
     * Returns the category being set.
     *
     * @return the category; never {@code null}
     */
    public IqQueryPrivacySettingsCategoryName name() {
        return name;
    }

    /**
     * Returns the new value for the category.
     *
     * @return the value; never {@code null}
     */
    public IqQueryPrivacySettingsVisibility value() {
        return value;
    }

    /**
     * Returns the user-list mutation list.
     *
     * @return an unmodifiable list; never {@code null}; empty for
     *         the bare-category shape
     */
    public List<IqSetPrivacyUserEntry> users() {
        return users;
    }

    /**
     * Returns the wire addressing mode.
     *
     * @return the addressing mode; never {@code null}
     */
    public IqSetPrivacyAddressingMode addressingMode() {
        return addressingMode;
    }

    /**
     * Returns the optional category-list digest.
     *
     * @return an {@link Optional} carrying the digest, or empty when
     *         the caller did not supply one
     */
    public Optional<String> dhash() {
        return Optional.ofNullable(dhash);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     *
     * @implNote {@code WAWebSetPrivacyJob.setPrivacy} composes
     *           {@code wap("iq",{to:S_WHATSAPP_NET, type:"set",
     *           xmlns:"privacy", id:generateId()}, p(...))}, where
     *           {@code p} dispatches between the bare/{@code _},
     *           LID/{@code f}, and PN/{@code g} category builders.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSetPrivacyJob",
            exports = "setPrivacy", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var categoryBuilder = new NodeBuilder()
                .description("category")
                .attribute("name", name.wire())
                .attribute("value", value.wire());
        if (!users.isEmpty()) {
            categoryBuilder.attribute("dhash", dhash != null ? dhash : "none");
            var userNodes = new ArrayList<Node>();
            for (var entry : users) {
                var userBuilder = new NodeBuilder()
                        .description("user")
                        .attribute("action", entry.action().wire())
                        .attribute("jid", entry.jid());
                if (addressingMode == IqSetPrivacyAddressingMode.LID) {
                    if (entry.username().isPresent()) {
                        userBuilder.attribute("username", entry.username().get());
                    } else if (entry.pnJid().isPresent()) {
                        userBuilder.attribute("pn_jid", entry.pnJid().get());
                    }
                }
                userNodes.add(userBuilder.build());
            }
            categoryBuilder.content(userNodes);
        }
        var privacyBuilder = new NodeBuilder()
                .description("privacy");
        if (addressingMode == IqSetPrivacyAddressingMode.LID && !users.isEmpty()) {
            privacyBuilder.attribute("addressing_mode", "lid");
        }
        var privacyNode = privacyBuilder
                .content(categoryBuilder.build())
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "privacy")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(privacyNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetPrivacyRequest) obj;
        return this.name == that.name
                && this.value == that.value
                && this.addressingMode == that.addressingMode
                && Objects.equals(this.users, that.users)
                && Objects.equals(this.dhash, that.dhash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, users, addressingMode, dhash);
    }

    @Override
    public String toString() {
        return "IqSetPrivacyRequest[name=" + name + ", value=" + value
                + ", users=" + users + ", addressingMode=" + addressingMode
                + ", dhash=" + dhash + ']';
    }
}
