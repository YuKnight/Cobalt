package com.github.auties00.cobalt.calls2.net.transport;

import com.github.auties00.cobalt.model.call.datachannel.RxSubscriptions;
import com.github.auties00.cobalt.model.call.datachannel.SenderSubscriptions;
import com.github.auties00.cobalt.model.call.datachannel.StreamDescriptors;

import java.util.Optional;

/**
 * Publishes a client's send layout and receive wishes to the selective-forwarding unit.
 *
 * <p>A group call reaches the selective-forwarding unit through the relay, and the unit
 * forwards only the streams and qualities each client asks for. A client expresses what
 * it sends and what it wants to receive by embedding serialized protobufs inside STUN
 * binding-request attributes: a {@link SenderSubscriptions} in the sender attribute and
 * an {@link RxSubscriptions} in the receiver attribute. This seam turns the call's
 * {@link StreamLayout} into the {@link StreamDescriptors} list, frames the sender and
 * receiver subscriptions as {@link SubscriptionStunAttribute} values for the STUN
 * message writer, and drives the periodic resend of the cached receive subscription so a
 * dropped binding does not strand the receiver.
 *
 * <p>The publisher also owns the hop-by-hop {@link RtcpRxSubscriptionTable} that records
 * which RTCP feedback the unit should forward, exposed through {@link #rtcpRxTable()}.
 *
 * <p>Implementations are driven by the single call transport thread and are not required
 * to be thread-safe.
 *
 * @implSpec An implementation serializes the reused model protobufs with their generated
 * specs and frames the bytes in a {@link SubscriptionStunAttribute} whose attribute type
 * is {@link SubscriptionStunAttribute#SENDER_SUBSCRIPTIONS_TYPE} for the sender
 * subscription and {@link SubscriptionStunAttribute#RECEIVER_SUBSCRIPTION_TYPE} for the
 * receiver subscription. {@link #publishRxSubscription(RxSubscriptions, long)} MUST
 * suppress a subscription identical to the last published one and MUST otherwise record
 * the new subscription and arm the resend timer, and the resend the implementation drives
 * MUST carry the most recently published subscription.
 */
public sealed interface SubscriptionPublisher permits LiveSubscriptionPublisher {
    /**
     * Builds the stream-descriptor list declaring every stream the layout publishes.
     *
     * <p>Emits one {@link com.github.auties00.cobalt.model.call.datachannel.StreamDescriptor}
     * per active stream: the audio media plus its forward-error-correction and
     * negative-acknowledgement descriptors, the same triple for each present video
     * simulcast layer, and the application-data, live-transcription, and hop-by-hop
     * forward-error-correction descriptors for whichever feature SSRCs the layout
     * allocates. Absent SSRCs yield no descriptors. The result is the
     * {@link StreamDescriptors} the SFU reads to set up forwarding.
     *
     * @param layout the SSRC and feature layout this client publishes; must not be {@code null}
     * @return the stream descriptors for the layout
     * @throws NullPointerException if {@code layout} is {@code null}
     */
    StreamDescriptors buildStreamDescriptors(StreamLayout layout);

    /**
     * Frames a sender subscription as the proprietary STUN sender-subscription attribute.
     *
     * <p>Serializes the {@link SenderSubscriptions} protobuf and wraps the bytes in a
     * {@link SubscriptionStunAttribute} of type
     * {@link SubscriptionStunAttribute#SENDER_SUBSCRIPTIONS_TYPE} so the STUN message
     * writer can append it to a binding request.
     *
     * @param senderSubscriptions the sender subscription to frame; must not be {@code null}
     * @return the STUN attribute carrying the serialized sender subscription
     * @throws NullPointerException if {@code senderSubscriptions} is {@code null}
     */
    SubscriptionStunAttribute buildSenderAttribute(SenderSubscriptions senderSubscriptions);

    /**
     * Frames a receive subscription as the proprietary STUN receiver-subscription attribute.
     *
     * <p>Serializes the {@link RxSubscriptions} protobuf and wraps the bytes in a
     * {@link SubscriptionStunAttribute} of type
     * {@link SubscriptionStunAttribute#RECEIVER_SUBSCRIPTION_TYPE}. Unlike
     * {@link #publishRxSubscription(RxSubscriptions, long)} this performs no suppression and
     * does not touch the cached state; it is the framing primitive the publish path builds on.
     *
     * @param rxSubscriptions the receive subscription to frame; must not be {@code null}
     * @return the STUN attribute carrying the serialized receive subscription
     * @throws NullPointerException if {@code rxSubscriptions} is {@code null}
     */
    SubscriptionStunAttribute buildReceiverAttribute(RxSubscriptions rxSubscriptions);

    /**
     * Publishes a receive subscription, suppressing it when it has not changed.
     *
     * <p>When the subscription is identical to the last published one this returns an
     * empty result and changes nothing, so the caller sends no binding. When it differs
     * this records it as the new cached subscription, arms or re-arms the resend timer
     * against the supplied clock, and returns the framed
     * {@link SubscriptionStunAttribute} for the caller to attach to a STUN binding
     * request.
     *
     * @param rxSubscriptions the receive subscription to publish; must not be {@code null}
     * @param nowNanos        the current time in the resend timer's nanosecond timebase
     * @return the STUN attribute to send, or an empty result when the subscription is a
     *         redundant resend
     * @throws NullPointerException if {@code rxSubscriptions} is {@code null}
     */
    Optional<SubscriptionStunAttribute> publishRxSubscription(RxSubscriptions rxSubscriptions, long nowNanos);

    /**
     * Returns the hop-by-hop RTCP-feedback subscription table for this call.
     *
     * <p>The caller registers and removes feedback subscriptions on the returned table to
     * tell the selective-forwarding unit which RTCP feedback to forward for each media
     * SSRC.
     *
     * @return the RTCP-feedback subscription table, never {@code null}
     */
    RtcpRxSubscriptionTable rtcpRxTable();

    /**
     * Releases the publisher's timer and clears its cached subscription and feedback table.
     *
     * <p>Cancels the resend timer if it is armed, clears the cached receive subscription
     * so a later transport republishes from scratch, and empties the RTCP-feedback table.
     * Idempotent: a second close is a no-op.
     */
    void close();
}
