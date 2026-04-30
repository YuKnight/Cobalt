package com.github.auties00.cobalt.registration.push.apns.courier;

import java.util.Map;

/**
 * Decoded APNS frame: a {@link ApnsPayloadTag} plus the
 * type-length-value fields that follow. Field ids are 1 byte.
 * values are arbitrary byte arrays (commonly UTF-8 strings, JSON,
 * certificates, or fixed-width status codes).
 *
 * @param tag    the wire tag of this packet
 * @param fields the {@code field-id -> raw bytes} map. Never
 *               {@code null}
 */
public record ApnsPacket(ApnsPayloadTag tag, Map<Integer, byte[]> fields) {
}
