package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.sync.SyncActionMessage;
import com.github.auties00.cobalt.model.sync.SyncActionMessageBuilder;
import com.github.auties00.cobalt.model.sync.SyncActionMessageRange;
import com.github.auties00.cobalt.model.sync.SyncActionMessageRangeBuilder;

import java.time.Instant;
import java.util.*;

/**
 * Utilities for comparing, merging, and rewriting
 * {@link SyncActionMessageRange} objects used by chat-scoped app state
 * mutations (archive, clear-chat, delete-chat, mark-chat-as-read).
 *
 * <p>Per WhatsApp Web {@code WAWebMessageRangeUtils}: a message range
 * describes the set of messages an action applies to. It is used both to
 * resolve conflicts between local and remote mutations on the same index
 * (via {@link #compareMessageRanges(SyncActionMessageRange, SyncActionMessageRange)}
 * and
 * {@link #mergeMessageRanges(SyncActionMessageRange, SyncActionMessageRange)})
 * and to make mutations deterministic across linked devices (by pinning
 * the affected messages by timestamp and key).
 *
 * <p>Several WA Web exports in this module are tied to browser-specific
 * infrastructure ({@code WAWebDBMessageRange}, {@code WAWebApiActiveMessageRanges},
 * {@code WAWebSyncdCoreApi.lockForSync}) that Cobalt does not maintain.
 * The {@code constructMessageRange}, {@code constructForwardMovingMessageRange}
 * and {@code lockForMessageRangeSync} exports are adapted outside this class:
 * {@link com.github.auties00.cobalt.client.WhatsAppClient#buildOutgoingMessageRange}
 * derives a minimal range from the chat's newest in-memory message, and
 * {@code WebAppStateService.pushPatches} serializes per-collection writes so
 * no explicit lock is required.
 *
 * @implNote The three store-bound exports above have no in-class stub in
 *           Cobalt; see {@code WhatsAppClient.buildOutgoingMessageRange} and
 *           {@code WebAppStateService.pushPatches} for their adaptations.
 */
@WhatsAppWebModule(moduleName = "WAWebMessageRangeUtils")
final class MessageRangeUtils {
    /**
     * The result of comparing two message ranges.
     *
     * <p>Per WhatsApp Web {@code MessageRangeEncloseType}: enum with four
     * string-valued members. Cobalt maps each to an equivalent Java enum
     * constant; the renaming from camel case to SCREAMING_SNAKE is purely
     * stylistic.
     *
     * @implNote WAWebMessageRangeUtils.MessageRangeEncloseType
     */
    @WhatsAppWebModule(moduleName = "WAWebMessageRangeUtils")
    enum EnclosureType {
        /**
         * Range A fully encloses range B (A covers all messages that B covers).
         *
         * @implNote WAWebMessageRangeUtils.MessageRangeEncloseType.RangeAEnclosesRangeB
         */
        RANGE_A_ENCLOSES_RANGE_B,

        /**
         * Range B fully encloses range A (B covers all messages that A covers).
         *
         * @implNote WAWebMessageRangeUtils.MessageRangeEncloseType.RangeBEnclosesRangeA
         */
        RANGE_B_ENCLOSES_RANGE_A,

        /**
         * Both ranges cover exactly the same set of messages.
         *
         * @implNote WAWebMessageRangeUtils.MessageRangeEncloseType.RangesAreEqual
         */
        RANGES_ARE_EQUAL,

        /**
         * Neither range fully encloses the other (partial overlap or disjoint).
         *
         * @implNote WAWebMessageRangeUtils.MessageRangeEncloseType.RangesNotEnclosing
         */
        RANGES_NOT_ENCLOSING
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @implNote NO_WA_BASIS - Java utility class pattern
     */
    private MessageRangeUtils() {
    }

    /**
     * Compares two message ranges to determine their enclosure relationship.
     *
     * <p>Per WhatsApp Web {@code compareMessageRanges}: checks in both
     * directions whether one range encloses the other and maps the two
     * boolean outcomes to one of the four {@link EnclosureType} values.
     *
     * @implNote WAWebMessageRangeUtils.compareMessageRanges
     * @param rangeA the first range (typically remote)
     * @param rangeB the second range (typically local)
     * @return the enclosure relationship between the two ranges
     */
    @WhatsAppWebExport(
            moduleName = "WAWebMessageRangeUtils",
            exports = "compareMessageRanges",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    static EnclosureType compareMessageRanges(SyncActionMessageRange rangeA, SyncActionMessageRange rangeB) {
        var aEnclosesB = encloses(rangeA, rangeB);
        var bEnclosesA = encloses(rangeB, rangeA);
        if (aEnclosesB && bEnclosesA) {
            return EnclosureType.RANGES_ARE_EQUAL;
        } else if (aEnclosesB) {
            return EnclosureType.RANGE_A_ENCLOSES_RANGE_B;
        } else if (bEnclosesA) {
            return EnclosureType.RANGE_B_ENCLOSES_RANGE_A;
        } else {
            return EnclosureType.RANGES_NOT_ENCLOSING;
        }
    }

    /**
     * Merges two message ranges into one that covers the union of both.
     *
     * <p>Per WhatsApp Web {@code mergeMessageRanges}:
     * <ul>
     *   <li>Takes the maximum of both {@code lastMessageTimestamp} values
     *   <li>Merges message lists: keeps only messages whose timestamp is
     *       {@code >=} the max timestamp; among duplicates (same key ID),
     *       keeps the one with the higher timestamp
     *   <li>Takes the maximum of both {@code lastSystemMessageTimestamp}
     *       values, but only sets it if it strictly exceeds the merged
     *       {@code lastMessageTimestamp}
     * </ul>
     *
     * @implNote WAWebMessageRangeUtils.mergeMessageRanges
     * @param rangeA the first range (typically remote)
     * @param rangeB the second range (typically local)
     * @return a new merged range covering both inputs
     */
    @WhatsAppWebExport(
            moduleName = "WAWebMessageRangeUtils",
            exports = "mergeMessageRanges",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    static SyncActionMessageRange mergeMessageRanges(SyncActionMessageRange rangeA, SyncActionMessageRange rangeB) {
        var aLastTimestamp = toEpochSeconds(rangeA.lastMessageTimestamp().orElse(null));
        var bLastTimestamp = toEpochSeconds(rangeB.lastMessageTimestamp().orElse(null));
        var maxTimestamp = Math.max(aLastTimestamp, bLastTimestamp);

        var mergedMessages = mergeMessages(rangeA.messages(), rangeB.messages(), maxTimestamp);

        var builder = new SyncActionMessageRangeBuilder()
                .messages(mergedMessages);

        if (maxTimestamp != 0) {
            builder.lastMessageTimestamp(Instant.ofEpochSecond(maxTimestamp));
        }

        var aSystemTimestamp = toEpochSeconds(rangeA.lastSystemMessageTimestamp().orElse(null));
        var bSystemTimestamp = toEpochSeconds(rangeB.lastSystemMessageTimestamp().orElse(null));
        if (aSystemTimestamp != 0 || bSystemTimestamp != 0) {
            var maxSystemTimestamp = Math.max(aSystemTimestamp, bSystemTimestamp);
            if (maxSystemTimestamp > maxTimestamp) {
                builder.lastSystemMessageTimestamp(Instant.ofEpochSecond(maxSystemTimestamp));
            }
        }

        return builder.build();
    }

    /**
     * Validates a message range and returns it unchanged when valid.
     *
     * <p>Per WhatsApp Web {@code validateMessageRange}: checks the range
     * against a series of invariants (non-null range, system timestamp
     * bounds, message count limit, per-message key/remoteJid/fromMe/id
     * invariants) and emits WAM critical-event metrics
     * ({@code uploadMdCriticalEventMetric}) for each failing invariant.
     * When any invariant fails the function returns {@code undefined},
     * otherwise it returns the input range unchanged.
     *
     * <p>Cobalt does not emit WAM telemetry, so the function is reduced to
     * a pass-through validator that returns {@code null} only when the
     * range itself is {@code null}. Applied mutation callers that need the
     * null-check already perform it inline via
     * {@code orElse(null)} on the action's optional {@code messageRange}.
     *
     * @implNote WAWebMessageRangeUtils.validateMessageRange - WAM telemetry
     *           emission is intentionally skipped (Cobalt does not ship
     *           WAM); the validation short-circuit on {@code null} range
     *           is preserved
     * @param messageRange the message range to validate, may be {@code null}
     * @return the same range when non-{@code null}, otherwise {@code null}
     */
    @WhatsAppWebExport(
            moduleName = "WAWebMessageRangeUtils",
            exports = "validateMessageRange",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    static SyncActionMessageRange validateMessageRange(SyncActionMessageRange messageRange) {
        if (messageRange == null) {
            return null; // ADAPTED: WAM telemetry emission skipped (not shipped in Cobalt)
        }
        // Cobalt treats the range as valid and defers field-level validation to callers.
        return messageRange;
    }

    /**
     * Returns a copy of the given message range with every message key's
     * {@code remoteJid} replaced by the given JID's string form.
     *
     * <p>Per WhatsApp Web {@code replaceMessageRangeRemoteJid}:
     * <pre>{@code
     * function E(e, t) {
     *   return {...t, messages: t.messages.map(function(m) {
     *     return {...m, key: {...m.key, remoteJid: e.toString()}};
     *   })};
     * }
     * }</pre>
     *
     * <p>Used by {@code applyMutations} in the clear-chat and delete-chat
     * handlers to swap the remote JID (which may reference the original
     * mutation index JID) for the resolved local chat JID before applying
     * the range to the chat database.
     *
     * @implNote WAWebMessageRangeUtils.replaceMessageRangeRemoteJid
     * @param remoteJid    the JID to stamp into every message key
     * @param messageRange the range whose messages should be rewritten
     * @return a new range with identical timestamps and rewritten message keys
     */
    @WhatsAppWebExport(
            moduleName = "WAWebMessageRangeUtils",
            exports = "replaceMessageRangeRemoteJid",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    static SyncActionMessageRange replaceMessageRangeRemoteJid(Jid remoteJid, SyncActionMessageRange messageRange) {
        var rewrittenMessages = new ArrayList<SyncActionMessage>(messageRange.messages().size());
        for (var msg : messageRange.messages()) {
            var existingKey = msg.key().orElse(null);
            var newKeyBuilder = new MessageKeyBuilder()
                    .parentJid(remoteJid);
            if (existingKey != null) {
                newKeyBuilder.fromMe(existingKey.fromMe());
                existingKey.id().ifPresent(newKeyBuilder::id);
                existingKey.senderJid().ifPresent(newKeyBuilder::senderJid);
            }
            var newMsgBuilder = new SyncActionMessageBuilder()
                    .key(newKeyBuilder.build());
            msg.timestamp().ifPresent(newMsgBuilder::timestamp);
            rewrittenMessages.add(newMsgBuilder.build());
        }

        var builder = new SyncActionMessageRangeBuilder()
                .messages(rewrittenMessages);
        messageRange.lastMessageTimestamp().ifPresent(builder::lastMessageTimestamp);
        messageRange.lastSystemMessageTimestamp().ifPresent(builder::lastSystemMessageTimestamp);
        return builder.build();
    }

    /**
     * Merges two message lists per WhatsApp Web {@code g} helper.
     *
     * <p>Concatenates both lists, then keeps only messages whose timestamp
     * is {@code >=} the given threshold. Among messages with the same key ID,
     * the one with the higher timestamp wins.
     *
     * @implNote WAWebMessageRangeUtils (function g - merge helper)
     * @param messagesA    the first message list
     * @param messagesB    the second message list
     * @param maxTimestamp the threshold timestamp (epoch seconds)
     * @return the merged and deduplicated message list
     */
    private static List<SyncActionMessage> mergeMessages(List<SyncActionMessage> messagesA, List<SyncActionMessage> messagesB, long maxTimestamp) {
        var byKeyId = new LinkedHashMap<String, SyncActionMessage>();
        var combined = new ArrayList<SyncActionMessage>(messagesA.size() + messagesB.size());
        combined.addAll(messagesA);
        combined.addAll(messagesB);

        for (var msg : combined) {
            var keyId = msg.key()
                    .flatMap(key -> key.id())
                    .orElse("");
            var msgTimestamp = toEpochSeconds(msg.timestamp().orElse(null));

            if (msgTimestamp >= maxTimestamp) {
                var existing = byKeyId.get(keyId);
                if (existing != null) {
                    var existingTimestamp = toEpochSeconds(existing.timestamp().orElse(null));
                    if (existingTimestamp < msgTimestamp) {
                        byKeyId.put(keyId, msg);
                    }
                } else {
                    byKeyId.put(keyId, msg);
                }
            }
        }

        return new ArrayList<>(byKeyId.values());
    }

    /**
     * Checks whether range {@code encloser} encloses range {@code enclosed}.
     *
     * <p>Per WhatsApp Web (function {@code m}): a range A encloses range B
     * when every message in B is accounted for by A. Specifically, for each
     * message in B:
     * <ul>
     *   <li>If the message has no timestamp, its key ID must be present in A's
     *       messages list
     *   <li>If the message's key ID is NOT found in A's messages, its timestamp
     *       must be strictly less than A's {@code lastMessageTimestamp}
     * </ul>
     *
     * @implNote WAWebMessageRangeUtils (function m - encloses helper)
     * @param encloser the range that should enclose
     * @param enclosed the range that should be enclosed
     * @return {@code true} if {@code encloser} encloses {@code enclosed}
     */
    private static boolean encloses(SyncActionMessageRange encloser, SyncActionMessageRange enclosed) {
        var encloserLastTimestamp = toEpochSeconds(encloser.lastMessageTimestamp().orElse(null));

        var encloserKeyIds = new HashSet<String>();
        for (var msg : encloser.messages()) {
            msg.key()
                    .flatMap(key -> key.id())
                    .ifPresent(encloserKeyIds::add);
        }

        for (var msg : enclosed.messages()) {
            var keyId = msg.key()
                    .flatMap(key -> key.id())
                    .orElse(null);
            var msgTimestamp = msg.timestamp().orElse(null);

            if (keyId != null && encloserKeyIds.contains(keyId)) {
                continue; // Message explicitly present in encloser's list
            }

            if (msgTimestamp == null) {
                return false;
            }

            if (encloserLastTimestamp <= toEpochSeconds(msgTimestamp)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts an {@link Instant} to epoch seconds, returning {@code 0}
     * for {@code null} values. Mirrors the WhatsApp Web convention of
     * treating missing timestamps as {@code 0}.
     *
     * @implNote ADAPTED: WAWebMessageRangeUtils - WA Web uses
     *           {@code WALongInt.numberOrThrowIfTooLarge(val ?? 0)}; Cobalt uses
     *           epoch seconds conversion with null coalescing to 0
     * @param instant the instant to convert, may be {@code null}
     * @return the epoch seconds, or {@code 0} if {@code null}
     */
    private static long toEpochSeconds(Instant instant) {
        return instant != null ? instant.getEpochSecond() : 0;
    }
}
