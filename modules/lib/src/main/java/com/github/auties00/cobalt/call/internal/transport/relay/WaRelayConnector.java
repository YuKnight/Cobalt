package com.github.auties00.cobalt.call.internal.transport.relay;

import com.github.auties00.cobalt.ack.AuthToken;
import com.github.auties00.cobalt.ack.CallRelay;
import com.github.auties00.cobalt.ack.RelayEndpoint;
import com.github.auties00.cobalt.ack.RelayToken;
import com.github.auties00.cobalt.call.internal.transport.ice.UdpDatagramTransport;
import com.github.auties00.cobalt.exception.WhatsAppCallException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Drives the WhatsApp relay Allocate handshake against a single {@code te2} endpoint and returns a
 * connected {@link UdpDatagramTransport} on success.
 *
 * <p>Each {@link #connect(CallRelay, int)} call resolves the chosen endpoint, builds an
 * {@link WaRelayMessageType#ALLOCATE_REQUEST} via {@link WaRelayAllocateRequestBuilder} keyed on the
 * offer's {@link CallRelay#callKey() call key}, and sends it over a fresh UDP socket. The
 * Allocate Success Response is matched on transaction id, integrity-checked via
 * {@link WaRelayMessageIntegrity}, and mined for its {@link WaRelayAttributeType#XOR_RELAYED_ADDRESS}
 * attribute. The request is retried up to {@link #MAX_ATTEMPTS} times at the
 * {@link #RECV_TIMEOUT_MILLIS}-millisecond retransmit timeout, and on success the result is an
 * {@link UdpDatagramTransport} ready for the next layer's traffic.
 *
 * @implNote This implementation feeds the offer's call key directly to the HMAC stamp: the
 * {@code <key>} content in WhatsApp's offer is the base64 text of the relay key, and
 * {@link WaRelayMessageIntegrity} keys the HMAC-SHA1 on those ASCII bytes (padding included) rather
 * than the base64-decoded binary form.
 */
public final class WaRelayConnector {
    /**
     * Holds the STUN transaction-id length in bytes (RFC 5389 section 6).
     */
    private static final int TRANSACTION_ID_LENGTH = 12;

    /**
     * Holds the per-attempt receive timeout in milliseconds.
     *
     * @implNote This implementation uses 200 milliseconds to match the first-retransmit timeout that
     * WhatsApp's wasm engine empirically applies.
     */
    private static final int RECV_TIMEOUT_MILLIS = 200;

    /**
     * Holds the total number of Allocate attempts before the handshake gives up.
     */
    private static final int MAX_ATTEMPTS = 3;

    /**
     * Interval at which the keepalive re-sends a fresh Allocate to the chosen relay for the call's
     * lifetime.
     *
     * @implNote This implementation uses 2000 ms, matching the cadence captured from the native
     * desktop client (Frida ws2_32), which re-Allocates the bridged relay roughly every two seconds so
     * the relay does not lapse the allocation and stop forwarding the participant's media.
     */
    private static final long RELAY_KEEPALIVE_MILLIS = 2000;

    /**
     * Provides random transaction ids, fresh per attempt.
     */
    private final SecureRandom random = new SecureRandom();

    /**
     * Carries the outcome of a successful Allocate handshake.
     *
     * @param transport      the live {@link UdpDatagramTransport} (a raw UDP socket connected to the
     *                       relay) over which the Allocate succeeded; it stays open for the call's
     *                       STUN, DTLS, and SRTP traffic and must be closed by the caller
     * @param driver         {@code null} on the raw-UDP path; non-{@code null} only if a future caller
     *                       reintroduces the browser-style DataChannel relay tunnel. Consumers
     *                       ({@code ActiveCallTransport}) treat a {@code null} driver as a signal to
     *                       use {@link #transport()} directly
     * @param relayedAddress the {@code XOR-RELAYED-ADDRESS} the server allocated for this client
     * @param transactionId  the 12-byte transaction id used by the request, useful for keepalive
     *                       correlation
     */
    public record Allocation(
            UdpDatagramTransport transport,
            RelayChannelDriver driver,
            InetSocketAddress relayedAddress,
            byte[] transactionId
    ) {
    }

    /**
     * Performs the Allocate handshake against the {@code te2} endpoint at the given index of the
     * spec's endpoint list.
     *
     * <p>Resolves the relay and auth tokens the endpoint references, resolves the endpoint's domain
     * name to an address, builds a single-entry IPv4 {@link WaRelayCallInfo}, and retransmits the
     * request up to {@link #MAX_ATTEMPTS} times, parsing the first valid Allocate Success Response.
     *
     * @param spec     the parsed offer transport spec supplying tokens, auth tokens, key, and
     *                 {@code te2} endpoints
     * @param te2Index the index into the spec's endpoint list of the endpoint to try
     * @return the allocation result
     * @throws NullPointerException      if {@code spec} is {@code null}
     * @throws WhatsAppCallException.Ice if DNS resolution fails, the handshake times out after
     *                                   {@link #MAX_ATTEMPTS} attempts, or the response is malformed,
     *                                   mismatched, or integrity-check rejected
     * @throws IllegalArgumentException  if {@code te2Index} is out of range or the spec is missing a
     *                                   token or auth token the endpoint references
     */
    public Allocation connect(CallRelay spec, int te2Index) {
        return connect(spec, te2Index, null);
    }

    /**
     * Performs the Allocate handshake using the token and call info of the {@code te2} endpoint at the
     * given index, but sends to {@code overrideRemote} when non-{@code null} instead of the te2's own
     * decoded address.
     *
     * <p>The relaylatency-advertised endpoints carry no token of their own, so they are allocated
     * against using a {@code te2} entry's token paired with the relaylatency transport address.
     *
     * @param spec          the parsed offer transport spec
     * @param te2Index      the index of the te2 entry supplying the token and call info
     * @param overrideRemote the relay address to send to, or {@code null} to use the te2's own address
     * @return the allocation result
     */
    public Allocation connect(CallRelay spec, int te2Index, InetSocketAddress overrideRemote) {
        Objects.requireNonNull(spec, "spec cannot be null");
        var te2 = List.copyOf(spec.endpoints()).get(te2Index);
        var relayToken = findToken(List.copyOf(spec.tokens()), te2.tokenId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "te2[" + te2Index + "] references token_id=" + te2.tokenId()
                                + " not present in spec"));
        @SuppressWarnings("unused")
        var authToken = findAuthToken(List.copyOf(spec.authTokens()), te2.authTokenId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "te2[" + te2Index + "] references auth_token_id=" + te2.authTokenId()
                                + " not present in spec"));
        var callKey = spec.callKey().orElseThrow(() -> new IllegalStateException(
                "spec.callKey() must be present for the HMAC stamp"));

        // The <te2> element's content bytes ARE the relay address: 4 bytes IPv4 + 2 bytes port
        // (big-endian), or 16 bytes IPv6 + 2 bytes port. The auth-token + relay-key are bound to
        // the specific relay endpoint the server picked, so DNS-resolving the domain name to a
        // different IP routes the Allocate request to the wrong relay (and times out). Falls back
        // to DNS only when the content is missing or malformed.
        var remote = overrideRemote != null ? overrideRemote : decodeTe2Endpoint(te2);
        var address = remote.getAddress();
        var relayPort = remote.getPort();

        // WA_CALL_INFO must enumerate the FULL (ipVersion x relay) candidate matrix, matching the
        // native Desktop client; a single entry makes the relay silently drop the Allocate. The
        // captured working call-info is, for each ipVersion in {any, IPv4, IPv6}, an aggregate entry
        // (no relay) followed by one entry per distinct relay, each carrying a descending ICE-style
        // priority hint. The priority values are the client's preference and are not validated by the
        // relay, so a descending sequence suffices.
        var distinctRelayIds = new java.util.LinkedHashSet<Integer>();
        for (var ep : spec.endpoints()) {
            distinctRelayIds.add(ep.relayId());
        }
        System.getLogger(WaRelayConnector.class.getName()).log(System.Logger.Level.INFO,
                "relay block distinct relay_ids=" + distinctRelayIds + " endpointCount=" + spec.endpoints().size());
        var entries = new java.util.ArrayList<WaRelayCallInfoEntry>();
        var priority = 4272282031L;
        for (Integer ipVersion : new Integer[]{null, 1, 2}) {
            entries.add(new WaRelayCallInfoEntryBuilder()
                    .ipVersion(ipVersion).relayId(null).priority(priority).build());
            priority -= 100_000_000L;
            for (var relayId : distinctRelayIds) {
                if (relayId == 0) {
                    continue; // 0 collapses to the aggregate (null) entry already emitted
                }
                entries.add(new WaRelayCallInfoEntryBuilder()
                        .ipVersion(ipVersion).relayId(relayId).priority(priority).build());
                priority -= 100_000_000L;
            }
        }
        var callInfo = new WaRelayCallInfoBuilder()
                .entries(entries)
                .build();

        // Edgeray media edges (domain_name like "edgeray-mxp1-1.wt.whatsapp.com") do NOT answer a raw
        // UDP Allocate on their te2 address: that UDP port expects a DTLS ClientHello and silently drops
        // STUN. So when targeting the te2 endpoint itself (no relaylatency override), tunnel the Allocate
        // through the pre-negotiated DataChannel that RelayChannelDriver stands up (DTLS -> SCTP ->
        // DataChannel). But the SAME relay also publishes a plain-TURN access point as the
        // <relaylatency><te> endpoint (a different IP of the same relay_name), which DOES answer raw-UDP
        // STUN and bridges to the peer by call-id; when connectAny passes that endpoint as overrideRemote
        // we take the raw-UDP path below against it, which is the path that produced audible media.
        if (overrideRemote == null && te2.domainName() != null && te2.domainName().contains("edgeray")) {
            return connectViaDataChannel(te2, relayToken, callInfo, address, relayPort, callKey, remote);
        }

        // Send the Allocate over a raw UDP socket connected to the te2 endpoint. Live ws2_32 captures
        // of the native Desktop client (.temp/ice_capture.jsonl) show the relay speaks raw STUN/TURN
        // on :3478, NOT a DTLS+SCTP+DataChannel tunnel (that is the browser Web client, which cannot
        // open raw UDP sockets). The earlier raw-UDP timeout that motivated the DataChannel detour was
        // a different defect (DNS-resolved relay IP and a base64-decoded key), both since fixed:
        // decodeTe2Endpoint uses the te2 bytes verbatim and the call key is the raw ASCII <key>.
        var transport = new UdpDatagramTransport(remote);
        var inbound = new java.util.concurrent.LinkedBlockingQueue<byte[]>();
        transport.setInboundListener(inbound::offer);
        boolean ownTransport = true;
        try {
            for (var attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                var transactionId = new byte[TRANSACTION_ID_LENGTH];
                random.nextBytes(transactionId);
                var request = WaRelayAllocateRequestBuilder.build(
                        transactionId, relayToken.bytes(), callInfo,
                        address, relayPort, callKey);
                System.getLogger(WaRelayConnector.class.getName()).log(System.Logger.Level.INFO,
                        "ALLOCATE-REQ to " + remote + " len=" + request.length
                                + " keyLen=" + callKey.length
                                + " tokenLen=" + relayToken.bytes().length
                                + " hex=" + java.util.HexFormat.of().formatHex(request));
                transport.send(request);

                byte[] responseBytes;
                try {
                    responseBytes = inbound.poll(RECV_TIMEOUT_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new WhatsAppCallException.Ice(
                            "Interrupted while awaiting Allocate response from " + te2.domainName(), e);
                }
                if (responseBytes == null) {
                    if (attempt == MAX_ATTEMPTS) {
                        throw new WhatsAppCallException.Ice(
                                "allocate response timed out after "
                                        + MAX_ATTEMPTS + " attempts against "
                                        + te2.domainName() + " (" + remote + ")");
                    }
                    continue;
                }
                var allocation = parseAllocateResponse(
                        responseBytes, transactionId, callKey, remote);
                // Hand the live socket to the upper layer: deregister the Allocate-phase listener so the
                // media/STUN demux can install its own, and leave the transport open for the call.
                transport.setInboundListener(null);
                ownTransport = false;
                // Keepalive: the native client re-sends a fresh Allocate (new transaction id) to the
                // chosen relay roughly every 2s for the call's lifetime. Without it the relay lets the
                // allocation lapse and stops bridging this participant's SRTP to the SFU (the server
                // never marks the participant connected). Fire-and-forget on the live socket; the loop
                // ends when the transport is closed at call teardown (send throws).
                var kaToken = relayToken.bytes();
                final var kaAddr = address;
                final var kaPort = relayPort;
                Thread.ofVirtual().name("relay-keepalive").start(() -> {
                    try {
                        while (true) {
                            Thread.sleep(RELAY_KEEPALIVE_MILLIS);
                            var kaTxid = new byte[TRANSACTION_ID_LENGTH];
                            random.nextBytes(kaTxid);
                            transport.send(WaRelayAllocateRequestBuilder.build(
                                    kaTxid, kaToken, callInfo, kaAddr, kaPort, callKey));
                        }
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                    } catch (RuntimeException _) {
                        // transport closed at call teardown; stop keepalive
                    }
                });
                return new Allocation(transport, null, allocation, transactionId);
            }
            throw new WhatsAppCallException.Ice("allocate fell through retry loop");
        } finally {
            if (ownTransport) {
                transport.close();
            }
        }
    }

    /**
     * Performs the Allocate handshake against an edgeray media edge by tunnelling it through the
     * pre-negotiated DataChannel that {@link RelayChannelDriver} stands up (DTLS to the relay, then
     * SCTP, then the {@code negotiated=true, id=0} DataChannel).
     *
     * <p>Edgeray edges expect a DTLS ClientHello on their UDP port and drop raw STUN, so the Allocate
     * Request (and subsequently all SRTP) is written as DataChannel application bytes via
     * {@link RelayChannelDriver#sendBinary(byte[])}; the Allocate Success Response arrives as an
     * inbound binary frame. The returned {@link Allocation} carries the live driver (not a raw UDP
     * socket), which {@code ActiveCallTransport} detects to route media through
     * {@code RelayDatagramTransport} over the same DataChannel. A keepalive re-sends a fresh Allocate
     * every {@link #RELAY_KEEPALIVE_MILLIS} over the channel for the call's lifetime.
     *
     * @param te2        the edgeray endpoint being allocated
     * @param relayToken the relay token referenced by {@code te2}
     * @param callInfo   the {@code WA-CALL-INFO} payload
     * @param address    the relay address bytes are decoded to
     * @param relayPort  the relay port
     * @param callKey    the HMAC key for the integrity stamp
     * @param remote     the decoded relay socket address
     * @return the allocation carrying the live {@link RelayChannelDriver}
     * @throws WhatsAppCallException.Ice if the channel bring-up or the Allocate over it fails
     */
    private Allocation connectViaDataChannel(com.github.auties00.cobalt.ack.RelayEndpoint te2,
                                             com.github.auties00.cobalt.ack.RelayToken relayToken,
                                             WaRelayCallInfo callInfo,
                                             InetAddress address,
                                             int relayPort,
                                             byte[] callKey,
                                             java.net.InetSocketAddress remote) {
        var logger = System.getLogger(WaRelayConnector.class.getName());
        var driver = new RelayChannelDriver(remote);
        driver.connect();
        var ok = false;
        try {
            for (var attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                var txid = new byte[TRANSACTION_ID_LENGTH];
                random.nextBytes(txid);
                var request = WaRelayAllocateRequestBuilder.build(
                        txid, relayToken.bytes(), callInfo, address, relayPort, callKey);
                logger.log(System.Logger.Level.INFO,
                        "ALLOCATE-REQ (datachannel) to " + remote + " len=" + request.length);
                driver.sendBinary(request);
                byte[] responseBytes;
                try {
                    responseBytes = driver.awaitBinary(RECV_TIMEOUT_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new WhatsAppCallException.Ice(
                            "Interrupted awaiting Allocate over DataChannel from " + remote, e);
                }
                if (responseBytes == null) {
                    if (attempt == MAX_ATTEMPTS) {
                        throw new WhatsAppCallException.Ice(
                                "allocate-over-datachannel timed out after " + MAX_ATTEMPTS
                                        + " attempts against " + te2.domainName() + " (" + remote + ")");
                    }
                    continue;
                }
                var relayedAddress = parseAllocateResponse(responseBytes, txid, callKey, remote);
                var kaToken = relayToken.bytes();
                final var kaAddr = address;
                final var kaPort = relayPort;
                Thread.ofVirtual().name("relay-dc-keepalive").start(() -> {
                    try {
                        while (true) {
                            Thread.sleep(RELAY_KEEPALIVE_MILLIS);
                            var kaTxid = new byte[TRANSACTION_ID_LENGTH];
                            random.nextBytes(kaTxid);
                            driver.sendBinary(WaRelayAllocateRequestBuilder.build(
                                    kaTxid, kaToken, callInfo, kaAddr, kaPort, callKey));
                        }
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                    } catch (RuntimeException _) {
                        // channel closed at call teardown; stop keepalive
                    }
                });
                ok = true;
                return new Allocation(null, driver, relayedAddress, txid);
            }
            throw new WhatsAppCallException.Ice("allocate-over-datachannel fell through retry loop");
        } finally {
            if (!ok) {
                driver.close();
            }
        }
    }

    /**
     * Attempts the Allocate handshake against every {@code te2} endpoint in turn and returns the first
     * that succeeds.
     *
     * <p>The relay-tokens block lists several endpoints (in the captured corpus a mix of edgeray media
     * edges and plain TURN relays); only the TURN relays answer a raw-UDP Allocate, while the edgeray
     * edges expect a DTLS handshake first and silently drop the Allocate. Rather than hardcode which
     * endpoint is which, this tries each one and returns the first {@link Allocation} that comes back,
     * logging the endpoint and outcome of each attempt. A single endpoint's failure (timeout, malformed
     * response, integrity rejection) is caught and the next endpoint is tried.
     *
     * @param spec the parsed offer transport spec
     * @return the first successful allocation
     * @throws NullPointerException      if {@code spec} is {@code null}
     * @throws WhatsAppCallException.Ice if no endpoint produced a successful Allocate
     */
    public Allocation connectAny(CallRelay spec) {
        return connectAny(spec, List.of());
    }

    /**
     * Attempts the Allocate against every {@code te2} endpoint and, if all fail, against each
     * relaylatency-advertised endpoint paired with each {@code te2} token.
     *
     * <p>The captured corpus shows a successful client allocates to the endpoints advertised in the
     * {@code <relaylatency><te>} stanzas rather than the offer-ACK {@code <te2>} endpoints; since those
     * relaylatency endpoints carry no token, each is tried with every {@code te2} token until one
     * Allocate succeeds.
     *
     * @param spec               the parsed offer transport spec
     * @param relaylatencyEndpoints the relay addresses advertised in {@code <relaylatency><te>} stanzas
     * @return the first successful allocation
     * @throws WhatsAppCallException.Ice if no endpoint produced a successful Allocate
     */
    public Allocation connectAny(CallRelay spec, List<InetSocketAddress> relaylatencyEndpoints) {
        Objects.requireNonNull(spec, "spec cannot be null");
        var logger = System.getLogger(WaRelayConnector.class.getName());
        var count = spec.endpoints().size();
        if (count == 0) {
            throw new WhatsAppCallException.Ice("relay-tokens block carried no te2 endpoints");
        }
        WhatsAppCallException.Ice last = null;
        // The relaylatency-advertised endpoints are the ones a successful client allocates to, so they
        // are tried FIRST; the offer-ACK te2 endpoints (which time out) are only a fallback. Trying te2
        // first wasted seconds of timeouts and the peer terminated the call before media came up.
        for (var endpoint : relaylatencyEndpoints) {
            for (var i = 0; i < count; i++) {
                try {
                    logger.log(System.Logger.Level.INFO,
                            "Allocate attempt against relaylatency endpoint " + endpoint + " with te2[" + i + "] token");
                    return connect(spec, i, endpoint);
                } catch (WhatsAppCallException.Ice e) {
                    logger.log(System.Logger.Level.INFO,
                            "relaylatency " + endpoint + " / te2[" + i + "] failed: " + e.getMessage());
                    last = e;
                } catch (IllegalArgumentException _) {
                }
            }
        }
        for (var i = 0; i < count; i++) {
            try {
                logger.log(System.Logger.Level.INFO, "Allocate attempt against te2[" + i + "]");
                return connect(spec, i);
            } catch (WhatsAppCallException.Ice e) {
                logger.log(System.Logger.Level.INFO,
                        "te2[" + i + "] Allocate failed: " + e.getMessage());
                last = e;
            } catch (IllegalArgumentException e) {
                logger.log(System.Logger.Level.INFO,
                        "te2[" + i + "] skipped: " + e.getMessage());
            }
        }
        throw last != null
                ? last
                : new WhatsAppCallException.Ice("no endpoint produced a successful Allocate");
    }

    /**
     * Parses an Allocate Success Response, verifies its integrity, and extracts the relayed address.
     *
     * <p>Decodes the packet, asserts the message type is {@link WaRelayMessageType#ALLOCATE_SUCCESS}
     * and the transaction id matches the request, verifies the {@code MESSAGE-INTEGRITY} attribute via
     * {@link WaRelayMessageIntegrity#verify(byte[], byte[])}, and decodes the
     * {@link WaRelayAttributeType#XOR_RELAYED_ADDRESS} attribute into an {@link InetSocketAddress}.
     *
     * @param responseBytes the response packet bytes
     * @param transactionId the request's transaction id, used to confirm the response is a match
     * @param relayKey      the HMAC-SHA1 key
     * @param relayRemote   the {@code te2} endpoint that was sent to, used in error messages
     * @return the {@code XOR-RELAYED-ADDRESS} attribute decoded as an {@link InetSocketAddress}
     * @throws WhatsAppCallException.Ice if the message type is unexpected, the transaction id does not
     *                                   match, the MAC verification fails, or no
     *                                   {@code XOR-RELAYED-ADDRESS} attribute is present
     */
    private static InetSocketAddress parseAllocateResponse(
            byte[] responseBytes, byte[] transactionId, byte[] relayKey,
            InetSocketAddress relayRemote) {
        var packet = WaRelayPacket.decode(responseBytes);
        System.Logger logger = System.getLogger(WaRelayConnector.class.getName());
        var attrSummary = new StringBuilder();
        for (var attr : packet.attributes()) {
            attrSummary.append(String.format(" 0x%04x(len=%d)",
                    attr.type(), attr.value() == null ? 0 : attr.value().length));
        }
        logger.log(System.Logger.Level.INFO,
                String.format("Allocate response: msgType=0x%04x len=%d attrs=[%s]",
                        packet.messageType(), responseBytes.length, attrSummary.toString().trim()));
        if (packet.messageType() != WaRelayMessageType.ALLOCATE_SUCCESS.wireValue()) {
            // Pull out an ERROR-CODE attribute (0x0009) value bytes for diagnostics if present.
            String errorBlurb = "";
            for (var attr : packet.attributes()) {
                if (attr.type() == 0x0009) {
                    errorBlurb = " ERROR-CODE bytes=" + java.util.HexFormat.of().formatHex(attr.value());
                    break;
                }
            }
            throw new WhatsAppCallException.Ice(
                    "allocate response from " + relayRemote
                            + " has unexpected message type 0x"
                            + Integer.toHexString(packet.messageType()) + errorBlurb);
        }
        if (!Arrays.equals(packet.transactionId(), transactionId)) {
            throw new WhatsAppCallException.Ice(
                    "allocate response transaction-id mismatch from " + relayRemote);
        }
        if (!WaRelayMessageIntegrity.verify(responseBytes, relayKey)) {
            throw new WhatsAppCallException.Ice(
                    "allocate response MAC verification failed from " + relayRemote);
        }
        for (var attr : packet.attributes()) {
            if (attr.type() == WaRelayAttributeType.WA_RELAYED_ADDRESS.wireValue()) {
                logger.log(System.Logger.Level.INFO,
                        "WA_RELAYED_ADDRESS bytes=" + java.util.HexFormat.of().formatHex(attr.value()));
                return decodeWaRelayedAddress(attr.value(), transactionId);
            }
            if (attr.type() == WaRelayAttributeType.XOR_RELAYED_ADDRESS.wireValue()) {
                var xor = WaRelayXorAddress.decode(attr.value(), transactionId);
                return new InetSocketAddress(xor.address(), xor.port());
            }
        }
        throw new WhatsAppCallException.Ice(
                "allocate response from " + relayRemote
                        + " did not carry a relayed-address attribute (looked for 0x4002 or 0x0016)");
    }

    /**
     * Decodes the WA-specific {@code WA-RELAYED-ADDRESS} (0x4002) 8-byte (IPv4) or 20-byte (IPv6)
     * attribute value.
     *
     * <p>The 8-byte IPv4 wire layout is the same as the RFC 5389 {@code XOR-MAPPED-ADDRESS}
     * format but with the address-family byte cleared to {@code 0x00}: 1 byte reserved,
     * 1 byte family (often zero in this WA-specific encoding), 2 bytes XOR'd port, 4 bytes XOR'd
     * IPv4 address. We treat any non-IPv6 length (8 bytes) as IPv4 regardless of the family byte
     * value.
     *
     * @param value         the attribute value bytes (8 for IPv4, 20 for IPv6)
     * @param transactionId the request's transaction id, used as part of the XOR mask
     * @return the decoded relayed transport address
     */
    private static InetSocketAddress decodeWaRelayedAddress(byte[] value, byte[] transactionId) {
        if (value == null || (value.length != 8 && value.length != 20)) {
            throw new WhatsAppCallException.Ice(
                    "WA-RELAYED-ADDRESS attribute has unexpected length " + (value == null ? 0 : value.length));
        }
        var withFamily = new byte[value.length];
        System.arraycopy(value, 0, withFamily, 0, value.length);
        // Force the address-family byte to the canonical RFC 5389 value so WaRelayXorAddress.decode
        // accepts it. The WA-specific 0x4002 encoding zero-fills the family byte rather than
        // setting 0x01 / 0x02; length is the authoritative discriminator.
        withFamily[1] = (byte) (value.length == 8 ? 0x01 : 0x02);
        var xor = WaRelayXorAddress.decode(withFamily, transactionId);
        return new InetSocketAddress(xor.address(), xor.port());
    }

    /**
     * Decodes the {@code <te2>} content bytes into the relay's {@link InetSocketAddress}.
     *
     * <p>The wire format is the relay's address followed by a big-endian 2-byte port. A 6-byte
     * payload is {@code 4 bytes IPv4 + 2 bytes port}; an 18-byte payload is
     * {@code 16 bytes IPv6 + 2 bytes port}.
     *
     * <p>Using the te2 bytes directly is required: the auth-token + relay-key returned in the
     * offer ACK are bound to the specific relay endpoint the server selected for this call.
     * DNS-resolving the domain name to a different load-balanced IP routes the Allocate request
     * to the wrong relay, which silently drops the request.
     *
     * @param te2 the candidate endpoint
     * @return the resolved relay socket address
     * @throws WhatsAppCallException.Ice if the content bytes are not the expected 6 or 18 bytes
     */
    private static InetSocketAddress decodeTe2Endpoint(RelayEndpoint te2) {
        var bytes = te2.bytes();
        if (bytes.length != 6 && bytes.length != 18) {
            throw new WhatsAppCallException.Ice(
                    "te2 endpoint content must be 6 (IPv4+port) or 18 (IPv6+port) bytes; got "
                            + bytes.length);
        }
        var addressLen = bytes.length == 6 ? 4 : 16;
        var addressBytes = new byte[addressLen];
        System.arraycopy(bytes, 0, addressBytes, 0, addressLen);
        var port = ((bytes[addressLen] & 0xFF) << 8) | (bytes[addressLen + 1] & 0xFF);
        try {
            System.getLogger(WaRelayConnector.class.getName()).log(System.Logger.Level.INFO,
                    "te2 '" + te2.domainName() + "' rawContent=" + java.util.HexFormat.of().formatHex(bytes)
                            + " -> decoded=" + InetAddress.getByAddress(addressBytes).getHostAddress() + ":" + port);
            return new InetSocketAddress(InetAddress.getByAddress(addressBytes), port);
        } catch (Exception e) {
            throw new WhatsAppCallException.Ice(
                    "te2 endpoint address bytes are invalid for " + te2.domainName(), e);
        }
    }

    /**
     * Looks up the {@link RelayToken} with the given wire id.
     *
     * @param tokens the candidate relay tokens
     * @param id     the wire id to match
     * @return the matching token, or {@link Optional#empty()} when none has that id
     */
    private static Optional<RelayToken> findToken(
            List<RelayToken> tokens, int id) {
        for (var t : tokens) {
            if (t.id() == id) return Optional.of(t);
        }
        return Optional.empty();
    }

    /**
     * Looks up the {@link AuthToken} with the given wire id.
     *
     * @param authTokens the candidate auth tokens
     * @param id         the wire id to match
     * @return the matching auth token, or {@link Optional#empty()} when none has that id
     */
    private static Optional<AuthToken> findAuthToken(
            List<AuthToken> authTokens, int id) {
        for (var t : authTokens) {
            if (t.id() == id) return Optional.of(t);
        }
        return Optional.empty();
    }
}
