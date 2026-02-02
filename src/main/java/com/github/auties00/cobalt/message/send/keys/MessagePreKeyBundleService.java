package com.github.auties00.cobalt.message.send.keys;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.key.SignalIdentityPublicKey;
import com.github.auties00.libsignal.state.SignalPreKeyBundle;
import com.github.auties00.libsignal.state.SignalPreKeyBundleBuilder;

import java.util.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * Service for fetching prekey bundles from the server.
 * Prekey bundles are needed to establish Signal sessions with devices
 * that we haven't communicated with before.
 */
public final class MessagePreKeyBundleService {
    private static final int MAX_DEVICES_PER_QUERY = 100;
    private static final String ENCRYPT_XMLNS = "encrypt";

    private final WhatsAppClient client;
    private final SignalSessionCipher sessionCipher;

    public MessagePreKeyBundleService(WhatsAppClient client, SignalSessionCipher sessionCipher) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.sessionCipher = Objects.requireNonNull(sessionCipher, "sessionCipher cannot be null");
    }

    /**
     * Fetches prekey bundles for the specified devices and establishes Signal sessions.
     * Devices are batched to avoid overwhelming the server.
     *
     * @param deviceJids the device JIDs to fetch prekeys for
     * @return map of device JIDs to their prekey bundles (empty map if fetch fails)
     */
    public Map<Jid, SignalPreKeyBundle> fetchAndProcessPreKeyBundles(Collection<Jid> deviceJids) {
        Objects.requireNonNull(deviceJids, "deviceJids cannot be null");

        if (deviceJids.isEmpty()) {
            return Map.of();
        }

        // Batch devices for querying
        var batches = batchDevices(deviceJids);
        var allBundles = new HashMap<Jid, SignalPreKeyBundle>();

        // Fetch prekeys in parallel for each batch
        try (var scope = StructuredTaskScope.open()) {
            var subtasks = new ArrayList<Subtask<Map<Jid, SignalPreKeyBundle>>>();
            for (var batch : batches) {
                subtasks.add(scope.fork(() -> fetchPreKeyBatch(batch)));
            }
            scope.join();

            // Collect results
            for (var subtask : subtasks) {
                if (subtask.state() == Subtask.State.SUCCESS) {
                    allBundles.putAll(subtask.get());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while fetching prekey bundles", e);
        }

        // Process bundles to establish sessions
        for (var entry : allBundles.entrySet()) {
            var deviceJid = entry.getKey();
            var bundle = entry.getValue();
            var address = deviceJid.toSignalAddress();
            sessionCipher.process(address, bundle);
        }

        return allBundles;
    }

    /**
     * Fetches prekey bundles for a single batch of devices.
     */
    private Map<Jid, SignalPreKeyBundle> fetchPreKeyBatch(List<Jid> deviceJids) {
        var query = buildPreKeyQuery(deviceJids);
        var response = client.sendNode(query);
        return parsePreKeyResponse(response);
    }

    /**
     * Batches device JIDs for querying.
     */
    private List<List<Jid>> batchDevices(Collection<Jid> deviceJids) {
        var batches = new ArrayList<List<Jid>>();
        var currentBatch = new ArrayList<Jid>();

        for (var jid : deviceJids) {
            currentBatch.add(jid);
            if (currentBatch.size() >= MAX_DEVICES_PER_QUERY) {
                batches.add(new ArrayList<>(currentBatch));
                currentBatch.clear();
            }
        }

        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        return batches;
    }

    /**
     * Builds the prekey query IQ stanza.
     *
     * <pre>
     * &lt;iq id="{uuid}" xmlns="encrypt" type="get" to="s.whatsapp.net"&gt;
     *   &lt;key&gt;
     *     &lt;user jid="{device_jid}"/&gt;
     *     ...
     *   &lt;/key&gt;
     * &lt;/iq&gt;
     * </pre>
     */
    private NodeBuilder buildPreKeyQuery(List<Jid> deviceJids) {
        var userNodes = deviceJids.stream()
                .map(jid -> new NodeBuilder()
                        .description("user")
                        .attribute("jid", jid)
                        .build())
                .toList();

        var keyNode = new NodeBuilder()
                .description("key")
                .content(userNodes)
                .build();

        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", ENCRYPT_XMLNS)
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(keyNode);
    }

    /**
     * Parses the prekey response into SignalPreKeyBundle objects.
     *
     * <pre>
     * &lt;iq id="{uuid}" type="result"&gt;
     *   &lt;list&gt;
     *     &lt;user jid="{device_jid}"&gt;
     *       &lt;registration&gt;{registration_id}&lt;/registration&gt;
     *       &lt;type&gt;{key_type}&lt;/type&gt;
     *       &lt;identity&gt;{identity_key_32bytes}&lt;/identity&gt;
     *       &lt;skey&gt;
     *         &lt;id&gt;{signed_prekey_id}&lt;/id&gt;
     *         &lt;value&gt;{signed_prekey_public_32bytes}&lt;/value&gt;
     *         &lt;signature&gt;{signature_64bytes}&lt;/signature&gt;
     *       &lt;/skey&gt;
     *       &lt;key&gt;
     *         &lt;id&gt;{one_time_prekey_id}&lt;/id&gt;
     *         &lt;value&gt;{one_time_prekey_public_32bytes}&lt;/value&gt;
     *       &lt;/key&gt;
     *     &lt;/user&gt;
     *   &lt;/list&gt;
     * &lt;/iq&gt;
     * </pre>
     */
    private Map<Jid, SignalPreKeyBundle> parsePreKeyResponse(Node response) {
        var result = new HashMap<Jid, SignalPreKeyBundle>();

        var listNode = response.getChild("list");
        if (listNode.isEmpty()) {
            return result;
        }

        for (var userNode : listNode.get().getChildren("user")) {
            try {
                var bundle = parseUserPreKeyBundle(userNode);
                if (bundle != null) {
                    var deviceJid = userNode.getRequiredAttributeAsJid("jid");
                    result.put(deviceJid, bundle);
                }
            } catch (Exception e) {
                // Log and continue with other devices
                var jid = userNode.getAttribute("jid").map(Object::toString).orElse("unknown");
                System.err.println("Failed to parse prekey bundle for " + jid + ": " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Parses a single user's prekey bundle from the response.
     */
    private SignalPreKeyBundle parseUserPreKeyBundle(Node userNode) {
        // Extract registration ID
        var registrationId = userNode.getChild("registration")
                .flatMap(Node::toContentInt)
                .orElseThrow(() -> new IllegalArgumentException("Missing registration ID"));

        // Extract identity key
        var identityKey = userNode.getChild("identity")
                .flatMap(Node::toContentBytes)
                .map(SignalIdentityPublicKey::ofDirect)
                .orElseThrow(() -> new IllegalArgumentException("Missing identity key"));

        // Extract signed prekey
        var signedPreKeyNode = userNode.getChild("skey")
                .orElseThrow(() -> new IllegalArgumentException("Missing signed prekey"));

        var signedPreKeyId = signedPreKeyNode.getChild("id")
                .flatMap(Node::toContentInt)
                .orElseThrow(() -> new IllegalArgumentException("Missing signed prekey ID"));

        var signedPreKeyValue = signedPreKeyNode.getChild("value")
                .flatMap(Node::toContentBytes)
                .map(SignalIdentityPublicKey::ofDirect)
                .orElseThrow(() -> new IllegalArgumentException("Missing signed prekey value"));

        var signedPreKeySignature = signedPreKeyNode.getChild("signature")
                .flatMap(Node::toContentBytes)
                .orElseThrow(() -> new IllegalArgumentException("Missing signed prekey signature"));

        var builder = new SignalPreKeyBundleBuilder()
                .registrationId(registrationId)
                .deviceId(0)
                .signedPreKeyId(signedPreKeyId)
                .signedPreKeyPublic(signedPreKeyValue)
                .signedPreKeySignature(signedPreKeySignature)
                .identityKey(identityKey);

        // Extract one-time prekey (optional)
        var preKeyNode = userNode.getChild("key");
        if (preKeyNode.isPresent()) {
            var preKeyId = preKeyNode.get().getChild("id")
                    .flatMap(Node::toContentInt)
                    .orElse(null);

            var preKeyValue = preKeyNode.get().getChild("value")
                    .flatMap(Node::toContentBytes)
                    .map(SignalIdentityPublicKey::ofDirect)
                    .orElse(null);

            if (preKeyId != null && preKeyValue != null) {
                builder.preKeyId(preKeyId);
                builder.preKeyPublic(preKeyValue);
            }
        }

        // Build the bundle
        return builder.build();
    }

    /**
     * Checks which devices from the given list don't have Signal sessions.
     *
     * @param deviceJids the device JIDs to check
     * @return list of device JIDs that need sessions
     */
    public List<Jid> findDevicesNeedingSessions(Collection<Jid> deviceJids) {
        var store = client.store();
        var result = new ArrayList<Jid>();

        for (var deviceJid : deviceJids) {
            var address = deviceJid.toSignalAddress();
            if (store.findSessionByAddress(address).isEmpty()) {
                result.add(deviceJid);
            }
        }

        return result;
    }

    /**
     * Ensures Signal sessions exist for all specified devices.
     * Fetches prekeys and creates sessions for any devices without sessions.
     *
     * @param deviceJids the device JIDs to ensure sessions for
     */
    public void ensureSessions(Collection<Jid> deviceJids) {
        var devicesNeedingSessions = findDevicesNeedingSessions(deviceJids);
        if (devicesNeedingSessions.isEmpty()) {
            return;
        }

        fetchAndProcessPreKeyBundles(devicesNeedingSessions);
    }
}
