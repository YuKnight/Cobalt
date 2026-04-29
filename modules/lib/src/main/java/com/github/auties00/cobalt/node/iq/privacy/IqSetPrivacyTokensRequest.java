package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps the {@code <tokens>} payload
 * (one {@code <token>} grandchild per requested type) in the
 * canonical {@code <iq xmlns="privacy" type="set">} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebSetPrivacyTokensJob")
public final class IqSetPrivacyTokensRequest implements IqOperation.Request {
    /**
     * The local user's PN JID emitted into every token's
     * {@code jid} attribute.
     */
    private final Jid userJid;

    /**
     * The issuance timestamp (seconds since epoch) emitted as the
     * {@code t} attribute on every token.
     */
    private final long timestampSeconds;

    /**
     * The token types being issued; one {@code <token>} grandchild
     * per entry. Never {@code null}; never empty.
     */
    private final List<IqSetPrivacyTokensTokenType> tokenTypes;

    /**
     * Constructs a new request.
     *
     * @param userJid          the local user's PN JID; never
     *                         {@code null}
     * @param timestampSeconds the issuance timestamp in seconds
     *                         since epoch
     * @param tokenTypes       the token types to issue; never
     *                         {@code null} and never empty
     * @throws NullPointerException     if {@code userJid} or
     *                                  {@code tokenTypes} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code tokenTypes} is
     *                                  empty
     */
    public IqSetPrivacyTokensRequest(Jid userJid, long timestampSeconds, List<IqSetPrivacyTokensTokenType> tokenTypes) {
        Objects.requireNonNull(userJid, "userJid cannot be null");
        Objects.requireNonNull(tokenTypes, "tokenTypes cannot be null");
        if (tokenTypes.isEmpty()) {
            throw new IllegalArgumentException("tokenTypes cannot be empty");
        }
        this.userJid = userJid;
        this.timestampSeconds = timestampSeconds;
        this.tokenTypes = List.copyOf(tokenTypes);
    }

    /**
     * Returns the local user's PN JID.
     *
     * @return the user JID; never {@code null}
     */
    public Jid userJid() {
        return userJid;
    }

    /**
     * Returns the issuance timestamp (seconds since epoch).
     *
     * @return the timestamp in seconds
     */
    public long timestampSeconds() {
        return timestampSeconds;
    }

    /**
     * Returns the token types being issued.
     *
     * @return an unmodifiable list; never {@code null}; never empty
     */
    public List<IqSetPrivacyTokensTokenType> tokenTypes() {
        return tokenTypes;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <tokens>} payload
     *
     * @implNote {@code WAWebSetPrivacyTokensJob.issuePrivacyToken}:
     *           {@code wap("iq",{to:S_WHATSAPP_NET,type:"set",
     *           xmlns:"privacy",id}, wap("tokens", null,
     *           types.map(...))) }.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSetPrivacyTokensJob",
            exports = "issuePrivacyToken", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebSetPrivacyTokensJob: each token wap("token",{jid:USER_JID(t), t:CUSTOM_STRING(String(r)), type:CUSTOM_STRING(e)})
        var tokenNodes = new ArrayList<Node>();
        for (var type : tokenTypes) {
            var tokenNode = new NodeBuilder()
                    .description("token")
                    .attribute("jid", userJid)
                    .attribute("t", String.valueOf(timestampSeconds))
                    .attribute("type", type.wire())
                    .build();
            tokenNodes.add(tokenNode);
        }
        // WAWebSetPrivacyTokensJob: wap("tokens", null, [tokens])
        var tokensNode = new NodeBuilder()
                .description("tokens")
                .content(tokenNodes)
                .build();
        // WAWebSetPrivacyTokensJob: wap("iq",{to:S_WHATSAPP_NET,type:"set",xmlns:"privacy",id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "privacy")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(tokensNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetPrivacyTokensRequest) obj;
        return this.timestampSeconds == that.timestampSeconds
                && Objects.equals(this.userJid, that.userJid)
                && Objects.equals(this.tokenTypes, that.tokenTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userJid, timestampSeconds, tokenTypes);
    }

    @Override
    public String toString() {
        return "IqSetPrivacyTokensRequest[userJid=" + userJid
                + ", timestampSeconds=" + timestampSeconds
                + ", tokenTypes=" + tokenTypes + ']';
    }
}
