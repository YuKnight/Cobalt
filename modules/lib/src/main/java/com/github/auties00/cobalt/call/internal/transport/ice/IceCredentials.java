package com.github.auties00.cobalt.call.internal.transport.ice;

import java.util.Base64;
import java.util.Objects;

/**
 * Holds the local and remote ufrag/password pairs an {@link IceAgent} uses to authenticate STUN
 * binding requests per RFC 8445 section 7.2.2.
 *
 * <p>RFC 8445 keys the USERNAME of an outbound binding request as {@code remoteUfrag:localUfrag}
 * and stamps MESSAGE-INTEGRITY with the peer's password. For WhatsApp's relay-mediated path the
 * {@code localPassword} carried here is the {@code relay_key} delivered by {@code RelayListUpdate}
 * rather than an SDP-negotiated password; the keying derivation is documented on
 * {@link com.github.auties00.cobalt.call.internal.transport.relay.WaRelayMessageIntegrity}.
 *
 * @param localUfrag     the local user fragment (a random 4 to 256 byte token per RFC 8445
 *                       section 5.3); the peer's USERNAME suffixes this when sending to the local
 *                       endpoint
 * @param localPassword  the local password, used to verify MESSAGE-INTEGRITY on inbound binding
 *                       requests
 * @param remoteUfrag    the remote user fragment, learned from the peer's offer or answer
 * @param remotePassword the remote password, used to compute MESSAGE-INTEGRITY on outbound binding
 *                       requests
 */
public record IceCredentials(
        String localUfrag,
        byte[] localPassword,
        String remoteUfrag,
        byte[] remotePassword
) {
    /**
     * Validates the components, rejecting {@code null} fields and empty ufrags.
     *
     * @throws NullPointerException     if any component is {@code null}
     * @throws IllegalArgumentException if {@code localUfrag} or {@code remoteUfrag} is empty
     */
    public IceCredentials {
        Objects.requireNonNull(localUfrag, "localUfrag cannot be null");
        Objects.requireNonNull(localPassword, "localPassword cannot be null");
        Objects.requireNonNull(remoteUfrag, "remoteUfrag cannot be null");
        Objects.requireNonNull(remotePassword, "remotePassword cannot be null");
        if (localUfrag.isEmpty()) {
            throw new IllegalArgumentException("localUfrag cannot be empty");
        }
        if (remoteUfrag.isEmpty()) {
            throw new IllegalArgumentException("remoteUfrag cannot be empty");
        }
    }

    /**
     * Builds an {@code IceCredentials} for a WhatsApp call from one relay's auth token and the
     * per-call relay key.
     *
     * <p>WA's transport model has no peer-to-peer ICE: both endpoints authenticate to the relay
     * using the same per-call credentials, so local and remote ufrag/password are identical. Per
     * {@code WAWebVoipRelayConnectionUtils.replaceIceCredentials}:
     * <ul>
     *   <li>{@code ice-ufrag} = base64-encoded {@code auth_token} bytes (carried as text on the wire).</li>
     *   <li>{@code ice-pwd} = the {@code relay_key} bytes.</li>
     * </ul>
     *
     * <p>The {@code auth_token} comes from the relay-tokens block's {@code <auth_token>} child
     * (one per relay). The {@code relay_key} comes from the {@code <key>} child and decodes to
     * 16 bytes after stripping the wire's outer base64-string quotes.
     *
     * @param authTokenBytes the raw auth-token bytes for the chosen relay candidate; must not be
     *                       {@code null} or empty
     * @param relayKeyBytes  the per-call relay key bytes (16 bytes on the captured corpus); must not
     *                       be {@code null}
     * @return the {@code IceCredentials} for the relay handshake
     * @throws NullPointerException     if either argument is {@code null}
     * @throws IllegalArgumentException if {@code authTokenBytes} is empty
     */
    public static IceCredentials fromRelay(byte[] authTokenBytes, byte[] relayKeyBytes) {
        Objects.requireNonNull(authTokenBytes, "authTokenBytes cannot be null");
        Objects.requireNonNull(relayKeyBytes, "relayKeyBytes cannot be null");
        if (authTokenBytes.length == 0) {
            throw new IllegalArgumentException("authTokenBytes cannot be empty");
        }
        var ufrag = Base64.getEncoder().withoutPadding().encodeToString(authTokenBytes);
        var pwd = relayKeyBytes.clone();
        return new IceCredentials(ufrag, pwd, ufrag, pwd.clone());
    }

    /**
     * Returns the USERNAME attribute value for an outbound binding request per RFC 8445
     * section 7.2.2.
     *
     * <p>The value is {@code remoteUfrag} and {@code localUfrag} joined by a {@code ':'}, so that
     * the peer can match the request against its own local ufrag.
     *
     * @return the USERNAME string of the form {@code remoteUfrag:localUfrag}
     */
    public String outboundUsername() {
        return remoteUfrag + ":" + localUfrag;
    }
}
