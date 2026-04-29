package com.github.auties00.cobalt.node.smax.bot;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps an optional list of scoping
 * {@code <bot jid/>} children and the version / digest attributes
 * in the canonical {@code <iq xmlns="bot" type="get"
 * to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBotBotListRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBotBotListIQMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBotBaseIQGetRequestMixin")
public final class SmaxBotBotListRequest implements SmaxOperation.Request {
    /**
     * The optional protocol revision the client supports — typically
     * {@code "2"} or {@code "3"}; may be {@code null}.
     */
    private final String botV;

    /**
     * The optional client-side directory digest; may be {@code null}.
     */
    private final String botBhash;

    /**
     * The list of bot JIDs to scope the query to; may be empty for
     * an unconstrained directory fetch.
     */
    private final List<Jid> botArgs;

    /**
     * Constructs a request.
     *
     * @param botV     the optional protocol revision; may be
     *                 {@code null}
     * @param botBhash the optional digest; may be {@code null}
     * @param botArgs  the bot JIDs to scope the query to; never
     *                 {@code null}, may be empty
     * @throws NullPointerException if {@code botArgs} is {@code null}
     */
    public SmaxBotBotListRequest(String botV, String botBhash, List<Jid> botArgs) {
        this.botV = botV;
        this.botBhash = botBhash;
        Objects.requireNonNull(botArgs, "botArgs cannot be null");
        this.botArgs = List.copyOf(botArgs);
    }

    /**
     * Returns the optional protocol revision.
     *
     * @return an {@link Optional} carrying the version
     */
    public Optional<String> botV() {
        return Optional.ofNullable(botV);
    }

    /**
     * Returns the optional client-side digest.
     *
     * @return an {@link Optional} carrying the digest
     */
    public Optional<String> botBhash() {
        return Optional.ofNullable(botBhash);
    }

    /**
     * Returns the bot JIDs scoping the query.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Jid> botArgs() {
        return botArgs;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <bot/>} payload
     *
     * @implNote {@code WASmaxOutBotBotListRequest.makeBotListRequest}
     *           composes
     *           {@code WASmaxOutBotBotListIQMixin}
     *           ({@code xmlns="bot"}, {@code to="s.whatsapp.net"})
     *           with
     *           {@code WASmaxOutBotBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over
     *           a {@code <bot v? bhash?>REPEATED_CHILD(bot, args, 0, ∞)</bot>}
     *           payload.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBotBotListRequest",
            exports = "makeBotListRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutBotBotListRequest: smax("bot", {v?, bhash?}, REPEATED_CHILD(bot, args, 0, ∞))
        var topBotBuilder = new NodeBuilder()
                .description("bot");
        if (botV != null) {
            topBotBuilder.attribute("v", botV);
        }
        if (botBhash != null) {
            topBotBuilder.attribute("bhash", botBhash);
        }
        for (var argJid : botArgs) {
            // WASmaxOutBotBotListRequest: smax("bot", {jid: JID(t)})
            var argNode = new NodeBuilder()
                    .description("bot")
                    .attribute("jid", argJid)
                    .build();
            topBotBuilder.content(argNode);
        }
        // smax("iq", {to: S_WHATSAPP_NET, xmlns: "bot", id: generateId(), type: "get"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "bot")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(topBotBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBotBotListRequest) obj;
        return Objects.equals(this.botV, that.botV)
                && Objects.equals(this.botBhash, that.botBhash)
                && Objects.equals(this.botArgs, that.botArgs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(botV, botBhash, botArgs);
    }

    @Override
    public String toString() {
        return "SmaxBotBotListRequest[botV=" + botV
                + ", botBhash=" + botBhash
                + ", botArgs=" + botArgs + ']';
    }
}
