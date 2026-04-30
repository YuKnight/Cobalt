package com.github.auties00.cobalt.client;

import it.auties.protobuf.annotation.ProtobufEnum;

/**
 * Enumerates the supported WhatsApp client flavours.
 *
 * <p>Cobalt can operate in two distinct modes that correspond to different
 * WhatsApp product surfaces: a web companion that mirrors WhatsApp Web and
 * Desktop, and a primary mobile client that directly registers a phone
 * number. The selected flavour affects registration, handshake payloads,
 * device capabilities, history sync behaviour and the set of listener
 * callbacks that fire.
 *
 * <p>This value is persisted in the store so that a previously registered
 * session can be loaded back with the correct client type and is also
 * emitted over the wire as part of the handshake.
 *
 * @see WhatsAppClientBuilder
 * @see WhatsAppDevice
 */
@ProtobufEnum
public enum WhatsAppClientType {
    /**
     * A web companion client that attaches to a primary mobile device via
     * QR code or pairing code, mirroring WhatsApp Web and Desktop.
     *
     * <p>In this mode the client does not own the phone number: it
     * exchanges identity with an existing primary account and operates as
     * a linked device. The companion-linking ceremony is driven by
     * {@link WhatsAppClientVerificationHandler.Web}.
     */
    WEB,

    /**
     * A primary mobile client that registers a phone number directly with
     * the WhatsApp servers.
     *
     * <p>In this mode the client owns the phone number, so it must go through
     * the full mobile registration flow (SMS/voice/WhatsApp verification code
     * delivery) and will receive registration-only events such as
     * {@link WhatsAppClientListener#onRegistrationCode(WhatsAppClient, long)}.
     */
    MOBILE
}
