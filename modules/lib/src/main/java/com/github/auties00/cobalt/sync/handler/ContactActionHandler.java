package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.OrphanMutationEntry;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.contact.ContactAction;
import com.github.auties00.cobalt.model.sync.action.contact.ContactActionBuilder;
import com.github.auties00.cobalt.model.sync.action.contact.UserStatusMuteAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Handles contact sync actions from the {@code critical_unblock_low} collection.
 *
 * <p>This handler processes mutations that synchronize address-book contact information
 * (first name, full name, username, LID mapping) across linked devices. It processes
 * both {@code SET} operations (create or update a contact) and {@code REMOVE} operations
 * (mark a contact as no longer in the user's address book).
 *
 * <p>Index format: {@code ["contact", contactJid]}.
 *
 * @implNote WAWebContactSync.default — singleton instance of the contact sync action
 *           handler extending {@code AccountSyncdActionBase}
 */
@WhatsAppWebModule(moduleName = "WAWebContactSync")
public final class ContactActionHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of this handler.
     *
     * @implNote WAWebContactSync — module-level singleton {@code v = new b()}
     */
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final ContactActionHandler INSTANCE = new ContactActionHandler(); // WAWebContactSync: var v = new b(); l.default = v

    /**
     * Logger for diagnostic messages emitted during contact sync processing.
     *
     * @implNote ADAPTED: WAWebContactSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(ContactActionHandler.class.getName()); // ADAPTED: WAWebContactSync — WALogger

    /**
     * Compiled pattern matching any Unicode whitespace character.
     *
     * <p>Used by {@link #deriveShortName(String)} to split a full name into words,
     * matching the WA Web {@code WAWebContactShortName.getShortName} behavior which
     * splits on {@code /\s/}.
     *
     * @implNote ADAPTED: WAWebContactShortName.getShortName — JS regex {@code /\s/} mapped
     *           to Java's {@code \s} pattern
     */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s"); // ADAPTED: WAWebContactShortName.getShortName — JS split(/\s/)

    /**
     * Private constructor preventing external instantiation.
     *
     * @implNote WAWebContactSync — class {@code b} constructor sets
     *           {@code collectionName = CriticalUnblockLow}
     */
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private ContactActionHandler() {

    }

    /**
     * Returns the action name for this handler.
     *
     * @implNote WAWebContactSync.getAction — returns {@code WASyncdConst.Actions.Contact}
     *           (value: {@code "contact"})
     * @return the action name string
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return ContactAction.ACTION_NAME; // WAWebContactSync.getAction
    }

    /**
     * Returns the sync collection this handler belongs to.
     *
     * @implNote WAWebContactSync — constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.CriticalUnblockLow}
     * @return the sync patch type
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return ContactAction.COLLECTION_NAME; // WAWebContactSync.collectionName
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebContactSync.getVersion — returns {@code 2}
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return ContactAction.ACTION_VERSION; // WAWebContactSync.getVersion
    }

    /**
     * Applies a single contact mutation and returns whether it succeeded.
     *
     * @implNote WAWebContactSync.applyMutations — per-mutation logic within the batch
     *           handler, delegating to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebContactSync.applyMutations
    }

    /**
     * Applies a single contact mutation and returns a detailed result.
     *
     * <p>For {@code SET} operations, this method:
     * <ul>
     *   <li>Validates the mutation value is a {@link ContactAction}</li>
     *   <li>Extracts and validates the contact JID from the index</li>
     *   <li>Skips LID contacts (returns {@code SKIPPED})</li>
     *   <li>Creates or updates the contact with full name, short name, username, and LID</li>
     *   <li>Retries orphan status mute mutations for the contact</li>
     * </ul>
     *
     * <p>For {@code REMOVE} operations, this method:
     * <ul>
     *   <li>Skips LID and bot contacts (returns {@code SKIPPED})</li>
     *   <li>Clears the contact's name and username fields</li>
     * </ul>
     *
     * @implNote WAWebContactSync.applyMutations — per-mutation processing within the
     *           batch {@code applyMutations(t, a, i)} method
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index()); // WAWebContactSync.applyMutations: var n = t.indexParts
        var contactJidString = indexArray.getString(1); // WAWebContactSync.applyMutations: var a = n[1]
        if (contactJidString == null || contactJidString.isEmpty()) { // WAWebContactSync.applyMutations: if (r("isStringNullOrEmpty")(a))
            return malformedActionIndex(); // WAWebContactSync.applyMutations: return l.malformedActionIndex()
        }

        var contactJid = Jid.of(contactJidString); // WAWebContactSync.applyMutations: var i = o("WAWebWidFactory").createUserWidOrThrow(a)
        var usernameEnabled = client.abPropsService() // WAWebContactSync.applyMutations: var h = o("WAWebUsernameGatingUtils").usernameContactSyncdEnabled()
                .getBool(ABProp.USERNAME_CONTACT_SYNCD_SUPPORT_ENABLE);

        switch (mutation.operation()) {
            case SET -> { // WAWebContactSync.applyMutations: if (t.operation === "set")
                if (!(mutation.value().action().orElse(null) instanceof ContactAction action)) { // WAWebContactSync.applyMutations: var u = t.value.contactAction; if (!u)
                    return malformedActionValue(); // WAWebContactSync.applyMutations: return ... malformedActionValue(l.collectionName)
                }

                if (contactJid.hasLidServer()) { // WAWebContactSync.applyMutations: if (i.isLid())
                    return MutationApplicationResult.skipped(); // WAWebContactSync.applyMutations: return {actionState: Skipped}
                }

                var contact = client.store() // WAWebContactSync.applyMutations: y.push(f) then createOrMergeAddressBookContacts(y)
                        .findContactByJid(contactJid)
                        .orElseGet(() -> client.store().addNewContact(contactJid));
                var fullName = action.fullName().orElse(""); // WAWebContactSync.applyMutations: var d = u.fullName; f.name = d != null ? d : ""
                contact.setFullName(fullName);
                var shortName = action.firstName() // WAWebContactSync.applyMutations: var s = c != null ? c : o("WAWebContactShortName").getShortName(d); f.shortName = s != null ? s : ""
                        .orElseGet(() -> deriveShortName(fullName));
                contact.setShortName(shortName);

                if (usernameEnabled) { // WAWebContactSync.applyMutations: h && !r("isStringNullOrEmpty")(_)
                    action.username() // WAWebContactSync.applyMutations: var _ = u.username
                            .filter(u -> !u.isEmpty()) // WAWebContactSync.applyMutations: !r("isStringNullOrEmpty")(_)
                            .map(u -> u.startsWith("@") ? u.substring(1) : u) // WAWebContactSync.applyMutations: _.startsWith("@") ? _.slice(1) : _
                            .ifPresent(contact::setUsername); // WAWebContactSync.applyMutations: f.username = ...
                }

                // WAWebContactSync.applyMutations:
                //   var g = m != null ? asUserLidOrThrow(createUserWidOrThrow(m, "lid")) : null;
                //   S.set(i, g != null ? g : WAWebLidMigrationUtils.toLid(i));
                //   if (i.isRegularUserPn() && g) v.push({lid: C, pn: i});
                //   ... yield createLidPnMappings({mappings: v, flushImmediately: true, learningSource: "other"})
                // ADAPTED: Cobalt's store is keyed by canonical JID, so the dual-key map S is not mirrored.
                // The createLidPnMappings call is gated on `contactJid.hasUserServer() && lidJid != null`,
                // matching WA Web's `isRegularUserPn() && g` condition.
                action.lidJid().ifPresent(lid -> { // WAWebContactSync.applyMutations: var m = u.lidJid
                    contact.setLid(lid); // WAWebContactSync.applyMutations: S.set(i, g); later ContactCollection.add for LID record
                    if (contactJid.hasUserServer()) { // WAWebContactSync.applyMutations: if (i.isRegularUserPn() && g)
                        client.store().registerLidMapping(contactJid, lid); // WAWebContactSync.applyMutations: createLidPnMappings({mappings: v, ...})
                    }
                });

                // WAWebContactSync.applyMutations: o("WAWebSyncContactsJob").syncNewContact(i)
                // SKIPPED: debounced background contact sync refresh; not mirrored in Cobalt.
                retryOrphanStatusMutes(client, contactJidString); // WAWebContactSync.applyMutations: o("WAWebSyncdOrphan").checkOrphanUserStatusMutes(y.map(...))

                return MutationApplicationResult.success(); // WAWebContactSync.applyMutations: {actionState: Success}
            }
            case REMOVE -> { // WAWebContactSync.applyMutations: t.operation === "remove"
                if (contactJid.hasLidServer() || contactJid.hasBotServer()) { // WAWebContactSync.applyMutations: i.isLid() || i.isBot() ? Skipped
                    return MutationApplicationResult.skipped(); // WAWebContactSync.applyMutations: {actionState: Skipped}
                }

                // WAWebContactSync.applyMutations:
                //   b.push(i)                                                                 // per-mutation: queue removal
                //   var I = []; if (h) { D = bulkGet(T); D.forEach((e,t) => { (e==null || e.isUsernameContact!==true) && I.push(b[t]) }) } else I = b;
                //   if (I.length>0) { yield setNotAddressBookContacts(x); ... }
                //   I.forEach(e => { var t = ContactCollection.get(e); t && t.setNotMyContact(); ... })
                // ADAPTED: Cobalt adapts the batch-level bulkGet + filter to a per-mutation
                // check. When username contacts gating is enabled, contacts marked
                // isUsernameContact (addedByUsername) are skipped; otherwise the contact's
                // address-book fields are cleared (setNotMyContact).
                var contact = client.store().findContactByJid(contactJid); // WAWebContactSync.applyMutations: ContactCollection.get(e)
                if (contact.isPresent()) { // WAWebContactSync.applyMutations: t && t.setNotMyContact()
                    if (usernameEnabled && contact.get().isAddedByUsername()) { // WAWebContactSync.applyMutations: h && D[t].isUsernameContact === true -> skip I.push
                        return MutationApplicationResult.success(); // WAWebContactSync.applyMutations: {actionState: Success}
                    }
                    contact.get().setFullName(null); // WAWebContactSync.applyMutations: setNotMyContact clears contact fields
                    contact.get().setShortName(null); // WAWebContactSync.applyMutations: setNotMyContact clears contact fields
                    contact.get().setUsername(null); // WAWebContactSync.applyMutations: setNotMyContact clears contact fields
                }
                return MutationApplicationResult.success(); // WAWebContactSync.applyMutations: {actionState: Success}
            }
            default -> { // WAWebContactSync.applyMutations: else { E++ ... Unsupported }
                return MutationApplicationResult.unsupported(); // WAWebContactSync.applyMutations: {actionState: Unsupported}
            }
        }
    }

    /**
     * Builds a pending mutation for syncing a contact to the server.
     *
     * <p>For {@code SET} operations (when {@code isDelete} is {@code false}), the mutation
     * includes the contact's full name, first name, LID JID, address book sync preference,
     * and username. For {@code REMOVE} operations (when {@code isDelete} is {@code true}),
     * only the operation type is set.
     *
     * <p>The contact JID is serialized in legacy format for the index, matching
     * WhatsApp Web's use of {@code e.toString({legacy: true})}.
     *
     * @implNote WAWebContactSync.getContactSyncMutation — builds a pending mutation via
     *           {@code WAWebSyncdActionUtils.buildPendingMutation} with the contact's
     *           action fields and index arguments
     * @param contactJid          the JID of the contact being synced
     * @param firstName           the contact's first name, or {@code null}
     * @param fullName            the contact's full name, or {@code null}
     * @param isDelete            whether this is a delete (REMOVE) operation
     * @param lid                 the contact's LID JID, or {@code null}
     * @param syncToAddressbook   whether to sync to primary address book, or {@code null}
     * @param username            the contact's username, or {@code null}
     * @return the pending mutation ready for submission to the sync pipeline
     */
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getContactSyncMutation(
            Jid contactJid,
            String firstName,
            String fullName,
            boolean isDelete,
            Jid lid,
            Boolean syncToAddressbook,
            String username
    ) {
        var timestamp = Instant.now(); // WAWebContactSync.getContactSyncMutation: var u = o("WATimeUtils").unixTimeMs()
        var action = new ContactActionBuilder() // WAWebContactSync.getContactSyncMutation: var c = {contactAction: {fullName: ..., firstName: ..., ...}}
                .fullName(fullName) // WAWebContactSync.getContactSyncMutation: fullName: r != null ? r : void 0
                .firstName(firstName) // WAWebContactSync.getContactSyncMutation: firstName: n != null ? n : void 0
                .lidJid(lid) // WAWebContactSync.getContactSyncMutation: lidJid: i ? i.toString() : void 0
                .saveOnPrimaryAddressbook(syncToAddressbook) // WAWebContactSync.getContactSyncMutation: saveOnPrimaryAddressbook: l != null ? l : void 0
                .username(username) // WAWebContactSync.getContactSyncMutation: username: s
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: u
                .contactAction(action) // WAWebContactSync.getContactSyncMutation: value: c (contains contactAction)
                .build();
        var operation = isDelete // WAWebContactSync.getContactSyncMutation: operation: a ? REMOVE : SET
                ? SyncdOperation.REMOVE
                : SyncdOperation.SET;
        // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs))
        // where indexArgs = [e.toString({legacy: true})] in WA Web.
        // ADAPTED: Cobalt uses Jid.toString() canonical form; WA Web's legacy form is not mirrored
        // because Cobalt normalizes JIDs to a single canonical representation.
        var index = JSON.toJSONString(List.of(actionName(), contactJid.toString()));
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, ... }
                index,
                value,
                operation,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }

    /**
     * Retries orphan status mute mutations that may have been blocked by the
     * absence of the specified contact.
     *
     * <p>When a contact is created or updated via a SET mutation, any previously
     * orphaned {@code userStatusMute} mutations referencing that contact's JID
     * are re-applied. Successfully applied orphan mutations are removed from
     * the store.
     *
     * @implNote WAWebContactSync.applyMutations —
     *           {@code o("WAWebSyncdOrphan").checkOrphanUserStatusMutes(y.map(...))}
     *           retrieves and re-applies orphan status mute mutations by model ID.
     *           WA Web catches errors with {@code .catch()} and logs; Cobalt uses
     *           try/catch and logs via java.util.logging.
     * @param client           the WhatsApp client instance
     * @param contactJidString the JID string of the contact to check orphans for
     */
    @WhatsAppWebExport(moduleName = "WAWebContactSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private void retryOrphanStatusMutes(WhatsAppClient client, String contactJidString) {
        // WAWebContactSync.applyMutations: o("WAWebSyncdOrphan").checkOrphanUserStatusMutes(y.map(function(e) { return e.id }))
        try {
            var entries = client.store().findOrphanMutationsByModel(UserStatusMuteAction.COLLECTION_NAME, contactJidString); // WAWebSyncdOrphan.checkOrphanUserStatusMutes -> getSyncActionsByModelInfosInTransaction
            if (entries.isEmpty()) {
                return;
            }

            var applied = new ArrayList<OrphanMutationEntry>(); // NO_WA_BASIS — Cobalt orphan retry bookkeeping
            for (var entry : entries) { // WAWebSyncdOrphan.checkOrphanUserStatusMutes -> applyIndividualMutations
                var orphanMutation = new DecryptedMutation.Trusted(
                        entry.index(),
                        entry.value(),
                        entry.operation(),
                        entry.timestamp(),
                        entry.actionVersion()
                );
                var result = UserStatusMuteHandler.INSTANCE.applyMutationResult(client, orphanMutation); // WAWebSyncdOrphan -> WAWebSyncdCollectionHandler.applyIndividualMutations
                if (result.actionState() == SyncActionState.SUCCESS) {
                    applied.add(entry);
                }
            }

            if (!applied.isEmpty()) {
                client.store().removeOrphanMutations(UserStatusMuteAction.COLLECTION_NAME, applied); // WAWebSyncdOrphan — cleanup applied orphans
            }
        } catch (Exception e) {
            // WAWebContactSync.applyMutations: .catch(function() { o("WALogger").ERROR(...).sendLogs(...) })
            LOGGER.warning("[syncd] contact: orphan status mutes check failed: " + e.getMessage()); // ADAPTED: WAWebContactSync.applyMutations — WALogger.ERROR replaced with j.u.l WARNING
        }
    }

    /**
     * Derives a short name from a full name by extracting the first word,
     * matching WhatsApp Web's {@code WAWebContactShortName.getShortName} logic.
     *
     * <p>The algorithm splits the full name on any Unicode whitespace character
     * (matching WA Web's JS {@code /\s/} regex), takes the first token, and
     * checks whether it contains at least one Unicode letter character (matching
     * the {@code WAWebAlphaRegex} character class). If the first token is
     * non-empty and contains a letter, it is returned; otherwise an empty string
     * is returned.
     *
     * @implNote WAWebContactShortName.getShortName — splits on {@code /\s/}, takes
     *           first token, tests against {@code WAWebAlphaRegex}, returns
     *           {@code asMaybeNonEmptyString(a)} or {@code null} (mapped to {@code ""})
     * @param fullName the full name to derive from
     * @return the first word of the full name, or an empty string if the name
     *         is blank or the first token does not contain a letter character
     */
    @WhatsAppWebExport(moduleName = "WAWebContactShortName", exports = "getShortName", adaptation = WhatsAppAdaptation.ADAPTED)
    static String deriveShortName(String fullName) {
        if (fullName == null || fullName.isEmpty()) { // WAWebContactShortName.getShortName: if (t == null) return null
            return ""; // ADAPTED: WA Web returns null, but caller coalesces to ""
        }
        var tokens = WHITESPACE_PATTERN.split(fullName, 2); // WAWebContactShortName.getShortName: var n = t.split(/\s/)
        var firstToken = tokens[0]; // WAWebContactShortName.getShortName: var a = n[0]
        if (firstToken.isEmpty()) { // WAWebContactShortName.getShortName: asMaybeNonEmptyString(a) returns null for empty
            return ""; // ADAPTED: WA Web returns null, but caller coalesces to ""
        }
        if (!containsLetter(firstToken)) { // WAWebContactShortName.getShortName: r("WAWebAlphaRegex").exec(a)
            return ""; // WAWebContactShortName.getShortName: WALogger.LOG(...), null -> coalesced to ""
        }
        return firstToken; // WAWebContactShortName.getShortName: o("WAWebNonEmptyString").asMaybeNonEmptyString(a)
    }

    /**
     * Checks whether the given string contains at least one Unicode letter character.
     *
     * <p>This is equivalent to testing whether the WA Web {@code WAWebAlphaRegex}
     * regex (a comprehensive Unicode letter character class) matches any character
     * in the string. Java's {@code Character.isLetter()} covers the same Unicode
     * categories (L* general category), so a simple code-point scan is sufficient.
     *
     * @implNote ADAPTED: WAWebAlphaRegex.exec(a) — WA Web uses a regex character class
     *           covering Unicode letters; Java's {@code Character.isLetter()} covers
     *           the same Unicode general categories
     * @param s the string to test
     * @return {@code true} if the string contains at least one Unicode letter
     */
    @WhatsAppWebExport(moduleName = "WAWebAlphaRegex", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean containsLetter(String s) {
        // WAWebAlphaRegex: regex character class for Unicode letters — exec() checks if any char matches
        return s.codePoints().anyMatch(Character::isLetter); // ADAPTED: WAWebAlphaRegex.exec(a) — regex match mapped to codePoint scan
    }
}
