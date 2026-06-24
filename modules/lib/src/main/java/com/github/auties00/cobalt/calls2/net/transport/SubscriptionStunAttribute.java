package com.github.auties00.cobalt.calls2.net.transport;

import com.github.auties00.cobalt.model.call.datachannel.RxSubscriptions;
import com.github.auties00.cobalt.model.call.datachannel.SenderSubscriptions;

import java.util.Arrays;
import java.util.Objects;

/**
 * One subscription protobuf ready to be embedded as a proprietary STUN attribute.
 *
 * <p>The selective-forwarding unit learns each client's send layout and receive
 * wishes from protobufs carried inside STUN binding-request attributes rather than
 * from a separate control channel. A {@code SubscriptionStunAttribute} pairs the
 * proprietary attribute type with the serialized protobuf {@linkplain #value() value}
 * so the STUN message writer can append it as a type-length-value entry. The two
 * attribute types this seam emits are {@link #SENDER_SUBSCRIPTIONS_TYPE} carrying a
 * serialized {@link SenderSubscriptions} and {@link #RECEIVER_SUBSCRIPTION_TYPE}
 * carrying a serialized {@link RxSubscriptions}.
 *
 * <p>The value held here is the unpadded protobuf encoding: it is the exact byte
 * sequence whose length the STUN attribute length field reports. Proprietary STUN
 * attributes are padded to a four-byte boundary on the wire, but the padding is
 * applied uniformly by the STUN type-length-value writer across every attribute, so
 * it is deliberately not folded into this value; {@link #paddedLength()} exposes the
 * on-wire footprint for buffer sizing without mutating the value.
 *
 * @param attributeType the proprietary STUN attribute type that frames the value
 * @param value         the unpadded serialized subscription protobuf; never {@code null}
 * @implNote This implementation carries the output of {@code add_stun_attr_sender_subscriptions}
 * and {@code add_stun_attr_receiver_subscription} (both fn5182 in {@code wa_stun_msg.cc} of the
 * wa-voip WASM module {@code ff-tScznZ8P}), which serialize the {@code SenderSubscriptions} and
 * {@code RxSubscriptions} protobufs into STUN attributes {@code 0x4025} and {@code 0x4021} with
 * four-byte padding. The padding is left to the STUN writer because the native
 * {@code wa_stun_add_binary_data_attr} (fn4838) rounds every binary attribute up to a four-byte
 * boundary identically; reproducing that rounding here would double-pad. The attribute-type
 * integers are kept as constants on this record rather than referenced from a STUN attribute-type
 * enum so the subscription layer carries no compile-time dependency on the STUN codec it feeds.
 */
public record SubscriptionStunAttribute(int attributeType, byte[] value) {
    /**
     * The proprietary STUN attribute type framing a serialized {@link SenderSubscriptions}.
     *
     * <p>Identifies the attribute in which a client publishes the SSRC-to-PID layout of
     * the media it sends, so receivers and the selective-forwarding unit can map each
     * forwarded SSRC back to its sender and layer.
     */
    public static final int SENDER_SUBSCRIPTIONS_TYPE = 0x4025;

    /**
     * The proprietary STUN attribute type framing a serialized {@link RxSubscriptions}.
     *
     * <p>Identifies the attribute in which a client publishes which participants and
     * video qualities it wishes to receive, which the selective-forwarding unit honours
     * when choosing the simulcast layers to forward.
     */
    public static final int RECEIVER_SUBSCRIPTION_TYPE = 0x4021;

    /**
     * The boundary, in bytes, that a proprietary STUN attribute value is padded up to.
     *
     * <p>The on-wire attribute value is zero-padded so the next attribute starts on a
     * four-byte boundary; {@link #paddedLength()} applies this boundary to report the
     * padded footprint.
     */
    private static final int STUN_ATTRIBUTE_PADDING = 4;

    /**
     * Canonicalizes the record, rejecting a {@code null} value and copying it defensively.
     *
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public SubscriptionStunAttribute {
        Objects.requireNonNull(value, "value cannot be null");
        value = value.clone();
    }

    /**
     * Returns the unpadded serialized subscription protobuf backing this attribute.
     *
     * <p>This accessor overrides the implicit record accessor to return a defensive copy
     * so the stored array cannot be mutated through the returned reference. The returned
     * length is the value the STUN attribute length field reports.
     *
     * @return a copy of the unpadded value bytes; never {@code null}
     */
    @Override
    public byte[] value() {
        return value.clone();
    }

    /**
     * Returns the on-wire footprint of this attribute value rounded up to the STUN
     * four-byte padding boundary.
     *
     * <p>The STUN type-length-value writer reports {@link #value()}{@code .length} in the
     * length field and then pads the value with zero bytes up to this rounded length so
     * the following attribute begins aligned. The four-byte type-and-length header that
     * precedes the value is not included.
     *
     * @return the padded value length in bytes, a multiple of four
     */
    public int paddedLength() {
        return (value.length + STUN_ATTRIBUTE_PADDING - 1) & -STUN_ATTRIBUTE_PADDING;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof SubscriptionStunAttribute that
                && this.attributeType == that.attributeType
                && Arrays.equals(this.value, that.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeType, Arrays.hashCode(value));
    }

    @Override
    public String toString() {
        return "SubscriptionStunAttribute[attributeType=0x" + Integer.toHexString(attributeType)
                + ", length=" + value.length + ']';
    }
}
