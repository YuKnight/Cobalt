package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the optional {@code <identifier>}
 * child inside the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="get">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingGetAccountNonceRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingHackBaseIQGetRequestMixin")
public final class SmaxGetAccountNonceRequest implements SmaxOperation.Request {
    /**
     * The optional {@code scope} attribute of the {@code <identifier>}
     * child; {@code null} omits the child entirely.
     */
    private final String identifierScope;

    /**
     * Constructs a request without an {@code <identifier>} child.
     */
    public SmaxGetAccountNonceRequest() {
        this(null);
    }

    /**
     * Constructs a request optionally carrying an
     * {@code <identifier scope="..."/>} child.
     *
     * @param identifierScope the {@code scope} attribute; may be
     *                        {@code null} to omit the child
     */
    public SmaxGetAccountNonceRequest(String identifierScope) {
        this.identifierScope = identifierScope;
    }

    /**
     * Returns the optional identifier scope.
     *
     * @return an {@link Optional} carrying the scope, or empty when the
     *         child is omitted
     */
    public Optional<String> identifierScope() {
        return Optional.ofNullable(identifierScope);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         optional {@code <identifier/>} payload
     *
     * @implNote {@code WASmaxOutBizLinkingGetAccountNonceRequest.makeGetAccountNonceRequest}
     *           composes
     *           {@code WASmaxOutBizLinkingHackBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over a
     *           bare {@code <iq xmlns="fb:thrift_iq" smax_id=12>} root
     *           that optionally carries a single
     *           {@code <identifier scope/>} child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizLinkingGetAccountNonceRequest",
            exports = "makeGetAccountNonceRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("type", "get");
        if (identifierScope != null) {
            var identifierNode = new NodeBuilder()
                    .description("identifier")
                    .attribute("scope", identifierScope)
                    .build();
            builder.content(identifierNode);
        }
        return builder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGetAccountNonceRequest) obj;
        return Objects.equals(this.identifierScope, that.identifierScope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifierScope);
    }

    @Override
    public String toString() {
        return "SmaxGetAccountNonceRequest[identifierScope=" + identifierScope + ']';
    }
}
