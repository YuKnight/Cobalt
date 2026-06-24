package com.github.auties00.cobalt.calls2.signaling;

import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represents one {@code <te>} latency report inside a {@code <relaylatency>} message.
 *
 * <p>A relay-latency report carries one such entry per relay the device probed. Each entry reports
 * the measured round-trip latency to one relay together with the device's estimated bandwidth toward
 * it and an optional connection identifier. The latency value is named {@code latency} on the wire
 * when it is a plain measurement and {@code xlatency} when it is an encrypted-round-trip measurement;
 * the {@link #encryptedRtt() encrypted-RTT flag} records which name applies. The {@code ul_bw} and
 * {@code dl_bw} attributes are the uplink and downlink bandwidth estimates and are omitted from the
 * wire when zero. The {@code is_favored} attribute marks the relay the device prefers. The
 * {@code conn_id} attribute, present only for a non-zero connection id, is the base64 encoding of an
 * eight-byte connection identifier. An optional {@code <enc>} child carries an encrypted measurement
 * blob.
 *
 * <p>On the wire the entry is
 * {@snippet lang="xml" :
 * <te latency="42" ul_bw="500" dl_bw="800" is_favored="1" relay_name="mxp1c01" conn_id="AAAAAAAAAAA=">
 *   <enc>...bytes...</enc>
 * </te>
 * }
 * with {@code latency} replaced by {@code xlatency} when the measurement is an encrypted round trip.
 *
 * @implNote This implementation models the {@code <te>} entry built by {@code serialize_relay_latency}
 * (fn11727) and parsed by {@code deserialize_te} (fn11622) in the wa-voip WASM module
 * {@code ff-tScznZ8P} ({@code stanzas/transport.cc}). The {@code latency} versus {@code xlatency}
 * attribute-name selection follows the encrypted-RTT flag the engine stores at the entry struct's
 * {@code +0x25}; the {@code ul_bw}/{@code dl_bw} omit-when-zero behaviour and the {@code conn_id}
 * present-only-when-non-zero behaviour are the serializer's. The {@code conn_id} is the base64 of an
 * eight-byte connection id, decoded here through {@link Base64}.
 *
 * @param latency      the measured round-trip latency
 * @param encryptedRtt whether the latency is an encrypted-round-trip measurement, selecting the
 *                     {@code xlatency} attribute name over {@code latency}
 * @param uplinkBw     the uplink bandwidth estimate, or {@code -1} when absent
 * @param downlinkBw   the downlink bandwidth estimate, or {@code -1} when absent
 * @param favored      whether the {@code is_favored} attribute marks this relay as preferred
 * @param relayName    the {@code relay_name} attribute, or {@code null} when absent
 * @param connectionId the eight-byte connection identifier, or {@code null} when the entry carries no
 *                     {@code conn_id}
 * @param enc          the {@code <enc>} child measurement blob, or {@code null} when absent
 * @see RelayLatencyStanza
 */
public record RelayLatencyEntry(int latency,
                                boolean encryptedRtt,
                                int uplinkBw,
                                int downlinkBw,
                                boolean favored,
                                String relayName,
                                byte[] connectionId,
                                byte[] enc) {
    /**
     * The wire element tag for a relay-latency entry.
     */
    public static final String ELEMENT = "te";

    /**
     * The wire attribute naming a plain round-trip latency measurement.
     */
    private static final String LATENCY_ATTRIBUTE = "latency";

    /**
     * The wire attribute naming an encrypted round-trip latency measurement.
     */
    private static final String XLATENCY_ATTRIBUTE = "xlatency";

    /**
     * The wire attribute naming the uplink bandwidth estimate.
     */
    private static final String UPLINK_BW_ATTRIBUTE = "ul_bw";

    /**
     * The wire attribute naming the downlink bandwidth estimate.
     */
    private static final String DOWNLINK_BW_ATTRIBUTE = "dl_bw";

    /**
     * The wire attribute marking the favored relay.
     */
    private static final String IS_FAVORED_ATTRIBUTE = "is_favored";

    /**
     * The wire attribute naming the short relay identifier.
     */
    private static final String RELAY_NAME_ATTRIBUTE = "relay_name";

    /**
     * The wire attribute carrying the base64 connection identifier.
     */
    private static final String CONN_ID_ATTRIBUTE = "conn_id";

    /**
     * The wire element tag for the encrypted measurement child.
     */
    private static final String ENC_ELEMENT = "enc";

    /**
     * The value of a boolean attribute that is set.
     */
    private static final String TRUE_LITERAL = "1";

    /**
     * Canonicalizes the record components, defensively copying the connection id and encrypted blob.
     */
    public RelayLatencyEntry {
        connectionId = connectionId == null ? null : connectionId.clone();
        enc = enc == null ? null : enc.clone();
    }

    /**
     * Returns the uplink bandwidth estimate, if present.
     *
     * @return an {@link OptionalInt} holding the {@code ul_bw}, or empty when absent
     */
    public OptionalInt uplinkBwValue() {
        return uplinkBw < 0 ? OptionalInt.empty() : OptionalInt.of(uplinkBw);
    }

    /**
     * Returns the downlink bandwidth estimate, if present.
     *
     * @return an {@link OptionalInt} holding the {@code dl_bw}, or empty when absent
     */
    public OptionalInt downlinkBwValue() {
        return downlinkBw < 0 ? OptionalInt.empty() : OptionalInt.of(downlinkBw);
    }

    /**
     * Returns the short relay identifier, if present.
     *
     * @return an {@link Optional} holding the {@code relay_name}, or empty when absent
     */
    public Optional<String> relayNameValue() {
        return Optional.ofNullable(relayName);
    }

    /**
     * Returns a defensive copy of the eight-byte connection identifier, if present.
     *
     * @return an {@link Optional} holding a copy of the connection id, or empty when the entry carries
     *         no {@code conn_id}
     */
    public Optional<byte[]> connectionIdValue() {
        return connectionId == null ? Optional.empty() : Optional.of(connectionId.clone());
    }

    /**
     * Returns the connection identifier backing this entry.
     *
     * <p>This accessor overrides the implicit record accessor to return a defensive copy so the stored
     * array cannot be mutated through the returned reference.
     *
     * @return a copy of the connection id, or {@code null} when the entry carries no {@code conn_id}
     */
    @Override
    public byte[] connectionId() {
        return connectionId == null ? null : connectionId.clone();
    }

    /**
     * Returns a defensive copy of the {@code <enc>} child measurement blob, if present.
     *
     * @return an {@link Optional} holding a copy of the encrypted blob, or empty when absent
     */
    public Optional<byte[]> encValue() {
        return enc == null ? Optional.empty() : Optional.of(enc.clone());
    }

    /**
     * Returns the encrypted measurement blob backing this entry.
     *
     * <p>This accessor overrides the implicit record accessor to return a defensive copy so the stored
     * array cannot be mutated through the returned reference.
     *
     * @return a copy of the {@code <enc>} bytes, or {@code null} when absent
     */
    @Override
    public byte[] enc() {
        return enc == null ? null : enc.clone();
    }

    /**
     * Builds the {@code <te>} node for this latency entry.
     *
     * <p>The latency attribute is named {@code xlatency} when {@link #encryptedRtt()} is set and
     * {@code latency} otherwise. The bandwidth estimates are omitted when absent, {@code is_favored} is
     * written only when set, and {@code conn_id} is written as the base64 of the connection id only
     * when present. The encrypted blob, when present, becomes an {@code <enc>} child.
     *
     * @return the relay-latency entry node
     */
    public Node toNode() {
        var builder = new NodeBuilder()
                .description(ELEMENT)
                .attribute(encryptedRtt ? XLATENCY_ATTRIBUTE : LATENCY_ATTRIBUTE, latency)
                .attribute(UPLINK_BW_ATTRIBUTE, uplinkBw, uplinkBw >= 0)
                .attribute(DOWNLINK_BW_ATTRIBUTE, downlinkBw, downlinkBw >= 0)
                .attribute(IS_FAVORED_ATTRIBUTE, TRUE_LITERAL, favored)
                .attribute(RELAY_NAME_ATTRIBUTE, relayName)
                .attribute(CONN_ID_ATTRIBUTE, connectionId == null ? null : Base64.getEncoder().encodeToString(connectionId));
        if (enc != null) {
            builder.content(new NodeBuilder()
                    .description(ENC_ELEMENT)
                    .content(enc)
                    .build());
        }
        return builder.build();
    }

    /**
     * Decodes a {@code <te>} node into a {@link RelayLatencyEntry}.
     *
     * <p>The latency name is resolved by probing {@code xlatency} first and then {@code latency}; the
     * presence of {@code xlatency} sets the encrypted-RTT flag. A node that is not a {@code <te>}
     * element, or one that carries neither a {@code latency} nor an {@code xlatency} attribute, yields
     * an empty result so callers iterating a mixed child list can skip it. The {@code conn_id} is
     * base64-decoded when present and parseable; an unparseable {@code conn_id} is treated as absent.
     *
     * @param node the relay-latency entry node
     * @return the decoded entry, or an empty result when the node is not a usable {@code <te>} element
     * @throws NullPointerException if {@code node} is {@code null}
     */
    public static Optional<RelayLatencyEntry> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription(ELEMENT)) {
            return Optional.empty();
        }
        var encryptedRtt = node.hasAttribute(XLATENCY_ATTRIBUTE);
        var latencyAttribute = encryptedRtt ? XLATENCY_ATTRIBUTE : LATENCY_ATTRIBUTE;
        var latency = node.getAttributeAsInt(latencyAttribute);
        if (latency.isEmpty()) {
            return Optional.empty();
        }
        var uplinkBw = node.getAttributeAsInt(UPLINK_BW_ATTRIBUTE, -1);
        var downlinkBw = node.getAttributeAsInt(DOWNLINK_BW_ATTRIBUTE, -1);
        var favored = TRUE_LITERAL.equals(node.getAttributeAsString(IS_FAVORED_ATTRIBUTE, null));
        var relayName = node.getAttributeAsString(RELAY_NAME_ATTRIBUTE, null);
        var connectionId = decodeConnectionId(node.getAttributeAsString(CONN_ID_ATTRIBUTE, null));
        var enc = node.getChild(ENC_ELEMENT)
                .flatMap(Node::toContentBytes)
                .orElse(null);
        return Optional.of(new RelayLatencyEntry(latency.getAsInt(), encryptedRtt, uplinkBw, downlinkBw,
                favored, relayName, connectionId, enc));
    }

    /**
     * Decodes the base64 {@code conn_id} attribute into the eight-byte connection identifier.
     *
     * @param encoded the base64 connection id, or {@code null}
     * @return the decoded connection id, or {@code null} when the attribute is absent or not valid
     *         base64
     */
    private static byte[] decodeConnectionId(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof RelayLatencyEntry that
                && this.latency == that.latency
                && this.encryptedRtt == that.encryptedRtt
                && this.uplinkBw == that.uplinkBw
                && this.downlinkBw == that.downlinkBw
                && this.favored == that.favored
                && Objects.equals(this.relayName, that.relayName)
                && Arrays.equals(this.connectionId, that.connectionId)
                && Arrays.equals(this.enc, that.enc));
    }

    @Override
    public int hashCode() {
        return Objects.hash(latency, encryptedRtt, uplinkBw, downlinkBw, favored, relayName,
                Arrays.hashCode(connectionId), Arrays.hashCode(enc));
    }

    @Override
    public String toString() {
        return "RelayLatencyEntry[" + (encryptedRtt ? "xlatency" : "latency") + '=' + latency
                + ", ulBw=" + uplinkBw + ", dlBw=" + downlinkBw + ", favored=" + favored
                + ", relayName=" + relayName + ']';
    }
}
