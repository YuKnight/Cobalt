package com.github.auties00.cobalt.node.iq.stats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Arrays;
import java.util.Objects;

/**
 * The outbound stanza variant. Wraps the {@code <sign_credential>}
 * payload (carrying the blinded credential point and the project
 * name) in the canonical
 * {@code <iq xmlns="privatestats" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPrivatestatsSignCredentialRequest")
public final class IqIssuePrivateStatsTokenRequest implements IqOperation.Request {
    /**
     * The protocol version advertised on the {@code <sign_credential>}
     * tag. Currently fixed at {@code "2"} in WA Web.
     */
    private static final String SIGN_CREDENTIAL_VERSION = "2";

    /**
     * Raw bytes of the blinded elliptic-curve point. The relay signs
     * this point and returns the signed-credential bytes, which the
     * client unblinds to obtain the redeemable token.
     */
    private final byte[] blindedCredential;

    /**
     * Project name (UTF-8 bytes) that scopes the minted credential
     * to a particular collector endpoint. Carried verbatim as the
     * {@code <project_name>} content.
     */
    private final byte[] projectName;

    /**
     * Constructs a new request.
     *
     * @param blindedCredential the blinded credential bytes. Never
     *                          {@code null}
     * @param projectName       the project-name bytes. Never
     *                          {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public IqIssuePrivateStatsTokenRequest(byte[] blindedCredential, byte[] projectName) {
        this.blindedCredential = Objects.requireNonNull(blindedCredential, "blindedCredential cannot be null").clone();
        this.projectName = Objects.requireNonNull(projectName, "projectName cannot be null").clone();
    }

    /**
     * Returns a defensive copy of the blinded-credential bytes
     * routed into the {@code <blinded_credential>} child.
     *
     * @return a clone of the blinded-credential bytes. Never
     *         {@code null}
     */
    public byte[] blindedCredential() {
        return blindedCredential.clone();
    }

    /**
     * Returns a defensive copy of the project-name bytes routed
     * into the {@code <project_name>} child.
     *
     * @return a clone of the project-name bytes. Never {@code null}
     */
    public byte[] projectName() {
        return projectName.clone();
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <sign_credential>} payload
     *
     * @implNote {@code WASmaxOutPrivatestatsSignCredentialRequest.makeSignCredentialRequest}
     *           emits {@code <iq xmlns="privatestats" type="get"
     *           to=S_WHATSAPP_NET>} carrying a
     *           {@code <sign_credential version="2">} child with
     *           {@code <blinded_credential>} and {@code <project_name>}
     *           grandchildren.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPrivatestatsSignCredentialRequest",
            exports = "makeSignCredentialRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebIssuePrivateStatsToken",
            exports = "getToken", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        // WASmaxOutPrivatestatsSignCredentialRequest: smax("blinded_credential", null, n)
        var blindedNode = new NodeBuilder()
                .description("blinded_credential")
                .content(blindedCredential)
                .build();
        // WASmaxOutPrivatestatsSignCredentialRequest: smax("project_name", null, r)
        var projectNameNode = new NodeBuilder()
                .description("project_name")
                .content(projectName)
                .build();
        // WASmaxOutPrivatestatsSignCredentialRequest: smax("sign_credential", {version: "2"}, ...)
        var signCredentialNode = new NodeBuilder()
                .description("sign_credential")
                .attribute("version", SIGN_CREDENTIAL_VERSION)
                .content(blindedNode, projectNameNode)
                .build();
        // WASmaxOutPrivatestatsSignCredentialRequest: smax("iq", {xmlns: "privatestats", id: generateId(), type: "get", to: S_WHATSAPP_NET}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "privatestats")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(signCredentialNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqIssuePrivateStatsTokenRequest) obj;
        return Arrays.equals(this.blindedCredential, that.blindedCredential)
                && Arrays.equals(this.projectName, that.projectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(blindedCredential), Arrays.hashCode(projectName));
    }

    @Override
    public String toString() {
        return "IqIssuePrivateStatsTokenRequest[blindedCredentialLength=" + blindedCredential.length
                + ", projectNameLength=" + projectName.length + ']';
    }
}
