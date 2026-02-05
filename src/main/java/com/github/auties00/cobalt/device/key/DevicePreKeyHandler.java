package com.github.auties00.cobalt.device.key;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * Service for fetching prekey bundles from the server.
 * Prekey bundles are needed to establish Signal sessions with devices
 * that we haven't communicated with before.
 *
 * @apiNote WAWebE2ESessionService: implements deduplication of concurrent session
 * establishment requests to prevent duplicate prekey fetches.
 */
public final class DevicePreKeyHandler {
    private static final int MAX_DEVICES_PER_QUERY = 100;
    private static final String ENCRYPT_XMLNS = "encrypt";

    private final WhatsAppClient client;
    private final SignalSessionCipher sessionCipher;

    /**
     * Tracks in-flight prekey fetch requests by device JID to prevent duplicate
     * concurrent session establishment requests.
     * <p>
     * When multiple message sends target the same device simultaneously, this ensures
     * we only fetch prekeys once and other callers wait for the same result.
     *
     * @apiNote WAWebE2ESessionService: deduplicates concurrent getE2ESession calls
     */
    private final ConcurrentHashMap<Jid, CompletableFuture<SignalPreKeyBundle>> inFlightRequests = new ConcurrentHashMap<>();

    public DevicePreKeyHandler(WhatsAppClient client, SignalSessionCipher sessionCipher) {
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
        return fetchAndProcessPreKeyBundles(deviceJids, false);
    }

    /**
     * Fetches prekey bundles for the specified devices and establishes Signal sessions.
     * Devices are batched to avoid overwhelming the server.
     * <p>
     * Implements deduplication: concurrent requests for the same device will share the
     * same fetch operation instead of making duplicate requests.
     * <p>
     * Per WhatsApp Web: the hasUserReasonIdentity flag can be set to indicate to the server
     * that the fetch is for identity verification purposes.
     *
     * @param deviceJids            the device JIDs to fetch prekeys for
     * @param hasUserReasonIdentity whether to include the "reason=identity" hint in the request
     * @return map of device JIDs to their prekey bundles (empty map if fetch fails)
     *
     * @apiNote WAWebE2ESessionService: deduplicates concurrent session establishment requests
     */
    public Map<Jid, SignalPreKeyBundle> fetchAndProcessPreKeyBundles(Collection<Jid> deviceJids, boolean hasUserReasonIdentity) {
        Objects.requireNonNull(deviceJids, "deviceJids cannot be null");

        if (deviceJids.isEmpty()) {
            return Map.of();
        }

        // Separate devices into those with in-flight requests and those needing new fetches
        var devicesNeedingFetch = new ArrayList<Jid>();
        var existingFutures = new HashMap<Jid, CompletableFuture<SignalPreKeyBundle>>();

        for (var deviceJid : deviceJids) {
            var existingFuture = inFlightRequests.get(deviceJid);
            if (existingFuture != null) {
                // Wait for existing request instead of making a duplicate
                existingFutures.put(deviceJid, existingFuture);
            } else {
                devicesNeedingFetch.add(deviceJid);
            }
        }

        // Register new futures for devices we're about to fetch
        var newFutures = new HashMap<Jid, CompletableFuture<SignalPreKeyBundle>>();
        for (var deviceJid : devicesNeedingFetch) {
            var future = new CompletableFuture<SignalPreKeyBundle>();
            var existing = inFlightRequests.putIfAbsent(deviceJid, future);
            if (existing != null) {
                // Race condition: another thread registered first, use their future
                existingFutures.put(deviceJid, existing);
            } else {
                newFutures.put(deviceJid, future);
            }
        }

        // Fetch prekeys for devices that need them
        var allBundles = new HashMap<Jid, SignalPreKeyBundle>();

        if (!newFutures.isEmpty()) {
            var devicesToFetch = new ArrayList<>(newFutures.keySet());
            var batches = batchDevices(devicesToFetch);

            try (var scope = StructuredTaskScope.open()) {
                var subtasks = new ArrayList<Subtask<Map<Jid, SignalPreKeyBundle>>>();
                for (var batch : batches) {
                    subtasks.add(scope.fork(() -> fetchPreKeyBatch(batch, hasUserReasonIdentity)));
                }
                scope.join();

                // Collect results and complete futures
                for (var subtask : subtasks) {
                    if (subtask.state() == Subtask.State.SUCCESS) {
                        var fetchedBundles = subtask.get();
                        allBundles.putAll(fetchedBundles);

                        // Complete futures for fetched devices
                        for (var entry : fetchedBundles.entrySet()) {
                            var future = newFutures.get(entry.getKey());
                            if (future != null) {
                                future.complete(entry.getValue());
                            }
                        }
                    }
                }

                // Complete any remaining futures with null (device not in response)
                for (var entry : newFutures.entrySet()) {
                    if (!entry.getValue().isDone()) {
                        entry.getValue().complete(null);
                    }
                }
            } catch (InterruptedException e) {
                // Complete futures exceptionally
                for (var future : newFutures.values()) {
                    future.completeExceptionally(e);
                }
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while fetching prekey bundles", e);
            } finally {
                // Remove our futures from in-flight tracking
                for (var deviceJid : newFutures.keySet()) {
                    inFlightRequests.remove(deviceJid);
                }
            }
        }

        // Wait for existing in-flight requests to complete
        for (var entry : existingFutures.entrySet()) {
            try {
                var bundle = entry.getValue().join();
                if (bundle != null) {
                    allBundles.put(entry.getKey(), bundle);
                }
            } catch (Exception e) {
                // Log and continue - the device won't have a bundle
            }
        }

        // Per WhatsApp Web: sort prekey bundles - primary devices first, then companions
        // This ensures primary device sessions are established before companion devices
        var sortedEntries = allBundles.entrySet().stream()
                .sorted((a, b) -> {
                    var deviceA = a.getKey().device();
                    var deviceB = b.getKey().device();
                    // Primary devices (device == 0) come first
                    var isPrimaryA = deviceA == 0;
                    var isPrimaryB = deviceB == 0;
                    if (isPrimaryA && !isPrimaryB) return -1;
                    if (!isPrimaryA && isPrimaryB) return 1;
                    // Then sort by device ID for consistency
                    return Integer.compare(deviceA, deviceB);
                })
                .toList();

        // Process bundles to establish sessions in sorted order
        for (var entry : sortedEntries) {
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
    private Map<Jid, SignalPreKeyBundle> fetchPreKeyBatch(List<Jid> deviceJids, boolean hasUserReasonIdentity) {
        var query = buildPreKeyQuery(deviceJids, hasUserReasonIdentity);
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
     * <p>
     * Per WhatsApp Web: the hasUserReasonIdentity flag adds a "reason=identity" attribute
     * to each user node, indicating to the server that the fetch is for identity purposes.
     *
     * <pre>
     * &lt;iq id="{uuid}" xmlns="encrypt" type="get" to="s.whatsapp.net"&gt;
     *   &lt;key&gt;
     *     &lt;user jid="{device_jid}" reason="identity"/&gt;
     *     ...
     *   &lt;/key&gt;
     * &lt;/iq&gt;
     * </pre>
     *
     * @param deviceJids            the device JIDs to query
     * @param hasUserReasonIdentity whether to include reason=identity attribute
     */
    private NodeBuilder buildPreKeyQuery(List<Jid> deviceJids, boolean hasUserReasonIdentity) {
        var userNodes = deviceJids.stream()
                .map(jid -> {
                    var builder = new NodeBuilder()
                            .description("user")
                            .attribute("jid", jid);
                    if (hasUserReasonIdentity) {
                        builder.attribute("reason", "identity");
                    }
                    return builder.build();
                })
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

    /**
     * Fetches and stores identity keys for users who don't have them cached.
     * <p>
     * This is an optimization that prefetches identity keys after device sync,
     * so they're available when needed for message encryption without an extra
     * round trip. Per WhatsApp Web: called after device sync for users with
     * validated key index info.
     * <p>
     * Query format:
     * <pre>
     * &lt;iq id="{uuid}" xmlns="encrypt" type="get" to="s.whatsapp.net"&gt;
     *   &lt;identity&gt;
     *     &lt;user jid="{user_jid}"/&gt;
     *     ...
     *   &lt;/identity&gt;
     * &lt;/iq&gt;
     * </pre>
     *
     * @param userJids the user JIDs to fetch identity keys for
     */
    public void fetchAndStoreIdentityKeys(Collection<Jid> userJids) {
        Objects.requireNonNull(userJids, "userJids cannot be null");

        if (userJids.isEmpty()) {
            return;
        }

        var store = client.store();

        // Filter out users that already have identity keys stored
        var usersNeedingKeys = new ArrayList<Jid>();
        for (var userJid : userJids) {
            // Check if we have identity key for the primary device
            var primaryDeviceJid = userJid.toUserJid().withDevice(0);
            var address = primaryDeviceJid.toSignalAddress();
            if (store.findSessionByAddress(address).isEmpty()) {
                usersNeedingKeys.add(userJid);
            }
        }

        if (usersNeedingKeys.isEmpty()) {
            return;
        }

        // Batch and fetch identity keys
        var batches = batchUsers(usersNeedingKeys, MAX_DEVICES_PER_QUERY);

        try (var scope = StructuredTaskScope.open()) {
            var subtasks = new ArrayList<Subtask<Void>>();
            for (var batch : batches) {
                subtasks.add(scope.fork(() -> {
                    fetchAndStoreIdentityKeyBatch(batch);
                    return null;
                }));
            }
            scope.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while fetching identity keys", e);
        }
    }

    /**
     * Fetches and stores identity keys for a single batch of users.
     */
    private void fetchAndStoreIdentityKeyBatch(List<Jid> userJids) {
        var query = buildIdentityKeyQuery(userJids);
        var response = client.sendNode(query);
        parseAndStoreIdentityKeyResponse(response);
    }

    /**
     * Builds the identity key query IQ stanza.
     */
    private NodeBuilder buildIdentityKeyQuery(List<Jid> userJids) {
        var userNodes = userJids.stream()
                .map(jid -> new NodeBuilder()
                        .description("user")
                        .attribute("jid", jid.toUserJid().withDevice(0)) // Query primary device
                        .build())
                .toList();

        var identityNode = new NodeBuilder()
                .description("identity")
                .content(userNodes)
                .build();

        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", ENCRYPT_XMLNS)
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(identityNode);
    }

    /**
     * Parses the identity key response and stores the keys.
     * <p>
     * Response format:
     * <pre>
     * &lt;iq id="{uuid}" type="result"&gt;
     *   &lt;list&gt;
     *     &lt;user jid="{device_jid}"&gt;
     *       &lt;type&gt;{key_type}&lt;/type&gt;
     *       &lt;identity&gt;{identity_key_32bytes}&lt;/identity&gt;
     *     &lt;/user&gt;
     *   &lt;/list&gt;
     * &lt;/iq&gt;
     * </pre>
     */
    private void parseAndStoreIdentityKeyResponse(Node response) {
        var listNode = response.getChild("list");
        if (listNode.isEmpty()) {
            return;
        }

        var store = client.store();

        for (var userNode : listNode.get().getChildren("user")) {
            try {
                // Check for error
                var errorNode = userNode.getChild("error");
                if (errorNode.isPresent()) {
                    continue;
                }

                var deviceJid = userNode.getRequiredAttributeAsJid("jid");

                // Extract identity key
                var identityKeyBytes = userNode.getChild("identity")
                        .flatMap(Node::toContentBytes)
                        .orElse(null);

                if (identityKeyBytes == null || identityKeyBytes.length != 32) {
                    continue;
                }

                // Store the identity key
                var address = deviceJid.toSignalAddress();
                var identityKey = SignalIdentityPublicKey.ofDirect(identityKeyBytes);
                store.addTrustedIdentity(address, identityKey);

            } catch (Exception e) {
                // Log and continue with other users
                var jid = userNode.getAttribute("jid").map(Object::toString).orElse("unknown");
                System.err.println("Failed to store identity key for " + jid + ": " + e.getMessage());
            }
        }
    }

    /**
     * Batches user JIDs for querying.
     */
    private List<List<Jid>> batchUsers(Collection<Jid> userJids, int batchSize) {
        var batches = new ArrayList<List<Jid>>();
        var currentBatch = new ArrayList<Jid>();

        for (var jid : userJids) {
            currentBatch.add(jid);
            if (currentBatch.size() >= batchSize) {
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
     * Stores an identity key directly from an account signature key.
     * <p>
     * Per WhatsApp Web: when hostedOverrideAdvAccountSignatureKeyEnabled is true
     * and the device list contains hosted devices, the accountSignatureKey from
     * the signed key index list is saved as the identity key for the user.
     *
     * @param userJid             the user JID to store the identity for
     * @param accountSignatureKey the 32-byte account signature key
     */
    public void storeIdentityFromAccountSignatureKey(Jid userJid, byte[] accountSignatureKey) {
        Objects.requireNonNull(userJid, "userJid cannot be null");
        Objects.requireNonNull(accountSignatureKey, "accountSignatureKey cannot be null");

        if (accountSignatureKey.length != 32) {
            throw new IllegalArgumentException("Account signature key must be 32 bytes");
        }

        var store = client.store();
        var primaryDeviceJid = userJid.toUserJid().withDevice(0);
        var address = primaryDeviceJid.toSignalAddress();
        var identityKey = SignalIdentityPublicKey.ofDirect(accountSignatureKey);
        store.addTrustedIdentity(address, identityKey);
    }
}
