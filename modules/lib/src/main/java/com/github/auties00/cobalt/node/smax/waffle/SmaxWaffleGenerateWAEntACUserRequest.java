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
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleGenerateWAEntACUserRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleBaseIQGetRequestMixin")
public final class SmaxWaffleGenerateWAEntACUserRequest implements SmaxOperation.Request {
    /**
     * The RSA encryption metadata subtree.
     */
    private final SmaxWaffleRsaEncryptionMetadata encryptionMetadata;

    /**
     * The client's wall-clock at request time.
     */
    private final long timestamp;

    /**
     * The numeric id of the legal-disclosure record the user
     * accepted at link-time.
     */
    private final int disclosureId;

    /**
     * The version string of the legal-disclosure record.
     */
    private final String disclosureVersion;

    /**
     * The user's language-group code (e.g. {@code "en"}).
     */
    private final String disclosureLg;

    /**
     * The user's locale code (e.g. {@code "US"}).
     */
    private final String disclosureLc;

    /**
     * Constructs a request.
     *
     * @param encryptionMetadata the RSA encryption metadata; never
     *                           {@code null}
     * @param timestamp          the UNIX epoch seconds at request time
     * @param disclosureId       the disclosure record id
     * @param disclosureVersion  the disclosure version; never
     *                           {@code null}
     * @param disclosureLg       the language-group code; never
     *                           {@code null}
     * @param disclosureLc       the locale code; never {@code null}
     * @throws NullPointerException if any object argument is
     *                              {@code null}
     */
    public SmaxWaffleGenerateWAEntACUserRequest(SmaxWaffleRsaEncryptionMetadata encryptionMetadata, long timestamp,
                   int disclosureId, String disclosureVersion,
                   String disclosureLg, String disclosureLc) {
        this.encryptionMetadata = Objects.requireNonNull(encryptionMetadata, "encryptionMetadata cannot be null");
        this.timestamp = timestamp;
        this.disclosureId = disclosureId;
        this.disclosureVersion = Objects.requireNonNull(disclosureVersion, "disclosureVersion cannot be null");
        this.disclosureLg = Objects.requireNonNull(disclosureLg, "disclosureLg cannot be null");
        this.disclosureLc = Objects.requireNonNull(disclosureLc, "disclosureLc cannot be null");
    }

    /**
     * Returns the RSA encryption metadata.
     *
     * @return the metadata; never {@code null}
     */
    public SmaxWaffleRsaEncryptionMetadata encryptionMetadata() {
        return encryptionMetadata;
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
     * Returns the disclosure record id.
     *
     * @return the id
     */
    public int disclosureId() {
        return disclosureId;
    }

    /**
     * Returns the disclosure version.
     *
     * @return the version; never {@code null}
     */
    public String disclosureVersion() {
        return disclosureVersion;
    }

    /**
     * Returns the language-group code.
     *
     * @return the lg code; never {@code null}
     */
    public String disclosureLg() {
        return disclosureLg;
    }

    /**
     * Returns the locale code.
     *
     * @return the lc code; never {@code null}
     */
    public String disclosureLc() {
        return disclosureLc;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     *
     * @implNote {@code WASmaxOutWaffleGenerateWAEntACUserRequest.makeGenerateWAEntACUserRequest}
     *           composes
     *           {@code WASmaxOutWaffleBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over the
     *           {@code <encryption_metadata/>}/{@code <timestamp/>}/{@code <disclosure id version lg lc/>}
     *           triple inside an
     *           {@code <iq xmlns="waffle" smax_id="37"
     *           to=S_WHATSAPP_NET>} envelope.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutWaffleGenerateWAEntACUserRequest",
            exports = "makeGenerateWAEntACUserRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var encryptionMetadataNode = encryptionMetadata.toNode();
        var timestampNode = new NodeBuilder()
                .description("timestamp")
                .content(timestamp)
                .build();
        var disclosureNode = new NodeBuilder()
                .description("disclosure")
                .attribute("id", disclosureId)
                .attribute("version", disclosureVersion)
                .attribute("lg", disclosureLg)
                .attribute("lc", disclosureLc)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "waffle")
                .attribute("smax_id", 37)
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(encryptionMetadataNode, timestampNode, disclosureNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxWaffleGenerateWAEntACUserRequest) obj;
        return this.timestamp == that.timestamp
                && this.disclosureId == that.disclosureId
                && Objects.equals(this.encryptionMetadata, that.encryptionMetadata)
                && Objects.equals(this.disclosureVersion, that.disclosureVersion)
                && Objects.equals(this.disclosureLg, that.disclosureLg)
                && Objects.equals(this.disclosureLc, that.disclosureLc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryptionMetadata, timestamp, disclosureId,
                disclosureVersion, disclosureLg, disclosureLc);
    }

    @Override
    public String toString() {
        return "SmaxWaffleGenerateWAEntACUserRequest[timestamp=" + timestamp
                + ", disclosureId=" + disclosureId
                + ", disclosureVersion=" + disclosureVersion
                + ", disclosureLg=" + disclosureLg
                + ", disclosureLc=" + disclosureLc + ']';
    }
}
