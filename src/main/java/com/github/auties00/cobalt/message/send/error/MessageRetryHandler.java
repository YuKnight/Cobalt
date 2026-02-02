package com.github.auties00.cobalt.message.send.error;

import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.device.phash.DevicePhashCalculator;
import com.github.auties00.cobalt.device.phash.DevicePhashVersion;
import com.github.auties00.cobalt.message.send.keys.MessagePreKeyBundleService;
import com.github.auties00.cobalt.message.send.MessageSendResult;
import com.github.auties00.cobalt.model.jid.Jid;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Handles retry logic for message sending including phash mismatch recovery.
 * <p>
 * Implements the retry strategies defined in the WhatsApp protocol:
 * <ul>
 *   <li>Phash mismatch: refresh device list, recalculate phash, retry</li>
 *   <li>Missing prekeys: fetch prekeys, establish sessions, retry</li>
 *   <li>Identity changed: notifyStr user, optionally retry</li>
 *   <li>Network errors: exponential backoff retry</li>
 * </ul>
 */
public final class MessageRetryHandler {
    private static final int MAX_PHASH_RETRIES = 3;
    private static final int MAX_NETWORK_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 100;
    private static final long MAX_BACKOFF_MS = 30000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private final DeviceService deviceService;
    private final MessagePreKeyBundleService preKeyService;

    /**
     * Cache of phash retries per message to prevent infinite loops.
     * Key: messageId, Value: retry count
     */
    private final ConcurrentHashMap<String, Integer> phashRetryCache;

    public MessageRetryHandler(DeviceService deviceService, MessagePreKeyBundleService preKeyService) {
        this.deviceService = Objects.requireNonNull(deviceService, "deviceService cannot be null");
        this.preKeyService = Objects.requireNonNull(preKeyService, "preKeyService cannot be null");
        this.phashRetryCache = new ConcurrentHashMap<>();
    }

    /**
     * Handles a phash mismatch by refreshing device lists and retrying.
     *
     * @param messageId      the message ID
     * @param expectedPhash  the phash the server expected
     * @param userJids       the user JIDs whose device lists need refreshing
     * @param retryAction    the action to retry after refreshing
     * @return the result of the retry
     */
    public MessageSendResult handlePhashMismatch(
            String messageId,
            String expectedPhash,
            Collection<Jid> userJids,
            Supplier<MessageSendResult> retryAction
    ) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(retryAction, "retryAction cannot be null");

        // Check retry count
        int retryCount = phashRetryCache.compute(messageId, (_, count) ->
                count == null ? 1 : count + 1
        );

        if (retryCount > MAX_PHASH_RETRIES) {
            phashRetryCache.remove(messageId);
            return new MessageSendResult.ProtocolError(
                    "PHASH_RETRY_EXHAUSTED",
                    "Exceeded maximum phash mismatch retries (" + MAX_PHASH_RETRIES + ")"
            );
        }

        try {
            // Refresh device lists
            var refreshedLists = deviceService.getDeviceLists(userJids);

            // Verify new phash matches expected
            var allDevices = new ArrayList<Jid>();
            for (var list : refreshedLists) {
                var userJid = list.userJid();
                for(var device : list.devices()) {
                    var deviceJid = device.toDeviceJid(userJid.user(), userJid.server());
                    allDevices.add(deviceJid);
                }
            }

            // Recalculate phash without meta bot (assume server doesn't expect it if there was a mismatch)
            var newPhash = DevicePhashCalculator.calculate(allDevices, DevicePhashVersion.V2, false);

            if (expectedPhash != null && !expectedPhash.equals(newPhash)) {
                // Still mismatched - this shouldn't happen unless there's a race
                // Log but continue with retry
                System.err.println("Warning: Phash still mismatched after refresh. " +
                        "Expected: " + expectedPhash + ", Got: " + newPhash);
            }

            // Retry the send
            var result = retryAction.get();

            // Clean up cache on success
            if (result.isSuccess()) {
                phashRetryCache.remove(messageId);
            }

            return new MessageSendResult.Retried(retryCount,
                    new MessageSendResult.PhashMismatch(expectedPhash, null), result);

        } catch (NoSuchAlgorithmException e) {
            phashRetryCache.remove(messageId);
            throw new InternalError("SHA-256 not available", e);
        } catch (Exception e) {
            phashRetryCache.remove(messageId);
            return new MessageSendResult.NetworkError(e);
        }
    }

    /**
     * Handles missing prekeys by fetching them and retrying.
     *
     * @param devices     the devices missing prekeys
     * @param retryAction the action to retry after fetching prekeys
     * @return the result of the retry
     */
    public MessageSendResult handleMissingPreKeys(
            List<Jid> devices,
            Supplier<MessageSendResult> retryAction
    ) {
        Objects.requireNonNull(devices, "devices cannot be null");
        Objects.requireNonNull(retryAction, "retryAction cannot be null");

        try {
            // Fetch prekeys for missing devices
            var bundles = preKeyService.fetchAndProcessPreKeyBundles(devices);

            if (bundles.isEmpty()) {
                return new MessageSendResult.ProtocolError(
                        "PREKEY_FETCH_FAILED",
                        "Failed to fetch prekeys for devices: " + devices
                );
            }

            // Retry the send
            return retryAction.get();

        } catch (Exception e) {
            return new MessageSendResult.NetworkError(e);
        }
    }

    /**
     * Handles sender key distribution needed for group messages.
     *
     * @param devices                       the devices needing sender key distribution
     * @param distributionAction           action to distribute sender keys
     * @param retryAction                  action to retry after distribution
     * @return the result of the retry
     */
    public MessageSendResult handleSenderKeyDistributionNeeded(
            List<Jid> devices,
            Runnable distributionAction,
            Supplier<MessageSendResult> retryAction
    ) {
        Objects.requireNonNull(devices, "devices cannot be null");
        Objects.requireNonNull(distributionAction, "distributionAction cannot be null");
        Objects.requireNonNull(retryAction, "retryAction cannot be null");

        try {
            // Distribute sender keys
            distributionAction.run();

            // Retry the send
            return retryAction.get();

        } catch (Exception e) {
            return new MessageSendResult.NetworkError(e);
        }
    }

    /**
     * Handles network errors with exponential backoff retry.
     *
     * @param messageId   the message ID
     * @param retryCount  current retry attempt
     * @param retryAction the action to retry
     * @return the result of the retry
     */
    public MessageSendResult handleNetworkError(
            String messageId,
            int retryCount,
            Supplier<MessageSendResult> retryAction
    ) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(retryAction, "retryAction cannot be null");

        if (retryCount >= MAX_NETWORK_RETRIES) {
            return new MessageSendResult.ProtocolError(
                    "NETWORK_RETRY_EXHAUSTED",
                    "Exceeded maximum network error retries (" + MAX_NETWORK_RETRIES + ")"
            );
        }

        // Calculate backoff with jitter
        var backoff = Math.min(
                INITIAL_BACKOFF_MS * Math.pow(BACKOFF_MULTIPLIER, retryCount),
                MAX_BACKOFF_MS
        );
        var jitter = (long) (backoff * 0.2 * Math.random());
        var sleepTime = (long) backoff + jitter;

        try {
            Thread.sleep(sleepTime);
            return retryAction.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new MessageSendResult.NetworkError(e);
        }
    }

    /**
     * Clears retry state for a message.
     * Call this after successful sends or when abandoning retries.
     *
     * @param messageId the message ID
     */
    public void clearRetryState(String messageId) {
        phashRetryCache.remove(messageId);
    }

    /**
     * Gets the current retry count for a message.
     *
     * @param messageId the message ID
     * @return the retry count, or 0 if no retries
     */
    public int getRetryCount(String messageId) {
        return phashRetryCache.getOrDefault(messageId, 0);
    }

    /**
     * Determines if a result is retryable.
     *
     * @param result the send result
     * @return true if the result indicates a retryable error
     */
    public static boolean isRetryable(MessageSendResult result) {
        return result.isRetryable();
    }

    /**
     * Determines the retry strategy for a given result.
     *
     * @param result the send result
     * @return the recommended retry strategy
     */
    public static MessageRetryStrategy getRetryStrategy(MessageSendResult result) {
        return switch (result) {
            case MessageSendResult.PhashMismatch _ -> MessageRetryStrategy.REFRESH_DEVICE_LIST;
            case MessageSendResult.MissingPreKeys _ -> MessageRetryStrategy.FETCH_PREKEYS;
            case MessageSendResult.SenderKeyDistributionNeeded _ -> MessageRetryStrategy.DISTRIBUTE_SENDER_KEY;
            case MessageSendResult.IdentityChanged _ -> MessageRetryStrategy.USER_CONFIRMATION;
            case MessageSendResult.NetworkError _ -> MessageRetryStrategy.EXPONENTIAL_BACKOFF;
            default -> MessageRetryStrategy.NONE;
        };
    }
}
