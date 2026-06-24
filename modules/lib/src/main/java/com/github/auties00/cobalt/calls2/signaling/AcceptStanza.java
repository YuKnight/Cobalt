package com.github.auties00.cobalt.calls2.signaling;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represents an {@code <accept>} signal: the callee answers the call.
 *
 * <p>An accept is the callee's answer to an offer. It is the heaviest non-offer action: it carries
 * the callee's capability advertisements, the offered audio codecs, the negotiated media descriptor,
 * the encryption options, the chosen network medium, the callee's key material (the {@code <enc>} key
 * blobs the participant crypto chain produces), and an optional embedded {@code <transport>} block
 * carrying the callee's transport and relay candidates. The transport block is held as a raw subtree
 * because its full grammar is owned by the transport layer; the key blobs are likewise held as raw
 * subtrees because their per-blob layout is owned by the participant-crypto layer.
 *
 * <p>On the wire the element is {@snippet lang="xml" :
 * <accept call-id="..." call-creator="...">
 *   <capability ver="1">MASK</capability>
 *   <audio enc="opus" rate="16000"/>
 *   <net medium="2"/>
 *   <enc>KEY_BLOB</enc>
 *   <media enc="N" rate="16000"/>
 *   <encopt keygen="2"/>
 *   <transport .../>
 * </accept>
 * }
 *
 * @implNote This implementation models the {@code <accept>} element (data offset {@code 0x1a352})
 * built by {@code make_and_send_accept} (fn11450) and {@code serialize_accept}/{@code deserialize_accept}
 * in the wa-voip WASM module {@code ff-tScznZ8P}: the callee audio capabilities (msg offset
 * {@code 0x64}), the voip capability version (fn11774), local candidates (fn11464), the data-channel
 * cert-fingerprint HMAC and e2e public-key blobs, the video codec capability (fn11451), and the relay
 * candidate (fn11468), over the common header stamped by {@code populate_common_call_attr} (fn11591).
 * The {@code <enc>} key blobs and the embedded {@code <transport>} block are retained as raw subtrees
 * because their layouts are owned by the participant-crypto and transport layers respectively. The
 * accept also receives an ack or nack like the offer; that ack is parsed by the ack layer, not by this
 * record.
 *
 * @param callId        the call identifier; never {@code null}
 * @param callCreator   the call creator's device JID; never {@code null}
 * @param netMedium     the chosen network-medium classification, or {@code -1} when absent
 * @param capabilities  the callee's capability advertisements; never {@code null}, possibly empty
 * @param audioCodecs   the offered audio codec descriptors; never {@code null}, possibly empty
 * @param encKeys       the raw {@code <enc>} key-blob subtrees; never {@code null}, possibly empty
 * @param media         the negotiated media descriptor, or {@code null} when absent
 * @param encOptions    the encryption options, or {@code null} when absent
 * @param transport     the embedded {@code <transport>} subtree, or {@code null} when absent
 * @see Calls2SignalingType#ACCEPT
 */
public record AcceptStanza(String callId, Jid callCreator, int netMedium, List<CallCapability> capabilities,
                           List<CallCodecDescriptor> audioCodecs, List<Node> encKeys, CallMediaDescriptor media,
                           CallEncOptions encOptions, Node transport) implements CallMessage {
    /**
     * The wire element tag for an accept signal.
     */
    public static final String ELEMENT = "accept";

    /**
     * The wire element tag for the chosen network-medium block.
     */
    private static final String NET_ELEMENT = "net";

    /**
     * The wire attribute naming the network-medium classification on the {@code <net>} block.
     */
    private static final String MEDIUM_ATTRIBUTE = "medium";

    /**
     * The wire element tag for an audio-format advertisement.
     */
    private static final String AUDIO_ELEMENT = CallCodecDescriptor.AUDIO_ELEMENT;

    /**
     * The wire element tag for a callee key-material blob.
     */
    private static final String ENC_ELEMENT = "enc";

    /**
     * The wire element tag for the embedded transport block.
     */
    private static final String TRANSPORT_ELEMENT = "transport";

    /**
     * Canonicalizes the record components, copying the capability, codec, and key lists.
     *
     * @throws NullPointerException if {@code callId}, {@code callCreator}, {@code capabilities},
     *                              {@code audioCodecs}, or {@code encKeys} is {@code null}, or if any
     *                              of those lists contains a {@code null} element
     */
    public AcceptStanza {
        Objects.requireNonNull(callId, "callId cannot be null");
        Objects.requireNonNull(callCreator, "callCreator cannot be null");
        Objects.requireNonNull(capabilities, "capabilities cannot be null");
        Objects.requireNonNull(audioCodecs, "audioCodecs cannot be null");
        Objects.requireNonNull(encKeys, "encKeys cannot be null");
        capabilities = List.copyOf(capabilities);
        audioCodecs = List.copyOf(audioCodecs);
        encKeys = List.copyOf(encKeys);
    }

    /**
     * Returns the chosen network-medium classification, if present.
     *
     * @return an {@link OptionalInt} holding the network medium, or empty when absent
     */
    public OptionalInt netMediumValue() {
        return netMedium < 0 ? OptionalInt.empty() : OptionalInt.of(netMedium);
    }

    /**
     * Returns the negotiated media descriptor, if present.
     *
     * @return an {@link Optional} holding the media descriptor, or empty when absent
     */
    public Optional<CallMediaDescriptor> mediaDescriptor() {
        return Optional.ofNullable(media);
    }

    /**
     * Returns the encryption options, if present.
     *
     * @return an {@link Optional} holding the encryption options, or empty when absent
     */
    public Optional<CallEncOptions> encOptionsValue() {
        return Optional.ofNullable(encOptions);
    }

    /**
     * Returns the embedded {@code <transport>} subtree, if present.
     *
     * @return an {@link Optional} holding the transport node, or empty when absent
     */
    public Optional<Node> transportNode() {
        return Optional.ofNullable(transport);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Calls2SignalingType#ACCEPT}
     */
    @Override
    public Calls2SignalingType type() {
        return Calls2SignalingType.ACCEPT;
    }

    /**
     * Builds the {@code <accept>} action node with its capability, audio, key, media, encryption, and
     * transport children.
     *
     * <p>Children are emitted in the order capabilities, audio formats, network medium, key blobs,
     * media, encryption options, transport; each audio format is a flat {@code <audio>} element and
     * the network medium a {@code <net medium>} element emitted only when present, and absent optional
     * children are omitted.
     *
     * @return the accept action node
     */
    @Override
    public Node toNode() {
        var children = new ArrayList<Node>();
        for (var capability : capabilities) {
            children.add(capability.toNode());
        }
        for (var codec : audioCodecs) {
            children.add(codec.toNode());
        }
        if (netMedium >= 0) {
            children.add(new NodeBuilder()
                    .description(NET_ELEMENT)
                    .attribute(MEDIUM_ATTRIBUTE, netMedium)
                    .build());
        }
        children.addAll(encKeys);
        if (media != null) {
            children.add(media.toNode());
        }
        if (encOptions != null) {
            children.add(encOptions.toNode());
        }
        if (transport != null) {
            children.add(transport);
        }
        var builder = CallMessages.stampHeader(new NodeBuilder().description(ELEMENT), callId, callCreator);
        if (!children.isEmpty()) {
            builder.content(children);
        }
        return builder.build();
    }

    /**
     * Decodes an {@code <accept>} action node into an {@link AcceptStanza}.
     *
     * <p>Capability children, the flat {@code <audio>} format children, the {@code <net medium>}
     * child, the {@code <enc>} key blobs, the {@code <media>} descriptor, the {@code <encopt>}
     * options, and the embedded {@code <transport>} subtree are each decoded when present; the key
     * blobs and transport subtree are retained verbatim.
     *
     * @param node the {@code <accept>} node
     * @return the decoded accept signal
     * @throws NullPointerException   if {@code node} is {@code null}
     * @throws NoSuchElementException if the required {@code call-id} or {@code call-creator} attribute
     *                                is absent
     */
    public static AcceptStanza of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        var callId = node.getRequiredAttributeAsString(CallMessages.CALL_ID_ATTRIBUTE);
        var callCreator = node.getRequiredAttributeAsJid(CallMessages.CALL_CREATOR_ATTRIBUTE);
        var netMedium = node.getChild(NET_ELEMENT)
                .map(net -> net.getAttributeAsInt(MEDIUM_ATTRIBUTE, -1))
                .orElse(-1);
        var capabilities = node.streamChildren(CallCapability.ELEMENT)
                .flatMap(child -> CallCapability.of(child).stream())
                .toList();
        var audioCodecs = node.streamChildren(AUDIO_ELEMENT)
                .flatMap(audio -> CallCodecDescriptor.of(audio).stream())
                .toList();
        var encKeys = node.getChildren(ENC_ELEMENT)
                .stream()
                .toList();
        var media = node.getChild(CallMediaDescriptor.ELEMENT).flatMap(CallMediaDescriptor::of).orElse(null);
        var encOptions = node.getChild(CallEncOptions.ELEMENT).flatMap(CallEncOptions::of).orElse(null);
        var transport = node.getChild(TRANSPORT_ELEMENT).orElse(null);
        return new AcceptStanza(callId, callCreator, netMedium, capabilities, audioCodecs, encKeys, media,
                encOptions, transport);
    }
}
