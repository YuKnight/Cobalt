package com.github.auties00.cobalt.pairing;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * Represents the lifecycle phase of the companion-side alt-device-linking
 * handshake. The state machine advances monotonically from
 * {@link #NOT_STARTED} through {@link #INITIALIZED} and
 * {@link #AFTER_SEND_COMPANION_HELLO} to
 * {@link #AFTER_SEND_COMPANION_FINISH}, with a single permitted regression
 * from the terminal state back to {@link #AFTER_SEND_COMPANION_HELLO} when
 * a repeated {@code primary_hello} forces the ADV secret to be regenerated.
 */
@WhatsAppWebModule(moduleName = "WAWebAltDeviceLinkingApi")
enum CompanionPairingStage {
    /**
     * Indicates that no pairing flow has been started, or that the cached
     * state has been cleared after a completed or aborted handshake.
     */
    NOT_STARTED,

    /**
     * Indicates that pairing has been initialized with a phone JID and
     * generation timestamp, but the {@code companion_hello} IQ has not yet
     * been transmitted.
     */
    INITIALIZED,

    /**
     * Indicates that {@code companion_hello} has been sent and acknowledged
     * by the server. The companion is waiting for the primary device to
     * deliver its {@code primary_hello} notification.
     */
    AFTER_SEND_COMPANION_HELLO,

    /**
     * Indicates that {@code companion_finish} has been sent. The handshake
     * is complete from the companion's perspective, but a repeat
     * {@code primary_hello} can still rewind the state to
     * {@link #AFTER_SEND_COMPANION_HELLO} after regenerating the ADV secret.
     */
    AFTER_SEND_COMPANION_FINISH
}
