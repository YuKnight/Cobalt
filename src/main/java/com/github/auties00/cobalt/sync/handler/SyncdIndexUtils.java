package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageKey;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Utility methods for working with sync action indices and message key
 * conversions during app state synchronization.
 *
 * <p>Per WhatsApp Web {@code WAWebSyncdIndexUtils}: this module provides
 * shared helpers used by multiple sync action handlers for converting
 * between sync action index parts and {@link MessageKey} objects, as
 * well as sentinel-returning functions for malformed mutation data.
 *
 * @implNote WAWebSyncdIndexUtils
 */
final class SyncdIndexUtils {
    /**
     * Logger for sync index utilities.
     *
     * @implNote ADAPTED: WAWebSyncdIndexUtils uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(SyncdIndexUtils.class.getName()); // ADAPTED: WAWebSyncdIndexUtils — WALogger

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @implNote NO_WA_BASIS — Java utility class pattern
     */
    private SyncdIndexUtils() {
    }

    /**
     * Converts a {@link MessageKey} to its DB ID string representation, but
     * WITHOUT the {@code fromMe} and {@code participant} segments.
     *
     * <p>Per WhatsApp Web {@code msgKeyToDbIdWithoutFromMeParticipant}: calls
     * {@code toString()} on the message key. If the message was sent by the
     * current user ({@code fromMe} is {@code true}) AND the remote JID is NOT
     * a user JID (i.e., it is a group, broadcast, or newsletter), then the
     * last underscore-separated segment is stripped from the serialized key.
     * For all other cases, the full {@code toString()} value is returned.
     *
     * <p>The WA Web MsgKey serialization format is:
     * {@code fromMe_remote_id[_self][_participant]}, joined by underscores.
     * Stripping the last segment removes the participant component, enabling
     * lookups where the participant is not part of the key.
     *
     * <p>Since Cobalt's {@link MessageKey#senderJid()} has fallback semantics
     * (returns {@code parentJid} when the raw field is null), this method
     * uses {@link #serializeMessageKey(MessageKey)} for the full serialization
     * and strips the last segment when applicable.
     *
     * @implNote WAWebSyncdIndexUtils.msgKeyToDbIdWithoutFromMeParticipant
     * @param key the message key to convert
     * @return the DB ID string with the participant segment removed when applicable
     */
    static String msgKeyToDbIdWithoutFromMeParticipant(MessageKey key) {
        // WAWebSyncdIndexUtils.msgKeyToDbIdWithoutFromMeParticipant (function d)
        var serialized = serializeMessageKey(key); // WAWebSyncdIndexUtils.d: var t = e.toString()
        var remoteJid = key.parentJid().orElse(null); // WAWebSyncdIndexUtils.d: e.remote
        if (!key.fromMe() || remoteJid == null || isUserJid(remoteJid)) { // WAWebSyncdIndexUtils.d: if (!e.fromMe || e.remote.isUser()) return t
            return serialized;
        }
        var lastUnderscore = serialized.lastIndexOf('_'); // WAWebSyncdIndexUtils.d: var n = t.lastIndexOf("_")
        if (lastUnderscore < 0) {
            return serialized; // ADAPTED: defensive null check — WA Web assumes underscore always exists
        }
        return serialized.substring(0, lastUnderscore); // WAWebSyncdIndexUtils.d: return t.substring(0, n)
    }

    /**
     * Converts sync action index parts back into a {@link MessageKey}.
     *
     * <p>Per WhatsApp Web {@code syncKeyToMsgKey}: the four index parts are
     * typically {@code [chatJid, messageId, fromMe, senderJid]}. The function:
     * <ul>
     *   <li>Validates that {@code remote} is a valid WID</li>
     *   <li>Creates a Jid from {@code remote}</li>
     *   <li>If the remote is NOT a user JID and NOT a newsletter:
     *     <ul>
     *       <li>If {@code fromMe} is {@code "0"}: validates {@code participant}
     *           is a valid WID and uses it as the participant</li>
     *       <li>If {@code fromMe} is {@code "1"}: uses the current user's JID
     *           as the participant</li>
     *     </ul>
     *   </li>
     *   <li>Returns a {@link MessageKey} with the resolved fields</li>
     * </ul>
     *
     * @implNote WAWebSyncdIndexUtils.syncKeyToMsgKey
     * @param store       the store to obtain the current user's JID for group fromMe resolution
     * @param remote      the remote JID string (chat JID)
     * @param id          the message ID string
     * @param fromMe      the fromMe string ({@code "0"} or {@code "1"})
     * @param participant the participant JID string (sender in group chats)
     * @return the resolved {@link MessageKey}, or empty if the input is invalid
     */
    static Optional<MessageKey> syncKeyToMsgKey(WhatsAppStore store, String remote, String id, String fromMe, String participant) {
        // WAWebSyncdIndexUtils.syncKeyToMsgKey (function m)
        if (remote == null || remote.isEmpty()) { // WAWebSyncdIndexUtils.m: if (!r("WAWebWid").isWid(t)) return ... null
            LOGGER.warning("syncKeyToMsgKey: invalid remote value"); // WAWebSyncdIndexUtils.m: WALogger.WARN
            return Optional.empty();
        }

        Jid remoteJid;
        try {
            remoteJid = Jid.of(remote); // WAWebSyncdIndexUtils.m: var u = o("WAWebWidFactory").createWid(t)
        } catch (Exception e) {
            LOGGER.warning("syncKeyToMsgKey: invalid remote value: " + remote); // WAWebSyncdIndexUtils.m: WALogger.WARN
            return Optional.empty();
        }

        Jid participantJid = null; // WAWebSyncdIndexUtils.m: var l = void 0
        var isUser = isUserJid(remoteJid); // WAWebSyncdIndexUtils.m: u.isUser()
        var isNewsletter = remoteJid.hasNewsletterServer(); // WAWebSyncdIndexUtils.m: u.isNewsletter()
        if (!isUser && !isNewsletter) { // WAWebSyncdIndexUtils.m: if (!u.isUser() && !u.isNewsletter())
            if ("1".equals(fromMe)) { // WAWebSyncdIndexUtils.m: a === "1" ? l = o("WAWebUserPrefsMeUser").getMePnUserOrThrow_DO_NOT_USE()
                participantJid = store.jid().orElse(null); // WAWebSyncdIndexUtils.m: getMePnUserOrThrow_DO_NOT_USE()
            } else { // WAWebSyncdIndexUtils.m: a === "0" && !r("WAWebWid").isWid(i) check
                if (participant == null || participant.isEmpty()) { // WAWebSyncdIndexUtils.m: if (a === "0" && !r("WAWebWid").isWid(i))
                    LOGGER.warning("syncKeyToMsgKey: invalid participant value"); // WAWebSyncdIndexUtils.m: WALogger.WARN
                    return Optional.empty();
                }
                try {
                    participantJid = Jid.of(participant); // WAWebSyncdIndexUtils.m: l = o("WAWebWidFactory").createWid(i)
                } catch (Exception e) {
                    LOGGER.warning("syncKeyToMsgKey: invalid participant value: " + participant); // WAWebSyncdIndexUtils.m: WALogger.WARN
                    return Optional.empty();
                }
            }
        }

        // WAWebSyncdIndexUtils.m: return new(r("WAWebMsgKey"))({fromMe: a === "1", remote: u, id: n, participant: l})
        var key = new MessageKeyBuilder()
                .fromMe("1".equals(fromMe))
                .parentJid(remoteJid)
                .id(id)
                .senderJid(participantJid)
                .build();
        return Optional.of(key);
    }

    /**
     * Extracts a {@link MessageKey} from a star action's index format.
     *
     * <p>Per WhatsApp Web {@code getMsgKeyFromStarActionIndex}: parses the
     * index string as a JSON array and expects at least 5 elements. The
     * message key is constructed from elements at indices 1 through 4
     * using {@link #syncKeyToMsgKey}.
     *
     * @implNote WAWebSyncdIndexUtils.getMsgKeyFromStarActionIndex
     * @param store the store for current user JID resolution
     * @param index the JSON-encoded index string from the star action
     * @return the resolved {@link MessageKey}, or empty if the index is malformed
     */
    static Optional<MessageKey> getMsgKeyFromStarActionIndex(WhatsAppStore store, String index) {
        // WAWebSyncdIndexUtils.getMsgKeyFromStarActionIndex (function p)
        var parsed = JSON.parseArray(index); // WAWebSyncdIndexUtils.p: var t = JSON.parse(e)
        if (parsed.size() < 5) { // WAWebSyncdIndexUtils.p: if (t.length < 5) throw ...
            LOGGER.warning("[sync-action] star action index malformed, cannot create MsgKey"); // WAWebSyncdIndexUtils.p: r("err")("[sync-action] star action index malformed...")
            return Optional.empty(); // ADAPTED: WAWebSyncdIndexUtils.p throws; Cobalt returns empty
        }
        var result = syncKeyToMsgKey( // WAWebSyncdIndexUtils.p: var n = m(t[1], t[2], t[3], t[4])
                store,
                parsed.getString(1),
                parsed.getString(2),
                parsed.getString(3),
                parsed.getString(4)
        );
        if (result.isEmpty()) { // WAWebSyncdIndexUtils.p: if (!n) throw ...
            LOGGER.warning("[sync-action] star index malformed, MsgKey failed"); // WAWebSyncdIndexUtils.p: WALogger.WARN
        }
        return result;
    }

    /**
     * Returns a malformed result for an invalid action index.
     *
     * <p>Per WhatsApp Web {@code malformedActionIndex}: uploads a critical event
     * metric with code {@code ACTION_INVALID_INDEX_DATA} and returns
     * {@code {actionState: Malformed}}.
     *
     * <p>In Cobalt, the WAM metric upload is intentionally omitted (telemetry
     * is not replicated), but the return value semantics are preserved.
     *
     * @implNote WAWebSyncdIndexUtils.malformedActionIndex
     * @param collectionName the collection name for diagnostic context (WAM metric parameter)
     * @param actionName     the action name for diagnostic context (WAM metric parameter)
     * @return a {@link MutationApplicationResult} with {@code MALFORMED} state
     */
    static MutationApplicationResult malformedActionIndex(String collectionName, String actionName) {
        // WAWebSyncdIndexUtils.malformedActionIndex (function _)
        // WAWebSyncdIndexUtils._: o("WAWebSyncdMetrics").uploadMdCriticalEventMetric(...) — WAM telemetry skipped
        LOGGER.fine(() -> "malformedActionIndex: collection=" + collectionName + ", action=" + actionName); // ADAPTED: WAWebSyncdIndexUtils._ — WAM metric replaced with fine log
        return MutationApplicationResult.malformed(); // WAWebSyncdIndexUtils._: {actionState: o("WASyncdConst").SyncActionState.Malformed}
    }

    /**
     * Returns a malformed result for an invalid action value.
     *
     * <p>Per WhatsApp Web {@code malformedActionValue}: returns
     * {@code {actionState: Malformed}}. Unlike {@code malformedActionIndex},
     * this function does NOT upload a WAM metric.
     *
     * @implNote WAWebSyncdIndexUtils.malformedActionValue
     * @param collectionName the collection name for diagnostic context
     * @return a {@link MutationApplicationResult} with {@code MALFORMED} state
     */
    static MutationApplicationResult malformedActionValue(String collectionName) {
        // WAWebSyncdIndexUtils.malformedActionValue (function f)
        // WAWebSyncdIndexUtils.f: return {actionState: o("WASyncdConst").SyncActionState.Malformed}
        return MutationApplicationResult.malformed(); // WAWebSyncdIndexUtils.f
    }

    /**
     * Serializes a {@link MessageKey} to the WA Web MsgKey string format.
     *
     * <p>Per WhatsApp Web, a MsgKey serializes as its constituent fields
     * joined by underscores: {@code fromMe_remote_id[_self][_participant]}.
     * Cobalt's {@link MessageKey} is a protobuf model and does not have
     * the same {@code toString()} semantics, so this method replicates
     * the WA Web serialization logic.
     *
     * <p>Because Cobalt's {@link MessageKey#senderJid()} has fallback semantics
     * (returns {@code parentJid} when the raw sender field is null), this method
     * only appends the participant segment when the remote JID is NOT a user
     * and NOT a newsletter. Per WhatsApp Web, participant is only present on
     * group/broadcast message keys, never on user or newsletter keys.
     *
     * @implNote ADAPTED: WAWebMsgKey.prototype.toString — Cobalt MessageKey has different
     *           toString semantics and senderJid fallback, so this method replicates
     *           the WA Web serialization with explicit participant-presence logic
     * @param key the message key to serialize
     * @return the WA Web-compatible serialized string
     */
    static String serializeMessageKey(MessageKey key) {
        // WAWebMsgKey constructor: this._serialized = E.join("_")
        // where E = [this.fromMe, this.remote, this.id, ...optional self, ...optional participant]
        var sb = new StringBuilder();
        sb.append(key.fromMe()); // WAWebMsgKey: this.fromMe
        sb.append('_');
        var remoteJid = key.parentJid().orElse(null);
        sb.append(remoteJid != null ? remoteJid.toString() : ""); // WAWebMsgKey: this.remote
        sb.append('_');
        sb.append(key.id().orElse("")); // WAWebMsgKey: this.id
        // WAWebMsgKey: participant is only set for group/broadcast keys (not user/newsletter)
        // Cobalt's senderJid() has fallback to parentJid, so we must only append it
        // when the remote is NOT a user and NOT a newsletter (matching WA Web behavior)
        if (remoteJid != null && !isUserJid(remoteJid) && !remoteJid.hasNewsletterServer()) { // ADAPTED: detect participant presence via remote type
            key.senderJid().ifPresent(sender -> { // WAWebMsgKey: this.participant
                sb.append('_');
                sb.append(sender);
            });
        }
        return sb.toString();
    }

    /**
     * Checks whether the given JID is a "user" JID per WhatsApp Web's definition.
     *
     * <p>Per WhatsApp Web {@code WAWebWid.prototype.isUser}: a WID is considered
     * a user if its server is {@code c.us}, {@code lid}, {@code bot},
     * {@code hosted}, or {@code hosted.lid}. Note that {@code s.whatsapp.net}
     * is the Cobalt-canonical user server equivalent to {@code c.us}.
     *
     * @implNote ADAPTED: WAWebWid.prototype.isUser — mapped to Cobalt's Jid server checks
     * @param jid the JID to check
     * @return {@code true} if the JID belongs to a user-category server
     */
    private static boolean isUserJid(Jid jid) {
        // WAWebWid.prototype.isUser: this.server === "c.us" || this.server === "lid" || this.server === "bot" || this.server === "hosted" || this.server === "hosted.lid"
        return jid.hasUserServer()        // c.us / s.whatsapp.net
                || jid.hasLidServer()     // lid
                || jid.hasBotServer()     // bot
                || jid.hasHostedServer()  // hosted
                || jid.hasHostedLidServer(); // hosted.lid
    }
}
