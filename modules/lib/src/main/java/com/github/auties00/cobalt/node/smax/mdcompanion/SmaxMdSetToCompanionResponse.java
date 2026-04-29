package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The inbound projection of the {@code <iq><pair-device/></iq>}
 * stanza.
 *
 * @implNote {@code WASmaxInMdSetToCompanionRequest.parseSetToCompanionRequest}
 *           validates the {@code <iq xmlns="md" from="s.whatsapp.net"
 *           type="set">} envelope, asserts the presence of the
 *           {@code <pair-device/>} child, and extracts the six
 *           {@code <ref/>} content-byte payloads.
 */
@WhatsAppWebModule(moduleName = "WASmaxInMdSetToCompanionRequest")
@WhatsAppWebModule(moduleName = "WASmaxInMdBaseIQSetRequestMixin")
public final class SmaxMdSetToCompanionResponse implements SmaxOperation.Response {
    /**
     * The {@code id} attribute of the inbound IQ stanza — needed to
     * echo back into the {@link SmaxMdSetToCompanionAcknowledgement} ack.
     */
    private final String iqId;

    /**
     * The {@code from} attribute of the inbound IQ stanza — needed
     * to echo back into the {@link SmaxMdSetToCompanionAcknowledgement} ack as the ack's
     * {@code to} attribute.
     */
    private final Jid iqFrom;

    /**
     * The list of pair-device reference byte payloads (always six
     * entries per WA Web).
     */
    private final List<byte[]> pairDeviceRefs;

    /**
     * Constructs a new {@code SmaxMdSetToCompanionResponse} projection.
     *
     * @param iqId           the IQ id; never {@code null}
     * @param iqFrom         the IQ from JID; never {@code null}
     * @param pairDeviceRefs the list of ref byte payloads; never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxMdSetToCompanionResponse(String iqId, Jid iqFrom, List<byte[]> pairDeviceRefs) {
        this.iqId = Objects.requireNonNull(iqId, "iqId cannot be null");
        this.iqFrom = Objects.requireNonNull(iqFrom, "iqFrom cannot be null");
        this.pairDeviceRefs = List.copyOf(Objects.requireNonNull(pairDeviceRefs, "pairDeviceRefs cannot be null"));
    }

    /**
     * Returns the IQ id.
     *
     * @return the id; never {@code null}
     */
    public String iqId() {
        return iqId;
    }

    /**
     * Returns the IQ from JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid iqFrom() {
        return iqFrom;
    }

    /**
     * Returns the list of pair-device reference byte payloads.
     *
     * @return an unmodifiable list of byte arrays; never {@code null}
     */
    public List<byte[]> pairDeviceRefs() {
        return pairDeviceRefs;
    }

    /**
     * Tries to parse an {@link SmaxMdSetToCompanionResponse} projection.
     *
     * @param node the inbound IQ stanza; never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     *         when the stanza does not match the documented shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInMdSetToCompanionRequest",
            exports = "parseSetToCompanionRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxMdSetToCompanionResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("iq")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("xmlns", "md")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("type", "set")) {
            return Optional.empty();
        }
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null || !"s.whatsapp.net".equals(from.server().toString())) {
            return Optional.empty();
        }
        var id = node.getAttributeAsString("id").orElse(null);
        if (id == null) {
            return Optional.empty();
        }
        var pairDevice = node.getChild("pair-device").orElse(null);
        if (pairDevice == null) {
            return Optional.empty();
        }
        var refs = pairDevice.streamChildren("ref")
                .map(ref -> ref.toContentBytes().orElse(null))
                .filter(Objects::nonNull)
                .toList();
        if (refs.size() != 6) {
            return Optional.empty();
        }
        return Optional.of(new SmaxMdSetToCompanionResponse(id, from, refs));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdSetToCompanionResponse) obj;
        if (!Objects.equals(this.iqId, that.iqId) || !Objects.equals(this.iqFrom, that.iqFrom)) {
            return false;
        }
        if (this.pairDeviceRefs.size() != that.pairDeviceRefs.size()) {
            return false;
        }
        for (var i = 0; i < this.pairDeviceRefs.size(); i++) {
            if (!Arrays.equals(this.pairDeviceRefs.get(i), that.pairDeviceRefs.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(iqId, iqFrom);
        for (var ref : pairDeviceRefs) {
            result = 31 * result + Arrays.hashCode(ref);
        }
        return result;
    }

    @Override
    public String toString() {
        var refsStr = new StringBuilder("[");
        for (var i = 0; i < pairDeviceRefs.size(); i++) {
            if (i > 0) {
                refsStr.append(", ");
            }
            refsStr.append(Arrays.toString(pairDeviceRefs.get(i)));
        }
        refsStr.append(']');
        return "SmaxMdSetToCompanionResponse[iqId=" + iqId
                + ", iqFrom=" + iqFrom
                + ", pairDeviceRefs=" + refsStr + ']';
    }
}
