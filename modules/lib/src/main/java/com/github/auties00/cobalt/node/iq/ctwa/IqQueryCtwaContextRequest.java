package com.github.auties00.cobalt.node.iq.ctwa;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps the
 * {@code <account_number>}/{@code <code>}/{@code <expected_source_url>}
 * grandchildren in the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="get"/>} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryCtwaContextJob")
public final class IqQueryCtwaContextRequest implements IqOperation.Request {
    /**
     * The business account's phone number (legacy-formatted JID
     * string) — extracted via
     * {@code WAWebWidFactory.createWid(...).toString({legacy:true})}.
     */
    private final String accountNumber;

    /**
     * The CTWA redirect code carried by the originating ad funnel.
     */
    private final String code;

    /**
     * The expected source URL the client received from the ad
     * funnel. The relay echoes this back to confirm anti-spoofing.
     */
    private final String expectedSourceUrl;

    /**
     * Constructs a new request.
     *
     * @param accountNumber     the business phone (legacy JID
     *                          string); never {@code null}
     * @param code              the redirect code; never {@code null}
     * @param expectedSourceUrl the expected source URL; never
     *                          {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public IqQueryCtwaContextRequest(String accountNumber, String code, String expectedSourceUrl) {
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber cannot be null");
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.expectedSourceUrl = Objects.requireNonNull(expectedSourceUrl, "expectedSourceUrl cannot be null");
    }

    /**
     * Returns the business phone (legacy JID string).
     *
     * @return the account number; never {@code null}
     */
    public String accountNumber() {
        return accountNumber;
    }

    /**
     * Returns the CTWA redirect code.
     *
     * @return the code; never {@code null}
     */
    public String code() {
        return code;
    }

    /**
     * Returns the expected source URL.
     *
     * @return the URL; never {@code null}
     */
    public String expectedSourceUrl() {
        return expectedSourceUrl;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the three grandchildren
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryCtwaContextJob",
            exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebQueryCtwaContextJob: wap("account_number", null, biz)
        var accountNumberNode = new NodeBuilder()
                .description("account_number")
                .content(accountNumber)
                .build();
        // WAWebQueryCtwaContextJob: wap("code", null, code)
        var codeNode = new NodeBuilder()
                .description("code")
                .content(code)
                .build();
        // WAWebQueryCtwaContextJob: wap("expected_source_url", null, src)
        var expectedSourceUrlNode = new NodeBuilder()
                .description("expected_source_url")
                .content(expectedSourceUrl)
                .build();
        // WAWebQueryCtwaContextJob: wap("iq",{xmlns:"fb:thrift_iq", id, type:"get", to:S_WHATSAPP_NET, smax_id:CtwaGetContext}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(accountNumberNode, codeNode, expectedSourceUrlNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqQueryCtwaContextRequest) obj;
        return Objects.equals(this.accountNumber, that.accountNumber)
                && Objects.equals(this.code, that.code)
                && Objects.equals(this.expectedSourceUrl, that.expectedSourceUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber, code, expectedSourceUrl);
    }

    @Override
    public String toString() {
        return "IqQueryCtwaContextRequest[accountNumber=" + accountNumber
                + ", code=" + code
                + ", expectedSourceUrl=" + expectedSourceUrl + ']';
    }
}
