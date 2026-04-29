package com.github.auties00.cobalt.node.smax.waffle;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleGetCertificateRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleBaseIQGetRequestMixin")
public final class SmaxWaffleGetCertificateRequest implements SmaxOperation.Request {
    /**
     * The client's wall-clock at request time, in seconds since the
     * UNIX epoch.
     */
    private final long timestamp;

    /**
     * Whether to include the
     * {@code <payload_enc_certificates/>} marker in the request,
     * triggering inclusion of the
     * {@code <encryption_pem/>}/{@code <signature_pem/>} pair in the
     * reply.
     */
    private final boolean hasPayloadEncCertificates;

    /**
     * Whether to include the {@code <password_pem/>} marker in the
     * request, triggering inclusion of the {@code <password_pem/>}
     * child in the reply.
     */
    private final boolean hasPasswordPem;

    /**
     * Constructs a request.
     *
     * @param timestamp                 the UNIX epoch seconds
     * @param hasPayloadEncCertificates whether to request the
     *                                  encryption + signature PEM
     *                                  pair
     * @param hasPasswordPem            whether to request the
     *                                  password PEM
     */
    public SmaxWaffleGetCertificateRequest(long timestamp, boolean hasPayloadEncCertificates, boolean hasPasswordPem) {
        this.timestamp = timestamp;
        this.hasPayloadEncCertificates = hasPayloadEncCertificates;
        this.hasPasswordPem = hasPasswordPem;
    }

    /**
     * Returns the request timestamp.
     *
     * @return the UNIX epoch seconds
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Reports whether the encryption + signature PEM pair is
     * requested.
     *
     * @return {@code true} when the marker is present
     */
    public boolean hasPayloadEncCertificates() {
        return hasPayloadEncCertificates;
    }

    /**
     * Reports whether the password PEM is requested.
     *
     * @return {@code true} when the marker is present
     */
    public boolean hasPasswordPem() {
        return hasPasswordPem;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     *
     * @implNote {@code WASmaxOutWaffleGetCertificateRequest.makeGetCertificateRequest}
     *           composes the timestamp child plus zero, one, or both
     *           marker children inside an
     *           {@code <iq xmlns="waffle" smax_id="51"
     *           to=S_WHATSAPP_NET>} envelope.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutWaffleGetCertificateRequest",
            exports = "makeGetCertificateRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        // WASmaxOutWaffleGetCertificateRequest: smax("timestamp", null, INT(t))
        children.add(new NodeBuilder()
                .description("timestamp")
                .content(timestamp)
                .build());
        if (hasPayloadEncCertificates) {
            // WASmaxOutWaffleGetCertificateRequest.makeGetCertificateRequestPayloadEncCertificates
            children.add(new NodeBuilder()
                    .description("payload_enc_certificates")
                    .build());
        }
        if (hasPasswordPem) {
            // WASmaxOutWaffleGetCertificateRequest.makeGetCertificateRequestPasswordPem
            children.add(new NodeBuilder()
                    .description("password_pem")
                    .build());
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "waffle")
                .attribute("smax_id", 51)
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(children);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxWaffleGetCertificateRequest) obj;
        return this.timestamp == that.timestamp
                && this.hasPayloadEncCertificates == that.hasPayloadEncCertificates
                && this.hasPasswordPem == that.hasPasswordPem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, hasPayloadEncCertificates, hasPasswordPem);
    }

    @Override
    public String toString() {
        return "SmaxWaffleGetCertificateRequest[timestamp=" + timestamp
                + ", hasPayloadEncCertificates=" + hasPayloadEncCertificates
                + ", hasPasswordPem=" + hasPasswordPem + ']';
    }
}
