package com.github.auties00.cobalt.node.smax.privatestats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound {@code <iq xmlns="privatestats" type="get">} stanza
 * variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPrivatestatsSignCredentialRequest")
public final class SmaxPrivatestatsSignCredentialRequest implements SmaxOperation.Request {
    /**
     * The blinded credential bytes generated locally by the client.
     */
    private final byte[] blindedCredentialElementValue;

    /**
     * The project-name string identifying the privatestats project
     * the credential is being minted for.
     */
    private final String projectNameElementValue;

    /**
     * Constructs a new sign-credential request.
     *
     * @param blindedCredentialElementValue the blinded credential
     *                                      bytes; never {@code null}
     * @param projectNameElementValue       the project name; never
     *                                      {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxPrivatestatsSignCredentialRequest(byte[] blindedCredentialElementValue, String projectNameElementValue) {
        this.blindedCredentialElementValue = Objects.requireNonNull(blindedCredentialElementValue,
                "blindedCredentialElementValue cannot be null");
        this.projectNameElementValue = Objects.requireNonNull(projectNameElementValue,
                "projectNameElementValue cannot be null");
    }

    /**
     * Returns the blinded credential bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] blindedCredentialElementValue() {
        return blindedCredentialElementValue;
    }

    /**
     * Returns the project name.
     *
     * @return the project name; never {@code null}
     */
    public String projectNameElementValue() {
        return projectNameElementValue;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <sign_credential>} payload
     *
     * @implNote {@code WASmaxOutPrivatestatsSignCredentialRequest.makeSignCredentialRequest}
     *           emits
     *           {@code <iq xmlns="privatestats" id=generateId()
     *           type="get" to=S_WHATSAPP_NET>
     *             <sign_credential version="2">
     *               <blinded_credential>{bytes}</blinded_credential>
     *               <project_name>{string}</project_name>
     *             </sign_credential>
     *           </iq>}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPrivatestatsSignCredentialRequest",
            exports = "makeSignCredentialRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // <blinded_credential>{bytes}</blinded_credential>
        var blindedCredentialNode = new NodeBuilder()
                .description("blinded_credential")
                .content(blindedCredentialElementValue)
                .build();
        // <project_name>{string}</project_name>
        var projectNameNode = new NodeBuilder()
                .description("project_name")
                .content(projectNameElementValue)
                .build();
        // <sign_credential version="2">{children}</sign_credential>
        var signCredentialNode = new NodeBuilder()
                .description("sign_credential")
                .attribute("version", "2")
                .content(blindedCredentialNode, projectNameNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "privatestats")
                .attribute("to", Jid.userServer())
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
        var that = (SmaxPrivatestatsSignCredentialRequest) obj;
        return Arrays.equals(this.blindedCredentialElementValue, that.blindedCredentialElementValue)
                && Objects.equals(this.projectNameElementValue, that.projectNameElementValue);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(projectNameElementValue);
        result = 31 * result + Arrays.hashCode(blindedCredentialElementValue);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxPrivatestatsSignCredentialRequest[blindedCredentialElementValue="
                + Arrays.toString(blindedCredentialElementValue)
                + ", projectNameElementValue=" + projectNameElementValue + ']';
    }
}
